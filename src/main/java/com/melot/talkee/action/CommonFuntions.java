package com.melot.talkee.action;


import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.talkee.driver.domain.ConfigInfo;
import com.melot.talkee.driver.domain.VersionInfo;
import com.melot.talkee.driver.service.TalkCommonService;
import com.melot.talkee.utils.CommonUtil;
import com.melot.talkee.utils.TagCodeEnum;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * Title: CommonFuntions
 * <p>
 * Description: 公共方法
 * </p>
 * @author 董毅<a href="mailto:yi.dong@melot.cn" />
 * @version V1.0
 * @since 2017-6-28 上午9:27:10
 */
public class CommonFuntions {

    /**
     * 获取最新版本号(10000201) 
     * @param jsonObject 业务参数：p，v
     * @param checkTag
     * @param request
     * @return
     */

    public JsonObject getLatestVersion(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 该接口需要验证token,未验证的返回错误码
        int p;
        String v;
        try {
            p = CommonUtil.getJsonParamInt(jsonObject, "p", 0, TagCodeEnum.PLATFORM_INCORRECT, 0, Integer.MAX_VALUE);
            v = CommonUtil.getJsonParamString(jsonObject, "v", "", TagCodeEnum.VERSION_INCORRECT, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        try {
            TalkCommonService talkCommonService = MelotBeanFactory.getBean("talkCommonService", TalkCommonService.class);
            VersionInfo versionInfo = talkCommonService.getLatestVersion(v, p);
            if (versionInfo != null) {
                result.addProperty("latestVersion",versionInfo.getVersionName());//   String  最新版本名称（对应接口v）
                result.addProperty("latestVersionDesc",versionInfo.getVersionDesc());//    String  最新版本描述
                result.addProperty("latestVersionURL",versionInfo.getVersionUrl());//     String  最新版本下载地址
                result.addProperty("checkResult",versionInfo.getCheckResult());//      number  1:无需升级 2:可选升级 3:强制升级 4:系统维护
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        return result;
    }

    /**
     * 获取配置信息 (10000202) 
     * @param jsonObject 业务参数：userId，enterType,classId
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getConfigInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 该接口需要验证token,未验证的返回错误码
        int p;
        String configKey;
        try {
            p = CommonUtil.getJsonParamInt(jsonObject, "p", 0, TagCodeEnum.PLATFORM_INCORRECT, 0, Integer.MAX_VALUE);
            configKey = CommonUtil.getJsonParamString(jsonObject, "configKey", null, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {
            TalkCommonService talkCommonService = MelotBeanFactory.getBean("talkCommonService", TalkCommonService.class);
            List<ConfigInfo> configInfoList  = talkCommonService.getConfigInfo(configKey, p);
            JsonArray jsonArray = new JsonArray();
            if (configInfoList != null && !configInfoList.isEmpty()) {
                for (ConfigInfo configInfo : configInfoList) {
                    String configValue = configInfo.getConfigValue();
                    JsonObject configObject = null;
                    if (StringUtils.isNotBlank(configValue)) {
                        JsonParser parser = new JsonParser();
                        JsonElement element = parser.parse(configValue);
                        if (element.isJsonObject()) {
                            configObject = new JsonObject();
                            configObject.addProperty("configKey", configInfo.getConfigKey());
                            configObject.add("configValue", element);
                            jsonArray.add(configObject);
                        }
                    }
                }
            }
            result.add("configInfo", jsonArray);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        return result;
    }
}
