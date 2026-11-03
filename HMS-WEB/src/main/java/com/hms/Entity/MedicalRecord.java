package com.hms.Entity;

public class MedicalRecord {
    private String idBenhan;
    private String idBn;
    private String ngayTao;
    private String tienSuBenh;
    private String trangThai; // "Đang theo dõi", "Mở", "Đã đóng"

    public MedicalRecord() {}

    public MedicalRecord(String idBenhan, String idBn, String ngayTao, String tienSuBenh, String trangThai) {
        this.idBenhan = idBenhan;
        this.idBn = idBn;
        this.ngayTao = ngayTao;
        this.tienSuBenh = tienSuBenh;
        this.trangThai = trangThai;
    }

    public String getIdBenhan() { return idBenhan; }
    public void setIdBenhan(String idBenhan) { this.idBenhan = idBenhan; }

    public String getIdBn() { return idBn; }
    public void setIdBn(String idBn) { this.idBn = idBn; }

    public String getNgayTao() { return ngayTao; }
    public void setNgayTao(String ngayTao) { this.ngayTao = ngayTao; }

    public String getTienSuBenh() { return tienSuBenh; }
    public void setTienSuBenh(String tienSuBenh) { this.tienSuBenh = tienSuBenh; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}

