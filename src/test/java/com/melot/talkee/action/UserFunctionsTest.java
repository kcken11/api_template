package com.melot.talkee.action;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.melot.talkee.utils.SecurityFunctions;

public class UserFunctionsTest {

	private static	HttpClient httpClient = null;
	
	private static	GetMethod getMethod = null;
	
	private static	String url ="http://10.0.3.56:9090/talkee/entrance";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		 httpClient = new HttpClient();
	}

	@Test
	public void testGetStudentExtraInfos(){
		PostMethod filePost = new PostMethod(url);
		HttpClient client = new HttpClient();
		try {

			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("token", "7e51529d9e2341efb75f51dab57e24ea");
			jsonObject.addProperty("userId", 442);
			jsonObject.addProperty("FuncTag", 20000108);
			jsonObject.addProperty("studentId", 604);

			jsonObject.addProperty("segment", 1);

			jsonObject.addProperty("p", 1);
			jsonObject.addProperty("v", "1.0.0");
			jsonObject.addProperty("c", 1);
			String s = SecurityFunctions.getSingedValue(jsonObject);
			jsonObject.addProperty("s", s);

			System.out.println(new Gson().toJson(jsonObject));


			Part[] parts = {new StringPart("parameter", new Gson().toJson(jsonObject))};
			filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));

			client.getHttpConnectionManager().getParams().setConnectionTimeout(50000);

			int status = client.executeMethod(filePost);
			byte[] bytes = filePost.getResponseBody();
			String retValue = new String(bytes, "UTF-8");
			System.out.println("responseJson : " + retValue);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			filePost.releaseConnection();
		}
	}






	public void httpGet(JsonObject jsonObject){
		System.out.println(String.format(url, new Gson().toJson(jsonObject)));
		try{

			
			String getMethodUrl = String.format(url, URLEncoder.encode(new Gson().toJson(jsonObject),"UTF-8"));
			System.out.println(getMethodUrl);
			getMethod = new GetMethod(getMethodUrl);
			getMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"UTF-8");  
			getMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
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
	
	@Test
	public void testChangePwd() throws UnsupportedEncodingException {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("token", "d7049571d84042988db1bf2e65722529");
		jsonObject.addProperty("FuncTag", 20110104);
		jsonObject.addProperty("userId", 12);
		jsonObject.addProperty("oldpwd", "1111a1");
		jsonObject.addProperty("newpwd", "482694Abc");
		jsonObject.addProperty("type", "1");
		
		jsonObject.addProperty("p", 1);
		jsonObject.addProperty("v", "1.0.0");
		jsonObject.addProperty("c", 1);
		jsonObject.addProperty("s", 1);
//		String s =	SecurityFunctions.getSingedValue(jsonObject);
//		jsonObject.addProperty("s", URLEncoder.encode(s,"UTF-8"));
		
		httpGet(jsonObject);
		
	}
	
	@Test
	public void testFindPwd() throws UnsupportedEncodingException {
		JsonObject jsonObject = new JsonObject();
//		jsonObject.addProperty("token", "d7049571d84042988db1bf2e65722529");
		jsonObject.addProperty("FuncTag", 20110104);
//		jsonObject.addProperty("userId", 12);
		jsonObject.addProperty("verifyCode", "724329");
		jsonObject.addProperty("newpwd", "1111a1");
		jsonObject.addProperty("phoneNum", "13111111111");
		jsonObject.addProperty("type", 2);
		
		jsonObject.addProperty("p", 1);
		jsonObject.addProperty("v", "1.0.0");
		jsonObject.addProperty("c", 1);
		jsonObject.addProperty("s", 1);
//		String s =	SecurityFunctions.getSingedValue(jsonObject);
//		jsonObject.addProperty("s", URLEncoder.encode(s,"UTF-8"));
		
		httpGet(jsonObject);
		
	}

	@Test
	public void testChangeUserInfo() throws UnsupportedEncodingException {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("token", "e5d949d62dbc404585421979c7b62311");
		jsonObject.addProperty("FuncTag", 20000102);
		jsonObject.addProperty("userId", 13);
		jsonObject.addProperty("cnUserName", "小王子");
		jsonObject.addProperty("enUserName", "emily");
		jsonObject.addProperty("birthday", 604627200000l);
		jsonObject.addProperty("gender", 0);
		
		jsonObject.addProperty("p", 1);
		jsonObject.addProperty("v", "1.0.0");
		jsonObject.addProperty("c", 1);
		String s =	SecurityFunctions.getSingedValue(jsonObject);
		jsonObject.addProperty("s", s);
		
		httpGet(jsonObject);
	}

	@Test
	public void testLogin() {

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("phoneNum", "18668110387");
		jsonObject.addProperty("FuncTag", 20110103);
		jsonObject.addProperty("loginType", 3);
		jsonObject.addProperty("pwd", "a12345");
		
		jsonObject.addProperty("p", 1);
		jsonObject.addProperty("v", "1.0.0");
		jsonObject.addProperty("c", 1);
		String s =	SecurityFunctions.getSingedValue(jsonObject);
		System.out.println("signed:"+s);
		jsonObject.addProperty("s", "s0~c%c~css0!s50#80");
		
		httpGet(jsonObject);
		
	}
	
	
	@Test
	public void testGetUserInfo() {

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("FuncTag", 20110105);
		jsonObject.addProperty("userId", 12);
		jsonObject.addProperty("token", "3d01ba154bee4f9fad59a2c3c7c09909");
		
		jsonObject.addProperty("p", 1);
		jsonObject.addProperty("v", "1.0.0");
		jsonObject.addProperty("c", 1);
		String s =	SecurityFunctions.getSingedValue(jsonObject);
		jsonObject.addProperty("s", s);
		
		httpGet(jsonObject);
		
	}

	@Test
	public void testRegisterViaPhoneNum() {
		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("phoneNum", "15868188955");
		jsonObject.addProperty("FuncTag", 20110101);
		jsonObject.addProperty("verifyCode", 935497);
		jsonObject.addProperty("pwd", "482694Abc");
		
		jsonObject.addProperty("p", 1);
		jsonObject.addProperty("v", "1.0.0");
		jsonObject.addProperty("c", 1);
//		String s =	SecurityFunctions.getSingedValue(jsonObject);
//		jsonObject.addProperty("s", s);
		
		httpGet(jsonObject);
		
	}

}
