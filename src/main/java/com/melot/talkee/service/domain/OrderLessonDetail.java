package com.melot.talkee.service.domain;

public class OrderLessonDetail {
    private Integer periodId;

    private Long lessonDate;

    private Long beginTime;

    private Long endTime;
    
    private Integer publishType;
    
    private Integer teacherId;
    
    private String teacherName;
    
    private Integer lessonState;
    
    private SimplifyStudentInfo studentInfo;
   
    private SimplifyLessonInfo lessonInfo;
    
	/**
	 * @return the lessonState
	 */
	public Integer getLessonState() {
		return lessonState;
	}

	/**
	 * @param lessonState the lessonState to set
	 */
	public void setLessonState(Integer lessonState) {
		this.lessonState = lessonState;
	}

	/**
	 * @return the periodId
	 */
	public Integer getPeriodId() {
		return periodId;
	}

	/**
	 * @param periodId the periodId to set
	 */
	public void setPeriodId(Integer periodId) {
		this.periodId = periodId;
	}

	/**
	 * @return the lessonDate
	 */
	public Long getLessonDate() {
		return lessonDate;
	}

	/**
	 * @param lessonDate the lessonDate to set
	 */
	public void setLessonDate(Long lessonDate) {
		this.lessonDate = lessonDate;
	}

	/**
	 * @return the beginTime
	 */
	public Long getBeginTime() {
		return beginTime;
	}

	/**
	 * @param beginTime the beginTime to set
	 */
	public void setBeginTime(Long beginTime) {
		this.beginTime = beginTime;
	}

	/**
	 * @return the endTime
	 */
	public Long getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	/**
	 * @return the publishType
	 */
	public Integer getPublishType() {
		return publishType;
	}

	/**
	 * @param publishType the publishType to set
	 */
	public void setPublishType(Integer publishType) {
		this.publishType = publishType;
	}

	/**
	 * @return the teacherId
	 */
	public Integer getTeacherId() {
		return teacherId;
	}

	/**
	 * @param teacherId the teacherId to set
	 */
	public void setTeacherId(Integer teacherId) {
		this.teacherId = teacherId;
	}

	/**
	 * @return the teacherName
	 */
	public String getTeacherName() {
		return teacherName;
	}

	/**
	 * @param teacherName the teacherName to set
	 */
	public void setTeacherName(String teacherName) {
		this.teacherName = teacherName;
	}

	/**
	 * @return the studentInfo
	 */
	public SimplifyStudentInfo getStudentInfo() {
		return studentInfo;
	}

	/**
	 * @param studentInfo the studentInfo to set
	 */
	public void setStudentInfo(SimplifyStudentInfo studentInfo) {
		this.studentInfo = studentInfo;
	}

	/**
	 * @return the lessonInfo
	 */
	public SimplifyLessonInfo getLessonInfo() {
		return lessonInfo;
	}

	/**
	 * @param lessonInfo the lessonInfo to set
	 */
	public void setLessonInfo(SimplifyLessonInfo lessonInfo) {
		this.lessonInfo = lessonInfo;
	}
    
}