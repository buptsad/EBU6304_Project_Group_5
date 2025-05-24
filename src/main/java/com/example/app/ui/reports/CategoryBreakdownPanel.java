package com.example.app.ui.reports;

import com.example.app.viewmodel.reports.CategoryBreakdownViewModel;
import com.example.app.viewmodel.reports.CategoryBreakdownViewModel.ChartDataChangeListener;
import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Map;


/**
 * A panel that displays a pie chart breakdown of expenses by category.
 * This panel listens for currency and data changes and updates the chart accordingly.
 * <p>
 * Features:
 * <ul>
 *   <li>Pie chart visualization of expenses by category</li>
 *   <li>Dynamic color assignment for each category</li>
 *   <li>Currency symbol adapts to user settings</li>
 *   <li>Supports time range selection for filtering data</li>
 *   <li>Listens to ViewModel and currency changes</li>
 * </ul>
 
 */
public class CategoryBreakdownPanel extends JPanel implements CurrencyChangeListener, ChartDataChangeListener {
    /** The ViewModel providing category breakdown data */
    private final CategoryBreakdownViewModel viewModel;
    /** The chart panel displaying the pie chart */
    private ChartPanel chartPanel;
    /** The current time range for the report */
    private String timeRange = "Last 30 days";

    /**
     * Constructs a new CategoryBreakdownPanel with the given ViewModel.
     *
     * @param viewModel the ViewModel for category breakdown data
     */
    public CategoryBreakdownPanel(CategoryBreakdownViewModel viewModel) {
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
     * Creates the pie chart for category breakdown.
     *
     * @return the JFreeChart instance
     */
    private JFreeChart createChart() {
        DefaultPieDataset dataset = createDataset();
        String title = "Expense Breakdown by Category (" + timeRange + ")";
        JFreeChart chart = ChartFactory.createPieChart(
                title, dataset, true, true, false
        );
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);

        int index = 0;
        for (String category : viewModel.getCategoryBudgets().keySet()) {
            plot.setSectionPaint(category, getColorForIndex(index));
            index++;
        }

        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        PieSectionLabelGenerator labelGenerator = new StandardPieSectionLabelGenerator(
                "{0}: " + currencySymbol + "{1} ({2})",
                new DecimalFormat("0.00"),
                new DecimalFormat("0.0%")
        );
        plot.setLabelGenerator(labelGenerator);
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);

        return chart;
    }

    /**
     * Creates the dataset for the pie chart using category expenses.
     *
     * @return the DefaultPieDataset for the chart
     */
    private DefaultPieDataset createDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        Map<String, Double> categoryExpenses = viewModel.getCategoryExpenses();
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        return dataset;
    }

    /**
     * Returns a color for the given index, cycling through a predefined palette.
     *
     * @param index the index of the category
     * @return the Color to use for the chart section
     */
    private Color getColorForIndex(int index) {
        Color[] colors = {
            new Color(65, 105, 225),  // Royal Blue
            new Color(255, 99, 71),   // Tomato
            new Color(50, 205, 50),   // Lime Green
            new Color(255, 165, 0),   // Orange
            new Color(106, 90, 205),  // Slate Blue
            new Color(220, 20, 60),   // Crimson
            new Color(0, 139, 139)    // Dark Cyan
        };
        return colors[index % colors.length];
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