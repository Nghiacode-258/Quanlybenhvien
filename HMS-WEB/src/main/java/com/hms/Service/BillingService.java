package com.hms.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.Entity.Invoice;
import com.hms.Repository.Db;
import com.hms.Repository.ExaminationRepository;
import com.hms.Repository.InvoiceRepository;
import com.hms.Repository.Labels;
import com.hms.Repository.StaffRepository;
import com.hms.dto.RevenueReportDto;

/** Viện phí, thu tiền và báo cáo doanh thu — lưu trên Supabase (bảng phieuthuvienphi). */
@Service
public class BillingService {

    private final InvoiceRepository invoices;
    private final ExaminationRepository exams;
    private final StaffRepository staff;

    public BillingService(InvoiceRepository invoices, ExaminationRepository exams, StaffRepository staff) {
        this.invoices = invoices;
        this.exams = exams;
        this.staff = staff;
    }

    public List<Invoice> getAllInvoices() {
        return invoices.findAll();
    }

    public Invoice getInvoiceById(String id) {
        return invoices.findById(id);
    }

    public List<Invoice> searchInvoices(String keyword, String status) {
        return filterInvoices(keyword, status, null, null, null, null, null);
    }

    public List<Invoice> filterInvoices(String keyword, String status, String viewType, Integer year, Integer month,
                                        String date, String selectedKey) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        return getAllInvoices().stream()
                .filter(inv -> {
                    if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status) && !status.equalsIgnoreCase(inv.getTrangThai())) {
                        return false;
                    }
                    if (!kw.isEmpty()) {
                        String target = (inv.getIdHoadon() + " " + inv.getIdPhieukham() + " "
                                + (inv.getPatientName() != null ? inv.getPatientName() : "") + " "
                                + (inv.getIdBn() != null ? inv.getIdBn() : "")).toLowerCase();
                        if (!target.contains(kw)) return false;
                    }
                    String effDate = inv.getEffectiveDate();
                    if (selectedKey != null && !selectedKey.trim().isEmpty()) return effDate.startsWith(selectedKey.trim());
                    if ("day".equalsIgnoreCase(viewType) && date != null && !date.trim().isEmpty()) return effDate.equals(date.trim());
                    if ("year".equalsIgnoreCase(viewType) && year != null) return effDate.startsWith(String.format("%04d", year));
                    if ("month".equalsIgnoreCase(viewType) && year != null && month != null) {
                        return effDate.startsWith(String.format("%04d-%02d", year, month));
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    public boolean payInvoice(String invoiceId, String method, String paymentDate) {
        return payInvoice(invoiceId, method, paymentDate, null);
    }

    /** Thu tiền hóa đơn; cashierId = nhân viên thu ngân đang đăng nhập (có thể null). */
    @Transactional
    public synchronized boolean payInvoice(String invoiceId, String method, String paymentDate, String cashierId) {
        Invoice inv = invoices.findById(invoiceId);
        if (inv == null || inv.isPaid()) return false;

        String code = Labels.PAYMENT.code(method, "TIEN_MAT");
        LocalDate d = Db.parseDate(paymentDate, "Ngày thu");
        LocalDateTime paidAt = (d == null ? LocalDate.now() : d).atTime(LocalTime.now());
        String cashier = (cashierId != null && staff.staffExists(cashierId)) ? cashierId : null;

        if (!invoices.markPaid(invoiceId, code, Db.sqlTs(paidAt), cashier)) {
            return false;
        }

        // Đã thu hết các hóa đơn của phiếu khám và bác sĩ đã khám xong => phiếu khám hoàn tất
        String examId = inv.getIdPhieukham();
        if (invoices.countPendingByExam(examId) == 0) {
            String cur = exams.currentStatusCode(examId);
            if ("CHO_THANH_TOAN".equals(cur) || "DA_KHAM".equals(cur)) {
                exams.updateStatus(examId, "HOAN_TAT");
            }
        }
        return true;
    }

    public long calculateTotalRevenue() {
        return invoices.totalPaid();
    }

    public long calculatePendingRevenue() {
        return invoices.totalPending();
    }

    /** Dữ liệu tổng hợp doanh thu + biểu đồ tương tác theo ngày / tháng / năm. */
    public RevenueReportDto getRevenueReport(String viewType, Integer year, Integer month, String date, String selectedKey) {
        LocalDate today = LocalDate.now();
        String vt = (viewType != null && !viewType.trim().isEmpty()) ? viewType.trim().toLowerCase() : "month";
        int y = (year != null && year > 1900) ? year : today.getYear();
        int m = (month != null && month >= 1 && month <= 12) ? month : today.getMonthValue();
        String d = (date != null && !date.trim().isEmpty()) ? date.trim() : today.toString();
        String selKey = selectedKey != null ? selectedKey.trim() : "";

        RevenueReportDto report = new RevenueReportDto();
        report.setViewType(vt);
        report.setYear(y);
        report.setMonth(m);
        report.setDate(d);
        report.setSelectedKey(selKey);

        List<Invoice> all = getAllInvoices();

        if ("year".equals(vt)) {
            for (int idx = 1; idx <= 12; idx++) {
                String key = String.format("%04d-%02d", y, idx);
                addBucket(report, all, "Tháng " + idx, key, inv -> inv.getEffectiveDate().startsWith(key));
            }
            String prefix = !selKey.isEmpty() ? selKey : String.format("%04d", y);
            setTotals(report, all.stream().filter(inv -> inv.getEffectiveDate().startsWith(prefix)).collect(Collectors.toList()));

        } else if ("day".equals(vt)) {
            LocalDate target;
            try {
                target = LocalDate.parse(d);
            } catch (Exception e) {
                target = today;
            }
            for (int offset = -3; offset <= 3; offset++) {
                LocalDate cur = target.plusDays(offset);
                String key = cur.toString();
                addBucket(report, all, cur.getDayOfMonth() + "/" + cur.getMonthValue(), key, inv -> key.equals(inv.getEffectiveDate()));
            }
            String active = !selKey.isEmpty() ? selKey : target.toString();
            setTotals(report, all.stream().filter(inv -> active.equals(inv.getEffectiveDate())).collect(Collectors.toList()));

        } else {
            int daysInMonth = YearMonth.of(y, m).lengthOfMonth();
            for (int day = 1; day <= daysInMonth; day++) {
                String key = String.format("%04d-%02d-%02d", y, m, day);
                addBucket(report, all, String.valueOf(day), key, inv -> key.equals(inv.getEffectiveDate()));
            }
            if (!selKey.isEmpty()) {
                setTotals(report, all.stream().filter(inv -> selKey.equals(inv.getEffectiveDate())).collect(Collectors.toList()));
            } else {
                String monthKey = String.format("%04d-%02d", y, m);
                setTotals(report, all.stream().filter(inv -> inv.getEffectiveDate().startsWith(monthKey)).collect(Collectors.toList()));
            }
        }
        return report;
    }

    private static void addBucket(RevenueReportDto report, List<Invoice> all, String label, String key, Predicate<Invoice> in) {
        List<Invoice> bucket = all.stream().filter(in).collect(Collectors.toList());
        report.getChartLabels().add(label);
        report.getChartPaidData().add(bucket.stream().filter(Invoice::isPaid).mapToLong(Invoice::getSoTien).sum());
        report.getChartDueData().add(bucket.stream().filter(i -> !i.isPaid()).mapToLong(Invoice::getSoTien).sum());
        report.getChartKeys().add(key);
    }

    private static void setTotals(RevenueReportDto report, List<Invoice> list) {
        report.setTotalPaid(list.stream().filter(Invoice::isPaid).mapToLong(Invoice::getSoTien).sum());
        report.setTotalDue(list.stream().filter(i -> !i.isPaid()).mapToLong(Invoice::getSoTien).sum());
        report.setTotalCount(list.size());
    }
}
