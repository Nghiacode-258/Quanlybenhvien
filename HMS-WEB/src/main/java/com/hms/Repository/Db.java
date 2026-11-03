package com.hms.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;

/** Các hàm tiện ích dùng chung cho tầng truy cập CSDL (Supabase / PostgreSQL). */
public final class Db {

    private Db() {}

    public static String nz(String s) {
        return s == null ? "" : s;
    }

    /** Trim; chuỗi rỗng -> null (để lưu NULL thay vì chuỗi rỗng). */
    public static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public static boolean blank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** Parse ngày dạng yyyy-MM-dd (cho phép có phần giờ phía sau). Rỗng -> null. */
    public static LocalDate parseDate(String s, String fieldLabel) {
        if (blank(s)) return null;
        String t = s.trim();
        if (t.length() > 10) t = t.substring(0, 10);
        try {
            return LocalDate.parse(t);
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldLabel + " không hợp lệ (định dạng yyyy-MM-dd): " + s);
        }
    }

    /** Parse giờ dạng HH:mm (cho phép "yyyy-MM-dd HH:mm" hoặc "yyyy-MM-ddTHH:mm"). Rỗng -> null. */
    public static LocalTime parseTime(String s, String fieldLabel) {
        if (blank(s)) return null;
        String t = s.trim();
        int idx = Math.max(t.indexOf(' '), t.indexOf('T'));
        if (idx >= 0) t = t.substring(idx + 1);
        try {
            return LocalTime.parse(t);
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldLabel + " không hợp lệ (định dạng HH:mm): " + s);
        }
    }

    public static Date sqlDate(LocalDate d) {
        return d == null ? null : Date.valueOf(d);
    }

    public static Timestamp sqlTs(LocalDateTime t) {
        return t == null ? null : Timestamp.valueOf(t);
    }

    /** DECIMAL -> Long (null -> null). */
    public static Long longOrNull(ResultSet rs, String col) throws SQLException {
        BigDecimal bd = rs.getBigDecimal(col);
        return bd == null ? null : bd.longValue();
    }

    /** DECIMAL -> long (null -> 0). */
    public static long longOrZero(ResultSet rs, String col) throws SQLException {
        Long v = longOrNull(rs, col);
        return v == null ? 0L : v;
    }

    /** Chuỗi "?,?,?" cho mệnh đề IN. */
    public static String placeholders(int n) {
        if (n <= 0) throw new IllegalArgumentException("placeholders cần n >= 1");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(',');
            sb.append('?');
        }
        return sb.toString();
    }

    public static <T> Collection<T> nullSafe(Collection<T> c) {
        return c == null ? Collections.<T>emptyList() : c;
    }
}
