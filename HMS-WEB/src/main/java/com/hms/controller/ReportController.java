package com.hms.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hms.Entity.Examination;
import com.hms.Entity.Staff;
import com.hms.Service.BillingService;
import com.hms.Service.ExaminationService;
import com.hms.Service.PatientService;
import com.hms.Service.PharmacyService;
import com.hms.Service.StaffService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/reports")
public class ReportController {
    
    private final StaffService staffService;
    private final PatientService patientService;
    private final ExaminationService examinationService;
    private final PharmacyService pharmacyService;
    private final BillingService billingService;

    public ReportController(PatientService patientService,
                            ExaminationService examinationService,
                            PharmacyService pharmacyService,
                            BillingService billingService,
                            StaffService staffService) {
        this.patientService = patientService;
        this.examinationService = examinationService;
        this.pharmacyService = pharmacyService;
        this.billingService = billingService;
        this.staffService = staffService;
    }

    @GetMapping
    public String index(Model model) {
        long totalPatients = patientService.getAllPatients().size();
        long totalExams = examinationService.getAllExaminations().size();
        long totalPaid = billingService.calculateTotalRevenue();
        long totalDue = billingService.calculatePendingRevenue();
        long totalMedicines = pharmacyService.getAllMedicines().size();

        // Phân bổ nhân sự theo vai trò: đếm từ CSDL (không tính nhân viên đã nghỉ việc)
        List<Staff> activeStaff = new ArrayList<>();
        for (Staff st : staffService.getAllStaff()) {
            if (!"Đã nghỉ việc".equalsIgnoreCase(st.getTrangThai())) activeStaff.add(st);
        }
        long totalStaff = activeStaff.size();
        String[][] roles = {
                {"Bác sĩ chuyên khoa", "Bác sĩ", "#0284c7"},
                {"Y tá / Điều dưỡng", "Y tá", "#059669"},
                {"Dược sĩ", "Dược sĩ", "#7c3aed"},
                {"Thu ngân", "Thu ngân", "#ea580c"},
                {"Ban Giám đốc & Quản trị", "Quản lý", "#0284c7"}
        };
        List<Map<String, Object>> staffStats = new ArrayList<>();
        for (String[] r : roles) {
            long count = activeStaff.stream().filter(st -> r[1].equalsIgnoreCase(st.getVaiTro())).count();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("label", r[0]);
            row.put("count", count);
            row.put("percent", totalStaff == 0 ? 0 : Math.round(count * 100.0 / totalStaff));
            row.put("color", r[2]);
            staffStats.add(row);
        }
        model.addAttribute("staffStats", staffStats);
        model.addAttribute("totalStaff", totalStaff);
        model.addAttribute("activeNav", "reports");
        model.addAttribute("totalPatients", totalPatients);
        model.addAttribute("totalExams", totalExams);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("totalDue", totalDue);
        model.addAttribute("totalMedicines", totalMedicines);

        return "reports/index";
    }

    @GetMapping("/export-csv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"hms-phieu-kham-" + LocalDate.now() + ".csv\"");

        PrintWriter writer = response.getWriter();
        // UTF-8 BOM for Excel
        writer.write('\uFEFF');
        writer.println("Mã phiếu,Mã BN,Tên bệnh nhân,Phòng khám,Bác sĩ,Ngày đăng ký,Triệu chứng,Trạng thái");

        List<Examination> list = examinationService.getAllExaminations();
        for (Examination ex : list) {
            writer.println(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                    ex.getIdPhieukham(),
                    ex.getIdBn(),
                    ex.getPatientName() != null ? ex.getPatientName() : "",
                    ex.getRoomName() != null ? ex.getRoomName() : "",
                    ex.getDoctorName() != null ? ex.getDoctorName() : "",
                    ex.getNgayDky(),
                    ex.getTrieuChung() != null ? ex.getTrieuChung().replace("\"", "\"\"") : "",
                    ex.getTrangThai()
            ));
        }
        writer.flush();
    }
}

