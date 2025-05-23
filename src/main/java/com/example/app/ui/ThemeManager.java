package com.example.app.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * 管理应用程序主题设置
 */
public class ThemeManager {
    private static final String CONFIG_FILE = "theme.properties";
    private static final String THEME_KEY = "theme";
    private static final String DARK_THEME = "dark";
    private static final String LIGHT_THEME = "light";
    
    private static ThemeManager instance;
    private String currentTheme;
    
    private ThemeManager() {
        // 从配置文件加载主题设置
        loadTheme();
    }
    
    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    /**
     * 获取当前主题
     * @return true 表示暗色主题，false 表示亮色主题
     */
    public boolean isDarkTheme() {
        return DARK_THEME.equals(currentTheme);
    }
    
    /**
     * 设置主题并保存到配置文件
     * @param isDark true 表示暗色主题，false 表示亮色主题
     */
    public void setTheme(boolean isDark) {
        currentTheme = isDark ? DARK_THEME : LIGHT_THEME;
        saveTheme();
    }
    
    /**
     * 从配置文件加载主题设置
     */
    private void loadTheme() {
        Properties properties = new Properties();
        File configFile = new File(CONFIG_FILE);
        
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
                currentTheme = properties.getProperty(THEME_KEY, DARK_THEME);
            } catch (Exception e) {
                System.err.println("Error loading theme configuration: " + e.getMessage());
                currentTheme = DARK_THEME; // 默认为暗色主题
            }
        } else {
            currentTheme = DARK_THEME; // 默认为暗色主题
        }
    }
    
    /**
     * 保存主题设置到配置文件
     */
    private void saveTheme() {
        Properties properties = new Properties();
        properties.setProperty(THEME_KEY, currentTheme);
        
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Theme Configuration");
        } catch (Exception e) {
            System.err.println("Error saving theme configuration: " + e.getMessage());
        }
    }
}