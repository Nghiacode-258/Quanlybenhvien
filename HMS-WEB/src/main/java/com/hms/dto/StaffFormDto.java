package com.hms.dto;

public class StaffFormDto {
    private String idNv;
    private String hoTen;
    private String gioiTinh;
    private String ngaySinh;
    private String phone;
    private String email;
    private String vaiTro;
    private String trangThai;
    private String specialtyDetail;
    private String idCk; // Chuyên khoa trực thuộc (chỉ dành cho Bác sĩ)
    private String idPk; // Phòng khám phân công (chỉ dành cho Bác sĩ)
    private String hocVi; // Học vị (chỉ dành cho Bác sĩ)
    private String soCchn; // Số chứng chỉ hành nghề (chỉ dành cho Bác sĩ)

    public StaffFormDto() {}

    public String getIdNv() { return idNv; }
    public void setIdNv(String idNv) { this.idNv = idNv; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getSpecialtyDetail() { return specialtyDetail; }
    public void setSpecialtyDetail(String specialtyDetail) { this.specialtyDetail = specialtyDetail; }
    public String getIdCk() { return idCk; }
    public void setIdCk(String idCk) { this.idCk = idCk; }
    public String getIdPk() { return idPk; }
    public void setIdPk(String idPk) { this.idPk = idPk; }
    public String getHocVi() { return hocVi; }
    public void setHocVi(String hocVi) { this.hocVi = hocVi; }
    public String getSoCchn() { return soCchn; }
    public void setSoCchn(String soCchn) { this.soCchn = soCchn; }
}