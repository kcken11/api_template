package com.melot.talkee.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.gson.JsonObject;
import com.melot.talkee.utils.SwapObj;
import com.melot.talkee.utils.TagCodeEnum;

@Controller
public class EntranceController extends AbstractController {

    /**
     * 日志记录对象
     */
    private Logger logger = Logger.getLogger(EntranceController.class);

    @RequestMapping("/entrance")
    public String execute(HttpServletRequest request, HttpServletResponse response) {

        PrintWriter out = null;
        try {
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");// 必须放在PrintWriter前
            response.setHeader("Content-type", "application/json;charset=UTF-8");
            out = response.getWriter();
        } catch (IOException e) {
            // 记录日志
            logger.error("在action中从response获取writer时发生异常.", e);
            // 本次请求结束
            return null;
        }

        JsonObject result = null;

        SwapObj swapObj = new SwapObj();
        String TagCode = generateParam(request, swapObj);
        if (TagCode == null) {
            try {
                result = process(swapObj, request);
                if (result == null) {
                    // 参数parameter值格式不对,FuncTag字段对应的整数值类型后台尚未处理
                    TagCode = TagCodeEnum.FUNCTAG_INCORRECT;
                }
            } catch (Exception e) {
                TagCode = "20001001";
                logger.error("消息处理函数中未捕获的异常.parameter:"
                        + swapObj.getParamJson().toString(), e);
            }
        }

        out.println(generateResult(result, request, TagCode));
        out.flush();
        out.close();
        return null;

    }


}