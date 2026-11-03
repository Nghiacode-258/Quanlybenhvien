package com.hms.dto;

public class PatientFormDto {
    private String idBn;
    private String hoTen;
    private String cccd;
    private String ngaySinh;
    private String gioiTinh = "Nam";
    private String sdt;
    private String diaChi;
    private String trangThai = "Ổn định";

    // Người thần
    private String relHoTen;
    private String relMoiQuanHe;
    private String relSdt;
    private String relGioiTinh;

    // Bảo hiểm y tế 
    private String soBhyt;
    private Integer mucHuong;
    private String ngayBatDau;
    private String ngayKetThuc;
    private String noiDangKy;
    private String nhomDoiTuong;

    public PatientFormDto() {}

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

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getRelHoTen() { return relHoTen; }
    public void setRelHoTen(String relHoTen) { this.relHoTen = relHoTen; }

    public String getRelMoiQuanHe() { return relMoiQuanHe; }
    public void setRelMoiQuanHe(String relMoiQuanHe) { this.relMoiQuanHe = relMoiQuanHe; }

    public String getRelSdt() { return relSdt; }
    public void setRelSdt(String relSdt) { this.relSdt = relSdt; }

    public String getRelGioiTinh() { return relGioiTinh; }
    public void setRelGioiTinh(String relGioiTinh) { this.relGioiTinh = relGioiTinh; }

    public String getSoBhyt() { return soBhyt; }
    public void setSoBhyt(String soBhyt) { this.soBhyt = soBhyt; }

    public Integer getMucHuong() { return mucHuong; }
    public void setMucHuong(Integer mucHuong) { this.mucHuong = mucHuong; }

    public String getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(String ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public String getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(String ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public String getNoiDangKy() { return noiDangKy; }
    public void setNoiDangKy(String noiDangKy) { this.noiDangKy = noiDangKy; }

    public String getNhomDoiTuong() { return nhomDoiTuong; }
    public void setNhomDoiTuong(String nhomDoiTuong) { this.nhomDoiTuong = nhomDoiTuong; }
}

