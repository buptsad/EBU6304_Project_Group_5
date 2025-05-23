package com.example.app.ui.dashboard;

import com.example.app.model.FinanceData;
import com.example.app.ui.CurrencyManager;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ChartFactory {
    
    public static JFreeChart createFinancialLineChart(FinanceData financeData) {
        // Create dataset
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        // Add income series
        TimeSeries incomeSeries = new TimeSeries("Income");
        addDataToSeries(incomeSeries, financeData.getDates(), financeData.getDailyIncomes());
        dataset.addSeries(incomeSeries);
        
        // Add expense series
        TimeSeries expenseSeries = new TimeSeries("Expenses");
        addDataToSeries(expenseSeries, financeData.getDates(), financeData.getDailyExpenses());
        dataset.addSeries(expenseSeries);
        
        // Add budget series
        TimeSeries budgetSeries = new TimeSeries("Daily Budget");
        double dailyBudget = financeData.getDailyBudget();
        for (LocalDate date : financeData.getDates()) {
            Date javaDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            budgetSeries.add(new Day(javaDate), dailyBudget);
        }
        dataset.addSeries(budgetSeries);
        
        // Create chart
        DateAxis domainAxis = new DateAxis("Date");
        NumberAxis rangeAxis = new NumberAxis("Amount (" + currencySymbol + ")");
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        
        // Income line (blue)
        renderer.setSeriesPaint(0, new Color(65, 105, 225));
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesShapesVisible(0, true);
        
        // Expense line (red)
        renderer.setSeriesPaint(1, new Color(178, 34, 34));
        renderer.setSeriesStroke(1, new BasicStroke(2.0f));
        renderer.setSeriesShapesVisible(1, true);
        
        // Budget line (green)
        renderer.setSeriesPaint(2, new Color(46, 139, 87));
        renderer.setSeriesStroke(2, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 
                                10.0f, new float[]{10.0f, 6.0f}, 0.0f));
        renderer.setSeriesShapesVisible(2, false);
        
        XYPlot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        JFreeChart chart = new JFreeChart("Daily Financial Overview", 
                JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        chart.setBackgroundPaint(Color.WHITE);
        
        return chart;
    }
    
    private static void addDataToSeries(TimeSeries series, List<LocalDate> dates, Map<LocalDate, Double> dataMap) {
        for (LocalDate date : dates) {
            Double value = dataMap.get(date);
            if (value != null) {
                Date javaDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
                series.add(new Day(javaDate), value);
            }
        }
    }
}