package com.melot.talkee.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@SuppressWarnings({"rawtypes","unchecked"})
public class TextFilter {
	
	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(TextFilter.class);
	
	private static int matchType = 1; // 1:最小长度匹配 2：最大长度匹配
	
	private static Map sensitiveWordsMap = null;
	private static String shorturlRegex = null;

    private static Set<String> specUnicodeSet = null;
	
	public static void init(String path) {
	    sensitiveWordsMap = Collections.synchronizedMap(new HashMap());
	    shorturlRegex = "";
	    specUnicodeSet = Collections.synchronizedSet(new HashSet<String>());
	    
		BufferedReader bufferReader = null;
		try {
			bufferReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8")); 
		    String line = null;
		    StringBuffer dataStr = new StringBuffer();
		    while ((line = bufferReader.readLine())!=null) {
		    	dataStr.append(line);
		    }
		    JsonObject dataJson = (JsonObject) new JsonParser().parse(dataStr.toString());
		    JsonArray sensitive_wordsArr = dataJson.get("sensitive_words").getAsJsonArray();
		    for (JsonElement jsonElement : sensitive_wordsArr) {
		    	String key = jsonElement.getAsString().trim();
				Map nowhash = sensitiveWordsMap;
				for (int j = 0; j < key.length(); j++) {
					char word = key.charAt(j);
					Object wordMap = nowhash.get(word);
					if (wordMap != null) {
						nowhash = (HashMap) wordMap;
					} else {
						HashMap<String, String> newWordHash = new HashMap<String, String>();
						newWordHash.put("isEnd", "0");
						nowhash.put(word, newWordHash);
						nowhash = newWordHash;
					}
					if (j == key.length() - 1) {
						nowhash.put("isEnd", "1");
					}
				}
			}
		    shorturlRegex = dataJson.get("shorturl_regex").getAsString();
		    
		    JsonArray specialUnicodewordsArr = dataJson.get("special_unicode").getAsJsonArray();
            for (JsonElement jsonElement : specialUnicodewordsArr) {
                specUnicodeSet.add(jsonElement.getAsString());
            }
		} catch (Exception e) {
			logger.error("Fail to read text filter data file", e);
		} finally {
			try {
				if (bufferReader!=null) bufferReader.close();
			} catch (IOException e) {
				logger.error("Fail to close text filter data file", e);
			}
		}
	}
	
	public void clear() {
		sensitiveWordsMap.clear();
		shorturlRegex = "";
		specUnicodeSet.clear();
	}

	/**
	 * 检查一个字符串从begin位置起开始是否有keyword符合， 如果有符合的keyword值，返回值为匹配keyword的长度，否则返回零
	 * flag 1:最小长度匹配 2：最大长度匹配
	 */
	private static int checkSensitiveWords(String txt, int begin, int flag) {
		Map nowhash = Collections.unmodifiableMap(sensitiveWordsMap);
		int maxMatchRes = 0;
		int res = 0;
		int l = txt.length();
		char word = 0;
		for (int i = begin; i < l; i++) {
			word = txt.charAt(i);
			Object wordMap = nowhash.get(word);
			if (wordMap != null) {
				res++;
				nowhash = (Map) wordMap;
				if (((String) nowhash.get("isEnd")).equals("1")) {
					if (flag == 1) {
						wordMap = null;
						nowhash = null;
						txt = null;
						return res;
					} else {
						maxMatchRes = res;
					}
				}
			} else {
				txt = null;
				nowhash = null;
				return maxMatchRes;
			}
		}
		txt = null;
		nowhash = null;
		return maxMatchRes;
	}

	/**
	 * 返回txt中关键字的列表
	 */
	public static Set<String> getSensitiveWords(String txt) {
		Set set = new HashSet();
		int l = txt.length();
		for (int i = 0; i < l;) {
			int len = checkSensitiveWords(txt, i, matchType);
			if (len > 0) {
				set.add(txt.substring(i, i + len));
				i += len;
			} else {
				i++;
			}
		}
		txt = null;
		return set;
	}

	/**
	 * 仅判断txt中是否有关键字
	 */
	public static boolean containsSensitiveWords(String txt) {
		for (int i = 0; i < txt.length(); i++) {
			int len = checkSensitiveWords(txt, i, 1);
			if (len > 0) {
				return true;
			}
		}
		txt = null;
		return false;
	}

	/**
	 * 是否短连接
	 * @return
	 */
	public static boolean isShortUrl(String txt) {
		Pattern pattern = Pattern.compile(shorturlRegex);
		Matcher matcher = pattern.matcher(txt);
		return matcher.find();
	}
    
    /**
     * 校验是否含有 Unicode 控制字符
     * @param input
     * @return true- 不包含， false - 包含
     */
    public static boolean checkSpecialUnicode(String input) {
        for (String specUnicode : specUnicodeSet) {
            if (input.contains(specUnicode)) {
                return false; 
            }
        }
        
        for (int i = 0; i < input.length(); i++) {
            String string = String.valueOf(input.charAt(i));
            String unicodeString = string2UnicodeNumber(string);
            if (specUnicodeSet.contains(unicodeString)) { 
                return false;
            }
        }
        return true;
    }
    
	/**
	 * 过滤 Unicode 控制字符
	 * @param input
	 * @return 返回去除 Unicode 控制字符后的字符串
	 */
    public static String filterSpecialUnicode(String input) {
        for (String specUnicode : specUnicodeSet) {
            if (input.contains(specUnicode)) {
                input = input.replaceAll(specUnicode, ""); 
            }
        }
        
        StringBuffer output = new StringBuffer();
        for (int i = 0; i < input.length(); i++) {
            String string = String.valueOf(input.charAt(i));
            String unicodeString = string2UnicodeNumber(string);
            if (!specUnicodeSet.contains(unicodeString)) { 
                output.append(string);
            }
        }
        return output.toString();
    }
    
    /**
    * 输出十进制，格式为&#00000; 如“中国”输出：&#20013;&#22269;
    * 
    * @param s
    * @return
    */
    public static String string2UnicodeNumber(String s) {
        try {
            StringBuffer out = new StringBuffer("");
            StringBuffer temp = null;
            StringBuffer number = null;
            byte[] bytes = s.getBytes("unicode");
            for (int i = 2; i < bytes.length - 1; i += 2) {
                temp = new StringBuffer("&#");
                number = new StringBuffer("");
                String str = Integer.toHexString(bytes[i + 1] & 0xff);
                for (int j = str.length(); j < 2; j++) {
                    temp.append("0");
                }
                String str1 = Integer.toHexString(bytes[i] & 0xff);
                // 十进制转化为十六进制，结果为C8。
                // Integer.toHexString(200);
                // 十六进制转化为十进制，结果140。
                // Integer.parseInt("8C",16);
                number.append(str1);
                number.append(str);

                BigInteger bi = new BigInteger(number.toString(), 16);
                String show = bi.toString(10);

                temp.append(show + ";");
                out.append(temp.toString());
            }

            return out.toString();
        } catch (UnsupportedEncodingException e) {
            logger.error("TextFilter.string2UnicodeNumber(" + s + ") execute exception.", e);
            return null;
        }
    }
	
	public static void main(String[] args) {
		init("C:\\Users\\admin\\Desktop/textFilter.data");
//		String txt = "毛主席aｚ.kktv1";
//		System.out.println(TextFilter.containsSensitiveWords(txt));
//		System.out.println(TextFilter.isShortUrl(txt));
		
		String input;
		input = "A&#8206;中国";
		System.out.println(checkSpecialUnicode(input));
		System.out.println(filterSpecialUnicode(input));
	}
	
}
