package com.hms.Entity;

public class HealthInsurance {
    private String idBhyt;
    private String idBn;
    private String soBhyt;
    private Integer mucHuong;
    private String ngayBatDau;
    private String ngayKetThuc;
    private String noiDangKy;
    private String nhomDoiTuong;

    public HealthInsurance() {}

    public HealthInsurance(String idBhyt, String idBn, String soBhyt, Integer mucHuong, String ngayBatDau, String ngayKetThuc, String noiDangKy, String nhomDoiTuong) {
        this.idBhyt = idBhyt;
        this.idBn = idBn;
        this.soBhyt = soBhyt;
        this.mucHuong = mucHuong;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.noiDangKy = noiDangKy;
        this.nhomDoiTuong = nhomDoiTuong;
    }

    public String getIdBhyt() { return idBhyt; }
    public void setIdBhyt(String idBhyt) { this.idBhyt = idBhyt; }

    public String getIdBn() { return idBn; }
    public void setIdBn(String idBn) { this.idBn = idBn; }

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

