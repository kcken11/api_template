package com.melot.talkee.service;

import com.melot.talkee.driver.domain.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.talkee.driver.service.TalkUserService;
import com.melot.talkee.redis.UserRedisSource;
import com.melot.talkee.service.domain.SimplifyStudentInfo;

public class UserService {

	private static Gson gson = new Gson();

	private static JsonParser jsonParser = new JsonParser();

	public static JsonObject userInfoToJson(Integer userId,int platform) {
		if (userId != null) {
			TalkUserService userService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
			if (userService != null) {
				UserInfo userInfo = userService.getUserInfoByUserId(userId);
				if (userInfo != null) {
					userInfo.setToken(UserRedisSource.getUserToken(userId,platform));
					
					String portrait = userService.getPortraitByUserId(userId);
					if (StringUtils.isNotBlank(portrait)) {
						userInfo.setPortrait(portrait);
					}
					
					String userInfoStr = gson.toJson(userInfo);
					JsonElement userInfoElement = jsonParser.parse(userInfoStr);
					if (userInfoElement == null) {
						return null;
					}
					JsonObject userInfoResult = userInfoElement.getAsJsonObject();
					// 删除密码相关数据
					if (userInfoResult.get("password") != null) {
					    userInfoResult.remove("password");
                    }
					int supply = 0;
					if (userInfo.getAccountType().intValue() == 1) {
						StudentInfo studentInfo = userService.getStudentInfoByUserId(userId);
						if (studentInfo != null) {
							// 是否补录用户信息
							if (StringUtils.isNotBlank(studentInfo.getCnNickname())) {
								supply = 1;
							}
							int userType = studentInfo.getUserType();
							JsonObject studentObject = jsonParser.parse(gson.toJson(studentInfo, StudentInfo.class)).getAsJsonObject();
							if (studentInfo.getBirthday() != null) {
								if (studentObject.get("birthday") != null) {
									studentObject.remove("birthday");
								}
								studentObject.addProperty("birthday", studentInfo.getBirthday().getTime());
							}
							if (studentInfo.getUpdateTime() != null) {
								if (userInfoResult.get("updateTime") != null) {
									userInfoResult.remove("updateTime");
								}
								userInfoResult.addProperty("updateTime", studentInfo.getUpdateTime().getTime());
							}
							
							Integer cityId = studentInfo.getCityId();
							if (cityId != null) {
								AdminCity adminCity = userService.getAdminCityById(cityId);
								if (adminCity != null && StringUtils.isNotBlank(adminCity.getName())) {
									studentObject.addProperty("cityName", adminCity.getName());
								}
							}
							
							Lesson lesson = LessonService.getCurrentLessonInfo(userId);
							if (lesson != null) {
								if (StringUtils.isNotBlank(lesson.getLessonName())) {
									studentObject.addProperty("lessonName", lesson.getLessonName());
								}
								if (StringUtils.isNotBlank(lesson.getLessonTitle())) {
									studentObject.addProperty("lessonTitle", lesson.getLessonTitle());
								}
							}
							
							int periodType = 1;
							if (userType < 1) {
								periodType = 3;
							}
							
							StudentPeriods studentPeriods = userService.getStudentPeriods(userId,periodType);
							int curPeriods = 0;
							int overPeriods = 0;
							int allPeriods = 0;
							int validPeriods=0;
							int accumulativeTotal=0;
							if (studentPeriods != null) {
								if (studentPeriods.getCurPeriods() != null) {
									curPeriods = studentPeriods.getCurPeriods();
								}
								if (studentPeriods.getAllPeriods() != null) {
									allPeriods = studentPeriods.getAllPeriods();
								}
								if (studentPeriods.getOverPeriods() != null) {
									overPeriods = studentPeriods.getOverPeriods();
								}	
								if (studentPeriods.getVaildPreviwPeriods()!= null) {
									validPeriods = studentPeriods.getVaildPreviwPeriods();
								}
								if(studentPeriods.getAccumulativeTotal()!=null){
									accumulativeTotal = studentPeriods.getAccumulativeTotal();
								}
							}
							studentObject.addProperty("curPeriods", curPeriods);
							studentObject.addProperty("overPeriods", overPeriods);
							studentObject.addProperty("allPeriods", allPeriods);
							studentObject.addProperty("vaildPreviwPeriods", validPeriods);
							studentObject.addProperty("accumulativeTotal",accumulativeTotal);
							userInfoResult.add("studentInfo", studentObject);
						}
					}else if (userInfo.getAccountType().intValue() == 2){
						TeacherInfo teacherInfo = userService.getTeacherInfoByUserId(userId);
						if (teacherInfo != null) {
							// 是否补录用户信息
							if (StringUtils.isNotBlank(teacherInfo.getTeacherName())) {
								supply = 1;
							}
							JsonObject teacherObject = jsonParser.parse(gson.toJson(teacherInfo, TeacherInfo.class)).getAsJsonObject();
							if (teacherInfo.getBirthday() != null) {
								if (teacherObject.get("birthday") != null) {
									teacherObject.remove("birthday");
								}
								teacherObject.addProperty("birthday", teacherInfo.getBirthday().getTime());
							}
							if (teacherInfo.getAddTime() != null) {
								if (userInfoResult.get("addTime") != null) {
									userInfoResult.remove("addTime");
								}
								teacherObject.addProperty("addTime", teacherInfo.getAddTime().getTime());
							}
							
							Integer cityId = teacherInfo.getCityId();
							if (cityId != null) {
								AdminCity adminCity = userService.getAdminCityById(cityId);
								if (adminCity != null && StringUtils.isNotBlank(adminCity.getEnName())) {
									teacherObject.addProperty("cityName", adminCity.getEnName());
								}
							}
							
						   TeacherOverPeriodCount overPeriodCount=LessonService.getTeacherOverPeriodCount(userId);
                           if(overPeriodCount!=null){
                                teacherObject.addProperty("needPeriods",overPeriodCount.getNeedPeriods());
                                teacherObject.addProperty("realPeriods",overPeriodCount.getRealPeriods());
                                teacherObject.addProperty("vaildPeriods",overPeriodCount.getVaildPeriods());
                                teacherObject.addProperty("extraPeriods",overPeriodCount.getExtraPeriods());
						   }
							userInfoResult.add("teacherInfo", teacherObject);
						}
					}
					CheckinInfo checkinInfo = userService.getCheckinInfo(userId);
					if (checkinInfo != null) {
						JsonObject checkin = jsonParser.parse(gson.toJson(checkinInfo, CheckinInfo.class)).getAsJsonObject();
						if (checkin.get("userId") != null) {
							checkin.remove("userId");
						}
						userInfoResult.add("checkin", checkin);
					}
					if (userInfo.getRegisterTime() != null) {
						if (userInfoResult.get("registerTime") != null) {
							userInfoResult.remove("registerTime");
						}
						userInfoResult.addProperty("registerTime", userInfo.getRegisterTime().getTime());
					}
					userInfoResult.addProperty("supply", supply);
					return userInfoResult;
				}
			}
		}
		return null;
	}
	

	/**
	 * 获取精简用户信息
	 * @param userId
	 * @return
	 */
	public static SimplifyStudentInfo getSimplifyStudentInfo(Integer userId){
		SimplifyStudentInfo simplifyStudentInfo = null;
		if (userId == null) {
			return simplifyStudentInfo;
		}
		TalkUserService userService = MelotBeanFactory.getBean("talkUserService", TalkUserService.class);
		if (userService != null) {
			// 设置预约用户信息
			StudentInfo studentInfo = userService.getStudentInfoByUserId(userId);
			if (studentInfo != null) {
				simplifyStudentInfo = new SimplifyStudentInfo();
				BeanUtils.copyProperties(studentInfo, simplifyStudentInfo);
				if (studentInfo.getBirthday() != null) {
					simplifyStudentInfo.setBirthday(studentInfo.getBirthday().getTime());
				}
				
				String portrait = userService.getPortraitByUserId(userId);
				if (StringUtils.isNotBlank(portrait)) {
					simplifyStudentInfo.setPortrait(portrait);
				}
				simplifyStudentInfo.setUserId(userId);
			}
		}
		return simplifyStudentInfo;
	}
	
}
