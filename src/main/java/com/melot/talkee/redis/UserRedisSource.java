package com.melot.talkee.redis;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.melot.common.melot_jedis.JedisWrapper;
import com.melot.sdk.core.util.MelotBeanFactory;

public class UserRedisSource {
	
    private static final String SOURCE_BEAN_NAME = "userRedisSource";
	
	private static Logger logger = Logger.getLogger(UserRedisSource.class);
	
	/**
	 * 获取redis资源
	 * @return
	 */
	private static JedisWrapper getInstance() {
//    	return new JedisWrapper(RedisDataSourceFactory.getGlobalInstance().getJedisPool(SOURCE_NAME), SOURCE_NAME);
	    return MelotBeanFactory.getBean(SOURCE_BEAN_NAME, JedisWrapper.class);
	}
	
	/**
	 * 用户token(utoken_userId)
	 */
	private static final String USER_TOKEN_KEY = "talk_utoken_%s_%s";
	
	public static String getUserToken(int userId,int platform) {
		JedisWrapper jedis = null;
		try {
			jedis = getInstance();
			String key = String.format(USER_TOKEN_KEY, userId,platform);
			return jedis.STRINGS.get(key);
		} catch (Exception e) {
			logger.error("UserRedisSource.getUserToken(" + userId + ") execute exception.", e);
		}
		return null;
	}
	
	public static long getUserTokenTtl(int userId,int platform) {
		JedisWrapper jedis = null;
		try {
			jedis = getInstance();
			String key = String.format(USER_TOKEN_KEY, userId,platform);
			return jedis.KEYS.ttl(key);
		} catch (Exception e) {
			logger.error("UserRedisSource.getUserTokenTtl(" + userId + ") execute exception.", e);
		}
		return 0;
	}
	
	public static boolean checkToken(int userId, String token,int platform) {
		String redisToken = UserRedisSource.getUserToken(userId,platform);
		if (StringUtils.isNotBlank(redisToken) && token.equals(redisToken)) {
			return true;
		}
		return false;
	}
}
