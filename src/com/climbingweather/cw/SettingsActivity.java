package com.climbingweather.cw;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    
    private Context mContext;
    
    private SharedPreferences mPreferences;
    
    private TextView mTempUnitValue;
    
    /** 
     * Called when the activity is first created. 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        mContext = this;
        
        setTitle("Settings");
        setContentView(R.layout.settings);
        
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        
        TableRow tempUnitRow = (TableRow)findViewById(R.id.tempUnitRow);
        tempUnitRow.setOnClickListener(tempUnitListener);
        
        // Text view
        mTempUnitValue = (TextView)findViewById(R.id.tempUnitValue);
        String label = mPreferences.getString("tempUnit", "f").equals("f") ? "Fahrenheit" : "Celsius";
        mTempUnitValue.setText(label);
        
    }
    
    /**
     * On click listener for temp Unit button
     */
    private OnClickListener tempUnitListener = new OnClickListener() {
        
        public void onClick(View v) {
            
            
            final String[] items = {"Fahrenheit", "Celsius"};
            final String[] itemKeys = {"f", "c"};
            
            String tempUnit = mPreferences.getString("tempUnit", "f");
            int indexSelected = 0;
            if (tempUnit.equals("c")) {
                indexSelected = 1;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Temperature Units");
            builder.setSingleChoiceItems(
                        items, indexSelected, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    // string value is items[item]
                    SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putString("tempUnit", itemKeys[item]);
                    editor.commit();
                    Toast.makeText(getApplicationContext(), "Setting '" + items[item] + "' Saved", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    mTempUnitValue.setText(mPreferences.getString("tempUnit", "f").equals("f") ? "Fahrenheit" : "Celsius");
                }
                
            });
            AlertDialog alert = builder.create();
            alert.show();
            
        }
        
    };

}
