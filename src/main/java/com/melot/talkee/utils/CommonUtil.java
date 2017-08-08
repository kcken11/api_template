package com.melot.talkee.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import bsh.EvalError;
import bsh.Interpreter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.melot.talkee.utils.domain.Version;

/**
 * 类说明：公共工具类
 */
public class CommonUtil {

    private static Logger logger = Logger.getLogger(CommonUtil.class);

    /**
     * 最小sdk默认为5
     */
    public static final int APK_SDK_VERSION_MIN = 5;

    /**
     * 最大sdk默认为15
     */
    public static final int APK_SDK_VERSION_MAX = 15;

    /**
     * 获取 Android 客户端的版本信息
     * 
     * @param filePath
     *            客户端APK 文件路径
     * @return Version
     */
    public static Version getAndroidManifestInfo(String filePath) {
        Version apk = null;
        // 解压文件
        if (filePath != null && filePath.length() > 0) {
            try {
            	StringBuilder randPath = new StringBuilder();
                randPath.append(Calendar.getInstance().getTimeInMillis());
                randPath.append(new Random().nextInt(1000));
                String destPath = doUnZip(filePath, randPath.toString());
                if (destPath != null) {
                    // 获取xml文件信息
                    File xmlFile = new File(destPath + "/AndroidManifest.xml");
                    if (xmlFile.exists()) {
                        String xml = AXMLPrinter
                                .decode(destPath + "/AndroidManifest.xml")
                                .replaceAll(
                                        "<\\s*application.+?</\\s*application\\s*>",
                                        "");
                        ByteArrayInputStream is = new ByteArrayInputStream(
                                xml.getBytes());
                        SAXParserFactory saxfac = SAXParserFactory
                                .newInstance();
                        SAXParser saxparser = saxfac.newSAXParser();
                        apk = new Version();
                        saxparser.parse(is, new AndroidHandler(apk));
                    }
                    delUnzipFile(destPath);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return apk;
    }

    /**
     * 删除解压的目录
     * 
     * @param path
     *            目录路径
     */
    private static void delUnzipFile(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            try {
                FileUtils.deleteDirectory(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 文件上传处理
     * 
     * @param src
     *            原文件路经
     * @param fileName
     *            文件名称
     * @param dsrpath
     *            上传文件存放的路经
     */
    public static String processUploadedFile(File src, String fileName,
            String dsrpath) {
        String savePath = "/" + DateUtil.getDateName();
        File fDir = new File(dsrpath + savePath);
        if (!fDir.exists()) {
            try {
                FileUtils.forceMkdir(fDir);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        savePath = savePath + "/" + fileName;
        try {
            FileInputStream in = null;
            FileOutputStream out = null;
            try {
                in = new FileInputStream(src);
                out = new FileOutputStream(dsrpath + savePath);
                byte[] buffer = new byte[4096];
                int bytes_read;
                while ((bytes_read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytes_read);
                }
            } finally {
                if (null != in) {
                    in.close();
                }
                if (null != out) {
                    out.close();
                }
            }
        } catch (Exception e) {
            return null;
        }
        return savePath;
    }

    /**
     * 将指定的压缩文件解压到指定的目标目录下. 如果指定的目标目录不存在或其父路径不存在, 将会自动创建.
     * 
     * @param zip
     *            将会解压的压缩文件
     * @param dest
     *            解压操作的目录
     */
    public static void unzip(File zip, File dest) throws IOException {
        unzip(FileUtils.openInputStream(zip), dest);
    }

    /**
     * 将指定的输入流解压到指定的目标目录下.
     * 
     * @param in
     *            将要解压的输入流
     * @param dest
     *            解压操作的目标目录
     */
    public static void unzip(InputStream in, File dest) throws IOException {
        unzip(new ZipInputStream(in), dest);
    }

    /**
     * 将指定的ZIP输入流解压到指定的目标目录下.
     * 
     * @param zin
     *            将要解压的ZIP输入流
     * @param dest
     *            解压操作的目标目录
     */
    public static void unzip(ZipInputStream zin, File dest) throws IOException {
        try {
            doUnzip(zin, dest);
        } finally {
            IOUtils.closeQuietly(zin);
        }
    }

    /**
     * 执行解压操作
     * 
     * @param zin
     *            Zip 输入流
     * @param dest
     *            目的文件
     * @throws IOException
     */
    private static void doUnzip(ZipInputStream zin, File dest)
            throws IOException {
        for (ZipEntry e; (e = zin.getNextEntry()) != null; zin.closeEntry()) {
            File file = new File(dest, e.getName());

            if (e.isDirectory()) {
                FileUtils.forceMkdir(file);
            } else {
                flushZip(zin, FileUtils.openOutputStream(file));
            }
        }
    }

    /**
     * 转储压缩数据
     * 
     * @param zin
     *            被转储的Zip 压缩数据流
     * @param out
     *            目的输出流
     * @throws IOException
     */
    private static void flushZip(ZipInputStream zin, OutputStream out)
            throws IOException {
        try {
            IOUtils.copy(zin, out);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * 解压缩apk文件
     * 
     * @param path
     *            路径
     * @param randPath
     *            随机路径
     * @return 目的路径
     * @throws IOException
     */
    public static String doUnZip(String path, String randPath)
            throws IOException {
        File sourceFile = new File(path);
        StringBuilder sb = new StringBuilder(sourceFile.getParent());
        sb.append("/");

        String destPath = sb.toString() + randPath;
        File destFile = new File(destPath);
        // 不存在该文件
        if (!sourceFile.exists()) {
            logger.error(destFile + " is not exist.");
            return null;
        }
        try {
            unzip(sourceFile, destFile);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e);
            return null;
        }
        return destPath;
    }

    /**
     * 校验字符串是否匹配 XSSTag
     * 
     * @param str
     *            被校验的字符串
     * @return true - 匹配，false - 不匹配
     */
    public static boolean matchXSSTag(String str) {

        String regEx_img = "<[\\s]*?img[\\s\\S]*?/>";
        String regEx_link = "<[\\s]*?link[\\s\\S]*?/>";
        String regEx_a = "<[\\s]*?a[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?a[\\s]*?>";
        String regEx_iframe = "<[\\s]*?iframe[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?iframe[\\s]*?>";
        String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";
        String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";

        boolean match = false;
        if (!match)
            match = Pattern.compile(regEx_a, Pattern.CASE_INSENSITIVE)
                    .matcher(str).find();
        if (!match)
            match = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE)
                    .matcher(str).find();
        if (!match)
            match = Pattern.compile(regEx_link, Pattern.CASE_INSENSITIVE)
                    .matcher(str).find();
        if (!match)
            match = Pattern.compile(regEx_iframe, Pattern.CASE_INSENSITIVE)
                    .matcher(str).find();
        if (!match)
            match = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE)
                    .matcher(str).find();
        if (!match)
            match = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE)
                    .matcher(str).find();
        return match;

    }

    /**
     * 获取 Http 请求中的 IP 地址
     * 
     * @param request
     *            Http 请求消息
     * @return IP Address
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (ip.equals("127.0.0.1")) {
                // 根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                ip = inet.getHostAddress();
            }
        }

        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15) { // "***.***.***.***".length()
                                              // = 15
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        return ip;
    }

    /**
     * 获取总页数
     * 
     * @param totalCount
     *            总个数
     * @param countPerPage
     *            每页显示的个数
     * @return 总页数
     */
    public static long getPageTotal(long totalCount, int countPerPage) {
        return ((totalCount % countPerPage) == 0 ? (totalCount / countPerPage)
                : (totalCount / countPerPage + 1));
    }

    /**
     * MD5散列算法
     * 
     * @param source
     *            被加密的字符串数据
     * @return 散列值
     */
    public static String md5(String source) {
        StringBuffer sb = new StringBuffer(32);

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(source.getBytes("utf-8"));

            for (int i = 0; i < bytes.length; ++i) {
                sb.append(Integer.toHexString((bytes[i] & 0xFF) | 0x100)
                        .toUpperCase().substring(1, 3));
            }
        } catch (Exception e) {
            logger.error("Can not encode the string '" + source + "' to MD5!",
                    e);
            return null;
        }
        return sb.toString();
    }

    /**
     * 获取去除手机号前缀的手机号码，国内手机号前缀去除(86)1385002381223和1385002381223是相同手机号
     * 
     * @param phoneNum
     *            手机号码
     * @param prefix
     *            前缀
     * @return 去除前缀后的手机号码
     */
    public static String validatePhoneNum(String phoneNum, String prefix) {
        String retPhoneNum = null;
        if (phoneNum != null) {
            if (phoneNum.startsWith("(" + prefix + ")")) {
                retPhoneNum = phoneNum.substring(prefix.length() + 2);
            } else if (phoneNum.startsWith(prefix)) {
                retPhoneNum = phoneNum.substring(prefix.length());
            } else {
                retPhoneNum = phoneNum;
            }

        }
        return retPhoneNum;
    }

    /**
     * 生成随机密码N位字母+数字组合
     * 
     * @param n
     *            位数
     * @param c
     *            是否大小写，-1 小写， 1 大写， 0 不区分大小写
     * @return 随机密码
     */
    public static String getRandom(int n, int c) {
        String password = null;
        String base = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        password = sb.toString();
        switch (c) {
        case -1:
            // 小写
            return password.toLowerCase();
        case 1:
            // 大写
            return password.toUpperCase();
        default:
            // 不分大小写
            return password;
        }
    }

    /**
     * 生成随机N位字母组合
     * 
     * @param n
     *            位数
     * @param c
     *            是否大小写，-1 小写， 1 大写， 0 不区分大小写
     * @return 随机字母组合
     */
    public static String getRandomLetter(int n, int c) {
        String password = null;
        String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        password = sb.toString();
        switch (c) {
        case -1:
            // 小写
            return password.toLowerCase();
        case 1:
            // 大写
            return password.toUpperCase();
        default:
            // 不分大小写
            return password;
        }
    }

    /**
     * 生成随机N位纯数字
     * 
     * @param n
     *            生成随机数的位数
     * @return 纯数字随机数值
     */
    public static String getRandomDigit(int n) {
        String password = null;
        String base = "0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        password = sb.toString();
        return password;
    }

    /**
     * 获取文件的后缀名
     * 
     * @param oriFileName
     *            原始文件名
     * @return 文件后缀
     */
    public static String getSuffix(String oriFileName) {
        return oriFileName.substring(oriFileName.lastIndexOf("."),
                oriFileName.length());
    }

    /**
     * 根据ID重新对资源文件重新命名
     * 
     * @param oriFileName
     *            原始文件名
     * @param id
     *            ID值
     * @return 新文件名
     */
    public static String getNewFileName(String oriFileName, int id) {
        return id
                + oriFileName.substring(oriFileName.lastIndexOf("."),
                        oriFileName.length());
    }

    /**
     * 前台资源传到本地
     * 
     * @param source
     *            源文件
     * @param targetDir
     *            目标目录
     * @throws IOException
     */
    public static void uploadFileToLocal(File source, File targetDir)
            throws IOException {
        FileUtils.copyFile(source, targetDir);
    }

    /**
     * 脚本运算
     * @param formula 字符串形式的计算表达式
     * @param bindings 参数列表
     * @return 运算结果对象
     */
    public static Object caculate(String formula, Map<String, Object> bindings) {
        Object result = null;
        if (StringUtils.isBlank(formula)) {
            return result;
        }
        
//      StringBuffer buffer = new StringBuffer("[formula="+formula+"][bindings="+new Gson().toJson(bindings)+"]");
        
        ScriptEngineManager manager = new ScriptEngineManager();
        
        // 得到所有的脚本引擎工厂
        List<ScriptEngineFactory> factories = manager.getEngineFactories();
        if (factories != null && factories.size() > 0) {
            if (bindings != null && bindings.size() > 0) {
                manager.setBindings(new SimpleBindings(bindings));
            }
            
            ScriptEngine engine = factories.get(0).getScriptEngine();
            try {
                result = engine.eval(formula, manager.getBindings());
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        } else {
            Interpreter interpreter = new Interpreter();
            if (bindings != null && bindings.size() > 0) {
                try {
                    for (String key : bindings.keySet()) {
                        interpreter.set(key, bindings.get(key));
                    }
                    result = interpreter.eval(formula);
                } catch (EvalError e) {
                    e.printStackTrace();
                }
            }
        }
        
//      buffer.append("[result="+result+"]");
//      System.out.println(buffer.toString());
        
        return result;
    }
    
    /**
     * 获取本机可用的空闲端口
     * @param port 指定的起始端口，当该值小于0，默认端口从 2000 开始获取
     * @return -1：未获取到可用的空闲端口
     */
    public static int getAvailablePort(int port) {
        if (port <= 0) {
            port = 2000;
        }
        
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
//                  System.out.println("占用IP【"+ip.getHostAddress()+"】端口【"+port+"】");
//                  port = getAvailablePort(ip.getHostAddress(), port);
//                  System.out.println("占用hostname【"+ip.getHostName()+"】端口【"+port+"】");
//                  port = getAvailablePort(ip.getHostName(), port);
//                  System.out.println("占用InetAddress【"+ip.getCanonicalHostName()+"】端口【"+port+"】");
                    port = getAvailablePort(ip, port);
                }
            }
        } catch (Exception e) {
            port = -1;
        }
        
        return port;
    }

    /**
     * 获取本机指定IP的可用空闲端口
     * @param ipAddress 本机InetAddress对象实例
     * @param port 起始端口
     * @return port
     */
    public static int getAvailablePort(InetAddress ipAddress, int port) {
        if (port <= 0) {
            return getAvailablePort(ipAddress, 2000);
        }
        for (int i = port; i < 65535; i++) {
            ServerSocket ss = null;
            try {
                ss = new ServerSocket();
                ss.bind(new InetSocketAddress(ipAddress, i));
                return i;
            } catch (IOException e) {
                // continue
            } finally {
                if (ss != null) {
                    try {
                        ss.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return port;
    }

    /**
     * 获取本机指定IP的可用空闲端口
     * @param hostname 主机名
     * @param port 起始端口
     * @return port
     */
    public static int getAvailablePort(String hostname, int port) {
        if (port <= 0) {
            return getAvailablePort(hostname, 2000);
        }
        for (int i = port; i < 65535; i++) {
            ServerSocket ss = null;
            try {
                ss = new ServerSocket();
                ss.bind(new InetSocketAddress(hostname, i));
                return i;
            } catch (IOException e) {
                // continue
            } finally {
                if (ss != null) {
                    try {
                        ss.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return port;
    }
    
    /**
     * 获取 userAgent 关键信息
     * @param request
     * @return
     */
    public static String getUserAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua != null) {
            // 解析userAgent关键信息
            String regex = "\\([^\\)]+\\)";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(ua);
            if (matcher.find()) {
                ua = matcher.group(0);
            }
            
			if (ua.indexOf("Android") > 0) {
				ua = ua.replaceFirst("U; ", "");
				ua = ua.toLowerCase();
				ua = ua.replaceFirst("build", "");
				String locale = containLocale(ua);
				if (locale != null) {
					ua = ua.replaceFirst(locale + "; ", "");
				}
				ua = replaceBlank(ua);
			}
			if (ua.indexOf("iPhone") > 0) {
				ua = ua.replaceFirst("CPU ", "");
				ua = ua.replaceFirst(" like Mac OS X", "");
				ua = ua.toLowerCase();
				String locale = containLocale(ua);
				if (locale != null) {
					ua = ua.replaceFirst("; "+ locale, "");
				}
				ua = ua.replaceAll("_", ".");
				ua = replaceBlank(ua);
			}
        }
        
        return ua;
    }
    
    private static String containLocale(String str) {
		String locales = "zh-tw,zh-cn,fr-ca,fr-fr,de-de,it-it,ja-jp,ko-kr,en-ca,en-gb,en-us,zh_tw,zh_cn,zh,fr_ca,fr_fr,fr,de_de,de,it_it,it,ja_jp,ja,ko_kr,ko,en_ca,en_gb,en_us,en";
		String[] array = locales.split(",");
		for (String locale : array) {
			if (str.toLowerCase().indexOf(locale) > 0) {
				return locale;
			}
		}
		return null;
	}
	
	public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
    
    /**
     * 查找一个对象在指定对象数组中的位置
     * @param <T> 泛型类型
     * @param arrays 对象数组
     * @param value 要查找的对象
     * @return index 数组里面的索引位置
     */
    public static <T> int findIndex(T[] arrays, T value) {
        int index = -1;
        for (int i = 0; i < arrays.length; i++) {
            if (value.equals(arrays[i])) {
                index = i;
            }
        }
        return index;
    }

    /** 
     * 删除文件或目录
     * @param   sPath    被删除文件的文件名 
     * @return 文件删除成功返回true，否则返回false 
     */  
    public static boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
                flag = true;
            } else {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File childrenFile : files) {
                        flag = deleteFile(childrenFile.getAbsolutePath());
                        if (!flag) {
                            break;
                        }
                    }
                    if (flag) {
                        flag = file.delete();
                    }
                }
            }
        }
        return flag;
    }
    
    /**
     * 从类路径或工程目录获取指定文件名的全路径
     * 
     * @param fileName
     *            文件名
     * @return 文件路径
     */
    public static String getFilePath(String fileName) {
        String sFileName = getFilePathFromAppDir(fileName);
        if (sFileName == null) {
            sFileName = getFilePathFromClassPath(fileName);
        }
        return sFileName;
    }

    /**
     * 从工程目录获取指定文件名的全路径
     * 
     * @param fileName
     *            文件名
     * @return 文件路径
     */
    public static String getFilePathFromAppDir(String fileName) {
        try {
            File file = new File(fileName);
            if (file.isFile()) {
                return file.getPath();
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 从类路径获取指定文件名的全路径
     * 
     * @param fileName
     *            文件名
     * @return 文件路径
     */
    public static String getFilePathFromClassPath(String fileName) {
        String sFileName = "";
        String classPath = System.getProperty("java.class.path");

        StringTokenizer stn = new StringTokenizer(classPath,
                System.getProperty("path.separator"));

        String filePath = "";
        File file = null;

        while (stn.hasMoreElements()) {
            filePath = (String) stn.nextElement();
            file = new File(filePath);
            if (file.isDirectory()) {
                String[] fileList = file.list(new CommonUtil().new CfgFileFilter(
                        fileName));
                if (fileList != null && fileList.length > 0) {
                    sFileName = getCanonicalPath(file.getPath())
                            + System.getProperty("file.separator")
                            + fileList[0];
                    break;
                }
            }
        }
        if (sFileName.equals("")) {
            URL url = Thread.currentThread().getContextClassLoader()
                    .getResource(fileName);
            if (url != null) {
                sFileName = url.toString();
            }
        }
        return sFileName;
    }

    /**
     * 返回抽象路径名的规范路径名字符串
     * 
     * @param file
     *            抽象路径名字符串
     * @return 规范路径名字符串
     */
    public static String getCanonicalPath(String file) {
        try {
            return (new File(file)).getCanonicalPath();
        } catch (Exception e) {
            return null;
        }
    }

    private class CfgFileFilter implements FilenameFilter {
        private String fileName;

        public CfgFileFilter(String fileName) {
            super();
            this.fileName = fileName;
        }

        public boolean accept(File dir, String name) {
            return (name.equals(fileName));
        }
    }

    public static class ErrorGetParameterException extends Exception {
        private static final long serialVersionUID = -7644168124080269354L;
        private String errCode = "11110000";// unknown - default error code

        public ErrorGetParameterException(String errCode) {
            if (errCode != null)
                this.errCode = errCode;
        }

        public String getErrCode() {
            return errCode;
        }
    }

    /****************************
     * Get parameter from json object
     *****************************/
    /**
     * 从 JsonObject 中获取整形属性值
     * 
     * @param jsonObject
     *            json对象
     * @param fieldValue
     *            属性字段名
     * @param defValue
     *            默认值
     * @param errTagCode
     *            错误码
     * @param min
     *            最小值
     * @param max
     *            最大值
     * @return 属性值
     * @throws ErrorGetParameterException
     */
    public static int getJsonParamInt(JsonObject jsonObject, String fieldValue,
            int defValue, String errTagCode, int min, int max)
            throws ErrorGetParameterException {
        try {
            JsonElement intJe = jsonObject.get(fieldValue);
            if (intJe != null && !intJe.isJsonNull()) {
                int iValue = intJe.getAsInt();
                if (iValue >= min && iValue <= max)
                    return iValue;
            }
        } catch (Exception e) {
        }
        if (errTagCode != null)
            throw new ErrorGetParameterException(errTagCode);
        else
            return defValue;
    }

    /**
     * 从 JsonObject 中获取长整形属性值
     * 
     * @param jsonObject
     *            json对象
     * @param fieldValue
     *            属性字段名
     * @param defValue
     *            默认值
     * @param errTagCode
     *            错误码
     * @param min
     *            最小值
     * @param max
     *            最大值
     * @return 属性值
     * @throws ErrorGetParameterException
     */
    public static long getJsonParamLong(JsonObject jsonObject,
            String fieldValue, long defValue, String errTagCode, long min,
            long max) throws ErrorGetParameterException {
        try {
            JsonElement intJe = jsonObject.get(fieldValue);
            if (intJe != null && !intJe.isJsonNull() && intJe.getAsLong() > 0) {
                long lValue = intJe.getAsLong();
                if (lValue >= min && lValue <= max)
                    return lValue;
            }
        } catch (Exception e) {
        }
        if (errTagCode != null)
            throw new ErrorGetParameterException(errTagCode);
        else
            return defValue;
    }

    /**
     * 从 JsonObject 中获取字符串类形属性值
     * 
     * @param jsonObject
     *            json对象
     * @param fieldValue
     *            属性字段名
     * @param defValue
     *            默认值
     * @param errTagCode
     *            错误码
     * @param minLen
     *            最小长度
     * @param maxLen
     *            最大长度
     * @return 属性值
     * @throws ErrorGetParameterException
     */
    public static String getJsonParamString(JsonObject jsonObject,
            String fieldValue, String defValue, String errTagCode, int minLen,
            int maxLen) throws ErrorGetParameterException {
        try {
            JsonElement strJe = jsonObject.get(fieldValue);
            if (strJe != null && !strJe.isJsonNull()) {
            	String strValue = null;
            	if (strJe.isJsonObject()) {
            		strValue = new Gson().toJson(strJe);
				}else{
					strValue = strJe.getAsString();
				}
                if ((strValue == null && minLen == 0)
                        || (strValue != null && strValue.trim().length() >= minLen && strValue
                                .trim().length() <= maxLen)) {
                    return strValue;
                }
            }
        } catch (Exception e) {
        }
        if (errTagCode != null)
            throw new ErrorGetParameterException(errTagCode);
        else
            return defValue;
    }

    
    /**
     * 从 JsonObject 中获取“yyyyMMdd”格式的日起值转换成日期的属性值
     * 
     * @param jsonObject
     *            json对象
     * @param fieldValue
     *            属性字段名
     * @param defValue
     *            默认值
     * @param errTagCode
     *            错误码
     * @param canBeEmpty
     *            被判断的值是否可空：true - 可以， false - 不可以
     * @return 属性值
     * @throws ErrorGetParameterException
     */
    public static Date getJsonParamDate(JsonObject jsonObject,
            String fieldValue, Date defValue, String errTagCode, boolean canBeEmpty) throws ErrorGetParameterException {
        try {
            JsonElement strJe = jsonObject.get(fieldValue);
            if (strJe != null && !strJe.isJsonNull()) {
                String strValue = strJe.getAsString();
                
                Date date = DateUtil.parseDateStringToDate(strValue, "yyyyMMdd");
                if (canBeEmpty || date != null) {
                    return date;
                }
            }
        } catch (Exception e) {
        }
        if (errTagCode != null)
            throw new ErrorGetParameterException(errTagCode);
        else
            return defValue;
    }

    /**
     * 从 JsonObject 中获取“yyyyMMddHHmmss”格式的日起值转换成日期的属性值
     * 
     * @param jsonObject
     *            json对象
     * @param fieldValue
     *            属性字段名
     * @param defValue
     *            默认值
     * @param errTagCode
     *            错误码
     * @param canBeEmpty
     *            被判断的值是否可空：true - 可以， false - 不可以
     * @return 属性值
     * @throws ErrorGetParameterException
     */
    public static Date getJsonParamDatetime(JsonObject jsonObject,
            String fieldValue, Date defValue, String errTagCode, boolean canBeEmpty) throws ErrorGetParameterException {
        try {
            JsonElement strJe = jsonObject.get(fieldValue);
            if (strJe != null && !strJe.isJsonNull()) {
                String strValue = strJe.getAsString();
                
                Date date = DateUtil.parseDateStringToDate(strValue,"yyyyMMddHHmmss");
                if (canBeEmpty || date != null) {
                    return date;
                }
            }
        } catch (Exception e) {
        }
        if (errTagCode != null)
            throw new ErrorGetParameterException(errTagCode);
        else
            return defValue;
    }

    /****************************
     * Get parameter from request object
     *****************************/
    /**
     * 从 HttpServletRequest 中获取整形属性值
     * 
     * @param request
     *            HttpServletRequest 对象
     * @param fieldValue
     *            属性字段名
     * @param defValue
     *            默认值
     * @param errTagCode
     *            错误码
     * @param min
     *            最小值
     * @param max
     *            最大值
     * @return 属性值
     * @throws ErrorGetParameterException
     */
    public static int getRequestParamInt(HttpServletRequest request,
            String fieldValue, int defValue, String errTagCode, int min, int max)
            throws ErrorGetParameterException {
        try {
            String paramValue = request.getParameter(fieldValue);
            if (paramValue != null) {
                int iValue = Integer.parseInt(paramValue);
                if (iValue >= min && iValue <= max)
                    return iValue;
            }
        } catch (Exception e) {
        }
        if (errTagCode != null)
            throw new ErrorGetParameterException(errTagCode);
        else
            return defValue;
    }

    /**
     * 从 HttpServletRequest 中获取长整形属性值
     * 
     * @param request
     *            HttpServletRequest 对象
     * @param fieldValue
     *            属性字段名
     * @param defValue
     *            默认值
     * @param errTagCode
     *            错误码
     * @param min
     *            最小值
     * @param max
     *            最大值
     * @return 属性值
     * @throws ErrorGetParameterException
     */
    public static long getRequestParamLong(HttpServletRequest request,
            String fieldValue, long defValue, String errTagCode, long min,
            long max) throws ErrorGetParameterException {
        try {
            String paramValue = request.getParameter(fieldValue);
            if (paramValue != null) {
                long iValue = Long.parseLong(paramValue);
                if (iValue >= min && iValue <= max)
                    return iValue;
            }
        } catch (Exception e) {
        }
        if (errTagCode != null)
            throw new ErrorGetParameterException(errTagCode);
        else
            return defValue;
    }

    /**
     * 从 HttpServletRequest 中获取字符串类型属性值
     * 
     * @param request
     *            HttpServletRequest 对象
     * @param fieldValue
     *            属性字段名
     * @param defValue
     *            默认值
     * @param errTagCode
     *            错误码
     * @param minLen
     *            最小长度
     * @param maxLen
     *            最大长度
     * @return 属性值
     * @throws ErrorGetParameterException
     */
    public static String getRequestParamString(HttpServletRequest request,
            String fieldValue, String defValue, String errTagCode, int minLen,
            int maxLen) throws ErrorGetParameterException {
        try {
            String paramValue = request.getParameter(fieldValue);
            if ((paramValue == null && 0 == minLen)
                    || (paramValue != null && paramValue.length() >= minLen && paramValue
                            .length() <= maxLen)) {
                return paramValue;
            }
        } catch (Exception e) {
        }

        if (errTagCode != null)
            throw new ErrorGetParameterException(errTagCode);
        else
            return defValue;
    }

    /**
     * 校验用户名，校验规则：
     * 1、长度：3~16位
     * 2、不允许纯数字
     * 3、检查敏感词、短网址
     * 
     * @param inname
     *            被校验的用户名
     * @return 校验结果：true - 通过，false - 不通过
     */
    public static boolean checkUserName(String inname) {
        // 去除字符两端中、英文空格
        inname = inname.trim();
        // 长度：3~16位
        if (inname.length() < 3 || inname.length() > 16)
            return false;
        int digitCount = 0;
        for (int i = 0; i < inname.length(); i++) {
            if (Character.isDigit(inname.charAt(i)))
                digitCount++;
        }
        // 不允许纯数字
        if (inname.length() == digitCount)
            return false;
        // 检查敏感词、短网址
        if (TextFilter.containsSensitiveWords(inname)
                || TextFilter.isShortUrl(inname))
            return false;
        return true;
    }

   

    /**
     * SAX解析器
     * 
     * @author hx1975
     */
    static class AndroidHandler extends DefaultHandler {
        private Version apk;

        public AndroidHandler(Version apk) {
            super();
            this.apk = apk;
        }

        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            if ("manifest".equals(qName)) {
                String s = attributes.getValue("android:versionCode");
                if (s != null) {
                    s = s.replaceAll("[^\\d]", "").trim();
                    if (s.length() == 0) {
                        s = "0";
                    } else {
                        try {
                            Integer.valueOf(s);
                        } catch (Exception e) {
                            s = "0";
                        }
                    }
                }

                apk.setVersionCode(Integer.parseInt(s));
                apk.setVersionName(attributes.getValue("android:versionName"));
            }
        }
    }

    
    /**
     * 获取有效Port
     * @param request
     * @param appId
     * @param platform
     * @param clientPort
     * @return
     */
    public static int getPort(HttpServletRequest request, int platform, int clientPort) {
        
        // 若web端根据参数clientPort返回port
        if (platform == PlatformEnum.WEB && clientPort > 0) {
            return clientPort;
        }
        
        String cdnport = request.getHeader("cdn_src_port");
        logger.debug("cdnport --------------------------------> " + cdnport);
        
        if (StringUtils.isNotBlank(cdnport) && !"null".equals(cdnport.trim())) {
        	try {
				return Integer.valueOf(cdnport);
			} catch (Exception e) {
				return 0;
			}
        }
        String kkclientport = request.getHeader("kkclientport");
        logger.debug("kkclientport --------------------------------> " + kkclientport);
        
        if (StringUtils.isNotBlank(kkclientport) && !"null".equals(kkclientport.trim())) {
        	try {
				return Integer.valueOf(kkclientport);
			} catch (Exception e) {
				return 0;
			}
        }
        return 0;
    }
    
    
    /**
	 * 判断字符是否是标点符号
	 * @param c 被判断的字符
	 * @return true，是标点符号；false，不是标点符号
	 */
	public static boolean isPunctuation(char c) {
		return String.valueOf(c).matches("[\\pP‘’“”]");
	}
    
    /**
	 * 判断字符是否是中文字符
	 * @param c 被判断的字符
	 * @return true，是中文字符；false，不是正文字符
	 */
	public static boolean isChinise(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}
	
	/**
	 * 判断字符串是否包含中文字符
	 * @param str 被判断的字符串
	 * @return true，包含中文字符；false，不包含中文字符
	 */
	public static boolean isHasChinise(String str) {
		if (StringUtils.isNotBlank(str)) {
			char[] charArray = str.toCharArray();
			for (char c : charArray) {
				if (isChinise(c)) {
					if (isPunctuation(c) == false)
						return true;
				}
			}
		}
		return false;
	}
    
    /**
     * 是否包含特殊字符
     * @param str
     * @return
     */
    public static boolean isHasSpecial (String str){
	    if(str.replaceAll("[\u4e00-\u9fa5]*[a-z]*[A-Z]*\\d*-*_*\\s*","").length()==0){ 
	        //不包含特殊字符 
	        return false; 
	    } 
	    return true; 
    }
    
    /**
     * 是否纯字母
     * @param str
     * @return
     */
    public static boolean isCharacter(String str){
    	char[] chars= str.toCharArray();   
 	    boolean tag = true;   
 	    for(char c : chars){  
 	    	if (!Character.isLetter(c)) {
 	    		tag = false;
 	    		break;
			}
 	    }   
 	    return tag;   
    }

}
