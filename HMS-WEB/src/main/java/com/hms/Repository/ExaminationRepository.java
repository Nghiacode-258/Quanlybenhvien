package com.hms.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.hms.Entity.Examination;
import com.hms.Entity.LabResult;
import com.hms.Entity.Prescription;

/** Phiếu khám (bảng phieukham, phieukham_chandoan) kèm kết quả xét nghiệm và đơn thuốc. */
@Repository
public class ExaminationRepository {

    private final JdbcTemplate jdbc;
    private final IdGenerator ids;
    private final LabResultRepository labs;
    private final PrescriptionRepository prescriptions;

    private static final String SELECT = """
            SELECT pk.id_phieukham, ba.id_bn, pk.id_pk, pk.id_bacsy, pk.id_benhan,
                   cd.id_icd, cd.ten_benh,
                   to_char(pk.thoi_gian_dky, 'YYYY-MM-DD') AS ngay_dky,
                   COALESCE(to_char(pk.thoi_gian_kham, 'HH24:MI'), '') AS gio_kham,
                   COALESCE(pk.trieu_chung, '') AS trieu_chung, pk.phi_kham, pk.trang_thai, COALESCE(pk.ket_luan, '') AS ket_luan,
                   bn.ho_ten AS ten_bn,
                   COALESCE((SELECT string_agg(s.sdt, ', ' ORDER BY s.sdt) FROM benhnhan_sdt s WHERE s.id_bn = bn.id_bn), '—') AS sdt_bn,
                   p.ten_phong, p.so_phong, nv.ho_ten AS ten_bs
            FROM phieukham pk
            JOIN benhan ba ON ba.id_benhan = pk.id_benhan
            JOIN benhnhan bn ON bn.id_bn = ba.id_bn
            JOIN phongkham p ON p.id_pk = pk.id_pk
            LEFT JOIN nhanvien nv ON nv.id_nv = pk.id_bacsy
            LEFT JOIN LATERAL (
                SELECT c.id_icd, c.ten_benh
                FROM phieukham_chandoan pc JOIN chandoan c ON c.id_icd = pc.id_icd
                WHERE pc.id_phieukham = pk.id_phieukham
                ORDER BY (pc.loai = 'CHINH') DESC, pc.id_icd
                LIMIT 1
            ) cd ON TRUE
            """;

    public ExaminationRepository(JdbcTemplate jdbc, IdGenerator ids, LabResultRepository labs,
                                 PrescriptionRepository prescriptions) {
        this.jdbc = jdbc;
        this.ids = ids;
        this.labs = labs;
        this.prescriptions = prescriptions;
    }

    private static Examination map(java.sql.ResultSet rs, int i) throws java.sql.SQLException {
        Examination e = new Examination();
        e.setIdPhieukham(rs.getString("id_phieukham"));
        e.setIdBn(rs.getString("id_bn"));
        e.setIdPk(rs.getString("id_pk"));
        e.setIdNv(rs.getString("id_bacsy"));
        e.setIdBenhan(rs.getString("id_benhan"));
        e.setIdCd(rs.getString("id_icd"));
        e.setNgayDky(rs.getString("ngay_dky"));
        e.setThoiGianKham(rs.getString("gio_kham"));
        e.setTrieuChung(rs.getString("trieu_chung"));
        e.setPhiKham(Db.longOrZero(rs, "phi_kham"));
        e.setTrangThai(Labels.EXAM.label(rs.getString("trang_thai")));
        e.setKetLuan(rs.getString("ket_luan"));
        e.setPatientName(rs.getString("ten_bn"));
        e.setPatientPhone(rs.getString("sdt_bn"));
        e.setRoomName(rs.getString("ten_phong") + " (" + rs.getString("so_phong") + ")");
        e.setDoctorName(rs.getString("ten_bs"));
        String icd = rs.getString("id_icd");
        e.setDiagnosisName(icd != null ? rs.getString("ten_benh") : "Chưa có chẩn đoán");
        e.setIcdCode(icd != null ? icd : "");
        e.setLabResults(new ArrayList<>());
        return e;
    }

    /** Nạp phiếu khám kèm xét nghiệm + đơn thuốc bằng đúng 4 truy vấn (không N+1). */
    private List<Examination> load(String where, Object... args) {
        List<Examination> list = jdbc.query(SELECT + where + " ORDER BY length(pk.id_phieukham) DESC, pk.id_phieukham DESC",
                ExaminationRepository::map, args);
        if (list.isEmpty()) return list;

        Map<String, Examination> byId = new LinkedHashMap<>();
        for (Examination e : list) byId.put(e.getIdPhieukham(), e);

        for (LabResult lr : labs.findByExamIds(byId.keySet())) {
            Examination e = byId.get(lr.getIdPhieukham());
            if (e != null) e.getLabResults().add(lr);
        }
        for (Map.Entry<String, Prescription> en : prescriptions.findByExamIds(byId.keySet()).entrySet()) {
            Examination e = byId.get(en.getKey());
            if (e != null) e.setPrescription(en.getValue());
        }
        return list;
    }

    public List<Examination> findAll() {
        return load("");
    }

    public Examination findById(String idPhieukham) {
        if (idPhieukham == null) return null;
        List<Examination> l = load(" WHERE pk.id_phieukham = ?", idPhieukham);
        return l.isEmpty() ? null : l.get(0);
    }

    /** Tập mã bệnh nhân có phiếu khám thuộc bác sĩ hoặc phòng khám chỉ định (dùng lọc hồ sơ theo bác sĩ). */
    public Set<String> findPatientIdsForDoctor(String doctorId, String roomId) {
        List<String> l = jdbc.queryForList("SELECT DISTINCT ba.id_bn FROM phieukham pk JOIN benhan ba ON ba.id_benhan = pk.id_benhan "
                + "WHERE pk.id_bacsy = ? OR pk.id_pk = ?", String.class, doctorId, roomId);
        return new TreeSet<>(l);
    }

    public LocalDate registrationDate(String idPhieukham) {
        List<java.sql.Date> l = jdbc.queryForList("SELECT CAST(thoi_gian_dky AS date) FROM phieukham WHERE id_phieukham = ?",
                java.sql.Date.class, idPhieukham);
        return l.isEmpty() || l.get(0) == null ? null : l.get(0).toLocalDate();
    }

    public String currentStatusCode(String idPhieukham) {
        List<String> l = jdbc.queryForList("SELECT trang_thai FROM phieukham WHERE id_phieukham = ?", String.class, idPhieukham);
        return l.isEmpty() ? null : l.get(0);
    }

    public long consultationFee(String idPhieukham) {
        Long v = jdbc.queryForObject("SELECT COALESCE(phi_kham, 0) FROM phieukham WHERE id_phieukham = ?", Long.class, idPhieukham);
        return v == null ? 0L : v;
    }

    public String nextId() {
        return ids.next("phieukham", "id_phieukham", "PKM", 3);
    }

    public void insert(String idPhieukham, String idBenhan, String idPk, String idBacsy, String soBhyt,
                       LocalDateTime registeredAt, LocalDateTime examAt, String trieuChung, long phiKham) {
        jdbc.update("INSERT INTO phieukham (id_phieukham, id_benhan, id_pk, id_bacsy, sobhyt, thoi_gian_dky, thoi_gian_kham, "
                        + "trieu_chung, phi_kham, trang_thai) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'CHO_KHAM')",
                idPhieukham, idBenhan, idPk, idBacsy, soBhyt, Db.sqlTs(registeredAt), Db.sqlTs(examAt),
                Db.trimToNull(trieuChung), phiKham);
    }

    public void updateClinical(String idPhieukham, LocalDateTime examAt, String trieuChung, String ketLuan, String idBacsy) {
        jdbc.update("UPDATE phieukham SET thoi_gian_kham = ?, trieu_chung = ?, ket_luan = ?, id_bacsy = COALESCE(?, id_bacsy) "
                        + "WHERE id_phieukham = ?",
                Db.sqlTs(examAt), Db.trimToNull(trieuChung), Db.trimToNull(ketLuan), idBacsy, idPhieukham);
    }

    public void updateStatus(String idPhieukham, String statusCode) {
        jdbc.update("UPDATE phieukham SET trang_thai = ? WHERE id_phieukham = ?", statusCode, idPhieukham);
    }

    public void replacePrimaryDiagnosis(String idPhieukham, String idIcd) {
        jdbc.update("DELETE FROM phieukham_chandoan WHERE id_phieukham = ? AND loai = 'CHINH'", idPhieukham);
        if (!Db.blank(idIcd)) {
            jdbc.update("INSERT INTO phieukham_chandoan (id_phieukham, id_icd, loai) VALUES (?, ?, 'CHINH') "
                    + "ON CONFLICT (id_phieukham, id_icd) DO UPDATE SET loai = 'CHINH'", idPhieukham, idIcd);
        }
    }

    public static LocalDateTime combine(LocalDate date, LocalTime time) {
        return (date == null || time == null) ? null : date.atTime(time);
    }

    public Collection<String> allIds() {
        return jdbc.queryForList("SELECT id_phieukham FROM phieukham", String.class);
    }
}
