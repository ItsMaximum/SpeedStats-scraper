package com.tsunderebug.speedrun4j.game;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class Variable {
	private String id;
	private String name;
	@SerializedName("is-subcategory") private boolean issubcategory;
	private Map<String, Object> values;
	private Map<String,String> possValues;
	private Map<String,String> scope;
	
	public String getID() {
		return id;
	}
	public String getName() {
		return name;
	}
	
	public boolean getIsSubcategory() {
		return issubcategory;
	}
	
	public Map<String, Object> getValues() {
		return values;
	}
	public String getId() {
		return id;
	}
	public boolean isIssubcategory() {
		return issubcategory;
	}
	public Map<String, String> getPossValues() {
		return possValues;
	}
	public Map<String,String> getScope() {
		return scope;
	}
	
	

}
