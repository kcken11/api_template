package com.melot.talkee.utils;

import com.google.gson.JsonObject;

public class SwapObj {

	private JsonObject paramJson = new JsonObject();
	private boolean checkTag = false;
	private int funcTag = 0;

	public JsonObject getParamJson() {
		return paramJson;
	}

	public void setParamJson(JsonObject paramJson) {
		this.paramJson = paramJson;
	}

	public boolean isCheckTag() {
		return checkTag;
	}

	public void setCheckTag(boolean checkTag) {
		this.checkTag = checkTag;
	}

	public int getFuncTag() {
		return funcTag;
	}

	public void setFuncTag(int funcTag) {
		this.funcTag = funcTag;
	}

}
