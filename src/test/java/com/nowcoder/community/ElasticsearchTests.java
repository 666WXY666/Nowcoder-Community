package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticTemplate;

    // 测试插入数据
    @Test
    public void testInsert() {
        // 数据是从Mysql数据库中取出来导入到ES中
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    // 测试插入多条数据（不要一次插入这么多，可能会导致校园网账号被封）
    @Test
    public void testInsertList() {
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100));
    }

    // 测试更新数据
    @Test
    public void testUpdate() {
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        post.setContent("我是新人, 使劲灌水.");
        // 通过save方法更新数据时，会先判断数据是否存在，如果存在则更新，如果不存在则插入
        discussPostRepository.save(post);
    }

    // 测试删除数据
    @Test
    public void testDelete() {
        // 删除数据时，会先判断数据是否存在，如果存在则删除，如果不存在则不做任何操作
        discussPostRepository.deleteById(231);
    }

    // 测试删除所有数据
    @Test
    public void testDeleteAll() {
        // 删除所有数据
        discussPostRepository.deleteAll();
    }

    // 测试使用Repository搜索数据（版本太高，不支持）
    @Test
    public void testSearchByRepository() {
    }

    // 测试使用Template搜索数据
    @Test
    public void testSearchByTemplate() {
        // 查询标准构建，匹配字段"content"和"title"中包含"互联网寒冬"关键字的数据
        // 在这里使用matches，而不是contains，contains必须包含完整的关键字，而matches不需要，如果要匹配更多字段使用or或者and
        Criteria criteria = new Criteria("title").matches("互联网寒冬").or(new Criteria("content").matches("互联网寒冬"));

        // 高亮查询
        List<HighlightField> highlightFieldList = new ArrayList<>();
        HighlightField highlightField = new HighlightField("title", HighlightFieldParameters.builder().withPreTags("<em>").withPostTags("</em>").build());
        highlightFieldList.add(highlightField);
        highlightField = new HighlightField("content", HighlightFieldParameters.builder().withPreTags("<em>").withPostTags("</em>").build());
        highlightFieldList.add(highlightField);
        Highlight highlight = new Highlight(highlightFieldList);
        HighlightQuery highlightQuery = new HighlightQuery(highlight, DiscussPost.class);

        // 构建查询
        CriteriaQueryBuilder builder = new CriteriaQueryBuilder(criteria)
                .withSort(Sort.by(Sort.Direction.DESC, "type"))
                .withSort(Sort.by(Sort.Direction.DESC, "score"))
                .withSort(Sort.by(Sort.Direction.DESC, "createTime"))
                .withHighlightQuery(highlightQuery)
                .withPageable(PageRequest.of(0, 10));
        CriteriaQuery query = new CriteriaQuery(builder);

        // 通过elasticsearchTemplate查询
        SearchHits<DiscussPost> result = elasticTemplate.search(query, DiscussPost.class);

        // 处理结果
        List<SearchHit<DiscussPost>> searchHitList = result.getSearchHits();
        List<DiscussPost> discussPostList = new ArrayList<>();
        for (SearchHit<DiscussPost> hit : searchHitList) {
            DiscussPost post = hit.getContent();
            // 将高亮结果添加到返回的结果类中显示
            var titleHighlight = hit.getHighlightField("title");
            if (titleHighlight.size() != 0) {
                post.setTitle(titleHighlight.get(0));
            }
            var contentHighlight = hit.getHighlightField("content");
            if (contentHighlight.size() != 0) {
                post.setContent(contentHighlight.get(0));
            }
            discussPostList.add(post);
        }

        // 构建Page对象
        Page<DiscussPost> page = new PageImpl<>(discussPostList, PageRequest.of(9, 10), result.getTotalHits());

        // 打印结果
        for (DiscussPost post : page) {
            System.out.println(post);
        }
    }
}
