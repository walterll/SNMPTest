package com.snmp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SingletonThread extends Thread{
    private static SingletonThread instanceThread;
    private Map<String, String> switchesMap = new HashMap<>();
    private SingletonThread() {
    };
    public static synchronized SingletonThread getInstance() {
        if (instanceThread == null) {
            instanceThread = new SingletonThread();
        }
        return instanceThread;
    }
    
    public void addSwitches(String ipAddr, String community) {
    	switchesMap.put(ipAddr, community);
    }
    
    public void deleteSwitches(String ipAddr) {
    	switchesMap.remove(ipAddr);
    }
    public void run() {
    	A:while (true) {
//	    	for (String ipAddr : switchesMap.keySet())
//	        System.out.println("ip: " + ipAddr + " community: " + switchesMap.get(ipAddr));
    		B:for (String ipAddr : switchesMap.keySet()) {
    			String community = switchesMap.get(ipAddr);
		    	try {
		    		Map<String, Long> portFlow = SnmpUtils.getFlow(ipAddr, community);
//		    		for (String Oid : portFlow.keySet()) {
//		    			System.out.println("Oid: " + Oid + " Flow: " + portFlow.get(Oid));
//		    		}
//		    		LogUtils.writeFlowData(ipAddr, portFlow);
		    		LogUtils.writeFlowDataX(ipAddr, portFlow);
				} catch (NullPointerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue A;//////////////////////////////////////////////////////////////
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		try {
    			Thread.sleep(60000);
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    }

    
}
