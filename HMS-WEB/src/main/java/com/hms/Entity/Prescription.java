package com.hms.Entity;

import java.util.ArrayList;
import java.util.List;

public class Prescription {
    private String idDt;
    private String idPhieukham;
    private String idNv;
    private String ngayKe;
    private Long tongTien;
    private String ghiChu;
    private String trangThai; // "Chờ cấp phát", "Đã cấp phát"

    // Joined fields
    private String patientName;
    private String patientId;
    private String doctorName;
    private List<PrescriptionDetail> items = new ArrayList<>();

    public Prescription() {}

    public Prescription(String idDt, String idPhieukham, String idNv, String ngayKe, Long tongTien, String ghiChu, String trangThai) {
        this.idDt = idDt;
        this.idPhieukham = idPhieukham;
        this.idNv = idNv;
        this.ngayKe = ngayKe;
        this.tongTien = tongTien;
        this.ghiChu = ghiChu;
        this.trangThai = trangThai;
    }

    public String getIdDt() { return idDt; }
    public void setIdDt(String idDt) { this.idDt = idDt; }

    public String getIdPhieukham() { return idPhieukham; }
    public void setIdPhieukham(String idPhieukham) { this.idPhieukham = idPhieukham; }

    public String getIdNv() { return idNv; }
    public void setIdNv(String idNv) { this.idNv = idNv; }

    public String getNgayKe() { return ngayKe; }
    public void setNgayKe(String ngayKe) { this.ngayKe = ngayKe; }

    public Long getTongTien() { return tongTien; }
    public void setTongTien(Long tongTien) { this.tongTien = tongTien; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public List<PrescriptionDetail> getItems() { return items; }
    public void setItems(List<PrescriptionDetail> items) { this.items = items != null ? items : new ArrayList<>(); }

    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public String getItemsSummary() {
        if (items == null || items.isEmpty()) return "Chưa có chi tiết thuốc";
        List<String> parts = new ArrayList<>();
        for (PrescriptionDetail d : items) {
            parts.add((d.getTenThuoc() != null ? d.getTenThuoc() : d.getIdThuoc()) + " × " + d.getSoLuong());
        }
        return String.join("; ", parts);
    }
}

