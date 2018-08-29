package com.snmp;

import java.util.Calendar;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class RealTimeFlow implements Runnable {
	private final long serialVersionUID = 1L;
	private final int refreshTime = 30;
	private final int timeRange = 30;
    private volatile boolean stop = false;
    private TimeSeries timeSeriesIn;
    private TimeSeries timeSeriesOut;
    private Flow lastFlow;
    private Flow lastFlowInc;
    private String ipAddr;
    private String port;
    
    public RealTimeFlow(String chartContent, String title, String yaxisName,
						List<Flow> flowInc, String ipAddr, String port, 
						Flow lastFlow, Flow lastFlowInc) { 
     	stop = false;
    	this.ipAddr = ipAddr;
    	this.port = port;
    	this.lastFlow = lastFlow;
    	this.lastFlowInc = lastFlowInc;
    }
    
    public JFreeChart createChart(String chartContent, String title, 
    		String yaxisName, List<Flow> flowInc) { 
    	//创建时序图对象   
    	timeSeriesIn = new TimeSeries("FlowIn", Minute.class); 
    	timeSeriesOut = new TimeSeries("FlowOut", Minute.class);
    	long maxIn = 0;
    	long maxOut = 0;
    	Day day = new Day(Calendar.getInstance().getTime());
    	for (Flow flow: flowInc) {
//    		if (flow.flowIn > maxIn)
//    			maxIn = flow.flowIn;
//    		if (flow.flowOut > maxOut)
//    			maxOut = flow.flowOut;
    		Hour hour = new Hour(flow.time / 60, day);
    		timeSeriesIn.add(new Minute(flow.time % 60, hour), flow.flowIn / 1024);
    		timeSeriesOut.add(new Minute(flow.time % 60, hour), flow.flowOut / 1024);
    	}
    	TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
//    	if (maxIn < maxOut) {
	    	timeseriescollection.addSeries(timeSeriesIn);
	    	timeseriescollection.addSeries(timeSeriesOut);
//    	}
//    	else {
//    		timeseriescollection.addSeries(timeSeriesOut);
//    		timeseriescollection.addSeries(timeSeriesIn);
//    	}

    	JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(title, 
    			"time", yaxisName, timeseriescollection, true, true, false); 
    	XYPlot xyplot = jfreechart.getXYPlot(); 
    	//纵坐标设定   
    	ValueAxis valueaxis = xyplot.getDomainAxis(); 
    	//自动设置数据轴数据范围   
    	valueaxis.setAutoRange(true); 
    	//数据轴固定数据范围 30s   
    	valueaxis.setFixedAutoRange(60000D * timeRange); 

    	valueaxis = xyplot.getRangeAxis(); 
    	valueaxis.setAutoRange(true);
    	//valueaxis.setRange(0.0D,200D);   

    	return jfreechart; 
    } 
    
    public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public Flow getLastFlow() {
		return lastFlow;
	}

	public void setLastFlow(Flow lastFlow) {
		this.lastFlow = lastFlow;
	}

	public Flow getLastFlowInc() {
		return lastFlowInc;
	}

	public void setLastFlowInc(Flow lastFlowInc) {
		this.lastFlowInc = lastFlowInc;
	}

    public void run() { 
    	while (!stop) { 
    		try { 
    			Thread.sleep(1000 * refreshTime);
//    			System.out.println(LogUtils.lastFlow);
//    			System.out.println(LogUtils.lastFlow.get("192.168.2.202"));
    			Flow tempFlow = LogUtils.lastFlow.get(ipAddr).get(port);
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
}
