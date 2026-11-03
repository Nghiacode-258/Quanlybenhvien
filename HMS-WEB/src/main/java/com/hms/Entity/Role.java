package com.hms.Entity;

public enum Role {
    MANAGER("manager", "Quản lý bệnh viện", "Quản lý", "fa-chart-line", "NV006", "Đỗ Văn Khánh", "/dashboard", "Theo dõi hoạt động tổng thể, nhân sự và báo cáo điều hành toàn viện."),
    DOCTOR("doctor", "Bác sĩ", "Bác sĩ", "fa-stethoscope", "NV001", "BS. Nguyễn Hoàng An", "/examination", "Khám bệnh, chẩn đoán ICD, chỉ định cận lâm sàng và kê đơn thuốc."),
    NURSE("nurse", "Y tá / Điều dưỡng", "Y tá", "fa-user-nurse", "NV003", "Điều dưỡng Lê Thu Trang", "/registration", "Tiếp đón bệnh nhân, đăng ký lượt khám, hỗ trợ cận lâm sàng và tra cứu hồ sơ."),
    PHARMACIST("pharmacist", "Dược sĩ", "Dược sĩ", "fa-capsules", "NV004", "DS. Phạm Quốc Huy", "/pharmacy", "Quản lý danh mục thuốc, kho lô hạn sử dụng và cấp phát đơn thuốc."),
    CASHIER("cashier", "Thu ngân", "Thu ngân", "fa-cash-register", "NV005", "Nguyễn Hải Yến", "/billing", "Tiếp nhận thanh toán viện phí, đăng ký khám và đối soát hóa đơn.");

    private final String key;
    private final String label;
    private final String shortLabel;
    private final String icon;
    private final String employeeId;
    private final String employeeName;
    private final String startUrl;
    private final String description;

    Role(String key, String label, String shortLabel, String icon, String employeeId, String employeeName, String startUrl, String description) {
        this.key = key;
        this.label = label;
        this.shortLabel = shortLabel;
        this.icon = icon;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.startUrl = startUrl;
        this.description = description;
    }

    public String getKey() { return key; }
    public String getLabel() { return label; }
    public String getShortLabel() { return shortLabel; }
    public String getIcon() { return icon; }
    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getStartUrl() { return startUrl; }
    public String getDescription() { return description; }

    public static Role fromKey(String key) {
        if (key == null) return MANAGER;
        for (Role r : values()) {
            if (r.key.equalsIgnoreCase(key.trim())) return r;
        }
        return MANAGER;
    }
}

