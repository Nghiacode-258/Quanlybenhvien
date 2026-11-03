package com.hms.Entity;

public class LabType {
    private String idLxn;
    private String tenLxn;
    private Long chiPhi;
    private String trangThai; // "Hoạt động"

    public LabType() {}

    public LabType(String idLxn, String tenLxn, Long chiPhi, String trangThai) {
        this.idLxn = idLxn;
        this.tenLxn = tenLxn;
        this.chiPhi = chiPhi;
        this.trangThai = trangThai;
    }

    public String getIdLxn() { return idLxn; }
    public void setIdLxn(String idLxn) { this.idLxn = idLxn; }

    public String getTenLxn() { return tenLxn; }
    public void setTenLxn(String tenLxn) { this.tenLxn = tenLxn; }

    public Long getChiPhi() { return chiPhi; }
    public void setChiPhi(Long chiPhi) { this.chiPhi = chiPhi; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}

