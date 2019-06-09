package com.kr.community.controller;

import com.kr.community.common.utils.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentController extends BaseController{


    @RequestMapping("/public/comment/getComment")
    public R getComment(long postId ){
        return R.ok().put("data",commentService.getComment(postId));
    }

    @RequestMapping("/comment/add")
    public R addComment(String content,long postId,long userId ){
       commentService.addComment(content,postId,userId);
        return R.ok();
    }

    /**
     * 删除一条评论， 注意（被采纳了的评论不能被删除）
     * @param postId
     * @param id
     * @return
     */
    @RequestMapping("/comment/delete")
    public R deleteComment(long postId,long id ){
        commentService.deleteComment(postId,id);
        return R.ok();
    }

    /**
     * 采纳一条评论，帖子由未结变为已结
     * @param postId
     * @param commentId
     * @return
     */
    @RequestMapping("/comment/adopt")
    public R adoptComment(long postId,long commentId ){
        commentService.adoptComment(postId,commentId);
        return R.ok();
    }

    /**
     * 点赞一条评论
     * @param commentId
     * @param userId
     * @return
     */
    @RequestMapping("/comment/voteupComment")
    public R voteUpComment(long commentId,long  userId){
        return commentService.voteUpComment(commentId,userId);
    }

    /**
     * 首页回帖周榜，由于个人社区回帖周榜数严重不足，这里就直接取回帖总榜吧
     * @return
     */
    @RequestMapping("/public/comment/getReplyPostList")
    public R getReplyPostList(){
        return R.ok().put("data", commentService.getReplyPostList());
    }
}
