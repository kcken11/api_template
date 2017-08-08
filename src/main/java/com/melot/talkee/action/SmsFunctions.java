package com.melot.talkee.action;


import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.talkee.driver.domain.SmsConfig;
import com.melot.talkee.driver.domain.UserInfo;
import com.melot.talkee.driver.service.TalkCommonService;
import com.melot.talkee.driver.service.TalkUserService;
import com.melot.talkee.redis.SmsRedisSource;
import com.melot.talkee.utils.CommonUtil;
import com.melot.talkee.utils.SmsTypeEnum;
import com.melot.talkee.utils.TagCodeEnum;

public class SmsFunctions {
	
	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(SmsFunctions.class);
	
	/**
	 * 短信发送接口(10110101)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 修改密码结果
	 */
	public JsonObject sendSms(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	     JsonObject result = new JsonObject();
	     String phoneNum;
	     int smsType;
	     try {
			 phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum",null,TagCodeEnum.PHONE_NUM_INCORRECT,Integer.MIN_VALUE, Integer.MAX_VALUE);
			 smsType = CommonUtil.getJsonParamInt(jsonObject, "smsType",0,TagCodeEnum.SMSTYPE_INCORRECT,Integer.MIN_VALUE, Integer.MAX_VALUE);
	     } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
	     } catch (Exception e) {
	        result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
	        return result;
	     }
		 if (phoneNum != null && StringUtils.isNumeric(phoneNum)) {
			 phoneNum = CommonUtil.validatePhoneNum(phoneNum, "86");
		 } else {
			 result.addProperty("TagCode", TagCodeEnum.PHONE_NUM_INCORRECT);
			 return result;
		 }
		 try {
			 if (SmsTypeEnum.isValidType(smsType) == false) {
				 result.addProperty("TagCode", TagCodeEnum.SMSTYPE_INCORRECT);
				 return result;
			 }
			 TalkUserService userService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
			 if (userService == null) {
				 result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
				 return result;
			 }
			 
			 TalkCommonService talkCommonService = MelotBeanFactory.getBean("talkCommonService", TalkCommonService.class);
			 if (talkCommonService == null) {
				 result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
				 return result;
			 }
			 
			 UserInfo userInfo = userService.getUserInfoByPhoneNumber(phoneNum);
			 if (smsType == SmsTypeEnum.REGISTER) {
				if (userInfo != null) {
					result.addProperty("TagCode", TagCodeEnum.PHONE_NUMBER_EXIST);
					return result;
				}
			 }
			 if (smsType == SmsTypeEnum.CHANGE_PWD || smsType == SmsTypeEnum.FIND_PWD) {
				if (userInfo == null) {
					result.addProperty("TagCode", TagCodeEnum.PHONE_NUMBER_NOT_EXIST);
					return result;
				}
			 }
			 
			SmsConfig smsConfig =	talkCommonService.getSmsConfig(smsType, 1);
			if (smsConfig != null) {
				 int dailyCount = smsConfig.getDailyCount().intValue();
		         // 单日短信发送个数限制
		         int todayCount = SmsRedisSource.getSendSmsCount(String.valueOf(smsType), String.valueOf(phoneNum));
		         if (todayCount < dailyCount) {
		            String format = smsConfig.getAdviceTemplate();
		            String verifyCode = CommonUtil.getRandomDigit(6);
		            String message = String.format(format, verifyCode);
		            switch (smsType) {
						case SmsTypeEnum.REGISTER:
							message = String.format(format, verifyCode);
							break;
						case SmsTypeEnum.CHANGE_PWD:
							message = String.format(format, verifyCode);
							break;
						default:
							break;
					}
		            // 保存验证码 
		            // TODO 测试短信不发送，保存验证码5分钟 
		            SmsRedisSource.sendSms(String.valueOf(phoneNum), String.valueOf(smsType), message);
		            SmsRedisSource.createPhoneSmsData(String.valueOf(phoneNum), String.valueOf(smsType), verifyCode, 5*60);
		            
		            result.addProperty("verifyCode", verifyCode);
		            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		         }else{
		        	 result.addProperty("TagCode", TagCodeEnum.SMS_SEND_MAX_COUNT_DAILY);
					 return result;
		         }
			}else {
				 result.addProperty("TagCode", TagCodeEnum.SMSTYPE_INCORRECT);
				 return result;
			}
		 } catch (Exception e) {
			 logger.error("sendSms error, jsonObject:" + jsonObject, e);
			 result.addProperty("TagCode", TagCodeEnum.PHONE_NUMBER_SEND_EXCEPTION);
		 }
		 return result;
	}
	

	/**
	 * 验证码校验(10110102)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 校验结果
	 */
	public JsonObject checkVerifyCode(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	     JsonObject result = new JsonObject();
	     String phoneNum,verifyCode;
	     int smsType;
	     try {
			 phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum",null,TagCodeEnum.PHONE_NUM_INCORRECT,Integer.MIN_VALUE, Integer.MAX_VALUE);
			 smsType = CommonUtil.getJsonParamInt(jsonObject, "smsType",0,TagCodeEnum.SMSTYPE_INCORRECT,Integer.MIN_VALUE, Integer.MAX_VALUE);
			 verifyCode = CommonUtil.getJsonParamString(jsonObject, "verifyCode",null,TagCodeEnum.VERIFY_CODE_INCORRECT,Integer.MIN_VALUE, Integer.MAX_VALUE);
	     } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
	     } catch (Exception e) {
	        result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
	        return result;
	     }
		 if (phoneNum != null && StringUtils.isNumeric(phoneNum)) {
			 phoneNum = CommonUtil.validatePhoneNum(phoneNum, "86");
		 } else {
			 result.addProperty("TagCode", TagCodeEnum.PHONE_NUM_INCORRECT);
			 return result;
		 }
		 try {
			 if (SmsTypeEnum.isValidType(smsType) == false) {
				 result.addProperty("TagCode", TagCodeEnum.SMSTYPE_INCORRECT);
				 return result;
			 }
			 
			 String data = SmsRedisSource.getPhoneSmsData(phoneNum, String.valueOf(smsType));
			 if (StringUtils.isBlank(data) || !data.equals(verifyCode)) {
				 result.addProperty("TagCode", TagCodeEnum.VERIFY_CODE_UNAVAIL);
				 return result;
			 }else{
				 result.addProperty("TagCode", TagCodeEnum.SUCCESS);
				 return result;
			 }
		 } catch (Exception e) {
			 logger.error("sendSms error, jsonObject:" + jsonObject, e);
			 result.addProperty("TagCode", TagCodeEnum.PHONE_NUMBER_SEND_EXCEPTION);
		 }
		 return result;
	}
}
