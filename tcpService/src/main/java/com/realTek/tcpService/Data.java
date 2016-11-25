package com.realTek.tcpService;

public class Data {
	private String flag;//服务器端采用flag+object的形式做简化处理  0#自己的id/给服务器    123456#/广播
	private Object msg;
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public Object getMsg() {
		return msg;
	}
	public void setMsg(Object msg) {
		this.msg = msg;
	}


}
