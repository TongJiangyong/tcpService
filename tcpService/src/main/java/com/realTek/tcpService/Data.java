package com.realTek.tcpService;

public class Data {
	private String flag;//服务器端采用flag+object的形式做简化处理
	private String command;
	private String commandInfo; // 用来传递info信息，没法办要加这个选项，因为gson的限制，没办法用下面的Object类来进行转换.....
	private Object msg;
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public Object getMsg() {
		return msg;
	}
	public void setMsg(Object msg) {
		this.msg = msg;
	}


}
