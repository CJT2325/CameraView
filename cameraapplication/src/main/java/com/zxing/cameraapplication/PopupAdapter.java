package com.zxing.cameraapplication;

import android.content.Context;
import android.hardware.Camera;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.1.4
 * 创建日期：2017/10/10
 * 描    述：
 * =====================================
 */
public class PopupAdapter extends BaseAdapter {

    List<Camera.Size> list;
    Context context;
    LayoutInflater inflater;

    public PopupAdapter(Context context, List<Camera.Size> list) {
        this.context = context;
        this.list = list;
        this.inflater = LayoutInflater.from(context);
    }

    public void setData(List<Camera.Size> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
        }
        TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
        Camera.Size size = list.get(position);
        tv.setText(size.width + " X " + size.height);
        return convertView;
    }

    public Camera.Size getSize(int position) {
        return list.get(position);
    }
}
