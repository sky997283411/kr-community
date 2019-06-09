package com.kr.community.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kr.community.entity.Comment;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lv-success
 * @since 2018-10-14
 */
public interface CommentMapper extends BaseMapper<Comment> {

    List<Map<String , Object>> getReplyPostList();

}
