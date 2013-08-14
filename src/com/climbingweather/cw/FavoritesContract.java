package com.climbingweather.cw;

import android.net.Uri;

public final class FavoritesContract {
    
    /**
     * Authority
     */
    public static final String AUTHORITY = "com.climbingweather.cw.provider.favorites";
    
    /**
     * URI
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    
    public interface Columns {
        public static final String ID = "_id";
        public static final String AREA_ID = "area_id";
        public static final String NAME = "name";
    }

}
