package com.hms.Repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Sinh mã tiếp theo dạng PREFIX + số (VD BN008, PKM010, HD022) dựa trên mã lớn nhất đang có trong bảng,
 * nên tự khớp với dữ liệu đã nhập tay/seed. Tên bảng/cột/tiền tố là hằng số trong code (không nhận từ người dùng).
 * Gọi bên trong phương thức synchronized + @Transactional của service để tránh trùng mã khi tạo đồng thời.
 */
@Component
public class IdGenerator {

    private final JdbcTemplate jdbc;

    public IdGenerator(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String next(String table, String column, String prefix, int width) {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(" + column + " FROM " + (prefix.length() + 1) + ") AS INTEGER)), 0) "
                + "FROM " + table + " WHERE " + column + " ~ ?";
        Integer max = jdbc.queryForObject(sql, Integer.class, "^" + prefix + "[0-9]{1,9}$");
        int n = (max == null ? 0 : max) + 1;
        return prefix + String.format("%0" + width + "d", n);
    }
}
