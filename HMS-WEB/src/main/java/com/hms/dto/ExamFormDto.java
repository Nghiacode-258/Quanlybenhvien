package com.hms.dto;

public class ExamFormDto {
    private String idBn;
    private String idPk;
    private String idNv;
    private String ngayDky;
    private Long phiKham = 150000L;
    private String thoiGianKham;
    private String trieuChung;

    public ExamFormDto() {}

    public String getIdBn() { return idBn; }
    public void setIdBn(String idBn) { this.idBn = idBn; }

    public String getIdPk() { return idPk; }
    public void setIdPk(String idPk) { this.idPk = idPk; }

    public String getIdNv() { return idNv; }
    public void setIdNv(String idNv) { this.idNv = idNv; }

    public String getNgayDky() { return ngayDky; }
    public void setNgayDky(String ngayDky) { this.ngayDky = ngayDky; }

    public Long getPhiKham() { return phiKham; }
    public void setPhiKham(Long phiKham) { this.phiKham = phiKham; }

    public String getThoiGianKham() { return thoiGianKham; }
    public void setThoiGianKham(String thoiGianKham) { this.thoiGianKham = thoiGianKham; }

    public String getTrieuChung() { return trieuChung; }
    public void setTrieuChung(String trieuChung) { this.trieuChung = trieuChung; }
}

