package com.hms.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.Entity.ClinicRoom;
import com.hms.Entity.Diagnosis;
import com.hms.Entity.LabResult;
import com.hms.Entity.LabType;
import com.hms.Entity.Specialty;
import com.hms.Entity.Staff;
import com.hms.Repository.Db;
import com.hms.Repository.ExaminationRepository;
import com.hms.Repository.LabResultRepository;
import com.hms.Repository.StaffRepository;
import com.hms.dto.StaffFormDto;

/** Nhân sự, chuyên khoa, phòng khám, danh mục xét nghiệm/chẩn đoán và kết quả xét nghiệm — lưu trên Supabase. */
@Service
public class StaffService {

    private final StaffRepository repo;
    private final LabResultRepository labResults;
    private final ExaminationRepository examinations;

    public StaffService(StaffRepository repo, LabResultRepository labResults, ExaminationRepository examinations) {
        this.repo = repo;
        this.labResults = labResults;
        this.examinations = examinations;
    }

    // ================================================================ NHÂN VIÊN

    public List<Staff> getAllStaff() {
        return repo.findAllStaff();
    }

    public List<Staff> getDoctors() {
        return getAllStaff().stream().filter(s -> "Bác sĩ".equalsIgnoreCase(s.getVaiTro())).collect(Collectors.toList());
    }

    public List<Staff> getStaffByRole(String roleName) {
        if (roleName == null || roleName.isBlank()) return getAllStaff();
        return getAllStaff().stream().filter(s -> roleName.equalsIgnoreCase(s.getVaiTro())).collect(Collectors.toList());
    }

    public Staff getStaffById(String id) {
        return repo.findStaffById(id);
    }

    @Transactional
    public synchronized Staff createStaff(StaffFormDto dto) {
        String idNv = repo.nextStaffId();
        List<String> phones = new ArrayList<>();
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) phones.add(dto.getPhone().trim());

        Staff staff = new Staff(
                idNv,
                dto.getHoTen() != null && !dto.getHoTen().isBlank() ? dto.getHoTen().trim() : "Chưa đặt tên",
                dto.getGioiTinh() != null ? dto.getGioiTinh().trim() : "Khác",
                dto.getNgaySinh() != null ? dto.getNgaySinh().trim() : "",
                phones,
                dto.getEmail() != null ? dto.getEmail().trim() : "",
                dto.getVaiTro() != null && !dto.getVaiTro().isBlank() ? dto.getVaiTro().trim() : "Bác sĩ",
                dto.getTrangThai() != null && !dto.getTrangThai().isBlank() ? dto.getTrangThai().trim() : "Đang làm việc",
                dto.getSpecialtyDetail() != null ? dto.getSpecialtyDetail().trim() : "");
        validateStaff(staff);
        String[] assign = resolveDoctorAssignment(staff.getVaiTro(), dto, true, idNv);
        try {
            repo.insertStaff(staff);
            if (assign != null) repo.assignDoctor(idNv, assign[0], assign[1], assign[2], assign[3]);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Thông tin nhân sự không hợp lệ hoặc vượt độ dài cho phép.");
        }
        return repo.findStaffById(idNv);
    }

    @Transactional
    public synchronized Staff updateStaff(StaffFormDto dto) {
        Staff staff = repo.findStaffById(dto.getIdNv());
        if (staff == null) return null;

        if (dto.getHoTen() != null && !dto.getHoTen().isBlank()) staff.setHoTen(dto.getHoTen().trim());
        if (dto.getGioiTinh() != null) staff.setGioiTinh(dto.getGioiTinh().trim());
        if (dto.getNgaySinh() != null) staff.setNgaySinh(dto.getNgaySinh().trim());
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            List<String> phones = new ArrayList<>();
            phones.add(dto.getPhone().trim());
            staff.setSdt(phones);
        }
        if (dto.getEmail() != null) staff.setEmail(dto.getEmail().trim());
        if (dto.getVaiTro() != null && !dto.getVaiTro().isBlank()) {
            String newRole = dto.getVaiTro().trim();
            if ("Bác sĩ".equalsIgnoreCase(staff.getVaiTro()) && !"Bác sĩ".equalsIgnoreCase(newRole)
                    && repo.countExamsOfDoctor(staff.getIdNv()) > 0) {
                throw new IllegalStateException("Không thể đổi vai trò nhân sự " + staff.getIdNv()
                        + " vì bác sĩ này đang phụ trách phiếu khám.");
            }
            staff.setVaiTro(newRole);
        }
        if (dto.getTrangThai() != null && !dto.getTrangThai().isBlank()) staff.setTrangThai(dto.getTrangThai().trim());
        if (dto.getSpecialtyDetail() != null) staff.setSpecialtyDetail(dto.getSpecialtyDetail().trim());

        validateStaff(staff);
        String[] assign = resolveDoctorAssignment(staff.getVaiTro(), dto, false, staff.getIdNv());
        try {
            repo.updateStaff(staff);
            if (assign != null) repo.assignDoctor(staff.getIdNv(), assign[0], assign[1], assign[2], assign[3]);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Thông tin nhân sự không hợp lệ hoặc vượt độ dài cho phép.");
        }
        return repo.findStaffById(staff.getIdNv());
    }

    /**
     * Chỉ Bác sĩ mới có chuyên khoa + phòng khám. Trả về {idCk, idPk} cần lưu, hoặc null nếu không phải bác sĩ
     * (hoặc form sửa không gửi các trường này nên giữ nguyên phân công cũ).
     */
    private String[] resolveDoctorAssignment(String vaiTro, StaffFormDto dto, boolean required) {
        return resolveDoctorAssignment(vaiTro, dto, required, dto.getIdNv());
    }

    private String[] resolveDoctorAssignment(String vaiTro, StaffFormDto dto, boolean required, String idNv) {
        if (!StaffRepository.ROLE_DOCTOR.equalsIgnoreCase(vaiTro)) return null;
        String idCk = Db.trimToNull(dto.getIdCk());
        String idPk = Db.trimToNull(dto.getIdPk());
        String hocVi = Db.trimToNull(dto.getHocVi());
        String soCchn = Db.trimToNull(dto.getSoCchn());
        if (soCchn != null) soCchn = soCchn.toUpperCase();
        if (idCk == null && idPk == null && hocVi == null && soCchn == null && !required) return null;
        if (idCk == null || repo.findSpecialtyById(idCk) == null) {
            throw new IllegalArgumentException("Vui lòng chọn chuyên khoa trực thuộc hợp lệ cho bác sĩ.");
        }
        if (idPk == null) throw new IllegalArgumentException("Vui lòng chọn số / vị trí phòng cho bác sĩ.");
        ClinicRoom room = repo.findRoomById(idPk);
        if (room == null) throw new IllegalArgumentException("Phòng khám không tồn tại: " + idPk);
        if (!idCk.equals(room.getIdCk())) {
            throw new IllegalArgumentException("Phòng " + room.getSoPhong() + " không thuộc chuyên khoa đã chọn.");
        }
        if (hocVi == null) throw new IllegalArgumentException("Vui lòng nhập học vị của bác sĩ.");
        if (hocVi.length() > 50) throw new IllegalArgumentException("Học vị tối đa 50 ký tự.");
        if (soCchn == null) throw new IllegalArgumentException("Vui lòng nhập số chứng chỉ hành nghề của bác sĩ.");
        if (!soCchn.matches("CCHN-\\d{4}")) {
            throw new IllegalArgumentException("Số chứng chỉ hành nghề phải đúng định dạng CCHN-xxxx (4 chữ số), ví dụ CCHN-0123.");
        }
        if (repo.licenseUsedByOther(soCchn, idNv)) {
            throw new IllegalArgumentException("Số chứng chỉ hành nghề [" + soCchn + "] đã thuộc về bác sĩ khác.");
        }
        return new String[] { idCk, idPk, hocVi, soCchn };
    }

    private void validateStaff(Staff s) {
        if (s.getVaiTro() != null && s.getVaiTro().length() > 20) throw new IllegalArgumentException("Vai trò tối đa 20 ký tự.");
        if (s.getHoTen() != null && s.getHoTen().length() > 100) throw new IllegalArgumentException("Họ tên tối đa 100 ký tự.");
        for (String p : s.getSdt()) {
            if (p.length() > 15) throw new IllegalArgumentException("Số điện thoại tối đa 15 ký tự.");
        }
    }

    @Transactional
    public void deleteStaff(String idNv) {
        if (idNv == null || idNv.isBlank()) return;
        if (repo.countExamsOfDoctor(idNv) > 0) {
            throw new IllegalStateException("Không thể xóa nhân sự " + idNv + " do đang phụ trách hồ sơ khám bệnh!");
        }
        if (repo.countDispensedBy(idNv) > 0) {
            throw new IllegalStateException("Không thể xóa nhân sự " + idNv + " do đã cấp phát đơn thuốc trong hệ thống!");
        }
        if (repo.countInvoicesCollectedBy(idNv) > 0) {
            throw new IllegalStateException("Không thể xóa nhân sự " + idNv + " do đã thu viện phí trong hệ thống!");
        }
        if (repo.countReceptionsBy(idNv) > 0) {
            throw new IllegalStateException("Không thể xóa nhân sự " + idNv + " do đã tiếp nhận phiếu khám!");
        }
        try {
            repo.deleteStaff(idNv);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Không thể xóa nhân sự " + idNv + " vì còn dữ liệu liên quan.");
        }
    }

    // ================================================================ CHUYÊN KHOA

    public List<Specialty> getAllSpecialties() {
        return repo.findAllSpecialties();
    }

    @Transactional
    public synchronized Specialty createSpecialty(Specialty specialty) {
        String idCk = (specialty.getIdCk() != null && !specialty.getIdCk().isBlank())
                ? specialty.getIdCk().trim().toUpperCase() : repo.nextSpecialtyId();
        String ten = specialty.getTenCk() != null && !specialty.getTenCk().isBlank() ? specialty.getTenCk().trim() : "Chuyên khoa mới";
        Specialty sp = new Specialty(idCk, ten);
        try {
            repo.insertSpecialty(sp);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Mã hoặc tên chuyên khoa đã tồn tại (" + idCk + " / " + ten + ").");
        }
        return sp;
    }

    @Transactional
    public Specialty updateSpecialty(Specialty specialty) {
        Specialty sp = repo.findSpecialtyById(specialty.getIdCk());
        if (sp == null) return null;
        if (specialty.getTenCk() != null && !specialty.getTenCk().isBlank()) sp.setTenCk(specialty.getTenCk().trim());
        try {
            repo.updateSpecialty(sp);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Tên chuyên khoa [" + sp.getTenCk() + "] đã tồn tại.");
        }
        return sp;
    }

    @Transactional
    public void deleteSpecialty(String idCk) {
        if (idCk == null || idCk.isBlank()) return;
        if (repo.countRoomsOfSpecialty(idCk) > 0) {
            throw new IllegalStateException("Không thể xóa chuyên khoa " + idCk + " vì đang có phòng khám trực thuộc!");
        }
        try {
            repo.deleteSpecialty(idCk);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Không thể xóa chuyên khoa " + idCk + " vì đang được bác sĩ sử dụng!");
        }
    }

    // ================================================================ PHÒNG KHÁM

    public List<ClinicRoom> getAllRooms() {
        return repo.findAllRooms();
    }

    @Transactional
    public synchronized ClinicRoom createRoom(ClinicRoom room) {
        if (Db.blank(room.getIdCk()) || repo.findSpecialtyById(room.getIdCk()) == null) {
            throw new IllegalArgumentException("Vui lòng chọn chuyên khoa hợp lệ cho phòng khám.");
        }
        if (Db.blank(room.getSoPhong())) throw new IllegalArgumentException("Số phòng không được để trống.");
        String idPk = (room.getIdPk() != null && !room.getIdPk().isBlank()) ? room.getIdPk().trim().toUpperCase() : repo.nextRoomId();
        if (Db.blank(room.getTenPhong())) throw new IllegalArgumentException("Tên phòng khám không được để trống.");
        room.setIdPk(idPk);
        room.setTenPhong(room.getTenPhong().trim());
        room.setSoPhong(room.getSoPhong().trim());
        if (room.getTrangThai() == null || room.getTrangThai().isBlank()) room.setTrangThai("Hoạt động");
        try {
            repo.insertRoom(room);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Mã phòng [" + idPk + "] hoặc số phòng [" + room.getSoPhong() + "] đã tồn tại.");
        }
        return repo.findRoomById(idPk);
    }

    @Transactional
    public ClinicRoom updateRoom(ClinicRoom room) {
        ClinicRoom r = repo.findRoomById(room.getIdPk());
        if (r == null) return null;
        if (room.getTenPhong() != null) r.setTenPhong(room.getTenPhong().trim());
        if (room.getSoPhong() != null) r.setSoPhong(room.getSoPhong().trim());
        if (room.getTrangThai() != null && !room.getTrangThai().isBlank()) r.setTrangThai(room.getTrangThai().trim());
        if (room.getIdCk() != null && !room.getIdCk().isBlank()) {
            if (repo.findSpecialtyById(room.getIdCk()) == null) throw new IllegalArgumentException("Chuyên khoa không tồn tại: " + room.getIdCk());
            r.setIdCk(room.getIdCk());
        }
        try {
            repo.updateRoom(r);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Số phòng [" + r.getSoPhong() + "] đã tồn tại.");
        }
        return repo.findRoomById(r.getIdPk());
    }

    @Transactional
    public void deleteRoom(String idPk) {
        if (idPk == null || idPk.isBlank()) return;
        if (repo.countExamsOfRoom(idPk) > 0) {
            throw new IllegalStateException("Không thể xóa phòng khám " + idPk + " vì đang có phiếu khám liên kết!");
        }
        try {
            repo.deleteRoom(idPk);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Không thể xóa phòng khám " + idPk + " vì còn dữ liệu liên quan.");
        }
    }

    // ================================================================ DANH MỤC & XÉT NGHIỆM

    public List<LabType> getAllLabTypes() {
        return repo.findAllLabTypes();
    }

    public List<Diagnosis> getAllDiagnoses() {
        return repo.findAllDiagnoses();
    }

    public List<LabResult> getAllLabResults() {
        return labResults.findAll();
    }

    public List<Staff> findDoctorsByRoom(String idPk) {
        return repo.findDoctorsByRoom(idPk);
    }

    @Transactional
    public boolean updateLabResult(String kqId, String result, String conclusion) {
        boolean ok = labResults.complete(kqId, result != null ? result.trim() : "", conclusion != null ? conclusion.trim() : "",
                Db.sqlTs(java.time.LocalDateTime.now()));
        if (ok) {
            // Khi mọi xét nghiệm của phiếu đã có kết quả, phiếu khám chuyển từ "Chờ xét nghiệm" sang "Chờ thanh toán"
            String examId = labResults.examIdOf(kqId);
            if (examId != null && labResults.countPending(examId) == 0
                    && "CHO_XET_NGHIEM".equals(examinations.currentStatusCode(examId))) {
                examinations.updateStatus(examId, "CHO_THANH_TOAN");
            }
        }
        return ok;
    }
}