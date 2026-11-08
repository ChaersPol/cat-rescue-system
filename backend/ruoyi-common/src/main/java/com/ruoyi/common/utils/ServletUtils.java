package com.ruoyi.common.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.text.Convert;

/**
 * 客户端工具类
 * 
 * @author ruoyi
 */
public class ServletUtils
{
    /**
     * 获取String参数
     */
    public static String getParameter(String name)
    {
        return getRequest().getParameter(name);
    }

    /**
     * 获取String参数
     */
    public static String getParameter(String name, String defaultValue)
    {
        return Convert.toStr(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt(String name)
    {
        return Convert.toInt(getRequest().getParameter(name));
    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt(String name, Integer defaultValue)
    {
        return Convert.toInt(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取Boolean参数
     */
    public static Boolean getParameterToBool(String name)
    {
        return Convert.toBool(getRequest().getParameter(name));
    }

    /**
     * 获取Boolean参数
     */
    public static Boolean getParameterToBool(String name, Boolean defaultValue)
    {
        return Convert.toBool(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获得所有请求参数
     *
     * @param request 请求对象{@link ServletRequest}
     * @return Map
     */
    public static Map<String, String[]> getParams(ServletRequest request)
    {
        final Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map);
    }

    /**
     * 获得所有请求参数
     *
     * @param request 请求对象{@link ServletRequest}
     * @return Map
     */
    public static Map<String, String> getParamMap(ServletRequest request)
    {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : getParams(request).entrySet())
        {
            params.put(entry.getKey(), StringUtils.join(entry.getValue(), ","));
        }
        return params;
    }

    /**
     * 获取request
     */
    public static HttpServletRequest getRequest()
    {
        return getRequestAttributes().getRequest();
    }

    /**
     * 获取当前请求的访问根地址（scheme://host[:port]），取不到返回空串
     * <p>
     * 反向代理场景优先取 X-Forwarded-Proto / X-Forwarded-Host，其次 Host 请求头，
     * 最后退回 request 自带的 scheme + serverName/serverPort。
     * 典型用途：生成二维码等「需要手机能访问到的绝对地址」时，避免把 localhost 写进去。
     */
    public static String getRequestBaseUrl()
    {
        HttpServletRequest request;
        try
        {
            ServletRequestAttributes attributes = getRequestAttributes();
            request = attributes == null ? null : attributes.getRequest();
        }
        catch (Exception e)
        {
            return StringUtils.EMPTY;
        }
        if (request == null)
        {
            return StringUtils.EMPTY;
        }

        String scheme = firstHeaderValue(request, "X-Forwarded-Proto");
        if (StringUtils.isBlank(scheme))
        {
            scheme = request.getScheme();
        }
        if (StringUtils.isBlank(scheme))
        {
            scheme = "http";
        }

        String host = firstHeaderValue(request, "X-Forwarded-Host");
        if (StringUtils.isBlank(host))
        {
            host = firstHeaderValue(request, "Host");
        }
        if (StringUtils.isBlank(host))
        {
            host = request.getServerName();
            int port = request.getServerPort();
            boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                    || ("https".equalsIgnoreCase(scheme) && port == 443);
            if (port > 0 && !defaultPort)
            {
                host = host + ":" + port;
            }
        }
        if (StringUtils.isBlank(host))
        {
            return StringUtils.EMPTY;
        }
        return scheme + "://" + host;
    }

    /**
     * 获取浏览器地址栏上的源地址（scheme://host[:port]），取不到返回空串
     * <p>
     * 依次看 Origin、Referer 请求头，只保留 scheme://host[:port]，路径部分丢掉。
     * <p>
     * 为什么要单独有这么一个方法：开发时前端在 81、后端在 8081，vite 代理会改写 Host 头
     * （changeOrigin=true），于是 {@link #getRequestBaseUrl()} 拿到的是后端端口 8081，
     * 而不是用户实际在用的 81。只有浏览器自己发的 Origin / Referer 才带真实端口。
     * 生产环境走 nginx 反代时，它同样等于用户访问的域名，结果一致。
     */
    public static String getClientOrigin()
    {
        HttpServletRequest request;
        try
        {
            ServletRequestAttributes attributes = getRequestAttributes();
            request = attributes == null ? null : attributes.getRequest();
        }
        catch (Exception e)
        {
            return StringUtils.EMPTY;
        }
        if (request == null)
        {
            return StringUtils.EMPTY;
        }
        String origin = firstHeaderValue(request, "Origin");
        if (StringUtils.isBlank(origin) || "null".equalsIgnoreCase(origin))
        {
            // file:// 页面、或跨域请求没带 Origin 时，退回 Referer（同源 XHR 一定会带）
            origin = firstHeaderValue(request, "Referer");
        }
        return toOrigin(origin);
    }

    /**
     * 从完整地址里截出 scheme://host[:port]，非法地址返回空串
     */
    private static String toOrigin(String url)
    {
        if (StringUtils.isBlank(url))
        {
            return StringUtils.EMPTY;
        }
        String value = url.trim();
        int schemeIdx = value.indexOf("://");
        if (schemeIdx <= 0)
        {
            return StringUtils.EMPTY;
        }
        int pathIdx = value.indexOf('/', schemeIdx + 3);
        String origin = pathIdx > 0 ? value.substring(0, pathIdx) : value;
        int queryIdx = origin.indexOf('?');
        if (queryIdx > 0)
        {
            origin = origin.substring(0, queryIdx);
        }
        return origin;
    }

    /**
     * 取逗号分隔的请求头中的第一个值（多级代理会追加多份）
     */
    private static String firstHeaderValue(HttpServletRequest request, String name)
    {
        String value = request.getHeader(name);
        if (StringUtils.isBlank(value))
        {
            return StringUtils.EMPTY;
        }
        return StringUtils.trimToEmpty(value.split(",")[0]);
    }

    /**
     * 获取response
     */
    public static HttpServletResponse getResponse()
    {
        return getRequestAttributes().getResponse();
    }

    /**
     * 获取session
     */
    public static HttpSession getSession()
    {
        return getRequest().getSession();
    }

    public static ServletRequestAttributes getRequestAttributes()
    {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (ServletRequestAttributes) attributes;
    }

    /**
     * 将字符串渲染到客户端
     * 
     * @param response 渲染对象
     * @param string 待渲染的字符串
     */
    public static void renderString(HttpServletResponse response, String string)
    {
        try
        {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(string);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 是否是Ajax异步请求
     * 
     * @param request
     */
    public static boolean isAjaxRequest(HttpServletRequest request)
    {
        String accept = request.getHeader("accept");
        if (accept != null && accept.contains("application/json"))
        {
            return true;
        }

        String xRequestedWith = request.getHeader("X-Requested-With");
        if (xRequestedWith != null && xRequestedWith.contains("XMLHttpRequest"))
        {
            return true;
        }

        String uri = request.getRequestURI();
        if (StringUtils.inStringIgnoreCase(uri, ".json", ".xml"))
        {
            return true;
        }

        String ajax = request.getParameter("__ajax");
        return StringUtils.inStringIgnoreCase(ajax, "json", "xml");
    }

    /**
     * 内容编码
     * 
     * @param str 内容
     * @return 编码后的内容
     */
    public static String urlEncode(String str)
    {
        try
        {
            return URLEncoder.encode(str, Constants.UTF8);
        }
        catch (UnsupportedEncodingException e)
        {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 内容解码
     * 
     * @param str 内容
     * @return 解码后的内容
     */
    public static String urlDecode(String str)
    {
        try
        {
            return URLDecoder.decode(str, Constants.UTF8);
        }
        catch (UnsupportedEncodingException e)
        {
            return StringUtils.EMPTY;
        }
    }
}
