package com.hms.Entity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class MedicineLot {
    private String idLo;
    private String idThuoc;
    private String soLo;
    private String hanSuDung;
    private Integer soLuongNhap;
    private Integer soLuongTon;
    private Long giaNhap;
    private Long giaBan;
    private String ngayNhap;
    private String ngaySanXuat;
    private String nhaCungCap;
    private String trangThai = "Hoạt động"; // "Hoạt động", "Đã loại bỏ"

    // Joined
    private String tenThuoc;
    private String donVi;

    public MedicineLot() {}

    public MedicineLot(String idLo, String idThuoc,String soLo, String hanSuDung, Integer soLuongNhap, Integer soLuongTon, Long giaNhap, Long giaBan) {
        this(idLo, idThuoc, soLo, hanSuDung, soLuongNhap, soLuongTon, giaNhap, giaBan, LocalDate.now().toString(), "", "", "Hoạt động");
    }

    public MedicineLot(String idLo, String idThuoc, String soLo, String hanSuDung, Integer soLuongNhap, Integer soLuongTon, Long giaNhap, Long giaBan, String ngayNhap, String ngaySanXuat, String nhaCungCap, String trangThai) {
        this.idLo = idLo;
        this.idThuoc = idThuoc;
        this.soLo = soLo;
        this.hanSuDung = hanSuDung;
        this.soLuongNhap = soLuongNhap;
        this.soLuongTon = soLuongTon;
        this.giaNhap = giaNhap;
        this.giaBan = giaBan;
        this.ngayNhap = ngayNhap != null && !ngayNhap.isEmpty() ? ngayNhap : LocalDate.now().toString();
        this.ngaySanXuat = ngaySanXuat;
        this.nhaCungCap = nhaCungCap;
        this.trangThai = trangThai != null ? trangThai : "Hoạt động";
    }

    public String getIdLo() { return idLo; }
    public void setIdLo(String idLo) { this.idLo = idLo; }

    public String getIdThuoc() { return idThuoc; }
    public void setIdThuoc(String idThuoc) { this.idThuoc = idThuoc; }

    public String getSoLo() { return soLo; }
    public void setSoLo(String soLo) { this.soLo = soLo; }

    public String getHanSuDung() { return hanSuDung; }
    public void setHanSuDung(String hanSuDung) { this.hanSuDung = hanSuDung; }

    public Integer getSoLuongNhap() { return soLuongNhap; }
    public void setSoLuongNhap(Integer soLuongNhap) { this.soLuongNhap = soLuongNhap; }

    public Integer getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(Integer soLuongTon) { this.soLuongTon = soLuongTon; }

    public Long getGiaNhap() { return giaNhap; }
    public void setGiaNhap(Long giaNhap) { this.giaNhap = giaNhap; }

    public Long getGiaBan() { return giaBan; }
    public void setGiaBan(Long giaBan) { this.giaBan = giaBan; }

    public String getNgayNhap() { return ngayNhap; }
    public void setNgayNhap(String ngayNhap) { this.ngayNhap = ngayNhap; }

    public String getNgaySanXuat() { return ngaySanXuat; }
    public void setNgaySanXuat(String ngaySanXuat) { this.ngaySanXuat = ngaySanXuat; }

    public String getNhaCungCap() { return nhaCungCap; }
    public void setNhaCungCap(String nhaCungCap) { this.nhaCungCap = nhaCungCap; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getTenThuoc() { return tenThuoc; }
    public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }

    public String getDonVi() { return donVi; }
    public void setDonVi(String donVi) { this.donVi = donVi; }

    public boolean isExpired() {
        if (hanSuDung == null || hanSuDung.isEmpty()) return false;
        try {
            return LocalDate.parse(hanSuDung).isBefore(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isExpiringSoon() {
        if (isExpired()) return false;
        if (hanSuDung == null || hanSuDung.isEmpty()) return false;
        try {
            LocalDate exp = LocalDate.parse(hanSuDung);
            long days = ChronoUnit.DAYS.between(LocalDate.now(), exp);
            return days >= 0 && days <= 60;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRemoved() {
        return "Đã loại bỏ".equalsIgnoreCase(trangThai) || (trangThai != null && trangThai.contains("loại bỏ"));
    }

    public String getExpiryBadgeClass() {
        if (isRemoved()) return "pill-danger";
        if (isExpired()) return "pill-danger";
        if (isExpiringSoon()) return "pill-warning";
        return "pill-success";
    }

    public String getExpiryStatusText() {
        if (isRemoved()) return "Đã loại bỏ";
        if (isExpired()) return "Hết hạn";
        if (isExpiringSoon()) return "Sắp hết hạn";
        return "Còn hạn";
    }
}
