package com.climbingweather.cw;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class AreaListAdapter extends ArrayAdapter<Area>
{
    private Context context;
    
    private List<Area> items;
    
    public AreaListAdapter(Context context, int resource, List<Area> items)
    {
        super(context, resource, items);
        this.context = context;
        this.items = items;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Area area = items.get(position);
        return area.getListRowView(convertView, parent, context);
    }
}