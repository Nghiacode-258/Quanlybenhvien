-- ============================================================================
-- SEED dữ liệu mẫu (chuyển từ MockDataStore cũ) — chạy SAU db/schema.sql và db/migration_app.sql
-- Idempotent: dùng ON CONFLICT DO NOTHING nên chạy lại không trùng, không ghi đè dữ liệu đã sửa.
-- Các nhân viên NV001..NV006 là tài khoản mô phỏng của 5 vai trò trong màn hình chọn vai trò
-- (xem com.hms.Entity.Role) nên BẮT BUỘC phải có.
-- ============================================================================
DO $$
DECLARE
  today date := (now() AT TIME ZONE 'Asia/Ho_Chi_Minh')::date;
BEGIN

-- 1. Chuyên khoa, phòng khám
INSERT INTO chuyenkhoa (id_ck, ten_ck) VALUES
  ('CK01', 'Nội tổng hợp'), ('CK02', 'Tim mạch'), ('CK03', 'Xét nghiệm'), ('CK04', 'Nhi khoa')
ON CONFLICT DO NOTHING;

INSERT INTO phongkham (id_pk, id_ck, ten_phong, so_phong, trang_thai) VALUES
  ('PK01', 'CK01', 'Phòng khám Nội tổng hợp', 'A-101', 'HOAT_DONG'),
  ('PK02', 'CK02', 'Phòng khám Tim mạch',     'A-202', 'HOAT_DONG'),
  ('PK03', 'CK03', 'Phòng xét nghiệm',        'B-105', 'HOAT_DONG'),
  ('PK04', 'CK04', 'Phòng khám Nhi',          'C-102', 'HOAT_DONG')
ON CONFLICT DO NOTHING;

-- 2. Nhân viên
INSERT INTO nhanvien (id_nv, ho_ten, gioi_tinh, ngay_sinh, vai_tro, trang_thai, email, chi_tiet) VALUES
  ('NV001', 'BS. Nguyễn Hoàng An',        'Nam', '1980-02-12', 'Bác sĩ',   'DANG_LAM', 'an.nguyen@hms.local',     'Thạc sĩ · Nội tổng hợp'),
  ('NV002', 'BS. Trần Mai Phương',        'Nữ',  '1985-09-21', 'Bác sĩ',   'DANG_LAM', 'phuong.tran@hms.local',   'Bác sĩ CKI · Tim mạch'),
  ('NV003', 'Điều dưỡng Lê Thu Trang',    'Nữ',  '1992-06-07', 'Y tá',     'DANG_LAM', 'trang.le@hms.local',      'Điều dưỡng đại học'),
  ('NV004', 'DS. Phạm Quốc Huy',          'Nam', '1988-04-18', 'Dược sĩ',  'DANG_LAM', 'huy.pham@hms.local',      'Dược sĩ đại học'),
  ('NV005', 'Nguyễn Hải Yến',             'Nữ',  '1991-12-03', 'Thu ngân', 'DANG_LAM', 'yen.nguyen@hms.local',    'Chuyên viên viện phí'),
  ('NV006', 'Đỗ Văn Khánh',               'Nam', '1978-08-13', 'Quản lý',  'DANG_LAM', 'khanh.do@hms.local',      'Ban Giám đốc'),
  ('NV007', 'Điều dưỡng Hoàng Minh Thảo', 'Nữ',  '1994-03-15', 'Y tá',     'DANG_LAM', 'thao.hoang@hms.local',    'Cử nhân điều dưỡng'),
  ('NV008', 'DS. Vũ Thị Mai',             'Nữ',  '1990-11-20', 'Dược sĩ',  'DANG_LAM', 'mai.vu@hms.local',        'Dược sĩ lâm sàng'),
  ('NV009', 'Đỗ Hoàng Long',              'Nam', '1993-05-10', 'Thu ngân', 'DANG_LAM', 'long.do@hms.local',       'Kế toán viên viện phí'),
  ('NV010', 'Trần Đình Trọng',            'Nam', '1982-10-05', 'Quản lý',  'DANG_LAM', 'trong.tran@hms.local',    'Phó Giám đốc điều hành'),
  ('NV011', 'BS. Lê Hoàng Nam',           'Nam', '1989-07-25', 'Bác sĩ',   'DANG_LAM', 'nam.le@hms.local',        'Bác sĩ CKI · Nhi khoa')
ON CONFLICT DO NOTHING;

INSERT INTO nhanvien_sdt (id_nv, sdt) VALUES
  ('NV001','0903001001'), ('NV002','0903001002'), ('NV003','0903001003'), ('NV004','0903001004'),
  ('NV005','0903001005'), ('NV006','0903001006'), ('NV007','0903001007'), ('NV008','0903001008'),
  ('NV009','0903001009'), ('NV010','0903001010'), ('NV011','0903001011')
ON CONFLICT DO NOTHING;

INSERT INTO bacsy (id_nv, id_ck, hoc_vi, so_cchn, id_pk) VALUES
  ('NV001', 'CK01', 'Thạc sĩ',     'CCHN-0001', 'PK01'),
  ('NV002', 'CK02', 'Bác sĩ CKI',  'CCHN-0002', 'PK02'),
  ('NV011', 'CK04', 'Bác sĩ CKI',  'CCHN-0011', 'PK04')
ON CONFLICT DO NOTHING;
INSERT INTO yta (id_nv, cap_bac) VALUES
  ('NV003', 'Điều dưỡng đại học'), ('NV007', 'Cử nhân điều dưỡng')
ON CONFLICT DO NOTHING;
INSERT INTO duocsi (id_nv, bang_cap) VALUES
  ('NV004', 'Dược sĩ đại học'), ('NV008', 'Dược sĩ lâm sàng')
ON CONFLICT DO NOTHING;

-- 3. Bệnh nhân, người thân, BHYT, bệnh án
INSERT INTO benhnhan (id_bn, ho_ten, ngay_sinh, cccd, gioi_tinh, dia_chi, ngay_tao, trang_thai) VALUES
  ('BN001', 'Nguyễn Minh Anh',  '1998-03-14', '001206012345', 'Nữ',  'Cầu Giấy, Hà Nội',     today,     'Đang theo dõi'),
  ('BN002', 'Trần Quốc Bảo',    '1987-11-02', '001199007654', 'Nam', 'Hà Đông, Hà Nội',      today - 1, 'Ổn định'),
  ('BN003', 'Lê Thu Hà',        '2003-07-19', '001203004321', 'Nữ',  'Đống Đa, Hà Nội',      today - 1, 'Ổn định'),
  ('BN004', 'Phạm Đức Long',    '1990-12-08', '001190009876', 'Nam', 'Thanh Xuân, Hà Nội',   today - 2, 'Đang theo dõi'),
  ('BN005', 'Đỗ Ngọc Linh',     '2005-01-25', '001205001122', 'Nữ',  'Long Biên, Hà Nội',    today - 3, 'Ổn định'),
  ('BN006', 'Vũ Thành Nam',     '1985-09-30', '001185009911', 'Nam', 'Nam Từ Liêm, Hà Nội',  today - 4, 'Ổn định'),
  ('BN007', 'Hoàng Thu Trang',  '1995-05-18', '001205008899', 'Nữ',  'Cầu Giấy, Hà Nội',     today - 5, 'Ổn định')
ON CONFLICT DO NOTHING;

INSERT INTO benhnhan_sdt (id_bn, sdt) VALUES
  ('BN001','0901234567'), ('BN002','0912345678'), ('BN002','0988123456'), ('BN003','0934567890'),
  ('BN004','0977654321'), ('BN005','0966123456'), ('BN006','0944112233'), ('BN007','0988776655')
ON CONFLICT DO NOTHING;

INSERT INTO nguoithan (id_nt, id_bn, ho_ten, moi_quan_he, gioi_tinh) VALUES
  ('NT001', 'BN001', 'Nguyễn Văn Minh', 'Bố', 'Nam'),
  ('NT002', 'BN003', 'Lê Thị Hương',    'Mẹ', 'Nữ'),
  ('NT003', 'BN007', 'Hoàng Văn Hùng',  'Bố', 'Nam')
ON CONFLICT DO NOTHING;
INSERT INTO nguoithan_sdt (id_nt, sdt) VALUES
  ('NT001','0909988776'), ('NT002','0901234432'), ('NT003','0911223344')
ON CONFLICT DO NOTHING;

INSERT INTO baohiemyte (sobhyt, id_bn, muc_huong, nhom_doi_tuong, noi_dang_ky, ngay_bat_dau, ngay_ket_thuc) VALUES
  ('HN4010123456789', 'BN001',  80, 'Hộ gia đình',   'Trạm y tế phường Dịch Vọng', today - 180, today + 185),
  ('HN4010987654321', 'BN002',  95, 'Người lao động', 'Bệnh viện quận Hà Đông',     today - 365, today + 30),
  ('HN4010112233445', 'BN005', 100, 'Trẻ em',         'Bệnh viện Nhi Trung ương',   today - 90,  today + 275),
  ('HN4010556677889', 'BN007',  80, 'Người lao động', 'Bệnh viện E Hà Nội',         today - 120, today + 245)
ON CONFLICT DO NOTHING;

INSERT INTO benhan (id_benhan, id_bn, ngay_tao, tien_su_benh, trang_thai) VALUES
  ('BA001', 'BN001', today - 60,  'Viêm mũi dị ứng theo mùa',            'DANG_MO'),
  ('BA002', 'BN002', today - 120, 'Tăng huyết áp nhẹ',                   'DANG_MO'),
  ('BA003', 'BN004', today - 30,  'Chưa ghi nhận bất thường',            'DANG_MO'),
  ('BA004', 'BN006', today - 45,  'Tăng huyết áp nhẹ, rối loạn lipid',   'DANG_MO'),
  ('BA005', 'BN007', today - 10,  'Viêm họng tái phát nhiều lần',        'DANG_MO'),
  ('BA006', 'BN003', today,       'Chưa ghi nhận bất thường',            'DANG_MO'),
  ('BA007', 'BN005', today,       'Chưa ghi nhận bất thường',            'DANG_MO')
ON CONFLICT DO NOTHING;

-- 4. Danh mục chẩn đoán ICD (id_icd chính là mã ICD) và loại xét nghiệm
INSERT INTO chandoan (id_icd, ten_benh) VALUES
  ('J06.9', 'Nhiễm trùng đường hô hấp trên cấp, không đặc hiệu'),
  ('I10',   'Tăng huyết áp vô căn'),
  ('J30.4', 'Viêm mũi dị ứng, không đặc hiệu'),
  ('R50.9', 'Sốt, không đặc hiệu'),
  ('E11.9', 'Đái tháo đường type 2, không biến chứng')
ON CONFLICT DO NOTHING;

INSERT INTO loaixetnghiem (id_lxn, ten_lxn, chi_phi, trang_thai) VALUES
  ('XN001', 'Công thức máu',             180000, 'HOAT_DONG'),
  ('XN002', 'Đường huyết',                90000, 'HOAT_DONG'),
  ('XN003', 'Chức năng gan (AST/ALT)',   220000, 'HOAT_DONG'),
  ('XN004', 'Tổng phân tích nước tiểu',  120000, 'HOAT_DONG')
ON CONFLICT DO NOTHING;

-- 5. Thuốc và lô thuốc (so_luong_ton_kho = tổng lô còn hạn, đang hoạt động)
INSERT INTO thuoc (id_thuoc, ten_thuoc, nha_san_xuat, can_ke_don, so_luong_ton_kho, don_vi, gia_ban) VALUES
  ('T001', 'Paracetamol 500 mg', 'DHG Pharma',      FALSE, 420, 'viên', 800),
  ('T002', 'Amoxicillin 500 mg', 'Imexpharm',       TRUE,  180, 'viên', 2000),
  ('T003', 'Cetirizine 10 mg',   'Stella',          FALSE, 95,  'viên', 1500),
  ('T004', 'Amlodipine 5 mg',    'GSK',             TRUE,  70,  'viên', 3000),
  ('T005', 'Oresol',             'Pharmedic',       FALSE, 24,  'gói',  4000),
  ('T006', 'Vitamin C 500 mg',   'Dược Hậu Giang',  FALSE, 0,   'viên', 1200)
ON CONFLICT DO NOTHING;

INSERT INTO lothuoc (id_lo, id_thuoc, so_lo, han_su_dung, so_luong_nhap, so_luong_con, gia_nhap, gia_ban,
                     ngay_nhap, ngay_san_xuat, nha_cung_cap, trang_thai) VALUES
  ('LO001', 'T001', 'PAR-2608', today + 220, 500, 300, 450,  800,  today - 140, today - 145, 'Công ty Dược phẩm TW1',        'HOAT_DONG'),
  ('LO002', 'T001', 'PAR-2610', today + 360, 200, 120, 470,  800,  today - 50,  today - 60,  'DHG Pharma Chi nhánh Hà Nội', 'HOAT_DONG'),
  ('LO003', 'T002', 'AMX-2605', today + 140, 300, 180, 1200, 2000, today - 220, today - 225, 'Imexpharm Phân phối',          'HOAT_DONG'),
  ('LO004', 'T003', 'CET-2609', today + 45,  120, 95,  900,  1500, today - 310, today - 320, 'Stella Dược liệu',             'HOAT_DONG'),
  ('LO005', 'T004', 'AML-2604', today + 310, 100, 70,  1800, 3000, today - 50,  today - 55,  'Dược phẩm GSK Việt Nam',       'HOAT_DONG'),
  ('LO006', 'T005', 'ORS-2607', today + 28,  60,  24,  2500, 4000, today - 330, today - 335, 'Công ty CP Pharmedic',         'HOAT_DONG'),
  -- 2 lô đã hết hạn để thử chức năng loại bỏ lô hết hạn
  ('LO007', 'T001', 'PAR-2512', today - 15,  100, 15,  420,  800,  today - 370, today - 380, 'Công ty Dược phẩm TW1',        'HOAT_DONG'),
  ('LO008', 'T002', 'AMX-2510', today - 60,  50,  50,  1100, 2000, today - 420, today - 425, 'Imexpharm Phân phối',          'HOAT_DONG')
ON CONFLICT DO NOTHING;

-- 6. Phiếu khám + chẩn đoán chính
INSERT INTO phieukham (id_phieukham, id_benhan, id_pk, id_bacsy, sobhyt, thoi_gian_dky, thoi_gian_kham,
                       trieu_chung, phi_kham, trang_thai, ket_luan) VALUES
  ('PKM001', 'BA001', 'PK01', 'NV001', 'HN4010123456789', today + TIME '08:15',        today + TIME '08:15',
     'Hắt hơi, sổ mũi, ngạt mũi', 150000, 'DA_KHAM',
     'Viêm mũi dị ứng cấp, chỉ định dùng thuốc kháng histamin.'),
  ('PKM002', 'BA002', 'PK02', 'NV002', 'HN4010987654321', today + TIME '09:10',        today + TIME '09:10',
     'Đau đầu nhẹ, huyết áp 145/90', 180000, 'CHO_THANH_TOAN',
     'Tăng huyết áp vô căn độ 1, duy trì thuốc hạ áp hàng ngày.'),
  ('PKM003', 'BA006', 'PK01', 'NV001', NULL,              today + TIME '09:30',        today + TIME '09:30',
     'Ho khan, rát họng', 150000, 'CHO_KHAM', NULL),
  ('PKM004', 'BA003', 'PK02', 'NV002', NULL,              today + TIME '10:00',        today + TIME '10:00',
     'Mệt mỏi kéo dài, khát nước', 150000, 'CHO_XET_NGHIEM', NULL),
  ('PKM005', 'BA007', 'PK04', 'NV011', 'HN4010112233445', today + TIME '10:15',        today + TIME '10:15',
     'Sốt nhẹ 38 độ, biếng ăn', 120000, 'CHO_KHAM', NULL),
  ('PKM006', 'BA001', 'PK01', 'NV001', 'HN4010123456789', (today - 30) + TIME '14:00', (today - 30) + TIME '14:00',
     'Tái khám hô hấp, nghẹt mũi về đêm', 150000, 'HOAN_TAT',
     'Viêm mũi xoang dị ứng đã ổn định, duy trì vệ sinh mũi họng.'),
  ('PKM007', 'BA002', 'PK02', 'NV002', 'HN4010987654321', (today - 60) + TIME '08:45', (today - 60) + TIME '08:45',
     'Tái khám tăng huyết áp định kỳ', 180000, 'HOAN_TAT',
     'Huyết áp kiểm soát ổn định 125/80 mmHg, tim đều, không phù.'),
  ('PKM008', 'BA004', 'PK02', 'NV002', NULL,              today + TIME '10:30',        today + TIME '10:30',
     'Tức ngực trái nhẹ khi gắng sức, hồi hộp', 180000, 'DA_KHAM',
     'Tăng huyết áp độ 1, rối loạn lipid máu, chỉ định thuốc hạ áp và điều chỉnh chế độ ăn.'),
  ('PKM009', 'BA005', 'PK01', 'NV001', 'HN4010556677889', today + TIME '11:00',        today + TIME '11:00',
     'Sốt 38.3 độ, đau rát họng, nuốt vướng', 150000, 'DA_KHAM',
     'Viêm họng cấp, chỉ định giảm sốt, kháng sinh và bù nước điện giải.')
ON CONFLICT DO NOTHING;

INSERT INTO phieukham_chandoan (id_phieukham, id_icd, loai) VALUES
  ('PKM001', 'J30.4', 'CHINH'), ('PKM002', 'I10', 'CHINH'), ('PKM006', 'J06.9', 'CHINH'),
  ('PKM007', 'I10',   'CHINH'), ('PKM008', 'I10', 'CHINH'), ('PKM009', 'J06.9', 'CHINH')
ON CONFLICT DO NOTHING;

-- 7. Kết quả xét nghiệm
INSERT INTO ketquaxetnghiem (id_kqxn, id_phieukham, id_lxn, ket_qua, ket_luan, thoi_gian, trang_thai) VALUES
  ('KQ001', 'PKM001', 'XN001', 'RBC 4.5, WBC 7.2, PLT 240 (Bình thường)', 'Chưa ghi nhận bất thường về huyết học',
     today + TIME '08:45', 'DA_CO_KET_QUA'),
  ('KQ002', 'PKM004', 'XN002', NULL, NULL, today + TIME '10:05', 'CHO_KET_QUA'),
  ('KQ003', 'PKM007', 'XN003', 'AST 22, ALT 25 (Bình thường)', 'Chức năng gan bình thường, nhịp xoang đều',
     (today - 60) + TIME '09:15', 'DA_CO_KET_QUA'),
  ('KQ004', 'PKM008', 'XN001', 'RBC 4.6, WBC 6.5, PLT 215 (Bình thường)', 'Công thức máu bình thường, không thiếu máu',
     today + TIME '10:50', 'DA_CO_KET_QUA')
ON CONFLICT DO NOTHING;

-- 8. Đơn thuốc + chi tiết (id_duocsi = dược sĩ đã cấp phát)
INSERT INTO donthuoc (id_dt, id_phieukham, id_duocsi, ngay_ke, ghi_chu, trang_thai) VALUES
  ('DT001', 'PKM001', NULL,    today,      'Uống sau khi ăn, tái khám sau 7 ngày',     'CHO_CAP_PHAT'),
  ('DT002', 'PKM002', 'NV004', today,      'Uống cố định 8h sáng hàng ngày',           'DA_CAP_PHAT'),
  ('DT003', 'PKM006', 'NV004', today - 30, 'Uống sau ăn 30 phút, nghỉ ngơi hợp lý',    'DA_CAP_PHAT'),
  ('DT004', 'PKM008', NULL,    today,      'Uống cố định 8h sáng hàng ngày',           'CHO_CAP_PHAT'),
  ('DT005', 'PKM009', NULL,    today,      'Uống sau ăn, uống nhiều nước ấm',          'CHO_CAP_PHAT')
ON CONFLICT DO NOTHING;

INSERT INTO chitietdonthuoc (id_dt, id_thuoc, so_luong, lieu_dung, cach_dung, ghi_chu, don_gia) VALUES
  ('DT001', 'T003', 7,  '1 viên/lần',               'Uống sau ăn tối',        'Dùng trong 7 ngày',            1500),
  ('DT001', 'T001', 20, '1 viên khi sốt > 38.5°C',  'Uống với nước ấm',       'Cách nhau tối thiểu 4-6 tiếng', 800),
  ('DT002', 'T004', 6,  '1 viên/ngày',              'Uống buổi sáng sau ăn',  'Đo huyết áp trước khi uống',   3000),
  ('DT003', 'T001', 10, '1 viên khi sốt',           'Uống sau ăn',            'Cách 6 tiếng',                 800),
  ('DT003', 'T003', 5,  '1 viên/ngày',              'Uống buổi tối sau ăn',   'Dùng 5 ngày',                  1500),
  ('DT004', 'T004', 10, '1 viên/ngày',              'Uống sau ăn sáng',       'Đo huyết áp trước khi uống',   3000),
  ('DT005', 'T002', 20, '2 viên/lần, ngày 2 lần',   'Uống sau ăn sáng, tối',  'Dùng đủ liều 5 ngày',          2000),
  ('DT005', 'T001', 10, '1 viên khi sốt > 38.5°C',  'Uống với nước ấm',       'Cách 6 tiếng',                 800)
ON CONFLICT DO NOTHING;

-- 9. Phiếu thu viện phí (CHUA_THU = hóa đơn đang chờ thu)
INSERT INTO phieuthuvienphi (id_hoadon, id_phieukham, id_nv, so_tien, loai_thu, phuong_thuc_tt, thoi_gian_thu, trang_thai) VALUES
  ('HD001', 'PKM001', 'NV005', 356500,  'Khám + xét nghiệm + thuốc',          'CHUYEN_KHOAN', today + TIME '09:00', 'DA_THU'),
  ('HD002', 'PKM002', NULL,    198000,  'Khám + thuốc',                       NULL, NULL, 'CHUA_THU'),
  ('HD003', 'PKM004', NULL,    240000,  'Khám + xét nghiệm',                  NULL, NULL, 'CHUA_THU'),
  ('HD004', 'PKM006', 'NV005', 520000,  'Khám chuyên khoa + Xét nghiệm máu',  'CHUYEN_KHOAN', (today - 3)  + TIME '09:00', 'DA_THU'),
  ('HD005', 'PKM007', 'NV009', 310000,  'Khám Tim mạch + Điện tâm đồ',        'TIEN_MAT',     (today - 8)  + TIME '09:00', 'DA_THU'),
  ('HD006', 'PKM006', 'NV005', 150000,  'Khám Nội tổng hợp',                  'TIEN_MAT',     (today - 13) + TIME '09:00', 'DA_THU'),
  ('HD007', 'PKM007', 'NV009', 680000,  'Khám Nhi khoa + Thuốc điều trị',     'CHUYEN_KHOAN', (today - 18) + TIME '09:00', 'DA_THU'),
  ('HD008', 'PKM006', 'NV005', 450000,  'Khám tổng quát đầu tháng',           'CHUYEN_KHOAN', (today - 23) + TIME '09:00', 'DA_THU'),
  ('HD009', 'PKM007', 'NV009', 2450000, 'Tổng kết viện phí tháng 9',          'CHUYEN_KHOAN', TIMESTAMP '2026-09-18 09:00', 'DA_THU'),
  ('HD010', 'PKM006', 'NV005', 1850000, 'Tổng kết viện phí tháng 8',          'TIEN_MAT',     TIMESTAMP '2026-08-22 09:00', 'DA_THU'),
  ('HD011', 'PKM007', 'NV009', 3200000, 'Tổng kết viện phí tháng 7',          'CHUYEN_KHOAN', TIMESTAMP '2026-07-14 09:00', 'DA_THU'),
  ('HD012', 'PKM006', 'NV005', 2100000, 'Tổng kết viện phí tháng 6',          'CHUYEN_KHOAN', TIMESTAMP '2026-06-11 09:00', 'DA_THU'),
  ('HD013', 'PKM007', 'NV009', 1950000, 'Tổng kết viện phí tháng 5',          'TIEN_MAT',     TIMESTAMP '2026-05-09 09:00', 'DA_THU'),
  ('HD014', 'PKM006', 'NV005', 2800000, 'Tổng kết viện phí tháng 4',          'CHUYEN_KHOAN', TIMESTAMP '2026-04-16 09:00', 'DA_THU'),
  ('HD015', 'PKM007', 'NV009', 3100000, 'Tổng kết viện phí tháng 3',          'CHUYEN_KHOAN', TIMESTAMP '2026-03-24 09:00', 'DA_THU'),
  ('HD016', 'PKM006', 'NV005', 1600000, 'Tổng kết viện phí tháng 2',          'TIEN_MAT',     TIMESTAMP '2026-02-19 09:00', 'DA_THU'),
  ('HD017', 'PKM007', 'NV009', 2300000, 'Tổng kết viện phí tháng 1',          'CHUYEN_KHOAN', TIMESTAMP '2026-01-12 09:00', 'DA_THU'),
  ('HD018', 'PKM006', 'NV005', 165500,  'Khám + thuốc đợt trước',             'CHUYEN_KHOAN', (today - 30) + TIME '15:00', 'DA_THU'),
  ('HD019', 'PKM007', 'NV005', 400000,  'Khám Tim mạch + xét nghiệm gan',    'TIEN_MAT',     (today - 60) + TIME '10:00', 'DA_THU'),
  ('HD020', 'PKM008', NULL,    390000,  'Khám + xét nghiệm + thuốc',          NULL, NULL, 'CHUA_THU'),
  ('HD021', 'PKM009', NULL,    198000,  'Khám + thuốc điều trị',              NULL, NULL, 'CHUA_THU')
ON CONFLICT DO NOTHING;

END $$;
