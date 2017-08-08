package com.melot.talkee.service.domain;

public class SimplifyStudentInfo {
    private Integer userId;

    private String cnNickname;

    private String enNickname;

    private Integer gender;

    private Integer userType;

    private Integer userLevel;

    private Long birthday;
    
    private Integer tag;
    
    private String portrait;

	/**
	 * @return the userId
	 */
	public Integer getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	/**
	 * @return the cnNickname
	 */
	public String getCnNickname() {
		return cnNickname;
	}

	/**
	 * @param cnNickname the cnNickname to set
	 */
	public void setCnNickname(String cnNickname) {
		this.cnNickname = cnNickname;
	}

	/**
	 * @return the enNickname
	 */
	public String getEnNickname() {
		return enNickname;
	}

	/**
	 * @param enNickname the enNickname to set
	 */
	public void setEnNickname(String enNickname) {
		this.enNickname = enNickname;
	}

	/**
	 * @return the gender
	 */
	public Integer getGender() {
		return gender;
	}

	/**
	 * @param gender the gender to set
	 */
	public void setGender(Integer gender) {
		this.gender = gender;
	}

	/**
	 * @return the userType
	 */
	public Integer getUserType() {
		return userType;
	}

	/**
	 * @param userType the userType to set
	 */
	public void setUserType(Integer userType) {
		this.userType = userType;
	}

	/**
	 * @return the userLevel
	 */
	public Integer getUserLevel() {
		return userLevel;
	}

	/**
	 * @param userLevel the userLevel to set
	 */
	public void setUserLevel(Integer userLevel) {
		this.userLevel = userLevel;
	}

	/**
	 * @return the birthday
	 */
	public Long getBirthday() {
		return birthday;
	}

	/**
	 * @param birthday the birthday to set
	 */
	public void setBirthday(Long birthday) {
		this.birthday = birthday;
	}

	/**
	 * @return the tag
	 */
	public Integer getTag() {
		return tag;
	}

	/**
	 * @param tag the tag to set
	 */
	public void setTag(Integer tag) {
		this.tag = tag;
	}

	/**
	 * @return the portrait
	 */
	public String getPortrait() {
		return portrait;
	}

	/**
	 * @param portrait the portrait to set
	 */
	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}
    
}