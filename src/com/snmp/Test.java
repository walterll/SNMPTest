package com.snmp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYDataset;

import com.snmp.HomeWindow2.AddSwitchWindow;



public class Test {
	class TimeCharttest extends JFrame {  		  
	    private static final long serialVersionUID = 2319942903676246265L; 	  
	    private ImagePanel imagePanel = new ImagePanel();
	    private CategoryDataset data;
	    private JFreeChart chart;
//	    private final Action addSwitchAction;
	    private TimeCharttest(double[] flowInPerHour, double[] flowOutPerHour) throws IOException {  
	        data = LogUtils.createHourDataset(flowInPerHour,flowOutPerHour);  
	        chart = LogUtils.createChart(data);  
	  
	        BufferedImage image = chart.createBufferedImage(800, 300,  
	                BufferedImage.TYPE_INT_RGB, null);  
	  
	        imagePanel.setImage(image);  
	        Container con = this.getContentPane();  
	        con.add(imagePanel, BorderLayout.CENTER);  
	        
	        this.setSize(900, 400);  
	        this.setVisible(true);  
	        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
	        
	        try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        long[] flowIn = new long[1440];
	   	 	long[] flowOut = new long[1440];
	   	 	LogUtils.readPortFlowData("192.168.2.248", "3074", "2018_8_8", flowIn, flowOut);
	   	 	final double[] flowInHour = new double[24];
			final double[] flowOutHour = new double[24];
	   	 	LogUtils.getFlowPerHour(flowInHour, flowOutHour, flowIn, flowOut);
	   	 data = LogUtils.createHourDataset(flowInPerHour,flowOutPerHour);  
	        chart = LogUtils.createChart(data);  
	  
	        image = chart.createBufferedImage(800, 300,  
	                BufferedImage.TYPE_INT_RGB, null);  
	  
	        imagePanel.setImage(image);
	        imagePanel.repaint();
//	        addSwitchAction = new AddSwitchAction(flowInHour, flowOutHour);
//	        JButton addSwitchButton = new JButton("AddSwitch");
//			addSwitchButton.setAction(addSwitchAction);			
//			addSwitchButton.setText("添加交换机");
//			addSwitchButton.setBounds(50, 50, 120, 60);
//			imagePanel.add(addSwitchButton);
//			addSwitchButton.setEnabled(true);
	    }  
	  
	    private class AddSwitchAction extends AbstractAction {
	    	double[] flowInPerHour, flowOutPerHour;
	    	public AddSwitchAction(double[] flowInPerHour, double[] flowOutPerHour) {
	    		this.flowInPerHour = flowInPerHour;
	    		this.flowOutPerHour = flowOutPerHour;
	    	}
	    	public void actionPerformed(ActionEvent e) {
	    		try {
					TimeCharttest t = new TimeCharttest(flowInPerHour, flowOutPerHour);
					t.setVisible(true);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		        
	    	}
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
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long[] flowIn = new long[1440];
   	 	long[] flowOut = new long[1440];
   	 	LogUtils.readPortFlowData("192.168.2.248", "3074", "2018_8_9", flowIn, flowOut);
   	 	final double[] flowInPerHour = new double[24];
		final double[] flowOutPerHour = new double[24];
   	 	LogUtils.getFlowPerHour(flowInPerHour, flowOutPerHour, flowIn, flowOut);
   	 	final Test test = new Test();
   	 	EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TimeCharttest frame = test.new TimeCharttest(flowInPerHour, flowOutPerHour);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
