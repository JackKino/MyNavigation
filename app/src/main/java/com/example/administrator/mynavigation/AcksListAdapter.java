package com.example.administrator.mynavigation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inuker.bluetooth.library.search.SearchResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by dingjikerbo on 2016/9/1.
 */
public class AcksListAdapter extends BaseAdapter implements Comparator<SearchResult> {

    private Context mContext;

    private List<String> mDataList;

    public AcksListAdapter(Context context) {
        mContext = context;
        mDataList = new ArrayList<String>();
    }

    public void setDataList(List<String> datas) {
        mDataList.clear();
        mDataList.addAll(datas);
      //  Collections.sort(mDataList, this);
        notifyDataSetChanged();
    }

    public void addDataList(List<String> datas) {
        mDataList.clear();
        mDataList.addAll(datas);
       // Collections.sort(mDataList, this);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int compare(SearchResult lhs, SearchResult rhs) {
        return rhs.rssi - lhs.rssi;
    }

    private static class ViewHolder {
        TextView acks_title;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.acks_listitem, null, false);

            holder = new ViewHolder();
            holder.acks_title = (TextView) convertView.findViewById(R.id.acks_title);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.acks_title.setText(mDataList.get(position));


        return convertView;
    }
}
