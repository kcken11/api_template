package com.melot.talkee.action;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.kktalkee.crm.module.domain.ResultObject;
import com.kktalkee.crm.module.domain.StudentExtraInfo;
import com.kktalkee.crm.module.service.TlkStudentService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.kktalkee.crm.module.domain.AdminInfo;
import com.kktalkee.crm.module.service.TlkAdminService;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.talkee.driver.domain.StudentInfo;
import com.melot.talkee.driver.domain.TeacherInfo;
import com.melot.talkee.driver.domain.UserInfo;
import com.melot.talkee.driver.service.TalkUserService;
import com.melot.talkee.driver.utils.PasswordFunctions;
import com.melot.talkee.redis.SmsRedisSource;
import com.melot.talkee.redis.UserRedisSource;
import com.melot.talkee.service.UserService;
import com.melot.talkee.utils.CommonUtil;
import com.melot.talkee.utils.HadoopLogger;
import com.melot.talkee.utils.LoginTypeEnum;
import com.melot.talkee.utils.PlatformEnum;
import com.melot.talkee.utils.SecurityFunctions;
import com.melot.talkee.utils.SmsTypeEnum;
import com.melot.talkee.utils.TagCodeEnum;

public class UserFunctions {

    /**
     * 日志记录对象
     */
    private static Logger logger = Logger.getLogger(UserFunctions.class);

    private static JsonParser jsonParser = new JsonParser();
    private static Gson gson = new Gson();


    /**
     * 刷新用户token(20110106)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return
     */
    public JsonObject refreshUserToken(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        // 获取参数
        int userId = 0, platform;
        String token;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "p", 0, null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, 1, Integer.MAX_VALUE);
            token = CommonUtil.getJsonParamString(jsonObject, "token", null, TagCodeEnum.TOKEN_INCORRECT, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

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

            String newToken = userService.refreshUserToken(userId, token, platform);
            if (StringUtils.isBlank(newToken)) {
                result.addProperty("TagCode", TagCodeEnum.TOKEN_EXPIRE);
                return result;
            } else {
                long ttl = UserRedisSource.getUserTokenTtl(userId, platform);
                result.addProperty("token", newToken);
                result.addProperty("expireTime", ttl);
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            }
        } catch (Exception e) {
            logger.error("getUserInfo error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        // 返回结果
        return result;
    }


    /**
     * 获取用户信息(20110105)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 修改密码结果
     */
    public JsonObject getUserInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        // 获取参数
        int userId = 0, platform;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "p", 0, null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

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

            result.add("userInfo", UserService.userInfoToJson(userId, platform));
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("getUserInfo error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }

        // 返回结果
        return result;
    }


    /**
     * 获取学员详细信息(20000108)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return
     */
    public JsonObject getStudentExtraInfos(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        // 获取参数
        int userId = 0, studentId = 0;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, 1, Integer.MAX_VALUE);
            studentId = CommonUtil.getJsonParamInt(jsonObject, "studentId", 0, TagCodeEnum.USERID_INCORRECT, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

        try {

            TlkStudentService tlkStudentService = MelotBeanFactory.getBean("tlkStudentService", TlkStudentService.class);
            if (tlkStudentService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }

            ResultObject resultObject = tlkStudentService.getWebStudentExtraInfos(studentId);

            StudentExtraInfo studentExtraInfo = (StudentExtraInfo) resultObject.getData();
            if (studentExtraInfo != null) {

                result.add("studentExtraInfos", new Gson().toJsonTree(studentExtraInfo));
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);

            } else {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
            }


        } catch (Exception e) {
            logger.error("getStudentExtraInfos error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }

        // 返回结果
        return result;
    }


    /**
     * 修改密码(20110104)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 修改密码结果
     */
    public JsonObject changePwd(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 获取参数
        int userId = 0, type;
        String verifyCode, oldpwd, newpwd, phoneNum = null;
        try {

            type = CommonUtil.getJsonParamInt(jsonObject, "type", 0, TagCodeEnum.CHANGE_OR_FIND_PWD_TYPE_INCORRECT, 1, Integer.MAX_VALUE);
            verifyCode = CommonUtil.getJsonParamString(jsonObject, "verifyCode", null, null, 1, Integer.MAX_VALUE);
            oldpwd = CommonUtil.getJsonParamString(jsonObject, "oldpwd", null, null, 1, Integer.MAX_VALUE);
            newpwd = CommonUtil.getJsonParamString(jsonObject, "newpwd", null, TagCodeEnum.NEW_PASSWORD_INCORRECT, 1, Integer.MAX_VALUE);
            if (StringUtils.isNotBlank(oldpwd)) {
                try {
                    // 进行秘密解析
                    JsonObject upObject = PasswordFunctions.decryptUDPD(oldpwd);
                    if (upObject.isJsonObject()) {
                        oldpwd = upObject.get("password").getAsString();
                    }
                } catch (Exception e) {
                    result.addProperty("TagCode", TagCodeEnum.OLD_PASSWORD_INCORRECT);
                    return result;
                }
            }
            try {
                // 进行秘密解析
                JsonObject upObject = PasswordFunctions.decryptUDPD(newpwd);
                if (upObject.isJsonObject()) {
                    newpwd = upObject.get("password").getAsString();
                }
            } catch (Exception e) {
                result.addProperty("TagCode", TagCodeEnum.NEW_PASSWORD_INCORRECT);
                return result;
            }

            if (type != 1 && type != 2) {
                result.addProperty("TagCode", TagCodeEnum.CHANGE_OR_FIND_PWD_TYPE_INCORRECT);
                return result;
            }
            if (type == 1) {
                userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, 1, Integer.MAX_VALUE);

                if (!checkTag) {
                    result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
                    return result;
                }
            } else if (type == 2) {
                phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum", null, TagCodeEnum.PHONE_NUM_INCORRECT, 1, Integer.MAX_VALUE);
            }

        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

        try {

            TalkUserService userService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
            if (userService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            UserInfo userInfo = null;
            int smsType = SmsTypeEnum.CHANGE_PWD;
            if (type == 1) {
                userInfo = userService.getUserInfoByUserId(userId);
                if (userInfo == null) {
                    result.addProperty("TagCode", TagCodeEnum.USERID_INCORRECT);
                    return result;
                }
            } else if (type == 2) {
                smsType = SmsTypeEnum.FIND_PWD;
                userInfo = userService.getUserInfoByPhoneNumber(phoneNum);
                if (userInfo == null) {
                    result.addProperty("TagCode", TagCodeEnum.PHONE_NUMBER_NOT_EXIST);
                    return result;
                }
            }

            if (StringUtils.isBlank(verifyCode) && StringUtils.isBlank(oldpwd)) {
                // 未找到验证码或验证码已失效
                result.addProperty("TagCode", TagCodeEnum.VERIFY_CODE_AND_OLD_PAW_ISNULL);
                return result;
            }

            // 如果有验证码，则进行校验
            if (StringUtils.isNotBlank(verifyCode)) {
                String data = SmsRedisSource.getPhoneSmsData(String.valueOf(userInfo.getPhoneNum()), String.valueOf(smsType));
                if (StringUtils.isBlank(data) || !data.equals(verifyCode)) {
                    // 未找到验证码或验证码已失效
                    result.addProperty("TagCode", TagCodeEnum.VERIFY_CODE_UNAVAIL);
                    return result;
                }
            } else if (StringUtils.isNotBlank(oldpwd)) {
                // 老密码验证
                JsonObject udpdJson = new JsonObject();
                udpdJson.addProperty("userId", userInfo.getUserId());
                udpdJson.addProperty("password", oldpwd);
                String udpd = PasswordFunctions.encryptUDPD(udpdJson);
                if (!udpd.equals(userInfo.getPassword())) {
                    // 未找到验证码或验证码已失效
                    result.addProperty("TagCode", TagCodeEnum.OLD_PASSWORD_ERROR);
                    return result;
                }
            }

            if (userService.modifyPassword(userInfo.getUserId(), newpwd)) {
                result.addProperty("userId", userInfo.getUserId());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            } else {
                result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
        } catch (Exception e) {
            logger.error("changePwd error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }

        // 返回结果
        return result;
    }


    /**
     * 修改用户信息(20000102)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 登录结果
     */
    public JsonObject changeUserInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        // 获取参数
        int userId, gender, age, cityId, platform;
        String loginName, enUserName, cnUserName, introduce, birthday, email, phoneNum;
        try {

            platform = CommonUtil.getJsonParamInt(jsonObject, "p", 0, null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, 1, Integer.MAX_VALUE);
            loginName = CommonUtil.getJsonParamString(jsonObject, "loginName", null, null, 1, Integer.MAX_VALUE);
            cnUserName = CommonUtil.getJsonParamString(jsonObject, "cnUserName", null, null, 1, Integer.MAX_VALUE);
            enUserName = CommonUtil.getJsonParamString(jsonObject, "enUserName", null, null, 1, Integer.MAX_VALUE);
            cityId = CommonUtil.getJsonParamInt(jsonObject, "cityId", 0, null, 1, Integer.MAX_VALUE);
            introduce = CommonUtil.getJsonParamString(jsonObject, "introduce", null, null, 1, Integer.MAX_VALUE);
            birthday = CommonUtil.getJsonParamString(jsonObject, "birthday", null, null, 1, Integer.MAX_VALUE);
            gender = CommonUtil.getJsonParamInt(jsonObject, "gender", -1, null, -1, Integer.MAX_VALUE);
            age = CommonUtil.getJsonParamInt(jsonObject, "age", 0, null, -1, Integer.MAX_VALUE);

            email = CommonUtil.getJsonParamString(jsonObject, "email", null, null, 1, Integer.MAX_VALUE);
            phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum", null, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }
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

            // 对登录用户名进行唯一性校验
            if (StringUtils.isNotBlank(loginName)) {
                UserInfo tempUserInfo = userService.getUserInfoByLoginName(loginName);
                if (tempUserInfo != null && tempUserInfo.getUserId().intValue() != userId) {
                    result.addProperty("TagCode", TagCodeEnum.USER_NAME_EXIST);
                    return result;
                }
            }

            int accountType = userInfo.getAccountType();
            Integer resultCode = null;
            if (accountType == 1) {
                // 学生中文名称不能为空
//				if (StringUtils.isBlank(cnUserName)) {
//					 result.addProperty("TagCode", TagCodeEnum.CN_USER_NAME_MISSING);
//			         return result;
//				}

                StudentInfo studentInfo = new StudentInfo();
                if (StringUtils.isNotBlank(birthday)) {
                    try {
                        studentInfo.setBirthday(new Date(Long.valueOf(birthday)));
                    } catch (Exception e) {
                        result.addProperty("TagCode", TagCodeEnum.BIRTHDAY_INCORRECT);
                        return result;
                    }
                }
                studentInfo.setCityId(cityId);
                studentInfo.setCnNickname(cnUserName);
                studentInfo.setEnNickname(enUserName);
                studentInfo.setEmail(email);
                studentInfo.setPhoneNum(phoneNum);
                if (gender != -1) {
                    studentInfo.setGender(gender);
                }
                studentInfo.setUserId(userId);
                studentInfo.setUpdateTime(new Date());

                resultCode = userService.modifyStudentInfo(loginName, studentInfo);
            } else {
                // 学生中文名称不能为空
//				if (StringUtils.isBlank(enUserName)) {
//					 result.addProperty("TagCode", TagCodeEnum.EN_USER_NAME_MISSING);
//			         return result;
//				}

                TeacherInfo teacherInfo = new TeacherInfo();
                if (age != 0) {
                    teacherInfo.setAge(age);
                }
                if (StringUtils.isNotBlank(birthday)) {
                    try {
                        teacherInfo.setBirthday(new Date(Long.valueOf(birthday)));
                    } catch (Exception e) {
                        result.addProperty("TagCode", TagCodeEnum.BIRTHDAY_INCORRECT);
                        return result;
                    }
                }
                teacherInfo.setCityId(cityId);
                if (gender != -1) {
                    teacherInfo.setGender(gender);
                }
                teacherInfo.setIntroduce(introduce);
                teacherInfo.setTeacherId(userId);
                teacherInfo.setEmail(email);
                teacherInfo.setTeacherName(enUserName);

                resultCode = userService.modifyTeacherInfo(loginName, teacherInfo);
            }

            if (resultCode != null && resultCode > 0) {
                result.add("userInfo", UserService.userInfoToJson(userId, platform));
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            } else {
                result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            }

        } catch (Exception e) {
            logger.error("changeUserInfo error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
//		// 返回结果
        return result;
    }


    /**
     * 登录接口(20110103)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 登录结果
     */
    public JsonObject login(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 获取参数
        int channel, loginType, platform, port;
        String phoneNum = null, pwd = null, verifyCode = null, userAccount = null, email = null, deviceUId, version, clientIp = null;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "p", 0, null, 1, Integer.MAX_VALUE);
            if (platform == PlatformEnum.WEB) {
                clientIp = CommonUtil.getJsonParamString(jsonObject, "clientIp", CommonUtil.getIpAddr(request), null, 0, Integer.MAX_VALUE);
            }
            port = CommonUtil.getPort(request, platform, 0);
            deviceUId = CommonUtil.getJsonParamString(jsonObject, "deviceUId", null, null, 1, Integer.MAX_VALUE);
            channel = CommonUtil.getJsonParamInt(jsonObject, "c", 0, null, 1, Integer.MAX_VALUE);
            version = CommonUtil.getJsonParamString(jsonObject, "v", null, null, 1, Integer.MAX_VALUE);
            loginType = CommonUtil.getJsonParamInt(jsonObject, "loginType", 0, TagCodeEnum.LOGIN_TYPE_INCORRECT, 1, Integer.MAX_VALUE);


            if (LoginTypeEnum.isValid(loginType) == false) {
                result.addProperty("TagCode", TagCodeEnum.LOGIN_TYPE_INCORRECT);
                return result;
            }
            if (loginType == LoginTypeEnum.PHONE_NUMBER) {
                phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum", null, TagCodeEnum.PHONE_NUM_INCORRECT, 1, Integer.MAX_VALUE);
                if (phoneNum != null && StringUtils.isNumeric(phoneNum)) {
                    phoneNum = CommonUtil.validatePhoneNum(phoneNum, "86");
                } else {
                    result.addProperty("TagCode", TagCodeEnum.PHONE_NUM_INCORRECT);
                    return result;
                }

                verifyCode = CommonUtil.getJsonParamString(jsonObject, "verifyCode", null, null, 1, Integer.MAX_VALUE);
                pwd = CommonUtil.getJsonParamString(jsonObject, "pwd", null, null, 1, Integer.MAX_VALUE);
                if (StringUtils.isBlank(verifyCode) && StringUtils.isBlank(pwd)) {
                    result.addProperty("TagCode", TagCodeEnum.VERIFY_CODE_AND_PAW_ISNULL);
                    return result;
                }
            } else {
                pwd = CommonUtil.getJsonParamString(jsonObject, "pwd", null, TagCodeEnum.PASSWORD_MISSING, 1, Integer.MAX_VALUE);
                if (loginType == LoginTypeEnum.ACCOUNT) {
                    userAccount = CommonUtil.getJsonParamString(jsonObject, "userAccount", null, TagCodeEnum.USER_ACCOUT_MISSING, 1, Integer.MAX_VALUE);
                } else if (loginType == LoginTypeEnum.EMAIL) {
                    email = CommonUtil.getJsonParamString(jsonObject, "email", null, TagCodeEnum.EMAIL_MISSING, 1, Integer.MAX_VALUE);
                }
            }
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }
        try {

            TalkUserService userService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
            if (userService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }

            if (StringUtils.isBlank(verifyCode) && StringUtils.isBlank(pwd)) {
                result.addProperty("TagCode", TagCodeEnum.VERIFY_CODE_AND_PAW_ISNULL);
                return result;
            }

            UserInfo userInfo = null;
            if (loginType == LoginTypeEnum.PHONE_NUMBER) {
                userInfo = userService.getUserInfoByPhoneNumber(phoneNum);
            } else if (loginType == LoginTypeEnum.ACCOUNT) {
                userInfo = userService.getUserInfoByLoginName(userAccount);
            } else if (loginType == LoginTypeEnum.EMAIL) {
                userInfo = userService.getUserInfoByEmail(email);
            }

            if (userInfo == null) {
                result.addProperty("TagCode", TagCodeEnum.ACCOUNT_OR_PASSWORD_ERROR);
                return result;
            }

            //校验是否被封禁
            int userType = userInfo.getAccountType();
            Integer tag = 0;
            if (userType == 1) {
                StudentInfo studentInfo = userService.getStudentInfoByUserId(userInfo.getUserId());
                tag = studentInfo.getTag();
            } else if (userType == 2) {
                TeacherInfo teacherInfo = userService.getTeacherInfoByUserId(userInfo.getUserId());
                tag = teacherInfo.getTag();
            }
            if (tag != null && tag.intValue() == -1) {
                result.addProperty("TagCode", TagCodeEnum.ACCOUNT_IS_FORBIDDEN);
                return result;
            }
            if (StringUtils.isNotBlank(verifyCode)) {
                String data = SmsRedisSource.getPhoneSmsData(phoneNum, String.valueOf(SmsTypeEnum.LOGIN));
                if (StringUtils.isBlank(data) || !data.equals(verifyCode)) {
                    result.addProperty("TagCode", TagCodeEnum.VERIFY_CODE_UNAVAIL);
                    return result;
                }
            } else if (StringUtils.isNotBlank(pwd)) {
                try {
                    // 进行秘密解析
                    JsonObject upObject = PasswordFunctions.decryptUDPD(pwd);
                    if (upObject.isJsonObject()) {
                        pwd = upObject.get("password").getAsString();
                    }
                } catch (Exception e) {
                    result.addProperty("TagCode", TagCodeEnum.ACCOUNT_OR_PASSWORD_ERROR);
                    return result;
                }
                JsonObject udpdJson = new JsonObject();
                udpdJson.addProperty("userId", userInfo.getUserId());
                udpdJson.addProperty("password", pwd);
                String udpd = PasswordFunctions.encryptUDPD(udpdJson);
                if (!udpd.equals(userInfo.getPassword())) {
                    result.addProperty("TagCode", TagCodeEnum.ACCOUNT_OR_PASSWORD_ERROR);
                    return result;
                }
            }

            // 登录日志
            HadoopLogger.loginLog(userInfo.getUserId(), new Date(), platform, channel, version, null, clientIp, port, loginType, deviceUId, SecurityFunctions.decodeED(jsonObject));
            String token = userService.recordLogin(userInfo.getUserId(), loginType, platform, clientIp);
            if (StringUtils.isBlank(token)) {
                result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
            //更新登录时间,ip
            result.add("loginResult", UserService.userInfoToJson(userInfo.getUserId(), platform));
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("login error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
//		// 返回结果
        return result;
    }


    /**
     * 手机注册(20110101)
     *
     * @param jsonObject
     * @return
     * @created 2103-12-21 by RC
     */
    public JsonObject registerViaPhoneNum(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        // 获取参数
        int channel, recommended, platform, port;
        String phoneNum, verifyCode, deviceUId, version, pwd, clientIp = null;
        try {
            verifyCode = CommonUtil.getJsonParamString(jsonObject, "verifyCode", null, TagCodeEnum.VERIFY_CODE_UNAVAIL, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "p", 0, null, 1, Integer.MAX_VALUE);
            if (platform == PlatformEnum.WEB) {
                clientIp = CommonUtil.getJsonParamString(jsonObject, "clientIp", CommonUtil.getIpAddr(request), null, 0, Integer.MAX_VALUE);
            }
            port = CommonUtil.getPort(request, platform, 0);
            deviceUId = CommonUtil.getJsonParamString(jsonObject, "deviceUId", null, null, 1, Integer.MAX_VALUE);
            channel = CommonUtil.getJsonParamInt(jsonObject, "c", 0, null, 1, Integer.MAX_VALUE);
            version = CommonUtil.getJsonParamString(jsonObject, "v", null, null, 1, Integer.MAX_VALUE);
            recommended = CommonUtil.getJsonParamInt(jsonObject, "recommended", 0, null, 1, Integer.MAX_VALUE);
            phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum", null, TagCodeEnum.PHONE_NUM_INCORRECT, 1, Integer.MAX_VALUE);

            if (phoneNum != null && StringUtils.isNumeric(phoneNum)) {
                phoneNum = CommonUtil.validatePhoneNum(phoneNum, "86");
            } else {
                result.addProperty("TagCode", TagCodeEnum.PHONE_NUM_INCORRECT);
                return result;
            }
            pwd = CommonUtil.getJsonParamString(jsonObject, "pwd", null, TagCodeEnum.PASSWORD_MISSING, 1, Integer.MAX_VALUE);

            if (StringUtils.isNotBlank(pwd)) {
                try {
                    // 进行秘密解析
                    JsonObject upObject = PasswordFunctions.decryptUDPD(pwd);
                    if (upObject.isJsonObject()) {
                        pwd = upObject.get("password").getAsString();
                    }
                } catch (Exception e) {
                    result.addProperty("TagCode", TagCodeEnum.ACCOUNT_OR_PASSWORD_ERROR);
                    return result;
                }
            }

        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

        // 校验验证码
        try {
            String data = SmsRedisSource.getPhoneSmsData(String.valueOf(phoneNum), String.valueOf(SmsTypeEnum.REGISTER));
            if (StringUtils.isNotBlank(data) && data.equals(verifyCode)) {
                Integer userId = null;

                TalkUserService userService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
                if (userService == null) {
                    result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                    return result;
                }

                // 手机号已注册过直接登录
                UserInfo tempUserInfo = userService.getUserInfoByPhoneNumber(phoneNum);
                if (tempUserInfo == null) {
                    userId = userService.registerViaPhoneNum(channel, recommended, platform, port, phoneNum, deviceUId, version, clientIp, pwd);

                    // 手机注册日志
                    HadoopLogger.registerLog(userId, new Date(), platform, channel, version, null, clientIp, port, LoginTypeEnum.PHONE_NUMBER, 0,
                            1, deviceUId, recommended, SecurityFunctions.decodeED(jsonObject));

                    userService.recordLogin(userId, LoginTypeEnum.PHONE_NUMBER, platform, clientIp);

                    result.add("loginResult", UserService.userInfoToJson(userId, platform));
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    return result;
                } else {
                    result.addProperty("TagCode", TagCodeEnum.PHONE_NUMBER_EXIST);
                    return result;
                }
            } else {
                // 未找到验证码或验证码已失效
                result.addProperty("TagCode", TagCodeEnum.VERIFY_CODE_UNAVAIL);
            }
        } catch (Exception e) {
            logger.error("registerViaPhoneNum error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        return result;
    }


    /**
     * 获取学生顾问（CC\CR）信息 (20000107)
     *
     * @param jsonObject
     * @return
     * @created 2103-12-21 by RC
     */
    public JsonObject getAdviserInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId = 0;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

        try {

            TalkUserService userService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
            if (userService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            StudentInfo studentInfo = userService.getStudentInfoByUserId(userId);
            if (studentInfo != null) {
                Integer adviserId = null;
                // 普通用户获取对应CRID
                if (studentInfo.getUserType() > 0) {
                    adviserId = studentInfo.getCrId();
                } else {
                    adviserId = studentInfo.getCcId();
                }
                try {
                    TlkAdminService tlkAdminService = MelotBeanFactory.getBean("tlkAdminService", TlkAdminService.class);
                    if (tlkAdminService != null) {
                        ResultObject resultObject = tlkAdminService.getAdminInfo(adviserId);
                           AdminInfo adminInfo =null;
                        if(resultObject!=null&&resultObject.isSuccess()&&resultObject.getData()!=null){
                            adminInfo= (AdminInfo) resultObject.getData();
                        }

                        if (adminInfo != null) {
                            if (StringUtils.isNotBlank(adminInfo.getRealName())) {
                                result.addProperty("adviserName", adminInfo.getRealName());
                            }
                            if (StringUtils.isNotBlank(adminInfo.getPhone())) {
                                result.addProperty("phoneNum", adminInfo.getPhone());
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("getAdviserInfo rpc tlkAdminService error, jsonObject:" + jsonObject, e);
                }
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("getAdviserInfo error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }

        // 返回结果
        return result;
    }

}
