package com.snmp;

public class Flow {
	int time;
	long flowIn;
	long flowOut;
	public Flow(int time, long flowIn, long flowOut) {
		this.time = time;
		this.flowIn = flowIn;
		this.flowOut = flowOut;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public long getFlowIn() {
		return flowIn;
	}
	public void setFlowIn(long flowIn) {
		this.flowIn = flowIn;
	}
	public long getFlowOut() {
		return flowOut;
	}
	public void setFlowOut(long flowOut) {
		this.flowOut = flowOut;
	}
}
