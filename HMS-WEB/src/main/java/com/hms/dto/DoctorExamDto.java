package com.hms.dto;

import java.util.ArrayList;
import java.util.List;

public class DoctorExamDto {
    private String idPhieukham;
    private String idNv;
    private String thoiGianKham;
    private String idCd;
    private String trieuChung;
    private String ketLuan;
    private List<String> labIds = new ArrayList<>();
    
    // Prescription
    private String rxNgayKe;
    private String rxGhiChu;
    private List<String> medicineId = new ArrayList<>();
    private List<Integer> quantity = new ArrayList<>();
    private List<String> dosage = new ArrayList<>();
    private List<String> usage = new ArrayList<>();
    private List<String> medicineNote = new ArrayList<>();

    public DoctorExamDto() {}

    public String getIdPhieukham() { return idPhieukham; }
    public void setIdPhieukham(String idPhieukham) { this.idPhieukham = idPhieukham; }

    public String getIdNv() { return idNv; }
    public void setIdNv(String idNv) { this.idNv = idNv; }

    public String getThoiGianKham() { return thoiGianKham; }
    public void setThoiGianKham(String thoiGianKham) { this.thoiGianKham = thoiGianKham; }

    public String getIdCd() { return idCd; }
    public void setIdCd(String idCd) { this.idCd = idCd; }

    public String getTrieuChung() { return trieuChung; }
    public void setTrieuChung(String trieuChung) { this.trieuChung = trieuChung; }

    public String getKetLuan() { return ketLuan; }
    public void setKetLuan(String ketLuan) { this.ketLuan = ketLuan; }

    public List<String> getLabIds() { return labIds; }
    public void setLabIds(List<String> labIds) { this.labIds = labIds != null ? labIds : new ArrayList<>(); }

    public String getRxNgayKe() { return rxNgayKe; }
    public void setRxNgayKe(String rxNgayKe) { this.rxNgayKe = rxNgayKe; }

    public String getRxGhiChu() { return rxGhiChu; }
    public void setRxGhiChu(String rxGhiChu) { this.rxGhiChu = rxGhiChu; }

    public List<String> getMedicineId() { return medicineId; }
    public void setMedicineId(List<String> medicineId) { this.medicineId = medicineId != null ? medicineId : new ArrayList<>(); }

    public List<Integer> getQuantity() { return quantity; }
    public void setQuantity(List<Integer> quantity) { this.quantity = quantity != null ? quantity : new ArrayList<>(); }

    public List<String> getDosage() { return dosage; }
    public void setDosage(List<String> dosage) { this.dosage = dosage != null ? dosage : new ArrayList<>(); }

    public List<String> getUsage() { return usage; }
    public void setUsage(List<String> usage) { this.usage = usage != null ? usage : new ArrayList<>(); }

    public List<String> getMedicineNote() { return medicineNote; }
    public void setMedicineNote(List<String> medicineNote) { this.medicineNote = medicineNote != null ? medicineNote : new ArrayList<>(); }
}

