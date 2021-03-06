package com.wz.crawler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by zn on 2018/6/28.
 */
@Slf4j
public class ECommerceProductDetailDao {

    private String dbName;

    public ECommerceProductDetailDao(String fullDBName) {
        this.dbName = fullDBName;

    }

    public void loopUpdateTaskIdIsNull() {
        int sortIndexs[] = findMinAndMaxSortIndex();
        int min = sortIndexs[0];
        int max = sortIndexs[1];
        int loop = 1000000;
        log.info("min:{},\t max:{}",min,max);
        String sql = "update ecommerce_product_detail set crawler_task_id = null where sort_index >= ? and sort_index < ? and crawler_task_id is not null and crawler_status = 0";
        Connection connection = MultiDataSource.getInstance().getConnection(dbName);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            while (true) {
                preparedStatement.setInt(1, min);
                preparedStatement.setInt(2, min + loop);
                int rows = preparedStatement.executeUpdate();
                if (min > max) {
                    break;
                }
                log.info("update [{}]ok, rows:{},start:{},to:{},loop:{}",dbName,rows, min,min+loop,loop);
                min += loop;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(connection,preparedStatement,resultSet);
        }
    }


    public int[] findMinAndMaxSortIndex() {
        Connection connection = MultiDataSource.getInstance().getConnection(dbName);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int[] minMax = new int[2];
        String sql = "SELECT MIN(sort_index) AS min_index,MAX(sort_index) AS max_index FROM ecommerce_product_detail";
        try {
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int min = resultSet.getInt("min_index");
                int max = resultSet.getInt("max_index");
                minMax[0] = min;
                minMax[1] = max;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(connection,preparedStatement,resultSet);
        }
        return minMax;
    }

    public int findCountIsCrawlerByStatus(int status) {
        String sql = "select count(1) as data_total from ecommerce_product_detail where crawler_status = " + status;
        Connection connection = MultiDataSource.getInstance().getConnection(dbName);
        log.info("sql: {}",sql);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int dataTotal = resultSet.getInt("data_total");
                return dataTotal;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(connection,preparedStatement,resultSet);
        }
        return 0;
    }
    public int findCountIsProductNameNotNull() {
        String sql = "select count(1) as data_total from ecommerce_product_detail where product_name is not null";
        Connection connection = MultiDataSource.getInstance().getConnection(dbName);
        log.info("sql: {}",sql);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int dataTotal = resultSet.getInt("data_total");
                return dataTotal;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(connection,preparedStatement,resultSet);
        }
        return 0;
    }

    public int deleteCrawlerMachine() {
        String sql = "delete from crawler_machine";
        Connection connection = MultiDataSource.getInstance().getConnection(dbName);
        log.info("sql: {}",sql);
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            int rows= preparedStatement.executeUpdate();
            return rows;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(connection,preparedStatement,null);
        }
        return 0;
    }
    public int deleteCrawlerTask() {
        String sql = "DELETE FROM crawler_task WHERE task_type = 3 ;";
        Connection connection = MultiDataSource.getInstance().getConnection(dbName);
        log.info("sql: {}",sql);
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            int rows= preparedStatement.executeUpdate();
            return rows;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(connection,preparedStatement,null);
        }
        return 0;
    }


    private void close(Connection connection, Statement statement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
