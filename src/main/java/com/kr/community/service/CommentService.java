package com.kr.community.service;

import com.kr.community.common.utils.R;
import com.kr.community.entity.Comment;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lv-success
 * @since 2018-10-14
 */
public interface CommentService extends BaseService<Comment> {

    List<Map<String, Object>> getComment(long postId);

    void addComment(String content, long postId, long userId);

    void deleteComment(long postId, long id);

    void adoptComment(long postId, long id);

    R voteUpComment(long commentId, long userId);

    List<Map<String, Object>> getReplyPostList();

}
