package com.wz.crawler;

import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Created by zn on 2018/6/28.
 */
@Slf4j
public class MultiDataSource {
    // key :
    private Map<String, List<DataSource>> dataSources = new HashMap();
    // key: server_name.db_name.
//    private Map<String,DruidDataSource> druidDataSourceMap = new HashMap();
    private Map<String,HikariDataSource> poolDataSourceMap = new HashMap();
    private static MultiDataSource instance = null;
    //public static String dataSourceXML = ApplicationCache.DEFAULT_DATA_SOURCE_XML_FILE_PAH;
    private long oldLastModify = 0;
    private long newLastModify = 0;

    private MultiDataSource() {
        log.warn("data sourde is change, reloading ..... ");
        readerXMLBuilderDataSources();
        initDruidDataSourceMap();
        log.warn("data sourde reloading finished ..... ");
    }

    public static MultiDataSource getInstance() {
        if (instance == null) {
            instance = new MultiDataSource();
        }
        return instance;
    }

    /**
     * 返回的key是 db server id。 value 是当前key下的 id.dbname的字符串。主要提供给前端页面使用
     * @return
     */
    public Map<String, List<DataSource>> getDataSource() {
        return dataSources;
    }

    public Connection getConnection(String fullDBName) {
        try {
            return poolDataSourceMap.get(fullDBName).getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean checkChange() {
        File file = new File(ApplicationCache.DEFAULT_DATA_SOURCE_XML_FILE_PAH);
        newLastModify = file.lastModified();
        if(newLastModify > oldLastModify) {
            oldLastModify = newLastModify;
            return true;
        }
        return false;
    }

    private void readerXMLBuilderDataSources() {
        File file = new File(ApplicationCache.DEFAULT_DATA_SOURCE_XML_FILE_PAH);
        System.out.println(file.getAbsolutePath());
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(ApplicationCache.DEFAULT_DATA_SOURCE_XML_FILE_PAH);
            Element root = document.getRootElement();
            List<Element> baseSourceElements = root.elements("base-source");
            for (int i = 0; i < baseSourceElements.size(); i++) {
                Element baseSource = baseSourceElements.get(i);
                String id = baseSource.attributeValue("id");
                String url = baseSource.attributeValue("url") + "?useUnicode=true&autoReconnect=true&useSSL=false";
                String userName = baseSource.attributeValue("user-name");
                String password = baseSource.attributeValue("password");
                String driverClass = baseSource.attributeValue("driver-class");
                List<Element> dataSourceElements = baseSource.elements("data-source");
                List<DataSource> dataSourceList = new ArrayList<DataSource>();
                for (int j = 0; j < dataSourceElements.size(); j++) {
                    DataSource dataSource = new DataSource();
                    Element dataSourceElement = dataSourceElements.get(j);
                    String dbName = dataSourceElement.attributeValue("db-name");
                    String count = dataSourceElement.attributeValue("count");
                    if(StringUtils.isNotBlank(count)) {
                        try {
                            dataSource.setCount(Integer.parseInt(count));
                        } catch (NumberFormatException e) {
                            dataSource.setCount(0);
                        }
                    }
                    dataSource.setId(id);
                    dataSource.setUrl(url.replaceAll("\\{db-name\\}", dbName));
                    dataSource.setUserName(userName);
                    dataSource.setDriverClass(driverClass);
                    dataSource.setDbName(dbName);
                    dataSource.setPassword(password);
                    dataSourceList.add(dataSource);
                }
                dataSources.put(id, dataSourceList);
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
    private void initDruidDataSourceMap() {
        Iterator<String> iterator = dataSources.keySet().iterator();
        while(iterator.hasNext()) {
            String key = iterator.next();
            List<DataSource> dataSourceList = dataSources.get(key);
            for(DataSource dataSource : dataSourceList) {
                String url = dataSource.getUrl();
                String userName = dataSource.getUserName();
                String password = dataSource.getPassword();
                String driverClass = dataSource.getDriverClass();
                String dataBaseName = dataSource.getDbName();
                String fullDBName = key + "." + dataBaseName;
                // 创建连接池对象
                HikariDataSource hikariDataSource = new HikariDataSource();
                hikariDataSource.setJdbcUrl(url);
                hikariDataSource.setUsername(userName);
                hikariDataSource.setPassword(password);
                hikariDataSource.setConnectionTestQuery("select count(1) from crawler_machine");
                hikariDataSource.setDriverClassName(driverClass);
                hikariDataSource.setMaximumPoolSize(10);
                hikariDataSource.setMinimumIdle(1);
                poolDataSourceMap.put(fullDBName, hikariDataSource);
            }
        }
        log.info("Init data source map finished.... ");
    }
}
