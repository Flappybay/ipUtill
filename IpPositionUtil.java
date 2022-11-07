package com.hzdongcheng.common.core.utils.ip;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hzdongcheng.common.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Objects;

/**
 * IP归属地查询工具类
 * @author yhb
 * @version 2.0
 * @date 2022/5/24 9:54
 **/
@Slf4j
public class IpPositionUtil {
    private static final String IP138_URL = "https://www.ip138.com/iplookup.asp?action=2&ip=";
    private static final String IP_CHAXUN_URL = "https://ipchaxun.com/";
    private static final String IP_TAOBAO_URL = "https://ip.taobao.com/outGetIpInfo";
    private static final String TAOBAO_FORM_PARAM = "accessKey=alibaba-inc&ip=";
    private static final String IP_LOCATION = "https://www.iplocation.net/ip-lookup";

    /**
     * 爬取ip138网站数据
     */
    public static String ip138Api(String ip) {
        String varIpResult = "var ip_result =";

        if (StringUtils.isBlank(ip)) {
            return "";
        }

        Document doc;
        try {
            doc = Jsoup.connect(IP138_URL+ip).timeout(3000).get();
            String elementScriptText = Objects.requireNonNull(doc.getElementsByTag("head").select("script").last()).data();

            String jsonStr = elementScriptText.substring(elementScriptText.indexOf(varIpResult) + varIpResult.length(),
                    elementScriptText.indexOf("var ip_begin ="));

            JSONObject ipAddressList = (JSONObject) JSONUtil.parseArray(JSONUtil.parseObj(jsonStr).get("ip_c_list")).get(0);

            String country = (String) ipAddressList.get("ct");
            String province = (String) ipAddressList.get("prov");
            String city = (String) ipAddressList.get("city");
            String area = (String) ipAddressList.get("area");
            String operator = (String) ipAddressList.get("yunyin");

            return String.format("%s%s%s%s %s", country, province, city, area, operator);
        }catch (Exception e){
           log.info("IP地址查询失败:{}", ip);
        }
        return "";
    }

    /**
     * 爬取ipChaXun网站数据
     */
    public static String ipChaXunApi(String ip) {
        if (StringUtils.isBlank(ip)) {
            return "";
        }
        Document doc;
        try {
            doc = Jsoup.connect(IP_CHAXUN_URL+ip).timeout(3000).get();
            String cityArea = doc.getElementsByTag("label").eq(1).text().split("：")[1];
            String operateStr = doc.getElementsByTag("label").eq(2).text();

            if (StringUtils.isNotBlank(operateStr)) {
                operateStr = operateStr.split("：")[1];
            }

            return String.format("%s %s", cityArea, operateStr);
        } catch (Exception e){
            log.info("IP地址查询失败:{}", ip);
        }
        return "";
    }

    /**
     * 淘宝IP地址库
     */
    public static String ipTaoBao(String ip) {
        if (StringUtils.isBlank(ip)) {
            return "";
        }
        try {
            String rspStr = HttpUtil.post(IP_TAOBAO_URL, TAOBAO_FORM_PARAM+ip, 3000);
            if (StringUtils.isEmpty(rspStr)) {
                return "";
            }

            JSONObject jsonObject = JSONUtil.parseObj(rspStr);
            if (!StringUtils.equals("0", jsonObject.getStr("code"))) {
                return "";
            }

            JSONObject data = (JSONObject) jsonObject.getObj("data");
            String country = data.getStr("country");
            String province = data.getStr("region");
            String city = data.getStr("city");
            String telecomOperators = data.getStr("isp");

            if (StringUtils.isNotBlank(province) && province.contains("X")){
                province = "";
            }
            if (StringUtils.isNotBlank(city) && city.contains("X")){
                city = "";
            }
            if (StringUtils.isNotBlank(telecomOperators) && telecomOperators.contains("X")){
                telecomOperators = "";
            }
            return String.format("%s%s%s %s", country, province, city, telecomOperators);
        } catch (Exception e){
            log.info("IP地址查询失败:{}", ip);
        }
        return "";
    }

    /**
     * 爬取ipLocationNet网站数据
     */
    public static String ipLocationNet(String ip) {
        if (StringUtils.isBlank(ip)) {
            return "";
        }
        Document doc;
        try {
            doc = Jsoup.connect(IP_LOCATION)
                    .timeout(3000)
                    .data("submit", "IP Lookup")
                    .data("query", ip)
                    .post();
            Elements ipInfoTableElements = doc.select("a[href='/go/ipinfo']").parents().select("h4").eq(0).next("table");

            String ipReturn = ipInfoTableElements.select("tr").get(1).select("td").get(0).text();
            if (!StringUtils.equals(ip, ipReturn)){
                return "";
            }

            String country =  ipInfoTableElements.select("tr").get(1).select("td").get(1).text();
            String province =  ipInfoTableElements.select("tr").get(1).select("td").get(2).text();
            String city =  ipInfoTableElements.select("tr").get(1).select("td").get(3).text();
            String isp =  ipInfoTableElements.select("tr").get(3).select("td").get(0).text();

            return String.format("%s %s %s  %s", country, province, city, isp);
        } catch (Exception e){
            log.info("IP地址查询失败:{}", ip);
        }
        return "";
    }
}
