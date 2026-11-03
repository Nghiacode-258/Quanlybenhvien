-- MediCore HMS: schema PostgreSQL cho Supabase
-- Chạy trong Supabase SQL Editor. Tất cả PK dùng TEXT để khớp ID chuỗi của dữ liệu demo.
-- SECURITY: RLS được bật và chỉ có policy cho role authenticated. Người dùng cần đăng nhập
-- Supabase Auth và chỉ nên được cấp quyền theo vai trò của họ trước khi sử dụng dữ liệu thật.

create table if not exists public.benh_nhan (
  id_bn text primary key,
  ho_ten text not null,
  cccd text,
  ngay_sinh date,
  gioi_tinh text,
  sdt text[] not null default '{}',
  dia_chi text,
  ngay_tao date default current_date,
  trang_thai text default 'Ổn định'
);
create table if not exists public.nguoi_nha (
  id_nt text primary key,
  id_bn text not null references public.benh_nhan(id_bn) on delete cascade,
  ho_ten text not null,
  moi_quan_he text,
  gioi_tinh text,
  sdt text[] not null default '{}'
);
create table if not exists public.bao_hiem_yte (
  id_bhyt text primary key,
  id_bn text not null unique references public.benh_nhan(id_bn) on delete cascade,
  so_bhyt text not null,
  muc_huong numeric(5,2) check (muc_huong between 0 and 100),
  ngay_bat_dau date,
  ngay_ket_thuc date,
  noi_dang_ky text,
  nhom_doi_tuong text
);
create table if not exists public.benh_an (
  id_benhan text primary key,
  id_bn text not null references public.benh_nhan(id_bn) on delete cascade,
  ngay_tao date default current_date,
  tien_su_benh text,
  trang_thai text default 'Mở'
);
create table if not exists public.chuyen_khoa (
  id_ck text primary key,
  ten_ck text not null unique
);
create table if not exists public.phong_kham (
  id_pk text primary key,
  ten_phong text not null,
  so_phong text,
  trang_thai text default 'Hoạt động',
  id_ck text references public.chuyen_khoa(id_ck) on delete set null
);
create table if not exists public.nhan_vien (
  id_nv text primary key,
  ho_ten text not null,
  gioi_tinh text,
  ngay_sinh date,
  sdt text[] not null default '{}',
  email text,
  vai_tro text not null,
  trang_thai text default 'Đang làm việc'
);
create table if not exists public.bac_sy (
  id_nv text primary key references public.nhan_vien(id_nv) on delete cascade,
  hoc_vi text,
  so_cchn text,
  id_ck text references public.chuyen_khoa(id_ck) on delete set null
);
create table if not exists public.y_ta (
  id_nv text primary key references public.nhan_vien(id_nv) on delete cascade,
  cap_bac text
);
create table if not exists public.duoc_sy (
  id_nv text primary key references public.nhan_vien(id_nv) on delete cascade,
  bang_cap text
);
create table if not exists public.loai_xet_nghiem (
  id_lxn text primary key,
  ten_lxn text not null,
  chi_phi numeric(14,2) not null default 0,
  trang_thai text default 'Hoạt động'
);
create table if not exists public.chan_doan (
  id_cd text primary key,
  ma_icd text not null,
  ten_benh text not null
);
create table if not exists public.phieu_kham (
  id_phieukham text primary key,
  id_bn text not null references public.benh_nhan(id_bn) on delete restrict,
  id_pk text references public.phong_kham(id_pk) on delete set null,
  id_nv text references public.nhan_vien(id_nv) on delete set null,
  id_benhan text references public.benh_an(id_benhan) on delete set null,
  id_cd text references public.chan_doan(id_cd) on delete set null,
  ngay_dky date default current_date,
  thoi_gian_kham time,
  trieu_chung text,
  ket_luan text,
  phi_kham numeric(14,2) not null default 0,
  trang_thai text default 'Chờ khám'
);
create table if not exists public.ket_qua_xet_nghiem (
  id_kqxn text primary key,
  id_phieukham text not null references public.phieu_kham(id_phieukham) on delete cascade,
  id_lxn text not null references public.loai_xet_nghiem(id_lxn) on delete restrict,
  ket_qua text,
  ket_luan text,
  thoi_gian date,
  trang_thai text default 'Chờ lấy mẫu'
);
create table if not exists public.thuoc (
  id_thuoc text primary key,
  ten_thuoc text not null,
  nha_san_xuat text,
  can_ke_don boolean not null default false,
  so_luong_ton_kho integer not null default 0 check (so_luong_ton_kho >= 0),
  don_vi text default 'đơn vị'
);
create table if not exists public.lo_thuoc (
  id_lo text primary key,
  id_thuoc text not null references public.thuoc(id_thuoc) on delete restrict,
  so_lo text,
  han_su_dung date,
  so_luong_nhap integer not null default 0 check (so_luong_nhap >= 0),
  so_luong_ton integer not null default 0 check (so_luong_ton >= 0),
  gia_nhap numeric(14,2) not null default 0,
  gia_ban numeric(14,2) not null default 0
);
create table if not exists public.don_thuoc (
  id_dt text primary key,
  id_phieukham text not null references public.phieu_kham(id_phieukham) on delete cascade,
  id_nv text references public.nhan_vien(id_nv) on delete set null,
  ngay_ke date default current_date,
  tong_tien numeric(14,2) not null default 0,
  ghi_chu text,
  trang_thai text default 'Chờ cấp phát'
);
create table if not exists public.chi_tiet_don_thuoc (
  id_ctdt text primary key,
  id_dt text not null references public.don_thuoc(id_dt) on delete cascade,
  id_thuoc text not null references public.thuoc(id_thuoc) on delete restrict,
  so_luong integer not null check (so_luong > 0),
  lieu_dung text,
  cach_dung text,
  ghi_chu text,
  don_gia numeric(14,2) not null default 0
);
create table if not exists public.phieu_thu_vien_phi (
  id_hoadon text primary key,
  id_phieukham text not null references public.phieu_kham(id_phieukham) on delete cascade,
  so_tien numeric(14,2) not null default 0,
  loai_thu text,
  phuong_thuc_tt text,
  thoi_gian_thu date,
  trang_thai text default 'Chưa thanh toán'
);

create index if not exists idx_nguoi_nha_id_bn on public.nguoi_nha(id_bn);
create index if not exists idx_benh_an_id_bn on public.benh_an(id_bn);
create index if not exists idx_phieu_kham_ngay on public.phieu_kham(ngay_dky);
create index if not exists idx_phieu_kham_trang_thai on public.phieu_kham(trang_thai);
create index if not exists idx_kqxn_phieu on public.ket_qua_xet_nghiem(id_phieukham);
create index if not exists idx_don_thuoc_phieu on public.don_thuoc(id_phieukham);
create index if not exists idx_hoa_don_phieu on public.phieu_thu_vien_phi(id_phieukham);

-- -----------------------------------------------------------------------------
-- RBAC: vai trò được gán bởi quản trị viên, không lấy từ lựa chọn phía trình duyệt.
-- Chạy toàn bộ file trong Supabase SQL Editor bằng tài khoản quản trị dự án.
-- -----------------------------------------------------------------------------
create table if not exists public.hms_user_roles (
  user_id uuid primary key references auth.users(id) on delete cascade,
  role text not null check (role in ('manager','doctor','nurse','pharmacist','cashier')),
  employee_id text unique references public.nhan_vien(id_nv) on delete set null,
  created_at timestamptz not null default now()
);

alter table public.hms_user_roles enable row level security;
revoke all on public.hms_user_roles from anon;
grant select, insert, update, delete on public.hms_user_roles to authenticated;

drop policy if exists "read own or manager role" on public.hms_user_roles;
drop policy if exists "manager manages roles" on public.hms_user_roles;

create or replace function public.hms_current_role()
returns text
language sql
stable
security definer
set search_path = public
as $$
  select r.role
  from public.hms_user_roles r
  where r.user_id = auth.uid()
  limit 1;
$$;
revoke all on function public.hms_current_role() from public, anon;
grant execute on function public.hms_current_role() to authenticated;

create policy "read own or manager role" on public.hms_user_roles
  for select to authenticated
  using (user_id = auth.uid() or public.hms_current_role() = 'manager');
create policy "manager manages roles" on public.hms_user_roles
  for all to authenticated
  using (public.hms_current_role() = 'manager')
  with check (public.hms_current_role() = 'manager');

-- View giới hạn cột cho quầy dược: không trả họ tên, CCCD, SĐT, địa chỉ,
-- triệu chứng hoặc nội dung bệnh án; chỉ dùng mã bệnh nhân để đối chiếu đơn.
create or replace view public.v_hms_don_thuoc_cap_phat as
select d.id_dt, d.id_phieukham, p.id_bn, d.id_nv, d.ngay_ke, d.tong_tien, d.ghi_chu, d.trang_thai
from public.don_thuoc d
join public.phieu_kham p on p.id_phieukham = d.id_phieukham
where public.hms_current_role() in ('manager','pharmacist');

-- View giới hạn thông tin cho thu ngân: chỉ có mã bệnh nhân và thông tin hóa đơn.
create or replace view public.v_hms_phieu_thu_vien_phi as
select h.id_hoadon, h.id_phieukham, p.id_bn, h.so_tien, h.loai_thu,
       h.phuong_thuc_tt, h.thoi_gian_thu, h.trang_thai
from public.phieu_thu_vien_phi h
join public.phieu_kham p on p.id_phieukham = h.id_phieukham
where public.hms_current_role() in ('manager','cashier');

revoke all on public.v_hms_don_thuoc_cap_phat from anon;
revoke all on public.v_hms_phieu_thu_vien_phi from anon;
grant select on public.v_hms_don_thuoc_cap_phat to authenticated;
grant select on public.v_hms_phieu_thu_vien_phi to authenticated;

-- Đồng bộ hóa đơn bằng hàm phía máy chủ. Bác sĩ/y tá không cần quyền SELECT bảng
-- viện phí để tính phí sau khi chỉ định xét nghiệm hoặc kê đơn.
create or replace function public.hms_sync_invoice_for_exam(p_exam_id text)
returns text
language plpgsql
security definer
set search_path = public
as $$
declare
  v_role text;
  v_exam public.phieu_kham%rowtype;
  v_lab_amount numeric(14,2) := 0;
  v_medicine_amount numeric(14,2) := 0;
  v_total numeric(14,2) := 0;
  v_invoice_id text;
  v_categories text := 'Phí khám';
begin
  if auth.uid() is null then
    raise exception 'Cần đăng nhập để thực hiện thao tác này.';
  end if;

  v_role := public.hms_current_role();
  if coalesce(v_role, '') not in ('manager','doctor','nurse','cashier') then
    raise exception 'Vai trò hiện tại không được phép đồng bộ chi phí khám.';
  end if;

  select * into v_exam from public.phieu_kham where id_phieukham = p_exam_id;
  if not found then
    raise exception 'Không tìm thấy phiếu khám %.', p_exam_id;
  end if;

  if v_role = 'doctor' and not exists (
    select 1 from public.hms_user_roles r
    where r.user_id = auth.uid() and r.employee_id = v_exam.id_nv
  ) then
    raise exception 'Bác sĩ chỉ được đồng bộ chi phí của phiếu được phân công.';
  end if;

  select coalesce(sum(x.chi_phi), 0) into v_lab_amount
  from (
    select distinct k.id_lxn
    from public.ket_qua_xet_nghiem k
    where k.id_phieukham = p_exam_id
  ) q
  join public.loai_xet_nghiem x on x.id_lxn = q.id_lxn;

  select coalesce(sum(d.tong_tien), 0) into v_medicine_amount
  from public.don_thuoc d
  where d.id_phieukham = p_exam_id;

  v_total := coalesce(v_exam.phi_kham, 0) + v_lab_amount + v_medicine_amount;
  if v_lab_amount > 0 then v_categories := v_categories || ' + Xét nghiệm'; end if;
  if v_medicine_amount > 0 then v_categories := v_categories || ' + Thuốc'; end if;

  select h.id_hoadon into v_invoice_id
  from public.phieu_thu_vien_phi h
  where h.id_phieukham = p_exam_id
    and h.trang_thai is distinct from 'Đã thanh toán'
  order by h.id_hoadon
  limit 1
  for update;

  if v_invoice_id is null then
    v_invoice_id := 'HD-' || substr(md5(random()::text || clock_timestamp()::text), 1, 14);
    insert into public.phieu_thu_vien_phi
      (id_hoadon, id_phieukham, so_tien, loai_thu, phuong_thuc_tt, thoi_gian_thu, trang_thai)
    values
      (v_invoice_id, p_exam_id, v_total, v_categories, '', null, 'Chưa thanh toán');
  else
    update public.phieu_thu_vien_phi
    set so_tien = v_total,
        loai_thu = v_categories,
        phuong_thuc_tt = case when so_tien is distinct from v_total then '' else phuong_thuc_tt end,
        thoi_gian_thu = case when so_tien is distinct from v_total then null else thoi_gian_thu end,
        trang_thai = case when so_tien is distinct from v_total then 'Chưa thanh toán' else trang_thai end
    where id_hoadon = v_invoice_id;
  end if;
  return v_invoice_id;
end;
$$;
revoke all on function public.hms_sync_invoice_for_exam(text) from public, anon;
grant execute on function public.hms_sync_invoice_for_exam(text) to authenticated;

-- Xóa policy cấp quyền toàn bộ kiểu cũ và tạo policy quản lý theo vai trò.
do $$
declare t text;
begin
  foreach t in array array[
    'benh_nhan','nguoi_nha','bao_hiem_yte','benh_an','chuyen_khoa','phong_kham',
    'nhan_vien','bac_sy','y_ta','duoc_sy','loai_xet_nghiem','chan_doan','phieu_kham',
    'ket_qua_xet_nghiem','thuoc','lo_thuoc','don_thuoc','chi_tiet_don_thuoc','phieu_thu_vien_phi'
  ] loop
    execute format('alter table public.%I enable row level security', t);
    execute format('drop policy if exists "authenticated full access" on public.%I', t);
    execute format('drop policy if exists "manager full access" on public.%I', t);
    execute format('drop policy if exists "nurse read" on public.%I', t);
    execute format('drop policy if exists "doctor read" on public.%I', t);
    execute format('drop policy if exists "pharmacist read" on public.%I', t);
    execute format('drop policy if exists "cashier read" on public.%I', t);
    execute format('drop policy if exists "nurse insert" on public.%I', t);
    execute format('drop policy if exists "nurse update" on public.%I', t);
    execute format('drop policy if exists "doctor insert" on public.%I', t);
    execute format('drop policy if exists "doctor update" on public.%I', t);
    execute format('drop policy if exists "doctor delete" on public.%I', t);
    execute format('drop policy if exists "pharmacist insert" on public.%I', t);
    execute format('drop policy if exists "pharmacist update" on public.%I', t);
    execute format('drop policy if exists "cashier update" on public.%I', t);
    execute format('revoke all on public.%I from anon', t);
    execute format('grant select, insert, update, delete on public.%I to authenticated', t);
    execute format('create policy "manager full access" on public.%I for all to authenticated using (public.hms_current_role() = ''manager'') with check (public.hms_current_role() = ''manager'')', t);
  end loop;
end $$;

-- Y tá/điều dưỡng: cận lâm sàng, xét nghiệm và hồ sơ nghiệp vụ.
do $$
declare t text;
begin
  foreach t in array array['benh_nhan','nguoi_nha','bao_hiem_yte','benh_an','chuyen_khoa','phong_kham','nhan_vien','bac_sy','loai_xet_nghiem','chan_doan','phieu_kham','ket_qua_xet_nghiem'] loop
    execute format('create policy "nurse read" on public.%I for select to authenticated using (public.hms_current_role() = ''nurse'')', t);
  end loop;
  foreach t in array array['ket_qua_xet_nghiem'] loop
    execute format('create policy "nurse insert" on public.%I for insert to authenticated with check (public.hms_current_role() = ''nurse'')', t);
    execute format('create policy "nurse update" on public.%I for update to authenticated using (public.hms_current_role() = ''nurse'') with check (public.hms_current_role() = ''nurse'')', t);
  end loop;
end $$;

-- Bác sĩ: đọc dữ liệu phục vụ khám, chỉnh sửa phiếu khám được phân công, kê đơn.
do $$
declare t text;
begin
  foreach t in array array['benh_nhan','nguoi_nha','bao_hiem_yte','benh_an','chuyen_khoa','phong_kham','nhan_vien','bac_sy','loai_xet_nghiem','chan_doan','phieu_kham','ket_qua_xet_nghiem','thuoc','lo_thuoc','don_thuoc','chi_tiet_don_thuoc'] loop
    execute format('create policy "doctor read" on public.%I for select to authenticated using (public.hms_current_role() = ''doctor'')', t);
  end loop;
  foreach t in array array['benh_an','don_thuoc','chi_tiet_don_thuoc'] loop
    execute format('create policy "doctor insert" on public.%I for insert to authenticated with check (public.hms_current_role() = ''doctor'')', t);
    execute format('create policy "doctor update" on public.%I for update to authenticated using (public.hms_current_role() = ''doctor'') with check (public.hms_current_role() = ''doctor'')', t);
  end loop;
  execute 'create policy "doctor update" on public.phieu_kham for update to authenticated using (public.hms_current_role() = ''doctor'') with check (public.hms_current_role() = ''doctor'')';
  execute 'create policy "doctor delete" on public.chi_tiet_don_thuoc for delete to authenticated using (public.hms_current_role() = ''doctor'')';
end $$;

-- Dược sĩ: chỉ danh mục thuốc, lô thuốc, đơn thuốc và chi tiết đơn thuốc.
do $$
declare t text;
begin
  foreach t in array array['thuoc','lo_thuoc','don_thuoc','chi_tiet_don_thuoc'] loop
    execute format('create policy "pharmacist read" on public.%I for select to authenticated using (public.hms_current_role() = ''pharmacist'')', t);
  end loop;
  foreach t in array array['thuoc','lo_thuoc'] loop
    execute format('create policy "pharmacist insert" on public.%I for insert to authenticated with check (public.hms_current_role() = ''pharmacist'')', t);
    execute format('create policy "pharmacist update" on public.%I for update to authenticated using (public.hms_current_role() = ''pharmacist'') with check (public.hms_current_role() = ''pharmacist'')', t);
  end loop;
  execute 'create policy "pharmacist update" on public.don_thuoc for update to authenticated using (public.hms_current_role() = ''pharmacist'') with check (public.hms_current_role() = ''pharmacist'')';
end $$;

-- Thu ngân: tiếp đón, đăng ký khám bệnh, quản lý hóa đơn và thu viện phí.
do $$
declare t text;
begin
  foreach t in array array['benh_nhan','nguoi_nha','bao_hiem_yte','benh_an','chuyen_khoa','phong_kham','nhan_vien','bac_sy','chan_doan','phieu_kham','phieu_thu_vien_phi'] loop
    execute format('create policy "cashier read" on public.%I for select to authenticated using (public.hms_current_role() = ''cashier'')', t);
  end loop;
  foreach t in array array['benh_nhan','nguoi_nha','bao_hiem_yte','phieu_kham','phieu_thu_vien_phi'] loop
    execute format('create policy "cashier insert" on public.%I for insert to authenticated with check (public.hms_current_role() = ''cashier'')', t);
    execute format('create policy "cashier update" on public.%I for update to authenticated using (public.hms_current_role() = ''cashier'') with check (public.hms_current_role() = ''cashier'')', t);
  end loop;
end $$;

-- Chỉ quản lý có quyền truy cập toàn bộ dữ liệu và dashboard/báo cáo.
-- Gán tài khoản thủ công sau khi tạo Auth user, ví dụ:
-- insert into public.hms_user_roles (user_id, role, employee_id)
-- values ('UUID-TRONG-SUPABASE-AUTH', 'manager', 'NV006');
