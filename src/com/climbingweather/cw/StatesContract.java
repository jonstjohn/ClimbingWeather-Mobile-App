package com.climbingweather.cw;

import android.net.Uri;

public final class StatesContract {
    
    /**
     * URI
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + CwContentProvider.AUTHORITY + "/states");
    
    /**
     * Columns
     */
    public interface Columns {
        public static final String ID = "_id";
        public static final String STATE_CODE = "state_code";
        public static final String NAME = "name";
        public static final String AREAS = "areas";
        public static final String UPDATED = "updated";
    }

}
