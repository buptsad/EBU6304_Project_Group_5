package com.example.app.ui.reports;

import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import com.example.app.viewmodel.reports.TrendReportViewModel;
import com.example.app.viewmodel.reports.TrendReportViewModel.ChartDataChangeListener;

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
import java.util.List;

/**
 * A panel that displays financial trends over time using a time series chart.
 * This panel supports grouping by various intervals (daily, weekly, monthly, etc.),
 * and listens for currency and data changes to update the chart accordingly.
 * <p>
 * Features:
 * <ul>
 *   <li>Time series chart visualization of income, expenses, and budget</li>
 *   <li>Supports grouping by day, week, fortnight, month, quarter, or year</li>
 *   <li>Dynamic currency symbol in axis label</li>
 *   <li>Supports time range and interval selection for filtering data</li>
 *   <li>Listens to ViewModel and currency changes</li>
 * </ul>
 
 */
public class TrendReportPanel extends JPanel implements CurrencyChangeListener, ChartDataChangeListener {

    /** The ViewModel providing trend report data */
    private final TrendReportViewModel viewModel;
    /** The chart panel displaying the time series chart */
    private ChartPanel chartPanel;
    /** The current time range for the report */
    private String timeRange = "Last 30 days";
    /** The current interval for grouping data */
    private String interval = "Daily";

    /**
     * Constructs a new TrendReportPanel with the given ViewModel.
     *
     * @param viewModel the ViewModel for trend report data
     */
    public TrendReportPanel(TrendReportViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addChangeListener(this);

        setLayout(new BorderLayout());

        // Create the chart
        JFreeChart chart = createChart();
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 500));
        chartPanel.setMouseWheelEnabled(true);

        add(chartPanel, BorderLayout.CENTER);

        // Register as currency change listener
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
    }

    /**
     * Creates the time series chart for financial trends.
     *
     * @return the JFreeChart instance
     */
    private JFreeChart createChart() {
        XYDataset dataset = createDataset();

        // Get current currency symbol
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

    /**
     * Creates the dataset for the time series chart using grouped income, expenses, and budget data.
     *
     * @return the XYDataset for the chart
     */
    private XYDataset createDataset() {
        // Create time series for income, expenses, and budget
        TimeSeries incomeSeries = new TimeSeries("Income");
        TimeSeries expensesSeries = new TimeSeries("Expenses");
        TimeSeries budgetSeries = new TimeSeries("Budget");

        // Get data from the ViewModel
        List<LocalDate> dates = viewModel.getDates();
        Map<LocalDate, Double> incomes = viewModel.getDailyIncomes();
        Map<LocalDate, Double> expenses = viewModel.getDailyExpenses();

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

    /**
     * Returns a sorted list of RegularTimePeriod objects.
     *
     * @param periods the set of periods to sort
     * @return the sorted list of periods
     */
    private List<RegularTimePeriod> getSortedPeriods(Set<RegularTimePeriod> periods) {
        List<RegularTimePeriod> sortedPeriods = new ArrayList<>(periods);
        Collections.sort(sortedPeriods);
        return sortedPeriods;
    }

    /**
     * Returns the appropriate RegularTimePeriod for the given date and interval.
     *
     * @param date the date to convert
     * @return the RegularTimePeriod for the interval
     */
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

    /**
     * Calculates the budget value for the given period based on the interval.
     *
     * @param period the RegularTimePeriod
     * @return the budget value for the period
     */
    private double calculateBudgetForPeriod(RegularTimePeriod period) {
        double monthlyBudget = viewModel.getMonthlyBudget();

        if (period instanceof Day) {
            return viewModel.getDailyBudget();
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

    /**
     * Configures the date axis format based on the selected interval.
     *
     * @param axis the DateAxis to configure
     */
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

    /**
     * Sets the time range for the report.
     *
     * @param timeRange the time range to display
     */
    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }

    /**
     * Sets the interval for grouping data.
     *
     * @param interval the interval to use (e.g., "Daily", "Weekly")
     */
    public void setInterval(String interval) {
        this.interval = interval;
    }

    /**
     * Refreshes the chart with the latest data and settings.
     */
    public void refreshChart() {
        JFreeChart chart = createChart();
        chartPanel.setChart(chart);
        chartPanel.repaint();
    }

    /**
     * Returns the start date based on the selected time range.
     *
     * @param range the time range string
     * @return the start LocalDate
     */
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

    /**
     * Called when the application currency changes.
     * Refreshes the chart to update currency symbols.
     *
     * @param currencyCode the new currency code
     * @param currencySymbol the new currency symbol
     */
    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        // Refresh chart when currency changes
        refreshChart();
    }

    /**
     * Called when the chart data changes in the ViewModel.
     * Refreshes the chart.
     */
    @Override
    public void onChartDataChanged() {
        // Called by ViewModel when data changes
        SwingUtilities.invokeLater(this::refreshChart);
    }

    /**
     * Called when this panel is removed from its container.
     * Cleans up listeners and resources.
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        // Clean up when panel is removed
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
        viewModel.removeChangeListener(this);
        viewModel.cleanup();
    }
}