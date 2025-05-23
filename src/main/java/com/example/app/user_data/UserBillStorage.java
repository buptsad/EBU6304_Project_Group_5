package com.example.app.user_data;

import com.example.app.ui.pages.AI.classification;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 存储用户账单数据
 * 这个类管理用户账单数据的物理存储
 */
public class UserBillStorage {
    private static final Logger LOGGER = Logger.getLogger(UserBillStorage.class.getName());
    private static final String BILL_FILENAME = "user_bill.csv";
    private static File billFile;
    private static String username; // 添加用户名字段
    
    // 定义CSV格式
    private static final String CSV_HEADER = "Date,Description,Category,Amount,Confirmed";
    private static final String CSV_FORMAT = "%s,%s,%s,%.2f,%b";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    /**
     * 设置当前用户名，并更新文件路径
     * @param username 当前用户的用户名
     */
    public static void setUsername(String username) {
        UserBillStorage.username = username;
        // 更新文件路径为用户特定路径
        String packagePath = ".\\user_data\\" + username;
        billFile = new File(packagePath, BILL_FILENAME);
        
        // 确保文件存在
        initializeStorage();
    }
    
    /**
     * 初始化存储目录和文件
     */
    private static void initializeStorage() {
        File directory = billFile.getParentFile();
        
        // 创建目录(如果不存在)
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                LOGGER.log(Level.INFO, "Created bill directory at: {0}", directory.getAbsolutePath());
            } else {
                LOGGER.log(Level.SEVERE, "Could not create bill directory at: {0}", directory.getAbsolutePath());
                return;
            }
        }
        
        // 如果文件不存在则创建文件
        if (!billFile.exists()) {
            try {
                if (billFile.createNewFile()) {
                    LOGGER.log(Level.INFO, "Created bill file at: {0}", billFile.getAbsolutePath());
                    
                    // 初始化CSV文件，添加表头
                    try (PrintWriter writer = new PrintWriter(new FileWriter(billFile))) {
                        writer.println(CSV_HEADER);
                    }
                    
                    LOGGER.log(Level.INFO, "Initialized CSV file header");
                } else {
                    LOGGER.log(Level.SEVERE, "Could not create bill file at: {0}", billFile.getAbsolutePath());
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error creating bill file", e);
            }
        } else {
            LOGGER.log(Level.INFO, "Bill file already exists at: {0}", billFile.getAbsolutePath());
        }
    }
    
    /**
     * 获取账单文件的路径
     * @return 账单文件的路径
     */
    public static String getBillFilePath() {
        return billFile.getAbsolutePath();
    }
    
    /**
     * 从CSV文件加载交易记录
     * @return 交易记录的列表
     */
    public static List<Object[]> loadTransactions() {
        List<Object[]> transactions = new ArrayList<>();
        
        // 确认文件存在
        if (!billFile.exists()) {
            LOGGER.log(Level.WARNING, "Bill file does not exist: {0}", billFile.getAbsolutePath());
            return transactions;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(billFile))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                // 跳过表头行
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // 处理CSV行，注意处理可能包含逗号的字段
                String[] parts = parseCSVLine(line);
                if (parts.length >= 5) {
                    try {
                        String dateStr = parts[0];
                        String description = parts[1];
                        String category = parts[2];
                        double amount = Double.parseDouble(parts[3]);
                        boolean confirmed = Boolean.parseBoolean(parts[4]);
                        
                        Object[] transaction = {dateStr, description, category, amount, confirmed};
                        transactions.add(transaction);
                    } catch (NumberFormatException e) {
                        LOGGER.log(Level.WARNING, "Error parsing transaction: " + line, e);
                    }
                }
            }
            
            LOGGER.log(Level.INFO, "Successfully loaded transactions from: {0}", billFile.getAbsolutePath());
            LOGGER.log(Level.INFO, "Loaded {0} transactions", transactions.size());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading transactions from file: " + e.getMessage(), e);
        }
        
        return transactions;
    }
    
    /**
     * 解析一行CSV数据，处理引号内可能包含逗号的情况
     */
    private static String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        // 添加最后一个字段
        result.add(current.toString());
        
        return result.toArray(new String[0]);
    }
    
    /**
     * 将交易记录保存到CSV文件
     * @param transactions 要保存的交易记录列表
     * @return 如果成功则返回true，否则返回false
     */
    public static boolean saveTransactions(List<Object[]> transactions) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(billFile))) {
            // 写入CSV表头
            writer.println(CSV_HEADER);
            
            // 如果没有交易记录，直接返回
            if (transactions.isEmpty()) {
                LOGGER.log(Level.INFO, "No transactions to save");
                return true;
            }
            
            // 写入每一条交易记录，不再进行AI分类
            for (Object[] transaction : transactions) {
                String dateStr = (String) transaction[0];
                String description = escapeCSV((String) transaction[1]);
                // 直接使用交易记录中的分类，不再调用AI进行分类
                String category = escapeCSV((String) transaction[2]);
                double amount = (Double) transaction[3];
                boolean confirmed = transaction.length > 4 ? (Boolean) transaction[4] : false;
                
                writer.println(String.format(CSV_FORMAT, dateStr, description, category, amount, confirmed));
            }
            
            LOGGER.log(Level.INFO, "Successfully saved {0} transactions to: {1}", 
                    new Object[]{transactions.size(), billFile.getAbsolutePath()});
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving transactions to file: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 转义CSV字段中的特殊字符
     */
    private static String escapeCSV(String field) {
        if (field == null) {
            return "";
        }
        
        // 如果字段包含逗号、引号或换行符，则用双引号包围并将内部引号替换为两个引号
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
    
    /**
     * 将新交易记录添加到现有记录中
     * @param newTransactions 要添加的新交易记录列表
     * @return 如果成功则返回true，否则返回false
     */
    public static boolean addTransactions(List<Object[]> newTransactions) {
        // 加载现有交易记录
        List<Object[]> existingTransactions = loadTransactions();
        
        // 添加新交易记录
        existingTransactions.addAll(newTransactions);
        
        // 保存更新后的交易记录
        return saveTransactions(existingTransactions);
    }
}