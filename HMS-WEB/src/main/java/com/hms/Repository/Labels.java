package com.hms.Repository;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Chuyển đổi hai chiều giữa MÃ lưu trong CSDL (VD CHO_KHAM) và NHÃN hiển thị trên giao diện
 * (VD "Chờ khám"). Giao diện/Thymeleaf vẫn so sánh theo nhãn tiếng Việt nên cần giữ nguyên nhãn.
 */
public final class Labels {

    public static final class Bi {
        private final Map<String, String> codeToLabel = new LinkedHashMap<>();
        private final Map<String, String> labelToCode = new LinkedHashMap<>();
        private final String name;

        private Bi(String name, String... pairs) {
            this.name = name;
            for (int i = 0; i + 1 < pairs.length; i += 2) {
                codeToLabel.put(pairs[i], pairs[i + 1]);
                labelToCode.put(pairs[i + 1].toLowerCase(), pairs[i]);
            }
        }

        /** Mã -> nhãn. Mã lạ giữ nguyên để không làm hỏng giao diện. */
        public String label(String code) {
            if (code == null) return "";
            String l = codeToLabel.get(code);
            return l != null ? l : code;
        }

        /** Nhãn (hoặc chính mã) -> mã; rỗng -> defaultCode; không hợp lệ -> IllegalArgumentException. */
        public String code(String labelOrCode, String defaultCode) {
            if (labelOrCode == null || labelOrCode.trim().isEmpty()) return defaultCode;
            String t = labelOrCode.trim();
            if (codeToLabel.containsKey(t)) return t;
            String c = labelToCode.get(t.toLowerCase());
            if (c == null) {
                throw new IllegalArgumentException("Giá trị " + name + " không hợp lệ: " + labelOrCode);
            }
            return c;
        }
    }

    public static final Bi EXAM = new Bi("trạng thái phiếu khám",
            "CHO_KHAM", "Chờ khám",
            "DANG_KHAM", "Đang khám",
            "CHO_XET_NGHIEM", "Chờ xét nghiệm",
            "CHO_THANH_TOAN", "Chờ thanh toán",
            "DA_KHAM", "Đã khám",
            "HOAN_TAT", "Hoàn tất",
            "HUY", "Đã hủy");

    public static final Bi LAB_RESULT = new Bi("trạng thái xét nghiệm",
            "CHO_KET_QUA", "Chờ lấy mẫu",
            "DA_CO_KET_QUA", "Hoàn thành",
            "HUY", "Đã hủy");

    public static final Bi PRESCRIPTION = new Bi("trạng thái đơn thuốc",
            "CHO_CAP_PHAT", "Chờ cấp phát",
            "DA_CAP_PHAT", "Đã cấp phát",
            "HUY", "Đã hủy");

    public static final Bi INVOICE = new Bi("trạng thái hóa đơn",
            "CHUA_THU", "Chưa thanh toán",
            "DA_THU", "Đã thanh toán",
            "HUY", "Đã hủy");

    public static final Bi PAYMENT = new Bi("phương thức thanh toán",
            "TIEN_MAT", "Tiền mặt",
            "CHUYEN_KHOAN", "Chuyển khoản",
            "THE", "Thẻ",
            "BAO_HIEM", "Bảo hiểm");

    public static final Bi STAFF_STATUS = new Bi("trạng thái nhân sự",
            "DANG_LAM", "Đang làm việc",
            "TAM_NGHI", "Nghỉ phép",
            "NGHI_VIEC", "Đã nghỉ việc");

    public static final Bi ROOM_STATUS = new Bi("trạng thái phòng khám",
            "HOAT_DONG", "Hoạt động",
            "TAM_NGUNG", "Tạm dừng",
            "BAO_TRI", "Bảo trì");

    public static final Bi LAB_TYPE_STATUS = new Bi("trạng thái loại xét nghiệm",
            "HOAT_DONG", "Hoạt động",
            "NGUNG", "Ngừng");

    public static final Bi RECORD_STATUS = new Bi("trạng thái bệnh án",
            "DANG_MO", "Đang theo dõi",
            "DONG", "Đã đóng");

    public static final Bi LOT_STATUS = new Bi("trạng thái lô thuốc",
            "HOAT_DONG", "Hoạt động",
            "DA_LOAI_BO", "Đã loại bỏ (Hết hạn)");

    private Labels() {}
}
