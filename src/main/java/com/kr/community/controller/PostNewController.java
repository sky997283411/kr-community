package com.kr.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kr.community.common.utils.R;
import com.kr.community.entity.Post;
import com.kr.community.entity.PostParams;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class PostNewController extends BaseController{

    private final String methodPrefix = "get";

//    @Autowired
//    AmqpTemplate amqpTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    /**
     * 将mysql中的帖子信息同步存入redis
     * @return
     */
    @RequestMapping("/public/initPost")
    public R initPost(){
        long start = System.currentTimeMillis();
        postNewService.initPost();
        long end = System.currentTimeMillis();
        return R.ok();
    }

    /**
     * 首页获取分页
     * @param param
     * @return
     */
    @RequestMapping("/public/post/Page")
    public R postPage(PostParams param){
        return R.ok().put("data",postNewService.postPage(param));
    }

    @RequestMapping("/public/post/getPostTop")
    public R getPostTop(){
        return R.ok().put("data",postNewService.getPostTop());
    }

    @RequestMapping("/public/post/getPost")
    public R getPost(long postId){
        return R.ok().put("data", postNewService.getPost(postId));
    }

    /**
     * 发表一篇新帖子
     * @param post
     * @return
     */
    @RequestMapping("/post/add")
    public R addPost(@Validated Post post){
        Post result = postNewService.addPost(post);
        return R.ok().put("data",result);
    }

    /**
     * 修改只支持内容和标题
     * @param post
     * @return
     */
    @RequestMapping("/post/modify")
    public R modifyPost(@Validated Post post){
        postNewService.modifyPost(post);
        return R.ok().put("data", post);
    }

    /**
     * 删除一篇帖子
     * @param id
     * @return
     */
    @RequestMapping("/post/delete")
    public R deletePost(long id){
        postNewService.deletePost(id);
        return R.ok();
    }

    /**
     * 对帖子加精
     * @param id
     * @return
     */
    @RequiresPermissions("sys:topLevel")
    @RequestMapping("/post/recommendPost")
    public R recommendPost(long id){
        postNewService.recommendPost(id);
        return R.ok();
    }

    /**
     * 取消加精
     * @param id
     * @return
     */
    @RequiresPermissions("sys:topLevel")
    @RequestMapping("/post/recommendPostCancel")
    public R recommendPostCancel(long id){
        postNewService.recommendPostCancel(id);
        return R.ok();
    }

    /**
     * 置顶帖子
     * @param id
     * @return
     */
    @ResponseBody
    @RequiresPermissions("sys:topLevel")
    @PostMapping("/post/topLevel")
    public R topLevel(Long id) {
        postNewService.topLevel(id);
        return R.ok();
    }

    /**
     * 取消置顶帖子
     * @param id
     * @return
     */
    @ResponseBody
    @RequiresPermissions("sys:topLevel")
    @PostMapping("/post/topLevelCancel")
    public R topLevelCancel(Long id) {
        postNewService.topLevelCancel(id);
        return R.ok();
    }

    /**
     * 浏览一篇帖子，缓存加1
     * @param postId
     * @return
     */
    @PostMapping("/public/post/view")
    public R view(Long postId) {
        postNewService.view(postId);
        return R.ok();
    }

    /**
     * 浏览一篇帖子，缓存加1
     * @return
     */
    @PostMapping("/public/post/getHotPostList")
    public R getHotPostList() {
        return R.ok().put("data", postNewService.getHotPostList());
    }







}
