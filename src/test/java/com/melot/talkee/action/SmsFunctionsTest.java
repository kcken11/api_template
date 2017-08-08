package com.melot.talkee.action;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.melot.talkee.utils.SecurityFunctions;

public class SmsFunctionsTest {
	
	private Logger logger = Logger.getLogger(SmsFunctionsTest.class);
	
	private static	HttpClient httpClient = null;
		
	private static	GetMethod getMethod = null;
	
	private static	String url ="http://10.0.3.46:8080/talkee/entrance?parameter=%s";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		 httpClient = new HttpClient();
	}

	@Test
	public void testSendSms() throws UnsupportedEncodingException {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("smsType", 3);
		jsonObject.addProperty("phoneNum", "13111111111");
		jsonObject.addProperty("FuncTag", 10110101);
		jsonObject.addProperty("p", 1);
		jsonObject.addProperty("v", "1.0.0");
		jsonObject.addProperty("c", 1);
		jsonObject.addProperty("s", 1);
		String s =	SecurityFunctions.getSingedValue(jsonObject);
		jsonObject.addProperty("s", s);
		
		httpGet(jsonObject);
	}

	
	public void httpGet(JsonObject jsonObject){
		System.out.println(String.format(url, new Gson().toJson(jsonObject)));
		try{

			
			
			String getMethodUrl = String.format(url, URLEncoder.encode(new Gson().toJson(jsonObject),"UTF-8"));
			
			getMethod = new GetMethod(getMethodUrl);
			int httpReturnCold = httpClient.executeMethod(getMethod);
			byte[] bytes =	getMethod.getResponseBody();
			String retValue = new String(bytes,"UTF-8");
			System.out.println("responseJson : "+retValue);
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
