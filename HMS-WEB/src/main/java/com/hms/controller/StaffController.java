package com.hms.controller;

import com.hms.Entity.ClinicRoom;
import com.hms.Entity.Specialty;
import com.hms.Entity.Staff;
import com.hms.Service.StaffService;
import com.hms.dto.StaffFormDto;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public String index(@RequestParam(required = false) String search,
                        @RequestParam(required = false) String role,
                        Model model) {
        List<Staff> staffList = staffService.getAllStaff();
        if (search != null && !search.trim().isEmpty()) {
            String kw = search.trim().toLowerCase();
            staffList = staffList.stream()
                    .filter(s -> (s.getHoTen() + " " + s.getIdNv() + " " + s.getEmail()).toLowerCase().contains(kw))
                    .collect(Collectors.toList());
        }
        if (role != null && !role.trim().isEmpty() && !"all".equalsIgnoreCase(role)) {
            staffList = staffList.stream()
                    .filter(s -> role.equalsIgnoreCase(s.getVaiTro()))
                    .collect(Collectors.toList());
        }

        List<Specialty> specialties = staffService.getAllSpecialties();
        List<ClinicRoom> rooms = staffService.getAllRooms();

        model.addAttribute("activeNav", "staff");
        model.addAttribute("staffList", staffList);
        model.addAttribute("specialties", specialties);
        model.addAttribute("rooms", rooms);
        model.addAttribute("search", search);
        model.addAttribute("role", role);

        model.addAttribute("newStaff", new StaffFormDto());
        model.addAttribute("newSpecialty", new Specialty());
        model.addAttribute("newRoom", new ClinicRoom());

        return "staff/index";
    }

    // --- STAFF CRUD ---
    @PostMapping("/create")
    public String createStaff(@ModelAttribute StaffFormDto form, RedirectAttributes ra) {
        if (form.getHoTen() == null || form.getHoTen().trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng nhập họ và tên nhân sự!");
            return "redirect:/staff";
        }
        try {
            Staff st = staffService.createStaff(form);
            ra.addFlashAttribute("successMessage", "Đã thêm mới nhân sự: " + st.getHoTen() + " (" + st.getIdNv() + ")");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi tạo nhân sự: " + e.getMessage());
        }
        return "redirect:/staff";
    }

    @PostMapping("/update")
    public String updateStaff(@ModelAttribute StaffFormDto form, RedirectAttributes ra) {
        if (form.getIdNv() == null || form.getIdNv().isBlank()) {
            ra.addFlashAttribute("errorMessage", "Mã nhân viên không hợp lệ!");
            return "redirect:/staff";
        }
        try {
            Staff st = staffService.updateStaff(form);
            if (st != null) {
                ra.addFlashAttribute("successMessage", "Đã cập nhật thông tin nhân sự " + st.getIdNv());
            } else {
                ra.addFlashAttribute("errorMessage", "Không tìm thấy nhân sự " + form.getIdNv());
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/staff";
    }

    @PostMapping("/delete")
    public String deleteStaff(@RequestParam String idNv, RedirectAttributes ra) {
        try {
            staffService.deleteStaff(idNv);
            ra.addFlashAttribute("successMessage", "Đã xóa thành công nhân sự " + idNv);
        } catch (IllegalStateException ie) {
            ra.addFlashAttribute("errorMessage", ie.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi khi xóa nhân sự: " + e.getMessage());
        }
        return "redirect:/staff";
    }

    // --- SPECIALTY CRUD ---
    @PostMapping("/specialty/create")
    public String createSpecialty(@ModelAttribute Specialty specialty, RedirectAttributes ra) {
        if (specialty.getTenCk() == null || specialty.getTenCk().trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng nhập tên chuyên khoa!");
            return "redirect:/staff";
        }
        try {
            Specialty sp = staffService.createSpecialty(specialty);
            ra.addFlashAttribute("successMessage", "Đã thêm chuyên khoa: " + sp.getTenCk() + " (" + sp.getIdCk() + ")");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi thêm chuyên khoa: " + e.getMessage());
        }
        return "redirect:/staff";
    }

    @PostMapping("/specialty/update")
    public String updateSpecialty(@ModelAttribute Specialty specialty, RedirectAttributes ra) {
        if (specialty.getIdCk() == null || specialty.getIdCk().isBlank()) {
            ra.addFlashAttribute("errorMessage", "Mã chuyên khoa không hợp lệ!");
            return "redirect:/staff";
        }
        try {
            Specialty sp = staffService.updateSpecialty(specialty);
            if (sp != null) {
                ra.addFlashAttribute("successMessage", "Đã cập nhật chuyên khoa " + sp.getIdCk());
            } else {
                ra.addFlashAttribute("errorMessage", "Không tìm thấy chuyên khoa " + specialty.getIdCk());
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi cập nhật chuyên khoa: " + e.getMessage());
        }
        return "redirect:/staff";
    }

    @PostMapping("/specialty/delete")
    public String deleteSpecialty(@RequestParam String idCk, RedirectAttributes ra) {
        try {
            staffService.deleteSpecialty(idCk);
            ra.addFlashAttribute("successMessage", "Đã xóa chuyên khoa " + idCk);
        } catch (IllegalStateException ie) {
            ra.addFlashAttribute("errorMessage", ie.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi khi xóa chuyên khoa: " + e.getMessage());
        }
        return "redirect:/staff";
    }

    // --- CLINIC ROOM CRUD ---
    @PostMapping("/room/create")
    public String createRoom(@ModelAttribute ClinicRoom room, RedirectAttributes ra) {
        if (room.getTenPhong() == null || room.getTenPhong().trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng nhập tên phòng khám!");
            return "redirect:/staff";
        }
        try {
            ClinicRoom r = staffService.createRoom(room);
            ra.addFlashAttribute("successMessage", "Đã thêm phòng khám: " + r.getTenPhong() + " (" + r.getIdPk() + ")");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi thêm phòng khám: " + e.getMessage());
        }
        return "redirect:/staff";
    }

    @PostMapping("/room/update")
    public String updateRoom(@ModelAttribute ClinicRoom room, RedirectAttributes ra) {
        if (room.getIdPk() == null || room.getIdPk().isBlank()) {
            ra.addFlashAttribute("errorMessage", "Mã phòng khám không hợp lệ!");
            return "redirect:/staff";
        }
        try {
            ClinicRoom r = staffService.updateRoom(room);
            if (r != null) {
                ra.addFlashAttribute("successMessage", "Đã cập nhật phòng khám " + r.getIdPk());
            } else {
                ra.addFlashAttribute("errorMessage", "Không tìm thấy phòng khám " + room.getIdPk());
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi cập nhật phòng khám: " + e.getMessage());
        }
        return "redirect:/staff";
    }

    @PostMapping("/room/delete")
    public String deleteRoom(@RequestParam String idPk, RedirectAttributes ra) {
        try {
            staffService.deleteRoom(idPk);
            ra.addFlashAttribute("successMessage", "Đã xóa phòng khám " + idPk);
        } catch (IllegalStateException ie) {
            ra.addFlashAttribute("errorMessage", ie.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi khi xóa phòng khám: " + e.getMessage());
        }
        return "redirect:/staff";
    }
}
