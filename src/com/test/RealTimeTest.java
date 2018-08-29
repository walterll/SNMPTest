package com.test;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class RealTimeTest  implements Runnable { 
//    private static final long serialVersionUID = 1L; 
    private TimeSeries timeSeries; 

    public RealTimeTest(String chartContent, String title, String yaxisName) { 
    	 
    } 

    @SuppressWarnings("deprecation") 
    private JFreeChart createChart(String chartContent, String title, 
    		String yaxisName) { 
    	//创建时序图对象   
    	timeSeries = new TimeSeries(chartContent, Millisecond.class); 
    	TimeSeriesCollection timeseriescollection = new TimeSeriesCollection( 
    			timeSeries); 
    	JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(title, 
    			"时间(秒)", yaxisName, timeseriescollection, true, true, false); 
    	XYPlot xyplot = jfreechart.getXYPlot(); 
    	//纵坐标设定   
    	ValueAxis valueaxis = xyplot.getDomainAxis(); 
    	//自动设置数据轴数据范围   
    	valueaxis.setAutoRange(true); 
    	//数据轴固定数据范围 30s   
    	valueaxis.setFixedAutoRange(30000D); 

    	valueaxis = xyplot.getRangeAxis(); 
    	//valueaxis.setRange(0.0D,200D);   

    	return jfreechart; 
    } 

    public void run() { 
    	while (true) { 
    		try { 
    			timeSeries.add(new Millisecond(), randomNum()); 
    			Thread.sleep(300); 
    		} catch (InterruptedException e) { 
    		} 
    	} 
    } 

    private long randomNum() { 
    	System.out.println((Math.random() * 20 + 80)); 
    	return (long) (Math.random() * 20 + 80); 
    } 
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RealTimeTest test = new RealTimeTest("", "", "");
		JFreeChart chart = test.createChart("", "", "");
		ChartPanel panel = new ChartPanel(chart);
		JFrame frame = new JFrame("Test Chart");
		frame.getContentPane().add(panel, new BorderLayout().CENTER); 
		frame.pack(); 
		frame.setVisible(true); 
		(new Thread(test)).start(); 
		frame.addWindowListener(new WindowAdapter() { 
		public void windowClosing(WindowEvent windowevent) { 
		System.exit(0); 
		} 

		}); 
		
		RealTimeTest test1 = new RealTimeTest("", "", "");
		JFreeChart chart1 = test1.createChart("", "", "");
		ChartPanel panel1 = new ChartPanel(chart1);
		JFrame frame1 = new JFrame("Test Chart1");
		frame1.getContentPane().add(panel1, new BorderLayout().CENTER); 
		frame1.pack(); 
		frame1.setVisible(true); 
		(new Thread(test1)).start(); 
		frame1.addWindowListener(new WindowAdapter() { 
		public void windowClosing(WindowEvent windowevent) { 
		System.exit(0); 
		} 

		}); 
	}

}
