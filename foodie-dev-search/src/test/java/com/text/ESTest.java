package com.text;

import com.imooc.Application;
import com.imooc.es.pojo.Stu;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.search.MultiMatchQuery;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ESTest {

    @Autowired
    private ElasticsearchTemplate esTemplate;

    /**
     * 不推荐使用 ElasticsearchTemplate 对索引进行管理（创建索引，更新映射，删除索引）
     * 就像是数据库或者是数据库中的表，我们平时是不会通过java代码频繁的去创建修改删除数据库的表
     * 我们只会针对数据做CRUD的操作
     * 在es中也是同理，我们尽量使用ElasticsearchTmplate对文档数据做CURD的操作
     * 1，属性（FieldType）类型不灵活
     * 2，主分片与副本分片数无法设置
     */
    @Test
    public void createIndexStu() {
        Stu stu = new Stu();
        stu.setStuId(100L);
        stu.setName("bat man");
        stu.setAge(18);
        stu.setMoney(18.8f);
        stu.setSign("i am spider man");
        stu.setDescription("i wish a spider man");
        IndexQuery indexQuery = new IndexQueryBuilder().withObject(stu).build();
        esTemplate.index(indexQuery); // 可以新建索引，更新值
    }

    @Test
    public void deleteIndexStu() {
        esTemplate.deleteIndex(Stu.class); // 删除索引
    }

    // ===========================  下面是进行文档操作  =================

    @Test
    public void updateStuDoc() {
        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("sign", "i am not spider man");
        sourceMap.put("money", 88.6f);
        sourceMap.put("age", 33);
        IndexRequest indexRequest = new IndexRequest();
        indexRequest.source(sourceMap);
        UpdateQuery updateQuery = new UpdateQueryBuilder().withClass(Stu.class)
                .withId("1002").withIndexRequest(indexRequest).build();

        esTemplate.update(updateQuery);
    }


    @Test
    public void getStuDoc() {
        GetQuery query = new GetQuery();
        query.setId("1002");
        Stu stu = esTemplate.queryForObject(query, Stu.class);

        System.out.println(stu);
    }

    @Test
    public void deleteStuDoc() {
        esTemplate.delete(Stu.class, "1002");
    }

    // ===========================  下面是进行搜索操作  =================

    @Test
    public void searchStuDoc() { // 分页
        Pageable pageable = PageRequest.of(0, 10);
        SearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("description", "save man"))
                .withPageable(pageable).build();
        AggregatedPage<Stu> pagedStu = esTemplate.queryForPage(query, Stu.class);
        pagedStu.getTotalPages();
        System.out.println("检索后的总分页数为：" + pagedStu.getTotalPages());
        List<Stu> stuList = pagedStu.getContent();
        for (Stu s : stuList) {
            System.out.println(s);
        }
    }

    @Test
    public void hightStuDoc() { // 分页
        String preTag = "<span>";
        String postTag = "</span>";
        Pageable pageable = PageRequest.of(0, 10);

        SortBuilder sortBuilder = new FieldSortBuilder("money").order(SortOrder.ASC);
        SortBuilder sortBuilderAge = new FieldSortBuilder("age").order(SortOrder.DESC);
        SearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("description", "save man"))
                .withHighlightFields(new HighlightBuilder.Field("description").preTags(preTag).postTags(postTag))
                .withSort(sortBuilder).withSort(sortBuilderAge).withPageable(pageable).build();
        AggregatedPage<Stu> pagedStu = esTemplate.queryForPage(query, Stu.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                List<Stu> stuListHightlight = new ArrayList<>();
                SearchHits hits = searchResponse.getHits();
                for (SearchHit h : hits) {

                    HighlightField highlightField = h.getHighlightFields().get("description");
                    String description = highlightField.getFragments()[0].toString();

                    Object stuId = (Object)h.getSourceAsMap().get("stuId");
                    String name = (String)h.getSourceAsMap().get("name");
                    Integer age = (Integer)h.getSourceAsMap().get("age");
                    String sign = (String)h.getSourceAsMap().get("sign");
                    Object money = (Object)h.getSourceAsMap().get("money");
                    Stu stuHL = new Stu();
                    stuHL.setDescription(description);
                    stuHL.setStuId(Long.valueOf(stuId.toString()));
                    stuHL.setName(name);
                    stuHL.setAge(age);
                    stuHL.setSign(sign);
                    stuHL.setMoney(Float.valueOf(money.toString()));
                    stuListHightlight.add(stuHL);
                }
                if (stuListHightlight.size() > 0) {
                    return new AggregatedPageImpl<>((List<T>) stuListHightlight);
                }
                return null;
            }
        });
        pagedStu.getTotalPages();
        System.out.println("检索后的总分页数为：" + pagedStu.getTotalPages());
        List<Stu> stuList = pagedStu.getContent();
        for (Stu s : stuList) {
            System.out.println(s);
        }
    }
}
