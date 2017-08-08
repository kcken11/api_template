package com.melot.talkee.utils;

import java.lang.reflect.Field;

public class PlatformEnum {

	/**web*/
	public static final int WEB = 1;
	
	/**ANDROID*/
	public static final int ANDROID = 2;

	/**iphone*/
	public static final int IPHONE = 3;
	
	/**android_pad*/
	public static final int ANDROID_PAD = 4;
	
	/**ipad*/
	public static final int IPAD = 5;
	
	/**WINDOWS*/
	public static final int WINDOWS = 6;
	
	/**MAC*/
	public static final int MAC = 7;
	
	private static final Class<PlatformEnum> objClass = PlatformEnum.class;
	
	/**
	 * 验证是否有效
	 * @param platform
	 * @return
	 */
	public static boolean isValid(int platform){
		Field[] fields = objClass.getFields();
        for( Field field : fields ){
        	try {
				if (field.getInt(objClass) == platform) {
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
