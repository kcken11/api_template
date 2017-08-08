package com.melot.talkee.utils;

import java.lang.reflect.Field;

public class LoginTypeEnum {

	/**账号登录*/
	public static final int ACCOUNT = 1;
	
	/**邮箱登录*/
	public static final int EMAIL = 2;
	
	/**手机号码登录*/
	public static final int PHONE_NUMBER = 3;
	
	private static final Class<LoginTypeEnum> objClass = LoginTypeEnum.class;
	
	/**
	 * 验证是否有效的
	 * @param smsType
	 * @return
	 */
	public static boolean isValid(int loginType){
		Field[] fields = objClass.getFields();
        for( Field field : fields ){
        	try {
				if (field.getInt(objClass) == loginType) {
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
