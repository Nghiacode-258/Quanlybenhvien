package com.hms.Entity;

import java.util.ArrayList;
import java.util.List;

public class PatientRelative {
    private String idNt;
    private String idBn;
    private String hoTen;
    private String moiQuanHe;
    private String gioiTinh;
    private List<String> sdt = new ArrayList<>();

    public PatientRelative() {}

    public PatientRelative(String idNt, String idBn, String hoTen, String moiQuanHe, String gioiTinh, List<String> sdt) {
        this.idNt = idNt;
        this.idBn = idBn;
        this.hoTen = hoTen;
        this.moiQuanHe = moiQuanHe;
        this.gioiTinh = gioiTinh;
        this.sdt = sdt != null ? sdt : new ArrayList<>();
    }

    public String getIdNt() { return idNt; }
    public void setIdNt(String idNt) { this.idNt = idNt; }

    public String getIdBn() { return idBn; }
    public void setIdBn(String idBn) { this.idBn = idBn; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getMoiQuanHe() { return moiQuanHe; }
    public void setMoiQuanHe(String moiQuanHe) { this.moiQuanHe = moiQuanHe; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public List<String> getSdt() { return sdt; }
    public void setSdt(List<String> sdt) { this.sdt = sdt; }

    public String getPhoneDisplay() {
        return (sdt != null && !sdt.isEmpty()) ? String.join(", ", sdt) : "—";
    }
}

