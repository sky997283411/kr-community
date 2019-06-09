package com.kr.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kr.community.common.utils.DateUtils;
import com.kr.community.common.utils.MapUtils;
import com.kr.community.common.utils.R;
import com.kr.community.common.utils.RedisUtil;
import com.kr.community.dao.CommentMapper;
import com.kr.community.dao.PostMapper;
import com.kr.community.dao.UserMapper;
import com.kr.community.entity.Comment;
import com.kr.community.entity.Post;
import com.kr.community.entity.User;
import com.kr.community.service.CommentService;
import com.kr.community.service.PostNewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lv-success
 * @since 2018-10-14
 */
@Service
public class CommentServiceImpl extends BaseServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    PostNewService postNewService;

    @Autowired
    CommentMapper commentMapper;

    @Autowired
    UserMapper userMapper;

    @Override
    public void join(Map<String, Object> map, String field) {

        Map<String, Object> joinColumns = new HashMap<>();

        if(map.get(field) == null) {
            return;
        }
        //字段的值
        String linkfieldValue = map.get(field).toString();
        Comment comment = this.getById(linkfieldValue);

        joinColumns.put("id", comment.getId());
        joinColumns.put("content", comment.getContent());
        joinColumns.put("created", comment.getCreated());

        map.put("comment", joinColumns);
    }

    @Override
    public List<Map<String, Object>> getComment(long postId) {
        List<Map<String, Object>> listResult = new ArrayList<>(10);
        Post post = postMapper.selectById(postId);
        List<Comment> comments = this.list(new QueryWrapper<Comment>().eq("post_id",postId).orderByDesc("status"));
        comments.forEach(comment -> {
            long userId = comment.getUserId();
            Date created = comment.getCreated();
            User user = userMapper.selectById(userId);
            Map<String, Object> commentMap = MapUtils.transformTomap(comment);
            commentMap.put("userId",userId);
            commentMap.put("username",user.getUsername());
            commentMap.put("vipLevel",user.getVipLevel());
            commentMap.put("honor",user.getHonor());
            commentMap.put("image",user.getImage());
            commentMap.put("timeGap",DateUtils.DateFromNow(new Date(created.getTime())));
            commentMap.put("voteUp",redisUtil.sGetSetSize("comment:voteup:"+ comment.getId()));
            listResult.add(commentMap);
        });

        return listResult;
    }

    @Override
    @Transactional
    public void addComment(String content, long postId, long userId) {
        Comment comment = new Comment();
        Date date = new Date();
        comment.setContent(content);
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setVoteDown(0);
        comment.setVoteUp(0);
        comment.setLevel(0);
        comment.setStatus(0);
        comment.setCreated(date);
        comment.setModified(date);
        if(this.save(comment)){
            Post post = postMapper.selectById(postId);
            int commentCount = post.getCommentCount()+1;
            post.setCommentCount(commentCount);
            postMapper.updateById(post);
            redisOperation(postId,1,commentCount);
        }
    }

    @Override
    @Transactional
    public void deleteComment(long postId, long id) {
        if(this.removeById(id)){
            Post post = postMapper.selectById(postId);
            int commentCount = post.getCommentCount() -1;
            post.setCommentCount(commentCount);
            postMapper.updateById(post);
            redisOperation(postId,-1,commentCount);
        }
    }

    @Override
    public void adoptComment(long postId, long id) {
        Comment comment = this.getById(id);
        comment.setStatus(1);
        if(this.updateById(comment)){
            //评论被采纳了，
            Post post = postMapper.selectById(postId);
            post.setStatus(1);
            postMapper.updateById(post);
            long categoryId = post.getCategoryId();
            String categoryKey = "post_category:" + categoryId;
            //修改post基表
            redisUtil.hset("post:"+postId,"status",1);
            //将未结里面的删除，并把数据平移至已结的里面
            double time = redisUtil.zdel(categoryKey + ":undone:time", postId);
            double commentCount = redisUtil.zdel(categoryKey + ":undone:comment", postId);
            redisUtil.zSet(categoryKey + ":done:time", postId , time);
            redisUtil.zSet(categoryKey + ":done:comment", postId , commentCount);
            //更新首页总分类
            postNewService.updatePostCategory0(new String[]{});
        };
    }

    @Override
    public R voteUpComment(long commentId, long userId) {
        long count = redisUtil.sSet("comment:voteup:"+commentId, userId);
        if(count>0){
            return R.ok("点赞成功");
        }

        return R.error("已赞");
    }

    @Override
    public List<Map<String, Object>> getReplyPostList() {
        List<Map<String, Object>> result = new ArrayList<>(20);
        List<Map<String, Object>> userComment = commentMapper.getReplyPostList();
        userComment.forEach(comment ->{
            Map<String, Object> item = new HashMap<>(5);
            Long userId = (Long) comment.get("userId");
            User user = userMapper.selectById(userId);
            item.putAll(comment);
            item.put("username", user.getUsername());
            item.put("imgSrc",user.getImage());
            result.add(item);
        });

        return result;
    }

    /**
     * 当评论增加或删除时，对redis post的操作
     * @param postId
     * @param increby
     */
    private void redisOperation(long postId, double increby, int commentCount){
        Post post = postMapper.selectById(postId);
        long categoryId = post.getCategoryId();
        String categoryKey = "post_category:" + categoryId;
        int status = post.getStatus();
        int recommend = post.getRecommend();
        //修改post基表中的数据
        boolean result = redisUtil.hset("post:"+ postId, "commentCount",commentCount);

        //1.修改全部类型的
        redisUtil.zincreby(categoryKey + ":all:comment", postId , increby);

        //2.判断是否已接
        if(0 == status){
            redisUtil.zincreby(categoryKey + ":undone:comment", postId , increby);
        }else{
            redisUtil.zincreby(categoryKey + ":done:comment", postId , increby);
        }

        //3.判断是否加精
        if(1 == recommend){
            redisUtil.zincreby(categoryKey + ":recommend:comment", postId , increby);
        }
        //更新首页总分类
        postNewService.updatePostCategory0(new String[]{});
    }


}
