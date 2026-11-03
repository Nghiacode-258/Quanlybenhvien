package com.hms.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.Entity.Medicine;
import com.hms.Entity.MedicineLot;
import com.hms.Entity.Prescription;
import com.hms.Entity.PrescriptionDetail;
import com.hms.Repository.Db;
import com.hms.Repository.PharmacyRepository;
import com.hms.Repository.PharmacyRepository.LotStock;
import com.hms.Repository.PrescriptionRepository;
import com.hms.Repository.StaffRepository;
import com.hms.dto.MedicineFormDto;
import com.hms.dto.MedicineLotFormDto;

/** Danh mục thuốc, kho lô (FEFO) và cấp phát đơn thuốc — lưu trên Supabase. */
@Service
public class PharmacyService {

    private final PharmacyRepository pharmacy;
    private final PrescriptionRepository prescriptions;
    private final StaffRepository staff;

    public PharmacyService(PharmacyRepository pharmacy, PrescriptionRepository prescriptions, StaffRepository staff) {
        this.pharmacy = pharmacy;
        this.prescriptions = prescriptions;
        this.staff = staff;
    }

    public List<Medicine> getAllMedicines() {
        return pharmacy.findAllMedicines();
    }

    public List<MedicineLot> getAllMedicineLots() {
        return pharmacy.findAllLots();
    }

    public List<Prescription> getAllPrescriptions() {
        return prescriptions.findAll();
    }

    public Prescription getPrescriptionById(String id) {
        return prescriptions.findById(id);
    }

    public Prescription getPrescriptionByExamId(String examId) {
        return prescriptions.findByExamId(examId);
    }

    public Medicine getMedicineById(String id) {
        return pharmacy.findMedicineById(id);
    }

    public MedicineLot getLotById(String idLo) {
        return pharmacy.findLotById(idLo);
    }

    /** Thêm thuốc mới vào danh mục (tồn kho ban đầu = 0 cho tới khi nhập lô). */
    @Transactional
    public synchronized Medicine createMedicine(MedicineFormDto dto) {
        if (dto.getTenThuoc() == null || dto.getTenThuoc().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên thuốc không được để trống.");
        }
        if (dto.getDonVi() == null || dto.getDonVi().trim().isEmpty()) {
            throw new IllegalArgumentException("Đơn vị tính không được để trống.");
        }
        if (dto.getGiaBan() != null && dto.getGiaBan() < 0) {
            throw new IllegalArgumentException("Giá bán không được nhỏ hơn 0.");
        }
        if (dto.getTenThuoc().trim().length() > 150) throw new IllegalArgumentException("Tên thuốc tối đa 150 ký tự.");
        if (dto.getDonVi().trim().length() > 20) throw new IllegalArgumentException("Đơn vị tính tối đa 20 ký tự.");

        String idThuoc = dto.getIdThuoc() != null ? dto.getIdThuoc().trim().toUpperCase() : "";
        if (idThuoc.isEmpty()) {
            idThuoc = pharmacy.nextMedicineId();
        } else {
            if (idThuoc.length() > 10) throw new IllegalArgumentException("Mã thuốc tối đa 10 ký tự.");
            if (pharmacy.findMedicineById(idThuoc) != null) {
                throw new IllegalArgumentException("Mã thuốc [" + idThuoc + "] đã tồn tại trong danh mục.");
            }
        }

        Medicine med = new Medicine(idThuoc, dto.getTenThuoc().trim(),
                dto.getNhaSanXuat() != null ? dto.getNhaSanXuat().trim() : "",
                Boolean.TRUE.equals(dto.getCanKeDon()), 0, dto.getDonVi().trim(),
                dto.getGiaBan() != null ? dto.getGiaBan() : 0L);
        try {
            pharmacy.insertMedicine(med);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Không thể lưu thuốc: dữ liệu không hợp lệ hoặc mã thuốc bị trùng.");
        }
        return pharmacy.findMedicineById(idThuoc);
    }

    /** Xóa thuốc: chỉ cho xóa khi chưa có lô và chưa nằm trong đơn thuốc nào (bảo toàn lịch sử). */
    @Transactional
    public synchronized String deleteMedicine(String idThuoc) {
        Medicine med = pharmacy.findMedicineById(idThuoc);
        if (med == null) throw new IllegalArgumentException("Không tìm thấy mã thuốc: " + idThuoc);
        if (pharmacy.countLots(idThuoc) > 0 || pharmacy.countPrescriptionItems(idThuoc) > 0) {
            throw new IllegalArgumentException("Thuốc [" + med.getTenThuoc()
                    + "] đã có lô hoặc nằm trong đơn thuốc nên không thể xóa (bảo toàn lịch sử dữ liệu).");
        }
        pharmacy.deleteMedicine(idThuoc);
        return "Đã xóa hoàn toàn thuốc [" + idThuoc + " - " + med.getTenThuoc() + "] khỏi danh mục thành công.";
    }

    /** Đồng bộ tồn kho thuốc từ các lô còn hạn và đang hoạt động. */
    @Transactional
    public synchronized void recalculateMedicineStock(String idThuoc) {
        if (idThuoc == null) return;
        pharmacy.recalculateStock(idThuoc, LocalDate.now());
    }

    /** Nhập lô thuốc mới. */
    @Transactional
    public synchronized MedicineLot createLot(MedicineLotFormDto dto) {
        if (dto.getIdThuoc() == null || dto.getIdThuoc().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn thuốc hợp lệ.");
        }
        Medicine med = pharmacy.findMedicineById(dto.getIdThuoc().trim());
        if (med == null) throw new IllegalArgumentException("Thuốc [" + dto.getIdThuoc() + "] không tồn tại.");
        if (dto.getSoLo() == null || dto.getSoLo().trim().isEmpty()) throw new IllegalArgumentException("Số lô không được để trống.");
        String soLo = dto.getSoLo().trim().toUpperCase();
        if (soLo.length() > 30) throw new IllegalArgumentException("Số lô tối đa 30 ký tự.");
        if (pharmacy.lotNumberExists(soLo)) throw new IllegalArgumentException("Số lô [" + soLo + "] đã tồn tại trong hệ thống.");
        if (dto.getSoLuongNhap() == null || dto.getSoLuongNhap() <= 0) throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0.");
        if (dto.getGiaNhap() != null && dto.getGiaNhap() < 0) throw new IllegalArgumentException("Giá nhập không được âm.");
        if (dto.getGiaBan() != null && dto.getGiaBan() < 0) throw new IllegalArgumentException("Giá bán không được âm.");
        if (dto.getHanSuDung() == null || dto.getHanSuDung().trim().isEmpty()) throw new IllegalArgumentException("Hạn sử dụng không được để trống.");

        LocalDate ngayNhap = LocalDate.now();
        if (dto.getNgayNhap() != null && !dto.getNgayNhap().trim().isEmpty()) {
            ngayNhap = Db.parseDate(dto.getNgayNhap(), "Ngày nhập");
        }
        LocalDate expDate = parseDateOrThrow(dto.getHanSuDung());
        if (!expDate.isAfter(ngayNhap)) {
            throw new IllegalArgumentException("Hạn sử dụng (" + expDate + ") phải sau ngày nhập (" + ngayNhap + ").");
        }
        if (dto.getNgaySanXuat() != null && !dto.getNgaySanXuat().trim().isEmpty()) {
            LocalDate mfg = parseDateOrThrow(dto.getNgaySanXuat());
            if (mfg.isAfter(expDate)) throw new IllegalArgumentException("Ngày sản xuất không thể sau hạn sử dụng.");
        }

        String idLo = pharmacy.nextLotId();
        MedicineLot lot = new MedicineLot(idLo, med.getIdThuoc(), soLo, expDate.toString(), dto.getSoLuongNhap(),
                dto.getSoLuongNhap(), dto.getGiaNhap() != null ? dto.getGiaNhap() : 0L,
                dto.getGiaBan() != null ? dto.getGiaBan() : (med.getGiaBan() != null ? med.getGiaBan() : 0L),
                ngayNhap.toString(), dto.getNgaySanXuat() != null ? dto.getNgaySanXuat().trim() : "",
                dto.getNhaCungCap() != null ? dto.getNhaCungCap().trim() : "", "Hoạt động");
        try {
            pharmacy.insertLot(lot);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Không thể nhập lô: số lô đã tồn tại cho thuốc này hoặc dữ liệu không hợp lệ.");
        }
        pharmacy.recalculateStock(med.getIdThuoc(), LocalDate.now());
        return pharmacy.findLotById(idLo);
    }

    private static LocalDate parseDateOrThrow(String s) {
        try {
            return LocalDate.parse(s.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Định dạng ngày không hợp lệ (chuẩn yyyy-MM-dd).");
        }
    }

    /** Loại bỏ lô đã hết hạn: lô chưa từng xuất kho thì xóa hẳn, lô đã xuất kho thì giữ lịch sử và đưa tồn về 0. */
    @Transactional
    public synchronized String deleteExpiredLot(String idLo) {
        MedicineLot lot = pharmacy.findLotById(idLo);
        if (lot == null) throw new IllegalArgumentException("Không tìm thấy lô thuốc mã: " + idLo);
        if (!lot.isExpired()) {
            throw new IllegalStateException("Lô thuốc [" + lot.getSoLo() + "] còn hạn sử dụng đến " + lot.getHanSuDung()
                    + ", không thể loại bỏ theo quy định!");
        }
        if (lot.isRemoved()) throw new IllegalStateException("Lô thuốc [" + lot.getSoLo() + "] đã được loại bỏ trước đó.");

        int tonTruoc = lot.getSoLuongTon() != null ? lot.getSoLuongTon() : 0;
        if (lot.getSoLuongNhap() != null && tonTruoc < lot.getSoLuongNhap()) {
            pharmacy.markLotRemoved(idLo); // đã từng cấp phát -> giữ lịch sử xuất nhập
        } else {
            pharmacy.deleteLot(idLo);
        }
        pharmacy.recalculateStock(lot.getIdThuoc(), LocalDate.now());
        return "Đã loại bỏ lô thuốc hết hạn [" + lot.getSoLo() + "] (HSD: " + lot.getHanSuDung() + ") và trừ " + tonTruoc + " đơn vị tồn kho.";
    }

    public boolean dispensePrescription(String prescriptionId) {
        return dispensePrescription(prescriptionId, null);
    }

    /**
     * Cấp phát đơn thuốc: trừ kho theo FEFO (chỉ lô còn hạn & đang hoạt động).
     * Toàn bộ chạy trong 1 transaction: thiếu tồn kho ở bất kỳ thuốc nào thì hủy hết, đơn vẫn ở trạng thái chờ.
     */
    @Transactional
    public synchronized boolean dispensePrescription(String prescriptionId, String pharmacistId) {
        Prescription rx = prescriptions.findById(prescriptionId);
        if (rx == null || "Đã cấp phát".equalsIgnoreCase(rx.getTrangThai())) return false;
        List<PrescriptionDetail> details = rx.getItems();
        if (details == null || details.isEmpty()) {
            throw new IllegalStateException("Đơn thuốc " + prescriptionId + " chưa có chi tiết thuốc để cấp phát.");
        }

        String pharmacist = (pharmacistId != null && staff.pharmacistExists(pharmacistId)) ? pharmacistId : null;
        if (!prescriptions.markDispensed(prescriptionId, pharmacist)) return false;

        LocalDate today = LocalDate.now();
        for (PrescriptionDetail d : details) {
            int remaining = d.getSoLuong();
            for (LotStock lot : pharmacy.lockUsableLots(d.getIdThuoc(), today)) {
                if (remaining <= 0) break;
                int deduct = Math.min(lot.soLuongCon, remaining);
                pharmacy.setLotRemaining(lot.idLo, lot.soLuongCon - deduct);
                remaining -= deduct;
            }
            if (remaining > 0) {
                throw new IllegalStateException("Không đủ tồn kho cho thuốc [" + d.getTenThuoc() + "]: thiếu " + remaining + " "
                        + (d.getDonVi() != null ? d.getDonVi() : "đơn vị") + ".");
            }
            pharmacy.recalculateStock(d.getIdThuoc(), today);
        }
        return true;
    }

    public List<Medicine> searchMedicines(String keyword, String status) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        return getAllMedicines().stream()
                .filter(m -> {
                    if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status)) {
                        if ("outOfStock".equalsIgnoreCase(status) && (m.getSoLuongTonKho() != null && m.getSoLuongTonKho() > 0)) return false;
                    }
                    if (kw.isEmpty()) return true;
                    String target = (m.getIdThuoc() + " " + m.getTenThuoc() + " " + (m.getNhaSanXuat() != null ? m.getNhaSanXuat() : "")).toLowerCase();
                    return target.contains(kw);
                })
                .collect(Collectors.toList());
    }

    public List<Medicine> searchMedicines(String keyword) {
        return searchMedicines(keyword, null);
    }

    public List<MedicineLot> searchLots(String keyword, String status) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        return getAllMedicineLots().stream()
                .filter(lot -> {
                    if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status)) {
                        if ("expired".equalsIgnoreCase(status) && !lot.isExpired()) return false;
                        if ("expiring".equalsIgnoreCase(status) && !lot.isExpiringSoon()) return false;
                        if ("valid".equalsIgnoreCase(status) && (lot.isExpired() || lot.isExpiringSoon())) return false;
                    }
                    if (kw.isEmpty()) return true;
                    String target = (lot.getSoLo() + " " + lot.getIdThuoc() + " " + (lot.getTenThuoc() != null ? lot.getTenThuoc() : "") + " "
                            + (lot.getNhaCungCap() != null ? lot.getNhaCungCap() : "")).toLowerCase();
                    return target.contains(kw);
                })
                .collect(Collectors.toList());
    }

    public List<Prescription> searchPrescriptions(String keyword, String status) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        return getAllPrescriptions().stream()
                .filter(p -> {
                    if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status) && !status.equalsIgnoreCase(p.getTrangThai())) {
                        return false;
                    }
                    if (kw.isEmpty()) return true;
                    String target = (p.getIdDt() + " " + p.getIdPhieukham() + " " + (p.getPatientName() != null ? p.getPatientName() : "") + " "
                            + (p.getPatientId() != null ? p.getPatientId() : "")).toLowerCase();
                    return target.contains(kw);
                })
                .collect(Collectors.toList());
    }
}
