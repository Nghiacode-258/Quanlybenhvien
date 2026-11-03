package com.hms.dto;

public class MedicineLotFormDto {
    private String idThuoc;
    private String soLo;
    private String ngayNhap;
    private String ngaySanXuat;
    private String hanSuDung;
    private Integer soLuongNhap;
    private Long giaNhap;
    private Long giaBan;
    private String nhaCungCap;

    public MedicineLotFormDto() {}

    public String getIdThuoc() { return idThuoc; }
    public void setIdThuoc(String idThuoc) { this.idThuoc = idThuoc; }

    public String getSoLo() { return soLo; }
    public void setSoLo(String soLo) { this.soLo = soLo; }

    public String getNgayNhap() { return ngayNhap; }
    public void setNgayNhap(String ngayNhap) { this.ngayNhap = ngayNhap; }

    public String getNgaySanXuat() { return ngaySanXuat; }
    public void setNgaySanXuat(String ngaySanXuat) { this.ngaySanXuat = ngaySanXuat; }

    public String getHanSuDung() { return hanSuDung; }
    public void setHanSuDung(String hanSuDung) { this.hanSuDung = hanSuDung; }

    public Integer getSoLuongNhap() { return soLuongNhap; }
    public void setSoLuongNhap(Integer soLuongNhap) { this.soLuongNhap = soLuongNhap; }

    public Long getGiaNhap() { return giaNhap; }
    public void setGiaNhap(Long giaNhap) { this.giaNhap = giaNhap; }

    public Long getGiaBan() { return giaBan; }
    public void setGiaBan(Long giaBan) { this.giaBan = giaBan; }

    public String getNhaCungCap() { return nhaCungCap; }
    public void setNhaCungCap(String nhaCungCap) { this.nhaCungCap = nhaCungCap; }
}

