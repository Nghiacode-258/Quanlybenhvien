package com.hms.dto;

public class InvoicePayDto {
    private String idHoadon;
    private String phuongThucTt = "Tiền mặt";
    private String thoiGianThu;

    public InvoicePayDto() {}

    public String getIdHoadon() { return idHoadon; }
    public void setIdHoadon(String idHoadon) { this.idHoadon = idHoadon; }

    public String getPhuongThucTt() { return phuongThucTt; }
    public void setPhuongThucTt(String phuongThucTt) { this.phuongThucTt = phuongThucTt; }

    public String getThoiGianThu() { return thoiGianThu; }
    public void setThoiGianThu(String thoiGianThu) { this.thoiGianThu = thoiGianThu; }
}

