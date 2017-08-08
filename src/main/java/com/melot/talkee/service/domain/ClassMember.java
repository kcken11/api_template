package com.melot.talkee.service.domain;

import java.io.Serializable;

/**
 * 课堂成员类
 * Created by mn on 2017/7/5.
 */
public class ClassMember implements Serializable {

    private Integer userId;

    private Integer accountType;

    private String portraitUrl;

    private Integer gender;

    private String cnNickName;

    private String enNickName;


    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getAccountType() {
        return accountType;
    }

    public void setAccountType(Integer accountType) {
        this.accountType = accountType;
    }

    public String getPortraitUrl() {
        return portraitUrl;
    }

    public void setPortraitUrl(String portraitUrl) {
        this.portraitUrl = portraitUrl;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getCnNickName() {
        return cnNickName;
    }

    public void setCnNickName(String cnNickName) {
        this.cnNickName = cnNickName;
    }

    public String getEnNickName() {
        return enNickName;
    }

    public void setEnNickName(String enNickName) {
        this.enNickName = enNickName;
    }


}
