package com.hms.Repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.hms.Entity.Invoice;

/** Phiếu thu viện phí (bảng phieuthuvienphi). Mỗi hóa đơn "Chưa thanh toán" = phiếu ở trạng thái CHUA_THU. */
@Repository
public class InvoiceRepository {

    private static final String SELECT = """
            SELECT hd.id_hoadon, hd.id_phieukham, ba.id_bn, hd.so_tien, COALESCE(hd.loai_thu, '') AS loai_thu,
                   COALESCE(hd.phuong_thuc_tt, '') AS phuong_thuc_tt,
                   COALESCE(to_char(hd.thoi_gian_thu, 'YYYY-MM-DD'), '') AS thoi_gian_thu, hd.trang_thai,
                   to_char(pk.thoi_gian_dky, 'YYYY-MM-DD') AS ngay_lap,
                   bn.ho_ten,
                   COALESCE((SELECT string_agg(s.sdt, ', ' ORDER BY s.sdt) FROM benhnhan_sdt s WHERE s.id_bn = bn.id_bn), '—') AS sdt_bn
            FROM phieuthuvienphi hd
            JOIN phieukham pk ON pk.id_phieukham = hd.id_phieukham
            JOIN benhan ba ON ba.id_benhan = pk.id_benhan
            JOIN benhnhan bn ON bn.id_bn = ba.id_bn
            WHERE hd.trang_thai <> 'HUY'
            """;

    private final JdbcTemplate jdbc;
    private final IdGenerator ids;

    public InvoiceRepository(JdbcTemplate jdbc, IdGenerator ids) {
        this.jdbc = jdbc;
        this.ids = ids;
    }

    private static Invoice map(java.sql.ResultSet rs, int i) throws java.sql.SQLException {
        String ptt = rs.getString("phuong_thuc_tt");
        Invoice inv = new Invoice(rs.getString("id_hoadon"), rs.getString("id_phieukham"), Db.longOrZero(rs, "so_tien"),
                rs.getString("loai_thu"), ptt.isEmpty() ? "" : Labels.PAYMENT.label(ptt), rs.getString("thoi_gian_thu"),
                Labels.INVOICE.label(rs.getString("trang_thai")));
        inv.setIdBn(rs.getString("id_bn"));
        inv.setPatientName(rs.getString("ho_ten"));
        inv.setPatientPhone(rs.getString("sdt_bn"));
        inv.setNgayLap(rs.getString("ngay_lap"));
        return inv;
    }

    public List<Invoice> findAll() {
        return jdbc.query(SELECT + " ORDER BY length(hd.id_hoadon) DESC, hd.id_hoadon DESC", InvoiceRepository::map);
    }

    public Invoice findById(String idHoadon) {
        if (idHoadon == null) return null;
        List<Invoice> l = jdbc.query(SELECT + " AND hd.id_hoadon = ?", InvoiceRepository::map, idHoadon);
        return l.isEmpty() ? null : l.get(0);
    }

    public String nextId() {
        return ids.next("phieuthuvienphi", "id_hoadon", "HD", 3);
    }

    public void insertPending(String idHoadon, String examId, long amount, String description) {
        jdbc.update("INSERT INTO phieuthuvienphi (id_hoadon, id_phieukham, id_nv, so_tien, loai_thu, phuong_thuc_tt, thoi_gian_thu, trang_thai) "
                + "VALUES (?, ?, NULL, ?, ?, NULL, NULL, 'CHUA_THU')", idHoadon, examId, amount, description);
    }

    /** Id hóa đơn đang chờ thu mới nhất của phiếu khám, null nếu không có. */
    public String findPendingIdByExam(String examId) {
        List<String> l = jdbc.queryForList("SELECT id_hoadon FROM phieuthuvienphi WHERE id_phieukham = ? AND trang_thai = 'CHUA_THU' "
                + "ORDER BY length(id_hoadon) DESC, id_hoadon DESC LIMIT 1", String.class, examId);
        return l.isEmpty() ? null : l.get(0);
    }

    public void updatePending(String idHoadon, long amount, String description) {
        jdbc.update("UPDATE phieuthuvienphi SET so_tien = ?, loai_thu = ? WHERE id_hoadon = ? AND trang_thai = 'CHUA_THU'",
                amount, description, idHoadon);
    }

    public int countByExam(String examId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM phieuthuvienphi WHERE id_phieukham = ? AND trang_thai <> 'HUY'",
                Integer.class, examId);
        return n == null ? 0 : n;
    }

    public int countPendingByExam(String examId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM phieuthuvienphi WHERE id_phieukham = ? AND trang_thai = 'CHUA_THU'",
                Integer.class, examId);
        return n == null ? 0 : n;
    }

    public long sumPaidByExam(String examId) {
        Long v = jdbc.queryForObject("SELECT COALESCE(SUM(so_tien), 0) FROM phieuthuvienphi WHERE id_phieukham = ? AND trang_thai = 'DA_THU'",
                Long.class, examId);
        return v == null ? 0L : v;
    }

    /** Thu tiền: chỉ chuyển được hóa đơn đang ở trạng thái CHUA_THU. Trả về false nếu không có hóa đơn nào được cập nhật. */
    public boolean markPaid(String idHoadon, String paymentCode, Timestamp paidAt, String cashierId) {
        return jdbc.update("UPDATE phieuthuvienphi SET trang_thai = 'DA_THU', phuong_thuc_tt = ?, thoi_gian_thu = ?, id_nv = ? "
                + "WHERE id_hoadon = ? AND trang_thai = 'CHUA_THU'", paymentCode, paidAt, cashierId, idHoadon) > 0;
    }

    public long totalPaid() {
        Long v = jdbc.queryForObject("SELECT COALESCE(SUM(so_tien), 0) FROM phieuthuvienphi WHERE trang_thai = 'DA_THU'", Long.class);
        return v == null ? 0L : v;
    }

    public long totalPending() {
        Long v = jdbc.queryForObject("SELECT COALESCE(SUM(so_tien), 0) FROM phieuthuvienphi WHERE trang_thai = 'CHUA_THU'", Long.class);
        return v == null ? 0L : v;
    }
}
