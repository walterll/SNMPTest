package com.snmp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.XYDataset;



/***********************
 * 添加交换机后直接自动开始查询流量并记录到日志
 * 流量界面屏蔽
 * @author Walterll
 *
 */
public class HomeWindow2 extends JFrame {
	private Map<String, String> switches;
	private Calendar ca;

	private JPanel contentPane;
	private SnmpUtils snmpUtils;
	
	private JButton addSwitchButton;
	private JButton manageSwitchButton;
	private final Action addSwitchAction = new AddSwitchAction();
	private final Action manageSwitchAction = new ManageSwitchAction();

	private JFrame addSwitchWindow;
	private JLabel ipLabel;
	private JTextField ipAddrText;
	private JLabel communityLabel;
	private JTextField communityText;
	private JButton searchButton;
	private JFrame switchWindow;
	private final Action searchSwitchAction = new SearchSwitchAction();
	
	private Action watchSwitchPortAction;
	
	private JFrame switchPortWindow;
	
	private Action watchPortFlowChartAction;
	
	private JFrame flowChartWindow;
	private Action changeFlowChartByDayAction;
	
	private Action watchPortRealTimeFlowAction;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					HomeWindow2 frame = new HomeWindow2();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the frame.
	 * 主界面
	 */
	public HomeWindow2() {
		super("交换机管理");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 380, 200);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
//		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		addSwitchButton = new JButton("AddSwitch");
		addSwitchButton.setAction(addSwitchAction);			
		addSwitchButton.setText("添加交换机");
		addSwitchButton.setBounds(50, 50, 120, 60);
		contentPane.add(addSwitchButton);
		addSwitchButton.setEnabled(true);			
		
		manageSwitchButton = new JButton("SwitchWindow");
		manageSwitchButton.setAction(manageSwitchAction);
		manageSwitchButton.setText("查看交换机");
		manageSwitchButton.setBounds(200, 50, 120, 60);
		contentPane.add(manageSwitchButton);
		manageSwitchButton.setEnabled(true);		
		
		switches = new HashMap<>();
	}
	
	/**
	 * 点击添加交换机按钮时执行
	 */
	private class AddSwitchAction extends AbstractAction {
		public AddSwitchAction() {
			
		}
		public void actionPerformed(ActionEvent e) {
			addSwitchWindow = new AddSwitchWindow();
			addSwitchWindow.setVisible(true);
		}
	}
	
	/**
	 * 点击管理交换机按钮时执行
	 */
	private class ManageSwitchAction extends AbstractAction {
		public ManageSwitchAction() {
			
		}
		public void actionPerformed(ActionEvent e) {
			switchWindow = new SwitchWindow();
			switchWindow.setVisible(true);
		}
	}
	
	/**************
	 * 添加交换机窗口
	 * @author Walterll
	 *
	 */
	class AddSwitchWindow extends JFrame{
		AddSwitchWindow() {
			super("添加交换机");
			setBounds(200, 200, 300, 150);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(contentPane);
			contentPane.setLayout(null);
			
			ipLabel = new JLabel("IP地址:");
			ipLabel.setBounds(60, 20, 54, 15);
			contentPane.add(ipLabel);
			
			ipAddrText = new JTextField();
			ipAddrText.setBounds(125, 20, 100, 20);
			contentPane.add(ipAddrText);
			ipAddrText.setColumns(15);
					
			communityLabel = new JLabel("Community:");
			communityLabel.setBounds(40, 50, 84, 15);
			contentPane.add(communityLabel);
			
			communityText = new JTextField();
			communityText.setBounds(125,50,100,20);
			contentPane.add(communityText);
			communityText.setColumns(20);
			
			searchButton = new JButton("Search");
			searchButton.setAction(searchSwitchAction);
			searchButton.setText("搜索");
			searchButton.setBounds(100, 80, 70, 20);
			contentPane.add(searchButton);
			searchButton.setEnabled(true);			
			
			
		}
	}
	
	/*****************
	 * 搜索交换机
	 * @author Walterll
	 *
	 */
	private class SearchSwitchAction extends AbstractAction {
		public SearchSwitchAction() {
			
		}
		public void actionPerformed(ActionEvent e) {
			String ipAddr = ipAddrText.getText();
			String community = communityText.getText();
			String switchInfo = null;
			int m = 1; //是否添加交换机
			try {
				switchInfo = SnmpUtils.searchSwitch(ipAddr, community);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (switchInfo == null) {
				JOptionPane.showMessageDialog(null, "未发现交换机");
			} else {
				Object[] options ={ "添加交换机", "取消" };  //自定义按钮上的文字
				m = JOptionPane.showOptionDialog(null, switchInfo, "发现交换机", JOptionPane.YES_NO_OPTION,
						 				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
//				System.out.println(m);
			}
			//添加交换机
			if (m == 0) {
				switches.put(ipAddr, community);
				LogUtils.lastFlow.put(ipAddr, new HashMap<String, Flow>());
				SingletonThread thread = SingletonThread.getInstance();
				thread.addSwitches(ipAddr, community);
				if (!thread.isAlive())
					thread.start();
				addSwitchWindow.dispose();
			}
		}
	}
	
	
	
	/**************
	 * 交换机窗口
	 * @author Walterll
	 *
	 */
	class SwitchWindow extends JFrame{
		SwitchWindow() {
			super("交换机列表");
			setBounds(200, 200, 250, switches.size() * 30 + 100);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(contentPane);
			contentPane.setLayout(null);
			
			JButton[] switchButtons = new JButton[switches.size()];
			int index = 0;
			for (String ipAddr : switches.keySet()) {
				watchSwitchPortAction = new WatchSwitchPortAction(ipAddr);
				switchButtons[index] = new JButton(ipAddr);
				
				
				switchButtons[index].setAction(watchSwitchPortAction);			
				switchButtons[index].setText(ipAddr);
				switchButtons[index].setBounds(40, index * 30 + 30, 120, 20);
				contentPane.add(switchButtons[index]);
				switchButtons[index].setEnabled(true);
				
				index++;
			}
		}
	}
	
	/******************
	 * 查看交换机流量窗口
	 * @author Walterll
	 *
	 */
	private class WatchSwitchPortAction extends AbstractAction {
		String ipAddress = null;
		public WatchSwitchPortAction(String ipAddr) {
			ipAddress = ipAddr;
		}
		public void actionPerformed(ActionEvent e) {
//			System.out.println(ipAddress);
			switchPortWindow = new SwitchPortWindow(ipAddress);
			switchPortWindow.setVisible(true);
			//测试LogUtils.lastFlow能否正确记录
//			for (String key : LogUtils.lastFlow.keySet()) {
//				for (String port: LogUtils.lastFlow.get(key).keySet()) {
//					Flow flow = LogUtils.lastFlow.get(key).get(port);
//					System.out.println(port + " " + flow.getTime() + ": " + flow.getFlowIn() + "  " + flow.getFlowOut());
//				}
//			}
		}
	}
	
	/**************
	 * 交换机端口窗口
	 * @author Walterll
	 *
	 */
	class SwitchPortWindow extends JFrame{
		SwitchPortWindow(String ipAddr) {
			final String ipAddress = ipAddr;
			final String community = switches.get(ipAddr);
			String[] portString = null;
//			int[] initialFlowIn = null, initialFlowOut = null;
			//初始化，获得端口描述和初始流量
			try {
				portString = SnmpUtils.searchPortY(ipAddr, community);
//				initialFlowIn = new int[portInfo.length];
//				initialFlowOut = new int[portInfo.length];
//				Thread.sleep(1000);
//				SnmpUtils.getFlow(ipAddr, community, initialFlowIn, initialFlowOut);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 		
			
			int maxLength = 0;
			for (int i = 0; i < portString.length; i++) {
				if (portString[i].length() > maxLength)
					maxLength = portString[i].length();
			}
			
			String[] portOID = new String[portString.length];
			String[] portInfo = new String[portString.length];
			for (int i = 0; i < portString.length; i++) {
				String[] tempStr = portString[i].split(":");
				portOID[i] = tempStr[0];
				portInfo[i] = tempStr[1];
			}
			
			setBounds(200, 200, 400 + maxLength * 6, portInfo.length * 20 + 100);
			setTitle(ipAddress);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(contentPane);
			contentPane.setLayout(null);
			
			JLabel portNameText = new JLabel("端口");
			portNameText.setBounds(30, 0, 120, 30);
			portNameText.setFont(new java.awt.Font("Dialog",   1,   15));  
			portNameText.setVisible(true);
			contentPane.add(portNameText);
			
//			JLabel flowInText = new JLabel("输入速率");
//			flowInText.setBounds(470, 10, 90, 30);
//			flowInText.setFont(new java.awt.Font("Dialog",   1,   15));  
//			flowInText.setVisible(true);
//			contentPane.add(flowInText);
//			
//			JLabel flowOutText = new JLabel("输出速率");
//			flowOutText.setBounds(580, 10, 90, 30);
//			flowOutText.setFont(new java.awt.Font("Dialog",   1,   15));  
//			flowOutText.setVisible(true);
//			contentPane.add(flowOutText);
			
			JLabel[] portNames = new JLabel[portInfo.length];
			JButton[] portFlowButton = new JButton[portInfo.length];
			JButton[] portRealTimeFlowButton = new JButton[portInfo.length];
//			final JLabel[] portFlowInPerSec = new JLabel[portInfo.length];
//			final JLabel[] portFlowOutPerSec = new JLabel[portInfo.length];
			//初始化窗口
			for (int i = 0; i < portInfo.length; i++) {
				portNames[i] = new JLabel(portInfo[i]);
				portNames[i].setBounds(30, 20 + i * 20, maxLength * 6, 20);
				portNames[i].setFont(new java.awt.Font("Dialog",   0,   12));  
				portNames[i].setVisible(true);
				contentPane.add(portNames[i]);
				
				watchPortFlowChartAction = new WatchPortFlowChartAction(ipAddr, portOID[i]);
				portFlowButton[i] = new JButton();
				portFlowButton[i].setAction(watchPortFlowChartAction);			
				portFlowButton[i].setText("小时流量图");
				portFlowButton[i].setBounds(50 + maxLength * 6, 20 + i * 20, 120, 20);
				contentPane.add(portFlowButton[i]);
				portFlowButton[i].setEnabled(true);
				
				watchPortRealTimeFlowAction = new WatchPortRealTimeFlowChartAction(ipAddr, portOID[i], portInfo[i]);
				portRealTimeFlowButton[i] = new JButton();
				portRealTimeFlowButton[i].setAction(watchPortRealTimeFlowAction);
				portRealTimeFlowButton[i].setText("实时流量图");
				portRealTimeFlowButton[i].setBounds(200 + maxLength * 6, 20 + i * 20, 120, 20);
				contentPane.add(portRealTimeFlowButton[i]);
				portRealTimeFlowButton[i].setEnabled(true);
//				portFlowInPerSec[i] = new JLabel();
//				portFlowInPerSec[i].setBounds(470, 40 + i * 20, 90, 20);
//				portFlowInPerSec[i].setFont(new java.awt.Font("Dialog",   0,   12));  
//				portFlowInPerSec[i].setVisible(true);
//				contentPane.add(portFlowInPerSec[i]);
//				
//				portFlowOutPerSec[i] = new JLabel();
//				portFlowOutPerSec[i].setBounds(580, 40 + i * 20, 90, 20);
//				portFlowOutPerSec[i].setFont(new java.awt.Font("Dialog",   0,   12));  
//				portFlowOutPerSec[i].setVisible(true);
//				contentPane.add(portFlowOutPerSec[i]);
			}
			
//			final int portNum = portInfo.length;//
//			final int[] flowIn = initialFlowIn;
//			final int[] flowOut = initialFlowOut;
//			new Thread(new Runnable(){
//
//				@Override
//				public void run() {
//					
//					try {
//						Thread.sleep(60000);
//					} catch (InterruptedException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//					// TODO Auto-generated method stub
//					while (true) {
//						int[] oldFlowIn = new int[portNum];
//						int[] oldFlowOut = new int[portNum];
//						for (int i = 0; i < portNum; i++) {
//							oldFlowIn[i] = flowIn[i];
//							oldFlowOut[i] = flowOut[i];
//						}
//						DecimalFormat decimalFormat = new DecimalFormat("0.00");
//						try {
//							SnmpUtils.getFlow(ipAddress, community, flowIn, flowOut);
//						} catch (IOException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
//						for (int i = 0; i < portNum; i++) {
//							float increaseFlowIn = flowIn[i] - oldFlowIn[i];
//							float increaseFlowOut = flowOut[i] - oldFlowOut[i];
//							increaseFlowIn = increaseFlowIn / 1024 / 1024;
//							increaseFlowOut = increaseFlowOut / 1024 / 1024;
//							String in = decimalFormat.format(increaseFlowIn);//format 返回的是字符串
//							String out = decimalFormat.format(increaseFlowOut);
//							portFlowInPerSec[i].setText(in + " mb/m");
//							portFlowOutPerSec[i].setText(out + " mb/m");
//						}
//						try {
//							Thread.sleep(60000);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//				}
//				
//			}).start();
		}
	}
	
	/******************
	 * 查看交换机小时流量表窗口
	 * @author Walterll
	 *
	 */
	private class WatchPortFlowChartAction extends AbstractAction {
		String ipAddr = null;
		String portOID = null;
		public WatchPortFlowChartAction(String ipAddr, String portOID) {
			this.ipAddr = ipAddr;
			this.portOID = portOID;
		}
		public void actionPerformed(ActionEvent e) {
			System.out.println(portOID);
//			switchPortWindow = new SwitchPortWindow(ipAddress);
//			switchPortWindow.setVisible(true);
			flowChartWindow = new FlowChartWindow(ipAddr, portOID);
			flowChartWindow.setVisible(true);
		}
	}
	
	
	class FlowChartWindow extends JFrame {  		  
	    private static final long serialVersionUID = 2319942903676246265L; 	  
	    private ImagePanel imagePanel = new ImagePanel(); 
	    private CategoryDataset data;
	    private JFreeChart chart;
	    private String ipAddr, portOID;
	    private FlowChartWindow(String ipAddr, String portOID) { 
	    	this.ipAddr = ipAddr;
	    	this.portOID = portOID;
	    	
	    	long[] flowIn = new long[1440];
	   	 	long[] flowOut = new long[1440];
	   	 	ca = Calendar.getInstance();
	   	 	
	   	 	String dayOfYear = LogUtils.getDayOfYearStr(ca);
	   	 	LogUtils.readPortFlowData(ipAddr, portOID, dayOfYear, flowIn, flowOut);
	   	 	final double[] flowInPerHour = new double[24];
			final double[] flowOutPerHour = new double[24];
	   	 	LogUtils.getFlowPerHour(flowInPerHour, flowOutPerHour, flowIn, flowOut);
	   	 	
//    		data = LogUtils.createHourDataset(flowInPerHour,flowOutPerHour);  
//	        chart = LogUtils.createChart(data, dayOfYear, "time", "kb");  
	   	 	chart = LogUtils.createChartX(flowInPerHour,flowOutPerHour, dayOfYear, "time", "kb");
	  
	        BufferedImage image = chart.createBufferedImage(1300, 650,  
	                BufferedImage.TYPE_INT_RGB, null);  
	  
	        imagePanel.setImage(image);  
////	        Container con = this.getContentPane();  
////	        con.add(imagePanel, BorderLayout.CENTER); 
//	        
//	        this.setSize(1300, 700);  
//	        this.setVisible(true);  
//	        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
	        
	        JPanel buttonPanel = new JPanel();
	        JButton lastDayButton = new JButton();
	        changeFlowChartByDayAction = new ChangeFlowChartByDayAction(ipAddr, portOID, false, imagePanel);
	        lastDayButton.setAction(changeFlowChartByDayAction);			
	        lastDayButton.setText("前一天");
	        lastDayButton.setBounds(600, 750, 100, 40);
//	        imagePanel.add(lastDayButton);
	        buttonPanel.add(lastDayButton);
			lastDayButton.setEnabled(true);
			
			JButton nextDayButton = new JButton();
	        changeFlowChartByDayAction = new ChangeFlowChartByDayAction(ipAddr, portOID, true, imagePanel);
	        nextDayButton.setAction(changeFlowChartByDayAction);			
	        nextDayButton.setText("后一天");
	        nextDayButton.setBounds(800, 750, 100, 40);
//	        imagePanel.add(nextDayButton);
	        buttonPanel.add(nextDayButton);
			nextDayButton.setEnabled(true);
			
			Container con = this.getContentPane();  
	        
			con.add(imagePanel, new BorderLayout().CENTER); 
			con.add(buttonPanel, new BorderLayout().SOUTH); 
			this.setSize(1300, 750);
			this.setTitle(ipAddr);
			this.setVisible(true); 
	    }  
	  
	}
	
	class ImagePanel extends JPanel {  		  
	    private static final long serialVersionUID = 4644786195524096243L;  	  
	    private BufferedImage image;  	  
	    public ImagePanel() {  
	        super();  
	    }  
	  
	    public void setImage(BufferedImage image) {  
	        this.image = image;  
	    }  
	  
	    protected void paintComponent(Graphics g) {  
	        super.paintComponent(g);  
	        g.setColor(Color.white);  
	  
	        // img = Toolkit.getDefaultToolkit().getImage("C:\\test.jpg");  
	        if (null != image) {  
	            this.setSize(image.getWidth(this), image.getHeight(this));  
	            g.fill3DRect(0, 0, image.getWidth(this), image.getHeight(this),  
	                    true);  
	            g.drawImage(image, 0, 0, null, this);  
	            setPreferredSize(new Dimension(image.getWidth(this),  
	                    image.getHeight(this)));  
	        }  
	    } 
	}
	
	/******************
	 * 查看交换机流量窗口
	 * @author Walterll
	 *
	 */
	private class ChangeFlowChartByDayAction extends AbstractAction {
		private String ipAddr, portOID;
		private ImagePanel imagePanel;
	    private CategoryDataset data;
	    private JFreeChart chart;
	    private boolean isNextDay;
		public ChangeFlowChartByDayAction(String ipAddr, String portOID, boolean isNextDay, ImagePanel imagePanel) {
			this.ipAddr = ipAddr;
			this.portOID = portOID;
			this.isNextDay = isNextDay;
			this.imagePanel = imagePanel;
		}
		public void actionPerformed(ActionEvent e) {
			if (isNextDay)
				ca.add(Calendar.DATE, 1);
			else 
				ca.add(Calendar.DATE, -1);
//			System.out.println(ca.get(Calendar.DATE));
			long[] flowIn = new long[1440];
			long[] flowOut = new long[1440];
	   	 	String dayOfYear = LogUtils.getDayOfYearStr(ca);
	   	 	LogUtils.readPortFlowData(ipAddr, portOID, dayOfYear, flowIn, flowOut);
	   	 	final double[] flowInPerHour = new double[24];
			final double[] flowOutPerHour = new double[24];
	   	 	LogUtils.getFlowPerHour(flowInPerHour, flowOutPerHour, flowIn, flowOut);
	   	 	
//	   	 	for (int i = 0; i < flowInPerHour.length; i++)
//	   	 		System.out.println(flowInPerHour[i]);
//	   	 	System.out.println();
	   	 	
//    		data = LogUtils.createHourDataset(flowInPerHour,flowOutPerHour);  
//	        chart = LogUtils.createChart(data, dayOfYear, "time", "kb");  
	   	 	chart = LogUtils.createChartX(flowInPerHour,flowOutPerHour, dayOfYear, "time", "kb");
	  
	        BufferedImage image = chart.createBufferedImage(1300, 700,  
	                BufferedImage.TYPE_INT_RGB, null);  
	  
	        imagePanel.setImage(image);  
	        Container con = flowChartWindow.getContentPane();  
	        con.add(imagePanel, BorderLayout.CENTER);
			flowChartWindow.repaint();
		}
	}
	
	/******************
	 * 查看交换机实时流量窗口
	 * @author Walterll
	 *
	 */
	private class WatchPortRealTimeFlowChartAction extends AbstractAction {
		String ipAddr = null;
		String port = null;
		String portInfo = null;
		public WatchPortRealTimeFlowChartAction(String ipAddr, String port, String portInfo) {
			this.ipAddr = ipAddr;
			this.port = port;
			this.portInfo = portInfo;
		}
		public void actionPerformed(ActionEvent e) {
			System.out.println(port);
//			switchPortWindow = new SwitchPortWindow(ipAddress);
//			switchPortWindow.setVisible(true);
//			flowChartWindow = new FlowChartWindow(ipAddr, portOID);
//			flowChartWindow.setVisible(true);
	    	Calendar ca = Calendar.getInstance();
	    	String dayOfYear = LogUtils.getDayOfYearStr(ca);
	    	List<Flow> flows = LogUtils.readLastHalfHourPortFlowData(ipAddr, port, dayOfYear);
//	    	RealTimeFlowChart.setLastFlow(flows.get(flows.size() - 1));
//	    	System.out.println("lastFlow = " + lastFlow.getTime() + " " + lastFlow.getFlowIn() + " " + lastFlow.getFlowOut());
	    	int tempTime = 0;
	    	long tempFlowIn = 0;
	    	long tempFlowOut = 0;
	    	int tempTimeInc = 0;
//	    	long tempFlowInInc = 0;
//	    	long tempFlowOutInc = 0;
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
//	    	RealTimeFlowChart.setLastFlowInc(flowsInc.get(flowsInc.size() - 1));
//	    	System.out.println("lastFlowInc = " + lastFlowInc.getTime() + " " + lastFlowInc.getFlowIn() + " " + lastFlowInc.getFlowOut());
//	    	for (Flow flowInc : flowsInc) {
//	    		System.out.println(flowInc.time / 60 + ":" + flowInc.time % 60 + " " + flowInc.flowIn + " " + flowInc.flowOut);
//	    	}
	    	
	    	final JFrame frame = new JFrame(ipAddr); 
//			final RealTimeFlowChart rtcp = new RealTimeFlowChart(ipAddr, portInfo, "kb", flowsInc, ipAddr, port, 
//														flows.get(flows.size() - 1), flowsInc.get(flowsInc.size() - 1)); 
			
	    	Flow flow = flows.size() > 0 ? flows.get(flows.size() - 1) : 
	    			new Flow(ca.get(Calendar.HOUR) * 60 + ca.get(Calendar.MINUTE), 0, 0);
	    	Flow flowInc = flowsInc.size() > 0 ? flowsInc.get(flowsInc.size() - 1) : 
    			new Flow(ca.get(Calendar.HOUR) * 60 + ca.get(Calendar.MINUTE), 0, 0);
//	    	final RealTimeFlow rtf = new RealTimeFlow(ipAddr, portInfo, "kb", flowsInc, ipAddr, port, 
//					flows.get(flows.size() - 1), flowsInc.get(flowsInc.size() - 1));
	    	final RealTimeFlow rtf = new RealTimeFlow(ipAddr, portInfo, "kb", flowsInc, ipAddr, port, 
					flow, flowInc);
			JFreeChart chart = rtf.createChart(ipAddr, portInfo, "kb", flowsInc);
			ChartPanel panel = new ChartPanel(chart);
		
			
			frame.getContentPane().add(panel, new BorderLayout().CENTER); 
			frame.pack(); 
			frame.setVisible(true); 
//			Thread flowChartThread = new Thread(rtcp);
//			flowChartThread.start();
			(new Thread(rtf)).start(); 
			frame.addWindowListener(new WindowAdapter() { 
			public void windowClosing(WindowEvent windowevent) { 
//			System.exit(0); 
				rtf.setStop(true);
				frame.dispose();
			} 

			}); 
		}
	}

}


