package com.snmp;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

public class SnmpUtils {

	/********************
	 * 搜索交换机
	 * @param ipAddr
	 * @param community
	 * @return
	 * @throws IOException
	 */
	public static String searchSwitch(String ipAddr, String community) throws IOException {
		Snmp snmp = new Snmp(new DefaultUdpTransportMapping());  
        snmp.listen();  
          
        CommunityTarget target = new CommunityTarget();  
        target.setCommunity(new OctetString(community));  
        target.setVersion(SnmpConstants.version2c);  
        target.setAddress(new UdpAddress(ipAddr + "/161"));  
        target.setTimeout(3000);    //3s  
        target.setRetries(1);
        
        Vector<? extends VariableBinding> vbs = sendRequest(snmp, createGetPdu(new OID("1.3.6.1.2.1.1.1.0")), target);
        if (vbs == null)
        	return null;
        String switchInfo = null;
        for (VariableBinding vb : vbs) {
        	switchInfo = vb.getVariable().toString();
        }
        snmp.close();
        return switchInfo;
        
	}
	
	/*******************
	 * 发送snmp请求
	 * @param snmp
	 * @param pdu
	 * @param target
	 * @return
	 * @throws IOException
	 */
	private static Vector<? extends VariableBinding> sendRequest(Snmp snmp, PDU pdu, CommunityTarget target)  
		    throws IOException {  
        ResponseEvent responseEvent = snmp.send(pdu, target);  
        PDU response = responseEvent.getResponse();  
          
        if (response == null) {  
            System.out.println("TimeOut...");
            return null;
        } else {  
            if (response.getErrorStatus() == PDU.noError) {  
                Vector<? extends VariableBinding> vbs = response.getVariableBindings();  
                for (VariableBinding vb : vbs) {  
                    System.out.println(vb + " ," + vb.getVariable().getSyntaxString());  
                    System.out.println("---------" + vb.getVariable().toString());
                }  
                return vbs;
            } else {  
                System.out.println("Error:" + response.getErrorStatusText());  
                return null;
            }  
        }  
	}  
	
	//经测试华为S2403H交换机无法使用TableUtils的方式返回数据，因此添加一个方法使用普通pdu方式发送snmp请求
	/************************
	 * 查询交换机端口描述信息
	 * @param ipAddr
	 * @param community
	 * @return
	 * @throws IOException
	 */
	public static String[] searchPortX(String ipAddr, String community) throws IOException {
		String[] portType = null;
		
		Snmp snmp = new Snmp(new DefaultUdpTransportMapping());  
        snmp.listen();  
          
        CommunityTarget target = new CommunityTarget();  
        target.setCommunity(new OctetString(community));  
        target.setVersion(SnmpConstants.version2c);  
        target.setAddress(new UdpAddress(ipAddr + "/161"));  
        target.setTimeout(3000);    //3s  
        target.setRetries(1);  
        
        PDU pdu = new PDU();  
        pdu.setType(PDU.GETBULK);  
//        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.2.2.1.10"))); //sysInfo
//        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.2.0")));//time
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.2.2.1.2")));
        pdu.setMaxRepetitions(60);
        
        ResponseEvent responseEvent = snmp.send(pdu, target);  
        PDU response = responseEvent.getResponse();    
        if (response == null) {  
            System.out.println("TimeOut...");  
        } else {  
            if (response.getErrorStatus() == PDU.noError) {  
                Vector<? extends VariableBinding> vbs = response.getVariableBindings(); 
                portType = new String[vbs.size()];
                int index = 0;
                for (VariableBinding vb : vbs) {  
                    System.out.println(vb + " ," + vb.getVariable().getSyntaxString());  
                    portType[index] = vb.getVariable().getSyntaxString();
                    index++;
                }  
            } else {  
                System.out.println("Error:" + response.getErrorStatusText());  
            }  
        }
        snmp.close();
        return portType;
	}
	
	/*********************
	 * 查询交换机端口描述信息
	 * @param ipAddr
	 * @param community
	 * @return
	 * @throws IOException
	 */
	public static String[] searchPort(String ipAddr, String community) throws IOException {
//		Map<OID, String> portMap = new HashMap<>();
		String[] portType;
		
		Snmp snmp = new Snmp(new DefaultUdpTransportMapping());  
		snmp.listen();
		 
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(community));
		target.setVersion(SnmpConstants.version2c);
		target.setAddress(new UdpAddress(ipAddr + "/161"));
		target.setTimeout(3000);
		target.setRetries(1);
		
		TableUtils utils = new TableUtils(snmp, new DefaultPDUFactory(PDU.GETBULK));//GETNEXT or GETBULK  
		utils.setMaxNumRowsPerPDU(5);   //only for GETBULK, set max-repetitions, default is 10  
		OID[] columnOids = new OID[] { new OID("1.3.6.1.2.1.2.2.1.2")};
		List<TableEvent> list = utils.getTable(target, columnOids, null, null);
		portType = new String[list.size()];
		int index = 0;
		for (TableEvent t : list) {
			System.out.println(t.getIndex() + t.getColumns()[0].getVariable().toString());
		//				 portMap.put(t.getIndex(), t.getColumns()[0].getVariable().toString());
			portType[index] = t.getColumns()[0].getVariable().toString();
			index++;
		}
		snmp.close();
		return portType;        
	}
	
	/*********************
	 * 查询交换机端口描述信息,返回字符串包含端口OID编号
	 * @param ipAddr
	 * @param community
	 * @return
	 * @throws IOException
	 */
	public static String[] searchPortY(String ipAddr, String community) throws IOException {
//		Map<OID, String> portMap = new HashMap<>();
		String[] portType;
		
		Snmp snmp = new Snmp(new DefaultUdpTransportMapping());  
		snmp.listen();
		 
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(community));
		target.setVersion(SnmpConstants.version2c);
		target.setAddress(new UdpAddress(ipAddr + "/161"));
		target.setTimeout(3000);
		target.setRetries(1);
		
		TableUtils utils = new TableUtils(snmp, new DefaultPDUFactory(PDU.GETBULK));//GETNEXT or GETBULK  
		utils.setMaxNumRowsPerPDU(5);   //only for GETBULK, set max-repetitions, default is 10  
		OID[] columnOids = new OID[] { new OID("1.3.6.1.2.1.2.2.1.2")};
		List<TableEvent> list = utils.getTable(target, columnOids, null, null);
		portType = new String[list.size()];
		int index = 0;
		for (TableEvent t : list) {
			System.out.println(t.getIndex() + t.getColumns()[0].getVariable().toString());
		//				 portMap.put(t.getIndex(), t.getColumns()[0].getVariable().toString());
			portType[index] = t.getIndex() + ":" + t.getColumns()[0].getVariable().toString();
			index++;
		}
		snmp.close();
		return portType;        
	}
	
	/***********************
	 * 查询交换机流量信息
	 * @param ipAddr
	 * @param community
	 * @param flowIn
	 * @param flowOut
	 * @throws IOException
	 */
	public static void getFlow(String ipAddr, String community, int[] flowIn, int[] flowOut) throws IOException {
		Snmp snmp = new Snmp(new DefaultUdpTransportMapping());  
		snmp.listen();
		 
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(community));
		target.setVersion(SnmpConstants.version2c);
		target.setAddress(new UdpAddress(ipAddr + "/161"));
		target.setTimeout(3000);
		target.setRetries(1);
		
		TableUtils utils = new TableUtils(snmp, new DefaultPDUFactory(PDU.GETBULK));//GETNEXT or GETBULK  
		utils.setMaxNumRowsPerPDU(5);   //only for GETBULK, set max-repetitions, default is 10  
		OID[] columnOids = new OID[] { 
				new OID("1.3.6.1.2.1.2.2.1.10"), 
	            new OID("1.3.6.1.2.1.2.2.1.16"),
	    };
		List<TableEvent> list = utils.getTable(target, columnOids, null, null);
		 
		int index = 0;
	    for (TableEvent e : list) {  
	       	VariableBinding[] vb = e.getColumns();
	       	flowIn[index] = vb[0].getVariable().toInt();
	       	flowOut[index] = vb[1].getVariable().toInt();
//		           System.out.println(e);  
	       	index++;
	    }     
	    snmp.close();
	}
	
	//HomeWindow2使用的获取端口流量方法，不传入数组，直接返回存储流量的Map
	/******************************
	 * 查询交换机流量信息
	 * @param ipAddr
	 * @param community
	 * @return
	 * @throws IOException
	 */
	public static Map<String, Long> getFlow(String ipAddr, String community) throws IOException {
		Snmp snmp = new Snmp(new DefaultUdpTransportMapping());  
		snmp.listen();
		 
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(community));
		target.setVersion(SnmpConstants.version2c);
		target.setAddress(new UdpAddress(ipAddr + "/161"));
		target.setTimeout(3000);
		target.setRetries(1);
		
		TableUtils utils = new TableUtils(snmp, new DefaultPDUFactory(PDU.GETBULK));//GETNEXT or GETBULK  
		utils.setMaxNumRowsPerPDU(5);   //only for GETBULK, set max-repetitions, default is 10  
		OID[] columnOids = new OID[] { 
//				new OID("1.3.6.1.2.1.2.2.1.10"), 
//	            new OID("1.3.6.1.2.1.2.2.1.16"),
				new OID("1.3.6.1.2.1.31.1.1.1.6"),
				new OID("1.3.6.1.2.1.31.1.1.1.10"),
	    };
		List<TableEvent> list = utils.getTable(target, columnOids, null, null);
		 
		Map<String, Long> portFlowMap = new HashMap<>();
	    for (TableEvent e : list) {  
	       	VariableBinding[] vb = e.getColumns();
	       	portFlowMap.put(vb[0].getOid().toString(), vb[0].getVariable().toLong());
	       	portFlowMap.put(vb[1].getOid().toString(), vb[1].getVariable().toLong());
//		           System.out.println(e);  
	       	
	    }   
	    snmp.close();		////////////
	    return portFlowMap;
	}
	
	/***************
	 * 生成PDU报文
	 * @param oid
	 * @return
	 */	
	private static PDU createGetPdu(OID oid) {  
        PDU pdu = new PDU();  
        pdu.setType(PDU.GET);  
        pdu.add(new VariableBinding(oid));
//        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.1.0"))); //  
//        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"))); //sysName  
//        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5")));   //expect an no_such_instance error  
        return pdu;  
    }  
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Snmp snmp = new Snmp(new DefaultUdpTransportMapping());  
        snmp.listen();  
          
        CommunityTarget target = new CommunityTarget();  
        target.setCommunity(new OctetString("public"));  
        target.setVersion(SnmpConstants.version2c);  
        target.setAddress(new UdpAddress("192.168.2.244/161"));  
        target.setTimeout(3000);    //3s  
        target.setRetries(1);  
        
//        sendRequest(snmp, createGetPdu(), target);
	}

}
