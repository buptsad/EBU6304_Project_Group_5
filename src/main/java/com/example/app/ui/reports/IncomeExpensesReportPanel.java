package com.example.app.ui.reports;

import com.example.app.viewmodel.reports.IncomeExpensesReportViewModel;
import com.example.app.viewmodel.reports.IncomeExpensesReportViewModel.ChartDataChangeListener;
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
import java.util.*;
import java.util.List; // Add this import to resolve ambiguity

public class IncomeExpensesReportPanel extends JPanel implements CurrencyChangeListener, ChartDataChangeListener {
    private final IncomeExpensesReportViewModel viewModel;
    private ChartPanel chartPanel;
    private String timeRange = "Last 30 days";
    private String interval = "Daily"; // Add interval support

    public IncomeExpensesReportPanel(IncomeExpensesReportViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addChangeListener(this);

        setLayout(new BorderLayout());
        JFreeChart chart = createChart();
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 500));
        chartPanel.setMouseWheelEnabled(true);

        add(chartPanel, BorderLayout.CENTER);

        CurrencyManager.getInstance().addCurrencyChangeListener(this);
    }

    private JFreeChart createChart() {
        XYDataset dataset = createDataset();
        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        String title = "Income vs. Expenses (" + timeRange + ")";
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title, "Date", "Amount (" + currencySymbol + ")", dataset, true, true, false
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);
        renderer.setSeriesPaint(0, new Color(0, 150, 0));
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesPaint(1, new Color(200, 0, 0));
        renderer.setSeriesStroke(1, new BasicStroke(2.5f));
        renderer.setSeriesPaint(2, new Color(0, 100, 180));
        renderer.setSeriesStroke(2, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{6.0f, 6.0f}, 0.0f));
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MMM d"));
        return chart;
    }

    private XYDataset createDataset() {
        TimeSeries incomeSeries = new TimeSeries("Income");
        TimeSeries expensesSeries = new TimeSeries("Expenses");
        TimeSeries netSeries = new TimeSeries("Net (Income - Expenses)");

        List<LocalDate> dates = viewModel.getDates();
        Map<LocalDate, Double> incomes = viewModel.getDailyIncomes();
        Map<LocalDate, Double> expenses = viewModel.getDailyExpenses();

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = getStartDateFromRange(timeRange);

        // Group data based on the selected interval
        Map<RegularTimePeriod, Double> groupedIncomes = new HashMap<>();
        Map<RegularTimePeriod, Double> groupedExpenses = new HashMap<>();

        // Process all dates in the range
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            Date utilDate = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            RegularTimePeriod period = getTimePeriod(utilDate);
            
            // Initialize period if not exists
            groupedIncomes.putIfAbsent(period, 0.0);
            groupedExpenses.putIfAbsent(period, 0.0);
            
            // Add actual data if available
            Double income = incomes.get(currentDate);
            Double expense = expenses.get(currentDate);
            
            if (income != null) {
                groupedIncomes.put(period, groupedIncomes.get(period) + income);
            }
            
            if (expense != null) {
                groupedExpenses.put(period, groupedExpenses.get(period) + expense);
            }
            
            currentDate = currentDate.plusDays(1);
        }

        // Add grouped data to series
        for (RegularTimePeriod period : getSortedPeriods(groupedIncomes.keySet())) {
            Double income = groupedIncomes.get(period);
            Double expense = groupedExpenses.get(period);
            
            if (income != null && income > 0) {
                incomeSeries.add(period, income);
            }
            if (expense != null && expense > 0) {
                expensesSeries.add(period, expense);
            }
            if (income != null && expense != null) {
                netSeries.add(period, income - expense);
            }
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(incomeSeries);
        dataset.addSeries(expensesSeries);
        dataset.addSeries(netSeries);

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
                Week week = new Week(date);
                // Group weeks into pairs (every 2 weeks)
                int fortnightNumber = (week.getWeek() - 1) / 2;
                return new Week(fortnightNumber * 2 + 1, week.getYear());
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

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }

    // Add interval setter
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
            case "Last 7 days": return today.minusDays(7);
            case "Last 30 days": return today.minusDays(30);
            case "Last 90 days": return today.minusDays(90);
            case "This month": return today.withDayOfMonth(1);
            case "Last month": return today.minusMonths(1).withDayOfMonth(1);
            case "This year": return today.withDayOfYear(1);
            default: return today.minusDays(30);
        }
    }

    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        refreshChart();
    }

    @Override
    public void onChartDataChanged() {
        SwingUtilities.invokeLater(this::refreshChart);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
        viewModel.removeChangeListener(this);
        viewModel.cleanup();
    }
}