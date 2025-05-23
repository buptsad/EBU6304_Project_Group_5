package com.example.app.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CSVDataImporter {
    
    public static List<Object[]> importTransactionsFromCSV(String filePath) {
        List<Object[]> transactions = new ArrayList<>();
        // 用于去重的集合
        Set<String> uniqueTransactions = new HashSet<>();
        
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath))) {
            // 跳过标题行
            String line = br.readLine();
            
            // 读取数据行
            while ((line = br.readLine()) != null) {
                // 跳过注释行或空行
                if (line.trim().startsWith("//") || line.trim().isEmpty()) {
                    continue;
                }
                
                // 分割CSV行
                String[] parts = line.split(",");
                if (parts.length < 4) {
                    System.err.println("无效的CSV行: " + line);
                    continue;
                }
                
                String date = parts[0].trim();
                String description = parts[1].trim();
                String category = parts[2].trim();
                
                // 处理金额
                double amount;
                try {
                    amount = Double.parseDouble(parts[3].trim());
                } catch (NumberFormatException e) {
                    System.err.println("无效的金额: " + parts[3]);
                    continue;
                }
                
                // 创建唯一标识，用于去重
                String uniqueKey = date + "|" + description + "|" + category + "|" + amount;
                if (!uniqueTransactions.contains(uniqueKey)) {
                    // 只有唯一的交易才添加
                    uniqueTransactions.add(uniqueKey);
                    Object[] transaction = new Object[] {date, description, category, amount};
                    transactions.add(transaction);
                } else {
                    System.out.println("跳过重复交易: " + date + " " + description + " " + amount);
                }
            }
            
        } catch (IOException e) {
            System.err.println("读取CSV文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
}