package com.climbingweather.cw;

import android.net.Uri;

public final class AreasContract {
    
    /**
     * Authority
     */
    public static final String AUTHORITY = "com.climbingweather.cw.provider.areas";
    
    /**
     * URI
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    
    public static final Uri FAVORITES_URI = Uri.parse("content://" + AUTHORITY + "/favorites");
    
    public interface Columns {
        public static final String AREA_ID = "_id";
        public static final String NAME = "name";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String STATE_CODE = "state_code";
        public static final String ELEVATION = "elevation";
        public static final String LIST_UPDATED = "list_updated";
        public static final String DETAIL_UPDATED = "detail_updated";
        public static final String DAILY_UPDATED = "daily_updated";
        public static final String HOURLY_UPDATED = "hourly_updated";
        public static final String AVERAGES_UPDATED = "averages_updated";
        public static final String NEARBY = "nearby";
        public static final String DAY1_HIGH = "d1_high";
        public static final String DAY1_SYMBOL = "d1_wsym";
        public static final String DAY2_HIGH = "d2_high";
        public static final String DAY2_SYMBOL = "d2_wsym";
        public static final String DAY3_HIGH = "d3_high";
        public static final String DAY3_SYMBOL = "d3_wsym";
    }
    
    public static final String[] PROJECTION_DEFAULT = {"area._id", "area._id AS area_id", "area.state_code", "area.name", "d1.high AS d1_high",
        "d1.wsym AS d1_wsym", "d2.high AS d2_high", "d2.wsym AS d2_wsym", "d3.high AS d3_high", "d3.wsym AS d3_wsym"};

}
