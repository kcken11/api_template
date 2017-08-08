package com.melot.talkee.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * 常量帮助类
 * 
 * @author liyue
 * 
 */
public class ConfigHelper {

	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(ConfigHelper.class);

	private static String accountLoginMax;
	private static Set<String> insideIp;

	public static Set<String> getFuncTags() {
		return funcTags;
	}

	public static void setFuncTags(Set<String> funcTags) {
		ConfigHelper.funcTags = funcTags;
	}

	private static Set<String> funcTags;

    public static void initConfig(String path) {

		FileInputStream fis = null;
		SAXBuilder saxBuiler = new SAXBuilder();
		// 加载配置文件
		try {
			fis = new FileInputStream(new File(path));
		} catch (FileNotFoundException e) {
			logger.error("未找到配置文件config.xml!", e);
		}
		try {
			Document doc = saxBuiler.build(fis);
			Element root = doc.getRootElement();
			accountLoginMax = root.getChildText("accountLoginMax");
			insideIp = new HashSet<String>();
			Element insideIpElement = root.getChild("insideIp");
            List<Element> ipChildren = insideIpElement.getChildren("ip");
			for (Element element : ipChildren) {
			    insideIp.add(element.getTextTrim());
			}
			funcTags = new HashSet<>();
			Element funcTagElement = root.getChild("excludeFuncTag");
			List<Element> funcTagChildren = funcTagElement.getChildren("funcTag");
			for (Element element : funcTagChildren) {
				System.out.println(element.getTextTrim());
				funcTags.add(element.getTextTrim());
			}

		} catch (Exception e) {
			logger.error("读取配置文件config.xml异常!", e);
		}
	}
    
    /**
     * @return the accountLoginMax
     */
    public static String getAccountLoginMax() {
        return accountLoginMax;
    }

    
    /**
     * @param accountLoginMax the accountLoginMax to set
     */
    public static void setAccountLoginMax(String accountLoginMax) {
        ConfigHelper.accountLoginMax = accountLoginMax;
    }

    
    /**
     * @return the insideIp
     */
    public static Set<String> getInsideIp() {
        return insideIp;
    }

    
    /**
     * @param insideIp the insideIp to set
     */
    public static void setInsideIp(Set<String> insideIp) {
        ConfigHelper.insideIp = insideIp;
    }

}
