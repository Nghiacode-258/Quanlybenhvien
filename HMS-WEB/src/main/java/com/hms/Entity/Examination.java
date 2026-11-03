package com.hms.Entity;

import java.util.ArrayList;
import java.util.List;

public class Examination {
    private String idPhieukham;
    private String idBn;
    private String idPk;
    private String idNv;
    private String idBenhan;
    private String idCd;
    private String ngayDky;
    private String thoiGianKham;
    private String trieuChung;
    private Long phiKham;
    private String trangThai; // "Chờ khám", "Chờ xét nghiệm", "Chờ thanh toán", "Đã khám", "Hoàn tất"
    private String ketLuan;

    // Joined fields
    private String patientName;
    private String patientPhone;
    private String roomName;
    private String doctorName;
    private String diagnosisName;
    private String icdCode;

    private Prescription prescription;
    private List<LabResult> labResults = new ArrayList<>();

    public Examination() {}

    public Examination(String idPhieukham, String idBn, String idPk, String idNv, String idBenhan, String idCd, String ngayDky, String thoiGianKham, String trieuChung, Long phiKham, String trangThai) {
        this.idPhieukham = idPhieukham;
        this.idBn = idBn;
        this.idPk = idPk;
        this.idNv = idNv;
        this.idBenhan = idBenhan;
        this.idCd = idCd;
        this.ngayDky = ngayDky;
        this.thoiGianKham = thoiGianKham;
        this.trieuChung = trieuChung;
        this.phiKham = phiKham;
        this.trangThai = trangThai;
    }

    public String getIdPhieukham() { return idPhieukham; }
    public void setIdPhieukham(String idPhieukham) { this.idPhieukham = idPhieukham; }

    public String getIdBn() { return idBn; }
    public void setIdBn(String idBn) { this.idBn = idBn; }

    public String getIdPk() { return idPk; }
    public void setIdPk(String idPk) { this.idPk = idPk; }

    public String getIdNv() { return idNv; }
    public void setIdNv(String idNv) { this.idNv = idNv; }

    public String getIdBenhan() { return idBenhan; }
    public void setIdBenhan(String idBenhan) { this.idBenhan = idBenhan; }

    public String getIdCd() { return idCd; }
    public void setIdCd(String idCd) { this.idCd = idCd; }

    public String getNgayDky() { return ngayDky; }
    public void setNgayDky(String ngayDky) { this.ngayDky = ngayDky; }

    public String getThoiGianKham() { return thoiGianKham; }
    public void setThoiGianKham(String thoiGianKham) { this.thoiGianKham = thoiGianKham; }

    public String getTrieuChung() { return trieuChung; }
    public void setTrieuChung(String trieuChung) { this.trieuChung = trieuChung; }

    public Long getPhiKham() { return phiKham; }
    public void setPhiKham(Long phiKham) { this.phiKham = phiKham; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getKetLuan() { return ketLuan; }
    public void setKetLuan(String ketLuan) { this.ketLuan = ketLuan; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDiagnosisName() { return diagnosisName; }
    public void setDiagnosisName(String diagnosisName) { this.diagnosisName = diagnosisName; }

    public String getIcdCode() { return icdCode; }
    public void setIcdCode(String icdCode) { this.icdCode = icdCode; }

    public Prescription getPrescription() { return prescription; }
    public void setPrescription(Prescription prescription) { this.prescription = prescription; }

    public List<LabResult> getLabResults() { return labResults; }
    public void setLabResults(List<LabResult> labResults) { this.labResults = labResults != null ? labResults : new ArrayList<>(); }

    public boolean hasPrescription() {
        return prescription != null;
    }

    public boolean hasPrescriptionItems() {
        return prescription != null && prescription.getItems() != null && !prescription.getItems().isEmpty();
    }

    public boolean hasLab(String labId) {
        if (labResults == null || labId == null) return false;
        return labResults.stream().anyMatch(lr -> labId.equals(lr.getIdLxn()));
    }

    public String getInitials() {
        if (patientName == null || patientName.trim().isEmpty()) return "BN";
        String[] parts = patientName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[parts.length - 2].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}

