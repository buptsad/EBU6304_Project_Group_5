package com.example.app.model;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * 负责管理预算数据的保存和加载
 */
public class BudgetManager {
    
    private static final String BUDGETS_FILE_NAME = "user_budgets.csv";
    
    /**
     * 保存预算数据到CSV文件
     * @param categoryBudgets 类别预算映射
     * @param directory CSV文件所在目录
     */
    public static void saveBudgetsToCSV(Map<String, Double> categoryBudgets, String directory) {
        Path filePath = Paths.get(directory, BUDGETS_FILE_NAME);
        
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            // 写入CSV头部
            writer.write("Category,Budget");
            writer.newLine();
            
            // 写入每个类别和预算
            for (Map.Entry<String, Double> entry : categoryBudgets.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
            
            System.out.println("预算数据已成功保存到: " + filePath);
            
        } catch (IOException e) {
            System.err.println("保存预算数据时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 从CSV文件加载预算数据
     * @param directory CSV文件所在目录
     * @return 类别预算映射
     */
    public static Map<String, Double> loadBudgetsFromCSV(String directory) {
        Map<String, Double> categoryBudgets = new LinkedHashMap<>();
        Path filePath = Paths.get(directory, BUDGETS_FILE_NAME);
        
        // 检查文件是否存在
        if (!Files.exists(filePath)) {
            System.out.println("预算文件不存在，将使用默认预算");
            return categoryBudgets; // 返回空映射，后续会填充默认值
        }
        
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            // 跳过CSV头部
            String line = reader.readLine();
            
            // 读取每行数据
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("//")) {
                    continue; // 跳过空行和注释
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String category = parts[0].trim();
                    try {
                        double budget = Double.parseDouble(parts[1].trim());
                        categoryBudgets.put(category, budget);
                    } catch (NumberFormatException e) {
                        System.err.println("解析预算值时出错: " + parts[1]);
                    }
                }
            }
            
            System.out.println("从 " + filePath + " 成功加载了 " + categoryBudgets.size() + " 个预算类别");
            
        } catch (IOException e) {
            System.err.println("加载预算数据时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return categoryBudgets;
    }
}