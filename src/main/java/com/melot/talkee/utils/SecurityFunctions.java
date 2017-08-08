package com.melot.talkee.utils;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.melot.talkee.driver.utils.PasswordFunctions;

public class SecurityFunctions {
    
//    private static char[] HEX_TAB_WEB = "0bae24dc39f56178".toCharArray();
    private static char[] HEX_TAB_WEB = "s~0!e@5#c$8%r^6&".toCharArray();
    
    private static char[] HEX_TAB_WEB1 = "K2DEZMIU7OP13YJ8".toCharArray();
	
	//a23883ee574dc53f
	private static byte[] SDMAP = "01239a48bcd567ef".getBytes();
	
	public static void init(String hexTabWeb, String hexTagWeb1, String sdmap, String sPrivateKey) {
	    if (hexTabWeb != null) {
            HEX_TAB_WEB = hexTabWeb.toCharArray();
        }
	    
	    if (hexTagWeb1 != null) {
            HEX_TAB_WEB1 = hexTagWeb1.toCharArray();
        }
	    
	    if (sdmap != null) {
	        SDMAP = sdmap.getBytes();
        }
	    
	    if (sPrivateKey != null) {
            S_PRIVATE_KEY = sPrivateKey;
        }
	}
	
	public static String signDeviceUID(String deviceUID){
		if(deviceUID == null || deviceUID.length() <= 0)
		    return null;
		int len = deviceUID.length();
		int sum = 0;
		for(int i=0; i<len; i+=2){
			char c = deviceUID.charAt(i);
			sum += c;
		}
		deviceUID += (char)SDMAP[sum%16];
		sum += deviceUID.charAt(0);
		sum += deviceUID.charAt(len>8?8:len-1);
		deviceUID += (char)SDMAP[sum%16];
		return deviceUID;
	}

	public static boolean checkDeviceUID(String deviceUID){
		if(deviceUID == null || deviceUID.length() <= 2)
			return false;
		int len = deviceUID.length();
		int sum = 0;
		for(int i=0; i<len-2; i+=2){
			char c = deviceUID.charAt(i);
			sum += c;
		}
		if(deviceUID.charAt(len-2) != (char)SDMAP[sum%16])
			return false;
		sum += deviceUID.charAt(0);
		sum += deviceUID.charAt(len>10?8:len-3);
		if(deviceUID.charAt(len-1) != (char)SDMAP[sum%16])
			return false;
		return true;
	}
	
	public static String getSingedValue(JsonObject jsonObject) {
		//根据不同的platform,使用不同的验证方法
		JsonElement platformje = jsonObject.get("p");
		int platform = -1;
		if (platformje != null) {
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
			}
		}
		if (PlatformEnum.isValid(platform) == false) {
			return null;
		}
		
		Set<Entry<String, JsonElement>> es = jsonObject.entrySet();
		Iterator<Entry<String, JsonElement>> ies = es.iterator();
		//构造验签参数
		String sv = null;
		HashMap<String, Object> params = new  HashMap<String, Object>();
		while(ies.hasNext()){
			Entry<String, JsonElement> e = ies.next();
			if(!e.getKey().equalsIgnoreCase("s"))
				params.put(e.getKey(), e.getValue().getAsString());
		}
		if (platform == 1){
			//使用Web验证方式
			sv = SecurityFunctions.slist_web(params);
			int sum = 0;
			for(int i=0; i<sv.length(); i+=2){
				sum += sv.charAt(i);
			}
			char a = HEX_TAB_WEB[sum%16];
			char b = HEX_TAB_WEB[sum%13];
			sv = sv + a + b;
		}
		else{
			//使用非Web验证方式
			sv = SecurityFunctions.slist(params);
		}
		return sv;
	}
	
	public static String checkSignedValue(JsonObject jsonObject){
		
		int platform = -1;
		// 提取验证platform
		JsonElement platformObj = jsonObject.get("p");
		if (platformObj == null) {
			return TagCodeEnum.PLATFORM_INCORRECT;
		}
		try {
			platform = platformObj.getAsInt();
		} catch (Exception e) {
			return TagCodeEnum.PLATFORM_INCORRECT;
		}
		int channel = 0;
		// 提取验证channel
		JsonElement channelObj = jsonObject.get("c");
		if (channelObj == null) {
			return TagCodeEnum.CHANNEL_INCORRECT;
		}
		try {
			channel = channelObj.getAsInt();
		} catch (Exception e) {
			return TagCodeEnum.CHANNEL_INCORRECT;
		}
		String version = null;
		// 提取验证version
		JsonElement versionObj = jsonObject.get("v");
		if (versionObj == null) {
			return TagCodeEnum.VERSION_INCORRECT;
		}
		try {
			version = versionObj.getAsString();
		} catch (Exception e) {
			return TagCodeEnum.VERSION_INCORRECT;
		}
		
		// 提取验证sign
		String sign = null;
		JsonElement signObj = jsonObject.get("s");
		if (signObj == null) {
			return TagCodeEnum.SIGN_INCORRECT;
		}
		try {
			sign = signObj.getAsString();
		} catch (Exception e) {
			return TagCodeEnum.SIGN_INCORRECT;
		}
		// platform无效
		if (PlatformEnum.isValid(platform) == false) {
			return TagCodeEnum.PLATFORM_INCORRECT;
		}
		// channel无效
		if (ChannelEnum.isValid(channel) == false) {
			return TagCodeEnum.CHANNEL_INCORRECT;
		}
		
		Set<Entry<String, JsonElement>> es = jsonObject.entrySet();
		Iterator<Entry<String, JsonElement>> ies = es.iterator();
		//构造验签参数
		HashMap<String, Object> params = new  HashMap<String, Object>();
		while(ies.hasNext()){
			Entry<String, JsonElement> e = ies.next();
			if(!e.getKey().equalsIgnoreCase("s")){
				 if (e.getValue().isJsonObject()) {
					 JsonObject object = e.getValue().getAsJsonObject();
					 params.put(e.getKey(), new Gson().toJson(object));
                 }else{
					 params.put(e.getKey(), e.getValue().getAsString());
				}
			}
		}
		
		
		boolean b = false;
		// web端调用(1): PlatformEnum.WEB
		if (platform == 1){
			//使用Web验证方式
			b = SecurityFunctions.cslist_web(params, sign);
		} else{
			//使用非Web验证方式
			b = SecurityFunctions.cslist(params, sign);
		}
		if(b == false){
			return TagCodeEnum.SIGN_INCORRECT;
		}
		
		return null;
	}
	
	public static String decodeUserNameAndPassword(JsonObject jsonObject) {
		//根据不同的platform,使用不同的验证方法
		JsonElement platformje = jsonObject.get("p");
		int platform = -1;
		if (platformje != null) {
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
			}
		}
		if (PlatformEnum.isValid(platform) == false) {
			return null;
		}
		
		String upDecoded = null;
		JsonElement up = jsonObject.get("up");
		if(up != null){
		    // web端调用(1): PlatformEnum.WEB
			if (platform == 1){
				//使用Web
				upDecoded = SecurityFunctions.upd_web(up.getAsString());
			}
			else{
				//使用非Web方式
				upDecoded = PasswordFunctions.upd(up.getAsString());
			}
		}
		
		return upDecoded;		
	}
	
	public static String decodeUUID(JsonObject jsonObject){
		//根据不同的platform,使用不同的验证方法
		JsonElement platformje = jsonObject.get("p");
		int platform = -1;
		if (platformje != null) {
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
			}
		}
		if (PlatformEnum.isValid(platform) == false) {
			return null;
		}
		
		String uuidDecoded = null;
		JsonElement uuid = jsonObject.get("uuid");
		if(uuid != null){
		    // web端调用(1): PlatformEnum.WEB
			if (platform == 1){
				return uuid.getAsString();//web will not encode the uuid, since it is called from server
			}
			else{
				//使用非Web方式
				uuidDecoded = PasswordFunctions.upd(uuid.getAsString());
			}
		}
		
		if(uuidDecoded != null){
			int u = uuidDecoded.indexOf("uuid=");
			if(u != 0)
				return null;
			uuidDecoded = uuidDecoded.substring(5, uuidDecoded.length());
		}
		
		return uuidDecoded;		
	}
	
	/**
	 * Only For Weixin Login/Register
	 * @param jsonObject
	 * @return
	 */
	public static String decodeUnionId(JsonObject jsonObject){

		//根据不同的platform,使用不同的验证方法
		JsonElement platformje = jsonObject.get("p");
		int platform = -1;
		if (platformje != null) {
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
			}
		}
		if (PlatformEnum.isValid(platform) == false) {
			return null;
		}
		
		String unionidDecoded = null;
		JsonElement unionid = jsonObject.get("unionid");
		if(unionid != null){
		    // web端调用(1): PlatformEnum.WEB
			if (platform == 1){
				return unionid.getAsString();//web will not encode the unionid, since it is called from server
			}
			else{
				//使用非Web方式
				unionidDecoded = PasswordFunctions.upd(unionid.getAsString());
			}
		}
		
		if(unionidDecoded != null){
			int u = unionidDecoded.indexOf("unionid=");
			if(u != 0)
				return null;
			unionidDecoded = unionidDecoded.substring(8, unionidDecoded.length());
		}
		
		return unionidDecoded;		
	}
	
	public static String decodeNewPassword(JsonObject jsonObject){
		//根据不同的platform,使用不同的验证方法
		JsonElement platformje = jsonObject.get("p");
		int platform = -1;
		if (platformje != null) {
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
			}
		}
		if (PlatformEnum.isValid(platform) == false) {
			return null;
		}
		
		String dpDecoded = null;
		JsonElement dp = jsonObject.get("dp");
		if(dp != null){
		    // web端调用(1): PlatformEnum.WEB
			if (platform == 1){
				dpDecoded = SecurityFunctions.upd_web(dp.getAsString());
			}
			else{
				//使用非Web方式
				dpDecoded = PasswordFunctions.upd(dp.getAsString());
			}
		}
		
		if(dpDecoded != null){
			int u = dpDecoded.indexOf("dp=");
			if(u != 0)
				return null;
			dpDecoded = dpDecoded.substring(3, dpDecoded.length());
		}
		
		return dpDecoded;
	}
	
	
	//*********************************************************************************************************
	//following is for web
	//private static char[] HEX_TAB_WEB = "0bae24dc39f56178".toCharArray();
	//private static char[] HEX_TAB_WEB1 = "K2DEZMIU7OP13YJ8".toCharArray();

	public static String slist_web(Map<String, Object> l){
		if(l == null || l.size() == 0)
			return null;
		try{
			Set<String> ks = l.keySet();
			//sort the keys, and do a slight order change
			String[] kss = new String[ks.size()];  
			ks.toArray(kss);
			Arrays.sort(kss, String.CASE_INSENSITIVE_ORDER);
			
			//create the string to be signed
			String stos = "";
			for(int i=0; i<kss.length; i++){
				Object o = l.get(kss[i]);
				if(o != null){
					stos += o.toString();
				}
			}
			if(stos.length() < 8){
				stos += "0123456789012345";
			}

			int[] s1= new int[8];
			for(int i = 0; i< stos.length() / 8 -1; i++){
				for(int j=0; j<8; j++){
					if((i+1)*8 +j < stos.length()){
						if(i == 0)
							s1[j] = stos.charAt(i*8 +j);
						s1[j] ^= stos.charAt((i+1)*8 +j); 
					}
				}
			}
			
			//encode the bytes to string
			String s0 = "";
			for(int i = 0; i < 8; i++){
				s0 += HEX_TAB_WEB[((s1[i] >>> 3) & 0xF)];
				s0 += HEX_TAB_WEB[(s1[i] & 0xF)];
			}
			return s0;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean cs_web(String s){
		if(s.length() < 2)
			return false;
		int sum = 0;
		for(int i=0; i<s.length()-2; i+=2){
			sum += s.charAt(i);
		}
		
		char a = HEX_TAB_WEB[sum%16];
		char b = HEX_TAB_WEB[sum%13];
		if(a == s.charAt(s.length() - 2) &&
			b == s.charAt(s.length() - 1))
			return true;
		return false;
	}

	public static boolean cslist_web(Map<String, Object> l, String s){
		if(s == null || s.isEmpty())
			return false;
		if(false == cs_web(s))
			return false; 
		String sm = SecurityFunctions.slist_web(l);
		if(sm == null || sm.isEmpty())
			return false;
		s = s.substring(0, s.length() - 2);
		if(sm.compareTo(s) == 0)
			return true;
		return false;
	}
	
	private static int getUPIndexOf(char a){
		int n = -1;
		for(int j = 0; j<16; j++){
			if(a == HEX_TAB_WEB1[j]){
				n = j; 
				break;
			}
		}
		return n;
	}
	
	public static String upd_web(String up){
		if(up == null || up.length() < 4 || up.length() %2 !=0)
			return null;
		int len = up.length()/2;
		char[] s1 = new char[len];
		int n,m;
		int j = 0;
		for(int i=0; i+1<up.length(); i+=2){
			n = getUPIndexOf(up.charAt(i));
			m = getUPIndexOf(up.charAt(i+1));
			if(n == -1 || m == -1)
				return null;
			s1[j++] = (char)(n << 4 | m);
		}
		int sum = s1[len - 1];
		for(int i=0; i<len -1; i++){
			s1[i] ^= sum;
		}
		String s2 = new String(s1);
		s2 = s2.substring(0, s2.length() -1);//remove the last sum char
		return s2;
	}
	
	//*********************************************************************************************************
	//following is for non web
	
	//TODO: get the S_PRIVATE_KEY value from config file
	public static String S_PRIVATE_KEY = "cc16be4b:346c51d";
	//slist is used to sign params in a map
	//
	public static String slist(Map<String, Object> l){
		if(l == null || l.size() == 0)
			return null;
		try{
			Set<String> ks = l.keySet();
			//sort the keys, and do a slight order change
			String[] kss = new String[ks.size()];  
			ks.toArray(kss);
			Arrays.sort(kss, String.CASE_INSENSITIVE_ORDER);
			//create the string to be signed
			String stos = "";
			for(int i=0; i<kss.length; i++){
				stos += kss[i] + ":";
				Object o = l.get(kss[i]);
				if(o != null){
					stos += o.toString();
				}
			}
			//append the private key
			stos += S_PRIVATE_KEY;
			//md5 the string 
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(stos.getBytes());
			byte[] tmp= md.digest();// MD5 的计算结果是一个 128 位的长整数，
			//encode the md5 value to string
			byte[] rs = new byte[26];
			BytesTransformer bts = new BytesTransformer(BytesTransformer.EBT_FOR_SIGN);
			if(bts.encode(tmp, tmp.length, rs, 26))
				return new String(rs);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean cslist(Map<String, Object> l, String s){
		if(s == null || s.isEmpty())
			return false;
		String sm = SecurityFunctions.slist(l);
		if(sm == null || sm.isEmpty())
			return false;
		if(sm.compareTo(s) == 0)
			return true;
		return false;
	}
	
	/**
	 * 解析android系统底层核心信息（用于识别模拟器）
	 * @param jsonObject
	 * @return
	 */
	public static String decodeED(JsonObject jsonObject){
	    String edDecoded = null;
	    try {
	        //根据不同的platform,使用不同的验证方法
	        JsonElement platformje = jsonObject.get("p");
	        int platform = -1;
	        if (platformje != null) {
	            try {
	                platform = platformje.getAsInt();
	            } catch (Exception e) {
	            }
	        }

	        if (PlatformEnum.isValid(platform) == false) {
				return null;
			}
	        
	        JsonElement ed = jsonObject.get("ed");
	        if(ed != null){
	            // web端调用(1): PlatformEnum.WEB
	            if (platform == 1){
	                return ed.getAsString();//web will not encode the uuid, since it is called from server
	            }
	            else{
	                //使用非Web方式
	                edDecoded = PasswordFunctions.upd(ed.getAsString());
	            }
	        }
	        
	        if(edDecoded != null){
	            int u = edDecoded.indexOf("ed=");
	            if(u != 0)
	                return null;
	            edDecoded = edDecoded.substring(3, edDecoded.length());
	        }
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return edDecoded;		
	}
	
}
