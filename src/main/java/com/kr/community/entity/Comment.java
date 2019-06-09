package com.kr.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
public class Comment extends Model<Comment> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 评论的内容
     */
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 回复的评论ID
     */
    private Long parentId;

    /**
     * 评论的内容ID
     */
    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    /**
     * 评论的用户ID
     */
    private Long userId;

    /**
     * “顶”的数量
     */
    private Integer voteUp;

    /**
     * “踩”的数量
     */
    private Integer voteDown;

    /**
     * 置顶等级
     */
    private Integer level;

    /**
     * 评论的状态
     */
    private Integer status;

    /**
     * 评论的时间
     */
    private Date created;

    /**
     * 评论的更新时间
     */
    private Date modified;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
