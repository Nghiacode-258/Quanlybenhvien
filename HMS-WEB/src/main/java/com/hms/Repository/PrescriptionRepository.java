package com.hms.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.hms.Entity.Prescription;
import com.hms.Entity.PrescriptionDetail;

/** Đơn thuốc (donthuoc) và chi tiết đơn thuốc (chitietdonthuoc). */
@Repository
public class PrescriptionRepository {

    private static final String SELECT = """
            SELECT dt.id_dt, dt.id_phieukham, pk.id_bacsy, COALESCE(to_char(dt.ngay_ke, 'YYYY-MM-DD'), '') AS ngay_ke,
                   dt.ghi_chu, dt.trang_thai,
                   (SELECT COALESCE(SUM(ct.so_luong * ct.don_gia), 0) FROM chitietdonthuoc ct WHERE ct.id_dt = dt.id_dt) AS tong_tien,
                   bn.id_bn, bn.ho_ten AS ten_bn, nv.ho_ten AS ten_bs
            FROM donthuoc dt
            JOIN phieukham pk ON pk.id_phieukham = dt.id_phieukham
            JOIN benhan ba ON ba.id_benhan = pk.id_benhan
            JOIN benhnhan bn ON bn.id_bn = ba.id_bn
            LEFT JOIN nhanvien nv ON nv.id_nv = pk.id_bacsy
            """;

    private final JdbcTemplate jdbc;
    private final IdGenerator ids;

    public PrescriptionRepository(JdbcTemplate jdbc, IdGenerator ids) {
        this.jdbc = jdbc;
        this.ids = ids;
    }

    private static Prescription map(java.sql.ResultSet rs, int i) throws java.sql.SQLException {
        Prescription p = new Prescription(rs.getString("id_dt"), rs.getString("id_phieukham"), rs.getString("id_bacsy"),
                rs.getString("ngay_ke"), Db.longOrZero(rs, "tong_tien"), rs.getString("ghi_chu"),
                Labels.PRESCRIPTION.label(rs.getString("trang_thai")));
        p.setPatientId(rs.getString("id_bn"));
        p.setPatientName(rs.getString("ten_bn"));
        p.setDoctorName(rs.getString("ten_bs"));
        p.setItems(new ArrayList<>());
        return p;
    }
    

    /** Nạp danh sách đơn + chi tiết bằng 2 truy vấn. */
    private List<Prescription> load(String where, Object... args) {
        List<Prescription> list = jdbc.query(SELECT + where + " ORDER BY length(dt.id_dt) DESC, dt.id_dt DESC",
                PrescriptionRepository::map, args);
        attachItems(list);
        return list;
    }

    private void attachItems(List<Prescription> list) {
        if (list.isEmpty()) return;
        Map<String, Prescription> byId = new LinkedHashMap<>();
        for (Prescription p : list) byId.put(p.getIdDt(), p);
        String in = Db.placeholders(byId.size());
        jdbc.query("SELECT ct.id_dt, ct.id_thuoc, ct.so_luong, COALESCE(ct.lieu_dung, '') AS lieu_dung, "
                + "COALESCE(ct.cach_dung, '') AS cach_dung, COALESCE(ct.ghi_chu, '') AS ghi_chu, ct.don_gia, t.ten_thuoc, t.don_vi "
                + "FROM chitietdonthuoc ct JOIN thuoc t ON t.id_thuoc = ct.id_thuoc "
                + "WHERE ct.id_dt IN (" + in + ") ORDER BY ct.id_dt, ct.id_thuoc", (rs) -> {
            Prescription p = byId.get(rs.getString("id_dt"));
            if (p == null) return;
            String idDt = rs.getString("id_dt");
            String idThuoc = rs.getString("id_thuoc");
            // Bảng chitietdonthuoc không có cột mã riêng; khóa chính là (id_dt, id_thuoc)
            PrescriptionDetail d = new PrescriptionDetail(idDt + "-" + idThuoc, idDt, idThuoc, rs.getInt("so_luong"),
                    rs.getString("lieu_dung"), rs.getString("cach_dung"), rs.getString("ghi_chu"), Db.longOrZero(rs, "don_gia"));
            d.setTenThuoc(rs.getString("ten_thuoc"));
            d.setDonVi(rs.getString("don_vi"));
            p.getItems().add(d);
        }, byId.keySet().toArray());
    }

    public List<Prescription> findAll() {
        return load("");
    }

    public Prescription findById(String idDt) {
        if (idDt == null) return null;
        List<Prescription> l = load(" WHERE dt.id_dt = ?", idDt);
        return l.isEmpty() ? null : l.get(0);
    }

    public Prescription findByExamId(String examId) {
        if (examId == null) return null;
        List<Prescription> l = load(" WHERE dt.id_phieukham = ?", examId);
        return l.isEmpty() ? null : l.get(0);
    }

    /** Đơn thuốc theo danh sách phiếu khám: key = mã phiếu khám. */
    public Map<String, Prescription> findByExamIds(Collection<String> examIds) {
        Map<String, Prescription> result = new LinkedHashMap<>();
        if (examIds == null || examIds.isEmpty()) return result;
        for (Prescription p : load(" WHERE dt.id_phieukham IN (" + Db.placeholders(examIds.size()) + ")", examIds.toArray())) {
            result.putIfAbsent(p.getIdPhieukham(), p);
        }
        return result;
    }

    public String nextId() {
        return ids.next("donthuoc", "id_dt", "DT", 3);
    }

    public void insert(String idDt, String examId, LocalDate ngayKe, String ghiChu) {
        jdbc.update("INSERT INTO donthuoc (id_dt, id_phieukham, id_duocsi, ngay_ke, ghi_chu, trang_thai) "
                + "VALUES (?, ?, NULL, ?, ?, 'CHO_CAP_PHAT')", idDt, examId, Db.sqlDate(ngayKe), Db.trimToNull(ghiChu));
    }

    public void updateHeader(String idDt, LocalDate ngayKe, String ghiChu) {
        jdbc.update("UPDATE donthuoc SET ngay_ke = ?, ghi_chu = ? WHERE id_dt = ?", Db.sqlDate(ngayKe), Db.trimToNull(ghiChu), idDt);
    }

    public void deleteItems(String idDt) {
        jdbc.update("DELETE FROM chitietdonthuoc WHERE id_dt = ?", idDt);
    }

    public void insertItem(String idDt, String idThuoc, int soLuong, String lieuDung, String cachDung, String ghiChu, long donGia) {
        jdbc.update("INSERT INTO chitietdonthuoc (id_dt, id_thuoc, so_luong, lieu_dung, cach_dung, ghi_chu, don_gia) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                idDt, idThuoc, soLuong, Db.trimToNull(lieuDung), Db.trimToNull(cachDung), Db.trimToNull(ghiChu), donGia);
    }

    /** Chuyển đơn sang "Đã cấp phát" và ghi dược sĩ cấp phát (có thể null). Trả về false nếu đơn không còn ở trạng thái chờ. */
    public boolean markDispensed(String idDt, String pharmacistId) {
        return jdbc.update("UPDATE donthuoc SET trang_thai = 'DA_CAP_PHAT', id_duocsi = ? WHERE id_dt = ? AND trang_thai = 'CHO_CAP_PHAT'",
                pharmacistId, idDt) > 0;
    }

    /** Tổng tiền thuốc (không tính đơn đã hủy) của một phiếu khám. */
    public long sumAmount(String examId) {
        Long v = jdbc.queryForObject("SELECT COALESCE(SUM(ct.so_luong * ct.don_gia), 0) FROM donthuoc dt "
                + "JOIN chitietdonthuoc ct ON ct.id_dt = dt.id_dt WHERE dt.id_phieukham = ? AND dt.trang_thai <> 'HUY'", Long.class, examId);
        return v == null ? 0L : v;
    }
}
