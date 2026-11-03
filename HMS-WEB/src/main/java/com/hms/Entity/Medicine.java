package com.hms.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table; 
@Entity
@Table(name = "thuoc")
public class Medicine {
    @Id 
    private String idThuoc;

    @Column(name = "ten_thuoc")
    private String tenThuoc;

    // private String hoatChat;
    // private String hamLuong;

    @Column(name = "nha_san_xuat")
    private String nhaSanXuat;

    @Column(name = "can_ke_don")
    private Boolean canKeDon = false;
    @Column(name = "so_luong_ton_kho")
    private Integer soLuongTonKho = 0;
    @Column(name = "don_vi")
    private String donVi; // "viên", "gói", "chai", v.v.
    @Column(name = "gia_ban")
    private Long giaBan = 1000L;
    // private String trangThai = "Đang kinh doanh"; // "Đang kinh doanh", "Ngừng kinh doanh"

    public Medicine() {}

    public Medicine(String idThuoc, String tenThuoc, String nhaSanXuat, Boolean canKeDon, Integer soLuongTonKho, String donVi, Long giaBan) {
        this.idThuoc = idThuoc;
        this.tenThuoc = tenThuoc;
        this.nhaSanXuat = nhaSanXuat;
        this.canKeDon = canKeDon;
        this.soLuongTonKho = soLuongTonKho;
        this.donVi = donVi;
        // this.hoatChat = "";
        // this.hamLuong = "";
        this.giaBan = giaBan;
        // this.trangThai = "Đang kinh doanh";
    }

    // public Medicine(String idThuoc, String tenThuoc, String hoatChat, String hamLuong, String nhaSanXuat, Boolean canKeDon, Integer soLuongTonKho, String donVi, Long giaBan, String trangThai) {
    //     this.idThuoc = idThuoc;
    //     this.tenThuoc = tenThuoc;
    //     // this.hoatChat = hoatChat;
    //     // this.hamLuong = hamLuong;
    //     this.nhaSanXuat = nhaSanXuat;
    //     this.canKeDon = canKeDon;
    //     this.soLuongTonKho = soLuongTonKho;
    //     this.donVi = donVi;
    //     this.giaBan = giaBan != null ? giaBan : 1000L;
    //     // this.trangThai = trangThai != null ? trangThai : "Đang kinh doanh";
    // }

    public String getIdThuoc() { return idThuoc; }
    public String setIdThuoc(String idThuoc) { return this.idThuoc = idThuoc; }

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

    public Integer getSoLuongTonKho() { return soLuongTonKho; }
    public void setSoLuongTonKho(Integer soLuongTonKho) { this.soLuongTonKho = soLuongTonKho; }

    public String getDonVi() { return donVi; }
    public void setDonVi(String donVi) { this.donVi = donVi; }

    public Long getGiaBan() { return giaBan; }
    public void setGiaBan(Long giaBan) { this.giaBan = giaBan; }

    // public String getTrangThai() { return trangThai; }
    // public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    // public boolean isDiscontinued() {
    //     return "Ngừng kinh doanh".equalsIgnoreCase(trangThai);
    // }

    
    public String getInventoryBadgeClass() {
        // if (isDiscontinued()) return "pill-danger";
        if (soLuongTonKho == null || soLuongTonKho <= 0) return "pill-danger";
        if (soLuongTonKho < 30) return "pill-danger";
        if (soLuongTonKho < 50) return "pill-warning";
        return "pill-success";
    }

    public String getInventoryStatusText() {
        // if (isDiscontinued()) return "Ngừng kinh doanh";
        if (soLuongTonKho == null || soLuongTonKho <= 0) return "Hết hàng";
        if (soLuongTonKho < 30) return "Sắp hết";
        if (soLuongTonKho < 50) return "Cần bổ sung";
        return "Bình thường";
    }
}
