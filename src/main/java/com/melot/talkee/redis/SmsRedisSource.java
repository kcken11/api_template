package com.melot.talkee.redis;

import com.melot.common.melot_jedis.JedisWrapper;
import com.melot.sdk.core.util.MelotBeanFactory;

import redis.clients.jedis.Jedis;

public class SmsRedisSource {
	
	private static final String SOURCE_BEAN_NAME = "smsRedisSource";
	
	// ["18657134568","测试短信"]
	private static final String SENDSMS_QUEUE_NAME = "talk_sms_send";
	// 当日一个手机号下行短信个数 smsCount_phonenum_smsType
	private static final String SMSCOUNT_KEY_FORMAT = "talk_smsCount_%s";
	// 短信验证码临时存储 用于判断是否有效 smsVerifyCode_phonenum_smsType
	private static final String SMSVERIFYCODE_KEY_FORMAT = "talk_smsVerifyCode_%s_%s";
	
	// 短信通道对应APPID
	private static final int appId = 5;
	
	/**
	 * 获取redis资源
	 * @return
	 */
	private static JedisWrapper getInstance() {
	    return MelotBeanFactory.getBean(SOURCE_BEAN_NAME, JedisWrapper.class);
	}
	
	/**
	 * 获取手机号发送短信总数
	 * @param phoneNum
	 * @return 当天已请求发送短信数量
	 */
	public static int getSendSmsCount(String smsType, String phoneNum) {
		JedisWrapper jedis = null;
		int total = 0;
		try {
			jedis = getInstance();
			String pattern = String.format(SMSCOUNT_KEY_FORMAT, phoneNum);
			String str = jedis.HASH.hget(pattern, smsType);
			if(str!=null) total = Integer.parseInt(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return total;
	}
	
	
	/**
	 * 请求发送短信
	 * @param phonenum 手机号
	 * @param message 短信内容
	 */
	public static void sendSms(String phoneNum, String smsType, String message) {
		boolean errHappend = false;
		Throwable t = null;
		Jedis jedis = null;
		JedisWrapper jedisWrapper = null;
		try {
			jedisWrapper = getInstance();
			if (jedisWrapper != null) {
				jedis = jedisWrapper.getJedis();
				jedis.rpush(SENDSMS_QUEUE_NAME, "[\""+phoneNum+"\",\""+smsType+"\",\""+message+"\",\""+appId+"\"]");
			}else {
				throw new Exception("get jedisWrapper is null");
			}
		} catch (Throwable e) {
			 errHappend = true;
			 t = e;
			 e.printStackTrace();
		}finally{
			if (jedisWrapper != null && jedis != null) {
                if(errHappend){
                	jedisWrapper.returnBrokenJedis(jedis, t);
                }else{
                	jedisWrapper.returnJedis(jedis);
                }
            }
		}
	}
	
	
	
	/**
	 * 创建各业务短信验证码/随机密码等数据
	 * @param phoneNum
	 * @param smsType
	 */
	public static void createPhoneSmsData(String phoneNum, String smsType, String value, int seconds) {

		JedisWrapper jedis = null;
		try {
			jedis = getInstance();
			String pattern = String.format(SMSVERIFYCODE_KEY_FORMAT, phoneNum, smsType);
			jedis.STRINGS.set(pattern, value);
			jedis.KEYS.expire(pattern, seconds);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取各业务短信验证码/随机密码等数据
	 * @param phoneNum
	 * @param smsType
	 * @return
	 */
	public static String getPhoneSmsData(String phoneNum, String smsType) {
		JedisWrapper jedis = null;
		String value = null;
		try {
			jedis = getInstance();
			String pattern = String.format(SMSVERIFYCODE_KEY_FORMAT, phoneNum, smsType);
			value = jedis.STRINGS.get(pattern);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}
}
