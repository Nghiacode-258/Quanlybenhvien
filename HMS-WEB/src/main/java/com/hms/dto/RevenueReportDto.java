package com.hms.dto;

import java.util.ArrayList;
import java.util.List;

public class RevenueReportDto {
    private String viewType = "month"; // "month", "year", "day"
    private int year;
    private int month;
    private String date; // "yyyy-MM-dd"
    private String selectedKey = ""; // Specific day or month selected on chart

    private long totalPaid = 0L;
    private long totalDue = 0L;
    private int totalCount = 0;

    private List<String> chartLabels = new ArrayList<>();
    private List<Long> chartPaidData = new ArrayList<>();
    private List<Long> chartDueData = new ArrayList<>();
    private List<String> chartKeys = new ArrayList<>();

    public RevenueReportDto() {}

    public String getViewType() { return viewType; }
    public void setViewType(String viewType) { this.viewType = viewType; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getSelectedKey() { return selectedKey; }
    public void setSelectedKey(String selectedKey) { this.selectedKey = selectedKey; }

    public long getTotalPaid() { return totalPaid; }
    public void setTotalPaid(long totalPaid) { this.totalPaid = totalPaid; }

    public long getTotalDue() { return totalDue; }
    public void setTotalDue(long totalDue) { this.totalDue = totalDue; }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

    public List<String> getChartLabels() { return chartLabels; }
    public void setChartLabels(List<String> chartLabels) { this.chartLabels = chartLabels; }

    public List<Long> getChartPaidData() { return chartPaidData; }
    public void setChartPaidData(List<Long> chartPaidData) { this.chartPaidData = chartPaidData; }

    public List<Long> getChartDueData() { return chartDueData; }
    public void setChartDueData(List<Long> chartDueData) { this.chartDueData = chartDueData; }

    public List<String> getChartKeys() { return chartKeys; }
    public void setChartKeys(List<String> chartKeys) { this.chartKeys = chartKeys; }
}

