
package org.sakaiproject.evaluation.tool.reporting;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Helps with making the likert charts
 * 
 * @author Steven Githens
 */
public class EvalLikertChartBuilder {

    private String[] responses = new String[] {};
    private int[] values = new int[] {};
    private boolean showPercentages = false;

    @SuppressWarnings("deprecation")
    public JFreeChart makeLikertChart() {

        DefaultCategoryDataset likertDataset = new DefaultCategoryDataset();

        int responsesCount = 0;
        for (int i = 0; i < responses.length; i++) {
            likertDataset.addValue(values[i], "Responses", responses[i]);
            responsesCount += values[i];
        }

        JFreeChart chart = ChartFactory.createBarChart(null, // "Likert Chart", // Chart title
                null, // "Choices", // domain axis label
                null, // "# of Responses", // range axis label
                likertDataset, PlotOrientation.HORIZONTAL, false, // show legend
                false, // show tooltips
                false // show URLs
                );

        // Set the background colours
        chart.setBackgroundPaint(Color.white);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);

        // Configure the bar colors and display
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(244, 252, 212));
        renderer.setDrawBarOutline(true);
        renderer.setOutlinePaint(new Color(34, 35, 237));
        renderer.setOutlineStroke(new BasicStroke(0.5f));
        renderer.setBaseItemLabelsVisible(true);
        if (showPercentages) {
            renderer.setBaseItemLabelGenerator(new LikertPercentageItemLabelGenerator(responsesCount));
        } else {
            renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        }
        // Turn off the Top Value Axis
        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setVisible(false);
        // System.out.println("THE UPPER MARGIN IS: " + rangeAxis.getUpperMargin());
        rangeAxis.setUpperMargin(0.35);

        CategoryAxis domainAxis = plot.getDomainAxis();

        // Set the font for the labels
        Font labelFont = new Font("Serif", Font.PLAIN, 6);

        CategoryItemRenderer itemRenderer = plot.getRenderer();
        itemRenderer.setBaseItemLabelFont(labelFont);

        plot.setOutlinePaint(null);

        domainAxis.setLabelFont(labelFont);
        domainAxis.setTickLabelFont(labelFont);
        rangeAxis.setLabelFont(labelFont);
        rangeAxis.setTickLabelFont(labelFont);

        return chart;
    }

    public void setResponses(String[] responses) {
        this.responses = responses;
    }

    public void setValues(int[] values) {
        this.values = values;
    }

    public void setShowPercentages(boolean showPercentages) {
        this.showPercentages = showPercentages;
    }
}
