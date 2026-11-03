package com.hms.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.Entity.Examination;
import com.hms.Entity.Prescription;
import com.hms.Entity.Staff;
import com.hms.Repository.Db;
import com.hms.Repository.ExaminationRepository;
import com.hms.Repository.IdGenerator;
import com.hms.Repository.InvoiceRepository;
import com.hms.Repository.LabResultRepository;
import com.hms.Repository.PatientRepository;
import com.hms.Repository.PharmacyRepository;
import com.hms.Repository.PrescriptionRepository;
import com.hms.Repository.StaffRepository;
import com.hms.dto.DoctorExamDto;
import com.hms.dto.ExamFormDto;

/** Phiếu khám, chỉ định xét nghiệm, kê đơn thuốc và hóa đơn đi kèm — lưu trên Supabase. */
@Service
public class ExaminationService {

    private final ExaminationRepository exams;
    private final LabResultRepository labs;
    private final PrescriptionRepository prescriptions;
    private final InvoiceRepository invoices;
    private final PatientRepository patients;
    private final StaffRepository staff;
    private final PharmacyRepository pharmacy;
    private final IdGenerator ids;

    public ExaminationService(ExaminationRepository exams, LabResultRepository labs, PrescriptionRepository prescriptions,
                              InvoiceRepository invoices, PatientRepository patients, StaffRepository staff,
                              PharmacyRepository pharmacy, IdGenerator ids) {
        this.exams = exams;
        this.labs = labs;
        this.prescriptions = prescriptions;
        this.invoices = invoices;
        this.patients = patients;
        this.staff = staff;
        this.pharmacy = pharmacy;
        this.ids = ids;
    }

    public List<Examination> getAllExaminations() {
        return exams.findAll();
    }

    public Examination getExaminationById(String id) {
        return exams.findById(id);
    }

    public List<Examination> searchExaminations(String keyword, String status, String roomId) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        return getAllExaminations().stream()
                .filter(ex -> {
                    if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status) && !status.equalsIgnoreCase(ex.getTrangThai())) {
                        return false;
                    }
                    if (roomId != null && !roomId.isEmpty() && !"all".equalsIgnoreCase(roomId) && !roomId.equalsIgnoreCase(ex.getIdPk())) {
                        return false;
                    }
                    if (kw.isEmpty()) return true;
                    String target = (ex.getIdPhieukham() + " " + ex.getPatientName() + " " + ex.getIdBn() + " "
                            + (ex.getRoomName() != null ? ex.getRoomName() : "") + " "
                            + (ex.getDoctorName() != null ? ex.getDoctorName() : "")).toLowerCase();
                    return target.contains(kw);
                })
                .collect(Collectors.toList());
    }

    /** Đăng ký lượt khám: tạo phiếu khám + hóa đơn "Phí khám" chưa thanh toán. */
    @Transactional
    public synchronized Examination createExamination(ExamFormDto dto) {
        if (Db.blank(dto.getIdBn()) || !patients.exists(dto.getIdBn().trim())) {
            throw new IllegalArgumentException("Bệnh nhân không tồn tại: " + dto.getIdBn());
        }
        if (Db.blank(dto.getIdPk()) || staff.findRoomById(dto.getIdPk().trim()) == null) {
            throw new IllegalArgumentException("Phòng khám không tồn tại: " + dto.getIdPk());
        }
        if (Db.blank(dto.getIdNv()) || !staff.doctorExists(dto.getIdNv().trim())) {
            throw new IllegalArgumentException("Bác sĩ không tồn tại hoặc nhân sự được chọn không phải bác sĩ: " + dto.getIdNv());
        }
        String idBn = dto.getIdBn().trim();
        LocalDate today = LocalDate.now();
        LocalDate ngayDky = Db.parseDate(dto.getNgayDky(), "Ngày đăng ký");
        if (ngayDky == null) ngayDky = today;
        LocalTime gio = Db.parseTime(dto.getThoiGianKham(), "Giờ khám");
        long fee = dto.getPhiKham() != null ? dto.getPhiKham() : 150000L;
        if (fee < 0) throw new IllegalArgumentException("Phí khám không được âm.");

        String examId = exams.nextId();
        String benhAn = patients.ensureOpenRecord(idBn, today);
        String soBhyt = patients.validInsuranceNumber(idBn, ngayDky);

        try {
            exams.insert(examId, benhAn, dto.getIdPk().trim(), dto.getIdNv().trim(), soBhyt,
                    ngayDky.atTime(LocalTime.now()), gio == null ? null : ngayDky.atTime(gio), dto.getTrieuChung(), fee);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Không thể tạo phiếu khám: dữ liệu không hợp lệ.");
        }
        invoices.insertPending(invoices.nextId(), examId, fee, "Phí khám");
        return exams.findById(examId);
    }

    /** Bác sĩ lưu kết quả khám: chẩn đoán, chỉ định xét nghiệm, đơn thuốc; cập nhật trạng thái và hóa đơn. */
    @Transactional
    public synchronized Examination updateDoctorExamination(DoctorExamDto dto) {
        String examId = dto.getIdPhieukham();
        if (examId == null || exams.findById(examId) == null) return null;

        LocalDate today = LocalDate.now();
        LocalDate ngayDky = exams.registrationDate(examId);
        LocalTime gio = Db.parseTime(dto.getThoiGianKham(), "Giờ khám");
        LocalDateTime examAt = ExaminationRepository.combine(ngayDky != null ? ngayDky : today, gio);

        String doctorId = Db.trimToNull(dto.getIdNv());
        if (doctorId != null && !staff.doctorExists(doctorId)) {
            throw new IllegalArgumentException("Bác sĩ không tồn tại: " + doctorId);
        }
        exams.updateClinical(examId, examAt, dto.getTrieuChung() != null ? dto.getTrieuChung().trim() : "",
                dto.getKetLuan() != null ? dto.getKetLuan().trim() : "", doctorId);

        // 1. Chẩn đoán chính
        String idCd = Db.trimToNull(dto.getIdCd());
        if (idCd != null && !staff.diagnosisExists(idCd)) {
            throw new IllegalArgumentException("Mã chẩn đoán ICD không tồn tại: " + idCd);
        }
        exams.replacePrimaryDiagnosis(examId, idCd);

        // 2. Chỉ định xét nghiệm
        Set<String> chosenLabs = new LinkedHashSet<>();
        if (dto.getLabIds() != null) {
            for (String l : dto.getLabIds()) if (!Db.blank(l)) chosenLabs.add(l.trim());
        }
        labs.deletePendingNotIn(examId, chosenLabs);
        Set<String> existingLabs = labs.findByExam(examId).stream().map(l -> l.getIdLxn()).collect(Collectors.toSet());
        for (String labId : chosenLabs) {
            if (existingLabs.contains(labId)) continue;
            try {
                labs.insertPending(labs.nextId(), examId, labId, Db.sqlTs(LocalDateTime.now()));
            } catch (DataIntegrityViolationException e) {
                throw new IllegalArgumentException("Loại xét nghiệm không tồn tại: " + labId);
            }
        }

        // 3. Đơn thuốc
        saveMedicines(examId, dto, today);

        // 4. Trạng thái + hóa đơn (phiếu đã hoàn tất thanh toán thì giữ nguyên)
        String cur = exams.currentStatusCode(examId);
        if (!"HOAN_TAT".equals(cur)) {
            exams.updateStatus(examId, labs.countPending(examId) > 0 ? "CHO_XET_NGHIEM" : "CHO_THANH_TOAN");
            syncInvoice(examId);
        }
        return exams.findById(examId);
    }

    private static final class Line {
        int qty;
        String dose, usage, note;
    }

    private void saveMedicines(String examId, DoctorExamDto dto, LocalDate today) {
        List<String> medIds = dto.getMedicineId();
        boolean hasMedicines = medIds != null && medIds.stream().anyMatch(id -> !Db.blank(id));
        Prescription rx = prescriptions.findByExamId(examId);
        // Đơn đã cấp phát thì kho đã bị trừ => không sửa chi tiết đơn nữa
        if (rx != null && "Đã cấp phát".equalsIgnoreCase(rx.getTrangThai())) return;

        if (!hasMedicines) {
            if (rx != null) prescriptions.deleteItems(rx.getIdDt());
            return;
        }

        // Gộp các dòng trùng thuốc (khóa chính chitietdonthuoc là (id_dt, id_thuoc))
        Map<String, Line> lines = new LinkedHashMap<>();
        for (int i = 0; i < medIds.size(); i++) {
            String mId = medIds.get(i);
            if (Db.blank(mId)) continue;
            mId = mId.trim();
            int q = (dto.getQuantity() != null && i < dto.getQuantity().size() && dto.getQuantity().get(i) != null) ? dto.getQuantity().get(i) : 1;
            if (q < 1) q = 1;
            Line ln = lines.computeIfAbsent(mId, k -> new Line());
            ln.qty += q;
            if (ln.dose == null) ln.dose = at(dto.getDosage(), i);
            if (ln.usage == null) ln.usage = at(dto.getUsage(), i);
            if (ln.note == null) ln.note = at(dto.getMedicineNote(), i);
        }
        for (String mId : lines.keySet()) {
            if (pharmacy.findMedicineById(mId) == null) throw new IllegalArgumentException("Thuốc [" + mId + "] không tồn tại.");
        }

        LocalDate ngayKe = Db.parseDate(dto.getRxNgayKe(), "Ngày kê đơn");
        if (ngayKe == null) ngayKe = today;
        String rxId;
        if (rx == null) {
            rxId = prescriptions.nextId();
            prescriptions.insert(rxId, examId, ngayKe, dto.getRxGhiChu());
        } else {
            rxId = rx.getIdDt();
            prescriptions.updateHeader(rxId, ngayKe, dto.getRxGhiChu());
            prescriptions.deleteItems(rxId);
        }
        for (Map.Entry<String, Line> e : lines.entrySet()) {
            Line ln = e.getValue();
            long price = pharmacy.currentUnitPrice(e.getKey(), today);
            prescriptions.insertItem(rxId, e.getKey(), ln.qty, ln.dose, ln.usage, ln.note, price);
        }
    }

    private static String at(List<String> list, int i) {
        return (list != null && i < list.size()) ? list.get(i) : "";
    }

    /**
     * Đồng bộ hóa đơn của phiếu khám = phí khám + xét nghiệm + thuốc.
     * Hóa đơn chưa thu được cập nhật (trừ phần đã thu); nếu mọi hóa đơn đã thu mà phát sinh thêm chi phí thì tạo hóa đơn bổ sung.
     */
    @Transactional
    public void syncInvoice(String examId) {
        if (exams.findById(examId) == null) return;
        long fee = exams.consultationFee(examId);
        long lab = labs.sumCost(examId);
        long rx = prescriptions.sumAmount(examId);
        long total = fee + lab + rx;

        List<String> cats = new ArrayList<>();
        cats.add("Phí khám");
        if (lab > 0) cats.add("Xét nghiệm");
        if (rx > 0) cats.add("Thuốc");
        String desc = String.join(" + ", cats);

        long paid = invoices.sumPaidByExam(examId);
        String pendingId = invoices.findPendingIdByExam(examId);
        if (pendingId != null) {
            invoices.updatePending(pendingId, Math.max(total - paid, 0L), desc);
        } else if (invoices.countByExam(examId) == 0) {
            invoices.insertPending(invoices.nextId(), examId, total, desc);
        } else if (total - paid > 0) {
            invoices.insertPending(invoices.nextId(), examId, total - paid, desc + " (bổ sung)");
        }
    }

    public List<Examination> getQueueForDoctor(String keyword) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        return getAllExaminations().stream()
                .filter(ex -> !"Hoàn tất".equalsIgnoreCase(ex.getTrangThai()))
                .filter(ex -> {
                    if (kw.isEmpty()) return true;
                    String target = (ex.getIdPhieukham() + " " + ex.getPatientName() + " " + (ex.getRoomName() != null ? ex.getRoomName() : "")).toLowerCase();
                    return target.contains(kw);
                })
                .sorted((a, b) -> a.getIdPhieukham().compareTo(b.getIdPhieukham()))
                .collect(Collectors.toList());
    }

    public List<Prescription> getRecentPrescriptionsForDoctor(String keyword) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        return prescriptions.findAll().stream()
                .filter(p -> {
                    if (kw.isEmpty()) return true;
                    String target = (p.getIdDt() + " " + p.getIdPhieukham() + " " + (p.getPatientName() != null ? p.getPatientName() : "")).toLowerCase();
                    return target.contains(kw);
                })
                .collect(Collectors.toList());
    }

    public List<Examination> getExaminationsForStaff(Staff st, String keyword, String status, String roomId) {
        if (st == null || !"Bác sĩ".equalsIgnoreCase(st.getVaiTro())) {
            return searchExaminations(keyword, status, roomId);
        }
        // Bác sĩ chỉ thấy các phiếu khám được giao cho chính mình (không xét theo phòng khám)
        String docId = st.getIdNv();
        return searchExaminations(keyword, status, roomId).stream()
                .filter(ex -> docId != null && docId.equals(ex.getIdNv()))
                .collect(Collectors.toList());
    }

    public boolean isDoctorAllowedToView(Staff st, Examination exam) {
        if (st == null || exam == null || !"Bác sĩ".equalsIgnoreCase(st.getVaiTro())) return true;
        String docId = st.getIdNv();
        return docId != null && docId.equals(exam.getIdNv());
    }
}