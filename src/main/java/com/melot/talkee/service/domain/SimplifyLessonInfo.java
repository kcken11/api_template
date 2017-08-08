package com.melot.talkee.service.domain;

public class SimplifyLessonInfo {
    private Integer lessonId;

    private Integer lessonType;
    
    private Integer lessonTypeName;

    private String lessonName;

    private Integer lessonLevel;
    
    private String lessonLevelName;

    private String lessonTitle;

    private Integer subLevel;
    
    private String subLevelName;

    private Integer lessonStatus;
    
    private String lessonUrl;
    
    private String originalLessonUrl;
    
    private Integer  studentAbnormalState;//    number  学生异常状态 1：普通请假 2：紧急请假 3：旷课 4：迟到 5：严重迟到 6:早退 7:异常 8:紧急请假 (免责)
    
    private Integer  teacherAbnormalState;//    number  老师异常状态 1：普通请假 2：紧急请假 3：旷课 4：迟到 5：严重迟到 6:早退 7:异常 8:严重迟到 +1 

    
    
    /**
     * @return the studentAbnormalState
     */
    public Integer getStudentAbnormalState() {
        return studentAbnormalState;
    }


    
    /**
     * @param studentAbnormalState the studentAbnormalState to set
     */
    public void setStudentAbnormalState(Integer studentAbnormalState) {
        this.studentAbnormalState = studentAbnormalState;
    }


    
    /**
     * @return the teacherAbnormalState
     */
    public Integer getTeacherAbnormalState() {
        return teacherAbnormalState;
    }


    
    /**
     * @param teacherAbnormalState the teacherAbnormalState to set
     */
    public void setTeacherAbnormalState(Integer teacherAbnormalState) {
        this.teacherAbnormalState = teacherAbnormalState;
    }


    /**
     * @return the originalLessonUrl
     */
    public String getOriginalLessonUrl() {
        return originalLessonUrl;
    }

    
    /**
     * @param originalLessonUrl the originalLessonUrl to set
     */
    public void setOriginalLessonUrl(String originalLessonUrl) {
        this.originalLessonUrl = originalLessonUrl;
    }
    
    
	/**
	 * @return the lessonTypeName
	 */
	public Integer getLessonTypeName() {
		return lessonTypeName;
	}

	/**
	 * @param lessonTypeName the lessonTypeName to set
	 */
	public void setLessonTypeName(Integer lessonTypeName) {
		this.lessonTypeName = lessonTypeName;
	}

	/**
	 * @return the lessonUrl
	 */
	public String getLessonUrl() {
		return lessonUrl;
	}

	/**
	 * @param lessonUrl the lessonUrl to set
	 */
	public void setLessonUrl(String lessonUrl) {
		this.lessonUrl = lessonUrl;
	}

	/**
	 * @return the lessonLevelName
	 */
	public String getLessonLevelName() {
		return lessonLevelName;
	}

	/**
	 * @param lessonLevelName the lessonLevelName to set
	 */
	public void setLessonLevelName(String lessonLevelName) {
		this.lessonLevelName = lessonLevelName;
	}

	/**
	 * @return the lessonId
	 */
	public Integer getLessonId() {
		return lessonId;
	}

	/**
	 * @param lessonId the lessonId to set
	 */
	public void setLessonId(Integer lessonId) {
		this.lessonId = lessonId;
	}

	/**
	 * @return the lessonType
	 */
	public Integer getLessonType() {
		return lessonType;
	}

	/**
	 * @param lessonType the lessonType to set
	 */
	public void setLessonType(Integer lessonType) {
		this.lessonType = lessonType;
	}

	/**
	 * @return the lessonName
	 */
	public String getLessonName() {
		return lessonName;
	}

	/**
	 * @param lessonName the lessonName to set
	 */
	public void setLessonName(String lessonName) {
		this.lessonName = lessonName;
	}

	/**
	 * @return the lessonLevel
	 */
	public Integer getLessonLevel() {
		return lessonLevel;
	}

	/**
	 * @param lessonLevel the lessonLevel to set
	 */
	public void setLessonLevel(Integer lessonLevel) {
		this.lessonLevel = lessonLevel;
	}

	/**
	 * @return the lessonTitle
	 */
	public String getLessonTitle() {
		return lessonTitle;
	}

	/**
	 * @param lessonTitle the lessonTitle to set
	 */
	public void setLessonTitle(String lessonTitle) {
		this.lessonTitle = lessonTitle;
	}

	/**
	 * @return the subLevelName
	 */
	public String getSubLevelName() {
		return subLevelName;
	}

	/**
	 * @param subLevelName the subLevelName to set
	 */
	public void setSubLevelName(String subLevelName) {
		this.subLevelName = subLevelName;
	}


	/**
	 * @return the subLevel
	 */
	public Integer getSubLevel() {
		return subLevel;
	}

	/**
	 * @param subLevel the subLevel to set
	 */
	public void setSubLevel(Integer subLevel) {
		this.subLevel = subLevel;
	}

	/**
	 * @return the lessonStatus
	 */
	public Integer getLessonStatus() {
		return lessonStatus;
	}

	/**
	 * @param lessonStatus the lessonStatus to set
	 */
	public void setLessonStatus(Integer lessonStatus) {
		this.lessonStatus = lessonStatus;
	}

    
}