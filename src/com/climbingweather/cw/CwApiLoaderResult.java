package com.climbingweather.cw;

public class CwApiLoaderResult {
    
    RESTClientResponse mResponse;

    public CwApiLoaderResult(RESTClientResponse response) {
        mResponse = response;
    }
    
    public RESTClientResponse getResponse()
    {
        return mResponse;
    }

}
