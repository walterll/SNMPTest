package com.snmp;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
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

import org.snmp4j.CommunityTarget;


/********************
 * 添加交换机后，交换机界面进入流量窗口开始查询流量
 * @author Walterll
 *
 */
public class HomeWindow extends JFrame {
	
	private Map<String, String> switches;

	private JPanel contentPane;
	
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
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					HomeWindow frame = new HomeWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public HomeWindow() {
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
		//测试能否动态添加按钮
//		switches.put("192.168.2.235", "admin");
//		switches.put("192.168.3.11", "private");
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
	/****************
	 * 点击搜索交换机
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
				Object[] options ={ "添加", "取消" };  //自定义按钮上的文字
				m = JOptionPane.showOptionDialog(null, switchInfo, "发现交换机", JOptionPane.YES_NO_OPTION,
						 				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
//				System.out.println(m);
			}
			//添加交换机
			if (m == 0) {
				switches.put(ipAddr, community);
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
			setBounds(200, 200, 200, switches.size() * 30 + 100);
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
	
	/************
	 * 点击交换机进入流量窗口
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
		}
	}
	
	/**************
	 * 交换机端口流量监视窗口
	 * @author Walterll
	 *
	 */
	class SwitchPortWindow extends JFrame{
		SwitchPortWindow(String ipAddr) {
			final String ipAddress = ipAddr;
			final String community = switches.get(ipAddr);
			String[] portInfo = null;
			int[] initialFlowIn = null, initialFlowOut = null;
			//初始化，获得端口描述和初始流量
			try {
				portInfo = SnmpUtils.searchPort(ipAddr, community);
				initialFlowIn = new int[portInfo.length];
				initialFlowOut = new int[portInfo.length];
				Thread.sleep(1000);
				SnmpUtils.getFlow(ipAddr, community, initialFlowIn, initialFlowOut);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
			setBounds(200, 200, 800, portInfo.length * 20 + 100);
			setTitle(ipAddress);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(contentPane);
			contentPane.setLayout(null);
			
			JLabel portNameText = new JLabel("端口");
			portNameText.setBounds(30, 10, 120, 30);
			portNameText.setFont(new java.awt.Font("Dialog",   1,   15));  
			portNameText.setVisible(true);
			contentPane.add(portNameText);
			
			JLabel flowInText = new JLabel("输入速率");
			flowInText.setBounds(470, 10, 90, 30);
			flowInText.setFont(new java.awt.Font("Dialog",   1,   15));  
			flowInText.setVisible(true);
			contentPane.add(flowInText);
			
			JLabel flowOutText = new JLabel("输出速率");
			flowOutText.setBounds(580, 10, 90, 30);
			flowOutText.setFont(new java.awt.Font("Dialog",   1,   15));  
			flowOutText.setVisible(true);
			contentPane.add(flowOutText);
			
			JLabel[] portNames = new JLabel[portInfo.length];
			final JLabel[] portFlowInPerSec = new JLabel[portInfo.length];
			final JLabel[] portFlowOutPerSec = new JLabel[portInfo.length];
			//初始化窗口
			for (int i = 0; i < portInfo.length; i++) {
				portNames[i] = new JLabel(portInfo[i]);
				portNames[i].setBounds(30, 40 + i * 20, 420, 20);
				portNames[i].setFont(new java.awt.Font("Dialog",   0,   12));  
				portNames[i].setVisible(true);
				contentPane.add(portNames[i]);
				
				portFlowInPerSec[i] = new JLabel();
				portFlowInPerSec[i].setBounds(470, 40 + i * 20, 90, 20);
				portFlowInPerSec[i].setFont(new java.awt.Font("Dialog",   0,   12));  
				portFlowInPerSec[i].setVisible(true);
				contentPane.add(portFlowInPerSec[i]);
				
				portFlowOutPerSec[i] = new JLabel();
				portFlowOutPerSec[i].setBounds(580, 40 + i * 20, 90, 20);
				portFlowOutPerSec[i].setFont(new java.awt.Font("Dialog",   0,   12));  
				portFlowOutPerSec[i].setVisible(true);
				contentPane.add(portFlowOutPerSec[i]);
			}
			
			final int portNum = portInfo.length;//
			final int[] flowIn = initialFlowIn;
			final int[] flowOut = initialFlowOut;
			new Thread(new Runnable(){

				@Override
				public void run() {
					
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					// TODO Auto-generated method stub
					while (true) {
						int[] oldFlowIn = new int[portNum];
						int[] oldFlowOut = new int[portNum];
						for (int i = 0; i < portNum; i++) {
							oldFlowIn[i] = flowIn[i];
							oldFlowOut[i] = flowOut[i];
						}
						DecimalFormat decimalFormat = new DecimalFormat("0.00");
						try {
							SnmpUtils.getFlow(ipAddress, community, flowIn, flowOut);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						for (int i = 0; i < portNum; i++) {
							float increaseFlowIn = flowIn[i] - oldFlowIn[i];
							float increaseFlowOut = flowOut[i] - oldFlowOut[i];
							increaseFlowIn = increaseFlowIn / 1024 / 1024;
							increaseFlowOut = increaseFlowOut / 1024 / 1024;
							String in = decimalFormat.format(increaseFlowIn);//format 返回的是字符串
							String out = decimalFormat.format(increaseFlowOut);
							portFlowInPerSec[i].setText(in + " mb/m");
							portFlowOutPerSec[i].setText(out + " mb/m");
						}
						try {
							Thread.sleep(60000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
			}).start();
		}
	}
}
