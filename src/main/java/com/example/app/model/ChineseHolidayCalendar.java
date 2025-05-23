package com.example.app.model;

import cn.hutool.core.date.ChineseDate;
import cn.hutool.core.date.DateUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChineseHolidayCalendar {
    // For demo purposes, only Spring Festival is considered a holiday
    // In lunar calendar, Spring Festival is always on the first day of the first lunar month
    private static final int SPRING_FESTIVAL_MONTH = 1;
    private static final int SPRING_FESTIVAL_DAY = 1;
    
    // Number of official holiday days for Spring Festival
    private static final int SPRING_FESTIVAL_DAYS = 7;
    
    public ChineseHolidayCalendar() {
        // No initialization needed
    }
    
    /**
     * Get Spring Festival date (first day) for a specific lunar year in Gregorian calendar
     */
    public LocalDate getSpringFestivalDate(int lunarYear) {
        // Create lunar date for Spring Festival
        ChineseDate lunarDate = new ChineseDate(lunarYear, SPRING_FESTIVAL_MONTH, SPRING_FESTIVAL_DAY);
        // Convert to Gregorian date
        Date gregorianDate = lunarDate.getGregorianDate();
        // Convert to LocalDate
        return DateUtil.toLocalDateTime(gregorianDate).toLocalDate();
    }
    
    /**
     * Get all Spring Festival holidays for a specific lunar year in Gregorian calendar
     */
    public List<LocalDate> getSpringFestivalHolidays(int lunarYear) {
        List<LocalDate> holidays = new ArrayList<>();
        LocalDate startDate = getSpringFestivalDate(lunarYear);
        
        // Add all days of the Spring Festival holiday period
        for (int i = 0; i < SPRING_FESTIVAL_DAYS; i++) {
            holidays.add(startDate.plusDays(i));
        }
        
        return holidays;
    }
    
    /**
     * Get all holidays for a given Gregorian calendar year
     */
    public List<LocalDate> getHolidaysForYear(int gregorianYear) {
        List<LocalDate> allHolidays = new ArrayList<>();
        
        // Get lunar years that might have Spring Festival in this Gregorian year
        
        // First check: typically most of year will match with lunar year
        ChineseDate midYearLunar = new ChineseDate(DateUtil.parse(gregorianYear + "-06-15"));
        int primaryLunarYear = midYearLunar.getChineseYear();
        
        // Also check previous lunar year (for January/February)
        int previousLunarYear = primaryLunarYear - 1;
        
        // Get Spring Festival dates for both lunar years
        List<LocalDate> previousLunarYearHolidays = getSpringFestivalHolidays(previousLunarYear);
        List<LocalDate> primaryLunarYearHolidays = getSpringFestivalHolidays(primaryLunarYear);
        
        // Add holidays that fall in the requested Gregorian year
        for (LocalDate holiday : previousLunarYearHolidays) {
            if (holiday.getYear() == gregorianYear) {
                allHolidays.add(holiday);
            }
        }
        
        for (LocalDate holiday : primaryLunarYearHolidays) {
            if (holiday.getYear() == gregorianYear) {
                allHolidays.add(holiday);
            }
        }
        
        return allHolidays;
    }
    
    /**
     * Get all holidays within a date range in Gregorian calendar
     */
    public List<LocalDate> getHolidaysInPeriod(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> holidays = new ArrayList<>();
        
        // Get all possible holiday years in the range
        int startYear = startDate.getYear();
        int endYear = endDate.getYear();
        
        for (int year = startYear; year <= endYear; year++) {
            List<LocalDate> yearHolidays = getHolidaysForYear(year);
            for (LocalDate holiday : yearHolidays) {
                if (!holiday.isBefore(startDate) && !holiday.isAfter(endDate)) {
                    holidays.add(holiday);
                }
            }
        }
        
        return holidays;
    }
    
    /**
     * Get all holidays within a date range in Chinese lunar calendar format
     */
    public List<ChineseDate> getHolidaysInPeriodLunar(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> gregorianHolidays = getHolidaysInPeriod(startDate, endDate);
        List<ChineseDate> lunarHolidays = new ArrayList<>();
        
        for (LocalDate date : gregorianHolidays) {
            ChineseDate lunarDate = toChineseDate(date);
            lunarHolidays.add(lunarDate);
        }
        
        return lunarHolidays;
    }
    
    /**
     * Check if a given date is a holiday
     */
    public boolean isHoliday(LocalDate date) {
        List<LocalDate> yearHolidays = getHolidaysForYear(date.getYear());
        return yearHolidays.stream().anyMatch(holiday -> holiday.equals(date));
    }
    
    /**
     * Convert Gregorian date to Chinese lunar date
     */
    public ChineseDate toChineseDate(LocalDate date) {
        Date utilDate = DateUtil.date(date);
        return new ChineseDate(utilDate);
    }
    
    /**
     * Convert Chinese lunar date to Gregorian date
     */
    public LocalDate fromChineseDate(int lunarYear, int lunarMonth, int lunarDay) {
        ChineseDate lunarDate = new ChineseDate(lunarYear, lunarMonth, lunarDay);
        Date gregorianDate = lunarDate.getGregorianDate();
        return DateUtil.toLocalDateTime(gregorianDate).toLocalDate();
    }
    
    /**
     * Format a date in Chinese lunar calendar representation
     */
    public String formatChineseDate(LocalDate date) {
        ChineseDate chineseDate = toChineseDate(date);
        return chineseDate.toString();
    }
    
    /**
     * Get the lunar year for a Gregorian date
     */
    public int getLunarYear(LocalDate date) {
        ChineseDate chineseDate = toChineseDate(date);
        return chineseDate.getChineseYear();
    }
}