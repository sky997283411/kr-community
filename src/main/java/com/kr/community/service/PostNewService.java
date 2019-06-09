package com.kr.community.service;



import com.kr.community.common.utils.RedisPage;
import com.kr.community.entity.Post;
import com.kr.community.entity.PostParams;

import java.util.List;
import java.util.Map;

public interface PostNewService extends BaseService<Post>{

    Post addPost(Post post);

    void deletePost(long id);

    void modifyPost(Post post);

    void recommendPost(long id);

    void recommendPostCancel(long id);

    void topLevel(long id);

    void topLevelCancel(long id);

    void view(long postId);

    List<Map<String, Object>> getHotPostList();

    RedisPage postPage(PostParams param);

    List<Map<Object, Object>> getPostTop();

    void updatePostCategory0(String[] paramkey);

    void initPost();

    Map<String, Object> getPost(long postId);
}
