package com.example.database.datasource;

import java.util.concurrent.CopyOnWriteArrayList;

/***
 *  DataSourceType
 */
public enum  DataSourceType {

    READ("SLAVE", "从库"),
    WRITE("MASTER", "主库");

    public static final  String SLAVE = "slaveDataSource";
    public static final  String SLAVE_0 = "slaveOneDataSource";
    public static final  String SLAVE_1 = "slaveTwoDataSource";
    public static final  String MASTER = "masterDataSource";

    // 存放多个从库　key  用于做负载均衡
    public static CopyOnWriteArrayList<String> slaveDataSources = new CopyOnWriteArrayList<>();

    static {
        slaveDataSources.add(SLAVE_0);
        slaveDataSources.add(SLAVE_1);
    }

    private String type;
    private String name;

    DataSourceType(String type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * 　获取从库　数据源信息
     * @param number
     */
   public static String getSlave(Integer number){
       return slaveDataSources.get(number);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
