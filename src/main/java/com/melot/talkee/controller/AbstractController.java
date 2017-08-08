package com.melot.talkee.controller;

import javax.servlet.http.HttpServletRequest;

import com.melot.talkee.utils.*;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.sdk.core.function.GenericFunction;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.talkee.redis.UserRedisSource;

/**
 * Controller 抽象接口
 *
 * @author Administrator
 */
public abstract class AbstractController {


    /**
     * 根据http request验证及提取各参数
     *
     * @param request
     * @param swapObj
     * @return
     */
    public String generateParam(HttpServletRequest request, SwapObj swapObj) {
        // 获取参数 parameter值
        String parameter = request.getParameter("parameter");
        if (parameter == null) {
            return TagCodeEnum.PARAMETER_INCORRECT;
        }
        try {
            JsonObject json=new JsonParser().parse(parameter).getAsJsonObject();
            swapObj.setParamJson(json);
        } catch (Exception e) {
            return TagCodeEnum.PARAMETER_INCORRECT;
        }

        // 提取参数FuncTag
        JsonElement funcTagje = swapObj.getParamJson().get("FuncTag");
        if (funcTagje == null) {
            return TagCodeEnum.FUNCTAG_INCORRECT;
        }
        try {
            swapObj.setFuncTag(funcTagje.getAsInt());
        } catch (Exception e) {
            return TagCodeEnum.FUNCTAG_INCORRECT;
        }
        String funcTag = String.valueOf(funcTagje.getAsInt());
        if (funcTag.length() != 8) {
            return TagCodeEnum.FUNCTAG_INCORRECT;
        }
        String safeTag = funcTag.substring(2, 4);
        if (!safeTag.equals("00") && !safeTag.equals("11")) {
            return TagCodeEnum.FUNCTAG_INCORRECT;
        }

        if(ConfigHelper.getFuncTags().contains(funcTag)){
            swapObj.setCheckTag(true);
            return null;
        }



        boolean tag = false;
        // 分析FuncTag是否进行安全校验
        if (safeTag.equals("11")) {
            // 此处调用安全校验模块进行安全校验
            String ipAddr = CommonUtil.getIpAddr(request);
            if (ConfigHelper.getInsideIp().contains(ipAddr)) {
                tag = true;
            }
        }
        // 首先进行通用数据校验 s、v、p、c
        // 安全sv验证
        String signResult = SecurityFunctions.checkSignedValue(swapObj.getParamJson());
        if (StringUtils.isNotBlank(signResult) && tag == false) {
            return signResult;
        }



		int platform = -1;
		// 提取验证platform
		JsonElement platformObj = swapObj.getParamJson().get("p");
		if (platformObj == null) {
			return TagCodeEnum.PLATFORM_INCORRECT;
		}
		try {
			platform = platformObj.getAsInt();
		} catch (Exception e) {
			return TagCodeEnum.PLATFORM_INCORRECT;
		}
        
        // 提取参数userId和token
        JsonElement userIdje = swapObj.getParamJson().get("userId");
        JsonElement tokenje = swapObj.getParamJson().get("token");
        if (userIdje != null && !userIdje.isJsonNull() && tokenje != null && !tokenje.isJsonNull()) {
            int userId = 0;
            String token = null;
            try {
                userId = userIdje.getAsInt();
            } catch (Exception e) {
                return TagCodeEnum.USERID_INCORRECT;
            }
            // 验证token
            try {
            	token = tokenje.getAsString();
            } catch (Exception e) {
                return TagCodeEnum.TOKEN_INCORRECT;
            }
            String redisToken = UserRedisSource.getUserToken(userId,platform);
            if (StringUtils.isBlank(redisToken)) {
            	 return TagCodeEnum.TOKEN_EXPIRE;
			}
    		if (token.equals(redisToken)) {
    			  swapObj.setCheckTag(true);
    		}else{
    			 return TagCodeEnum.TOKEN_IS_ERROR;
    		}
        }

        return null;
    }

    /**
     * 根据http request生成返回结果
     *
     * @param result
     * @param request
     * @param TagCode
     * @return
     */
    public String generateResult(JsonObject result,
                                 HttpServletRequest request, String TagCode) {
        // 提取参数callback
        // 根据传入参数判断是否需要加入外包装
        String before = "";
        String after = "";
        String callback = request.getParameter("callback");
        if (callback != null) {
            before = callback + "(";
            after = ")";
        }
        if (result == null) {
            if (TagCode != null) {
                result = new JsonObject();
                result.addProperty("TagCode", TagCode);
            } else {
                result = new JsonObject();
            }
        }
        return before + result.toString() + after;
    }

    /**
     * 处理接口请求
     *
     * @param swapObj
     * @param request
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public JsonObject process(SwapObj swapObj, HttpServletRequest request)
            throws Exception {
        int funcTag = swapObj.getFuncTag();
        JsonObject paramObject = swapObj.getParamJson();
        GenericFunction genericFunction = (GenericFunction) MelotBeanFactory.getBean(String.valueOf(funcTag));
        if (genericFunction == null) {
            JsonObject result = new JsonObject();
            result.addProperty("TagCode",
                    TagCodeEnum.FUNCTAG_INCORRECT);
            return result;
        }
        return genericFunction.execute(paramObject, swapObj.isCheckTag(),
                request);
    }


}
