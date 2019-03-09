package org.cloudbus.cloudsim.examples;

import java.awt.Color;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBrokerEx;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LayeredBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A simple demonstration application showing how to create a superimposed horizontal bar chart.
 *
 * @author Arnaud Lelievre
 */
public class LayeredBarPlot extends ApplicationFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Creates a new demo instance.
     *
     * @param title  the frame title.
     */
    public LayeredBarPlot(final String title) {

        super(title);

        final CategoryDataset dataset = createDataset();
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);

    }

    // ****************************************************************************
    // * JFREECHART DEVELOPER GUIDE                                               *
    // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
    // * to purchase from Object Refinery Limited:                                *
    // *                                                                          *
    // * http://www.object-refinery.com/jfreechart/guide.html                     *
    // *                                                                          *
    // * Sales are used to provide funding for the JFreeChart project - please    * 
    // * support us so that we can continue developing free software.             *
    // ****************************************************************************
    public static CategoryDataset createVmDataset(List<DatacenterBrokerEx.VmCreatedHistory> list) {
    	double[] create = new double[list.size()];
    	double[] destory = new double[list.size()];
    	
    	for(int i =0; i < list.size() ; i++) {
    		create[i] = list.get(i).getEndTime() - list.get(i).getStartTime();
    	}
    	final double[][] data = {create, destory};
    	return DatasetUtilities.createCategoryDataset("", "Vm", data);
    }
    public static CategoryDataset createCloudletDataset(List<? extends Cloudlet> list) {
    	double[] response = new double[list.size()];
    	double[] schedule = new double[list.size()];
    	
    	for(int i =0; i < list.size() ; i++) {
    		response[i] = list.get(i).getActualCPUTime();
    		//schedule[i] =((CloudletEx) list.get(i)).getDeadline()-((CloudletEx) list.get(i)).getArrivalTime();
    	}
    	final double[][] data = {response,schedule};
    	
    	return DatasetUtilities.createCategoryDataset("", "", data);
    }
    /**
     * Returns a sample dataset.
     * 
     * @return a sample dataset.
     */
    private CategoryDataset createDataset() {

        // create a dataset...
        final double[][] data = new double[][] {
            {41.0, 33.0, 22.0, 64.0, 42.0, 62.0, 22.0, 14.0},
            {55.0, 63.0, 55.0, 48.0, 54.0, 37.0, 41.0, 39.0},
            {57.0, 75.0, 43.0, 33.0, 63.0, 46.0, 57.0, 33.0}
        };

        return DatasetUtilities.createCategoryDataset("", "Task", data);
        
    }
    
    /**
     * Creates a chart for the specified dataset.
     * 
     * @param dataset  the dataset.
     * 
     * @return a chart.
     */
    public static JFreeChart createChart(final CategoryDataset dataset) {

        final CategoryAxis categoryAxis = new CategoryAxis("Cloudlets");
        //categoryAxis.setMaxCategoryLabelWidthRatio(10.0f);
        final ValueAxis valueAxis = new NumberAxis("Time (ms)");


        final CategoryPlot plot = new CategoryPlot(dataset,
                                             categoryAxis,
                                             valueAxis,
                                             new LayeredBarRenderer());
        
        plot.setOrientation(PlotOrientation.VERTICAL);
        final JFreeChart chart = new JFreeChart("Cloudlet Layered Bar Chart",
                                          JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // set the background color for the chart...
        chart.setBackgroundPaint(Color.lightGray);

        final LayeredBarRenderer renderer = (LayeredBarRenderer) plot.getRenderer();

        // we can set each series bar width individually or let the renderer manage a standard view.
        // the width is set in percentage, where 1.0 is the maximum (100%).
        renderer.setSeriesBarWidth(0, 0.7);
        renderer.setSeriesBarWidth(1, 0.7);

        renderer.setItemMargin(0.01);
        final CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryMargin(0.25);
        domainAxis.setUpperMargin(0.05);
        domainAxis.setLowerMargin(0.05);
        
        return chart;
        
    }
    
    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    public static void main(final String[] args) {

        final LayeredBarPlot demo = new LayeredBarPlot("Layered Bar Chart Demo 1");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }

}


