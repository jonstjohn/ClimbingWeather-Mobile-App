package com.climbingweather.cw;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class RESTClient {
    
    /**
     * Class for logging
     */
    private static final String TAG = RESTLoader.class.getName();
    
    /**
     * Response
     */
    private RESTClientResponse mRestResponse;
    
    /**
     * Enum for HTTP methods
     */
    public enum HTTPMethod {
        GET,
        POST,
        PUT,
        DELETE
    }
    
    /**
     * HTTP method
     */
    private HTTPMethod mMethod;
    
    /**
     * REST uri
     */
    private Uri mUri;
    
    private Bundle mParams;
    
    public RESTClient(HTTPMethod method, Uri uri) {
        
        mMethod   = method;
        mUri = uri;
    }
    
    public RESTClient(HTTPMethod method, Uri uri, Bundle params) {
        mMethod   = method;
        mUri = uri;
        mParams = params;
    }

    /**
     * Send request
     * @return
     */
    public RESTClientResponse sendRequest() {
        
        try {
            // At the very least we always need an action.
            if (mUri == null) {
                Log.e(TAG, "You did not define an action. REST call canceled.");
                return new RESTClientResponse();
            }
            
            HttpRequestBase request = null;
            
            // Build request based on HTTP method
            switch (mMethod) {
            
                case GET:
                    request = new HttpGet();
                    attachUriWithQuery(request, mUri, mParams);
                    break;
                
                case DELETE:
                    request = new HttpDelete();
                    attachUriWithQuery(request, mUri, mParams);
                    break;
                
                case POST:
                    request = new HttpPost();
                    request.setURI(new URI(mUri.toString()));
                    
                    // Attach form entity if necessary. Note: some REST APIs
                    // require you to POST JSON. This is easy to do, simply use
                    // postRequest.setHeader('Content-Type', 'application/json')
                    // and StringEntity instead. Same thing for the PUT case 
                    // below.
                    HttpPost postRequest = (HttpPost) request;
                    
                    if (mParams != null) {
                        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(paramsToList(mParams));
                        postRequest.setEntity(formEntity);
                    }
                    break;
                
                case PUT:
                    request = new HttpPut();
                    request.setURI(new URI(mUri.toString()));
                    
                    // Attach form entity if necessary.
                    HttpPut putRequest = (HttpPut) request;
                    
                    if (mParams != null) {
                        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(paramsToList(mParams));
                        putRequest.setEntity(formEntity);
                    }
                    break;
            }
            
            if (request != null) {
                HttpClient client = new DefaultHttpClient();
                
                Log.d(TAG, "Executing request: "+ methodToString(mMethod) +": "+ mUri.toString());
                
                // Finally, we send our request using HTTP, synchronous.
                HttpResponse response = client.execute(request);
                
                HttpEntity responseEntity = response.getEntity();
                StatusLine responseStatus = response.getStatusLine();
                int statusCode = responseStatus != null ? responseStatus.getStatusCode() : 0;
                
                // Create repsonse and return
                mRestResponse = new RESTClientResponse(
                        responseEntity != null
                                ? EntityUtils.toString(responseEntity)
                                : null,
                        statusCode);
                return mRestResponse;
            }
            
            // Request was null if we get here, so let's just send our empty RESTResponse like usual.
            return new RESTClientResponse();
        }
        catch (URISyntaxException e) {
            Log.e(TAG, "URI syntax was incorrect. "+ methodToString(mMethod) +": "+ mUri.toString(), e);
            return new RESTClientResponse();
        }
        catch (UnsupportedEncodingException e) {
            Log.e(TAG, "A UrlEncodedFormEntity was created with an unsupported encoding.", e);
            return new RESTClientResponse();
        }
        catch (ClientProtocolException e) {
            Log.e(TAG, "There was a problem when sending the request.", e);
            return new RESTClientResponse();
        }
        catch (IOException e) {
            Log.e(TAG, "There was a problem when sending the request.", e);
            return new RESTClientResponse();
        }
    }
    
    /**
     * Client response
     */
    public static class RESTClientResponse {
        
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
    
    /**
     * Atach URI with query
     * @param request
     * @param uri
     * @param params
     */
    private static void attachUriWithQuery(HttpRequestBase request, Uri uri, Bundle params) {
        try {
            if (params == null) {
                // No params were given or they have already been
                // attached to the Uri.
                request.setURI(new URI(uri.toString()));
            }
            else {
                Uri.Builder uriBuilder = uri.buildUpon();
                
                // Loop through our params and append them to the Uri.
                for (BasicNameValuePair param : paramsToList(params)) {
                    uriBuilder.appendQueryParameter(param.getName(), param.getValue());
                }
                
                uri = uriBuilder.build();
                request.setURI(new URI(uri.toString()));
            }
        }
        catch (URISyntaxException e) {
            Log.e(TAG, "URI syntax was incorrect: "+ uri.toString());
        }
    }
    
    private static List<BasicNameValuePair> paramsToList(Bundle params) {
        ArrayList<BasicNameValuePair> formList = new ArrayList<BasicNameValuePair>(params.size());
        
        for (String key : params.keySet()) {
            Object value = params.get(key);
            
            // We can only put Strings in a form entity, so we call the toString()
            // method to enforce. We also probably don't need to check for null here
            // but we do anyway because Bundle.get() can return null.
            if (value != null) formList.add(new BasicNameValuePair(key, value.toString()));
        }
        
        return formList;
    }
    
    private static String methodToString(HTTPMethod method) {
        switch (method) {
            case GET:
                return "GET";
                
            case POST:
                return "POST";
                
            case PUT:
                return "PUT";
                
            case DELETE:
                return "DELETE";
        }
        
        return "";
    }
}
