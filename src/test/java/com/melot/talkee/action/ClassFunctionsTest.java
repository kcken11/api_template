package com.melot.talkee.action;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.melot.talkee.utils.SecurityFunctions;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.UnsupportedEncodingException;

public class ClassFunctionsTest {

    private Logger logger = Logger.getLogger(ClassFunctionsTest.class);



  private static String url = "http://10.0.3.56:9090/talkee/entrance";
   // private static String url = "http://127.0.0.1:8080/talkee/entrance";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Test
    public void testClassInfo() throws UnsupportedEncodingException {

        PostMethod filePost = new PostMethod(url);
        HttpClient client = new HttpClient();

        try {
            // 通过以下方法可以模拟页面参数提交

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("token", "75b0219d0c4c4aa298aaa731b66c0caf");
            jsonObject.addProperty("userId", 27);
            jsonObject.addProperty("FuncTag", 50000105);
            jsonObject.addProperty("periodId", 6715);

            jsonObject.addProperty("p", 5);
            jsonObject.addProperty("v", "1.0.1");
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

    @Test
    public void testClassIn() throws UnsupportedEncodingException {

        PostMethod filePost = new PostMethod(url);
        HttpClient client = new HttpClient();

        try {
            // 通过以下方法可以模拟页面参数提交

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("token", "1fc163c3754046b997b453e49d97868c");
            jsonObject.addProperty("userId", 253);
            jsonObject.addProperty("FuncTag", 50000101);
            jsonObject.addProperty("periodId", 4392);
            jsonObject.addProperty("deviceUid", "ios");
            jsonObject.addProperty("enterType", 1);
            jsonObject.addProperty("roleType", 1);

            jsonObject.addProperty("p", 1);
            jsonObject.addProperty("v", "1.0.1");
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

    @Test
    public void testClassOut() throws UnsupportedEncodingException {

        PostMethod filePost = new PostMethod(url);
        HttpClient client = new HttpClient();

        try {
            // 通过以下方法可以模拟页面参数提交

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("token", "1fc163c3754046b997b453e49d97868c");
            jsonObject.addProperty("userId", 253);
            jsonObject.addProperty("FuncTag", 50000102);
            jsonObject.addProperty("periodId", 4392);
            jsonObject.addProperty("deviceUid", "ios");
            jsonObject.addProperty("exitType", 1);
            jsonObject.addProperty("roleType", 1);

            jsonObject.addProperty("p", 1);
            jsonObject.addProperty("v", "1.0.1");
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

    @Test
    public void testWriteWhiteBoard() throws UnsupportedEncodingException {

        PostMethod filePost = new PostMethod(url);
        HttpClient client = new HttpClient();



        try {
            // 通过以下方法可以模拟页面参数提交

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("FuncTag", 50110103);
            jsonObject.addProperty("periodId", 2987);
            jsonObject.addProperty("segment", 1);
           /* String s = SecurityFunctions.getSingedValue(jsonObject);
            jsonObject.addProperty("s", s);*/

            System.out.println(new Gson().toJson(jsonObject));
            StringBuffer buffer =new StringBuffer("abcdefg");
            for (int i = 0; i <18 ; i++) {
                    buffer.append(buffer.toString());
            }
            jsonObject.addProperty("messageData", buffer.toString());
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
    @Test
    public void testReadboardRecord() throws UnsupportedEncodingException {

        PostMethod filePost = new PostMethod(url);
        HttpClient client = new HttpClient();



        try {
            // 通过以下方法可以模拟页面参数提交

            JsonObject jsonObject = new JsonObject();
//            jsonObject.addProperty("token", "b31be66845fa49a1acae9e82acdb45a5");
//            jsonObject.addProperty("userId", 253);
            jsonObject.addProperty("FuncTag", 50110104);
            jsonObject.addProperty("periodId", 2987);
            jsonObject.addProperty("segment", 1);
         /*   jsonObject.addProperty("p", 1);
            jsonObject.addProperty("v", "1.0.1");
            jsonObject.addProperty("c", 1);*/
           /* String s = SecurityFunctions.getSingedValue(jsonObject);
            jsonObject.addProperty("s", s);*/

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

    @Test
    public void testRecordInfo() throws UnsupportedEncodingException {

        PostMethod filePost = new PostMethod(url);
        HttpClient client = new HttpClient();



        try {
            // 通过以下方法可以模拟页面参数提交

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("token", "75b0219d0c4c4aa298aaa731b66c0caf");
//            jsonObject.addProperty("userId", 253);
            jsonObject.addProperty("FuncTag", 50000106);
            jsonObject.addProperty("periodId", 6709);
            jsonObject.addProperty("userId", 27);
           jsonObject.addProperty("p", 5);
            jsonObject.addProperty("v", "1.0.1");
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
}
