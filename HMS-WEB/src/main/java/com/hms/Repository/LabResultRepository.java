package com.hms.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.hms.Entity.LabResult;

/** Kết quả xét nghiệm (bảng ketquaxetnghiem). */
@Repository
public class LabResultRepository {

    private static final String SELECT = """
            SELECT kq.id_kqxn, kq.id_phieukham, kq.id_lxn, COALESCE(kq.ket_qua, '') AS ket_qua, COALESCE(kq.ket_luan, '') AS ket_luan,
                   COALESCE(to_char(kq.thoi_gian, 'YYYY-MM-DD'), '') AS thoi_gian, kq.trang_thai,
                   lx.ten_lxn, lx.chi_phi, bn.id_bn, bn.ho_ten
            FROM ketquaxetnghiem kq
            JOIN loaixetnghiem lx ON lx.id_lxn = kq.id_lxn
            JOIN phieukham pk ON pk.id_phieukham = kq.id_phieukham
            JOIN benhan ba ON ba.id_benhan = pk.id_benhan
            JOIN benhnhan bn ON bn.id_bn = ba.id_bn
            """;

    private final JdbcTemplate jdbc;
    private final IdGenerator ids;

    public LabResultRepository(JdbcTemplate jdbc, IdGenerator ids) {
        this.jdbc = jdbc;
        this.ids = ids;
    }

    private static LabResult map(java.sql.ResultSet rs, int i) throws java.sql.SQLException {
        LabResult lr = new LabResult(rs.getString("id_kqxn"), rs.getString("id_phieukham"), rs.getString("id_lxn"),
                rs.getString("ket_qua"), rs.getString("ket_luan"), rs.getString("thoi_gian"),
                Labels.LAB_RESULT.label(rs.getString("trang_thai")));
        lr.setTenLxn(rs.getString("ten_lxn"));
        lr.setChiPhi(Db.longOrZero(rs, "chi_phi"));
        lr.setPatientId(rs.getString("id_bn"));
        lr.setPatientName(rs.getString("ho_ten"));
        return lr;
    }

    public List<LabResult> findAll() {
        return jdbc.query(SELECT + " ORDER BY length(kq.id_kqxn) DESC, kq.id_kqxn DESC", LabResultRepository::map);
    }

    public List<LabResult> findByExamIds(Collection<String> examIds) {
        if (examIds == null || examIds.isEmpty()) return new ArrayList<>();
        return jdbc.query(SELECT + " WHERE kq.id_phieukham IN (" + Db.placeholders(examIds.size()) + ") ORDER BY kq.id_kqxn",
                LabResultRepository::map, examIds.toArray());
    }

    public List<LabResult> findByExam(String examId) {
        return findByExamIds(List.of(examId));
    }

    public String nextId() {
        return ids.next("ketquaxetnghiem", "id_kqxn", "KQ", 3);
    }

    public void insertPending(String idKqxn, String examId, String labId, Timestamp orderedAt) {
        jdbc.update("INSERT INTO ketquaxetnghiem (id_kqxn, id_phieukham, id_lxn, thoi_gian, trang_thai) VALUES (?, ?, ?, ?, 'CHO_KET_QUA')",
                idKqxn, examId, labId, orderedAt);
    }

    /** Xóa các chỉ định CHƯA có kết quả của phiếu khám mà bác sĩ đã bỏ chọn. Kết quả đã hoàn thành không bao giờ bị xóa. */
    public void deletePendingNotIn(String examId, Collection<String> keepLabIds) {
        if (keepLabIds == null || keepLabIds.isEmpty()) {
            jdbc.update("DELETE FROM ketquaxetnghiem WHERE id_phieukham = ? AND trang_thai = 'CHO_KET_QUA'", examId);
            return;
        }
        List<Object> args = new ArrayList<>();
        args.add(examId);
        args.addAll(keepLabIds);
        jdbc.update("DELETE FROM ketquaxetnghiem WHERE id_phieukham = ? AND trang_thai = 'CHO_KET_QUA' AND id_lxn NOT IN ("
                + Db.placeholders(keepLabIds.size()) + ")", args.toArray());
    }

    public boolean complete(String idKqxn, String ketQua, String ketLuan, Timestamp at) {
        int n = jdbc.update("UPDATE ketquaxetnghiem SET ket_qua = ?, ket_luan = ?, thoi_gian = ?, trang_thai = 'DA_CO_KET_QUA' "
                + "WHERE id_kqxn = ?", ketQua, ketLuan, at, idKqxn);
        return n > 0;
    }

    /** Tổng chi phí xét nghiệm (không tính phiếu đã hủy) của một phiếu khám. */
    public long sumCost(String examId) {
        Long v = jdbc.queryForObject("SELECT COALESCE(SUM(lx.chi_phi), 0) FROM ketquaxetnghiem kq "
                + "JOIN loaixetnghiem lx ON lx.id_lxn = kq.id_lxn WHERE kq.id_phieukham = ? AND kq.trang_thai <> 'HUY'", Long.class, examId);
        return v == null ? 0L : v;
    }

    public String examIdOf(String idKqxn) {
        List<String> l = jdbc.queryForList("SELECT id_phieukham FROM ketquaxetnghiem WHERE id_kqxn = ?", String.class, idKqxn);
        return l.isEmpty() ? null : l.get(0);
    }

    public int countPending(String examId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM ketquaxetnghiem WHERE id_phieukham = ? AND trang_thai = 'CHO_KET_QUA'",
                Integer.class, examId);
        return n == null ? 0 : n;
    }
}
