package com.hms.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.Entity.HealthInsurance;
import com.hms.Entity.MedicalRecord;
import com.hms.Entity.Patient;
import com.hms.Entity.PatientRelative;
import com.hms.Entity.Staff;
import com.hms.Repository.Db;
import com.hms.Repository.ExaminationRepository;
import com.hms.Repository.PatientRepository;
import com.hms.dto.PatientFormDto;

/** Bệnh nhân, người thân, BHYT, bệnh án — dữ liệu lưu trên Supabase (PostgreSQL). */
@Service
public class PatientService {

    private final PatientRepository patients;
    private final ExaminationRepository examinations;

    public PatientService(PatientRepository patients, ExaminationRepository examinations) {
        this.patients = patients;
        this.examinations = examinations;
    }

    public List<Patient> getAllPatients() {
        return patients.findAll();
    }

    public Patient getPatientById(String id) {
        return patients.findById(id);
    }

    public List<Patient> searchPatients(String keyword, String status) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        return getAllPatients().stream()
                .filter(p -> {
                    boolean matchesStatus = (status == null || status.isEmpty() || "all".equalsIgnoreCase(status)
                            || status.equalsIgnoreCase(p.getTrangThai()));
                    if (!matchesStatus) return false;
                    if (kw.isEmpty()) return true;
                    String target = (p.getIdBn() + " " + p.getHoTen() + " " + (p.getCccd() != null ? p.getCccd() : "") + " "
                            + p.getPhoneDisplay() + " " + (p.getDiaChi() != null ? p.getDiaChi() : "")).toLowerCase();
                    return target.contains(kw);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public synchronized Patient savePatient(PatientFormDto dto) {
        String patientId = dto.getIdBn();
        boolean isEdit = patientId != null && !patientId.trim().isEmpty() && patients.exists(patientId.trim());
        if (isEdit) {
            patientId = patientId.trim();
        } else {
            patientId = patients.nextPatientId();
        }

        String hoTen = dto.getHoTen() != null ? dto.getHoTen().trim() : "";
        if (hoTen.isEmpty()) throw new IllegalArgumentException("Họ tên bệnh nhân không được để trống.");
        if (hoTen.length() > 100) throw new IllegalArgumentException("Họ tên tối đa 100 ký tự.");
        String cccd = dto.getCccd() != null ? dto.getCccd().trim() : "";
        if (cccd.length() > 12) throw new IllegalArgumentException("Số CCCD tối đa 12 ký tự.");

        List<String> phoneList = splitPhones(dto.getSdt());

        Patient p = new Patient();
        p.setIdBn(patientId);
        p.setHoTen(hoTen);
        p.setCccd(cccd);
        p.setNgaySinh(dto.getNgaySinh());
        p.setGioiTinh(dto.getGioiTinh() != null ? dto.getGioiTinh() : "Nam");
        p.setSdt(phoneList);
        p.setDiaChi(dto.getDiaChi() != null ? dto.getDiaChi().trim() : "");
        p.setTrangThai(dto.getTrangThai() != null && !dto.getTrangThai().isBlank() ? dto.getTrangThai().trim() : "Ổn định");
        p.setNgayTao(LocalDate.now().toString());

        try {
            if (isEdit) patients.update(p); else patients.insert(p);

            // Người thân (UI quản lý 1 người thân / bệnh nhân)
            if (dto.getRelHoTen() != null && !dto.getRelHoTen().trim().isEmpty()) {
                String relId = patients.firstRelativeId(patientId);
                boolean relExists = relId != null;
                if (!relExists) relId = patients.nextRelativeId();
                PatientRelative rel = new PatientRelative();
                rel.setIdNt(relId);
                rel.setIdBn(patientId);
                rel.setHoTen(dto.getRelHoTen().trim());
                rel.setMoiQuanHe(dto.getRelMoiQuanHe() != null && !dto.getRelMoiQuanHe().isBlank() ? dto.getRelMoiQuanHe().trim() : "Người nhà");
                rel.setGioiTinh(dto.getRelGioiTinh());
                List<String> relPhones = new ArrayList<>();
                if (dto.getRelSdt() != null && !dto.getRelSdt().trim().isEmpty()) relPhones.add(dto.getRelSdt().trim());
                rel.setSdt(relPhones);
                patients.upsertRelative(rel, relExists);
            }

            // BHYT
            if (dto.getSoBhyt() != null && !dto.getSoBhyt().trim().isEmpty()) {
                String so = dto.getSoBhyt().trim();
                patients.check_bhyt(so);
                if (so.length() > 15) throw new IllegalArgumentException("Số thẻ BHYT tối đa 15 ký tự.");
                if (patients.insuranceNumberUsedByOther(so, patientId)) {
                    throw new IllegalArgumentException("Số thẻ BHYT [" + so + "] đã thuộc về bệnh nhân khác.");
                }
                LocalDate start = Db.parseDate(dto.getNgayBatDau(), "Ngày bắt đầu BHYT");
                LocalDate end = Db.parseDate(dto.getNgayKetThuc(), "Ngày kết thúc BHYT");
                if (start == null) start = LocalDate.now();
                if (end == null) end = start.plusYears(1); // CSDL bắt buộc có hạn thẻ
                if (end.isBefore(start)) throw new IllegalArgumentException("Ngày kết thúc BHYT phải sau ngày bắt đầu.");
                int level = dto.getMucHuong() != null ? dto.getMucHuong() : 80;
                if (level < 0 || level > 100) throw new IllegalArgumentException("Mức hưởng BHYT phải từ 0 đến 100.");

                HealthInsurance ins = new HealthInsurance(so, patientId, so, level, start.toString(), end.toString(),
                        dto.getNoiDangKy(), dto.getNhomDoiTuong());
                patients.upsertInsurance(patients.currentInsuranceNumber(patientId), ins);
            }
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Dữ liệu bệnh nhân không hợp lệ hoặc bị trùng (kiểm tra độ dài CCCD, số điện thoại, số BHYT).");
        }
        return patients.findById(patientId);
    }

    private static List<String> splitPhones(String raw) {
        List<String> phones = new ArrayList<>();
        if (raw != null && !raw.trim().isEmpty()) {
            for (String part : raw.split("[,;\\n]+")) {
                String clean = part.trim();
                if (clean.isEmpty()) continue;
                if (clean.length() > 15) throw new IllegalArgumentException("Số điện thoại [" + clean + "] quá dài (tối đa 15 ký tự).");
                if (!phones.contains(clean)) phones.add(clean);
            }
        }
        return phones;
    }

    public List<MedicalRecord> getMedicalRecordsByPatient(String patientId) {
        if (patientId == null) return Collections.emptyList();
        return patients.findRecordsByPatient(patientId);
    }

    public List<Patient> getPatientsForStaff(Staff staff, String keyword, String status) {
        if (staff == null || !"Bác sĩ".equalsIgnoreCase(staff.getVaiTro())) {
            return searchPatients(keyword, status);
        }
        Set<String> doctorPatientIds = examinations.findPatientIdsForDoctor(staff.getIdNv(), staff.getIdPk());
        return searchPatients(keyword, status).stream()
                .filter(p -> doctorPatientIds.contains(p.getIdBn()))
                .collect(Collectors.toList());
    }
}
