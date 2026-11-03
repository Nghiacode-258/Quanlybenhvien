package com.hms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.hms.Repository.Db;
import com.hms.Repository.Labels;

/** Test thuần (không cần kết nối CSDL) cho phần ánh xạ mã <-> nhãn và các hàm tiện ích. */
class LabelsTest {

    @Test
    void examStatusRoundTrip() {
        assertThat(Labels.EXAM.label("CHO_THANH_TOAN")).isEqualTo("Chờ thanh toán");
        assertThat(Labels.EXAM.code("Chờ khám", null)).isEqualTo("CHO_KHAM");
        assertThat(Labels.EXAM.code("HOAN_TAT", null)).isEqualTo("HOAN_TAT");
        assertThat(Labels.EXAM.code("", "CHO_KHAM")).isEqualTo("CHO_KHAM");
    }

    @Test
    void unknownLabelIsRejected() {
        assertThatThrownBy(() -> Labels.PAYMENT.code("Bitcoin", null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void paymentAndStaffLabelsMatchUi() {
        assertThat(Labels.PAYMENT.code("Chuyển khoản", null)).isEqualTo("CHUYEN_KHOAN");
        assertThat(Labels.STAFF_STATUS.code("Nghỉ phép", null)).isEqualTo("TAM_NGHI");
        assertThat(Labels.ROOM_STATUS.label("TAM_NGUNG")).isEqualTo("Tạm dừng");
    }

    @Test
    void dateAndTimeParsing() {
        assertThat(Db.parseDate("2026-10-28 10:15", "Ngày")).hasToString("2026-10-28");
        assertThat(Db.parseDate("  ", "Ngày")).isNull();
        assertThat(Db.parseTime("09:00", "Giờ")).hasToString("09:00");
        assertThatThrownBy(() -> Db.parseDate("28/10/2026", "Ngày")).isInstanceOf(IllegalArgumentException.class);
    }
}
