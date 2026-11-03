package com.hms.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.hms.Entity.Medicine;
import com.hms.Entity.MedicineLot;

/** Danh mục thuốc (thuoc) và lô thuốc (lothuoc). */
@Repository
public class PharmacyRepository {

    /** Một dòng lô còn dùng được, đã khóa FOR UPDATE để trừ kho FEFO. */
    public static final class LotStock {
        public final String idLo;
        public final int soLuongCon;
        public LotStock(String idLo, int soLuongCon) {
            this.idLo = idLo;
            this.soLuongCon = soLuongCon;
        }
    }

    private final JdbcTemplate jdbc;
    private final IdGenerator ids;

    public PharmacyRepository(JdbcTemplate jdbc, IdGenerator ids) {
        this.jdbc = jdbc;
        this.ids = ids;
    }

    // ================================================================ THUỐC

    private static Medicine mapMedicine(java.sql.ResultSet rs, int i) throws java.sql.SQLException {
        return new Medicine(rs.getString("id_thuoc"), rs.getString("ten_thuoc"), Db.nz(rs.getString("nha_san_xuat")),
                rs.getBoolean("can_ke_don"), rs.getInt("so_luong_ton_kho"), rs.getString("don_vi"),
                Db.longOrZero(rs, "gia_ban"));
    }

    private static final String SELECT_MED =
            "SELECT id_thuoc, ten_thuoc, nha_san_xuat, can_ke_don, so_luong_ton_kho, don_vi, gia_ban FROM thuoc";

    public List<Medicine> findAllMedicines() {
        return jdbc.query(SELECT_MED + " ORDER BY id_thuoc", PharmacyRepository::mapMedicine);
    }

    public Medicine findMedicineById(String idThuoc) {
        if (idThuoc == null) return null;
        List<Medicine> l = jdbc.query(SELECT_MED + " WHERE id_thuoc = ?", PharmacyRepository::mapMedicine, idThuoc);
        return l.isEmpty() ? null : l.get(0);
    }

    public String nextMedicineId() {
        return ids.next("thuoc", "id_thuoc", "T", 3);
    }

    public void insertMedicine(Medicine m) {
        jdbc.update("INSERT INTO thuoc (id_thuoc, ten_thuoc, nha_san_xuat, can_ke_don, so_luong_ton_kho, don_vi, gia_ban) "
                        + "VALUES (?, ?, ?, ?, 0, ?, ?)",
                m.getIdThuoc(), m.getTenThuoc(), Db.trimToNull(m.getNhaSanXuat()), Boolean.TRUE.equals(m.getCanKeDon()),
                m.getDonVi(), m.getGiaBan() == null ? 0L : m.getGiaBan());
    }

    public void deleteMedicine(String idThuoc) {
        jdbc.update("DELETE FROM thuoc WHERE id_thuoc = ?", idThuoc);
    }

    public int countLots(String idThuoc) {
        return count("SELECT COUNT(*) FROM lothuoc WHERE id_thuoc = ?", idThuoc);
    }

    public int countPrescriptionItems(String idThuoc) {
        return count("SELECT COUNT(*) FROM chitietdonthuoc WHERE id_thuoc = ?", idThuoc);
    }

    /** Đồng bộ tồn kho của thuốc = tổng số còn lại của các lô còn hạn và đang hoạt động. */
    public void recalculateStock(String idThuoc, LocalDate today) {
        jdbc.update("UPDATE thuoc SET so_luong_ton_kho = COALESCE((SELECT SUM(so_luong_con) FROM lothuoc "
                        + "WHERE id_thuoc = ? AND trang_thai = 'HOAT_DONG' AND han_su_dung >= ? AND so_luong_con > 0), 0) "
                        + "WHERE id_thuoc = ?", idThuoc, Db.sqlDate(today), idThuoc);
    }

    /** Đơn giá bán hiện hành: giá lô còn hạn gần hết hạn nhất (FEFO), nếu không có thì giá bán của thuốc. */
    public long currentUnitPrice(String idThuoc, LocalDate today) {
        List<Long> l = jdbc.queryForList("SELECT CAST(gia_ban AS BIGINT) FROM lothuoc WHERE id_thuoc = ? AND trang_thai = 'HOAT_DONG' "
                + "AND han_su_dung >= ? AND so_luong_con > 0 ORDER BY han_su_dung, id_lo LIMIT 1", Long.class, idThuoc, Db.sqlDate(today));
        if (!l.isEmpty() && l.get(0) != null) return l.get(0);
        Medicine m = findMedicineById(idThuoc);
        return m == null || m.getGiaBan() == null ? 0L : m.getGiaBan();
    }

    // ================================================================ LÔ THUỐC

    private static final String SELECT_LOT = """
            SELECT l.id_lo, l.id_thuoc, l.so_lo, to_char(l.han_su_dung, 'YYYY-MM-DD') AS han_su_dung,
                   l.so_luong_nhap, l.so_luong_con, l.gia_nhap, l.gia_ban,
                   COALESCE(to_char(l.ngay_nhap, 'YYYY-MM-DD'), '') AS ngay_nhap,
                   COALESCE(to_char(l.ngay_san_xuat, 'YYYY-MM-DD'), '') AS ngay_san_xuat,
                   COALESCE(l.nha_cung_cap, '') AS nha_cung_cap, l.trang_thai, t.ten_thuoc, t.don_vi
            FROM lothuoc l JOIN thuoc t ON t.id_thuoc = l.id_thuoc
            """;

    private static MedicineLot mapLot(java.sql.ResultSet rs, int i) throws java.sql.SQLException {
        MedicineLot lot = new MedicineLot(rs.getString("id_lo"), rs.getString("id_thuoc"), rs.getString("so_lo"),
                rs.getString("han_su_dung"), rs.getInt("so_luong_nhap"), rs.getInt("so_luong_con"),
                Db.longOrZero(rs, "gia_nhap"), Db.longOrZero(rs, "gia_ban"), rs.getString("ngay_nhap"),
                rs.getString("ngay_san_xuat"), rs.getString("nha_cung_cap"), Labels.LOT_STATUS.label(rs.getString("trang_thai")));
        lot.setTenThuoc(rs.getString("ten_thuoc"));
        lot.setDonVi(rs.getString("don_vi"));
        return lot;
    }

    public List<MedicineLot> findAllLots() {
        return jdbc.query(SELECT_LOT + " ORDER BY l.han_su_dung, l.id_lo", PharmacyRepository::mapLot);
    }

    public MedicineLot findLotById(String idLo) {
        if (idLo == null) return null;
        List<MedicineLot> l = jdbc.query(SELECT_LOT + " WHERE l.id_lo = ?", PharmacyRepository::mapLot, idLo);
        return l.isEmpty() ? null : l.get(0);
    }

    public boolean lotNumberExists(String soLo) {
        return count("SELECT COUNT(*) FROM lothuoc WHERE UPPER(so_lo) = UPPER(?)", soLo) > 0;
    }

    public String nextLotId() {
        return ids.next("lothuoc", "id_lo", "LO", 3);
    }

    public void insertLot(MedicineLot lot) {
        jdbc.update("INSERT INTO lothuoc (id_lo, id_thuoc, so_lo, han_su_dung, so_luong_nhap, so_luong_con, gia_nhap, gia_ban, "
                        + "ngay_nhap, ngay_san_xuat, nha_cung_cap, trang_thai) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'HOAT_DONG')",
                lot.getIdLo(), lot.getIdThuoc(), lot.getSoLo(), Db.sqlDate(Db.parseDate(lot.getHanSuDung(), "Hạn sử dụng")),
                lot.getSoLuongNhap(), lot.getSoLuongTon(), lot.getGiaNhap(), lot.getGiaBan(),
                Db.sqlDate(Db.parseDate(lot.getNgayNhap(), "Ngày nhập")),
                Db.sqlDate(Db.parseDate(lot.getNgaySanXuat(), "Ngày sản xuất")), Db.trimToNull(lot.getNhaCungCap()));
    }

    public void deleteLot(String idLo) {
        jdbc.update("DELETE FROM lothuoc WHERE id_lo = ?", idLo);
    }

    /** Lô đã từng xuất kho nhưng hết hạn: giữ lịch sử, đánh dấu loại bỏ và đưa tồn về 0. */
    public void markLotRemoved(String idLo) {
        jdbc.update("UPDATE lothuoc SET trang_thai = 'DA_LOAI_BO', so_luong_con = 0 WHERE id_lo = ?", idLo);
    }

    /** Các lô còn hạn/đang hoạt động của thuốc theo FEFO, khóa dòng để trừ kho an toàn khi nhiều người cấp phát. */
    public List<LotStock> lockUsableLots(String idThuoc, LocalDate today) {
        return jdbc.query("SELECT id_lo, so_luong_con FROM lothuoc WHERE id_thuoc = ? AND trang_thai = 'HOAT_DONG' "
                        + "AND han_su_dung >= ? AND so_luong_con > 0 ORDER BY han_su_dung, id_lo FOR UPDATE",
                (rs, i) -> new LotStock(rs.getString("id_lo"), rs.getInt("so_luong_con")), idThuoc, Db.sqlDate(today));
    }

    public void setLotRemaining(String idLo, int remaining) {
        jdbc.update("UPDATE lothuoc SET so_luong_con = ? WHERE id_lo = ?", remaining, idLo);
    }

    private int count(String sql, Object arg) {
        Integer n = jdbc.queryForObject(sql, Integer.class, arg);
        return n == null ? 0 : n;
    }
}
