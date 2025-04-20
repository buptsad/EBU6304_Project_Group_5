package com.example.app.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 管理应用程序主题设置，支持持久化存储和加载
 */
public class ThemeManager {
    private static final Logger logger = LoggerFactory.getLogger(ThemeManager.class);
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.app";
    private static final String CONFIG_FILE = CONFIG_DIR + "/theme.properties";
    private static final String THEME_KEY = "theme";
    
    // 使用枚举增强类型安全性
    public enum Theme { DARK, LIGHT }
    
    // 单例实例（双重检查锁定优化）
    private static volatile ThemeManager instance;
    private Theme currentTheme;
    
    private ThemeManager() {
        loadTheme();
    }
    
    public static ThemeManager getInstance() {
        if (instance == null) {
            synchronized (ThemeManager.class) {
                if (instance == null) {
                    instance = new ThemeManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 获取当前主题
     */
    public Theme getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * 设置主题并保存
     */
    public void setTheme(Theme theme) {
        currentTheme = theme;
        saveTheme();
    }
    
    /**
     * 从配置文件加载主题
     */
    private void loadTheme() {
        Properties properties = new Properties();
        Path configPath = Paths.get(CONFIG_FILE);
        
        try {
            if (Files.exists(configPath)) {
                try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
                    properties.load(fis);
                    String themeValue = properties.getProperty(THEME_KEY, Theme.DARK.name());
                    currentTheme = Theme.valueOf(themeValue);
                }
            } else {
                // 创建配置目录（如果不存在）
                Files.createDirectories(Paths.get(CONFIG_DIR));
                currentTheme = Theme.DARK;
            }
        } catch (Exception e) {
            logger.error("Failed to load theme configuration, using default.", e);
            currentTheme = Theme.DARK;
        }
    }
    
    /**
     * 保存主题到配置文件
     */
    private void saveTheme() {
        Properties properties = new Properties();
        properties.setProperty(THEME_KEY, currentTheme.name());
        
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Theme Configuration");
        } catch (Exception e) {
            logger.error("Failed to save theme configuration.", e);
        }
    }
}