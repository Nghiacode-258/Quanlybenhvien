package com.hms.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.hms.Entity.HealthInsurance;
import com.hms.Entity.MedicalRecord;
import com.hms.Entity.Patient;
import com.hms.Entity.PatientRelative;

/** Bệnh nhân, số điện thoại, người thân, BHYT, bệnh án (bảng benhnhan*, nguoithan*, baohiemyte, benhan). */
@Repository
public class PatientRepository {

    private final JdbcTemplate jdbc;
    private final IdGenerator ids;

    public PatientRepository(JdbcTemplate jdbc, IdGenerator ids) {
        this.jdbc = jdbc;
        this.ids = ids;
    }

    // ---------------------------------------------------------------- đọc

    private static final String SELECT_PATIENT = """
            SELECT id_bn, ho_ten, TRIM(cccd) AS cccd, COALESCE(to_char(ngay_sinh, 'YYYY-MM-DD'), '') AS ngay_sinh,
                   COALESCE(gioi_tinh, '') AS gioi_tinh, COALESCE(dia_chi, '') AS dia_chi,
                   COALESCE(to_char(ngay_tao, 'YYYY-MM-DD'), '') AS ngay_tao, trang_thai
            FROM benhnhan
            """;

    public List<Patient> findAll() {
        return load(null);
    }

    public Patient findById(String idBn) {
        if (idBn == null) return null;
        List<Patient> list = load(idBn);
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean exists(String idBn) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM benhnhan WHERE id_bn = ?", Integer.class, idBn);
        return n != null && n > 0;
    }

    /** Nạp bệnh nhân + SĐT + người thân + BHYT bằng 5 truy vấn (tránh N+1 vì CSDL ở xa). */
    private List<Patient> load(String onlyId) {
        String where = onlyId == null ? "" : " WHERE id_bn = ?";
        Object[] args = onlyId == null ? new Object[0] : new Object[] { onlyId };

        List<Patient> patients = jdbc.query(SELECT_PATIENT + where + " ORDER BY length(id_bn) DESC, id_bn DESC", (rs, i) -> {
            Patient p = new Patient();
            p.setIdBn(rs.getString("id_bn"));
            p.setHoTen(rs.getString("ho_ten"));
            p.setCccd(Db.nz(rs.getString("cccd")));
            p.setNgaySinh(rs.getString("ngay_sinh"));
            p.setGioiTinh(rs.getString("gioi_tinh"));
            p.setDiaChi(rs.getString("dia_chi"));
            p.setNgayTao(rs.getString("ngay_tao"));
            p.setTrangThai(rs.getString("trang_thai"));
            p.setSdt(new ArrayList<>());
            return p;
        }, args);
        if (patients.isEmpty()) return patients;

        Map<String, Patient> byId = new LinkedHashMap<>();
        for (Patient p : patients) byId.put(p.getIdBn(), p);

        // SĐT bệnh nhân
        jdbc.query("SELECT id_bn, sdt FROM benhnhan_sdt" + where + " ORDER BY id_bn, sdt", (rs) -> {
            Patient p = byId.get(rs.getString("id_bn"));
            if (p != null) p.getSdt().add(rs.getString("sdt"));
        }, args);

        // Người thân (+ SĐT)
        Map<String, PatientRelative> relById = new HashMap<>();
        jdbc.query("SELECT id_nt, id_bn, ho_ten, COALESCE(moi_quan_he, '') AS moi_quan_he, COALESCE(gioi_tinh, '') AS gioi_tinh "
                + "FROM nguoithan" + where + " ORDER BY id_nt DESC", (rs) -> {
            Patient p = byId.get(rs.getString("id_bn"));
            if (p == null) return;
            PatientRelative r = new PatientRelative(rs.getString("id_nt"), rs.getString("id_bn"), rs.getString("ho_ten"),
                    rs.getString("moi_quan_he"), rs.getString("gioi_tinh"), new ArrayList<>());
            relById.put(r.getIdNt(), r);
            p.setRelative(r); // UI chỉ hiển thị 1 người thân; ORDER BY id_nt DESC nên bản ghi cũ nhất ghi đè cuối cùng
        }, args);
        if (!relById.isEmpty()) {
            jdbc.query("SELECT id_nt, sdt FROM nguoithan_sdt ORDER BY id_nt, sdt", (rs) -> {
                PatientRelative r = relById.get(rs.getString("id_nt"));
                if (r != null) r.getSdt().add(rs.getString("sdt"));
            });
        }

        // BHYT: lấy thẻ có ngày hết hạn muộn nhất của mỗi bệnh nhân
        jdbc.query("SELECT DISTINCT ON (id_bn) sobhyt, id_bn, muc_huong, to_char(ngay_bat_dau, 'YYYY-MM-DD') AS bd, "
                + "to_char(ngay_ket_thuc, 'YYYY-MM-DD') AS kt, COALESCE(noi_dang_ky, '') AS noi_dang_ky, "
                + "COALESCE(nhom_doi_tuong, '') AS nhom_doi_tuong FROM baohiemyte" + where
                + " ORDER BY id_bn, ngay_ket_thuc DESC", (rs) -> {
            Patient p = byId.get(rs.getString("id_bn"));
            if (p == null) return;
            String so = rs.getString("sobhyt");
            p.setInsurance(new HealthInsurance(so, rs.getString("id_bn"), so, rs.getInt("muc_huong"),
                    rs.getString("bd"), rs.getString("kt"), rs.getString("noi_dang_ky"), rs.getString("nhom_doi_tuong")));
        }, args);

        return patients;
    }

    // ---------------------------------------------------------------- ghi bệnh nhân

    public String nextPatientId() {
        return ids.next("benhnhan", "id_bn", "BN", 3);
    }

    public void insert(Patient p) {
        check_before_insert(p);

        jdbc.update("INSERT INTO benhnhan (id_bn, ho_ten, ngay_sinh, cccd, gioi_tinh, dia_chi, ngay_tao, trang_thai) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                p.getIdBn(), p.getHoTen(), Db.sqlDate(Db.parseDate(p.getNgaySinh(), "Ngày sinh")),
                Db.trimToNull(p.getCccd()), Db.trimToNull(p.getGioiTinh()), Db.trimToNull(p.getDiaChi()),
                Db.sqlDate(Db.parseDate(p.getNgayTao(), "Ngày tạo")), p.getTrangThai());
        replacePhones(p.getIdBn(), p.getSdt());
    }

    private void check_before_insert(Patient p) {
        if (Db.trimToNull(p.getHoTen()) == null)
            throw new IllegalArgumentException("Họ và tên không được để trống");
        if (Db.trimToNull(p.getNgaySinh()) == null)
            throw new IllegalArgumentException("Ngày sinh không được để trống");
        java.time.LocalDate ngaySinh = java.time.LocalDate.parse(Db.trimToNull(p.getNgaySinh()));
        if (ngaySinh.isAfter(java.time.LocalDate.now()))
            throw new IllegalArgumentException("Ngày sinh không hợp lệ");
        String cccd = Db.trimToNull(p.getCccd());
        if (cccd == null)
            throw new IllegalArgumentException("Số CCCD không được để trống");
        if (!cccd.matches("\\d{12}"))
            throw new IllegalArgumentException("Số CCCD phải gồm đúng 12 chữ số");
        if (Db.trimToNull(p.getGioiTinh()) == null)
            throw new IllegalArgumentException("Giới tính không được để trống");
        if (Db.trimToNull(p.getDiaChi()) == null)
            throw new IllegalArgumentException("Địa chỉ không được để trống");
        check_sdt(p.getPhoneDisplay());
        // check_bhyt(h.getSoBhyt());
    }

    private void check_sdt(String sdt) {
        String s = Db.trimToNull(sdt);
        if (s == null)
            throw new IllegalArgumentException("Số điện thoại không được để trống");

        java.util.Set<String> daGap = new java.util.HashSet<>();
        for (String part : s.split(",")) {
            String so = part.trim();
            if (so.isEmpty())
                throw new IllegalArgumentException("Số điện thoại không được để trống giữa các dấu phẩy");
            if (!so.matches("0\\d{9,10}"))
                throw new IllegalArgumentException("Số điện thoại \"" + so + "\" không hợp lệ (phải gồm 10 hoặc 11 chứ số, bắt đầu bằng 0)");
            if (!daGap.add(so))
                throw new IllegalArgumentException("Số điện thoại \"" + so + "\" bị nhập trùng");
        }
    }

    public void check_bhyt(String bhyt){
        String s =  Db.trimToNull(bhyt);

        // if (s == null)
        //     throw new IllegalArgumentException("Số bảo hiểm y tế không được để trống");
        s = s.replaceAll("[\\s-]", "").toUpperCase();

        if (!s.matches("[A-Z]{2}[1-5]\\d{12}"))
            throw new IllegalArgumentException(
                "Số thẻ BHYT không hợp lệ (gồm 15 ký tự: 2 chữ cái + 13 chữ số)");
    }

    public void update(Patient p) {
        jdbc.update("UPDATE benhnhan SET ho_ten = ?, ngay_sinh = ?, cccd = ?, gioi_tinh = ?, dia_chi = ?, trang_thai = ? WHERE id_bn = ?",
                p.getHoTen(), Db.sqlDate(Db.parseDate(p.getNgaySinh(), "Ngày sinh")), Db.trimToNull(p.getCccd()),
                Db.trimToNull(p.getGioiTinh()), Db.trimToNull(p.getDiaChi()), p.getTrangThai(), p.getIdBn());
        replacePhones(p.getIdBn(), p.getSdt());
    }

    private void replacePhones(String idBn, Collection<String> phones) {
        jdbc.update("DELETE FROM benhnhan_sdt WHERE id_bn = ?", idBn);
        for (String s : Db.nullSafe(phones)) {
            jdbc.update("INSERT INTO benhnhan_sdt (id_bn, sdt) VALUES (?, ?) ON CONFLICT DO NOTHING", idBn, s);
        }
    }

    // ---------------------------------------------------------------- người thân

    public String firstRelativeId(String idBn) {
        List<String> l = jdbc.queryForList("SELECT id_nt FROM nguoithan WHERE id_bn = ? ORDER BY id_nt LIMIT 1", String.class, idBn);
        return l.isEmpty() ? null : l.get(0);
    }

    public String nextRelativeId() {
        return ids.next("nguoithan", "id_nt", "NT", 3);
    }

    public void upsertRelative(PatientRelative r, boolean exists) {
        if (exists) {
            jdbc.update("UPDATE nguoithan SET ho_ten = ?, moi_quan_he = ?, gioi_tinh = ? WHERE id_nt = ?",
                    r.getHoTen(), Db.trimToNull(r.getMoiQuanHe()), Db.trimToNull(r.getGioiTinh()), r.getIdNt());
        } else {
            jdbc.update("INSERT INTO nguoithan (id_nt, id_bn, ho_ten, moi_quan_he, gioi_tinh) VALUES (?, ?, ?, ?, ?)",
                    r.getIdNt(), r.getIdBn(), r.getHoTen(), Db.trimToNull(r.getMoiQuanHe()), Db.trimToNull(r.getGioiTinh()));
        }
        jdbc.update("DELETE FROM nguoithan_sdt WHERE id_nt = ?", r.getIdNt());
        for (String s : Db.nullSafe(r.getSdt())) {
            jdbc.update("INSERT INTO nguoithan_sdt (id_nt, sdt) VALUES (?, ?) ON CONFLICT DO NOTHING", r.getIdNt(), s);
        }
    }

    // ---------------------------------------------------------------- BHYT

    /** Số BHYT hiện tại của bệnh nhân (thẻ hết hạn muộn nhất) hoặc null. */
    public String currentInsuranceNumber(String idBn) {
        List<String> l = jdbc.queryForList("SELECT sobhyt FROM baohiemyte WHERE id_bn = ? ORDER BY ngay_ket_thuc DESC LIMIT 1",
                String.class, idBn);
        return l.isEmpty() ? null : l.get(0);
    }

    public boolean insuranceNumberUsedByOther(String soBhyt, String idBn) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM baohiemyte WHERE sobhyt = ? AND id_bn <> ?", Integer.class, soBhyt, idBn);
        return n != null && n > 0;
    }

    /** Thẻ BHYT còn hiệu lực tại ngày chỉ định (dùng gắn vào phiếu khám) hoặc null. */
    public String validInsuranceNumber(String idBn, LocalDate onDate) {
        List<String> l = jdbc.queryForList(
                "SELECT sobhyt FROM baohiemyte WHERE id_bn = ? AND ngay_bat_dau <= ? AND ngay_ket_thuc >= ? "
                        + "ORDER BY ngay_ket_thuc DESC LIMIT 1", String.class, idBn, Db.sqlDate(onDate), Db.sqlDate(onDate));
        return l.isEmpty() ? null : l.get(0);
    }

    /** Thêm mới hoặc cập nhật thẻ BHYT; oldNumber != null nghĩa là thẻ hiện có (có thể đổi số thẻ). */
    public void upsertInsurance(String oldNumber, HealthInsurance h) {
        LocalDate bd = Db.parseDate(h.getNgayBatDau(), "Ngày bắt đầu BHYT");
        LocalDate kt = Db.parseDate(h.getNgayKetThuc(), "Ngày kết thúc BHYT");
        if (oldNumber == null) {
            jdbc.update("INSERT INTO baohiemyte (sobhyt, id_bn, muc_huong, nhom_doi_tuong, noi_dang_ky, ngay_bat_dau, ngay_ket_thuc) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    h.getSoBhyt(), h.getIdBn(), h.getMucHuong(), Db.trimToNull(h.getNhomDoiTuong()),
                    Db.trimToNull(h.getNoiDangKy()), Db.sqlDate(bd), Db.sqlDate(kt));
        } else {
            jdbc.update("UPDATE baohiemyte SET sobhyt = ?, muc_huong = ?, nhom_doi_tuong = ?, noi_dang_ky = ?, "
                            + "ngay_bat_dau = ?, ngay_ket_thuc = ? WHERE sobhyt = ?",
                    h.getSoBhyt(), h.getMucHuong(), Db.trimToNull(h.getNhomDoiTuong()), Db.trimToNull(h.getNoiDangKy()),
                    Db.sqlDate(bd), Db.sqlDate(kt), oldNumber);
        }
    }

    // ---------------------------------------------------------------- bệnh án

    public List<MedicalRecord> findRecordsByPatient(String idBn) {
        return jdbc.query("SELECT id_benhan, id_bn, to_char(ngay_tao, 'YYYY-MM-DD') AS ngay_tao, COALESCE(tien_su_benh, '') AS tien_su_benh, "
                        + "trang_thai FROM benhan WHERE id_bn = ? ORDER BY ngay_tao DESC, id_benhan DESC",
                (rs, i) -> new MedicalRecord(rs.getString("id_benhan"), rs.getString("id_bn"), rs.getString("ngay_tao"),
                        rs.getString("tien_su_benh"), Labels.RECORD_STATUS.label(rs.getString("trang_thai"))),
                idBn);
    }

    /** Bệnh án đang mở gần nhất của bệnh nhân; chưa có thì tạo mới (phiếu khám bắt buộc thuộc 1 bệnh án). */
    public String ensureOpenRecord(String idBn, LocalDate today) {
        List<String> l = jdbc.queryForList(
                "SELECT id_benhan FROM benhan WHERE id_bn = ? AND trang_thai = 'DANG_MO' ORDER BY ngay_tao DESC, id_benhan DESC LIMIT 1",
                String.class, idBn);
        if (!l.isEmpty()) return l.get(0);
        String id = ids.next("benhan", "id_benhan", "BA", 3);
        jdbc.update("INSERT INTO benhan (id_benhan, id_bn, ngay_tao, tien_su_benh, trang_thai) VALUES (?, ?, ?, NULL, 'DANG_MO')",
                id, idBn, Db.sqlDate(today));
        return id;
    }
}
