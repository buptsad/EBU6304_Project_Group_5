package com.example.app.ui.reports;

import com.example.app.model.FinanceData;
import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

public class TrendReportPanel extends JPanel implements CurrencyChangeListener {
    
    private final FinanceData financeData;
    private ChartPanel chartPanel;
    private String timeRange = "Last 30 days";
    private String interval = "Daily";
    
    public TrendReportPanel(FinanceData financeData) {
        this.financeData = financeData;
        
        setLayout(new BorderLayout());
        
        // Create the chart
        JFreeChart chart = createChart();
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 500));
        chartPanel.setMouseWheelEnabled(true);
        
        add(chartPanel, BorderLayout.CENTER);
        
        // 注册货币变化监听器
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
    }
    
    private JFreeChart createChart() {
        XYDataset dataset = createDataset();
        
        // 获取当前货币符号
        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        
        String title = "Financial Trends - " + interval + " (" + timeRange + ")";
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title,
                "Date",
                "Amount (" + currencySymbol + ")",
                dataset,
                true,
                true,
                false
        );
        
        // Customize the plot
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        
        // Customize the renderer
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);
        
        // Income line (green)
        renderer.setSeriesPaint(0, new Color(0, 150, 0));
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        
        // Expenses line (red)
        renderer.setSeriesPaint(1, new Color(200, 0, 0));
        renderer.setSeriesStroke(1, new BasicStroke(2.5f));
        
        // Budget line (blue)
        renderer.setSeriesPaint(2, new Color(0, 100, 180));
        renderer.setSeriesStroke(2, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1.0f, new float[] {6.0f, 6.0f}, 0.0f));
        
        // Customize the date axis format based on interval
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        configureAxisFormat(axis);
        
        return chart;
    }
    
    private XYDataset createDataset() {
        // Create time series for income, expenses, and budget
        TimeSeries incomeSeries = new TimeSeries("Income");
        TimeSeries expensesSeries = new TimeSeries("Expenses");
        TimeSeries budgetSeries = new TimeSeries("Budget");
        
        // Get data from the model
        List<LocalDate> dates = financeData.getDates();
        Map<LocalDate, Double> incomes = financeData.getDailyIncomes();
        Map<LocalDate, Double> expenses = financeData.getDailyExpenses();
        
        // Filter dates based on time range
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = getStartDateFromRange(timeRange);
        
        // Group data based on the selected interval
        Map<RegularTimePeriod, Double> groupedIncomes = new HashMap<>();
        Map<RegularTimePeriod, Double> groupedExpenses = new HashMap<>();
        
        for (LocalDate date : dates) {
            if (date.isAfter(startDate.minusDays(1)) && date.isBefore(endDate.plusDays(1))) {
                Date utilDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
                RegularTimePeriod period = getTimePeriod(utilDate);
                
                Double income = incomes.get(date);
                Double expense = expenses.get(date);
                
                if (income != null) {
                    groupedIncomes.put(period, groupedIncomes.getOrDefault(period, 0.0) + income);
                }
                
                if (expense != null) {
                    groupedExpenses.put(period, groupedExpenses.getOrDefault(period, 0.0) + expense);
                }
            }
        }
        
        // Add data to series
        for (RegularTimePeriod period : getSortedPeriods(groupedIncomes.keySet())) {
            if (groupedIncomes.containsKey(period)) {
                incomeSeries.add(period, groupedIncomes.get(period));
            }
        }
        
        for (RegularTimePeriod period : getSortedPeriods(groupedExpenses.keySet())) {
            if (groupedExpenses.containsKey(period)) {
                expensesSeries.add(period, groupedExpenses.get(period));
            }
            
            // Add budget line based on interval
            double budget = calculateBudgetForPeriod(period);
            budgetSeries.add(period, budget);
        }
        
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(incomeSeries);
        dataset.addSeries(expensesSeries);
        dataset.addSeries(budgetSeries);
        
        return dataset;
    }
    
    private List<RegularTimePeriod> getSortedPeriods(Set<RegularTimePeriod> periods) {
        List<RegularTimePeriod> sortedPeriods = new ArrayList<>(periods);
        Collections.sort(sortedPeriods);
        return sortedPeriods;
    }
    
    private RegularTimePeriod getTimePeriod(Date date) {
        switch (interval) {
            case "Daily":
                return new Day(date);
            case "Weekly":
                return new Week(date);
            case "Fortnightly":
                // JFreeChart doesn't have a built-in fortnight period
                Week week = new Week(date);
                return new Week((week.getWeek() + 1) / 2, week.getYear());
            case "Monthly":
                return new Month(date);
            case "Quarterly":
                return new Quarter(date);
            case "Yearly":
                return new Year(date);
            default:
                return new Day(date);
        }
    }
    
    private double calculateBudgetForPeriod(RegularTimePeriod period) {
        double monthlyBudget = financeData.getMonthlyBudget();
        
        if (period instanceof Day) {
            return financeData.getDailyBudget();
        } else if (period instanceof Week) {
            return monthlyBudget / 4.33;  // Average weeks per month
        } else if (period instanceof Month) {
            return monthlyBudget;
        } else if (period instanceof Quarter) {
            return monthlyBudget * 3;
        } else if (period instanceof Year) {
            return monthlyBudget * 12;
        } else {
            // Handle fortnightly (2 weeks) as special case
            return monthlyBudget / 2.165;  // Half of monthly
        }
    }
    
    private void configureAxisFormat(DateAxis axis) {
        SimpleDateFormat format;
        
        switch (interval) {
            case "Daily":
                format = new SimpleDateFormat("MMM d");
                break;
            case "Weekly":
            case "Fortnightly":
                format = new SimpleDateFormat("MMM d");
                break;
            case "Monthly":
                format = new SimpleDateFormat("MMM yyyy");
                break;
            case "Quarterly":
            case "Yearly":
                format = new SimpleDateFormat("yyyy");
                break;
            default:
                format = new SimpleDateFormat("MMM d");
        }
        
        axis.setDateFormatOverride(format);
    }
    
    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }
    
    public void setInterval(String interval) {
        this.interval = interval;
    }
    
    public void refreshChart() {
        JFreeChart chart = createChart();
        chartPanel.setChart(chart);
        chartPanel.repaint();
    }
    
    private LocalDate getStartDateFromRange(String range) {
        LocalDate today = LocalDate.now();
        
        switch (range) {
            case "Last 7 days":
                return today.minusDays(7);
            case "Last 30 days":
                return today.minusDays(30);
            case "Last 90 days":
                return today.minusDays(90);
            case "This month":
                return today.withDayOfMonth(1);
            case "Last month":
                return today.minusMonths(1).withDayOfMonth(1);
            case "This year":
                return today.withDayOfYear(1);
            default:
                return today.minusDays(30);
        }
    }

    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        // 货币变化时刷新图表
        refreshChart();
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        // 移除组件时取消监听
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
    }
}