package com.melot.talkee.action;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.melot.talkee.utils.SecurityFunctions;

public class FileFunctionsTest {
	
	private Logger logger = Logger.getLogger(FileFunctionsTest.class);
		
	
	private static	String url ="http://10.0.3.46:8080/talkee/entrance";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testUpload() throws UnsupportedEncodingException {
		 File file = new File("F:/456.png");
	     PostMethod filePost = new PostMethod(url);
	     HttpClient client = new HttpClient();
	     
	     try {
	         // 通过以下方法可以模拟页面参数提交
	    	 
	    	 JsonObject jsonObject = new JsonObject();
	 		 jsonObject.addProperty("token", "1885b71b604a4d4ebef84d1ffda6e613");
	 		 jsonObject.addProperty("userId", 5);
	 		 jsonObject.addProperty("FuncTag", 30110101);
	 		 jsonObject.addProperty("fileType", 1);
	 		 jsonObject.addProperty("lessonId", 1);
	 		 
	 		 jsonObject.addProperty("p", 1);
			 jsonObject.addProperty("v", "1.0.1");
			 jsonObject.addProperty("c", 1);
			 String s =	SecurityFunctions.getSingedValue(jsonObject);
			 jsonObject.addProperty("s", s);
	
			 System.out.println(new Gson().toJson(jsonObject));
			 
			 File file1 = new File("F:/1.png");
			 File file2 = new File("F:/2.png");
			 File file3 = new File("F:/3.png");
			 File file4 = new File("F:/4.png");
			 File file5 = new File("D:/test.png");
			 File file6 = new File("F:/6.png");
			 
			 
	         Part[] parts = { 
//	        		 new FilePart(file1.getName(), file1),
//	        		 new FilePart(file2.getName(),  file2),
//	        		 new FilePart(file3.getName(),  file3),
//	        		 new FilePart(file4.getName(),  file4) ,
	        		 new FilePart(file5.getName(), file5) ,
//	        		 new FilePart(file6.getName(),  file6) ,
	        		 new StringPart("parameter", new Gson().toJson(jsonObject))};
	         filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
	         
	         client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
	         
	         int status = client.executeMethod(filePost);
	         byte[] bytes =	filePost.getResponseBody();
	         String retValue = new String(bytes,"UTF-8");
			 System.out.println("responseJson : "+retValue);
	         if (status == HttpStatus.SC_OK) {
	             System.out.println("上传成功");
	         } else {
	             System.out.println("上传失败");
	         }
	     } catch (Exception ex) {
	         ex.printStackTrace();
	     } finally {
	         filePost.releaseConnection();
	     }
	}
}
