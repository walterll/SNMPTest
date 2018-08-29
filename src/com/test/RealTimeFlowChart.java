package com.test;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.snmp.LogUtils;


public class RealTimeFlowChart extends ChartPanel implements Runnable { 
    private static final long serialVersionUID = 1L;
    private volatile static boolean stop = false;
    private static TimeSeries timeSeriesIn;
    private static TimeSeries timeSeriesOut;
    private static Flow lastFlow;
    private static Flow lastFlowInc;
    private String ipAddr;
    private String port;

    public RealTimeFlowChart(String chartContent, String title, String yaxisName, 
    						List<Flow> flowInc, String ipAddr, String port) { 
    	super(createChart(chartContent, title, yaxisName, flowInc)); 
    	stop = false;
    } 

    @SuppressWarnings("deprecation") 
    private static JFreeChart createChart(String chartContent, String title, 
    		String yaxisName, List<Flow> flowInc) { 
    	//创建时序图对象   
    	timeSeriesIn = new TimeSeries(chartContent, Minute.class); 
    	timeSeriesOut = new TimeSeries(chartContent, Minute.class);
    	Day day = new Day(Calendar.getInstance().getTime());
    	for (Flow flow: flowInc) {
    		Hour hour = new Hour(flow.time / 60, day);
    		timeSeriesIn.add(new Minute(flow.time % 60, hour), flow.flowIn / 1024);
    		timeSeriesOut.add(new Minute(flow.time % 60, hour), flow.flowOut / 1024);
    	}
    	TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
    	timeseriescollection.addSeries(timeSeriesIn);
    	timeseriescollection.addSeries(timeSeriesOut);

    	JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(title, 
    			"时间(秒)", yaxisName, timeseriescollection, true, true, false); 
    	XYPlot xyplot = jfreechart.getXYPlot(); 
    	//纵坐标设定   
    	ValueAxis valueaxis = xyplot.getDomainAxis(); 
    	//自动设置数据轴数据范围   
    	valueaxis.setAutoRange(true); 
    	//数据轴固定数据范围 30s   
    	valueaxis.setFixedAutoRange(30000D * 60); 

    	valueaxis = xyplot.getRangeAxis(); 
    	//valueaxis.setRange(0.0D,200D);   

    	return jfreechart; 
    } 

    public void run() { 
    	while (!stop) { 
    		try { 
    			Thread.sleep(60000);
//    			System.out.println(LogUtils.lastFlow);
//    			System.out.println(LogUtils.lastFlow.get("192.168.2.202"));
    			Flow tempFlow = LogUtils.lastFlow.get("192.168.2.246").get("14");
    			System.out.println(tempFlow.getTime() + " " + tempFlow.getFlowIn() + " " + tempFlow.getFlowOut());
    			if (lastFlow.getTime() != tempFlow.getTime()) {
    				lastFlowInc = new Flow(lastFlow.getTime(), 
    						(lastFlow.getFlowIn() - tempFlow.getFlowIn()) / (lastFlow.getTime() - tempFlow.getTime())
    						,(lastFlow.getFlowOut() - tempFlow.getFlowOut()) / (lastFlow.getTime() - tempFlow.getTime()));
    				lastFlow = tempFlow;
    			}
    			Day day = new Day(Calendar.getInstance().getTime());
    			Hour hour = new Hour(lastFlowInc.getTime() / 60, day);
    			timeSeriesIn.add(new Minute(lastFlowInc.getTime() % 60, hour), lastFlowInc.getFlowIn() / 1024); 
    			timeSeriesOut.add(new Minute(lastFlowInc.getTime() % 60, hour), lastFlowInc.getFlowOut() / 1024);
    			 
    		} catch (Exception e) { 
    		} 
    	} 
    } 

    private long randomNum() { 
    	System.out.println((Math.random() * 20 + 80)); 
    	return (long) (Math.random() * 20 + 80); 
    } 
    
    public static List<Flow> readLastHalfHourPortFlowData(String ipAddr, String port, String dayOfYear) {
    	String path = "flowData/"  + ipAddr + "_" + dayOfYear + "/" + port + ".txt";
   	 Reader read;
   	Calendar calendar = Calendar.getInstance();
   	int minutes=calendar.get(Calendar.MINUTE);//分 
    int hours=calendar.get(Calendar.HOUR_OF_DAY);//小时
    int index = 0;
    List<Flow> flows = new ArrayList<>();
    if (minutes + hours * 60 > 30)
    	index = hours * 60 + minutes - 30;
    else 
    	index = 0;
   	 try {
			read = new FileReader(new File(path));
			BufferedReader br = new BufferedReader(read);
			String tempString;
			int row = 0;
			while ((tempString = br.readLine()) != null) {
				String[] time1 = tempString.split(":");
				String[] time2 = time1[1].split("  ");
				int hour = Integer.valueOf(time1[0]);
				int minute = Integer.valueOf(time2[0]);
				int time = hour * 60 + minute;
				if (time < index)
					continue;
				flows.add(new Flow(time, Long.valueOf(time2[1]), Long.valueOf(time2[2])));
//					flowIn[time] = Long.valueOf(time2[1]);
//					flowOut[time] = Long.valueOf(time2[2]);
				
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   	 return flows;
    }
    public static void test() {
    	String ipAddr = "192.168.2.246";
    	String port = "14";
    	Calendar ca = Calendar.getInstance();
    	String dayOfYear = LogUtils.getDayOfYearStr(ca);
    	List<Flow> flows = readLastHalfHourPortFlowData(ipAddr, port, dayOfYear);
    	RealTimeFlowChart.lastFlow = flows.get(flows.size() - 1);
    	System.out.println("lastFlow = " + lastFlow.getTime() + " " + lastFlow.getFlowIn() + " " + lastFlow.getFlowOut());
    	int tempTime = 0;
    	long tempFlowIn = 0;
    	long tempFlowOut = 0;
    	int tempTimeInc = 0;
//    	long tempFlowInInc = 0;
//    	long tempFlowOutInc = 0;
    	List<Flow> flowsInc = new ArrayList<>();	//kb
    	for (Flow flow : flows) {
    		if (tempTime != 0) {
 
    			if (flow.time == tempTime)
    				continue;
    			tempTimeInc = flow.time - tempTime;
    			while (flow.time > tempTime) {
    				flowsInc.add(new Flow(tempTime + 1, (flow.flowIn - tempFlowIn) / (tempTimeInc),
    						(flow.flowOut - tempFlowOut) / (tempTimeInc)));
    				tempTime ++;
    			}
    		} else { 
    		}
    		tempTime = flow.time;
    		tempFlowIn = flow.flowIn;
    		tempFlowOut = flow.flowOut;
    	}
    	RealTimeFlowChart.lastFlowInc = flowsInc.get(flowsInc.size() - 1);
    	System.out.println("lastFlowInc = " + lastFlowInc.getTime() + " " + lastFlowInc.getFlowIn() + " " + lastFlowInc.getFlowOut());
//    	for (Flow flowInc : flowsInc) {
//    		System.out.println(flowInc.time / 60 + ":" + flowInc.time % 60 + " " + flowInc.flowIn + " " + flowInc.flowOut);
//    	}
    	
    	final JFrame frame = new JFrame("Test Chart"); 
		RealTimeFlowChart rtcp = new RealTimeFlowChart("Random Data", "随机数", "数值", flowsInc); 
		frame.getContentPane().add(rtcp, new BorderLayout().CENTER); 
		frame.pack(); 
		frame.setVisible(true); 
//		Thread flowChartThread = new Thread(rtcp);
//		flowChartThread.start();
		(new Thread(rtcp)).start(); 
		frame.addWindowListener(new WindowAdapter() { 
		public void windowClosing(WindowEvent windowevent) { 
//		System.exit(0); 
			stop = true;
			frame.dispose();
		} 

		}); 
//		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	
    }
    /****************
     * 运行这个main测试无法从LogUtils.lastFlow获得数据
     * 因为此进程和HomeWindow2的进程使用各自不同的方法区，无法取到对方的静态变量
     * @param args
     */
    public static void main(String[] args) {
    	System.out.println(LogUtils.lastFlow);
    	String ipAddr = "192.168.2.202";
    	String port = "11";
    	Calendar ca = Calendar.getInstance();
    	String dayOfYear = LogUtils.getDayOfYearStr(ca);
    	List<Flow> flows = readLastHalfHourPortFlowData(ipAddr, port, dayOfYear);
    	RealTimeFlowChart.lastFlow = flows.get(flows.size() - 1);
    	System.out.println("lastFlow = " + lastFlow.getTime() + " " + lastFlow.getFlowIn() + " " + lastFlow.getFlowOut());
    	int tempTime = 0;
    	long tempFlowIn = 0;
    	long tempFlowOut = 0;
    	int tempTimeInc = 0;
//    	long tempFlowInInc = 0;
//    	long tempFlowOutInc = 0;
    	List<Flow> flowsInc = new ArrayList<>();
    	for (Flow flow : flows) {
    		if (tempTime != 0) {
 
    			if (flow.time == tempTime)
    				continue;
    			tempTimeInc = flow.time - tempTime;
    			while (flow.time > tempTime) {
    				flowsInc.add(new Flow(tempTime + 1, (flow.flowIn - tempFlowIn) / (tempTimeInc),
    						(flow.flowOut - tempFlowOut) / (tempTimeInc)));
    				tempTime ++;
    			}
    		}
    		tempTime = flow.time;
    		tempFlowIn = flow.flowIn;
    		tempFlowOut = flow.flowOut;
    	}
    	RealTimeFlowChart.lastFlowInc = flowsInc.get(flowsInc.size() - 1);
    	System.out.println("lastFlowInc = " + lastFlowInc.getTime() + " " + lastFlowInc.getFlowIn() + " " + lastFlowInc.getFlowOut());
//    	for (Flow flowInc : flowsInc) {
//    		System.out.println(flowInc.time / 60 + ":" + flowInc.time % 60 + " " + flowInc.flowIn + " " + flowInc.flowOut);
//    	}
    	
    	JFrame frame = new JFrame("Test Chart"); 
		RealTimeFlowChart rtcp = new RealTimeFlowChart("Random Data", "随机数", "数值", flowsInc); 
		frame.getContentPane().add(rtcp, new BorderLayout().CENTER); 
		frame.pack(); 
		frame.setVisible(true); 
		(new Thread(rtcp)).start(); 
		frame.addWindowListener(new WindowAdapter() { 
		public void windowClosing(WindowEvent windowevent) { 
		System.exit(0); 
		} 

		}); 
		 
	}
} 
