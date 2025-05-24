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
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A panel that displays a time series chart comparing income and expenses.
 * This panel listens for currency and data changes and updates the chart accordingly.
 * <p>
 * Features:
 * <ul>
 *   <li>Time series chart visualization of income, expenses, and net difference</li>
 *   <li>Dynamic currency symbol in axis label</li>
 *   <li>Supports time range selection for filtering data</li>
 *   <li>Listens to ViewModel and currency changes</li>
 * </ul>
 
 */
public class IncomeExpensesReportPanel extends JPanel implements CurrencyChangeListener, ChartDataChangeListener {
    /** The ViewModel providing income and expenses data */
    private final IncomeExpensesReportViewModel viewModel;
    /** The chart panel displaying the time series chart */
    private ChartPanel chartPanel;
    /** The current time range for the report */
    private String timeRange = "Last 30 days";

    /**
     * Constructs a new IncomeExpensesReportPanel with the given ViewModel.
     *
     * @param viewModel the ViewModel for income and expenses data
     */
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

    /**
     * Creates the time series chart for income and expenses.
     *
     * @return the JFreeChart instance
     */
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

    /**
     * Creates the dataset for the time series chart using income and expenses data.
     *
     * @return the XYDataset for the chart
     */
    private XYDataset createDataset() {
        TimeSeries incomeSeries = new TimeSeries("Income");
        TimeSeries expensesSeries = new TimeSeries("Expenses");
        TimeSeries netSeries = new TimeSeries("Net (Income - Expenses)");

        List<LocalDate> dates = viewModel.getDates();
        Map<LocalDate, Double> incomes = viewModel.getDailyIncomes();
        Map<LocalDate, Double> expenses = viewModel.getDailyExpenses();

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = getStartDateFromRange(timeRange);

        for (LocalDate date : dates) {
            if (date.isAfter(startDate.minusDays(1)) && date.isBefore(endDate.plusDays(1))) {
                Date utilDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Day day = new Day(utilDate);

                Double income = incomes.get(date);
                Double expense = expenses.get(date);

                if (income != null) incomeSeries.add(day, income);
                if (expense != null) expensesSeries.add(day, expense);
                if (income != null && expense != null) netSeries.add(day, income - expense);
            }
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(incomeSeries);
        dataset.addSeries(expensesSeries);
        dataset.addSeries(netSeries);

        return dataset;
    }

    /**
     * Sets the time range for the report and updates the chart.
     *
     * @param timeRange the time range to display
     */
    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
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
            case "Last 7 days": return today.minusDays(7);
            case "Last 30 days": return today.minusDays(30);
            case "Last 90 days": return today.minusDays(90);
            case "This month": return today.withDayOfMonth(1);
            case "Last month": return today.minusMonths(1).withDayOfMonth(1);
            case "This year": return today.withDayOfYear(1);
            default: return today.minusDays(30);
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
        refreshChart();
    }

    /**
     * Called when the chart data changes in the ViewModel.
     * Refreshes the chart.
     */
    @Override
    public void onChartDataChanged() {
        SwingUtilities.invokeLater(this::refreshChart);
    }

    /**
     * Called when this panel is removed from its container.
     * Cleans up listeners and resources.
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
        viewModel.removeChangeListener(this);
        viewModel.cleanup();
    }
}