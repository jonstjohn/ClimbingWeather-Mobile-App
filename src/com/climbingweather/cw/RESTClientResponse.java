package com.climbingweather.cw;

/**
 * Client response
 */
public class RESTClientResponse {
    
    private String mData;
    
    private int mCode;
    
    public RESTClientResponse() {
    }
    
    public RESTClientResponse(String data, int code) {
        mData = data;
        mCode = code;
    }
    
    public String getData() {
        return mData;
    }
    
    public int getCode() {
        return mCode;
    }
}
