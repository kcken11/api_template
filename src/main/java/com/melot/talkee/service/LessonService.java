package com.melot.talkee.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.talkee.driver.domain.*;
import com.melot.talkee.driver.service.TalkLessonService;
import com.melot.talkee.driver.service.TalkOrderService;
import com.melot.talkee.driver.service.TalkUserService;
import com.melot.talkee.service.domain.SimplifyLessonInfo;
import com.melot.talkee.service.domain.SimplifyStudentInfo;
import com.melot.talkee.utils.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.*;

public class LessonService {

    private static Logger logger = Logger.getLogger(LessonService.class);

    private static Gson gson = new Gson();

    private static JsonParser jsonParser = new JsonParser();

    public static JsonObject getPublishLessonJson(PublishLesson publishLesson) {
        if (publishLesson != null) {
            TalkOrderService orderService = MelotBeanFactory.getBean("talkOrderService", TalkOrderService.class);
            if (orderService != null) {
                Date beginTimeDate = publishLesson.getBeginTime();
                Date endTimeDate = publishLesson.getEndTime();
                Date lessonDate = DateUtil.parseDateStringToDate(DateUtil.formatDate(beginTimeDate, "yyyyMMdd"), "yyyyMMdd");
                Integer state = publishLesson.getState();
                JsonObject object = new JsonObject();
                object.addProperty("beginTime", beginTimeDate.getTime());
                object.addProperty("endTime", endTimeDate.getTime());
                object.addProperty("lessonDate", lessonDate.getTime());
                object.addProperty("periodId", publishLesson.getPeriodId());
                object.addProperty("publishType", publishLesson.getPublishType());
                object.addProperty("state", publishLesson.getState());
                if (state.intValue() == 2) {
                    List<Integer> paramList = new ArrayList<Integer>();
                    paramList.add(publishLesson.getPeriodId());
                    List<OrderLesson> orderLessonList = orderService.getOrderLessonByPeriodList(paramList);
                    // 此处可能包含公开课 一对多
                    if (orderLessonList != null && orderLessonList.size() > 0) {
                        SimplifyLessonInfo lessonInfo = null;
                        SimplifyStudentInfo studentInfo = null;
                        for (OrderLesson orderLesson : orderLessonList) {
                            Integer studentId = orderLesson.getStudentId();
                            Integer lessonId = orderLesson.getLessonId();
                            studentInfo = UserService.getSimplifyStudentInfo(studentId);

                            lessonInfo = getSimplifyLessonInfo(lessonId);
                            if (lessonInfo != null) {
                                lessonInfo.setLessonStatus(orderLesson.getLessonState());
                            }
                            StudentCheckin studentCheckin = orderLesson.getStudentCheckin();
                            if (studentCheckin != null) {
                                lessonInfo.setStudentAbnormalState(studentCheckin.getAbnormalState());
                            }
                            TeacherCheckin teacherCheckin = orderLesson.getTeacherCheckin();
                            if (teacherCheckin != null) {
                                lessonInfo.setTeacherAbnormalState(teacherCheckin.getAbnormalState());
                            }
                        }
                        if (studentInfo != null) {
                            object.add("studentInfo", jsonParser.parse(gson.toJson(studentInfo, SimplifyStudentInfo.class)));
                        }
                        if (lessonInfo != null) {
                            object.add("lessonInfo", jsonParser.parse(gson.toJson(lessonInfo, SimplifyLessonInfo.class)));
                        }
                    }
                }
                return object;
            }
        }
        return null;
    }

    public static JsonObject getLessonJson(Lesson lesson) {
        if (lesson != null) {
            TalkLessonService lessonService = MelotBeanFactory.getBean("talkLessonService", TalkLessonService.class);
            if (lessonService != null) {
                String lessonName = lesson.getLessonName();
                String originalLessonUrl = lesson.getOriginalLessonUrl();
                Integer lessonId = lesson.getLessonId();
                Integer lessonLevel = lesson.getLessonLevel();
                Integer lessonType = lesson.getLessonType();

                JsonObject object = new JsonObject();
                object.addProperty("lessonName", lessonName);
                object.addProperty("url", originalLessonUrl);
                object.addProperty("lessonId", lessonId);
                object.addProperty("lessonLevel", lessonLevel);
                object.addProperty("lessonType", lessonType);

                return object;
            }
        }
        return null;
    }

    public static JsonObject getOrderLessonJson(OrderLesson orderLesson) {
        if (orderLesson != null) {
            TalkLessonService lessonService = MelotBeanFactory.getBean("talkLessonService", TalkLessonService.class);
            if (lessonService != null) {
                Integer histId = orderLesson.getHistId();
                Date beginTimeDate = orderLesson.getBeginTime();
                Date endTimeDate = orderLesson.getEndTime();
                Date lessonDate = DateUtil.parseDateStringToDate(DateUtil.formatDate(beginTimeDate, "yyyyMMdd"), "yyyyMMdd");
                Integer lessonState = orderLesson.getLessonState();
                Integer publishType = orderLesson.getPublishType();
                Integer periodId = orderLesson.getPeriodId();
                Integer teacherId = orderLesson.getTeacherId();
                Integer studentId = orderLesson.getStudentId();
                Integer type = orderLesson.getType();
                Integer isStudentComment = orderLesson.getIsStudentComment();
                Integer isTeacherComment = orderLesson.getIsTeacherComment();


                JsonObject object = new JsonObject();
                object.addProperty("histId",histId);
                object.addProperty("beginTime", beginTimeDate.getTime());
                object.addProperty("endTime", endTimeDate.getTime());
                object.addProperty("lessonDate", lessonDate.getTime());
                object.addProperty("lessonState", lessonState);
                object.addProperty("publishType", publishType);
                object.addProperty("periodId", periodId);
                object.addProperty("teacherId", teacherId);
                if (type == null) {
                    object.addProperty("type", 0);
                } else {
                    object.addProperty("type", type);

                }

                object.addProperty("isStudentComment", isStudentComment);
                object.addProperty("isTeacherComment", isTeacherComment);

                StudentCheckin studentCheckin = orderLesson.getStudentCheckin();
                if (studentCheckin != null) {
                    object.addProperty("studentAbnormalState", studentCheckin.getAbnormalState());
                }

                TeacherCheckin teacherCheckin = orderLesson.getTeacherCheckin();
                if (teacherCheckin != null) {
                    object.addProperty("teacherAbnormalState", teacherCheckin.getAbnormalState());
                }
                TalkUserService talkUserService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
                if (talkUserService != null) {
                    if (publishType == 3) {
                        String teacherName = "课程顾问";
                        com.melot.talkee.driver.domain.AdminInfo adminInfo = talkUserService.getAdminInfoByUserId(teacherId);
                        if (adminInfo != null) {
                            teacherName = adminInfo.getCnNickname();
                        }
                        object.addProperty("teacherName", teacherName);
                    } else {
                        TeacherInfo teacherInfo = talkUserService.getTeacherInfoByUserId(teacherId);
                        if (teacherInfo != null) {
                            object.addProperty("teacherName", teacherInfo.getTeacherName());

                            String portrait = talkUserService.getPortraitByUserId(teacherId);
                            object.addProperty("teacherPortrait", portrait);
                        }
                    }
                }

                Integer lessonId = orderLesson.getLessonId();
                SimplifyStudentInfo studentInfo = UserService.getSimplifyStudentInfo(studentId);

                SimplifyLessonInfo lessonInfo = getSimplifyLessonInfo(lessonId);

                if (studentInfo != null) {
                    object.add("studentInfo", jsonParser.parse(gson.toJson(studentInfo, SimplifyStudentInfo.class)));
                }
                if (lessonInfo != null) {
                    object.add("lessonInfo", jsonParser.parse(gson.toJson(lessonInfo, SimplifyLessonInfo.class)));
                }
                return object;
            }
        }
        return null;
    }


    /**
     * 获取精简用户预约课程信息
     *
     * @param lessonId
     * @return
     */
    public static SimplifyLessonInfo getSimplifyLessonInfo(Integer lessonId) {
        SimplifyLessonInfo lessonInfo = null;
        if (lessonId == null) {
            return lessonInfo;
        }
        TalkLessonService lessonService = MelotBeanFactory.getBean("talkLessonService", TalkLessonService.class);
        if (lessonService != null) {
            Lesson lesson = lessonService.getLessonById(lessonId);
            if (lesson != null) {
                lessonInfo = new SimplifyLessonInfo();
                BeanUtils.copyProperties(lesson, lessonInfo);
                int lessonLevelId = lesson.getLessonLevel();
                int subLevelId = lesson.getSubLevel();

                if (StringUtils.isNotBlank(lessonInfo.getLessonUrl())) {
                    String lessonUrl = lessonInfo.getLessonUrl();
                    int index = lessonUrl.lastIndexOf("/");
                    if (index > 0) {
                        try {
                            String fileName = lessonUrl.substring(index + 1);
                            String filePath = lessonUrl.substring(0, index + 1);
                            lessonUrl = filePath + URLEncoder.encode(fileName, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    lessonInfo.setLessonUrl(lessonUrl);
                }

                if (StringUtils.isNotBlank(lessonInfo.getOriginalLessonUrl())) {
                    String originalLessonUrl = lessonInfo.getOriginalLessonUrl();
                    int index = originalLessonUrl.lastIndexOf("/");
                    if (index > 0) {
                        try {
                            String fileName = originalLessonUrl.substring(index + 1);
                            String filePath = originalLessonUrl.substring(0, index + 1);
                            originalLessonUrl = filePath + URLEncoder.encode(fileName, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    lessonInfo.setOriginalLessonUrl(originalLessonUrl);
                }

                // 通过lessonLevel、subLevel 获取lessonLevel信息
                LessonLevel lessonLevel = lessonService.getLessonLevelById(lessonLevelId, 0);
                if (lessonLevel != null) {
                    lessonInfo.setLessonLevelName(lessonLevel.getLevelName());
                }
                LessonLevel subLevel = lessonService.getLessonLevelById(subLevelId, lessonLevelId);
                if (subLevel != null) {
                    lessonInfo.setSubLevelName(subLevel.getLevelName());
                }
            }
        }
        return lessonInfo;
    }


    /**
     * 获取用户最近要上课或最后已上课
     *
     * @param userId
     * @return
     */
    public static Lesson getCurrentLessonInfo(Integer userId) {
        if (userId != null) {
            TalkLessonService lessonService = MelotBeanFactory.getBean("talkLessonService", TalkLessonService.class);
            if (lessonService != null) {
                return lessonService.getCurrentLessonByUserId(userId);
            }
        }
        return null;
    }

    /**
     * 获取老师完结课程汇总
     *
     * @param teacherId
     * @return
     */
    public static TeacherOverPeriodCount getTeacherOverPeriodCount(Integer teacherId) {
        if (teacherId != null) {
            TalkOrderService talkOrderService = MelotBeanFactory.getBean("talkOrderService", TalkOrderService.class);
            if (talkOrderService != null) {
                return talkOrderService.getTeacherOverPeriodCount(teacherId);
            }
        }
        return null;
    }

    /**
     * 获取老师完结课程汇总
     *
     * @param studentId
     * @return
     */
    public static JsonElement getDetailComment(Integer studentId, Integer periodId) {
        if (studentId != null && periodId != null) {
            TalkOrderService talkOrderService = MelotBeanFactory.getBean("talkOrderService", TalkOrderService.class);
            if (talkOrderService == null) {
                return null;
            }
            TalkLessonService talkLessonService = MelotBeanFactory.getBean("talkLessonService", TalkLessonService.class);
            if (talkLessonService == null) {
                return null;
            }
            Map<String, List<JsonObject>> detailCommentMap = new HashMap<String, List<JsonObject>>();
            // 获取可用详细问题列表
            List<DetailCommentQuestion> questions = talkLessonService.getDetailCommentQuestion(null, 1);
            if (questions != null && questions.size() > 0) {
                List<TeacherDetailComment> teacherDetailComments = talkOrderService.getDetailCommentList(periodId, studentId);
                Map<Integer, String> commentMap = new HashMap<Integer, String>();
                if (teacherDetailComments != null && teacherDetailComments.size() > 0) {
                    for (TeacherDetailComment teacherDetailComment : teacherDetailComments) {
                        commentMap.put(teacherDetailComment.getQuestionId(), teacherDetailComment.getTeacherAnswer());
                    }
                }
                List<JsonObject> commentList = null;
                JsonObject jsonObject = null;
                for (DetailCommentQuestion questionInfo : questions) {
                    String questionType = questionInfo.getQuestionType();
                    int questionId = questionInfo.getQuestionId();
                    String question = questionInfo.getQuestion();
                    if (detailCommentMap.containsKey(questionType)) {
                        commentList = detailCommentMap.get(questionType);
                    } else {
                        commentList = new ArrayList<JsonObject>();
                    }
                    jsonObject = new JsonObject();
                    jsonObject.addProperty("questionId", questionId);
                    jsonObject.addProperty("question", question);
                    if (commentMap.containsKey(questionId)) {
                        jsonObject.addProperty("comment", commentMap.get(questionId));
                    }
                    commentList.add(jsonObject);
                    detailCommentMap.put(questionType, commentList);
                }
                JsonArray jsonArray = new JsonArray();
                if (detailCommentMap != null && detailCommentMap.size() > 0) {
                    JsonObject jsonOb = null;
                    Type type = new TypeToken<List<JsonObject>>() {
                    }.getType();
                    for (Map.Entry<String, List<JsonObject>> entry : detailCommentMap.entrySet()) {
                        jsonOb = new JsonObject();
                        jsonOb.addProperty("questionType", entry.getKey());
                        jsonOb.add("detail", new Gson().toJsonTree(entry.getValue(), type));
                        jsonArray.add(jsonOb);
                    }
                }
                return jsonArray;
            }
        }
        return null;
    }


    /**
     * 获取对应时区查询每天开始
     *
     * @param dailyBeginTime
     * @param beginTime
     * @return
     */
    public static Date getDailyBeginTime(Date dailyBeginTime, Date beginTime) {
        // 开始时间大于等于开始时间，小于结束数据
        Date dailyEndTime = DateUtil.addOnField(dailyBeginTime, Calendar.DATE, 1);
        if (beginTime.getTime() >= dailyBeginTime.getTime() && beginTime.getTime() < dailyEndTime.getTime()) {
            return dailyBeginTime;
        } else {
            // 开始时间加一天
            dailyBeginTime = dailyEndTime;
            return getDailyBeginTime(dailyBeginTime, beginTime);
        }
    }
}
