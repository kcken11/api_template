package com.melot.talkee.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Hadoop统计
 * @author Administrator
 *
 */
public class HadoopLogger {

	private static Logger hadoopLogger = Logger.getLogger("hadoopLogger");
	
	/**
	 * HadoopLogger 埋点
	 * @param logInfo 日子信息
	 */
	public static void log(String logInfo) {
	    if (StringUtils.isNotBlank(logInfo)) {
	        hadoopLogger.info(logInfo);
        }
	}
	
	/**
	 * 登录日志
	 * @param userId
	 * @param date
	 * @param platform
	 * @param channel
	 * @param version
	 * @param route
	 * @param ip
	 * @param loginType
	 * @param ed
	 */
	public static void loginLog(int userId, Date date, int platform,int channel,String version,String route,String ip,int port, int loginType,String deviceUId,String ed) {
		String dateString = null;
		try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateString = dateFormat.format(date);
        } catch (Exception e) {}
		
		hadoopLogger.info("user_log_v2:"
				+ (userId > 0 ? userId + "" : "") + "^" 
				+ (dateString == null ? "" : dateString + "") + "^"
				+ (platform > 0 ? platform + "" : "") + "^"
				+ (channel > 0 ? channel + "" : "") + "^"
				+ (StringUtils.isNotBlank(version) ? version : "") + "^"
				+ (StringUtils.isNotBlank(route) ? route : "") + "^"
				+ (ip == null ? "" : ip + "") + "^"
				+ (port > 0 ? port + "" : "") + "^"
				+ loginType + "^"
				+ (StringUtils.isNotBlank(deviceUId) ? deviceUId : "") + "^"
				+ (ed == null ? "" : ed + "") + "^");
	}
	
	/**
	 * 注册日志
	 * @param userId
	 * @param date
	 * @param terminal
	 * @param openplatform
	 * @param referrerId
	 * @param ip
	 * @param qudaoId
	 * @param deviceUId
	 * @param inviter
	 * @param appId
	 * @param ed android模拟器信息
	 */
	public static void registerLog(int userId, Date date, int platform,int channel,String version,String route,String ip,int port, int registerType, int referrerId,
			int terminal, String deviceUId, int inviter, String ed) {
		String dateString = null;
		try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateString = dateFormat.format(date);
        } catch (Exception e) {}
		
		hadoopLogger.info("user_info_v1:" 
		        + (userId > 0 ? userId + "" : "")
                + "^" + (dateString == null ? "" : dateString + "") + "^"
                + (platform > 0 ? platform + "" : "") + "^"
				+ (channel > 0 ? channel + "" : "") + "^"
				+ (StringUtils.isNotBlank(version) ? version : "") + "^"
				+ (StringUtils.isNotBlank(route) ? route : "") + "^"
				 + (ip == null ? "" : ip + "") + "^"
				+ (port > 0 ? port + "" : "") + "^"
                + (terminal > 0 ? terminal + "" : "") + "^" + registerType
                + "^" + (referrerId > 0 ? referrerId + "" : "") + "^"
                + (deviceUId == null ? "" : deviceUId + "") + "^"
                + (inviter > 0 ? inviter + "" : "") + "^"
                + (ed == null ? "" : ed + "") + "^");
	}
	
	/**
	 * 安装包安装日志
	 * @param userId
	 * @param versioncode
	 * @param channel
	 * @param date
	 * @param installorder
	 * @param platform
	 * @param appId
	 */
	public static void installLog(int userId, int versioncode, int channel, Date date, int installorder, int platform, Map<String, Object> otherInfo) {
		String dateString = "";
		try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateString = dateFormat.format(date);
        } catch (Exception e) {}
		
		String deviceUId = "";
		if (otherInfo != null && otherInfo.containsKey("deviceUId")) {
		    deviceUId = (String) otherInfo.get("deviceUId");
        }
		
		hadoopLogger.info("user_install_pack_v1:"
                + (userId > 0 ? userId + "" : "") + "^"
                + (versioncode > 0 ? versioncode + "" : "") + "^"
                + (channel > 0 ? channel + "" : "") + "^"
                + (dateString == null ? "" : dateString + "") + "^"
                + (installorder > 0 ? installorder + "" : "") + "^"
                + (platform > 0 ? platform + "" : "") + "^"
                + deviceUId + "^");
	}
	
	/**
	 * APP渠道推广请求日志
	 * @param ip
	 * @param ua
	 * @param channel
	 * @param date
	 * @param appId
	 */
	public static void promoteAccessLog(String ip, String ua, int channel, Date date, int platform) {
		String dateString = null;
		try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateString = dateFormat.format(date);
        } catch (Exception e) {}
		
		hadoopLogger.info("app_promote_access_v1:"
				+ (ip == null ? "" : ip + "") + "^"
				+ (ua == null ? "" : ua + "") + "^"
				+ (channel > 0 ? channel + "" : "") + "^"
				+ (dateString == null ? "" : dateString + "") + "^"
				+ (platform > 0 ? platform + "" : "") + "^");
	}
	
	/**
	 * APP渠道推广激活日志
	 * @param userId
	 * @param ip
	 * @param ua
	 * @param channel
	 * @param terminal
	 * @param qudaoId
	 * @param date
	 * @param appId
	 */
	public static void promoteActiveLog(int userId, String ip, String ua, int channel, int terminal, int qudaoId, Date date) {

		String dateString = null;
		try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateString = dateFormat.format(date);
        } catch (Exception e) {}
		
		hadoopLogger.info("app_promote_active_v1:"
				+ (userId > 0 ? userId + "" : "") + "^" 
				+ (ip == null ? "" : ip + "") + "^"
				+ (ua == null ? "" : ua + "") + "^"
				+ (channel > 0 ? channel + "" : "") + "^"
				+ (terminal > 0 ? terminal + "" : "") + "^"
				+ (qudaoId > 0 ? qudaoId + "" : "") + "^"
				+ (dateString == null ? "" : dateString + "") + "^");
	
	}
	
	/**
	 * 购买会员日志
	 * @param userId
	 * @param referrerId
	 * @param propId
	 * @param periodOfValidity
	 * @param buyTime
	 * @param platform
	 * @param appId
	 * @param channel
	 */
	public static void buyPropLog(int userId, int referrerId, int propId, int periodOfValidity, Date buyTime, int platform,  int channel) {
		String dateString = null;
		try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateString = dateFormat.format(buyTime);
        } catch (Exception e) {}
		
		hadoopLogger.info("buy_prop_v1:"
				+ (userId > 0 ? userId + "" : "") + "^" 
				+ (referrerId > 0 ? referrerId + "" : "") + "^"
				+ (propId > 0 ? propId + "" : "") + "^"
				+ (periodOfValidity > 0 ? periodOfValidity + "" : "") + "^"
				+ (dateString == null ? "" : dateString + "") + "^"
				+ (platform > 0 ? platform + "" : "") + "^"
				+ (channel > 0 ? channel + "" : "") + "^");
	}
   
	
}
