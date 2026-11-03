-- Schema code 2 không có RLS. Bảng ở schema public sẽ lộ qua Supabase Data API (anon key).
-- Bật RLS và KHÔNG tạo policy: app Spring kết nối bằng user postgres nên không bị ảnh hưởng,
-- còn truy cập qua anon/authenticated key bị chặn hoàn toàn.
do $$
declare t text;
begin
  for t in select tablename from pg_tables where schemaname = 'public' loop
    execute format('alter table public.%I enable row level security', t);
  end loop;
end $$;
-- View kế thừa quyền của chủ view, nên thu hồi quyền truy cập API:
revoke all on public.v_donthuoc_tongtien from anon, authenticated;
revoke all on public.v_phieukham_tongquan from anon, authenticated;
