package com.hms.Entity;

public class LabResult {
    private String idKqxn;
    private String idPhieukham;
    private String idLxn;
    private String ketQua;
    private String ketLuan;
    private String thoiGian;
    private String trangThai; // "Hoàn thành", "Chờ lấy mẫu"

    // Joined fields
    private String tenLxn;
    private Long chiPhi;
    private String patientName;
    private String patientId;

    public LabResult() {}

    public LabResult(String idKqxn, String idPhieukham, String idLxn, String ketQua, String ketLuan, String thoiGian, String trangThai) {
        this.idKqxn = idKqxn;
        this.idPhieukham = idPhieukham;
        this.idLxn = idLxn;
        this.ketQua = ketQua;
        this.ketLuan = ketLuan;
        this.thoiGian = thoiGian;
        this.trangThai = trangThai;
    }

    public String getIdKqxn() { return idKqxn; }
    public void setIdKqxn(String idKqxn) { this.idKqxn = idKqxn; }

    public String getIdPhieukham() { return idPhieukham; }
    public void setIdPhieukham(String idPhieukham) { this.idPhieukham = idPhieukham; }

    public String getIdLxn() { return idLxn; }
    public void setIdLxn(String idLxn) { this.idLxn = idLxn; }

    public String getKetQua() { return ketQua; }
    public void setKetQua(String ketQua) { this.ketQua = ketQua; }

    public String getKetLuan() { return ketLuan; }
    public void setKetLuan(String ketLuan) { this.ketLuan = ketLuan; }

    public String getThoiGian() { return thoiGian; }
    public void setThoiGian(String thoiGian) { this.thoiGian = thoiGian; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getTenLxn() { return tenLxn; }
    public void setTenLxn(String tenLxn) { this.tenLxn = tenLxn; }

    public Long getChiPhi() { return chiPhi; }
    public void setChiPhi(Long chiPhi) { this.chiPhi = chiPhi; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
}

