package com.chry.util.http;

public class LoadEvent {
	public static int OK = 0;
	public static int ERROR = -1;
	public int status;
	public Exception error;
	private LoadEvent(int status, Exception e) {
		this.status = status;
		this.error = e;
	}
	public static final LoadEvent OKEvent = new LoadEvent(0, null);
	public static LoadEvent getEvent(Exception e){
		return e == null ? OKEvent : new LoadEvent(-1, e);
	}
}
