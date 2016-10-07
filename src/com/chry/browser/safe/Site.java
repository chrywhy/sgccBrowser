package com.chry.browser.safe;

public class Site {
	private String _id;
	private String _url;
	private String _name;
	private String _risk;
	private String _type;
	
	public Site() {
		_url = "";
		_name = "";
		_type = "";
		_risk = "";
	}
		
	public Site(String pattern, String name, String type, String risk) {
		_id = "";
		_url = pattern;
		_name = name;
		_type = type;
		_risk = risk;
	}
	
	public String getId() {
		return _id;
	}
	
	public void setId(String id) {
		_id = id;
	}
	
	public String getPattern() {
		return _url;
	}
	
	public void setPattern(String pattern) {
		_url = pattern;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public String getType() {
		return _type;
	}

	public void setType(String type) {
		_type = type;
	}

	public String getRisk() {
		return _risk;
	}

	public void setRisk(String risk) {
		_risk = risk;
	}
}
