-- ============================================================================
-- MIGRATION cho ứng dụng HMS-WEB (chạy SAU db/schema.sql, TRƯỚC db/seed.sql)
-- Script idempotent: chạy lại nhiều lần không lỗi, KHÔNG xóa dữ liệu.
-- Chạy trong Supabase > SQL Editor.
-- ============================================================================

-- 1. Bệnh nhân: trạng thái theo dõi hiển thị trên giao diện ("Ổn định", "Đang theo dõi"...)
ALTER TABLE benhnhan ADD COLUMN IF NOT EXISTS trang_thai VARCHAR(30) NOT NULL DEFAULT 'Ổn định';

-- 2. Nhân viên: mô tả chuyên môn tự do (vd "Thạc sĩ · Nội tổng hợp"), dùng cho cả Thu ngân/Quản lý
--    vốn không có bảng con riêng.
ALTER TABLE nhanvien ADD COLUMN IF NOT EXISTS chi_tiet VARCHAR(150);

-- 3. Bác sĩ: form thêm nhân sự chưa nhập chuyên khoa / số CCHN => cho phép NULL (UNIQUE vẫn giữ,
--    Postgres cho phép nhiều NULL). Thêm phòng khám được phân công.
ALTER TABLE bacsy ALTER COLUMN id_ck DROP NOT NULL;
ALTER TABLE bacsy ALTER COLUMN so_cchn DROP NOT NULL;
ALTER TABLE bacsy ADD COLUMN IF NOT EXISTS id_pk VARCHAR(10)
    REFERENCES phongkham(id_pk) ON DELETE SET NULL ON UPDATE CASCADE;

-- 4. Phiếu khám: thêm kết luận; mở rộng trạng thái theo luồng khám -> xét nghiệm -> thanh toán;
--    bỏ ràng buộc thoi_gian_kham >= thoi_gian_dky (ứng dụng dùng thoi_gian_kham là giờ hẹn khám).
ALTER TABLE phieukham ADD COLUMN IF NOT EXISTS ket_luan TEXT;
ALTER TABLE phieukham DROP CONSTRAINT IF EXISTS chk_pkham_tg;
ALTER TABLE phieukham DROP CONSTRAINT IF EXISTS chk_pkham_trangthai;
ALTER TABLE phieukham ADD CONSTRAINT chk_pkham_trangthai CHECK (trang_thai IN
    ('CHO_KHAM','DANG_KHAM','CHO_XET_NGHIEM','CHO_THANH_TOAN','DA_KHAM','HOAN_TAT','HUY'));

-- 5. Phiếu thu viện phí: ứng dụng tạo hóa đơn "chưa thu" ngay khi đăng ký khám, sau đó thu tiền.
--    => cho phép trạng thái CHUA_THU, và các cột thông tin thu tiền được NULL cho tới khi thu.
ALTER TABLE phieuthuvienphi ALTER COLUMN id_nv DROP NOT NULL;
ALTER TABLE phieuthuvienphi ALTER COLUMN phuong_thuc_tt DROP NOT NULL;
ALTER TABLE phieuthuvienphi ALTER COLUMN thoi_gian_thu DROP NOT NULL;
ALTER TABLE phieuthuvienphi ALTER COLUMN thoi_gian_thu DROP DEFAULT;
ALTER TABLE phieuthuvienphi ALTER COLUMN trang_thai SET DEFAULT 'CHUA_THU';
ALTER TABLE phieuthuvienphi DROP CONSTRAINT IF EXISTS chk_pt_loai;
ALTER TABLE phieuthuvienphi ALTER COLUMN loai_thu TYPE VARCHAR(100);
ALTER TABLE phieuthuvienphi DROP CONSTRAINT IF EXISTS chk_pt_ptt;
ALTER TABLE phieuthuvienphi ADD CONSTRAINT chk_pt_ptt CHECK
    (phuong_thuc_tt IS NULL OR phuong_thuc_tt IN ('TIEN_MAT','CHUYEN_KHOAN','THE','BAO_HIEM'));
ALTER TABLE phieuthuvienphi DROP CONSTRAINT IF EXISTS chk_pt_trangthai;
ALTER TABLE phieuthuvienphi ADD CONSTRAINT chk_pt_trangthai CHECK
    (trang_thai IN ('CHUA_THU','DA_THU','HUY'));
CREATE INDEX IF NOT EXISTS idx_pt_phieukham ON phieuthuvienphi(id_phieukham);

-- 6. Lô thuốc: trạng thái dạng mã
ALTER TABLE lothuoc DROP CONSTRAINT IF EXISTS chk_lo_trangthai;
ALTER TABLE lothuoc ADD CONSTRAINT chk_lo_trangthai CHECK (trang_thai IN ('HOAT_DONG','DA_LOAI_BO'));

-- 7. Index phục vụ các truy vấn của ứng dụng
CREATE INDEX IF NOT EXISTS idx_benhan_bn      ON benhan(id_bn);
CREATE INDEX IF NOT EXISTS idx_pkham_benhan   ON phieukham(id_benhan);
CREATE INDEX IF NOT EXISTS idx_pkham_bacsy    ON phieukham(id_bacsy);
CREATE INDEX IF NOT EXISTS idx_kqxn_phieukham ON ketquaxetnghiem(id_phieukham);
CREATE INDEX IF NOT EXISTS idx_dt_phieukham   ON donthuoc(id_phieukham);

-- 8. Bảng mới tạo ra từ migration này cũng cần bật RLS (giống db/rls.sql)
DO $$
DECLARE t text;
BEGIN
  FOR t IN SELECT tablename FROM pg_tables WHERE schemaname = 'public' LOOP
    EXECUTE format('ALTER TABLE public.%I ENABLE ROW LEVEL SECURITY', t);
  END LOOP;
END $$;
