package com.melot.talkee.utils;

public class TagCodeEnum {

	/** 处理正确，无错误返回 */
	public static final String SUCCESS = "00000000";
	
	/** s字段解码失败 */
	public static final String SIGN_INCORRECT = "00000001";
	
	/** p字段解析错误 */
	public static final String PLATFORM_INCORRECT = "00000002";
	
	/** v字段解析错误 */
	public static final String VERSION_INCORRECT = "00000003";
	
	/** C字段解析错误 */
	public static final String CHANNEL_INCORRECT = "00000004";
	
	/** userId字段解析错误 */
	public static final String USERID_INCORRECT = "00000005";
	
	/**  phoneNum字段解析错误 */
	public static final String PHONE_NUM_INCORRECT = "00000006";
	
	/** token字段解析错误 */
	public static final String TOKEN_INCORRECT = "00000007";
	
	/** token 过期 */
	public static final String TOKEN_EXPIRE = "00000008";

	/** 参数parameter值格式不对,不能转化成json对象 */
	public static final String PARAMETER_INCORRECT = "00000009";

	/** 参数parameter值格式不对,FuncTag字段对应的整数值类型后台尚未处理 */
	public static final String FUNCTAG_INCORRECT = "00000010";
	
	/** 验证码无效 */
	public static final String VERIFY_CODE_UNAVAIL = "00000011";
	
	/** pwd字段无效 */
	public static final String PASSWORD_MISSING = "00000012";
	
	/** pwd 纯数字 */
	public static final String PASSWORD_IS_NUMBER = "00000013";
	
	/** pwd 包含中文 */
	public static final String PASSWORD_HAS_CHINISE = "00000014";
	
	/** pwd 包含特殊字符 */
	public static final String PASSWORD_HAS_SPECIAL = "00000015";
	
	/** pwd 纯字母 */
	public static final String PASSWORD_IS_CHARACTER = "00000016";
	
	/** token 错误 */
	public static final String TOKEN_IS_ERROR = "00000017";
	
	/** 验证码解析错误 */
	public static final String VERIFY_CODE_INCORRECT = "00000018";
	
	/**  phoneNum无效 */
	public static final String PHONE_NUM_INVALID = "00000101";

	/**  phoneNum在短信发送黑名单内 */
	public static final String SMS_SEND_BLACK = "00000102";
	
	/**  phoneNum超出一分钟发送数 */
	public static final String SMS_SEND_MAX_COUNT_MINUTE = "00000103";
	
	/**  phoneNum超出一天发送数 */
	public static final String SMS_SEND_MAX_COUNT_DAILY = "00000104";
	
	/**  IP黑名单 */
	public static final String IP_BLACK = "00000105";
	
	/**  IP一小时内业务请求上限 */
	public static final String IP_ASK_MAX_COUNT_HOUR = "00000106";
	
	/**  IP一天内业务请求上限 */
	public static final String IP_ASK_MAX_COUNT_DAILY = "00000107";
	
	/**  token 未检查 */
	public static final String TOKEN_NOT_CHECKED = "00000108";
	
	/**  没有访问权限 未检查 */
	public static final String NOT_ACCESS_AUTHORITY  = "00000109";
	
	/** 调用存储过程异常(详情查看日志) */
	public static final String PROCEDURE_EXCEPTION = "00000201";

	/** 调用存储过程未得到正常结果(详情查看日志) */
	public static final String IRREGULAR_RESULT = "00000202";

	/** 执行数据库语句异常 */
	public static final String EXECSQL_EXCEPTION = "00000203";

	/** 结束数据库操作事务异常 */
	public static final String ENDTRANSACTION_EXCEPTION = "00000204";
	
	/** 模块获取异常 */
	public static final String GET_MODULE_EXCEPTION = "00000205";
	
	/** 模块执行异常 */
	public static final String MODULE_EXECUTE_EXCEPTION = "00000206";
	
	/** 参数parameter值格式不对,SMSTYPE解析失败 */
	public static final String SMSTYPE_INCORRECT = "10010101";
	
	/** 手机号码未绑定 */
	public static final String PHONE_NUMBER_UNBIND = "10010102";
	
	/** 手机号码已存在 */
	public static final String PHONE_NUMBER_EXIST = "10010103";
	
	/** 手机号码不存在 */
	public static final String PHONE_NUMBER_NOT_EXIST = "10010104";

	/** 手机发送异常 */
	public static final String PHONE_NUMBER_SEND_EXCEPTION = "10010105";

    /** configKey 解析异常 */
    public static final String CONFIG_KEY_INCORRECT = "10020201";
	
	/** 参数parameter值格式不对,userName解析失败 */
	public static final String USER_NAME_EXIST = "20010201";
	
	/** 参数parameter值格式不对,cnUserName缺失 */
	public static final String CN_USER_NAME_MISSING = "20010202";
	
	/** 参数parameter值格式不对,enUserName缺失 */
	public static final String EN_USER_NAME_MISSING = "20010203";
	
	/** 参数parameter值格式不对,loginName已存在 */
	public static final String LOGIN_NAME_HAS_EXIST = "20010204";

	/** 参数parameter值格式不对,birthday解析失败 */
	public static final String BIRTHDAY_INCORRECT = "20000104";
	
	
	/** 参数parameter值格式不对,loginType解析失败 */
	public static final String LOGIN_TYPE_INCORRECT = "20010301";
	
	/** 参数parameter值格式不对,验证码和密码为空 */
	public static final String VERIFY_CODE_AND_PAW_ISNULL = "20010302";
	
	/** 参数parameter值格式不对,userAccout缺失 */
	public static final String USER_ACCOUT_MISSING = "20010303";
	
	/** 参数parameter值格式不对,userAccout缺失 */
	public static final String EMAIL_MISSING = "20010304";
	
	/** 账号或密码错误 */
	public static final String ACCOUNT_OR_PASSWORD_ERROR = "20010305";

	/** 账号或密码错误 */
	public static final String ACCOUNT_IS_FORBIDDEN = "20010306";

	
	/** 参数parameter值格式不对,oldpwd解析失败 */
	public static final String OLD_PASSWORD_INCORRECT = "20010401";
	
	/** 参数parameter值格式不对,newpwd解析失败 */
	public static final String NEW_PASSWORD_INCORRECT = "20010402";
	
	/** 参数parameter值格式不对,oldpwd错误 */
	public static final String OLD_PASSWORD_ERROR = "20010403";
	
	/** 参原始密码和验证码其中一个不能为空 */
	public static final String VERIFY_CODE_AND_OLD_PAW_ISNULL   = "20010404";
	
	/** 修改或找回密码类型错误 */
	public static final String CHANGE_OR_FIND_PWD_TYPE_INCORRECT   = "20010405";
	
	/** 新密码和原密码相同 */
	public static final String OLD_PASSWORD_NEW_PASSWORD_IDENTICAL   = "20010406";
	
	/**文件上传失败，fileType解析失败 */
	public static final String FILE_TYPE_INCORRECT = "30010101";
	
	/**文件上传失败，文件找不到 */
	public static final String FILE_NOT_FIND = "30010102";
	
	/**文件上传失败，lessonId解析失败 */
	public static final String LESSON_ID_INCORRECT = "30010103";

	/**OSS token获取失败，文件格式不正确 */
	public static final String File_FORMART_INCORRECT = "30010104";

	/**OSS token 获取失败*/
	public static final String OSS_ERR = "30010105";


	/** 参数parameter值格式不对,publishType解析失败 */
	public static final String PUBLISHTYPE_INCORRECT = "40010101";
	
	/** 参数parameter值格式不对,maxNum 解析失败 */
	public static final String MAX_NUM_INCORRECT = "40010102";
	
	/** 参数parameter值格式不对,lesson_time 解析失败 */
	public static final String LESSON_TIME_INCORRECT = "40010103";
	
	/** 参数parameter值格式不对,lesson_date 解析失败 */
	public static final String LESSON_DATE_INCORRECT = "40010201";
	
	/** 参数parameter值格式不对,beginTime 解析失败 */
	public static final String BEGIN_TIME_INCORRECT = "40010401";
	
	/** 参数parameter值格式不对,endTime 解析失败 */
	public static final String END_TIME_INCORRECT = "40010402";
	
	/** 参数parameter值格式不对,periodList 解析失败 */
	public static final String PERIOD_LIST_INCORRECT = "40020101";
	
	/** 参数parameter值格式不对,lessonState 解析失败 */
	public static final String QUERY_STATE_INCORRECT = "40020201";
	
	/** 参数parameter值格式不对,start 解析失败 */
	public static final String START_INCORRECT = "40020202";
	
	/** 参数parameter值格式不对,offset 解析失败 */
	public static final String OFFSET_INCORRECT = "40020203";
	
	
	/** 参数parameter值格式不对,periodId 解析失败 */
	public static final String PERIOD_ID_INCORRECT = "40020301";

	/** 参数parameter值格式不对,cancleReason 解析失败 */
	public static final String CANCLE_REASON_INCORRECT = "40020302";
	

	/** 参数parameter值格式不对,studentId 解析失败 */
	public static final String STUDENT_ID_INCORRECT = "40040101";
	

	/** 参数parameter值格式不对,COMMENT_ 解析失败 */
	public static final String COMMENT_INCORRECT = "40040201";
	

	/** 参数parameter值格式不对,没权限 */
	public static final String NO_PERMISSION = "40040202";

   /**参数parameter值格式不对,subLevel 解析失败**/
	public static final String SUBLEVEL_INCORRECT ="40040203" ;

  /**该用户不是老师**/
	public static final String USER_IS_NOT_TEACHER ="40040204" ;

	/**学生不存在**/
	public static final String STUDENT_NOT_EXIST ="40040205" ;

	/** 参数parameter值格式不对,enterType解析失败 */
	public static final String ENTERTYPE_INCORRECT ="50010102" ;

	/** 参数parameter值格式不对,deviceUid解析失败 */
	public static final String DEVICEUID_INCORRECT ="50010103" ;

	/** 参数parameter值格式不对,用户不存在 */
	public static final String USER_NOT_EXIST ="50010104" ;

	/** 参数parameter值格式不对,课程不存在 */
	public static final String LESSON_NOT_EXIST ="50010105" ;

	/** 参数parameter值格式不对,roleType 解析错误 */
	public static final String ROLETYPE_INCORRECT ="50010106" ;

	   /** 参数parameter值格式不对,segment解析错误 */
    public static final String SEGMENT_INCORRECT ="50010301" ;

    /** 参数parameter值格式不对,message_data 解析错误 */
    public static final String MESSAGE_DATA_INCORRECT ="50010302" ;

}
