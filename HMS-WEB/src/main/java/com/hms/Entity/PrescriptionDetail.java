package com.hms.Entity;

public class PrescriptionDetail {
    private String idCtdt;
    private String idDt;
    private String idThuoc;
    private Integer soLuong;
    private String lieuDung;
    private String cachDung;
    private String ghiChu;
    private Long donGia;

    // Joined
    private String tenThuoc;
    private String donVi;

    public PrescriptionDetail() {}

    public PrescriptionDetail(String idCtdt, String idDt, String idThuoc, Integer soLuong, String lieuDung, String cachDung, String ghiChu, Long donGia) {
        this.idCtdt = idCtdt;
        this.idDt = idDt;
        this.idThuoc = idThuoc;
        this.soLuong = soLuong;
        this.lieuDung = lieuDung;
        this.cachDung = cachDung;
        this.ghiChu = ghiChu;
        this.donGia = donGia;
    }

    public String getIdCtdt() { return idCtdt; }
    public void setIdCtdt(String idCtdt) { this.idCtdt = idCtdt; }

    public String getIdDt() { return idDt; }
    public void setIdDt(String idDt) { this.idDt = idDt; }

    public String getIdThuoc() { return idThuoc; }
    public void setIdThuoc(String idThuoc) { this.idThuoc = idThuoc; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public String getLieuDung() { return lieuDung; }
    public void setLieuDung(String lieuDung) { this.lieuDung = lieuDung; }

    public String getCachDung() { return cachDung; }
    public void setCachDung(String cachDung) { this.cachDung = cachDung; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public Long getDonGia() { return donGia; }
    public void setDonGia(Long donGia) { this.donGia = donGia; }

    public String getTenThuoc() { return tenThuoc; }
    public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }

    public String getDonVi() { return donVi; }
    public void setDonVi(String donVi) { this.donVi = donVi; }

    public Long getThanhTien() {
        if (soLuong == null || donGia == null) return 0L;
        return soLuong.longValue() * donGia;
    }
}

