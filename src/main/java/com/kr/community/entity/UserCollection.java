package com.kr.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserCollection extends Model<UserCollection> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long postId;

    private Long postUserId;

    private Date created;

    private Date modified;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
