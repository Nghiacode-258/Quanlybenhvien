package com.hms.dto;

public class MedicineFormDto {
    private String idThuoc;
    private String tenThuoc;
    // private String hoatChat;
    // private String hamLuong;
    private String nhaSanXuat;
    private Boolean canKeDon = false;
    private String donVi;
    private Long giaBan;

    public MedicineFormDto() {}

    public String getIdThuoc() { return idThuoc; }
    public void setIdThuoc(String idThuoc) { this.idThuoc = idThuoc; }

    public String getTenThuoc() { return tenThuoc; }
    public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }

    // public String getHoatChat() { return hoatChat; }
    // public void setHoatChat(String hoatChat) { this.hoatChat = hoatChat; }

    // public String getHamLuong() { return hamLuong; }
    // public void setHamLuong(String hamLuong) { this.hamLuong = hamLuong; }

    public String getNhaSanXuat() { return nhaSanXuat; }
    public void setNhaSanXuat(String nhaSanXuat) { this.nhaSanXuat = nhaSanXuat; }

    public Boolean getCanKeDon() { return canKeDon; }
    public void setCanKeDon(Boolean canKeDon) { this.canKeDon = canKeDon; }

    public String getDonVi() { return donVi; }
    public void setDonVi(String donVi) { this.donVi = donVi; }

    public Long getGiaBan() { return giaBan; }
    public void setGiaBan(Long giaBan) { this.giaBan = giaBan; }
}

