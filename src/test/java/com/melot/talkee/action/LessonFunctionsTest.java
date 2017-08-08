package com.melot.talkee.action;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.melot.talkee.utils.DateUtil;
import com.melot.talkee.utils.SecurityFunctions;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LessonFunctionsTest {

	private static	HttpClient httpClient = null;

	private static	String url ="http://10.0.3.56:9090/talkee/entrance";
//	private static	String url ="http://127.0.0.1:8080/talkee/entrance";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		httpClient = new HttpClient();
	}


	@Test
	public void testGetParentLevelList() throws UnsupportedEncodingException {

		PostMethod filePost = new PostMethod(url);
		HttpClient client = new HttpClient();
		try {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("token", "1fc163c3754046b997b453e49d97868c");
			jsonObject.addProperty("userId", 253);
			jsonObject.addProperty("FuncTag", 40000405);
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


	@Test
	public void testGetOrderLesson(){
		PostMethod filePost = new PostMethod(url);
		HttpClient client = new HttpClient();
		try {

			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("token", "cafd2b28f41a468c92d1ed33295677a3");
			jsonObject.addProperty("userId", 409);
			jsonObject.addProperty("FuncTag", 40000204);
			jsonObject.addProperty("publishType", 1);
			jsonObject.addProperty("queryState", 8);

			jsonObject.addProperty("beginTime", 0L);
			jsonObject.addProperty("endTime", 0L);
			jsonObject.addProperty("start", 1);
			jsonObject.addProperty("offset", 10);
			jsonObject.addProperty("order", "desc");

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

			client.executeMethod(filePost);
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
	public void testGetAwaitLessonList(){
		PostMethod filePost = new PostMethod(url);
		HttpClient client = new HttpClient();
		try {

			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("token", "cdb81a608a304c10bc807b06d97ae734");
			jsonObject.addProperty("userId", 409);
			jsonObject.addProperty("FuncTag", 40000205);
			jsonObject.addProperty("publishType", 1);
			jsonObject.addProperty("beginTime", 0L);
			jsonObject.addProperty("endTime", 0L);
			jsonObject.addProperty("start", 1);
			jsonObject.addProperty("offset", 10);
			jsonObject.addProperty("order", "desc");

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

	@Test
	public void testUpdateStudentCancelType(){
		PostMethod filePost = new PostMethod(url);
		HttpClient client = new HttpClient();
		try {

			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("token", "cafd2b28f41a468c92d1ed33295677a3");
			jsonObject.addProperty("userId", 409);
			jsonObject.addProperty("FuncTag", 40000206);
			jsonObject.addProperty("histId", 1831);

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




	@Test
	public void testGetLessonInfoList() {
		PostMethod filePost = new PostMethod(url);
		HttpClient client = new HttpClient();
		try {

			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("token", "991b982a1b9941beb629f3e995d5acb3");
			jsonObject.addProperty("userId", 253);
			jsonObject.addProperty("FuncTag", 40000406);
			jsonObject.addProperty("lessonLevel", 5);
			jsonObject.addProperty("start", 1);
			jsonObject.addProperty("offset", 10);
			jsonObject.addProperty("order", "desc");

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
	//老师评价发布
	@Test
	public void testCreateTeacherComment() {
		PostMethod filePost = new PostMethod(url);
		HttpClient client = new HttpClient();
		try {

			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("token", "59216631c7f14c7fb74497ed0c75cf89");
			jsonObject.addProperty("userId", 645);
			jsonObject.addProperty("histId", 9);
			jsonObject.addProperty("teacherId", 645);
			jsonObject.addProperty("FuncTag", 40000407);
			jsonObject.addProperty("periodId", 2543);
			jsonObject.addProperty("studentId", 58);

			jsonObject.addProperty("pronunciation", 5);
			jsonObject.addProperty("participation", 5);
			jsonObject.addProperty("comprehension", 5);
			jsonObject.addProperty("fluency", 4);
			jsonObject.addProperty("creativity", 5);

			jsonObject.addProperty("summary", "222");
			jsonObject.addProperty("suggestion", "222");
			jsonObject.addProperty("other", "22");


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
	//学生查看老师评价
	@Test
	public void testGetTeacherCommentInfo() {
		PostMethod filePost = new PostMethod(url);
		HttpClient client = new HttpClient();
		try {

			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("token", "30217f0065cd4d44bd84585216883650");
			jsonObject.addProperty("userId", 290);
			jsonObject.addProperty("teacherId", 22);
			jsonObject.addProperty("FuncTag", 40000408);
			jsonObject.addProperty("periodId", 2545);
			jsonObject.addProperty("studentId", 290);

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

	//学生对老师的评价
	@Test
	public void testCreateStudentComment() {
		PostMethod filePost = new PostMethod(url);
		HttpClient client = new HttpClient();
		try {

			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("token", "59216631c7f14c7fb74497ed0c75cf89");
			jsonObject.addProperty("userId", 645);
			jsonObject.addProperty("FuncTag", 40000409);
			jsonObject.addProperty("studentId", 645);
			jsonObject.addProperty("periodId", 3591);
			jsonObject.addProperty("teacherId", 253);

			jsonObject.addProperty("videoSharpness", 4);
			jsonObject.addProperty("soundArticulation", 4);
			jsonObject.addProperty("atmosphere", 4);
			jsonObject.addProperty("interaction", 5);

			jsonObject.addProperty("requireIds", "1,2,3");


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

	//老师查看学生评价
	@Test
	public void testGetStudentCommentInfo() {
		PostMethod filePost = new PostMethod(url);
		HttpClient client = new HttpClient();
		try {

			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("token", "b46b3fd2d46947e49ce0d0ac0295f219");
			jsonObject.addProperty("userId", 22);
			jsonObject.addProperty("teacherId", 22);
			jsonObject.addProperty("FuncTag", 40000410);
			jsonObject.addProperty("periodId", 2545);
			jsonObject.addProperty("studentId", 290);

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


}
