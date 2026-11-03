-- 0. XÓA VIEW VÀ BẢNG CŨ (CẢNH BÁO: xóa toàn bộ dữ liệu hiện có)
DROP VIEW IF EXISTS v_donthuoc_tongtien, v_phieukham_tongquan;
DROP TABLE IF EXISTS
    chitietdonthuoc, donthuoc, lothuoc, thuoc,
    phieuthuvienphi, ketquaxetnghiem,
    phieukham_chandoan, phieukham_nhanvien, phieukham,
    loaixetnghiem, chandoan,
    benhan, baohiemyte,
    duocsi, yta, bacsy, nhanvien_sdt, nhanvien,
    phongkham, chuyenkhoa,
    nguoithan_sdt, nguoithan, benhnhan_sdt, benhnhan
CASCADE;

CREATE TABLE benhnhan (
    id_bn VARCHAR(10) PRIMARY KEY,
    ho_ten VARCHAR(100) NOT NULL,
    ngay_sinh DATE,
    cccd CHAR(12),
    gioi_tinh VARCHAR(10)
);
CREATE TABLE benhnhan_sdt (
    id_bn VARCHAR(10),
    sdt VARCHAR(15),
    PRIMARY KEY (id_bn, sdt),
    FOREIGN KEY (id_bn) REFERENCES benhnhan(id_bn) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE TABLE nguoithan (
    id_nt VARCHAR(10) PRIMARY KEY,
    id_bn VARCHAR(10) NOT NULL,
    ho_ten VARCHAR(100) NOT NULL,
    moi_quan_he VARCHAR(50),
    gioi_tinh VARCHAR(10),
    FOREIGN KEY (id_bn) REFERENCES benhnhan(id_bn) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE TABLE nguoithan_sdt (
    id_nt VARCHAR(10),
    sdt VARCHAR(15),
    PRIMARY KEY (id_nt, sdt),
    FOREIGN KEY (id_nt) REFERENCES nguoithan(id_nt) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE TABLE chuyenkhoa (
    id_ck VARCHAR(10) PRIMARY KEY,
    ten_ck VARCHAR(100) NOT NULL UNIQUE
);
CREATE TABLE phongkham (
    id_pk VARCHAR(10) PRIMARY KEY,
    id_ck VARCHAR(10) NOT NULL,
    ten_phong VARCHAR(100) NOT NULL,
    so_phong VARCHAR(10) NOT NULL,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'HOAT_DONG',
    CONSTRAINT chk_pk_trangthai CHECK (trang_thai IN ('HOAT_DONG','TAM_NGUNG','BAO_TRI')),
    CONSTRAINT uq_pk_so_phong UNIQUE (so_phong),
    FOREIGN KEY (id_ck) REFERENCES chuyenkhoa(id_ck) ON DELETE RESTRICT ON UPDATE CASCADE
);
CREATE TABLE nhanvien (
    id_nv VARCHAR(10) PRIMARY KEY,
    ho_ten VARCHAR(100) NOT NULL,
    gioi_tinh VARCHAR(10),
    ngay_sinh DATE,
    vai_tro VARCHAR(20) NOT NULL,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'DANG_LAM',
    CONSTRAINT chk_nv_trangthai CHECK (trang_thai IN ('DANG_LAM','NGHI_VIEC','TAM_NGHI'))
);
CREATE TABLE nhanvien_sdt (
    id_nv VARCHAR(10),
    sdt VARCHAR(15),
    PRIMARY KEY (id_nv, sdt),
    FOREIGN KEY (id_nv) REFERENCES nhanvien(id_nv) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE TABLE bacsy (
    id_nv VARCHAR(10) PRIMARY KEY,
    id_ck VARCHAR(10) NOT NULL,
    hoc_vi VARCHAR(50),
    so_cchn VARCHAR(30) NOT NULL UNIQUE,
    FOREIGN KEY (id_nv) REFERENCES nhanvien(id_nv) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_ck) REFERENCES chuyenkhoa(id_ck) ON DELETE RESTRICT ON UPDATE CASCADE
);
CREATE TABLE yta (
    id_nv VARCHAR(10) PRIMARY KEY,
    cap_bac VARCHAR(50),
    FOREIGN KEY (id_nv) REFERENCES nhanvien(id_nv) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE TABLE duocsi (
    id_nv VARCHAR(10) PRIMARY KEY,
    bang_cap VARCHAR(100),
    FOREIGN KEY (id_nv) REFERENCES nhanvien(id_nv) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE TABLE baohiemyte (
    sobhyt VARCHAR(15) PRIMARY KEY,
    id_bn VARCHAR(10) NOT NULL,
    muc_huong SMALLINT NOT NULL,
    nhom_doi_tuong VARCHAR(50),
    noi_dang_ky VARCHAR(150),
    ngay_bat_dau DATE NOT NULL,
    ngay_ket_thuc DATE NOT NULL,
    CONSTRAINT chk_bhyt_muchuong CHECK (muc_huong BETWEEN 0 AND 100),
    CONSTRAINT chk_bhyt_thoihan CHECK (ngay_ket_thuc >= ngay_bat_dau),
    FOREIGN KEY (id_bn) REFERENCES benhnhan(id_bn) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE TABLE benhan (
    id_benhan VARCHAR(10) PRIMARY KEY,
    id_bn VARCHAR(10) NOT NULL,
    ngay_tao DATE NOT NULL,
    tien_su_benh TEXT,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'DANG_MO',
    CONSTRAINT chk_ba_trangthai CHECK (trang_thai IN ('DANG_MO','DONG')),
    FOREIGN KEY (id_bn) REFERENCES benhnhan(id_bn) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE TABLE chandoan (
    id_icd VARCHAR(10) PRIMARY KEY,
    ten_benh VARCHAR(255) NOT NULL
);
CREATE TABLE loaixetnghiem (
    id_lxn VARCHAR(10) PRIMARY KEY,
    ten_lxn VARCHAR(100) NOT NULL,
    chi_phi DECIMAL(12,2) NOT NULL DEFAULT 0,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'HOAT_DONG',
    CONSTRAINT chk_lxn_chiphi CHECK (chi_phi >= 0),
    CONSTRAINT chk_lxn_trangthai CHECK (trang_thai IN ('HOAT_DONG','NGUNG'))
);
CREATE TABLE phieukham (
    id_phieukham VARCHAR(15) PRIMARY KEY,
    id_benhan VARCHAR(10) NOT NULL,
    id_pk VARCHAR(10) NOT NULL,
    id_bacsy VARCHAR(10),
    sobhyt VARCHAR(15),
    thoi_gian_dky TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    thoi_gian_kham TIMESTAMP,
    trieu_chung TEXT,
    phi_kham DECIMAL(12,2) NOT NULL DEFAULT 0,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'CHO_KHAM',
    CONSTRAINT chk_pkham_phi CHECK (phi_kham >= 0),
    CONSTRAINT chk_pkham_tg CHECK (thoi_gian_kham IS NULL OR thoi_gian_kham >= thoi_gian_dky),
    CONSTRAINT chk_pkham_trangthai CHECK (trang_thai IN ('CHO_KHAM','DANG_KHAM','DA_KHAM','HUY')),
    FOREIGN KEY (id_benhan) REFERENCES benhan(id_benhan) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (id_pk) REFERENCES phongkham(id_pk) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (id_bacsy) REFERENCES bacsy(id_nv) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (sobhyt) REFERENCES baohiemyte(sobhyt) ON DELETE SET NULL ON UPDATE CASCADE
);
CREATE INDEX idx_pkham_thoigian ON phieukham(thoi_gian_dky);
CREATE INDEX idx_pkham_trangthai ON phieukham(trang_thai);
CREATE TABLE phieukham_nhanvien (
    id_phieukham VARCHAR(15),
    id_nv VARCHAR(10),
    thoi_gian_tiep_nhan TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_phieukham, id_nv),
    FOREIGN KEY (id_phieukham) REFERENCES phieukham(id_phieukham) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_nv) REFERENCES nhanvien(id_nv) ON DELETE RESTRICT ON UPDATE CASCADE
);
CREATE TABLE phieukham_chandoan (
    id_phieukham VARCHAR(15),
    id_icd VARCHAR(10),
    loai VARCHAR(10) NOT NULL DEFAULT 'CHINH',
    ghi_chu VARCHAR(255),
    PRIMARY KEY (id_phieukham, id_icd),
    CONSTRAINT chk_pkcd_loai CHECK (loai IN ('CHINH','PHU')),
    FOREIGN KEY (id_phieukham) REFERENCES phieukham(id_phieukham) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_icd) REFERENCES chandoan(id_icd) ON DELETE RESTRICT ON UPDATE CASCADE
);
CREATE TABLE ketquaxetnghiem (
    id_kqxn VARCHAR(15) PRIMARY KEY,
    id_phieukham VARCHAR(15) NOT NULL,
    id_lxn VARCHAR(10) NOT NULL,
    ket_qua TEXT,
    ket_luan TEXT,
    thoi_gian TIMESTAMP,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'CHO_KET_QUA',
    CONSTRAINT chk_kqxn_trangthai CHECK (trang_thai IN ('CHO_KET_QUA','DA_CO_KET_QUA','HUY')),
    FOREIGN KEY (id_phieukham) REFERENCES phieukham(id_phieukham) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_lxn) REFERENCES loaixetnghiem(id_lxn) ON DELETE RESTRICT ON UPDATE CASCADE
);
CREATE TABLE phieuthuvienphi (
    id_hoadon VARCHAR(15) PRIMARY KEY,
    id_phieukham VARCHAR(15) NOT NULL,
    id_nv VARCHAR(10) NOT NULL,
    so_tien DECIMAL(12,2) NOT NULL,
    loai_thu VARCHAR(20) NOT NULL,
    phuong_thuc_tt VARCHAR(20) NOT NULL,
    thoi_gian_thu TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'DA_THU',
    CONSTRAINT chk_pt_sotien CHECK (so_tien >= 0),
    CONSTRAINT chk_pt_loai CHECK (loai_thu IN ('TAM_UNG','THANH_TOAN','HOAN_TIEN')),
    CONSTRAINT chk_pt_ptt CHECK (phuong_thuc_tt IN ('TIEN_MAT','CHUYEN_KHOAN','THE')),
    CONSTRAINT chk_pt_trangthai CHECK (trang_thai IN ('DA_THU','HUY')),
    FOREIGN KEY (id_phieukham) REFERENCES phieukham(id_phieukham) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (id_nv) REFERENCES nhanvien(id_nv) ON DELETE RESTRICT ON UPDATE CASCADE
);
CREATE TABLE thuoc (
    id_thuoc VARCHAR(10) PRIMARY KEY,
    ten_thuoc VARCHAR(150) NOT NULL,
    nha_san_xuat VARCHAR(150),
    can_ke_don BOOLEAN NOT NULL DEFAULT TRUE,
    so_luong_ton_kho INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_thuoc_tonkho CHECK (so_luong_ton_kho >= 0)
);
CREATE TABLE lothuoc (
    id_lo VARCHAR(10) PRIMARY KEY,
    id_thuoc VARCHAR(10) NOT NULL,
    so_lo VARCHAR(30) NOT NULL,
    han_su_dung DATE NOT NULL,
    so_luong_nhap INTEGER NOT NULL,
    so_luong_con INTEGER NOT NULL,
    gia_nhap DECIMAL(12,2) NOT NULL,
    gia_ban DECIMAL(12,2) NOT NULL,
    CONSTRAINT uq_lo UNIQUE (id_thuoc, so_lo),
    CONSTRAINT chk_lo_sl CHECK (so_luong_nhap >= 0 AND so_luong_con >= 0 AND so_luong_con <= so_luong_nhap),
    CONSTRAINT chk_lo_gia CHECK (gia_nhap >= 0 AND gia_ban >= 0),
    FOREIGN KEY (id_thuoc) REFERENCES thuoc(id_thuoc) ON DELETE RESTRICT ON UPDATE CASCADE
);
CREATE INDEX idx_lo_hsd ON lothuoc(han_su_dung);
CREATE TABLE donthuoc (
    id_dt VARCHAR(15) PRIMARY KEY,
    id_phieukham VARCHAR(15) NOT NULL,
    id_duocsi VARCHAR(10),
    ngay_ke DATE NOT NULL,
    ghi_chu VARCHAR(255),
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'CHO_CAP_PHAT',
    CONSTRAINT chk_dt_trangthai CHECK (trang_thai IN ('CHO_CAP_PHAT','DA_CAP_PHAT','HUY')),
    FOREIGN KEY (id_phieukham) REFERENCES phieukham(id_phieukham) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (id_duocsi) REFERENCES duocsi(id_nv) ON DELETE SET NULL ON UPDATE CASCADE
);
CREATE TABLE chitietdonthuoc (
    id_dt VARCHAR(15),
    id_thuoc VARCHAR(10),
    so_luong INTEGER NOT NULL,
    lieu_dung VARCHAR(100),
    cach_dung VARCHAR(255),
    ghi_chu VARCHAR(255),
    don_gia DECIMAL(12,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (id_dt, id_thuoc),
    CONSTRAINT chk_ctdt_sl CHECK (so_luong > 0),
    CONSTRAINT chk_ctdt_dongia CHECK (don_gia >= 0),
    FOREIGN KEY (id_dt) REFERENCES donthuoc(id_dt) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_thuoc) REFERENCES thuoc(id_thuoc) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE OR REPLACE VIEW v_donthuoc_tongtien AS
SELECT dt.id_dt, COALESCE(SUM(ct.so_luong * ct.don_gia), 0) AS tong_tien
FROM donthuoc dt LEFT JOIN chitietdonthuoc ct ON ct.id_dt = dt.id_dt
GROUP BY dt.id_dt;

CREATE OR REPLACE VIEW v_phieukham_tongquan AS
SELECT pk.*,
       EXISTS (SELECT 1 FROM ketquaxetnghiem kq WHERE kq.id_phieukham = pk.id_phieukham) AS co_xn,
       EXISTS (SELECT 1 FROM donthuoc dt WHERE dt.id_phieukham = pk.id_phieukham) AS ke_don
FROM phieukham pk;

-- ============================================================================
-- MỞ RỘNG cho giao diện hiện tại (code 2 gốc không có các cột này)
-- ============================================================================
ALTER TABLE thuoc    ADD COLUMN IF NOT EXISTS don_vi VARCHAR(20) DEFAULT 'viên';
ALTER TABLE thuoc    ADD COLUMN IF NOT EXISTS gia_ban DECIMAL(12,2) NOT NULL DEFAULT 0;
ALTER TABLE lothuoc  ADD COLUMN IF NOT EXISTS ngay_nhap DATE DEFAULT CURRENT_DATE;
ALTER TABLE lothuoc  ADD COLUMN IF NOT EXISTS ngay_san_xuat DATE;
ALTER TABLE lothuoc  ADD COLUMN IF NOT EXISTS nha_cung_cap VARCHAR(150);
ALTER TABLE lothuoc  ADD COLUMN IF NOT EXISTS trang_thai VARCHAR(20) NOT NULL DEFAULT 'HOAT_DONG';
ALTER TABLE benhnhan ADD COLUMN IF NOT EXISTS dia_chi VARCHAR(255);
ALTER TABLE benhnhan ADD COLUMN IF NOT EXISTS ngay_tao DATE DEFAULT CURRENT_DATE;
ALTER TABLE nhanvien ADD COLUMN IF NOT EXISTS email VARCHAR(100);
