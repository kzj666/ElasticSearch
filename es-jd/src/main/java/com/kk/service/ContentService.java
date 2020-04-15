package com.kk.service;

/*
@author kzj
@date 2020/4/14 - 23:31
*/

import com.alibaba.fastjson.JSON;
import com.kk.pojo.Content;
import com.kk.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 业务编写
 */
@Service
public class ContentService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    //1、解析数据，放入索引库中
    public boolean parseContent(String keywords) throws IOException {
        List<Content> contents = new HtmlParseUtil().parseJD(keywords);
        //把查询到的数据放入es中
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2m");
        for (int i = 0; i < contents.size(); i++) {
            bulkRequest.add(
                    new IndexRequest("jd_goods")
                            .source(JSON.toJSONString(contents.get(i)), XContentType.JSON) );
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulkResponse.hasFailures();
    }

    //2、获取这些数据，实现基本搜索功能
    public List<Map<String, Object>> searchPage(String keyword, int pageNo, int pageSize) throws IOException {
        if(pageNo <= 1){
            pageNo = 1;
        }

        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);

        //精准匹配
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(TimeValue.timeValueSeconds(60));

        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        ArrayList<Map<String, Object>> list = new ArrayList<>();
        //解析结果
        for (SearchHit documentFields : search.getHits().getHits()) {
            list .add(documentFields.getSourceAsMap());
        }
        return list;
    }


    //2、获取这些数据，实现高亮搜索功能
    public List<Map<String, Object>> searchPageHighlightBuilder(String keyword, int pageNo, int pageSize) throws IOException {
        if(pageNo <= 1){
            pageNo = 1;
        }

        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);

        //精准匹配
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(TimeValue.timeValueSeconds(60));

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        //设置多个高亮不重复显示
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);


        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        ArrayList<Map<String, Object>> list = new ArrayList<>();
        //解析结果
        for (SearchHit hit : search.getHits().getHits()) {

            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            //原来的结果
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //解析高亮的字段
            if (null != title) {
                Text[] fragments = title.fragments();
                String newTitle = "";
                for (Text text : fragments) {
                    newTitle += text;
                }
                sourceAsMap.put("title", newTitle);
            }
            list .add(sourceAsMap);
        }
        return list;
    }


}
