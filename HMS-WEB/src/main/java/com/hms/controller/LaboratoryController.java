package com.hms.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hms.Entity.LabResult;
import com.hms.Service.StaffService;

@Controller
@RequestMapping("/laboratory")
public class LaboratoryController {

    private final StaffService staffService;

    public LaboratoryController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public String index(@RequestParam(required = false) String search,
                        @RequestParam(required = false) String status,
                        Model model) {
        List<LabResult> list = staffService.getAllLabResults();
        if (search != null && !search.trim().isEmpty()) {
            String kw = search.trim().toLowerCase();
            list = list.stream().filter(l -> (l.getIdKqxn() + " " + l.getPatientName() + " " + l.getTenLxn() + " " + l.getIdPhieukham()).toLowerCase().contains(kw)).collect(Collectors.toList());
        }
        if (status != null && !status.trim().isEmpty() && !"all".equalsIgnoreCase(status)) {
            list = list.stream().filter(l -> status.equalsIgnoreCase(l.getTrangThai())).collect(Collectors.toList());
        }

        long completed = list.stream().filter(l -> "Hoàn thành".equalsIgnoreCase(l.getTrangThai())).count();
        long pending = list.stream().filter(l -> !"Hoàn thành".equalsIgnoreCase(l.getTrangThai())).count();

        model.addAttribute("activeNav", "laboratory");
        model.addAttribute("labResults", list);
        model.addAttribute("completedCount", completed);
        model.addAttribute("pendingCount", pending);
        model.addAttribute("totalCount", list.size());
        model.addAttribute("search", search);
        model.addAttribute("status", status);

        return "laboratory/index";
    }

    @PostMapping("/result")
    public String saveResult(@RequestParam String idKqxn,
                             @RequestParam String ketQua,
                             @RequestParam(required = false) String ketLuan,
                             RedirectAttributes ra) {
        boolean ok = staffService.updateLabResult(idKqxn, ketQua, ketLuan);
        if (ok) {
            ra.addFlashAttribute("successMessage", "Đã cập nhật kết quả xét nghiệm " + idKqxn);
        }
        return "redirect:/laboratory";
    }
}

