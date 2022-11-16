package com.mycloud.ordernacos.controller;

import com.mycloud.ordernacos.vo.Student;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/mongo")
public class MongoController {

    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 创建集合
     */
    @RequestMapping("/createCollection")
    public void createCollection() {
        boolean a = mongoTemplate.collectionExists(Student.class);
        if (!a) {
            mongoTemplate.createCollection(Student.class);
        }
    }

    /**
     * 批量添加
     *
     * @return
     */
    @RequestMapping("/insert")
    public Student insertStudent() {
        List<Student> list = new ArrayList<>();
        Student student = new Student();
        student.setId(1l);
        student.setUsername("jack");
        student.setAge(18);
        student.setSex("男");
        student.setTimer(LocalDateTime.now());

        list.add(student);

        //mongoTemplate.insert(list, Student.class); // _id如果存在，则抛异常
        return mongoTemplate.save(student); // _id如果存在，则更新数据，save不支持list批量操作
    }

    @RequestMapping("/findone")
    public Student findone(String name) {
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("username").is(name), Criteria.where("_id").is(1)).orOperator(Criteria.where("username").is("rose"));
        Student student = mongoTemplate.findOne(new Query().addCriteria(criteria), Student.class);
        return student;
    }

    @RequestMapping("/find")
    public List<Student> find(String name) {
        List<Student> students = mongoTemplate.find(new Query()
                .addCriteria(Criteria.where("username").is(name))
                .with(Sort.by(Sort.Order.desc("age"))), Student.class);
        return students;
    }

    /**
     * and 和 or 嵌套查询
     */
    @RequestMapping("/fzfind")
    public List<Student> fzfind() {
        Criteria criteria = new Criteria();
        criteria.orOperator(new Criteria().andOperator(Criteria.where("username").is("rose"), Criteria.where("_id").is(2))
                , new Criteria().andOperator(Criteria.where("sex").is("男"), Criteria.where("age").gte(18)));
        List<Student> students = mongoTemplate.find(new Query(criteria)
                .with(Sort.by(Sort.Order.desc("age"))), Student.class);
        return students;
    }

    /**
     * 分页
     */
    @RequestMapping("/findByPage")
    public List<Student> findByPage(String name) {
        List<Student> students = mongoTemplate.find(new Query()
                .addCriteria(Criteria.where("username").is(name))
                .with(Sort.by(Sort.Order.desc("age")))
                .skip(0)
                .limit(10), Student.class);
        return students;
    }

    /**
     * json语句查询
     */
    @RequestMapping("/basicFind")
    public List<Student> basicFind() {
        List<Student> students = mongoTemplate.find(new BasicQuery("{$or:[{$and:[{\"username\":\"rose\"},{\"_id\":2}]}, {$and:[{\"sex\":\"男\", \"age\": {$gte:18}}]}]}"), Student.class);
        return students;
    }

    /**
     * 聚合操作
     */
    @RequestMapping("/aggregate")
    public AggregationResults<Student> aggregate() {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("age").is(18));
        GroupOperation groupOperation = Aggregation.group("id")
                .sum("age").as("t")
                .avg("age").as("a");
        ProjectionOperation projectionOperation = Aggregation.project("t", "a"); // 这里好像没法实现script写法的改变字段名的效果，只能控制返回哪些字段

        TypedAggregation<Student> aggregation = Aggregation.newAggregation(Student.class, matchOperation, groupOperation, projectionOperation);
        aggregation.withOptions(AggregationOptions.builder().allowDiskUse(true).build()); // 设置可查询大于100m的数据
        return mongoTemplate.aggregate(aggregation, Student.class);
    }

    @RequestMapping("/bucket")
    public List<Object> bucket() {
        BucketOperation bucketOperation = Aggregation.bucket("age")
                .withBoundaries(0, 20, 30)
                .withDefaultBucket("other")
                .andOutput("age").sum().as("count");

        TypedAggregation aggregation = Aggregation.newAggregation(Student.class, bucketOperation);
        return mongoTemplate.aggregate(aggregation, Object.class).getMappedResults();
    }
}
