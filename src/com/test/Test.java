package com.test;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class Test {
	public static void main(String[] args) { 
		JFrame frame = new JFrame("Test Chart"); 
		RealTimeChart rtcp = new RealTimeChart("Random Data", "随机数", "数值"); 
		frame.getContentPane().add(rtcp, new BorderLayout().CENTER); 
		frame.pack(); 
		frame.setVisible(true); 
		(new Thread(rtcp)).start(); 
		frame.addWindowListener(new WindowAdapter() { 
		public void windowClosing(WindowEvent windowevent) { 
		System.exit(0); 
		} 

		}); 
		 
	
	JFrame frame1 = new JFrame("Test Chart1"); 
	RealTimeChart rtcp1 = new RealTimeChart("Random Data1", "随机数", "数值"); 
	frame1.getContentPane().add(rtcp1, new BorderLayout().CENTER); 
	frame1.pack(); 
	frame1.setVisible(true); 
	(new Thread(rtcp1)).start(); 
	frame1.addWindowListener(new WindowAdapter() { 
	public void windowClosing(WindowEvent windowevent) { 
	System.exit(0); 
	} 

	}); 
	} 

}
