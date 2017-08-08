package com.melot.talkee.utils;

import java.lang.reflect.Field;

public class SmsTypeEnum {
	/**手机注册*/
	public static final int REGISTER = 1;
	/**修改密码*/
	public static final int CHANGE_PWD = 2;
	/**找回密码*/
	public static final int FIND_PWD = 3;
	/**绑定手机*/
	public static final int BINDING = 4;
	/**解绑手机*/
	public static final int UNBINDING = 5;
	/**手机登录*/
	public static final int LOGIN = 6;
	
	private static final Class<SmsTypeEnum> objClass = SmsTypeEnum.class;
	
	/**
	 * 验证是否有效的smsType
	 * @param smsType
	 * @return
	 */
	public static boolean isValidType(int smsType){
		Field[] fields = objClass.getFields();
        for( Field field : fields ){
        	try {
				if (field.getInt(objClass) == smsType) {
					return true;
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
        }
		return false;
	}
	
}
