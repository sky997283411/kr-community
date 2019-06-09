package com.kr.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.kr.community.common.utils.DateUtils;
import com.kr.community.common.utils.MapUtils;
import com.kr.community.common.utils.RedisPage;
import com.kr.community.common.utils.RedisUtil;
import com.kr.community.entity.Category;
import com.kr.community.entity.Post;
import com.kr.community.entity.PostParams;
import com.kr.community.entity.User;
import com.kr.community.dao.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kr.community.dao.PostMapper;
import com.kr.community.dao.UserMapper;
import com.kr.community.service.PostNewService;
import java.util.*;

@Service
public class PostNewServiceImpl extends BaseServiceImpl<PostMapper, Post> implements PostNewService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private UserMapper userMapper;

    private final int topCounts = 3;

    /**
     * 同步帖子相关的信息至redis
     * @param
     */
    @Override
    public void initPost() {
        //同步postId基表
        List<Post> postList = this.list(new QueryWrapper<Post>().orderByAsc("created"));
        postList.forEach(post -> {
            redisUtil.hmset("post:"+ post.getId(), MapUtils.transformTomap(post));
        });

        //同步postcategory分类表
        postList.forEach(post -> {
            long categoryId = post.getCategoryId();
            Date created = post.getCreated();
            int comment_count= post.getCommentCount();
            int status = post.getStatus();
            int recommend = post.getRecommend();
            long postId = post.getId();
            double dateScore = Double.parseDouble(DateUtils.format(created,DateUtils.DATE_TIME_PATTERN_SS));
            String categoryKey =  "post_category:" + categoryId;

            //全部类型的
            redisUtil.zSet(categoryKey + ":all:time", postId , dateScore);
            redisUtil.zSet(categoryKey + ":all:comment", postId , comment_count);
            //判断已结或未结
            if(0 == status && 0!=postId){
                redisUtil.zSet(categoryKey + ":undone:time", postId , dateScore);
                redisUtil.zSet(categoryKey + ":undone:comment", postId , comment_count);
            }else {
                redisUtil.zSet(categoryKey + ":done:time", postId , dateScore);
                redisUtil.zSet(categoryKey + ":done:comment", postId , comment_count);
            }
            //判断是否加精
            if(1 == recommend){
                redisUtil.zSet(categoryKey + ":recommend:time", postId , dateScore);
                redisUtil.zSet(categoryKey + ":recommend:comment", postId , comment_count);
            }
        });
        //更新首页总分类
        updatePostCategory0(new String[]{});
    }

    @Override
    public Map<String, Object> getPost(long postId) {
        Map<String, Object> resultMap = new HashMap<>(30);
        Post post = this.getById(postId);
        long userId = post.getUserId();
        long categoryId = post.getCategoryId();
        Date created = post.getCreated();
        Category category= categoryMapper.selectById(categoryId);
        User user = userMapper.selectById(userId);
        Map<String, Object> postMap = MapUtils.transformTomap(post);
        resultMap.putAll(postMap);
        resultMap.put("userId",userId);
        resultMap.put("username",user.getUsername());
        resultMap.put("vipLevel",user.getVipLevel());
        resultMap.put("honor",user.getHonor());
        resultMap.put("image",user.getImage());
        resultMap.put("timeGap",DateUtils.DateFromNow(new Date(created.getTime())));
        resultMap.put("categoryName",category.getName());
        resultMap.put("timeGap",DateUtils.DateFromNow(new Date(created.getTime())));
        resultMap.put("viewCount",redisUtil.get("post:view:"+postId));
        return resultMap;
    }

    /**
     * 新帖子，在redis中插入四种类型：
     * 1.全部 按时间排的，2.全部 按评论排的 3.未结 按时间排的 4.未结按评论数排的
     * @param post
     * @return
     */
    @Override
    @Transactional
    public Post addPost(Post post) {
        Long categoryId = post.getCategoryId();
        Category category = categoryMapper.selectById(categoryId);
        Date now = new Date();
        post.setCategoryName(category.getName());
        post.setViewCount(0);
        post.setVoteDown(0);
        post.setVoteUp(0);
        post.setCommentCount(0);
        post.setRecommend(0);
        post.setCreated(now);
        post.setModified(now);
        post.setStatus(0);
        post.setLevel(0);

        if(this.save(post)){
            long id = post.getId();
            Map<String, Object> setMap = MapUtils.transformTomap(post);
            //post:id基本hash表添加
            redisUtil.hmset("post:"+id, setMap);
            double dateScore = Double.parseDouble(DateUtils.format(now,DateUtils.DATE_TIME_PATTERN_SS));
            String categoryKey = "post_category:" + categoryId;
            //全部按时间排序
            redisUtil.zSet(categoryKey + ":all:time", id , dateScore);
            //全部按评论数排序
            redisUtil.zSet(categoryKey + ":all:comment", id , 0);
            //未结按时间排序
            redisUtil.zSet(categoryKey + ":undone:time", id , dateScore);
            //未结按评论数排的
            redisUtil.zSet(categoryKey + ":undone:comment", id , 0);

            //更新首页总分类
            updatePostCategory0(new String[]{});
        }
        return post;
    }

    /**
     * 删除一条帖子的时候，首先要删除与帖子相关的内容，其次与帖子相关的评论的处理
     * @param id
     */
    @Override
    public void deletePost(long id) {
        Post postInfo = this.getById(id);
        long categoryId = postInfo.getCategoryId();
        int status = postInfo.getStatus();
        int recommend = postInfo.getRecommend();
        if(this.removeById(id)){
            //todo： 评论相关的处理

            //1.删除帖子基表内容
            redisUtil.del("post:"+id);

            //2.删除全部类型的
            String categoryKey = "post_category:" + categoryId;
            redisUtil.zdel(categoryKey + ":all:time", id);
            redisUtil.zdel(categoryKey + ":all:comment", id);

            //3.判断已接还是未结
            if(0 ==status){
                redisUtil.zdel(categoryKey + ":undone:time", id);
                redisUtil.zdel(categoryKey + ":undone:comment", id);
            }else{
                redisUtil.zdel(categoryKey + ":done:time", id);
                redisUtil.zdel(categoryKey + ":done:comment", id);
            }

            //4.判断是否为加精
            if(1==recommend){
                redisUtil.zdel(categoryKey + ":recommend:time", id);
                redisUtil.zdel(categoryKey + ":recommend:comment", id);
            }

            //更新首页总分类
            updatePostCategory0(new String[]{});
        }
    }

    @Override
    public void modifyPost(Post post) {
        long id = post.getId();
        String title = post.getTitle();
        String content = post.getContent();
        if(this.updateById(post)){
            redisUtil.hset("post:"+id, "title",title);
            redisUtil.hset("post:"+id, "content",content);
        }
    }

    @Override
    public void recommendPost(long id) {
        Post post = this.getById(id);
        post.setRecommend(1);
        long categoryId = post.getCategoryId();
        String categoryKey = "post_category:" + categoryId;
        if(this.updateById(post)){
            redisUtil.hset("post:"+ id, "recommend",1);
            double dateScore = Double.parseDouble(DateUtils.format(post.getCreated(),DateUtils.DATE_TIME_PATTERN_SS));
            redisUtil.zSet(categoryKey + ":recommend:time", id , dateScore);
            double commentCount = redisUtil.getZsetScore(categoryKey + ":all:comment",id);
            redisUtil.zSet(categoryKey + ":recommend:comment", id , commentCount);
            updatePostCategory0(new String[]{":recommend:time",":recommend:comment"});
        }
    }

    @Override
    public void recommendPostCancel(long id) {
        Post post = this.getById(id);
        post.setRecommend(0);
        long categoryId = post.getCategoryId();
        String categoryKey = "post_category:" + categoryId;
        if(this.updateById(post)){
            redisUtil.hset("post:"+ id, "recommend",0);
            redisUtil.zdel(categoryKey + ":recommend:time", id);
            redisUtil.zdel(categoryKey + ":recommend:comment", id);
            updatePostCategory0(new String[]{":recommend:time",":recommend:comment"});
        }
    }

    @Override
    public void topLevel(long id) {
        Post post = this.getById(id);
        post.setLevel(1);
        post.setLevelTime(new Date());
        long categoryId = post.getCategoryId();
        int status = post.getStatus();
        int recommend = post.getRecommend();
        Date now = new Date();
        double doubleDateScore = (Double.parseDouble(DateUtils.format(now,DateUtils.DATE_TIME_PATTERN_SS)))*2;
        if(updateById(post)){
            redisUtil.lRemove("post:level",1, id);
            redisUtil.llSet("post:level",id);
            redisUtil.hset("post:"+id,"level",1);
            redisUtil.hset("post:"+id,"levelTime",new Date());

            //1.对于置顶的帖子，排序时直接跳出规则，给其分数为当前时间*2，这样置顶的这类帖子永远都是排在前面，
            // 后置顶的帖子总是在最前面
            //2.更新全部类型的
            String categoryKey = "post_category:" + categoryId;
            redisUtil.zSet(categoryKey + ":all:time", id,doubleDateScore);
            redisUtil.zSet(categoryKey + ":all:comment", id,doubleDateScore);

            //3.判断已接还是未结
            if(0 == status){
                redisUtil.zSet(categoryKey + ":undone:time", id, doubleDateScore);
                redisUtil.zSet(categoryKey + ":undone:comment", id, doubleDateScore);
            }else{
                redisUtil.zSet(categoryKey + ":done:time", id, doubleDateScore);
                redisUtil.zSet(categoryKey + ":done:comment", id, doubleDateScore);
            }

            //4.判断是否为加精
            if(1==recommend){
                redisUtil.zSet(categoryKey + ":recommend:time", id, doubleDateScore);
                redisUtil.zSet(categoryKey + ":recommend:comment", id, doubleDateScore);
            }

            //更新首页总分类
            updatePostCategory0(new String[]{});
        };
    }

    @Override
    public void topLevelCancel(long id) {
        Post post = this.getById(id);
        post.setLevel(0);
        long categoryId = post.getCategoryId();
        int status = post.getStatus();
        int recommend = post.getRecommend();
        Date created = post.getCreated();
        double dateScore = Double.parseDouble(DateUtils.format(created,DateUtils.DATE_TIME_PATTERN_SS));
        int commentCount = post.getCommentCount();
        if(updateById(post)){
            redisUtil.lRemove("post:level",1, id);
            redisUtil.hset("post:"+id,"level",0);

            //1.取消置顶时，重新设为原值
            //2.更新全部类型的
            String categoryKey = "post_category:" + categoryId;
            redisUtil.zSet(categoryKey + ":all:time", id, dateScore);
            redisUtil.zSet(categoryKey + ":all:comment", id,commentCount);

            //3.判断已接还是未结
            if(0 == status){
                redisUtil.zSet(categoryKey + ":undone:time", id, dateScore);
                redisUtil.zSet(categoryKey + ":undone:comment", id, commentCount);
            }else{
                redisUtil.zSet(categoryKey + ":done:time", id, dateScore);
                redisUtil.zSet(categoryKey + ":done:comment", id, commentCount);
            }

            //4.判断是否为加精
            if(1==recommend){
                redisUtil.zSet(categoryKey + ":recommend:time", id, dateScore);
                redisUtil.zSet(categoryKey + ":recommend:comment", id, commentCount);
            }

            //更新首页总分类
            updatePostCategory0(new String[]{});


        };
    }

    @Override
    public void view(long postId) {
        redisUtil.incr("post:view:"+ postId,1);
    }

    @Override
    public List<Map<String, Object>> getHotPostList() {
        List<Map<String, Object>> result = new ArrayList<>(10);
        Set<Integer> listId = redisUtil.getZrevaRangeItems("post_category:0:all:comment",0,13);
        listId.forEach(postId ->{
            Map<String, Object> hotPost = new HashMap<>(3);
            Map<Object, Object> itemMap = redisUtil.hmget("post:"+ postId);
            int commentCount = (int)itemMap.get("commentCount");
            String title = (String) itemMap.get("title");
            int level = (int)itemMap.get("level");
            if(0 == level){
                hotPost.put("postId",postId);
                hotPost.put("title",title);
                hotPost.put("commentCount",commentCount);
                result.add(hotPost);
            }
        });
        return result;
    }

    @Override
    public RedisPage postPage(PostParams param) {
        int categoryId = param.getCategoryId();
        int type= param.getType();
        int order = param.getOrder();
        int pageIndex = param.getPageIndex();
        int pageSize = param.getPageSize();
        RedisPage page = new RedisPage(pageIndex,pageSize);

        String categoryKey = "post_category:" + categoryId;
        List<Map<Object, Object>> result = new ArrayList<>();
        switch (type){
            case 1:
                if(1 == order ){
                    doTimeResultTask(categoryKey,pageIndex,pageSize,result,"all",page);
                }else{
                    doCommentResultTask(categoryKey,pageIndex,pageSize,result,"all",page);
                }
                break;
            case 2 :
                if(1 == order ){
                    doTimeResultTask(categoryKey,pageIndex,pageSize,result,"undone",page);
                }else{
                    doCommentResultTask(categoryKey,pageIndex,pageSize,result,"undone",page);
                }
                break;
            case 3 :
                if(1 == order ){
                    doTimeResultTask(categoryKey,pageIndex,pageSize,result,"done",page);
                }else{
                    doCommentResultTask(categoryKey,pageIndex,pageSize,result,"done",page);
                }
                break;
            case 4 :
                if(1 == order ){
                    doTimeResultTask(categoryKey,pageIndex,pageSize,result,"recommend",page);
                }else{
                    doCommentResultTask(categoryKey,pageIndex,pageSize,result,"recommend",page);
                }
                break;
        }

        return page;
    }

    @Override
    public List<Map<Object, Object>> getPostTop() {
        List<Map<Object, Object>> result = new ArrayList<>(4);
        List<Object> listId = redisUtil.lGet("post:level",0,topCounts);
        setPostItem(listId,result);
        return result;
    }

    private void setPostItem(Collection listId,List<Map<Object, Object>> result){
        listId.forEach(obj ->{
            Map<Object, Object> itemMap = redisUtil.hmget("post:"+ obj);
            Integer userId =(Integer)itemMap.get("userId");
            long created = (long)itemMap.get("created");
            int categoryId = (int)itemMap.get("categoryId");
            Category category= categoryMapper.selectById(categoryId);
            itemMap.put("categoryName",category.getName());
            User user = userMapper.selectById(userId);
            String honor = user.getHonor();
            int vipLevel = user.getVipLevel();
            String image = user.getImage();
            itemMap.put("username",user.getUsername());
            itemMap.put("imgSrc",image);
            itemMap.put("honor",honor);
            itemMap.put("vipLevel",vipLevel);
            itemMap.put("timeGap",DateUtils.DateFromNow(new Date(created)));
            itemMap.put("content",null);
            result.add(itemMap);
        });
    }



    private void doTimeResultTask(String categoryKey,int pageIndex,int pageSize,
                                                       List<Map<Object, Object>> result, String type,RedisPage page){
        long total = redisUtil.getZtotalNumber(categoryKey + ":"+type + ":time");
        page.setTotal(total);
        long start = pageSize * (pageIndex - 1);
        long end =  pageSize * pageIndex - 1;
        Set<Integer> totals = redisUtil.getZrevaRangeItems(categoryKey + ":"+type + ":time", start, end);
        setPostItem(totals,result);
        page.setRecords(result);
    }

    private void doCommentResultTask(String categoryKey,int pageIndex, int pageSize, List<Map<Object, Object>> result,
                                     String type,RedisPage page){
        long total = redisUtil.getZtotalNumber(categoryKey + ":"+type + ":comment");
        page.setTotal(total);
        long start = pageSize * (pageIndex - 1);
        long end =  pageSize * pageIndex-1;
        Set<Integer> totals = redisUtil.getZrevaRangeItems(categoryKey + ":"+type + ":comment",start,end);
        setPostItem(totals,result);
        page.setRecords(result);
    }

    /**
     * 当帖子相关由更新操作时，刷新Category0类型的数据
     */
    public void updatePostCategory0(String[] paramkey){
        if(paramkey.length<1){
            paramkey = new String[]{":all:time",":all:comment",":undone:time",":undone:comment",
                    ":done:time",":done:comment",":recommend:time",":recommend:comment"};
        }
        List<Category> categories = categoryMapper.selectList(new QueryWrapper<>());
        for(String key : paramkey){
            List<String> otherKeys = new ArrayList<>(8);
            categories.forEach(category -> {
                long categoryId = category.getId();
                String categoryKey = "post_category:" + categoryId + key;
                otherKeys.add(categoryKey);
            });
            String detKey = "post_category:0" + key;
            redisUtil.zUnionAndStore("",otherKeys,detKey);
        }
    }




}
