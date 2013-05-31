package com.climbingweather.cw;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import android.content.Context;

public class CwCache {
    
    private Context context;
    
    // Cache time for API results
    private final static Long cacheSeconds = 1800L; // 1800 = 30 minutes
    public final static Long cacheMillis = cacheSeconds * 1000;
    
    public CwCache(Context context)
    {
        this.context = context;
    }

    public void write(String fileName, String string)
    {
        File file = new File(context.getCacheDir(), fileName);
        writeToFile(file, string);
    }
    
    public String read(String fileName) throws IOException
    {
        File file = new File(context.getCacheDir(), fileName);
        return readFileAsString(file.getAbsolutePath());
    }
    
    public boolean isFresh(String fileName)
    {
        File file = new File(context.getCacheDir(), fileName);
        return file.exists() && file.lastModified() / 1000L > (System.currentTimeMillis() / 1000L - cacheSeconds);
    }
    
    private static String readFileAsString(String filePath) throws java.io.IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line, results = "";
        while( ( line = reader.readLine() ) != null)
        {
            results += line;
        }
        reader.close();
        return results;
    }
    
    private static void writeToFile(File file, String string)
    {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            fos.write(string.getBytes());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            
        }
        
        try {
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
