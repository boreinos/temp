package com.afilon.mayor.v11.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.model.Ballot;
import com.afilon.mayor.v11.model.DrawerItem;

import java.util.ArrayList;

/**
 * Created by BReinosa on 8/10/2017.
 * currently unused 8/11/2017
 */
public class DrawerAdapter extends ArrayAdapter<DrawerItem> {
    Context context;
    ArrayList<DrawerItem> drawerItemList;
    int layoutResId;

    public DrawerAdapter(Context context, int resource) {
        super(context, resource);
    }

    public DrawerAdapter(Context context, int layoutResourceID, ArrayList<DrawerItem> listItmes) {
        super(context,layoutResourceID,listItmes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        DrawerItemHolder drawerHolder;
        View view = convertView;
        if(view ==null){
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            drawerHolder = new DrawerItemHolder();
            view = inflater.inflate(layoutResId, parent, false);
            drawerHolder.ItemName = (TextView) view.findViewById(R.id.drawer_itemName);
            drawerHolder.quantity = (TextView) view.findViewById(R.id.drawer_qnty);
            drawerHolder.icon = (ImageView) view.findViewById(R.id.drawer_icon);
            view.setTag(drawerHolder);
        } else {
            drawerHolder = (DrawerItemHolder) view.getTag();
        }
        DrawerItem dItem = (DrawerItem) this.drawerItemList.get(position);
        drawerHolder.icon.setImageDrawable((view.getResources().getDrawable(dItem.getImgResID())));
        drawerHolder.ItemName.setText(dItem.getItemName());
        drawerHolder.quantity.setText(dItem.getQuantity());
        return view;
    }

    private static class DrawerItemHolder{
        TextView ItemName;
        ImageView icon;
        TextView quantity;
    }

}
