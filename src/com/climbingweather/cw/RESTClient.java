package com.climbingweather.cw;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class RESTClient {
    
    /**
     * Class for logging
     */
    private static final String TAG = RESTClient.class.getName();
    
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
    
    /**
     * Parameters
     */
    private Bundle mParams;
    
    /**
     * Constructor
     * @param method
     * @param uri
     */
    public RESTClient(HTTPMethod method, Uri uri) {
        
        mMethod   = method;
        mUri = uri;
    }
    
    /**
     * Constructor
     * @param method
     * @param uri
     * @param params
     */
    public RESTClient(HTTPMethod method, Uri uri, Bundle params) {
        mMethod   = method;
        mUri = uri;
        mParams = params;
    }

    /**
     * Send request
     * @return RESTClientResponse
     */
    public RESTClientResponse sendRequest() {
        
        try {
            if (mUri == null) {
                Log.e(TAG, "You did not define a URI. REST call canceled.");
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
                DefaultHttpClient client = new DefaultHttpClient();
                
                // Enable GZIP
                    client.addRequestInterceptor(new HttpRequestInterceptor() {

                        public void process(
                                final HttpRequest request,
                                final HttpContext context) throws HttpException, IOException {
                            if (!request.containsHeader("Accept-Encoding")) {
                                request.addHeader("Accept-Encoding", "gzip");
                            }
                        }

                    });

                    client.addResponseInterceptor(new HttpResponseInterceptor() {

                        public void process(
                                final HttpResponse response,
                                final HttpContext context) throws HttpException, IOException {
                            HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                Header ceheader = entity.getContentEncoding();
                                if (ceheader != null) {
                                    HeaderElement[] codecs = ceheader.getElements();
                                    for (int i = 0; i < codecs.length; i++) {
                                        if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                                            response.setEntity(
                                                    new GzipDecompressingEntity(response.getEntity()));
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                        
                        

                    });
                    
                    Log.d(TAG, "Executing request: "+ methodToString(mMethod) +": "+ mUri.toString());
                    
                    // Finally, we send our request using HTTP, synchronous.
                    HttpResponse response = client.execute(request);
                    
                    HttpEntity responseEntity = response.getEntity();
                    StatusLine responseStatus = response.getStatusLine();
                    int statusCode = responseStatus != null ? responseStatus.getStatusCode() : 0;
                    
                    // Create response and return
                    mRestResponse = new RESTClientResponse(
                            responseEntity != null
                                    ? EntityUtils.toString(responseEntity)
                                    : null,
                            statusCode);
                    return mRestResponse;
                
            }
            
            // Request was null if we get here, so let's just send our empty RESTResponse like usual.
            return new RESTClientResponse();
        } catch (URISyntaxException e) {
            Log.e(TAG, "URI syntax was incorrect. "+ methodToString(mMethod) +": "+ mUri.toString(), e);
            return new RESTClientResponse();
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "A UrlEncodedFormEntity was created with an unsupported encoding.", e);
            return new RESTClientResponse();
        } catch (ClientProtocolException e) {
            Log.e(TAG, "There was a problem when sending the request.", e);
            return new RESTClientResponse();
        } catch (IOException e) {
            Log.e(TAG, "There was a problem when sending the request.", e);
            return new RESTClientResponse();
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
        } catch (URISyntaxException e) {
            Log.e(TAG, "URI syntax was incorrect: "+ uri.toString());
        }
    }
    
    /**
     * Convert params to list
     * @param params
     * @return
     */
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
    
    /**
     * Convert method to string
     * @param method
     * @return
     */
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
    
    static class GzipDecompressingEntity extends HttpEntityWrapper {
        public GzipDecompressingEntity(final HttpEntity entity) {
           super(entity);
        }

        @Override
        public InputStream getContent() throws IOException, IllegalStateException {
           // the wrapped entity's getContent() decides about repeatability
           InputStream wrappedin = wrappedEntity.getContent();
           return new GZIPInputStream(wrappedin);
        }

        @Override
        public long getContentLength() {
           // length of ungzipped content is not known
           return -1;
        }
}
}
