package com.melot.talkee.utils;

import java.util.ArrayList;
import java.util.List;

import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.talkee.driver.domain.Channel;
import com.melot.talkee.driver.service.TalkCommonService;

public class ChannelEnum {

    /**主站渠道号 */
    public static final int DEFAUL_WEB_CHANNEL = 1;
    
    private static List<Integer> CHANNEL_LIST = new ArrayList<Integer>();
	
	/**
	 * 验证是否有效
	 * @param platform
	 * @return
	 */
	public static boolean isValid(int channel){
	    boolean tag = false;
	    if (!CHANNEL_LIST.isEmpty() && CHANNEL_LIST.contains(channel)) {
            return true;
        }
	    TalkCommonService talkCommonService = MelotBeanFactory.getBean("talkCommonService", TalkCommonService.class);
        if (talkCommonService != null) {
            List<Channel> channelList = talkCommonService.getChannelList(null);
            if (channelList != null) {
                for (Channel tempChannel : channelList) {
                    if (tempChannel.getChannelId().intValue() == channel) {
                        tag = true;
                    }
                    CHANNEL_LIST.add(tempChannel.getChannelId());
                }
            }
        }
		return tag;
	}
    
}
