package com.melot.talkee.action;

import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.talkee.driver.domain.*;
import com.melot.talkee.driver.service.TalkClassService;
import com.melot.talkee.driver.service.TalkLessonService;
import com.melot.talkee.driver.service.TalkOrderService;
import com.melot.talkee.driver.service.TalkUserService;
import com.melot.talkee.service.domain.ClassMember;
import com.melot.talkee.utils.CommonUtil;
import com.melot.talkee.utils.TagCodeEnum;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by mn on 2017/5/8.
 */
public class ClassFuntions {
    /**
     * 进入课堂(50000101) （update？insert?  action_lessoning)
     * 业务流程
     * 1.根据userId 查询用户类型(info_user_register)
     * 2.根据课程classId, userId拿到课程的信息(开始时间，结束时间）
     * 3.
     *
     * @param jsonObject 业务参数：userId，enterType,classId
     * @param checkTag
     * @param request
     * @return
     */

    public JsonObject enterClass(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, periodId, enterType, p, roleType;
        String deviceUid;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, 0, Integer.MAX_VALUE);
            periodId = CommonUtil.getJsonParamInt(jsonObject, "periodId", 0, TagCodeEnum.PERIOD_ID_INCORRECT, 0, Integer.MAX_VALUE);
            enterType = CommonUtil.getJsonParamInt(jsonObject, "enterType", 0, TagCodeEnum.ENTERTYPE_INCORRECT, 0, Integer.MAX_VALUE);
            p = CommonUtil.getJsonParamInt(jsonObject, "p", 0, TagCodeEnum.PLATFORM_INCORRECT, 0, Integer.MAX_VALUE);
            deviceUid = CommonUtil.getJsonParamString(jsonObject, "deviceUid", "", TagCodeEnum.DEVICEUID_INCORRECT, 0, Integer.MAX_VALUE);
            roleType = CommonUtil.getJsonParamInt(jsonObject, "roleType", 1, TagCodeEnum.ROLETYPE_INCORRECT, 1, Integer.MAX_VALUE);

        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {
            TalkClassService talkClassService = MelotBeanFactory.getBean("talkClassService", TalkClassService.class);
            String resultCode = talkClassService.enterClass(userId, roleType, periodId, p, deviceUid);
            result.addProperty("TagCode", resultCode);
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }

        return result;
    }

    /**
     * 退出课堂（50000102）
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject exitClass(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, periodId, exitType, p, roleType;
        String deviceUid;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, 0, Integer.MAX_VALUE);
            periodId = CommonUtil.getJsonParamInt(jsonObject, "periodId", 0, TagCodeEnum.PERIOD_ID_INCORRECT, 0, Integer.MAX_VALUE);
            exitType = CommonUtil.getJsonParamInt(jsonObject, "exitType", 0, TagCodeEnum.ENTERTYPE_INCORRECT, 0, Integer.MAX_VALUE);
            p = CommonUtil.getJsonParamInt(jsonObject, "p", 0, TagCodeEnum.PLATFORM_INCORRECT, 0, Integer.MAX_VALUE);
            deviceUid = CommonUtil.getJsonParamString(jsonObject, "deviceUid", "", TagCodeEnum.DEVICEUID_INCORRECT, 0, Integer.MAX_VALUE);
            roleType = CommonUtil.getJsonParamInt(jsonObject, "roleType", 1, TagCodeEnum.ROLETYPE_INCORRECT, 1, Integer.MAX_VALUE);

        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {
            TalkClassService talkClassService = MelotBeanFactory.getBean("talkClassService", TalkClassService.class);
            String resultCode = talkClassService.outClass(userId, roleType, periodId, p, deviceUid);
            result.addProperty("TagCode", resultCode);
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }

        return result;
    }


    /**
     * 记录白板消息（50000103）
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject whiteboardMessage(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId, periodId, segment;
        String messageData;
        try {
            //    userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, 0, Integer.MAX_VALUE);
            periodId = CommonUtil.getJsonParamInt(jsonObject, "periodId", 0, TagCodeEnum.PERIOD_ID_INCORRECT, 0, Integer.MAX_VALUE);
            segment = CommonUtil.getJsonParamInt(jsonObject, "segment", 0, TagCodeEnum.SEGMENT_INCORRECT, 0, Integer.MAX_VALUE);
            messageData = CommonUtil.getJsonParamString(jsonObject, "messageData", "", TagCodeEnum.MESSAGE_DATA_INCORRECT, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {
            TalkUserService talkUserService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
            if (talkUserService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            TalkClassService talkClassService = MelotBeanFactory.getBean("talkClassService", TalkClassService.class);
            if (talkClassService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            /*
            UserInfo userInfo = talkUserService.getUserInfoByUserId(userId);
            if (userInfo.getAccountType() == 1) {
                result.addProperty("TagCode", TagCodeEnum.NO_PERMISSION);
                return result;
            }*/
            talkClassService.deleteOldRecord(periodId);
            int dataMax = 1024 * 900;
            int dataSize = messageData.length();
            int parts = dataSize / dataMax + (dataSize % dataMax > 0 ? 1 : 0);
            int start, end = 0;
            String substr;
            String resultCode = TagCodeEnum.SUCCESS;
            for (int s = 0; s < parts; s++) {
                start = s * dataMax;
                end = ((start + dataMax) > dataSize ? dataSize : (start + dataMax));
                substr = messageData.substring(start, end);
                resultCode = talkClassService.recordWhiteboard(0, periodId, segment, substr);
            }
            result.addProperty("TagCode", resultCode);
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }

        return result;
    }

    /**
     * 获取白板消息（50000104）
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject whiteboardRecord(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId, periodId, segment;
        try {
            //   userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, 0, Integer.MAX_VALUE);
            periodId = CommonUtil.getJsonParamInt(jsonObject, "periodId", 0, TagCodeEnum.PERIOD_ID_INCORRECT, 0, Integer.MAX_VALUE);
            segment = CommonUtil.getJsonParamInt(jsonObject, "segment", 0, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {
            TalkUserService talkUserService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
            if (talkUserService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            TalkClassService talkClassService = MelotBeanFactory.getBean("talkClassService", TalkClassService.class);
            if (talkClassService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
          /*  UserInfo userInfo = talkUserService.getUserInfoByUserId(userId);
            if (userInfo.getAccountType() == 1) {
                result.addProperty("TagCode", TagCodeEnum.NO_PERMISSION);
                return result;
            }*/
            //  List<Whiteboard> list = talkClassService.getWhiteboardList(userId, periodId, segment);


            Whiteboard whiteboard = mixdata(periodId, segment, talkClassService);

            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.add("messageData", new Gson().toJsonTree(whiteboard));
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }

        return result;
    }

    public JsonObject recordInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId, periodId, segment;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, 0, Integer.MAX_VALUE);
            periodId = CommonUtil.getJsonParamInt(jsonObject, "periodId", 0, TagCodeEnum.PERIOD_ID_INCORRECT, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {
            TalkUserService talkUserService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
            if (talkUserService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            TalkClassService talkClassService = MelotBeanFactory.getBean("talkClassService", TalkClassService.class);
            if (talkClassService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            UserInfo userInfo = talkUserService.getUserInfoByUserId(userId);
            if (userInfo == null) {
                result.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
                return result;
            }
            LessonRecord lessonRecord = talkClassService.getLessonRecord(periodId);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.add("lessonRecord", new Gson().toJsonTree(lessonRecord));

        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }

        return result;
    }


    public JsonObject classInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId, periodId;
        String token;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, 0, Integer.MAX_VALUE);
            periodId = CommonUtil.getJsonParamInt(jsonObject, "periodId", 0, TagCodeEnum.PERIOD_ID_INCORRECT, 0, Integer.MAX_VALUE);
            token = CommonUtil.getJsonParamString(jsonObject, "token", "", null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {
            TalkUserService talkUserService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
            if (talkUserService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            TalkClassService talkClassService = MelotBeanFactory.getBean("talkClassService", TalkClassService.class);
            if (talkClassService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            TalkOrderService talkOrderService = MelotBeanFactory.getBean("talkOrderService", TalkOrderService.class);
            if (talkClassService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }

            TalkLessonService talkLessonService = MelotBeanFactory.getBean("talkLessonService", TalkLessonService.class);
            if (talkLessonService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            UserInfo userInfo = talkUserService.getUserInfoByUserId(userId);

            StudentLesson lesson = talkOrderService.queryStudentLessonByPeriodId(periodId);
            if (lesson == null) {
                result.addProperty("TagCode", TagCodeEnum.LESSON_NOT_EXIST);
                return result;
            }
            Lesson confLesson = talkLessonService.getLessonById(lesson.getLessonId());

            StudentInfo student = talkUserService.getStudentInfoByUserId(lesson.getStudentId());
            student.setPortrait(talkUserService.getPortraitByUserId(lesson.getStudentId()));

            AdminInfo adminInfo = null;
            TeacherInfo teacher = null;
            ClassMember teachermemeber = new ClassMember();
            ClassMember studentmemeber = new ClassMember();
            teachermemeber.setAccountType(TalkCommonEnum.USER_ACCOUNT_TYPE_TEACHER);
            //调试课
            if (lesson.getConsType() == 3) {
                adminInfo = talkUserService.getAdminInfoByUserId(lesson.getTeacherId());
                String portraitUrl = talkUserService.getPortraitByUserIdAndType(adminInfo.getAdminId(), 3);
                teachermemeber.setPortraitUrl(portraitUrl);
                teachermemeber.setCnNickName(adminInfo.getCnNickname());
                teachermemeber.setEnNickName(adminInfo.getCnNickname());
                teachermemeber.setGender(1);
                teachermemeber.setUserId(adminInfo.getUserId());

            } else {
                teacher=talkUserService.getTeacherInfoByUserId(lesson.getTeacherId());
                teacher.setPortrait(talkUserService.getPortraitByUserId(lesson.getTeacherId()));
                teachermemeber.setCnNickName(teacher.getTeacherName());
                teachermemeber.setEnNickName(teacher.getTeacherName());
                teachermemeber.setGender(teacher.getGender());
                teachermemeber.setUserId(teacher.getTeacherId());

            }


            //构造返回数据

            studentmemeber.setAccountType(TalkCommonEnum.USER_ACCOUNT_TYPE_STUDENT);
            studentmemeber.setPortraitUrl(student.getPortrait() == null ? "" : student.getPortrait());
            studentmemeber.setCnNickName(student.getCnNickname() == null ? "" : student.getCnNickname());
            studentmemeber.setEnNickName(student.getEnNickname() == null ? "" : student.getEnNickname());
            studentmemeber.setGender(student.getGender());
            studentmemeber.setUserId(student.getUserId());


            //身份是学生
            if (userInfo.getAccountType() == 1) {
                result.add("myInfo", new Gson().toJsonTree(studentmemeber));
                result.add("peerInfo", new Gson().toJsonTree(teachermemeber));
                result.addProperty("identity", "student");
            } else if (userInfo.getAccountType() == 2) {
                result.add("myInfo", new Gson().toJsonTree(teachermemeber));
                result.add("peerInfo", new Gson().toJsonTree(studentmemeber));
                result.addProperty("identity", "teacher");
            }

            //课程信息
            result.addProperty("lessonId", lesson.getLessonId());
            result.addProperty("lessonName", confLesson.getLessonName());
            result.addProperty("periodId", periodId);
            result.addProperty("partId", 1);
            result.addProperty("channel", lesson.getPeriodId());
            result.addProperty("channelKey", lesson.getChannelKey() == null ? "" : lesson.getChannelKey());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.addProperty("token", token);

        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }


        return result;
    }


    public Whiteboard mixdata(Integer periodId, Integer segment, TalkClassService talkClassService) {
        Whiteboard result = null;
        Whiteboard temp = null;
        int limit = 1;
        int offset = 0;
        temp = talkClassService.getWhiteBoard(periodId, segment, offset, limit);
        if (temp == null) {
            return temp;
        }
        result = temp;
        StringBuffer buffer = new StringBuffer();
        while (temp != null) {
            buffer.append(temp.getMsgData());
            offset++;
            temp = talkClassService.getWhiteBoard(periodId, segment, offset, limit);
        }
        result.setMsgData(buffer.toString());
        return result;
    }
}
