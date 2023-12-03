package com.example.caroproject.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.caroproject.Data.PlayerInfo;
import com.example.caroproject.R;
public class FriendListAdapter extends ArrayAdapter<PlayerInfo> {
    private Context context;
    private PlayerInfo[] items;
    public FriendListAdapter(Context context,int layout,PlayerInfo[] items){
        super(context, R.layout.custom_friendlist_view,items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(R.layout.custom_friendlist_view, null);
        TextView name = (TextView) row.findViewById(R.id.txtViewName);
        ImageView avatar = (ImageView) row.findViewById(R.id.imgViewAvatar);
        TextView status = (TextView) row.findViewById(R.id.txtViewStatus);
        Button btnRemove = (Button) row.findViewById(R.id.btnRemove);
        name.setText(items[position].getUsername());
        avatar.setImageResource(items[position].getAvatar());
//        if (items[position].getStatus())
//            status.setText("Online");
//        else status.setText("Offline");
        return (row);
    }
}
