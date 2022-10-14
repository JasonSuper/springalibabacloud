package com.mycloud.ordernacos.controller;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import com.mycloud.ordernacos.vo.Goods;
import com.mycloud.ordernacos.vo.MyIndex;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.HighlightQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ElasticSearch Java Api 测试
 */
@RestController
@RequestMapping("/es")
public class EsController {

    @Resource
    private ElasticsearchRestTemplate esTemplate;

    /**
     * 创建索引
     *
     * @throws IOException
     */
    @RequestMapping("/createIndex")
    public void createIndex() throws IOException {
        boolean goods = esTemplate.indexOps(Goods.class).create();//新版本用这个方法
        System.out.println("goods = " + goods);
    }

    /**
     * 如果已经创建了index,可用此方法设置mapping
     */
    @RequestMapping("/createIndexMapping")
    public void testCreateIndexWithoutIndex() {
        IndexCoordinates indexCoordinates = IndexCoordinates.of("goods");//已创建的索引
        // 根据索引实体，获取mapping字段
        Document mapping = esTemplate.indexOps(indexCoordinates).createMapping(Goods.class);
        // 创建索引mapping
        esTemplate.indexOps(indexCoordinates).putMapping(mapping);
    }

    @RequestMapping("/deleteIndex")
    public void deleteIndex() {
        boolean rs = esTemplate.indexOps(Goods.class).delete();
        System.out.println("删除索引：" + rs);
    }

    @RequestMapping("/add")
    public void add() {
        Goods goods = new Goods();
        goods.setId(1);
        goods.setName("已死之人");
        goods.setPrice(99);
        goods.setCategory("古典");
        esTemplate.save(goods);
    }

    @RequestMapping("/search")
    public String search(String id) {
        IndexCoordinates indexCoordinates = IndexCoordinates.of("goods");
        QueryBuilders.boolQuery().should();
        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchQuery("id", id)).build();
        SearchHits<Goods> goods = esTemplate.search(query, Goods.class, indexCoordinates);
        return JSON.toJSONString(goods);
    }

    @RequestMapping("/update")
    public void update() {
        IndexCoordinates indexCoordinates = IndexCoordinates.of("goods");
        /*Goods goods = new Goods();
        goods.setId(1);
        goods.setName("西八之人");*/

        Document document = Document.create();
        document.put("id", 1);
        document.put("name", "西八之人");

        UpdateQuery.Builder builder = UpdateQuery.builder("1").withDocument(document);
        esTemplate.update(builder.build(), indexCoordinates);
    }

    /**
     * 分页排序 和 去重
     */
    @RequestMapping("pageAndSort")
    public Page<MyIndex> pageAndSort(String title) {
        PageRequest pageRequest = PageRequest.of(0, 2);

        NativeSearchQueryBuilder query = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (StringUtils.isNotBlank(title)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", title));
        }

        boolQueryBuilder.must(QueryBuilders.matchQuery("auth", "已死之人"));

        RangeQueryBuilder rangeQueryBuilder = new RangeQueryBuilder("price");
        rangeQueryBuilder.gte("50").lte("100");

        query.withQuery(boolQueryBuilder);
        query.withFilter(rangeQueryBuilder);
        query.withPageable(pageRequest);
        query.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));

        // 去重的字段不能是text类型。所以，price的mapping要有keyword，且通过price去重。
        query.withCollapseField("price");

        // 高亮price属性，有个小坑，只有作为查询条件的字段，才会高亮
        query.withHighlightBuilder(new HighlightBuilder().field("auth"));

        SearchHits<MyIndex> searchHits = esTemplate.search(query.build(), MyIndex.class);

        List<MyIndex> blogs = new ArrayList<>();
        for (SearchHit<MyIndex> searchHit : searchHits) {
            blogs.add(searchHit.getContent()); // 高亮不在content属性里，这里会导致高亮属性丢失。在searchHit.getHighlightFields()里
        }

        return new PageImpl<>(blogs, pageRequest, searchHits.getTotalHits());
    }
}
