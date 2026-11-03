package com.hms.Entity;

public class Diagnosis {
    private String idCd;
    private String maIcd;
    private String tenBenh;

    public Diagnosis() {}

    public Diagnosis(String idCd, String maIcd, String tenBenh) {
        this.idCd = idCd;
        this.maIcd = maIcd;
        this.tenBenh = tenBenh;
    }

    public String getIdCd() { return idCd; }
    public void setIdCd(String idCd) { this.idCd = idCd; }

    public String getMaIcd() { return maIcd; }
    public void setMaIcd(String maIcd) { this.maIcd = maIcd; }

    public String getTenBenh() { return tenBenh; }
    public void setTenBenh(String tenBenh) { this.tenBenh = tenBenh; }

    public String getDisplayName() {
        return maIcd + " — " + tenBenh;
    }
}

