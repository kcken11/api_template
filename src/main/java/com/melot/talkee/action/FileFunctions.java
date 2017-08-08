package com.melot.talkee.action;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import com.melot.talkee.driver.service.TalkSecurityService;
import com.melot.talkee.utils.*;
import org.apache.log4j.Logger;
import com.google.gson.JsonObject;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.talkee.driver.domain.EcloudSource;
import com.melot.talkee.driver.domain.UserInfo;
import com.melot.talkee.driver.service.TalkUserService;

public class FileFunctions {

    /**
     * 日志记录对象
     */
    private static Logger logger = Logger.getLogger(FileFunctions.class);


    /**
     * 阿里oss上传回调(30110101)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 文件上传结果
     */
    public JsonObject uploadCallback(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        // 获取参数
        int userId, fileType, lessonId;
        String fileName;
        Map<String, Object> paramMap = new HashMap<String, Object>();
        try {
            fileType = CommonUtil.getJsonParamInt(jsonObject, "fileType", 0, TagCodeEnum.FILE_TYPE_INCORRECT, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, 1, Integer.MAX_VALUE);
            fileName = CommonUtil.getJsonParamString(jsonObject, "fileName", null, TagCodeEnum.FILE_NOT_FIND, 1, Integer.MAX_VALUE);
            lessonId = CommonUtil.getJsonParamInt(jsonObject, "lessonId", 0, null, 1, Integer.MAX_VALUE);
            if (fileType == 2) {
                if (lessonId == 0) {
                    result.addProperty("TagCode", TagCodeEnum.LESSON_ID_INCORRECT);
                    return result;
                }
                paramMap.put("lessonId", lessonId);
            }
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }
        Map<String, String> fileMap = new HashMap<>();
        String filename = fileName.substring(fileName.lastIndexOf("/"),fileName.length());
        fileMap.put(filename,fileName);
        
        try {
            TalkUserService userService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
            if (userService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            UserInfo userInfo = userService.getUserInfoByUserId(userId);
            if (userInfo == null) {
                result.addProperty("TagCode", TagCodeEnum.USERID_INCORRECT);
                return result;
            }
            // 文件数据是否保存成功
            if (userService.fileUpload(userInfo.getUserId(), fileType, fileMap, paramMap)) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }
        } catch (Exception e) {
            logger.error("fileUpload error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        // 返回结果
        return result;
    }

    /**
     * 获取OSS-临时安全令牌(30110102)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getSecurityToken(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        logger.info(jsonObject.toString());
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId, fileType, lessonId;
        String fileName;
        try {
            fileType = CommonUtil.getJsonParamInt(jsonObject, "fileType", 0, TagCodeEnum.FILE_TYPE_INCORRECT, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, 1, Integer.MAX_VALUE);
            lessonId = CommonUtil.getJsonParamInt(jsonObject, "lessonId", 0, null, 1, Integer.MAX_VALUE);
            fileName = CommonUtil.getJsonParamString(jsonObject, "fileName", null, TagCodeEnum.FILE_NOT_FIND, 1, Integer.MAX_VALUE);
            //课件
            if (fileType == 2) {
                if (lessonId == 0) {
                    result.addProperty("TagCode", TagCodeEnum.LESSON_ID_INCORRECT);
                    return result;
                }
            }

            if (!validateFileSuffix(fileName, fileType)) {
                result.addProperty("TagCode", TagCodeEnum.File_FORMART_INCORRECT);
                return result;
            }
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

        //获取token
        try {
            TalkSecurityService securityService = (TalkSecurityService) MelotBeanFactory.getBean("talkSecurityService", TalkSecurityService.class);
        	if (securityService == null) {
				result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
				return result;
			}
            
            EcloudSource ecloudSource = securityService.getOssToken(userId);

            if (ecloudSource != null) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("bucketName", ecloudSource.getBucketName());
                result.addProperty("endpoint", ecloudSource.getEndpoint());
                result.addProperty("accessKeyId",ecloudSource.getAccessKeyId());
                result.addProperty("accessKeySecret", ecloudSource.getAccessKeySecret());
                result.addProperty("securityToken", ecloudSource.getSecurityToken());
                result.addProperty("savePath", getSavePath(fileType, fileName));
                result.addProperty("region",ecloudSource.getRegion());
            } else {
                result.addProperty("TagCode", TagCodeEnum.OSS_ERR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("get.security.token.failed :" + e.getLocalizedMessage());
        }

        return result;
    }


    /**
     * 校验文件格式
     * @param fileName
     * @param fileType
     * @return
     */
    private boolean validateFileSuffix(String fileName, int fileType) {
        String[] filenames = fileName.split(",");
        if (fileType == 1) {
            for (String filename : filenames) {
                if (filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith("gif") || filename.endsWith(".bmp")) {
                    return true;
                }
                return false;
            }
        } else {
            return true;
        }
        return false;
    }

    /**
     * 获取云服务存储路径
     * @param fileType
     * @param filename
     * @return
     */
    private String getSavePath(int fileType, String filename) {
        String typePath = "";
        if (fileType == 1) {
            typePath = "aliyun/portrait/" + DateUtil.formatDate(new Date(), "yyyyMMdd");
        } else {
            typePath = "aliyun/lesson/" + DateUtil.formatDate(new Date(), "yyyyMMdd");
        }
        String suffix = filename.substring(filename.lastIndexOf("."), filename.length());
        return typePath + "/" + System.currentTimeMillis() + suffix;
    }

}
