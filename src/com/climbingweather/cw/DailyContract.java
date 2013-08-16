package com.climbingweather.cw;

import android.net.Uri;

public final class DailyContract {

    /**
     * Authority
     */
    public static final String AUTHORITY = "com.climbingweather.cw.provider.daily";
    
    /**
     * URI
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    
    public interface Columns {
        public static final String ID = "_id";
        public static final String AREA_ID = "area_id";
        public static final String HIGH = "high";
        public static final String LOW = "low";
        public static final String PRECIP_DAY = "precip_day";
        public static final String PRECIP_NIGHT = "precip_night";
        public static final String DATE = "date";
        public static final String DATE_FORMATTED = "date_formatted";
        public static final String DAY = "day";
        public static final String SKY = "sky";
        public static final String RELATIVE_HUMIDITY = "relative_humidity";
        public static final String WEATHER_SYMBOL = "wsym";
        public static final String RAIN_AMOUNT= "rain_amount";
        public static final String SNOW_AMOUNT= "snow_amount";
        public static final String WIND_SUSTAINED = "wind_sustained";
        public static final String WIND_GUST= "wind_gust";
        public static final String WEATHER = "weather";
        public static final String UPDATED = "updated";
        public static final String DETAIL_UPDATED = "detail_updated";
    }
}
