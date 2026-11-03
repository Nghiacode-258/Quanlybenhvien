package com.hms.controller;

import com.hms.Entity.Role;
import com.hms.Entity.Staff;
import com.hms.Service.StaffService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RoleSelectionController {

    private final StaffService staffService;

    public RoleSelectionController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping("/")
    public String roleSelect(Model model, HttpSession session) {
        model.addAttribute("roles", Role.values());
        Role current = (Role) session.getAttribute(GlobalControllerAdvice.SESSION_ROLE);
        model.addAttribute("selectedRole", current != null ? current.getKey() : "manager");
        return "auth/role-select";
    }

    @GetMapping("/select-role/{roleKey}")
    public String selectRole(@PathVariable String roleKey, HttpSession session) {
        Role role = Role.fromKey(roleKey);
        session.setAttribute(GlobalControllerAdvice.SESSION_ROLE, role);
        Staff staff = staffService.getStaffById(role.getEmployeeId());
        if (staff != null) {
            session.setAttribute(GlobalControllerAdvice.SESSION_STAFF, staff);
        }
        return "redirect:" + role.getStartUrl();
    }

    @GetMapping("/switch-staff/{id}")
    public String switchStaff(@PathVariable String id, HttpSession session, HttpServletRequest request) {
        Staff staff = staffService.getStaffById(id);
        if (staff != null) {
            session.setAttribute(GlobalControllerAdvice.SESSION_STAFF, staff);
            for (Role r : Role.values()) {
                if (r.getShortLabel().equalsIgnoreCase(staff.getVaiTro())) {
                    session.setAttribute(GlobalControllerAdvice.SESSION_ROLE, r);
                    break;
                }
            }
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null && !referer.isBlank() ? referer : "/dashboard");
    }

    @PostMapping("/reset-demo")
    public String resetDemo(HttpSession session) {
        // Dữ liệu nằm trên Supabase nên KHÔNG xóa gì ở CSDL; chỉ đặt lại phiên làm việc (vai trò/nhân viên đang chọn).
        session.removeAttribute(GlobalControllerAdvice.SESSION_ROLE);
        session.removeAttribute(GlobalControllerAdvice.SESSION_STAFF);
        return "redirect:/";
    }
}
