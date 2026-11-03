package com.hms.Entity;

public class ClinicRoom {
    private String idPk;
    private String tenPhong;
    private String soPhong;
    private String trangThai; // "Hoạt động", "Tạm dừng"
    private String idCk;
    private String tenChuyenKhoa;

    public ClinicRoom() {}

    public ClinicRoom(String idPk, String tenPhong, String soPhong, String trangThai, String idCk) {
        this.idPk = idPk;
        this.tenPhong = tenPhong;
        this.soPhong = soPhong;
        this.trangThai = trangThai;
        this.idCk = idCk;
    }

    public String getIdPk() { return idPk; }
    public void setIdPk(String idPk) { this.idPk = idPk; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    public String getSoPhong() { return soPhong; }
    public void setSoPhong(String soPhong) { this.soPhong = soPhong; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getIdCk() { return idCk; }
    public void setIdCk(String idCk) { this.idCk = idCk; }

    public String getTenChuyenKhoa() { return tenChuyenKhoa; }
    public void setTenChuyenKhoa(String tenChuyenKhoa) { this.tenChuyenKhoa = tenChuyenKhoa; }

    public String getDisplayName() {
        return tenPhong + " · " + soPhong;
    }
}

