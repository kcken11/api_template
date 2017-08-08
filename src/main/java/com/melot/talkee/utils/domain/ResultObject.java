package com.melot.talkee.utils.domain;

import java.io.Serializable;

/**
 * Title:
 * <p>
 * Description:
 * </p>
 *
 * @author 范玉全<a href="mailto:yuquan.fan@melot.cn">
 * @version V1.0
 * @since 11:14 2017/7/12
 */
public class ResultObject implements Serializable {

    private static final long serialVersionUID = 6222170154100037105L;

    private Integer total;

    private boolean success;

    private String errorMsg;

    private Object data;

    private Object common;
    //错误码提示
    private String errCode;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getCommon() {
        return common;
    }

    public void setCommon(Object common) {
        this.common = common;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }
}
