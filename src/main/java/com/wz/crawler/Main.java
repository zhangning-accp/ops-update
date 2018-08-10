package com.wz.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javafx.scene.chart.PieChart;

public class Main {
    private static int target = 1;
    private static MultiDataSource dataSource = MultiDataSource.getInstance();
    private static Map<Integer,String> fullNameMap = new HashMap<>();

    /**
     *
     * @param message 消息内容.如果内容有变量，采用{}方式
     * @param values 消息值
     */
    private static void message(String message, boolean isLine,Object ... values) {
        message = message.replaceAll("\\{\\}","%s");
        message = String.format(message, values);
        if(isLine) {
            System.out.println(message);
        } else {
            System.out.print(message);
        }
    }
    private static void mainMenu() {
        Map<String,List<DataSource>> map = dataSource.getDataSource();
        List<String> fullDBNames = new ArrayList<>();
        Iterator<String> iterator = map.keySet().iterator();
        message("请选择需要 update 的库",true);
        message("----------------------------------------------------------------------------",true);
        target = 1;
        fullNameMap.clear();
        while(iterator.hasNext()) {
            String key = iterator.next();
            List<DataSource> list = map.get(key);
            for(DataSource dataSource : list) {
                message("{}: {}\t",false,target,dataSource.getFullDbName());
                fullNameMap.put(target,dataSource.getFullDbName());
                target ++;

            }
            message("",true);
        }
        message("----------------------------------------------------------------------------",true);
    }


    public static void main(String[] args) {
        ECommerceProductDetailDao dao = null;
	    Scanner scanner = new Scanner(System.in);
	    while(true) {
            mainMenu();
            int selected = scanner.nextInt();
            if (selected < 1 || selected > target) {
                message("无法识别的操作代码，请重新选择......", true);
                continue;
            } else {
                String fullName = fullNameMap.get(selected);
                message("确定要对数据库{} 进行update操作吗？，此操作中途不可撤销，输入y开始，输入n退出此操作",
                        true, fullName);
                String yesOrNo = scanner.next();
                switch (yesOrNo) {
                    case "y":
                        dao = new ECommerceProductDetailDao(fullName);
                        dao.loopUpdateTaskIdIsNull();
                        message("数据库{} 更新task_id 完毕..", true, fullName);
                        break;
               }
           }
        }

    }
}
