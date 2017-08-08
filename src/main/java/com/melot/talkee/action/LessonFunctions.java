package com.melot.talkee.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.talkee.driver.domain.*;
import com.melot.talkee.driver.service.TalkLessonService;
import com.melot.talkee.driver.service.TalkOrderService;
import com.melot.talkee.driver.service.TalkPublishService;
import com.melot.talkee.driver.service.TalkUserService;
import com.melot.talkee.redis.UserRedisSource;
import com.melot.talkee.service.LessonService;
import com.melot.talkee.utils.CommonUtil;
import com.melot.talkee.utils.DateUtil;
import com.melot.talkee.utils.TagCodeEnum;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;

public class LessonFunctions {

    /**
     * 日志记录对象
     */
    private static Logger logger = Logger.getLogger(LessonFunctions.class);

    private static String DATE_FORMAT = "yyyyMMdd";

    /**
     * 课程发布 (40000101)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 详细列表
     */
    public JsonObject publishLesson(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 获取参数
        int teacherId, publishType, maxNum, platform;
        String token, lessonTime;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "p", 0, null, 1, Integer.MAX_VALUE);
            teacherId = CommonUtil.getJsonParamInt(jsonObject, "teacherId", 0, TagCodeEnum.USERID_INCORRECT, 1, Integer.MAX_VALUE);
            token = CommonUtil.getJsonParamString(jsonObject, "token", null, TagCodeEnum.TOKEN_INCORRECT, 1, Integer.MAX_VALUE);
            publishType = CommonUtil.getJsonParamInt(jsonObject, "publishType", 0, TagCodeEnum.PUBLISHTYPE_INCORRECT, -1, Integer.MAX_VALUE);
            maxNum = CommonUtil.getJsonParamInt(jsonObject, "maxNum", 0, TagCodeEnum.MAX_NUM_INCORRECT, -1, Integer.MAX_VALUE);
            lessonTime = CommonUtil.getJsonParamString(jsonObject, "lessonTime", null, TagCodeEnum.LESSON_TIME_INCORRECT, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

        try {
            boolean checkResult = UserRedisSource.checkToken(teacherId, token, platform);
            if (checkResult == false) {
                result.addProperty("TagCode", TagCodeEnum.TOKEN_EXPIRE);
                return result;
            }

            Map<Long, Long> tempLessonTime = new HashMap<Long, Long>();

            Map<String, Long> lessonTimeMap = null;
            List<String> lessonDateList = new ArrayList<String>();
            try {
                Type type = new TypeToken<Map<String, Long>>() {
                }.getType();
                lessonTimeMap = new Gson().fromJson(lessonTime, type);
                if (lessonTimeMap != null && lessonTimeMap.size() > 0) {
                    long currentTime = new Date().getTime();
                    String lessonDate = null;
                    for (Map.Entry<String, Long> entry : lessonTimeMap.entrySet()) {
                        // 开始时间大于结束时间 或者开始时间小于当前时间
                        long begineTime = Long.valueOf(entry.getKey());
                        long endTime = entry.getValue();

                        if (begineTime >= endTime || begineTime <= currentTime) {
                            result.addProperty("TagCode", TagCodeEnum.LESSON_TIME_INCORRECT);
                            return result;
                        }
                        lessonDate = DateUtil.formatDate(new Date(begineTime), DATE_FORMAT);
                        if (StringUtils.isNotBlank(lessonDate) && !lessonDateList.contains(lessonDate)) {
                            lessonDateList.add(lessonDate);
                        }
                        tempLessonTime.put(begineTime, endTime);
                    }
                } else {
                    result.addProperty("TagCode", TagCodeEnum.LESSON_TIME_INCORRECT);
                    return result;
                }
            } catch (Exception e) {
                result.addProperty("TagCode", TagCodeEnum.LESSON_TIME_INCORRECT);
                return result;
            }

            TalkPublishService publishService = MelotBeanFactory.getBean("talkPublishService", TalkPublishService.class);
            if (publishService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }

            String tagCode = publishService.publishLesson(teacherId, tempLessonTime, publishType, maxNum, null);
            if (StringUtils.isNotBlank(tagCode)) {
                if (tagCode.equals(TagCodeEnum.SUCCESS)) {
                    if (lessonDateList != null && lessonDateList.size() > 0) {
                        JsonArray jsonArray = new JsonArray();
                        List<PublishLesson> publishLessonList = null;
                        for (String lessonDate : lessonDateList) {
                            publishLessonList = publishService.getDailyPublishLessonList(teacherId, DateUtil.parseDateStringToDate(lessonDate, DATE_FORMAT), null, publishType);
                            if (publishLessonList != null && publishLessonList.size() > 0) {
                                for (PublishLesson detail : publishLessonList) {
                                    jsonArray.add(LessonService.getPublishLessonJson(detail));
                                }
                            }
                        }
                        result.add("publishLessonDetailList", jsonArray);
                        result.addProperty("publishType", publishType);
                        result.addProperty("teacherId", teacherId);
                    }
                }
                result.addProperty("TagCode", tagCode);
            } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_EXECUTE_EXCEPTION);
            }
        } catch (Exception e) {
            logger.error("publishLesson error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        // 返回结果
        return result;
    }


    /**
     * 获取每天课程发布详细列表  (40000102)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 详细列表
     */
    public JsonObject getDailyPublishLesson(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 获取参数
        int teacherId, publishType, platform;
        String token, lessonDate;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "p", 0, null, 1, Integer.MAX_VALUE);
            teacherId = CommonUtil.getJsonParamInt(jsonObject, "teacherId", 0, TagCodeEnum.USERID_INCORRECT, 1, Integer.MAX_VALUE);
            token = CommonUtil.getJsonParamString(jsonObject, "token", null, TagCodeEnum.TOKEN_INCORRECT, 1, Integer.MAX_VALUE);
            publishType = CommonUtil.getJsonParamInt(jsonObject, "publishType", 0, null, -1, Integer.MAX_VALUE);
            lessonDate = CommonUtil.getJsonParamString(jsonObject, "lessonDate", null, TagCodeEnum.LESSON_DATE_INCORRECT, -1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

        try {
            boolean checkResult = UserRedisSource.checkToken(teacherId, token, platform);
            if (checkResult == false) {
                result.addProperty("TagCode", TagCodeEnum.TOKEN_EXPIRE);
                return result;
            }
            Date tempDate = null;
            try {
                tempDate = new Date(Long.valueOf(lessonDate));
            } catch (Exception e) {
                result.addProperty("TagCode", TagCodeEnum.LESSON_DATE_INCORRECT);
                return result;
            }

            TalkPublishService publishService = MelotBeanFactory.getBean("talkPublishService", TalkPublishService.class);
            if (publishService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            int orderCount = 0;
            int publishCount = 0;
            List<PublishLesson> publishLessonList = publishService.getDailyPublishLessonList(teacherId, tempDate, null, publishType == 0 ? null : publishType);
            if (publishLessonList != null && publishLessonList.size() > 0) {
                publishCount = publishLessonList.size();
                JsonArray jsonArray = new JsonArray();
                for (PublishLesson detail : publishLessonList) {
                    if (detail.getState().intValue() == 2) {
                        orderCount++;
                    }
                    jsonArray.add(LessonService.getPublishLessonJson(detail));
                }
                result.add("publishLessonDetailList", jsonArray);
            }
            result.addProperty("orderCount", orderCount);
            result.addProperty("publishCount", publishCount);
            result.addProperty("teacherId", teacherId);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("getDailyPublishLesson error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        // 返回结果
        return result;
    }


    /**
     * 编辑发布信息 (40000103)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 详细列表
     */
    public JsonObject modifyPublishLesson(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 获取参数
        int teacherId, publishType, maxNum, platform;
        String token, lessonTime, lessonDate;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "p", 0, null, 1, Integer.MAX_VALUE);
            teacherId = CommonUtil.getJsonParamInt(jsonObject, "teacherId", 0, TagCodeEnum.USERID_INCORRECT, 1, Integer.MAX_VALUE);
            token = CommonUtil.getJsonParamString(jsonObject, "token", null, TagCodeEnum.TOKEN_INCORRECT, 1, Integer.MAX_VALUE);
            publishType = CommonUtil.getJsonParamInt(jsonObject, "publishType", 0, TagCodeEnum.PUBLISHTYPE_INCORRECT, -1, Integer.MAX_VALUE);
            lessonDate = CommonUtil.getJsonParamString(jsonObject, "lessonDate", null, TagCodeEnum.LESSON_DATE_INCORRECT, 1, Integer.MAX_VALUE);
            maxNum = CommonUtil.getJsonParamInt(jsonObject, "maxNum", 0, TagCodeEnum.MAX_NUM_INCORRECT, -1, Integer.MAX_VALUE);
            lessonTime = CommonUtil.getJsonParamString(jsonObject, "lessonTime", null, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

        try {
            boolean checkResult = UserRedisSource.checkToken(teacherId, token, platform);
            if (checkResult == false) {
                result.addProperty("TagCode", TagCodeEnum.TOKEN_EXPIRE);
                return result;
            }

            Date tempDate = null;
            try {
                tempDate = new Date(Long.valueOf(lessonDate));
            } catch (Exception e) {
                result.addProperty("TagCode", TagCodeEnum.LESSON_DATE_INCORRECT);
                return result;
            }

            Map<Long, Long> tempLessonTime = new HashMap<Long, Long>();
            Map<String, Long> lessonTimeMap = null;
            try {
                if (StringUtils.isNotBlank(lessonTime)) {
                    Type type = new TypeToken<Map<String, Long>>() {
                    }.getType();
                    lessonTimeMap = new Gson().fromJson(lessonTime, type);
                    if (lessonTimeMap != null && lessonTimeMap.size() > 0) {
                        for (Map.Entry<String, Long> entry : lessonTimeMap.entrySet()) {
                            // 开始时间大于结束时间 或者开始时间小于当前时间
                            long begineTime = Long.valueOf(entry.getKey());
                            long endTime = entry.getValue();
                            if (begineTime >= endTime) {
                                result.addProperty("TagCode", TagCodeEnum.LESSON_TIME_INCORRECT);
                                return result;
                            }
                            tempLessonTime.put(begineTime, endTime);
                        }
                    } else {
                        result.addProperty("TagCode", TagCodeEnum.LESSON_TIME_INCORRECT);
                        return result;
                    }
                }
            } catch (Exception e) {
                result.addProperty("TagCode", TagCodeEnum.LESSON_TIME_INCORRECT);
                return result;
            }

            TalkPublishService publishService = MelotBeanFactory.getBean("talkPublishService", TalkPublishService.class);
            if (publishService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            int orderCount = 0;
            int publishCount = 0;
            List<PublishLesson> publishLessonList = publishService.modifyPublishLesson(teacherId, tempDate, tempLessonTime, publishType, maxNum, null);
            if (publishLessonList != null && publishLessonList.size() > 0) {
                publishCount = publishLessonList.size();
                JsonArray jsonArray = new JsonArray();
                for (PublishLesson detail : publishLessonList) {
                    if (detail.getState().intValue() == 2) {
                        orderCount++;
                    }
                    jsonArray.add(LessonService.getPublishLessonJson(detail));
                }
                result.add("publishLessonDetailList", jsonArray);
            }
            result.addProperty("orderCount", orderCount);
            result.addProperty("publishCount", publishCount);
            result.addProperty("teacherId", teacherId);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("publishLesson error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        // 返回结果
        return result;
    }


    /**
     * 通过时间获取课程发布列表信息   (40000104)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 详细列表
     */
    public JsonObject getPublishLessonList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 获取参数
        int teacherId, publishType, platform;
        String token, beginTime, endTime;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "p", 0, null, 1, Integer.MAX_VALUE);
            teacherId = CommonUtil.getJsonParamInt(jsonObject, "teacherId", 0, TagCodeEnum.USERID_INCORRECT, 1, Integer.MAX_VALUE);
            token = CommonUtil.getJsonParamString(jsonObject, "token", null, TagCodeEnum.TOKEN_INCORRECT, 1, Integer.MAX_VALUE);
            publishType = CommonUtil.getJsonParamInt(jsonObject, "publishType", 0, null, -1, Integer.MAX_VALUE);
            beginTime = CommonUtil.getJsonParamString(jsonObject, "beginTime", null, TagCodeEnum.BEGIN_TIME_INCORRECT, 1, Integer.MAX_VALUE);
            endTime = CommonUtil.getJsonParamString(jsonObject, "endTime", null, TagCodeEnum.END_TIME_INCORRECT, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

        try {
            boolean checkResult = UserRedisSource.checkToken(teacherId, token, platform);
            if (checkResult == false) {
                result.addProperty("TagCode", TagCodeEnum.TOKEN_EXPIRE);
                return result;
            }
            Date tempBeginDate = null;
            try {
                tempBeginDate = new Date(Long.valueOf(beginTime));
            } catch (Exception e) {
                result.addProperty("TagCode", TagCodeEnum.BEGIN_TIME_INCORRECT);
                return result;
            }

            Date tempEndDate = null;
            try {
                tempEndDate = new Date(Long.valueOf(endTime));
            } catch (Exception e) {
                result.addProperty("TagCode", TagCodeEnum.END_TIME_INCORRECT);
                return result;
            }

            TalkPublishService publishService = MelotBeanFactory.getBean("talkPublishService", TalkPublishService.class);
            if (publishService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }

            List<PublishLessonCount> publishLessonList = publishService.getPublishLessonList(teacherId, tempBeginDate, tempEndDate, null, publishType == 0 ? null : publishType);
            if (publishLessonList != null && publishLessonList.size() > 0) {

                // 通过开始时间进行排序
                Collections.sort(publishLessonList, new Comparator<PublishLessonCount>() {
                    @Override
                    public int compare(PublishLessonCount a, PublishLessonCount b) {
                        Long one = a.getLessonDate();
                        Long two = b.getLessonDate();
                        return one.compareTo(two);
                    }
                });

                result.add("publishLessonList", new Gson().toJsonTree(publishLessonList));
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("getPublishLessonList error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        // 返回结果
        return result;
    }


    /**
     * 课程预约   (40000201)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 详细列表
     */
    public JsonObject orderLesson(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId;
        String periodList;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, 1, Integer.MAX_VALUE);
            periodList = CommonUtil.getJsonParamString(jsonObject, "periodList", null, TagCodeEnum.PERIOD_LIST_INCORRECT, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

        try {
            List<Integer> list = new ArrayList<Integer>();
            try {
                List<String> periods = Arrays.asList(periodList.split(","));
                if (periods == null || periods.size() == 0) {
                    result.addProperty("TagCode", TagCodeEnum.PERIOD_LIST_INCORRECT);
                    return result;
                } else {
                    for (String periodId : periods) {
                        list.add(Integer.valueOf(periodId));
                    }
                }
            } catch (Exception e) {
                result.addProperty("TagCode", TagCodeEnum.PERIOD_LIST_INCORRECT);
                return result;
            }

            TalkOrderService orderService = MelotBeanFactory.getBean("talkOrderService", TalkOrderService.class);
            if (orderService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            String tagCode = orderService.orderLesson(userId, list, userId);
            if (StringUtils.isNotBlank(tagCode)) {
                if (tagCode.equals(TagCodeEnum.SUCCESS)) {
                    List<OrderLesson> orderLessonList = orderService.getOrderLessonByPeriodList(list);
                    JsonArray jsonArray = new JsonArray();
                    if (orderLessonList != null && orderLessonList.size() > 0) {
                        for (OrderLesson detail : orderLessonList) {
                            jsonArray.add(LessonService.getOrderLessonJson(detail));
                        }
                        result.add("orderLessonList", jsonArray);
                    }
                }
                result.addProperty("TagCode", tagCode);
            } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_EXECUTE_EXCEPTION);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("orderLesson error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        // 返回结果
        return result;
    }

    /**
     * 获取课程预约列表   (40000202)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 详细列表
     */
    public JsonObject getOrderLesson(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId, publishType, queryState, start, offset;
        String beginTime, endTime, order;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            publishType = CommonUtil.getJsonParamInt(jsonObject, "publishType", 0, null, -1, Integer.MAX_VALUE);
            queryState = CommonUtil.getJsonParamInt(jsonObject, "queryState", 0, TagCodeEnum.QUERY_STATE_INCORRECT, -1, Integer.MAX_VALUE);
            beginTime = CommonUtil.getJsonParamString(jsonObject, "beginTime", null, null, 1, Integer.MAX_VALUE);
            endTime = CommonUtil.getJsonParamString(jsonObject, "endTime", null, null, 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, TagCodeEnum.START_INCORRECT, -1, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 10, TagCodeEnum.OFFSET_INCORRECT, -1, Integer.MAX_VALUE);
            order = CommonUtil.getJsonParamString(jsonObject, "order", null, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

        try {
            Date tempBeginDate = null;
            if (StringUtils.isNotBlank(beginTime)) {
                try {
                    tempBeginDate = new Date(Long.valueOf(beginTime));
                } catch (Exception e) {
                    result.addProperty("TagCode", TagCodeEnum.BEGIN_TIME_INCORRECT);
                    return result;
                }
            }
            Date tempEndDate = null;
            if (StringUtils.isNotBlank(endTime)) {
                try {
                    tempEndDate = new Date(Long.valueOf(endTime));
                } catch (Exception e) {
                    result.addProperty("TagCode", TagCodeEnum.END_TIME_INCORRECT);
                    return result;
                }
            }

            TalkOrderService orderService = MelotBeanFactory.getBean("talkOrderService", TalkOrderService.class);
            if (orderService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            int totalCount = 0;
            Pager<OrderLesson> pager = orderService.getPagerOrderLessonList(userId, queryState, tempBeginDate, tempEndDate, publishType == 0 ? null : publishType, start, offset, order);
            if (pager != null) {
                totalCount = pager.getTotalCount();
                List<OrderLesson> orderLessonList = pager.getPageItems();
                if (orderLessonList != null && orderLessonList.size() > 0) {
                    System.out.println("orderLessonList");
                    JsonArray jsonArray = new JsonArray();
                    for (OrderLesson detail : orderLessonList) {
                        jsonArray.add(LessonService.getOrderLessonJson(detail));
                    }
                    result.add("orderLessonList", jsonArray);
                }
            }
            result.addProperty("totalCount", totalCount);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getOrderLesson error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        // 返回结果
        return result;
    }


    /**
     * 老师端待上课表   (40000205)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 详细列表
     */
    public JsonObject getAwaitLessonList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId, publishType, start, offset;
        String beginTime, endTime, order;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            publishType = CommonUtil.getJsonParamInt(jsonObject, "publishType", 0, null, -1, Integer.MAX_VALUE);
            beginTime = CommonUtil.getJsonParamString(jsonObject, "beginTime", null, null, 1, Integer.MAX_VALUE);
            endTime = CommonUtil.getJsonParamString(jsonObject, "endTime", null, null, 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, TagCodeEnum.START_INCORRECT, -1, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 10, TagCodeEnum.OFFSET_INCORRECT, -1, Integer.MAX_VALUE);
            order = CommonUtil.getJsonParamString(jsonObject, "order", null, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

        try {
            Date tempBeginDate = null;
            if (StringUtils.isNotBlank(beginTime)) {
                try {
                    tempBeginDate = new Date(Long.valueOf(beginTime));
                } catch (Exception e) {
                    result.addProperty("TagCode", TagCodeEnum.BEGIN_TIME_INCORRECT);
                    return result;
                }
            }
            Date tempEndDate = null;
            if (StringUtils.isNotBlank(endTime)) {
                try {
                    tempEndDate = new Date(Long.valueOf(endTime));
                } catch (Exception e) {
                    result.addProperty("TagCode", TagCodeEnum.END_TIME_INCORRECT);
                    return result;
                }
            }

            TalkOrderService orderService = MelotBeanFactory.getBean("talkOrderService", TalkOrderService.class);
            if (orderService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            int totalCount = 0;
            Pager<OrderLesson> pager = orderService.getAwaitLessonList(userId, tempBeginDate, tempEndDate, publishType == 0 ? null : publishType, start, offset, order);
            if (pager != null) {
                totalCount = pager.getTotalCount();
                List<OrderLesson> orderLessonList = pager.getPageItems();
                if (orderLessonList != null && orderLessonList.size() > 0) {
                    JsonArray jsonArray = new JsonArray();
                    for (OrderLesson detail : orderLessonList) {
                        jsonArray.add(LessonService.getOrderLessonJson(detail));
                    }
                    result.add("orderLessonList", jsonArray);
                }
            }
            result.addProperty("totalCount", totalCount);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getOrderLesson error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        // 返回结果
        return result;
    }

    /**
     * 老师端已上课表   (40000205)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 详细列表
     */
    public JsonObject getOverLessonList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId, publishType, start, offset;
        String beginTime, endTime, order;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            publishType = CommonUtil.getJsonParamInt(jsonObject, "publishType", 0, null, -1, Integer.MAX_VALUE);
            beginTime = CommonUtil.getJsonParamString(jsonObject, "beginTime", null, null, 1, Integer.MAX_VALUE);
            endTime = CommonUtil.getJsonParamString(jsonObject, "endTime", null, null, 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, TagCodeEnum.START_INCORRECT, -1, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 10, TagCodeEnum.OFFSET_INCORRECT, -1, Integer.MAX_VALUE);
            order = CommonUtil.getJsonParamString(jsonObject, "order", null, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

        try {
            Date tempBeginDate = null;
            if (StringUtils.isNotBlank(beginTime)) {
                try {
                    tempBeginDate = new Date(Long.valueOf(beginTime));
                } catch (Exception e) {
                    result.addProperty("TagCode", TagCodeEnum.BEGIN_TIME_INCORRECT);
                    return result;
                }
            }
            Date tempEndDate = null;
            if (StringUtils.isNotBlank(endTime)) {
                try {
                    tempEndDate = new Date(Long.valueOf(endTime));
                } catch (Exception e) {
                    result.addProperty("TagCode", TagCodeEnum.END_TIME_INCORRECT);
                    return result;
                }
            }

            TalkOrderService orderService = MelotBeanFactory.getBean("talkOrderService", TalkOrderService.class);
            if (orderService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            int totalCount = 0;
            Pager<OrderLesson> pager = orderService.getOverLessonList(userId, tempBeginDate, tempEndDate, publishType == 0 ? null : publishType, start, offset, order);
            if (pager != null) {
                totalCount = pager.getTotalCount();
                List<OrderLesson> orderLessonList = pager.getPageItems();
                if (orderLessonList != null && orderLessonList.size() > 0) {
                    JsonArray jsonArray = new JsonArray();
                    for (OrderLesson detail : orderLessonList) {
                        jsonArray.add(LessonService.getOrderLessonJson(detail));
                    }
                    result.add("orderLessonList", jsonArray);
                }
            }
            result.addProperty("totalCount", totalCount);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getOrderLesson error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        // 返回结果
        return result;
    }

    /**
     * 老师端已上课表   (40000205)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 详细列表
     */
    public JsonObject getLeaveLessonList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId, publishType, start, offset;
        String beginTime, endTime, order;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            publishType = CommonUtil.getJsonParamInt(jsonObject, "publishType", 0, null, -1, Integer.MAX_VALUE);
            beginTime = CommonUtil.getJsonParamString(jsonObject, "beginTime", null, null, 1, Integer.MAX_VALUE);
            endTime = CommonUtil.getJsonParamString(jsonObject, "endTime", null, null, 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, TagCodeEnum.START_INCORRECT, -1, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 10, TagCodeEnum.OFFSET_INCORRECT, -1, Integer.MAX_VALUE);
            order = CommonUtil.getJsonParamString(jsonObject, "order", null, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

        try {
            Date tempBeginDate = null;
            if (StringUtils.isNotBlank(beginTime)) {
                try {
                    tempBeginDate = new Date(Long.valueOf(beginTime));
                } catch (Exception e) {
                    result.addProperty("TagCode", TagCodeEnum.BEGIN_TIME_INCORRECT);
                    return result;
                }
            }
            Date tempEndDate = null;
            if (StringUtils.isNotBlank(endTime)) {
                try {
                    tempEndDate = new Date(Long.valueOf(endTime));
                } catch (Exception e) {
                    result.addProperty("TagCode", TagCodeEnum.END_TIME_INCORRECT);
                    return result;
                }
            }

            TalkOrderService orderService = MelotBeanFactory.getBean("talkOrderService", TalkOrderService.class);
            if (orderService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            int totalCount = 0;
            Pager<OrderLesson> pager = orderService.getLeaveLessonList(userId, tempBeginDate, tempEndDate, publishType == 0 ? null : publishType, start, offset, order);
            if (pager != null) {
                totalCount = pager.getTotalCount();
                List<OrderLesson> orderLessonList = pager.getPageItems();
                if (orderLessonList != null && orderLessonList.size() > 0) {
                    JsonArray jsonArray = new JsonArray();
                    for (OrderLesson detail : orderLessonList) {
                        jsonArray.add(LessonService.getOrderLessonJson(detail));
                    }
                    result.add("orderLessonList", jsonArray);
                }
            }
            result.addProperty("totalCount", totalCount);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getOrderLesson error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        // 返回结果
        return result;
    }

    /**
     * 更新学生请假，老师确认的状态   (40000206)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 详细列表
     */
    public JsonObject updateStudentCancelType(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId, histId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            histId = CommonUtil.getJsonParamInt(jsonObject, "histId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);

        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

        try {

            TalkLessonService talkLessonService = MelotBeanFactory.getBean("talkLessonService", TalkLessonService.class);
            if (talkLessonService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            talkLessonService.updateStdentCancelType(histId, 0);

            result.addProperty("TagCode", TagCodeEnum.SUCCESS);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getOrderLesson error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        // 返回结果
        return result;
    }


    /**
     * 取消课程预约列表   (40000203)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 详细列表
     */
    public JsonObject cancleOrder(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId, periodId;
        String cancleReason;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            periodId = CommonUtil.getJsonParamInt(jsonObject, "periodId", 0, TagCodeEnum.PERIOD_ID_INCORRECT, -1, Integer.MAX_VALUE);
            cancleReason = CommonUtil.getJsonParamString(jsonObject, "cancleReason", null, TagCodeEnum.CANCLE_REASON_INCORRECT, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

        try {
            TalkOrderService orderService = MelotBeanFactory.getBean("talkOrderService", TalkOrderService.class);
            if (orderService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            String tagCode = orderService.cancleOrder(userId, periodId, cancleReason, userId);
            if (StringUtils.isNotBlank(tagCode)) {
                result.addProperty("TagCode", tagCode);
            } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_EXECUTE_EXCEPTION);
            }
        } catch (Exception e) {
            logger.error("orderLesson error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        // 返回结果
        return result;
    }


    /**
     * 获取学生课程预约列表   (40000204)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 详细列表
     */
    public JsonObject getStudentLesson(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId;
        String beginTime, endTime;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            beginTime = CommonUtil.getJsonParamString(jsonObject, "beginTime", null, TagCodeEnum.BEGIN_TIME_INCORRECT, 1, Integer.MAX_VALUE);
            endTime = CommonUtil.getJsonParamString(jsonObject, "endTime", null, TagCodeEnum.END_TIME_INCORRECT, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

        try {
            Date tempBeginDate = null;
            if (StringUtils.isNotBlank(beginTime)) {
                try {
                    tempBeginDate = new Date(Long.valueOf(beginTime));
                } catch (Exception e) {
                    result.addProperty("TagCode", TagCodeEnum.BEGIN_TIME_INCORRECT);
                    return result;
                }
            }
            Date tempEndDate = null;
            if (StringUtils.isNotBlank(endTime)) {
                try {
                    tempEndDate = new Date(Long.valueOf(endTime));
                } catch (Exception e) {
                    result.addProperty("TagCode", TagCodeEnum.END_TIME_INCORRECT);
                    return result;
                }
            }

            TalkOrderService orderService = MelotBeanFactory.getBean("talkOrderService", TalkOrderService.class);
            if (orderService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            List<OrderLesson> orderLessonList = orderService.getOrderLessonList(userId, null, tempBeginDate, tempEndDate, null);

            Map<Long, Map<String, LinkedList<OrderLesson>>> weekOrderMap = new HashMap<Long, Map<String, LinkedList<OrderLesson>>>();
            Date dailyBeginTime = tempBeginDate;
            Map<String, LinkedList<OrderLesson>> dailyOrderLessonMap = null;

            String am = "am";
            String pm = "pm";
            String night = "night";

            // 按查询天数，进行数据补录
            while (dailyBeginTime.getTime() < tempEndDate.getTime()) {
                dailyOrderLessonMap = new HashMap<String, LinkedList<OrderLesson>>();
                dailyOrderLessonMap.put(am, new LinkedList<OrderLesson>());
                dailyOrderLessonMap.put(pm, new LinkedList<OrderLesson>());
                dailyOrderLessonMap.put(night, new LinkedList<OrderLesson>());
                weekOrderMap.put(dailyBeginTime.getTime(), dailyOrderLessonMap);

                dailyBeginTime = DateUtil.addOnField(dailyBeginTime, Calendar.DATE, 1);
            }


            int orderCount = 0;
            if (orderLessonList != null && orderLessonList.size() > 0) {
                // 按时间递增排序
                Collections.sort(orderLessonList, new Comparator<OrderLesson>() {
                    @Override
                    public int compare(OrderLesson o1, OrderLesson o2) {
                        long objTime1 = o1.getBeginTime().getTime();
                        long objTime2 = o2.getBeginTime().getTime();
                        if (objTime2 < objTime1) {
                            return 1;
                        } else if (objTime2 > objTime1) {
                            return -1;
                        }
                        return 0;
                    }
                });

                Date tempBegin = null;
                // 初始化每天开始时间
                dailyBeginTime = tempBeginDate;
                // 初始查询每天结束时间
                Date dailyEndTime = DateUtil.addOnField(dailyBeginTime, Calendar.DATE, 1);
                LinkedList<OrderLesson> intervalList = null;
                for (OrderLesson detail : orderLessonList) {
                    // 发布课程开始时间
                    tempBegin = detail.getBeginTime();
                    // 如果发布开始时间消息大于结束时间，则从新获取每天开始时间
                    if (tempBegin.getTime() >= dailyEndTime.getTime()) {
                        dailyBeginTime = LessonService.getDailyBeginTime(dailyBeginTime, tempBegin);
                        dailyEndTime = DateUtil.addOnField(dailyBeginTime, Calendar.DATE, 1);
                    }
                    if (tempBegin.getTime() >= dailyBeginTime.getTime() && tempBegin.getTime() < dailyEndTime.getTime()) {
                        if (weekOrderMap.containsKey(dailyBeginTime.getTime())) {
                            dailyOrderLessonMap = weekOrderMap.get(dailyBeginTime.getTime());
                            if (detail.getLessonState() == 3) {
                                continue;
                            }
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(detail.getBeginTime());
                            int hour = cal.get(Calendar.HOUR_OF_DAY);
                            if (hour >= 0 && hour < 12) {
                                intervalList = dailyOrderLessonMap.get(am);
                                intervalList.add(detail);
                                dailyOrderLessonMap.put(am, intervalList);
                            } else if (hour >= 12 && hour < 18) {
                                intervalList = dailyOrderLessonMap.get(pm);
                                intervalList.add(detail);
                                dailyOrderLessonMap.put(pm, intervalList);
                            } else if (hour >= 18 && hour < 24) {
                                intervalList = dailyOrderLessonMap.get(night);
                                intervalList.add(detail);
                                dailyOrderLessonMap.put(night, intervalList);
                            }
                            weekOrderMap.put(dailyBeginTime.getTime(), dailyOrderLessonMap);
                            orderCount++;
                        }
                    }
                }
            }

            List<Map.Entry<Long, Map<String, LinkedList<OrderLesson>>>> entryList = new ArrayList<Map.Entry<Long, Map<String, LinkedList<OrderLesson>>>>(weekOrderMap.entrySet());
            Collections.sort(entryList, new Comparator<Map.Entry<Long, Map<String, LinkedList<OrderLesson>>>>() {
                @Override
                public int compare(Entry<Long, Map<String, LinkedList<OrderLesson>>> o1, Entry<Long, Map<String, LinkedList<OrderLesson>>> o2) {
                    if (o2.getKey().longValue() < o1.getKey().longValue()) {
                        return 1;
                    } else if (o2.getKey().longValue() > o1.getKey().longValue()) {
                        return -1;
                    }
                    return 0;
                }
            });

            // 按一天 上午、下午、晚上进行分组
            LinkedList<LinkedList<JsonArray>> dailyList = new LinkedList<LinkedList<JsonArray>>();
            LinkedList<JsonArray> tempList = null;
            for (Map.Entry<Long, Map<String, LinkedList<OrderLesson>>> entry : entryList) {

                dailyOrderLessonMap = entry.getValue();
                tempList = new LinkedList<JsonArray>();

                tempList.add(getIntervalList(dailyOrderLessonMap.get(am)));
                tempList.add(getIntervalList(dailyOrderLessonMap.get(pm)));
                tempList.add(getIntervalList(dailyOrderLessonMap.get(night)));

                dailyList.add(tempList);
            }

            Type type = new TypeToken<LinkedList<LinkedList<JsonArray>>>() {
            }.getType();

            result.add("studentLessonList", new Gson().toJsonTree(dailyList, type));
            result.addProperty("orderCount", orderCount);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("orderLesson error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        // 返回结果
        return result;
    }


    private JsonArray getIntervalList(LinkedList<OrderLesson> intervalList) {
        JsonArray jsonArray = new JsonArray();
        if (intervalList != null && intervalList.size() > 0) {
            for (OrderLesson orderLesson : intervalList) {
                jsonArray.add(LessonService.getOrderLessonJson(orderLesson));
            }
        }
        return jsonArray;
    }

    /**
     * 获取课件列表  (40110301)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 详细列表
     */
    public JsonObject getCourseware(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
//		if (!checkTag) {
//			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//			return result;
//		}
        // 获取参数
        int lessonId;
        try {
            lessonId = CommonUtil.getJsonParamInt(jsonObject, "lessonId", 0, TagCodeEnum.LESSON_ID_INCORRECT, -1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }
        try {
            TalkLessonService talkLessonService = MelotBeanFactory.getBean("talkLessonService", TalkLessonService.class);
            if (talkLessonService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            List<Courseware> coursewares = talkLessonService.getCourseware(lessonId);
            if (coursewares != null && coursewares.size() > 0) {
                Collections.sort(coursewares, new Comparator<Courseware>() {
                    @Override
                    public int compare(Courseware o1, Courseware o2) {
                        return o1.getGrank().compareTo(o2.getGrank());
                    }
                });
                LinkedList<String> fileList = new LinkedList<String>();
                for (Courseware courseware : coursewares) {
                    if (StringUtils.isNotBlank(courseware.getCoswUrl())) {
                        String lessonUrl = courseware.getCoswUrl();
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
                        fileList.add(lessonUrl);
                    }
                }
                Type type = new TypeToken<LinkedList<String>>() {
                }.getType();
                result.add("coursewareList", new Gson().toJsonTree(fileList, type));
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("orderLesson error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        // 返回结果
        return result;
    }

    /**
     * 获取详细评论  (40000401)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 详细列表
     */
    public JsonObject getDetailComment(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId, studentId, periodId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            studentId = CommonUtil.getJsonParamInt(jsonObject, "studentId", 0, TagCodeEnum.STUDENT_ID_INCORRECT, -1, Integer.MAX_VALUE);
            periodId = CommonUtil.getJsonParamInt(jsonObject, "periodId", 0, TagCodeEnum.PERIOD_ID_INCORRECT, -1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }
        try {
            TalkUserService talkUserService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
            if (talkUserService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            UserInfo userInfo = talkUserService.getUserInfoByUserId(userId);
            if (userInfo == null) {
                result.addProperty("TagCode", TagCodeEnum.USERID_INCORRECT);
                return result;
            }
            result.add("detailComment", LessonService.getDetailComment(studentId, periodId));
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("orderLesson error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        // 返回结果
        return result;
    }

    /**
     * 添加评论  (40000402)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 详细列表
     */
    public JsonObject addComment(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId, studentId, periodId, questionId;
        String comment;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            studentId = CommonUtil.getJsonParamInt(jsonObject, "studentId", 0, TagCodeEnum.STUDENT_ID_INCORRECT, -1, Integer.MAX_VALUE);
            periodId = CommonUtil.getJsonParamInt(jsonObject, "periodId", 0, TagCodeEnum.PERIOD_ID_INCORRECT, -1, Integer.MAX_VALUE);
            questionId = CommonUtil.getJsonParamInt(jsonObject, "questionId", 0, null, -1, Integer.MAX_VALUE);
            comment = CommonUtil.getJsonParamString(jsonObject, "comment", null, TagCodeEnum.COMMENT_INCORRECT, -1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }
        try {
            TalkUserService talkUserService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
            if (talkUserService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }

            TalkOrderService talkOrderService = MelotBeanFactory.getBean("talkOrderService", TalkOrderService.class);
            if (talkOrderService == null) {
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                return result;
            }
            UserInfo userInfo = talkUserService.getUserInfoByUserId(userId);
            if (userInfo == null) {
                result.addProperty("TagCode", TagCodeEnum.USERID_INCORRECT);
                return result;
            }
            // 学生禁止给自己添加评论
            if (userInfo.getAccountType() == null || userInfo.getAccountType().intValue() == 1) {
                result.addProperty("TagCode", TagCodeEnum.NO_PERMISSION);
                return result;
            }
            // 是否是学生对于课程老师或cr


            int resultCode = talkOrderService.addComment(periodId, studentId, questionId == 0 ? null : questionId, comment);
            if (resultCode > 0) {

            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("orderLesson error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        // 返回结果
        return result;
    }

    /**
     * 获取用户子等级列表   (40000403)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 详细列表
     */
    public JsonObject getUserSubLevelList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId, studentId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            studentId = CommonUtil.getJsonParamInt(jsonObject, "studentId", 0, TagCodeEnum.STUDENT_ID_INCORRECT, -1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }
        try {
            TalkUserService talkUserService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
            TalkLessonService talkLessonService = MelotBeanFactory.getBean("talkLessonService", TalkLessonService.class);
            TeacherInfo teacher = talkUserService.getTeacherInfoByUserId(userId);
            if (teacher == null) {
                result.addProperty("TagCode", TagCodeEnum.USER_IS_NOT_TEACHER);
            } else {
                StudentInfo student = talkUserService.getStudentInfoByUserId(studentId);
                if (student == null) {
                    result.addProperty("TagCode", TagCodeEnum.STUDENT_NOT_EXIST);
                } else {
                    result.addProperty("userLevel", student.getUserLevel());
                    result.addProperty("userSubLevel", student.getSubLevel());
                    List<LessonLevel> levels = talkLessonService.selectByParentLevel(student.getUserLevel());
                    result.addProperty("subLevelList", JSONArray.toJSONString(levels));
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            logger.info("LessFuntions.getUserSubLevelList.request.fail " + e.getLocalizedMessage());

        }
        return result;
    }


    /**
     * 修改用户当前待上课程    (40000404)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 详细列表
     */
    public JsonObject modifyUserCurrentLesson(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId, periodId, studentId, subLevel;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            periodId = CommonUtil.getJsonParamInt(jsonObject, "periodId", 0, TagCodeEnum.PERIOD_ID_INCORRECT, -1, Integer.MAX_VALUE);
            studentId = CommonUtil.getJsonParamInt(jsonObject, "studentId", 0, TagCodeEnum.STUDENT_ID_INCORRECT, -1, Integer.MAX_VALUE);
            subLevel = CommonUtil.getJsonParamInt(jsonObject, "subLevel", 0, TagCodeEnum.SUBLEVEL_INCORRECT, -1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

        //TODO  业务暂放
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }


    /**
     * 获取课程大等级(40000405)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getParentLevelList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }
        try {
            TalkUserService talkUserService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
            TalkLessonService talkLessonService = MelotBeanFactory.getBean("talkLessonService", TalkLessonService.class);
            TeacherInfo teacher = talkUserService.getTeacherInfoByUserId(userId);
            if (teacher == null) {
                result.addProperty("TagCode", TagCodeEnum.USER_IS_NOT_TEACHER);
                return result;
            } else {
                if (talkLessonService == null) {
                    result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                    return result;
                }
                List<LessonLevel> lessonLevelList = talkLessonService.getParentLevel();

                result.addProperty("result", JSONArray.toJSONString(lessonLevelList));
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            logger.info("LessFuntions.getParentLevelList.request.fail " + e.getLocalizedMessage());
        }
        return result;

    }

    /**
     * 根据课程等级获取课程信息列表(40000406)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getLessonInfoList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId, lessonLevel, start, offset;
        String order;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            lessonLevel = CommonUtil.getJsonParamInt(jsonObject, "lessonLevel", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);

            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, TagCodeEnum.START_INCORRECT, -1, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 10, TagCodeEnum.OFFSET_INCORRECT, -1, Integer.MAX_VALUE);
            order = CommonUtil.getJsonParamString(jsonObject, "order", null, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
            return result;
        }

        try {
            TalkUserService talkUserService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
            TalkLessonService talkLessonService = MelotBeanFactory.getBean("talkLessonService", TalkLessonService.class);
            TeacherInfo teacher = talkUserService.getTeacherInfoByUserId(userId);
            if (teacher == null) {
                result.addProperty("TagCode", TagCodeEnum.USER_IS_NOT_TEACHER);
                return result;
            } else {
                if (talkLessonService == null) {
                    result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                    return result;
                }
                int totalCount = 0;
                Pager<Lesson> pager = talkLessonService.getLessonInfoList(lessonLevel, start, offset, order);
                if (pager != null) {
                    totalCount = pager.getTotalCount();
                    List<Lesson> lessonList = pager.getPageItems();
                    if (lessonList != null && lessonList.size() > 0) {
                        JsonArray jsonArray = new JsonArray();
                        for (Lesson lesson : lessonList) {
                            jsonArray.add(LessonService.getLessonJson(lesson));
                        }
                        result.add("result", jsonArray);
                    }
                }
                result.addProperty("totalCount", totalCount);
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);

            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getLessonInfoList error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }

        return result;
    }

    /**
     * 老师添加评语(40000407)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject createTeacherComment(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId = 0, histId = 0,periodId = 0, studentId = 0, teacherId = 0, pronunciation = 0, participation = 0, comprehension = 0, fluency = 0, creativity = 0;
        String summary = null, suggestion = null, other = null;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            histId = CommonUtil.getJsonParamInt(jsonObject, "histId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            teacherId = CommonUtil.getJsonParamInt(jsonObject, "teacherId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            periodId = CommonUtil.getJsonParamInt(jsonObject, "periodId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);

            studentId = CommonUtil.getJsonParamInt(jsonObject, "studentId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);

            pronunciation = CommonUtil.getJsonParamInt(jsonObject, "pronunciation", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            participation = CommonUtil.getJsonParamInt(jsonObject, "participation", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            comprehension = CommonUtil.getJsonParamInt(jsonObject, "comprehension", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            fluency = CommonUtil.getJsonParamInt(jsonObject, "fluency", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            creativity = CommonUtil.getJsonParamInt(jsonObject, "creativity", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);

            summary = CommonUtil.getJsonParamString(jsonObject, "summary", null, null, 1, Integer.MAX_VALUE);
            suggestion = CommonUtil.getJsonParamString(jsonObject, "suggestion", null, null, 1, Integer.MAX_VALUE);
            other = CommonUtil.getJsonParamString(jsonObject, "other", null, null, 1, Integer.MAX_VALUE);

        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
        }

        try {
            TalkUserService talkUserService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
            TalkLessonService talkLessonService = MelotBeanFactory.getBean("talkLessonService", TalkLessonService.class);
            TeacherInfo teacher = talkUserService.getTeacherInfoByUserId(userId);
            if (teacher == null) {
                result.addProperty("TagCode", TagCodeEnum.USER_IS_NOT_TEACHER);
            } else {
                if (talkLessonService == null) {
                    result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                    return result;
                }
            }

            TeacherCommentToStudent comment = new TeacherCommentToStudent();
            comment.setHistId(histId);
            comment.setTeacherId(teacherId);
            comment.setPeriodId(periodId);
            comment.setStudentId(studentId);
            comment.setPronunciation(pronunciation);
            comment.setParticipation(participation);
            comment.setComprehension(comprehension);
            comment.setCreativity(creativity);
            comment.setFluency(fluency);
            comment.setSuggestion(suggestion);
            comment.setOther(other);
            comment.setSummary(summary);

            int code = talkLessonService.createTeacherComment(comment);
            if (code > 0) {
                result.addProperty("result", code);
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            } else {
                result.addProperty("result", code);
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("createTeacherComment error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }

        return result;
    }

    /**
     * 查看老师评语(40000408)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getTeacherCommentInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId = 0, teacherId = 0, periodId = 0, studentId=0;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            teacherId = CommonUtil.getJsonParamInt(jsonObject, "teacherId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            periodId = CommonUtil.getJsonParamInt(jsonObject, "periodId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            studentId = CommonUtil.getJsonParamInt(jsonObject, "studentId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
        }

        try {
            TalkUserService talkUserService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
            TalkLessonService talkLessonService = MelotBeanFactory.getBean("talkLessonService", TalkLessonService.class);
            StudentInfo studentInfo = talkUserService.getStudentInfoByUserId(userId);
            if (studentInfo == null) {
                result.addProperty("TagCode", TagCodeEnum.STUDENT_NOT_EXIST);
            } else {
                if (talkLessonService == null) {
                    result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                    return result;
                }
            }

            TeacherCommentToStudent comment = talkLessonService.getTeacherCommentInfo(teacherId, periodId, studentId);

            result.addProperty("result", JSONObject.toJSONString(comment));
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getTeacherCommentInfo error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }

        return result;
    }

    /**
     * 学生添加课堂反馈(40000409)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject createStudentComment(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId = 0, periodId = 0, teacherId = 0, studentId = 0, videoSharpness = 0, soundArticulation = 0, atmosphere = 0, interaction = 0;
        String requireIds = null;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            periodId = CommonUtil.getJsonParamInt(jsonObject, "periodId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            teacherId = CommonUtil.getJsonParamInt(jsonObject, "teacherId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            studentId = CommonUtil.getJsonParamInt(jsonObject, "studentId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);

            videoSharpness = CommonUtil.getJsonParamInt(jsonObject, "videoSharpness", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            soundArticulation = CommonUtil.getJsonParamInt(jsonObject, "soundArticulation", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            atmosphere = CommonUtil.getJsonParamInt(jsonObject, "atmosphere", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            interaction = CommonUtil.getJsonParamInt(jsonObject, "interaction", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);

            requireIds = CommonUtil.getJsonParamString(jsonObject, "requireIds", null, null, 1, Integer.MAX_VALUE);

        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
        }

        try {
            TalkUserService talkUserService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
            TalkLessonService talkLessonService = MelotBeanFactory.getBean("talkLessonService", TalkLessonService.class);
            TeacherInfo teacher = talkUserService.getTeacherInfoByUserId(userId);
            if (teacher == null) {
                result.addProperty("TagCode", TagCodeEnum.USER_IS_NOT_TEACHER);
            } else {
                if (talkLessonService == null) {
                    result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                    return result;
                }
            }
            ClassroomComment comment = new ClassroomComment();
            comment.setTeacherId(teacherId);
            comment.setPeriodId(periodId);
            comment.setUserId(studentId);
            comment.setAtmosphere(atmosphere);
            comment.setSoundArticulation(soundArticulation);
            comment.setInteraction(interaction);
            comment.setVideoSharpness(videoSharpness);
            comment.setRequireIds(requireIds);

            int code = talkLessonService.createStudentComment(comment);
            if (code > 0) {
                result.addProperty("result", code);
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            } else {
                result.addProperty("result", code);
                result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("createStudentComment error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }

        return result;
    }

    /**
     * 老师查看学生课堂反馈(40000410)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getStudentCommentInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId = 0, teacherId=0, periodId = 0, studentId = 0;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            teacherId = CommonUtil.getJsonParamInt(jsonObject, "teacherId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            periodId = CommonUtil.getJsonParamInt(jsonObject, "periodId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);
            studentId = CommonUtil.getJsonParamInt(jsonObject, "studentId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);

        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
        }

        try {
            TalkUserService talkUserService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
            TalkLessonService talkLessonService = MelotBeanFactory.getBean("talkLessonService", TalkLessonService.class);
            TeacherInfo teacherInfo = talkUserService.getTeacherInfoByUserId(userId);
            if (teacherInfo == null) {
                result.addProperty("TagCode", TagCodeEnum.STUDENT_NOT_EXIST);
            } else {
                if (talkLessonService == null) {
                    result.addProperty("TagCode", TagCodeEnum.GET_MODULE_EXCEPTION);
                    return result;
                }
            }

            ClassroomComment comment = talkLessonService.getStudentCommentInfo(teacherId, periodId, studentId);

            result.addProperty("result", JSONObject.toJSONString(comment));
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getTeacherCommentInfo error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }

        return result;
    }



    /**
     * 获取学生小要求配置信息(40000411)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getRequirementList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId = 0;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_INCORRECT, -1, Integer.MAX_VALUE);

        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_INCORRECT);
        }

        try {
            TalkLessonService talkLessonService = MelotBeanFactory.getBean("talkLessonService", TalkLessonService.class);


            List<Requirement> list = talkLessonService.getRequirementList();

            result.addProperty("result", JSONObject.toJSONString(list));
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getTeacherCommentInfo error, jsonObject:" + jsonObject, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }

        return result;
    }


}
