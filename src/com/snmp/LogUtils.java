package com.snmp;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;


public class LogUtils {
	public static Map<String,Map<String, Flow>> lastFlow = new HashMap<>();
	/**********************
	 * 存储一次查询得到的交换机流量信息
	 * @param ipAddr
	 * @param flowData
	 * @throws IOException
	 */
	public static void writeFlowData(String ipAddr, Map<String, Long> flowData) throws IOException {
		Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR);//获取年份
        int month=ca.get(Calendar.MONTH) + 1;//获取月份  
        int day=ca.get(Calendar.DATE);//获取日
        int minute=ca.get(Calendar.MINUTE);//分 
        int hour=ca.get(Calendar.HOUR_OF_DAY);//小时
        int second=ca.get(Calendar.SECOND);//秒
        int WeekOfYear = ca.get(Calendar.DAY_OF_WEEK); 
        String dayOfYear = year + "_" + month + "_" + day;
        String minuteOfDay = hour + ":" + minute;
        
        String dir = "flowData";
        judeDirExists(new File(dir));
        String filePath = "flowData/"  + ipAddr + "_" + dayOfYear;
		judeDirExists(new File(filePath));
		
		//遍历端口
		for (String Oid : flowData.keySet()) {
			String file = filePath + "/" + Oid.substring(21) + ".txt";
			judeFileExists(new File(file));
			
			Writer fw;
	        try {
	        	fw = new FileWriter(file, true);
	        	BufferedWriter bw = new BufferedWriter (fw);  
	        	if (Oid.substring(18, 20).equals("10")) {
	        		fw.write(minuteOfDay);
	        		fw.write("  ");
	        		fw.write(flowData.get(Oid).toString());
	        		fw.write("  ");
	        		fw.write(flowData.get(Oid.substring(0, 18) + "16" + Oid.substring(20)).toString());
	        		fw.write("\r\n");
	        	}
	        	fw.flush();
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		
		
	}
	
	
	/**********************
	 * 存储一次查询得到的交换机流量信息
	 * 此方法用于存储64位的流量信息
	 * @param ipAddr
	 * @param flowData
	 * @throws IOException
	 */
		public static void writeFlowDataX(String ipAddr, Map<String, Long> flowData) throws IOException {
			Calendar ca = Calendar.getInstance();
	        int year = ca.get(Calendar.YEAR);//获取年份
	        int month=ca.get(Calendar.MONTH) + 1;//获取月份  ////////////////月份最小为0,所以使用时要加1
	        int day=ca.get(Calendar.DATE);//获取日
	        int minute=ca.get(Calendar.MINUTE);//分 
//	        int hour=ca.get(Calendar.HOUR);//小时 
	        int hour=ca.get(Calendar.HOUR_OF_DAY);	//////////////////24小时制
	        int second=ca.get(Calendar.SECOND);//秒
	        int WeekOfYear = ca.get(Calendar.DAY_OF_WEEK); 
	        String dayOfYear = year + "_" + month + "_" + day;
	        String minuteOfDay = hour + ":" + minute;
	        
	        String filePath = "flowData/"  + ipAddr + "_" + dayOfYear;
			judeDirExists(new File(filePath));
			
			
			for (String Oid : flowData.keySet()) {
//				System.out.println(Oid.substring(21));	//OID最后的端口编号
//				if (Oid.substring(21, 22).equals("6"))
//					System.out.println(Oid.substring(0, 21) + "10" + Oid.substring(22));
//				PrintWriter fw;
				Writer fw = null;
		        try {
//		        	fw = new PrintWriter(file);  	//会覆盖文件内容
		        	if (Oid.substring(21, 22).equals("6")) {
		        		String file = filePath + "/" + Oid.substring(23) + ".txt";
		        		judeFileExists(new File(file));
		        		fw = new FileWriter(file, true);
		        		BufferedWriter bw = new BufferedWriter (fw);  
		        		fw.write(minuteOfDay);
		        		fw.write("  ");
		        		fw.write(flowData.get(Oid).toString());
		        		fw.write("  ");
		        		fw.write(flowData.get(Oid.substring(0, 21) + "10" + Oid.substring(22)).toString());
		        		fw.write("\r\n");
		        		fw.flush();
		        		fw.close();
		        		
		        		LogUtils.lastFlow.get(ipAddr).put(Oid.substring(23), 
		        				new Flow(hour * 60 + minute, Long.valueOf(flowData.get(Oid).toString()),
		        						Long.valueOf(flowData.get(Oid.substring(0, 21) + "10" + Oid.substring(22)).toString())));
		        	}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
	
	/*****************
	 * 判断文件是否存在，不存在则创建
	 * @param file
	 */
     public static void judeFileExists(File file) {
 
         if (file.exists()) {
//             System.out.println("file exists");
         } else {
             System.out.println("file not exists, create it ...");
             try {
                 file.createNewFile();
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         } 
     }
 
     /*****************
      * 判断文件夹是否存在，不存在则创建
      * @param file
      */
     public static void judeDirExists(File file) {
 
         if (file.exists()) {
             if (file.isDirectory()) {
//                 System.out.println("dir exists");
             } else {
                 System.out.println("the same name file exists, can not create dir");
             }
         } else {
             System.out.println("dir not exists, create it ...");
             file.mkdir();
         } 
     }
     
     /**************
      * 读取存储的端口流量信息
      * @param ipAddr
      * @param port
      * @param dayOfYear
      * @param flowIn
      * @param flowOut
      */
     public static void readPortFlowData(String ipAddr, String port, String dayOfYear,
    		 							long[] flowIn, long[] flowOut) {
    	 String path = "flowData/"  + ipAddr + "_" + dayOfYear + "/" + port + ".txt";
    	 Reader read;
    	 try {
			read = new FileReader(new File(path));
			BufferedReader br = new BufferedReader(read);
			String tempString;
			while ((tempString = br.readLine()) != null) {
				String[] time1 = tempString.split(":");
				String[] time2 = time1[1].split("  ");
				int hour = Integer.valueOf(time1[0]);
				int minute = Integer.valueOf(time2[0]);
				int time = hour * 60 + minute;
				flowIn[time] = Long.valueOf(time2[1]);
				flowOut[time] = Long.valueOf(time2[2]);
			}			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     }
     
     /**********************
      * 读取端口最近半小时的流量数据
      * @param ipAddr
      * @param port
      * @param dayOfYear
      * @return
      */
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
// 					flowIn[time] = Long.valueOf(time2[1]);
// 					flowOut[time] = Long.valueOf(time2[2]); 				
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
     
     /******************
      * 根据小时流量数据生成图形数据
      * @param flowIn
      * @param flowOut
      * @return
      */
     public static CategoryDataset createHourDataset(double[] flowIn, double[] flowOut) {
    	String[] rowKeys = {"FlowIn","FlowOut"};
    	String[] colKeys = new String[24];
  		for (int i = 0; i < 24; i++) 
  			colKeys[i] = i + ":00";  		
    	 
    	double[][] data = new double[2][24];
    	data[0] = flowIn;
    	data[1] = flowOut;
    	return DatasetUtilities.createCategoryDataset(rowKeys, colKeys, data);
     }
     
     /*********************
      * 根据某一个小时的分钟流量数据生成图形数据
      * @param flowIn
      * @param flowOut
      * @return
      */
     public static CategoryDataset createMinuteInOneHourDataset(double[] flowIn, double[] flowOut) {
    	String[] rowKeys = {"FlowIn","FlowOut"};
    	String[] colKeys = new String[59];
  		for (int i = 0; i < 59; i++) 
  			colKeys[i] = String.valueOf(i + 1);  		
    	 
    	double[][] data = new double[2][59];
    	data[0] = flowIn;
    	data[1] = flowOut;
    	return DatasetUtilities.createCategoryDataset(rowKeys, colKeys, data);
     }
     
     /********************
      * 根据分钟流量数据生成图形数据
      * @param flowIn
      * @param flowOut
      * @return
      */
     public static CategoryDataset createDataset(double[] flowIn, double[] flowOut) {
    	String[] rowKeys = {"FlowIn","FlowOut"};
    	String[] colKeys = new String[1440];
  		for (int i = 0; i < 1440; i++)
  			colKeys[i] = i / 60 + ":" + i % 60;
    	 
    	double[][] data = new double[2][1440];
    	data[0] = flowIn;
    	data[1] = flowOut;
    	return DatasetUtilities.createCategoryDataset(rowKeys, colKeys, data);
     }
     /**
 	 * 创建CategoryDataset对象
 	 * 测试用
 	 */
 	public static CategoryDataset createDataset() {
 		String[] rowKeys = {"A平台"};
 		String[] colKeys = {"0:00", "1:00", "2:00", "7:00", "8:00", "9:00",
  				"10:00", "11:00", "12:00", "13:00", "16:00", "20:00", "21:00",
  				"23:00"};
 		double[][] data = {{4, 3, 1, 1, 1, 1, 2, 2, 2, 1, 8, 2, 1, 1},};
 		// 或者使用类似以下代码
 		// DefaultCategoryDataset categoryDataset = new
 		// DefaultCategoryDataset();
 		// categoryDataset.addValue(10, "rowKey", "colKey");
 		return DatasetUtilities.createCategoryDataset(rowKeys, colKeys, data);
 	}
 // 根据CategoryDataset创建JFreeChart对象
 	public static JFreeChart createChart(CategoryDataset categoryDataset) {
 		// 创建JFreeChart对象：ChartFactory.createLineChart
 		JFreeChart jfreechart = ChartFactory.createLineChart("不同类别按小时计算拆线图", // 标题
 				"年分", // categoryAxisLabel （category轴，横轴，X轴标签）
 				"数量", // valueAxisLabel（value轴，纵轴，Y轴的标签）
 				categoryDataset, // dataset
 				PlotOrientation.VERTICAL, true, // legend
 				false, // tooltips
 				false); // URLs
 		// 使用CategoryPlot设置各种参数。以下设置可以省略。
 		CategoryPlot plot = (CategoryPlot)jfreechart.getPlot();
 		// 背景色 透明度
 		plot.setBackgroundAlpha(0.5f);
 		// 前景色 透明度
 		plot.setForegroundAlpha(0.5f);
 		// 其他设置 参考 CategoryPlot类
 		LineAndShapeRenderer renderer = (LineAndShapeRenderer)plot.getRenderer();
 		renderer.setBaseShapesVisible(true); // series 点（即数据点）可见
 		renderer.setBaseLinesVisible(true); // series 点（即数据点）间有连线可见
 		renderer.setUseSeriesOffset(true); // 设置偏移量
 		renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
 		renderer.setBaseItemLabelsVisible(true);
 		return jfreechart;
 	}
 	
 // 根据CategoryDataset创建JFreeChart对象
  	public static JFreeChart createChart(CategoryDataset categoryDataset, String title,
  											String xLabel, String yLabel) {
  		// 创建JFreeChart对象：ChartFactory.createLineChart
  		JFreeChart jfreechart = ChartFactory.createLineChart(title, // 标题
  				xLabel, // categoryAxisLabel （category轴，横轴，X轴标签）
  				yLabel, // valueAxisLabel（value轴，纵轴，Y轴的标签）
  				categoryDataset, // dataset
  				PlotOrientation.VERTICAL, true, // legend
  				false, // tooltips
  				false); // URLs
  		// 使用CategoryPlot设置各种参数。以下设置可以省略。
  		CategoryPlot plot = (CategoryPlot)jfreechart.getPlot();
  		// 背景色 透明度
  		plot.setBackgroundAlpha(0.5f);
  		// 前景色 透明度
  		plot.setForegroundAlpha(0.5f);
  		// 其他设置 参考 CategoryPlot类
  		LineAndShapeRenderer renderer = (LineAndShapeRenderer)plot.getRenderer();
  		renderer.setBaseShapesVisible(true); // series 点（即数据点）可见
  		renderer.setBaseLinesVisible(true); // series 点（即数据点）间有连线可见
  		renderer.setUseSeriesOffset(true); // 设置偏移量
  		renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
  		renderer.setBaseItemLabelsVisible(true);
  		return jfreechart;
  	}
  	/***************
  	 * 使用时间序列表生成一天的流量图
  	 * @param flowIn	每小时输入流量增量
  	 * @param flowOut	每小时输出流量增量
  	 * @param title
  	 * @param xLabel
  	 * @param yLabel
  	 * @return
  	 */
  	public static JFreeChart createChartX(double[] flowIn, double[] flowOut, String title,
				String xLabel, String yLabel) {
  		TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
  		TimeSeries timeSeriesIn = new TimeSeries("FlowIn", Hour.class);
  		TimeSeries timeSeriesOut = new TimeSeries("FlowOut", Hour.class);
  		Day day = new Day(Calendar.getInstance().getTime());
  		for (int i = 0; i < 24; i++) {
  			timeSeriesIn.add(new Hour(i + 1, day), flowIn[i]);
  			timeSeriesOut.add(new Hour(i + 1, day), flowOut[i]);
  		}
  		timeseriescollection.addSeries(timeSeriesIn);
  		timeseriescollection.addSeries(timeSeriesOut);
  		return ChartFactory.createTimeSeriesChart(title, 
  				xLabel, yLabel, timeseriescollection, true, true, false);
  	}
 	
 	/********************
 	 * 获得某一小时的分钟流量
 	 * @param hour
 	 * @param flowInPerMin
 	 * @param flowOutPerMin
 	 * @param flowIn
 	 * @param flowOut
 	 */
 	public static void getHourFlowPerMin(int hour, double[] flowInPerMin, double[] flowOutPerMin,
 										long[] flowIn, long[] flowOut) {
// 		flowInPerMin = new double[59];
// 		flowOutPerMin = new double[59];
 		for (int i = 1; i < 60; i++) {
 			int minute = hour * 60 + i;
 			if (flowIn[minute - 1] != 0 && flowIn[minute] != 0)
 				flowInPerMin[i - 1] = (flowIn[minute] - flowIn[minute - 1]) / 1024;
 			if (flowOut[minute - 1] != 0 && flowOut[minute] != 0)
 				flowOutPerMin[i - 1] = (flowOut[minute] - flowOut[minute - 1]) / 1024;
 		}
 	}
 	
 	/**************
 	 * 获得小时流量数据
 	 * @param flowInPerHour
 	 * @param flowOutPerHour
 	 * @param flowIn
 	 * @param flowOut
 	 */
 	public static void getFlowPerHour(double[] flowInPerHour, double[] flowOutPerHour,
				long[] flowIn, long[] flowOut) {
 		for (int i = 0; i < 24; i++) {
	   		 double startFlowIn = 0, startFlowOut = 0, endFlowIn = 0, endFlowOut = 0;
	   		 for (int j = 0; j < 60; j++) {
	   			 if (flowIn[i * 60 + j] != 0) {
	   				 startFlowIn = flowIn[i * 60 + j];
	   				 break;
	   			 }
	   		 }
	   		 for (int j = 0; j < 60; j++) {
	   			 if (flowOut[i * 60 + j] != 0) {
	   				 startFlowOut = flowOut[i * 60 + j];
	   				 break;
	   			 }
	   		 }
	   		 for (int j = 59; j >= 0; j--) {
	   			 if (flowIn[i * 60 + j] != 0) {
	   				 endFlowIn = flowIn[i * 60 + j];
	   				 break;
	   			 }
	   		 }
	   		 for (int j = 59; j >= 0; j--) {
	   			 if (flowOut[i * 60 + j] != 0) {
	   				 endFlowOut = flowOut[i * 60 + j];
	   				 break;
	   			 }
	   		 }
	   		 flowInPerHour[i] = (endFlowIn - startFlowIn) / 1024;
	   		 flowOutPerHour[i] = (endFlowOut - startFlowOut) / 1024;
 		}
 	}
 	
 	/*****************
 	 * 根据Calendar对象生成日期字符串
 	 * @param ca
 	 * @return
 	 */
 	public static String getDayOfYearStr(Calendar ca) {
 		int year = ca.get(Calendar.YEAR);//获取年份
        int month=ca.get(Calendar.MONTH) + 1;//获取月份  
        int day=ca.get(Calendar.DATE);//获取日
        int minute=ca.get(Calendar.MINUTE);//分 
        int hour=ca.get(Calendar.HOUR_OF_DAY);//小时
        int second=ca.get(Calendar.SECOND);//秒
        int WeekOfYear = ca.get(Calendar.DAY_OF_WEEK); 
        String dayOfYear = year + "_" + month + "_" + day;
        return dayOfYear;
 	}

     
     public static void main(String[] args) {
//    	 Map<String, Long> flowTest = new HashMap<>();
//    	 flowTest.put("1.3.6.1.2.1.2.2.1.16.9", 953341209L);
//    	 flowTest.put("1.3.6.1.2.1.2.2.1.10.4", 23341209L);
//    	 flowTest.put("1.3.6.1.2.1.2.2.1.16.5", 41209L);
//    	 flowTest.put("1.3.6.1.2.1.2.2.1.10.9", 0L);
//    	 flowTest.put("1.3.6.1.2.1.2.2.1.16.4", 341209L);
//    	 flowTest.put("1.3.6.1.2.1.2.2.1.10.5", 23341209L);
//    	 flowTest.put("1.3.6.1.2.1.2.2.1.16.19", 209L);
//    	 flowTest.put("1.3.6.1.2.1.2.2.1.10.19", 341209L);
//    	 
//    	 
//		try {
//			writeFlowData("192.168.2.201", flowTest);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    	// 步骤1：创建CategoryDataset对象（准备数据）
// 		CategoryDataset dataset = createDataset();
// 		// 步骤2：根据Dataset 生成JFreeChart对象，以及做相应的设置
// 		JFreeChart freeChart = createChart(dataset);
// 		
// 		ChartFrame cf = new ChartFrame("Test", freeChart);  
//        cf.pack();  
//        cf.setVisible(true);
    	 long[] flowIn = new long[1440];
    	 long[] flowOut = new long[1440];
    	 readPortFlowData("192.168.2.248", "3074", "2018_8_9", flowIn, flowOut);
    	 for (int i = 0; i < 1440; i++)
    		 System.out.println(i / 60 + ":" + i % 60 + " " + flowIn[i]);
    	 /********************
    	  * 测试画出一天每分钟数据的图
    	 double[] flowInPerMin = new double[1440];
    	 double[] flowOutPerMin = new double[1440];
    	 flowInPerMin[0] = 0;
    	 flowOutPerMin[0] = 0;
    	 for (int i = 1; i < 1440; i++) {
    		 if (flowIn[i] != 0 && flowIn[i - 1] != 0) {
	    		 flowInPerMin[i] = (flowIn[i] - flowIn[i - 1]) / 1024;
	    		 flowOutPerMin[i] = (flowOut[i] - flowOut[i - 1]) / 1024;
    		 }
    	 }
    	 ******************/
    	 /*******************
    	  * 测试画出一天中每小时的流量
    	  
    	 double[] flowInPerHour = new double[24];
    	 double[] flowOutPerHour = new double[24];
    	 for (int i = 0; i < 24; i++) {
    		 double startFlowIn = 0, startFlowOut = 0, endFlowIn = 0, endFlowOut = 0;
    		 for (int j = 0; j < 60; j++) {
    			 if (flowIn[i * 60 + j] != 0) {
    				 startFlowIn = flowIn[i * 60 + j];
    				 break;
    			 }
    		 }
    		 for (int j = 0; j < 60; j++) {
    			 if (flowOut[i * 60 + j] != 0) {
    				 startFlowOut = flowOut[i * 60 + j];
    				 break;
    			 }
    		 }
    		 for (int j = 59; j > 0; j--) {
    			 if (flowIn[i * 60 + j] != 0) {
    				 endFlowIn = flowIn[i * 60 + j];
    				 break;
    			 }
    		 }
    		 for (int j = 59; j > 0; j--) {
    			 if (flowOut[i * 60 + j] != 0) {
    				 endFlowOut = flowOut[i * 60 + j];
    				 break;
    			 }
    		 }
    		 flowInPerHour[i] = (endFlowIn - startFlowIn) / 1024;
    		 flowOutPerHour[i] = (endFlowOut - startFlowOut) / 1024;
    	 }
    	 ********************/
//    	 for (int i = 0; i < 1440; i++)
//    		 System.out.println(i / 60 + ":" + i % 60 + " " + flowInPerMin[i]);
//    	 for (int i = 0; i < 24; i++)
//    		 System.out.println(i + ":00 :" + flowInPerHour[i]);
    	 
    	 double[] flowInPerMin = new double[59], flowOutPerMin = new double[59];
    	 getHourFlowPerMin(17, flowInPerMin, flowOutPerMin, flowIn, flowOut);
//    	 for (int i = 0; i < flowInPerMin.length; i++)
//    		 System.out.println(i + 1 + ": " + flowInPerMin[i]);
    	 double[] flowInPerHour = new double[24], flowOutPerHour = new double[24];
    	 getFlowPerHour(flowInPerHour, flowOutPerHour, flowIn, flowOut);
    	 
//    	// 步骤1：创建CategoryDataset对象（准备数据）
////  		CategoryDataset dataset = createMinuteInOneHourDataset(flowInPerMin, flowOutPerMin);
//  		CategoryDataset dataset = createHourDataset(flowInPerHour, flowOutPerHour);
//  		// 步骤2：根据Dataset 生成JFreeChart对象，以及做相应的设置
//  		JFreeChart freeChart = createChart(dataset);
//  		
//  		ChartFrame cf = new ChartFrame("Test", freeChart);  
//         cf.pack();  
//         cf.setVisible(true);
	}
}
