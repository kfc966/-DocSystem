package com.example.blogapi.dao.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Document {
    /**
     * 文档id
     */
    private String id;

    /**
     * 发布者id
     */
    private Long publisherId;

    /**
     * 发布者名字
     */
    @TableField(exist = false)
    private String publisher;

    /**
     * 文档名称
     */
    private String docTitle;

    /**
     * uri
     */
    private String docUri;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 文件所有权
     */
    Byte ownerType;
}
