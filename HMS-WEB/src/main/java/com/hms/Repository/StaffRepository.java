package com.hms.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.hms.Entity.ClinicRoom;
import com.hms.Entity.Diagnosis;
import com.hms.Entity.LabType;
import com.hms.Entity.Specialty;
import com.hms.Entity.Staff;

/** Nhân viên (+bác sĩ/y tá/dược sĩ), chuyên khoa, phòng khám, loại xét nghiệm, danh mục chẩn đoán ICD. */
@Repository
public class StaffRepository {

    public static final String ROLE_DOCTOR = "Bác sĩ";
    public static final String ROLE_NURSE = "Y tá";
    public static final String ROLE_PHARMACIST = "Dược sĩ";

    private final JdbcTemplate jdbc;
    private final IdGenerator ids;

    public StaffRepository(JdbcTemplate jdbc, IdGenerator ids) {
        this.jdbc = jdbc;
        this.ids = ids;
    }

    // ================================================================ NHÂN VIÊN

    private static final String SELECT_STAFF = """
            SELECT nv.id_nv, nv.ho_ten, COALESCE(nv.gioi_tinh, '') AS gioi_tinh,
                   COALESCE(to_char(nv.ngay_sinh, 'YYYY-MM-DD'), '') AS ngay_sinh,
                   COALESCE(nv.email, '') AS email, nv.vai_tro, nv.trang_thai,
                   COALESCE(NULLIF(concat_ws(' · ', bs.hoc_vi, ck.ten_ck, yt.cap_bac, ds.bang_cap), ''),
                            nv.chi_tiet, '') AS chi_tiet,
                   bs.id_pk, bs.id_ck, bs.hoc_vi, bs.so_cchn
            FROM nhanvien nv
            LEFT JOIN bacsy bs ON bs.id_nv = nv.id_nv
            LEFT JOIN chuyenkhoa ck ON ck.id_ck = bs.id_ck
            LEFT JOIN yta yt ON yt.id_nv = nv.id_nv
            LEFT JOIN duocsi ds ON ds.id_nv = nv.id_nv
            """;

    public List<Staff> findAllStaff() {
        return loadStaff(null);
    }

    public Staff findStaffById(String idNv) {
        if (idNv == null) return null;
        List<Staff> l = loadStaff(idNv);
        return l.isEmpty() ? null : l.get(0);
    }

    private List<Staff> loadStaff(String onlyId) {
        String where = onlyId == null ? "" : " WHERE nv.id_nv = ?";
        Object[] args = onlyId == null ? new Object[0] : new Object[] { onlyId };
        List<Staff> list = jdbc.query(SELECT_STAFF + where + " ORDER BY nv.id_nv", (rs, i) -> {
            Staff s = new Staff();
            s.setIdNv(rs.getString("id_nv"));
            s.setHoTen(rs.getString("ho_ten"));
            s.setGioiTinh(rs.getString("gioi_tinh"));
            s.setNgaySinh(rs.getString("ngay_sinh"));
            s.setEmail(rs.getString("email"));
            s.setVaiTro(rs.getString("vai_tro"));
            s.setTrangThai(Labels.STAFF_STATUS.label(rs.getString("trang_thai")));
            s.setSpecialtyDetail(rs.getString("chi_tiet"));
            s.setIdPk(rs.getString("id_pk"));
            s.setIdCk(rs.getString("id_ck"));
            s.setHocVi(rs.getString("hoc_vi"));
            s.setSoCchn(rs.getString("so_cchn"));
            s.setSdt(new ArrayList<>());
            return s;
        }, args);
        if (list.isEmpty()) return list;
        Map<String, Staff> byId = new LinkedHashMap<>();
        for (Staff s : list) byId.put(s.getIdNv(), s);
        String phoneWhere = onlyId == null ? "" : " WHERE id_nv = ?";
        jdbc.query("SELECT id_nv, sdt FROM nhanvien_sdt" + phoneWhere + " ORDER BY id_nv, sdt", (rs) -> {
            Staff s = byId.get(rs.getString("id_nv"));
            if (s != null) s.getSdt().add(rs.getString("sdt"));
        }, args);
        return list;
    }

    public String nextStaffId() {
        return ids.next("nhanvien", "id_nv", "NV", 3);
    }

    /** nhanvien.chi_tiet chỉ dùng cho vai trò KHÔNG có bảng con (Thu ngân, Quản lý). */
    private static String freeTextDetail(Staff s) {
        String r = s.getVaiTro();
        boolean hasSubtable = ROLE_DOCTOR.equalsIgnoreCase(r) || ROLE_NURSE.equalsIgnoreCase(r) || ROLE_PHARMACIST.equalsIgnoreCase(r);
        return hasSubtable ? null : Db.trimToNull(s.getSpecialtyDetail());
    }

    /** Y tá -> yta.cap_bac, Dược sĩ -> duocsi.bang_cap (Bác sĩ: hoc_vi do assignDoctor ghi). */
    private void saveRoleDetail(Staff s) {
        String detail = Db.trimToNull(s.getSpecialtyDetail());
        if (ROLE_NURSE.equalsIgnoreCase(s.getVaiTro())) {
            jdbc.update("UPDATE yta SET cap_bac = ? WHERE id_nv = ?", detail, s.getIdNv());
        } else if (ROLE_PHARMACIST.equalsIgnoreCase(s.getVaiTro())) {
            jdbc.update("UPDATE duocsi SET bang_cap = ? WHERE id_nv = ?", detail, s.getIdNv());
        }
    }
    public void insertStaff(Staff s) {
        jdbc.update("INSERT INTO nhanvien (id_nv, ho_ten, gioi_tinh, ngay_sinh, vai_tro, trang_thai, email, chi_tiet) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                s.getIdNv(), s.getHoTen(), Db.trimToNull(s.getGioiTinh()),
                Db.sqlDate(Db.parseDate(s.getNgaySinh(), "Ngày sinh")), s.getVaiTro(),
                Labels.STAFF_STATUS.code(s.getTrangThai(), "DANG_LAM"), Db.trimToNull(s.getEmail()),
                freeTextDetail(s));
        replacePhones(s.getIdNv(), s.getSdt());
        syncSubtype(s.getIdNv(), s.getVaiTro());
        saveRoleDetail(s);
    }

    public void updateStaff(Staff s) {
        jdbc.update("UPDATE nhanvien SET ho_ten = ?, gioi_tinh = ?, ngay_sinh = ?, vai_tro = ?, trang_thai = ?, email = ?, chi_tiet = ? "
                        + "WHERE id_nv = ?",
                s.getHoTen(), Db.trimToNull(s.getGioiTinh()), Db.sqlDate(Db.parseDate(s.getNgaySinh(), "Ngày sinh")),
                s.getVaiTro(), Labels.STAFF_STATUS.code(s.getTrangThai(), "DANG_LAM"), Db.trimToNull(s.getEmail()),
                Db.trimToNull(s.getSpecialtyDetail()), s.getIdNv());
        replacePhones(s.getIdNv(), s.getSdt());
        syncSubtype(s.getIdNv(), s.getVaiTro());
    }

    private void replacePhones(String idNv, Collection<String> phones) {
        jdbc.update("DELETE FROM nhanvien_sdt WHERE id_nv = ?", idNv);
        for (String p : Db.nullSafe(phones)) {
            jdbc.update("INSERT INTO nhanvien_sdt (id_nv, sdt) VALUES (?, ?) ON CONFLICT DO NOTHING", idNv, p);
        }
    }

    /** Đảm bảo nhân viên có đúng 1 bản ghi ở bảng con theo vai trò (bacsy / yta / duocsi). */
    private void syncSubtype(String idNv, String vaiTro) {
        boolean doctor = ROLE_DOCTOR.equalsIgnoreCase(vaiTro);
        boolean nurse = ROLE_NURSE.equalsIgnoreCase(vaiTro);
        boolean pharmacist = ROLE_PHARMACIST.equalsIgnoreCase(vaiTro);
        if (!doctor) jdbc.update("DELETE FROM bacsy WHERE id_nv = ?", idNv);
        if (!nurse) jdbc.update("DELETE FROM yta WHERE id_nv = ?", idNv);
        if (!pharmacist) jdbc.update("DELETE FROM duocsi WHERE id_nv = ?", idNv);
        if (doctor) jdbc.update("INSERT INTO bacsy (id_nv) VALUES (?) ON CONFLICT DO NOTHING", idNv);
        if (nurse) jdbc.update("INSERT INTO yta (id_nv) VALUES (?) ON CONFLICT DO NOTHING", idNv);
        if (pharmacist) jdbc.update("INSERT INTO duocsi (id_nv) VALUES (?) ON CONFLICT DO NOTHING", idNv);
    }

    /** Ghi chuyên khoa, phòng khám, học vị, số chứng chỉ hành nghề của bác sĩ (bản ghi bacsy đã được syncSubtype tạo). */
    public void assignDoctor(String idNv, String idCk, String idPk, String hocVi, String soCchn) {
        jdbc.update("UPDATE bacsy SET id_ck = ?, id_pk = ?, hoc_vi = ?, so_cchn = ? WHERE id_nv = ?",
                idCk, idPk, hocVi, soCchn, idNv);
    }

    /** Số chứng chỉ hành nghề đã thuộc về bác sĩ khác chưa (cột so_cchn là UNIQUE). */
    public boolean licenseUsedByOther(String soCchn, String idNv) {
        return count2("SELECT COUNT(*) FROM bacsy WHERE UPPER(so_cchn) = UPPER(?) AND id_nv <> ?", soCchn, idNv) > 0;
    }

    public void deleteStaff(String idNv) {
        jdbc.update("DELETE FROM nhanvien WHERE id_nv = ?", idNv); // bảng sdt + bảng con xóa theo ON DELETE CASCADE
    }

    public boolean doctorInRoom(String idNv, String idPk) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM bacsy WHERE id_nv = ? AND id_pk = ?",
                Integer.class, idNv, idPk);
        return n != null && n > 0;
    }

    public List<Staff> findDoctorsByRoom(String idPk) {
        List<Staff> result = new ArrayList<>();
        for (Staff s : loadStaff(null)) {
            if (ROLE_DOCTOR.equalsIgnoreCase(s.getVaiTro()) && idPk.equals(s.getIdPk())) {
                result.add(s);
            }
        }
        return result;
    }

    public boolean doctorExists(String idNv) {
        return count("SELECT COUNT(*) FROM bacsy WHERE id_nv = ?", idNv) > 0;
    }

    public boolean staffExists(String idNv) {
        return count("SELECT COUNT(*) FROM nhanvien WHERE id_nv = ?", idNv) > 0;
    }

    public boolean pharmacistExists(String idNv) {
        return count("SELECT COUNT(*) FROM duocsi WHERE id_nv = ?", idNv) > 0;
    }

    public int countExamsOfDoctor(String idNv) {
        return count("SELECT COUNT(*) FROM phieukham WHERE id_bacsy = ?", idNv);
    }

    public int countDispensedBy(String idNv) {
        return count("SELECT COUNT(*) FROM donthuoc WHERE id_duocsi = ?", idNv);
    }

    public int countInvoicesCollectedBy(String idNv) {
        return count("SELECT COUNT(*) FROM phieuthuvienphi WHERE id_nv = ?", idNv);
    }

    public int countReceptionsBy(String idNv) {
        return count("SELECT COUNT(*) FROM phieukham_nhanvien WHERE id_nv = ?", idNv);
    }

    // ================================================================ CHUYÊN KHOA

    public List<Specialty> findAllSpecialties() {
        return jdbc.query("SELECT id_ck, ten_ck FROM chuyenkhoa ORDER BY id_ck",
                (rs, i) -> new Specialty(rs.getString("id_ck"), rs.getString("ten_ck")));
    }

    public Specialty findSpecialtyById(String idCk) {
        List<Specialty> l = jdbc.query("SELECT id_ck, ten_ck FROM chuyenkhoa WHERE id_ck = ?",
                (rs, i) -> new Specialty(rs.getString("id_ck"), rs.getString("ten_ck")), idCk);
        return l.isEmpty() ? null : l.get(0);
    }

    public String nextSpecialtyId() {
        return ids.next("chuyenkhoa", "id_ck", "CK", 2);
    }

    public void insertSpecialty(Specialty s) {
        jdbc.update("INSERT INTO chuyenkhoa (id_ck, ten_ck) VALUES (?, ?)", s.getIdCk(), s.getTenCk());
    }

    public void updateSpecialty(Specialty s) {
        jdbc.update("UPDATE chuyenkhoa SET ten_ck = ? WHERE id_ck = ?", s.getTenCk(), s.getIdCk());
    }

    public void deleteSpecialty(String idCk) {
        jdbc.update("DELETE FROM chuyenkhoa WHERE id_ck = ?", idCk);
    }

    public int countRoomsOfSpecialty(String idCk) {
        return count("SELECT COUNT(*) FROM phongkham WHERE id_ck = ?", idCk);
    }

    // ================================================================ PHÒNG KHÁM

    private static final String SELECT_ROOM = """
            SELECT p.id_pk, p.ten_phong, p.so_phong, p.trang_thai, p.id_ck, ck.ten_ck
            FROM phongkham p JOIN chuyenkhoa ck ON ck.id_ck = p.id_ck
            """;

    private static ClinicRoom mapRoom(java.sql.ResultSet rs, int i) throws java.sql.SQLException {
        ClinicRoom r = new ClinicRoom(rs.getString("id_pk"), rs.getString("ten_phong"), rs.getString("so_phong"),
                Labels.ROOM_STATUS.label(rs.getString("trang_thai")), rs.getString("id_ck"));
        r.setTenChuyenKhoa(rs.getString("ten_ck"));
        return r;
    }

    public List<ClinicRoom> findAllRooms() {
        return jdbc.query(SELECT_ROOM + " ORDER BY p.id_pk", StaffRepository::mapRoom);
    }

    public ClinicRoom findRoomById(String idPk) {
        List<ClinicRoom> l = jdbc.query(SELECT_ROOM + " WHERE p.id_pk = ?", StaffRepository::mapRoom, idPk);
        return l.isEmpty() ? null : l.get(0);
    }

    public String nextRoomId() {
        return ids.next("phongkham", "id_pk", "PK", 2);
    }

    public void insertRoom(ClinicRoom r) {
        jdbc.update("INSERT INTO phongkham (id_pk, id_ck, ten_phong, so_phong, trang_thai) VALUES (?, ?, ?, ?, ?)",
                r.getIdPk(), r.getIdCk(), r.getTenPhong(), r.getSoPhong(), Labels.ROOM_STATUS.code(r.getTrangThai(), "HOAT_DONG"));
    }

    public void updateRoom(ClinicRoom r) {
        jdbc.update("UPDATE phongkham SET id_ck = ?, ten_phong = ?, so_phong = ?, trang_thai = ? WHERE id_pk = ?",
                r.getIdCk(), r.getTenPhong(), r.getSoPhong(), Labels.ROOM_STATUS.code(r.getTrangThai(), "HOAT_DONG"), r.getIdPk());
    }

    public void deleteRoom(String idPk) {
        jdbc.update("DELETE FROM phongkham WHERE id_pk = ?", idPk);
    }

    public int countExamsOfRoom(String idPk) {
        return count("SELECT COUNT(*) FROM phieukham WHERE id_pk = ?", idPk);
    }

    // ================================================================ DANH MỤC

    public List<LabType> findAllLabTypes() {
        return jdbc.query("SELECT id_lxn, ten_lxn, chi_phi, trang_thai FROM loaixetnghiem ORDER BY id_lxn",
                (rs, i) -> new LabType(rs.getString("id_lxn"), rs.getString("ten_lxn"), Db.longOrZero(rs, "chi_phi"),
                        Labels.LAB_TYPE_STATUS.label(rs.getString("trang_thai"))));
    }

    /** id_icd chính là mã ICD (vd J06.9) nên idCd và maIcd cùng nhận giá trị này. */
    public List<Diagnosis> findAllDiagnoses() {
        return jdbc.query("SELECT id_icd, ten_benh FROM chandoan ORDER BY id_icd",
                (rs, i) -> new Diagnosis(rs.getString("id_icd"), rs.getString("id_icd"), rs.getString("ten_benh")));
    }

    public boolean diagnosisExists(String idIcd) {
        return count("SELECT COUNT(*) FROM chandoan WHERE id_icd = ?", idIcd) > 0;
    }

    private int count2(String sql, Object a, Object b) {
        Integer n = jdbc.queryForObject(sql, Integer.class, a, b);
        return n == null ? 0 : n;
    }

    private int count(String sql, Object arg) {
        Integer n = jdbc.queryForObject(sql, Integer.class, arg);
        return n == null ? 0 : n;
    }
}