package com.hms.Entity;

public class Specialty {
    private String idCk;
    private String tenCk;

    public Specialty() {}

    public Specialty(String idCk, String tenCk) {
        this.idCk = idCk;
        this.tenCk = tenCk;
    }

    public String getIdCk() { return idCk; }
    public void setIdCk(String idCk) { this.idCk = idCk; }

    public String getTenCk() { return tenCk; }
    public void setTenCk(String tenCk) { this.tenCk = tenCk; }
}

