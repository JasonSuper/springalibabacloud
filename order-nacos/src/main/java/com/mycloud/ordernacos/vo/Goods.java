package com.mycloud.ordernacos.vo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "goods", shards = 3, replicas = 0)
public class Goods {
    @Id
    private Integer id;

    //@Field(type = FieldType.Text, analyzer = "ik_max_word") 没安装中文分词，暂时注释
    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String images;

    @Field(type = FieldType.Integer)
    private Integer price;
}
