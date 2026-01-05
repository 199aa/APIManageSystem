package com.api.model;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class CustomerApp implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name; // 应用名称
    private String description; // 应用描述
    private String icon; // 图标样式
    private String color; // 主题色
    private String appKey; // 应用Key
    private String appSecret; // 应用密钥
    private Integer status; // 状态 1-启用 0-禁用
    private Long userId; // 所属用户ID
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间

    // 统计字段（不在数据库中）
    private Integer apiCount; // 授权接口数量
    private Integer todayCall; // 今日调用次数
}
