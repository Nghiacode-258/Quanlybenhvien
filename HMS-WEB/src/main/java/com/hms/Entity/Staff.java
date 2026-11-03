package com.hms.Entity;

import java.util.ArrayList;
import java.util.List;

public class Staff {
    private String idNv;
    private String hoTen;
    private String gioiTinh;
    private String ngaySinh;
    private List<String> sdt = new ArrayList<>();
    private String email;
    private String vaiTro; // "Bác sĩ", "Y tá", "Dược sĩ", "Thu ngân", "Quản lý"
    private String trangThai; // "Đang làm việc"
    private String specialtyDetail; // e.g. "Thạc sĩ · Nội tổng hợp" hoặc "Điều dưỡng đại học"
    private String idPk; // Mã phòng khám phân công (dành cho Bác sĩ)
    private String idCk; // Mã chuyên khoa trực thuộc (dành cho Bác sĩ)
    private String hocVi; // Học vị (dành cho Bác sĩ)
    private String soCchn; // Số chứng chỉ hành nghề (dành cho Bác sĩ)

    public Staff() {}

    public Staff(String idNv, String hoTen, String gioiTinh, String ngaySinh, List<String> sdt, String email, String vaiTro, String trangThai, String specialtyDetail) {
        this(idNv, hoTen, gioiTinh, ngaySinh, sdt, email, vaiTro, trangThai, specialtyDetail, null);
    }

    public Staff(String idNv, String hoTen, String gioiTinh, String ngaySinh, List<String> sdt, String email, String vaiTro, String trangThai, String specialtyDetail, String idPk) {
        this.idNv = idNv;
        this.hoTen = hoTen;
        this.gioiTinh = gioiTinh;
        this.ngaySinh = ngaySinh;
        this.sdt = sdt != null ? sdt : new ArrayList<>();
        this.email = email;
        this.vaiTro = vaiTro;
        this.trangThai = trangThai;
        this.specialtyDetail = specialtyDetail;
        this.idPk = idPk;
    }

    public String getIdNv() { return idNv; }
    public void setIdNv(String idNv) { this.idNv = idNv; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }

    public List<String> getSdt() { return sdt; }
    public void setSdt(List<String> sdt) { this.sdt = sdt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getSpecialtyDetail() { return specialtyDetail; }
    public void setSpecialtyDetail(String specialtyDetail) { this.specialtyDetail = specialtyDetail; }

    public String getInitials() {
        if (hoTen == null || hoTen.trim().isEmpty()) return "NV";
        String[] parts = hoTen.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[parts.length - 2].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    public String getPhoneDisplay() {
        return (sdt != null && !sdt.isEmpty()) ? String.join(", ", sdt) : "—";
    }

    public String getIdPk() { return idPk; }
    public void setIdPk(String idPk) { this.idPk = idPk; }
    public String getIdCk() { return idCk; }
    public void setIdCk(String idCk) { this.idCk = idCk; }
    public String getHocVi() { return hocVi; }
    public void setHocVi(String hocVi) { this.hocVi = hocVi; }
    public String getSoCchn() { return soCchn; }
    public void setSoCchn(String soCchn) { this.soCchn = soCchn; }
}