package com.hms.Entity;

import java.util.ArrayList;
import java.util.List;

public class Patient {
    private String idBn;
    private String hoTen;
    private String cccd;
    private String ngaySinh;
    private String gioiTinh;
    private List<String> sdt = new ArrayList<>();
    private String diaChi;
    private String ngayTao;
    private String trangThai;

    // Related entities for convenience in templates
    private PatientRelative relative;
    private HealthInsurance insurance;

    public Patient() {}

    public Patient(String idBn, String hoTen, String cccd, String ngaySinh, String gioiTinh, List<String> sdt, String diaChi, String ngayTao, String trangThai) {
        this.idBn = idBn;
        this.hoTen = hoTen;
        this.cccd = cccd;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.sdt = sdt != null ? sdt : new ArrayList<>();
        this.diaChi = diaChi;
        this.ngayTao = ngayTao;
        this.trangThai = trangThai;
    }

    public String getIdBn() { return idBn; }
    public void setIdBn(String idBn) { this.idBn = idBn; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public List<String> getSdt() { return sdt; }
    public void setSdt(List<String> sdt) { this.sdt = sdt; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getNgayTao() { return ngayTao; }
    public void setNgayTao(String ngayTao) { this.ngayTao = ngayTao; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public PatientRelative getRelative() { return relative; }
    public void setRelative(PatientRelative relative) { this.relative = relative; }

    public HealthInsurance getInsurance() { return insurance; }
    public void setInsurance(HealthInsurance insurance) { this.insurance = insurance; }

    public String getInitials() {
        if (hoTen == null || hoTen.trim().isEmpty()) return "BN";
        String[] parts = hoTen.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[parts.length - 2].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    public String getPhoneDisplay() {
        return (sdt != null && !sdt.isEmpty()) ? String.join(", ", sdt) : "—";
    }

    public Integer getTuoi() {
        if (ngaySinh == null || ngaySinh.trim().isEmpty()) return null;
        try {
            java.time.LocalDate birth = java.time.LocalDate.parse(ngaySinh.trim());
            return java.time.Period.between(birth, java.time.LocalDate.now()).getYears();
        } catch (Exception e) {
            return null;
        }
    }
}

