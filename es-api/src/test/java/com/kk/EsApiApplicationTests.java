package com.kk;

import com.alibaba.fastjson.JSON;
import com.kk.pojo.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;

@SpringBootTest
class EsApiApplicationTests {

    @Resource
    private RestHighLevelClient client;

    //测试索引的创建
    @Test
    void testCreateIndex() throws IOException {
        //1、创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("kk_index");
        //2、执行创建请求
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse);
    }

    //测试获取索引
    @Test
    void testExistIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("kk_index");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    //测试删除索引
    @Test
    void testDeleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("kk_index");
        AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.isAcknowledged());
    }

/*----------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

    //测试添加文档
    @Test
    void testAddDocument() throws IOException {
        //创建对象
        User user = new User("kk", 18);
        //创建索引请求
        IndexRequest request = new IndexRequest("new_index");
        // PUT/kk_index/_doc/1
        request.id("1");
        //超时时间
        request.timeout(TimeValue.timeValueSeconds(1));
        //将我们的数据放入请求，json
        request.source(JSON.toJSONString(user), XContentType.JSON);
        //客户端发送请求，获取响应的结果
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());
        System.out.println(response.status());
    }

    //测试获取文档，判断是否存在
    @Test
    void testIsExists() throws IOException {
        GetRequest getRequest = new GetRequest("new_index","1");
        // 不获取返回的_source的上下文
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");

        boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println(exists);

    }

    //获取文档信息
    @Test
    void testIGetDocument() throws IOException {
        GetRequest getRequest = new GetRequest("new_index","1");
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        // 打印文档的内容
        System.out.println(getResponse.getSourceAsString());
        System.out.println(getResponse);
    }

    //更新文档信息
    @Test
    void testIUpdateDocument() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("new_index","1");
        User user = new User("kzj", 3);
        updateRequest.doc(JSON.toJSONString(user),XContentType.JSON);
        UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(updateResponse.status());
    }

    //删除文档信息
    @Test
    void testDeleteDocument() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("new_index","1");
        DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println(deleteResponse.status());
    }

    //批量插入数据
    @Test
    void testBulkRequest() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");
        ArrayList<User> userList = new ArrayList<>();
        userList.add(new User("aa",18));
        userList.add(new User("bb",18));
        userList.add(new User("cc",18));
        userList.add(new User("dd",18));
        userList.add(new User("ee",18));
        userList.add(new User("ff",18));

        // 批处理请求
        for (int i = 0; i < userList.size(); i++) {
            // 这里批量操作，只需要修改对应的请求即可
            bulkRequest.add(
                    new IndexRequest("new_index")
                    .id(""+(i+1))
                    .source(JSON.toJSONString(userList.get(i)), XContentType.JSON));
        }
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulkResponse.status());
        System.out.println(bulkResponse.hasFailures());
    }

    // 查询
    // SearchRequest搜索请求
    // SearchSourceBuilder 条件构造[高亮，精确匹配，匹配所有]
    @Test
    void testQueryDocument() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        /*
            .boolQuery()        布尔查询
            .matchAllQuery()    匹配所有
            .termQuery()        精确查询
         */
        //搜索条件构建器
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //使用QueryBuilders工具类来实现
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "aa");
        //query()需要传入QueryBuilder构造器
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(TimeValue.timeValueSeconds(60));
        sourceBuilder.from();
        sourceBuilder.size();
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(searchResponse.getHits()));
        System.out.println("---------------------------------------------------------------");
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap());
        }
    }

}
