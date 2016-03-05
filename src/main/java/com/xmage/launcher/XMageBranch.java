package com.xmage.launcher;

public class XMageBranch {
	public String title;
	public String url;
	
	public XMageBranch(String title, String url) {
		super();
		this.title = title;
		this.url = url;
	}
	
	@Override
	public String toString() {
		return title + (url != null ? " — " + url : "");
	}
	
}
