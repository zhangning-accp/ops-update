package com.wz.crawler;

import lombok.Data;

/**
 * Created by zn on 2018/6/28.
 */
@Data
public class DataSource {
    private String id;
    private String url;
    private String userName;
    private String password;
    private String driverClass;
    private String dbName;
    private int count;
    /**
     * 检查是否可用
     */
    private boolean view = false;
    /**
     * 已经导出
     */
    private boolean export = false;
    public String getFullDbName() {
        return id + "." + dbName;
    }
}

