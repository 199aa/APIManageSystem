package com.api.model;

import lombok.Data;
import java.util.Date;

@Data
public class BlacklistWhitelist {
    private Long id;
    private String ipAddress;
    private String listType;  // blacklist, whitelist
    private String description;
    private Date createTime;
    private Date updateTime;
}
