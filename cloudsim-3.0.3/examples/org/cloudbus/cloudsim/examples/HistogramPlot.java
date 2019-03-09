package org.cloudbus.cloudsim.examples;
/**
 * plot the histogram for vmlist, cloudletlist
 * @author tianzhangh
 */
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletEx;
import org.cloudbus.cloudsim.DatacenterBrokerEx;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demo of the {@link HistogramDataset} class.
 */
public class HistogramPlot extends ApplicationFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7153680558431810552L;

	/**
     * Creates a new demo.
     *
     * @param title  the frame title.
     */
    public HistogramPlot(String title) {
        super(title);
        JPanel chartPanel = createDemoPanel();
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
    }

    public static IntervalXYDataset createVmDataset(List<DatacenterBrokerEx.VmCreatedHistory> list) {
    	HistogramDataset dataset = new HistogramDataset();
    	double[] execution = new double[list.size()];
    	for(int i =0; i < list.size(); i++) {
    		execution[i] = list.get(i).getEndTime() - list.get(i).getStartTime();
    	}
    	dataset.addSeries("Vm Running Time:", execution, 100, 10.0, 2000.0);
    	return dataset;
    }
    /**
     * Creates a sample {@link HistogramDataset}.
     *
     * @return the dataset.
     */
    public static IntervalXYDataset createCloudletDataset(List<? extends Cloudlet> list) {
        HistogramDataset dataset = new HistogramDataset();
        double[] exevalues = new double[list.size()];
        double[] deadlinevalues = new double[list.size()];
        for(int i =0; i < list.size(); i++) {
        	exevalues[i] = list.get(i).getActualCPUTime();
        	deadlinevalues[i] = ((CloudletEx) list.get(i)).getDeadline()-((CloudletEx) list.get(i)).getArrivalTime();
        }
        dataset.addSeries("Response Time", exevalues, 100, 10.0, 2000.0);
        
        dataset.addSeries("Scheduling Window", deadlinevalues, 100, 10.0, 2000.0);
        return dataset;
    }
    
    /**
     * Creates a sample {@link HistogramDataset}.
     *
     * @return the dataset.
     */
    private static IntervalXYDataset createDataset() {
        HistogramDataset dataset = new HistogramDataset();
        double[] values = new double[1000];
        Random generator = new Random(12345678L);
        for (int i = 0; i < 1000; i++) {
            values[i] = generator.nextGaussian() + 5;
        }
        dataset.addSeries("H1", values, 100, 2.0, 8.0);
        values = new double[1000];
        for (int i = 0; i < 1000; i++) {
            values[i] = generator.nextGaussian() + 7;
        }
        dataset.addSeries("H2", values, 100, 4.0, 10.0);
        return dataset;
    }
    /**
     * Creates a chart.
     *
     * @param dataset  a dataset.
     *
     * @return The chart.
     */
    public static JFreeChart createChart(IntervalXYDataset dataset) {
        JFreeChart chart = ChartFactory.createHistogram(
            "",
            "Time",
            "Amount of Cloudlets",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        plot.setForegroundAlpha(0.85f);
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        // flat bars look best...
        renderer.setBarPainter(new StandardXYBarPainter());
        renderer.setShadowVisible(false);
        return chart;
    }
    

    /**
     * Creates a panel for the demo (used by SuperDemo.java).
     *
     * @return A panel.
     */
    public static JPanel createDemoPanel() {
        JFreeChart chart = createChart(createDataset());
        try {
			saveToFile("demo.png",chart.createBufferedImage(1000, 500));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        ChartPanel panel = new ChartPanel(chart);
        panel.setMouseWheelEnabled(true);
        return panel;
    }
    public static void saveToFile(String name,BufferedImage img) throws FileNotFoundException, IOException{
    	File outputfile = new File(name);
    	ImageIO.write(img, "png", outputfile);
    	}

    /**
     * The starting point for the demo.
     *
     * @param args  ignored.
     *
     * @throws IOException  if there is a problem saving the file.
     */
    public static void main(String[] args) throws IOException {

        HistogramPlot demo = new HistogramPlot(
                "JFreeChart: HistogramPlot.java");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }

}
