package com.wz.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javafx.scene.chart.PieChart;

public class Main {
    private static int target = 1;
    private static MultiDataSource dataSource = MultiDataSource.getInstance();
    private static Map<Integer, String> fullNameMap = new LinkedHashMap<>();

    /**
     * @param message 消息内容.如果内容有变量，采用{}方式
     * @param values  消息值
     */
    private static void message(String message, boolean isLine, Object... values) {
        message = message.replaceAll("\\{\\}", "%s");
        message = String.format(message, values);
        if (isLine) {
            System.out.println(message);
        } else {
            System.out.print(message);
        }
    }

    private static void mainMenu() {
        Map<String, List<DataSource>> map = dataSource.getDataSource();
        List<String> fullDBNames = new ArrayList<>();
        Iterator<String> iterator = map.keySet().iterator();
        message("----------------------------------------------------------------------------", true);
        message("Please choose database name:", true);
        message("----------------------------------------------------------------------------", true);
        target = 1;
        fullNameMap.clear();
        while (iterator.hasNext()) {
            String key = iterator.next();
            List<DataSource> list = map.get(key);
            for (DataSource dataSource : list) {
                if(dataSource.isView()) {
                    message("{}: {}\t", false, target, dataSource.getFullDbName());
                    fullNameMap.put(target, dataSource.getFullDbName());
                    target++;
                }

            }
            message("", true);
        }
        message("----------------------------------------------------------------------------", true);
    }

    private static int updateOrSelectMenu(Scanner scanner) {

        message("1. Count crawler status = 0 \t 2. Update task_id = null \t 3. delete machine \t 4. delete crawler task_typ = 3 \t 4. Exit", true);
        int selected = scanner.nextInt();
        return selected;
    }


    public static void main(String[] args) {
        ECommerceProductDetailDao dao = null;
        Scanner scanner = new Scanner(System.in);
        while (true) {
            mainMenu();
            int selected = scanner.nextInt();
            if (selected < 1 || selected > target) {
                message("Unknown operation code, Please reselect........", true);
                continue;
            } else {
                String fullName = fullNameMap.get(selected);
                dao = new ECommerceProductDetailDao(fullName);
                int oper = updateOrSelectMenu(scanner);
                if (oper == 2) {
                    message("Are you sure update database {}  ?,y/n:",
                            true, fullName);
                    String yesOrNo = scanner.next();
                    switch (yesOrNo) {
                        case "y":
                            message("start update....", true);
                            dao.loopUpdateTaskIdIsNull();
                            message("database {} updated ..", true, fullName);
                            break;
                    }
                } else if (oper == 1) {
                    message("start count....", true);
                    long start = System.currentTimeMillis();
                    int count = dao.findCountIsCrawlerStatusIsZero();
                    long end = System.currentTimeMillis();
                    message("database {} count:[{}],elapsed time:{} second", true, fullName, count,(end - start) / 1000);
                } else if (oper == 3) {
                    message("Are you sure delete crawler machine ? database {},y/n:",
                            true, fullName);
                    String yesOrNo = scanner.next();
                    switch (yesOrNo) {
                        case "y":
                            message("start delete crawler machine....", true);
                            int rows = dao.deleteCrawlerMachine();
                            message("deleted  rows: {}", true, rows);
                            break;
                    }
                    continue;
                }else if (oper == 4) {
                    message("Are you sure delete crawler task type = 3 ? database {},y/n:",
                            true, fullName);
                    String yesOrNo = scanner.next();
                    switch (yesOrNo) {
                        case "y":
                            message("start delete crawler task ....", true);
                            int rows = dao.deleteCrawlerTask();
                            message("deleted  rows: {}", true, rows);
                            break;
                    }
                }
            }
        }

    }
}
