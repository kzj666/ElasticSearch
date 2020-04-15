package com.kk.utils;

/*
@author kzj
@date 2020/4/14 - 22:56
*/


import com.kk.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * @author kzj
 * 爬取html网页工具类
 */
@Component
public class HtmlParseUtil {
    public static List<Content> parseJD(String keywords) throws IOException{
        // 获得请求https://search.jd.com/Search?keyword=java
        String url = "https://search.jd.com/Search?keyword="+keywords;
        //解析网页(Jsoup返回Document就是浏览器Dom对象)
        Document document = Jsoup.parse(new URL(url),30000);
        Element element = document.getElementById("J_goodsList");
        //获取所有的li元素
        Elements elements = element.getElementsByTag("li");
        //element1就是每一个li标签
        ArrayList<Content> list = new ArrayList<>();

        for (Element element1 : elements) {
            // 对于多图的网站，所有图片都是延迟加载的（即懒加载）以提高加载速度，所以爬图片要根据真实的标签属性拿
            String img = element1.getElementsByTag("img").eq(0).attr("source-data-lazy-img");
            String price = element1.getElementsByClass("p-price").eq(0).text();
            String title = element1.getElementsByClass("p-name").eq(0).text();

            Content content = new Content(title, img, price);
            list.add(content);

        }
        return list;
    }
}
