package com.hms.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hms.Entity.Examination;
import com.hms.Entity.Invoice;
import com.hms.Service.BillingService;
import com.hms.Service.ExaminationService;
import com.hms.dto.InvoicePayDto;
import com.hms.dto.RevenueReportDto;

@Controller
@RequestMapping("/billing")
public class BillingController {

    private final BillingService billingService;
    private final ExaminationService examinationService;

    public BillingController(BillingService billingService,
                             ExaminationService examinationService) {
        this.billingService = billingService;
        this.examinationService = examinationService;
    }

    @GetMapping
    public String index(@RequestParam(required = false, defaultValue = "month") String view,
                        @RequestParam(required = false) Integer year,
                        @RequestParam(required = false) Integer month,
                        @RequestParam(required = false) String date,
                        @RequestParam(required = false) String selectedKey,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) String status,
                        Model model) {
        RevenueReportDto report = billingService.getRevenueReport(view, year, month, date, selectedKey);
        List<Invoice> invoices = billingService.filterInvoices(search, status, report.getViewType(), report.getYear(), report.getMonth(), report.getDate(), report.getSelectedKey());

        model.addAttribute("activeNav", "billing");
        model.addAttribute("report", report);
        model.addAttribute("invoices", invoices);

        model.addAttribute("view", report.getViewType());
        model.addAttribute("year", report.getYear());
        model.addAttribute("month", report.getMonth());
        model.addAttribute("date", report.getDate());
        model.addAttribute("selectedKey", report.getSelectedKey());

        model.addAttribute("totalPaid", report.getTotalPaid());
        model.addAttribute("totalDue", report.getTotalDue());
        model.addAttribute("totalCount", report.getTotalCount());

        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("todayDate", LocalDate.now().toString());

        model.addAttribute("allYears", List.of(2026, 2025, 2024));
        model.addAttribute("allMonths", List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

        model.addAttribute("payForm", new InvoicePayDto());

        return "billing/index";
    }

    @PostMapping("/pay")
    public String payInvoice(@ModelAttribute InvoicePayDto form,
                             @RequestParam(required = false) String view,
                             @RequestParam(required = false) Integer year,
                             @RequestParam(required = false) Integer month,
                             @RequestParam(required = false) String selectedKey,
                             jakarta.servlet.http.HttpSession session,
                             RedirectAttributes ra) {
        com.hms.Entity.Staff cur = (com.hms.Entity.Staff) session.getAttribute(GlobalControllerAdvice.SESSION_STAFF);
        boolean ok;
        try {
            ok = billingService.payInvoice(form.getIdHoadon(), form.getPhuongThucTt(), form.getThoiGianThu(), cur != null ? cur.getIdNv() : null);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            ok = false;
            return "redirect:/billing";
        }
        if (ok) {
            ra.addFlashAttribute("successMessage", "Đã thu thành công hóa đơn " + form.getIdHoadon() + " (" + form.getPhuongThucTt() + ").");
        } else {
            ra.addFlashAttribute("errorMessage", "Không thể thu hóa đơn " + form.getIdHoadon() + " (không tìm thấy hoặc đã được thanh toán).");
        }
        StringBuilder redirectUrl = new StringBuilder("redirect:/billing");
        boolean hasParam = false;
        if (view != null && !view.isBlank()) {
            redirectUrl.append(hasParam ? "&" : "?").append("view=").append(view);
            hasParam = true;
        }
        if (year != null) {
            redirectUrl.append(hasParam ? "&" : "?").append("year=").append(year);
            hasParam = true;
        }
        if (month != null) {
            redirectUrl.append(hasParam ? "&" : "?").append("month=").append(month);
            hasParam = true;
        }
        if (selectedKey != null && !selectedKey.isBlank()) {
            redirectUrl.append(hasParam ? "&" : "?").append("selectedKey=").append(selectedKey);
        }
        return redirectUrl.toString();
    }

    @GetMapping("/print/{id}")
    public String printInvoice(@PathVariable String id, Model model, RedirectAttributes ra) {
        Invoice invoice = billingService.getInvoiceById(id);
        if (invoice == null) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy hóa đơn " + id);
            return "redirect:/billing";
        }

        Examination exam = examinationService.getExaminationById(invoice.getIdPhieukham());
        model.addAttribute("invoice", invoice);
        model.addAttribute("exam", exam);
        return "billing/print";
    }
}

