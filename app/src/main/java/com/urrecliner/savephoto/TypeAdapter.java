package com.urrecliner.savephoto;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import static com.urrecliner.savephoto.Vars.mActivity;
import static com.urrecliner.savephoto.Vars.mContext;
import static com.urrecliner.savephoto.Vars.placeInfos;
import static com.urrecliner.savephoto.Vars.placeType;
import static com.urrecliner.savephoto.Vars.typeAdapter;
import static com.urrecliner.savephoto.Vars.typeIcons;
import static com.urrecliner.savephoto.Vars.typeNames;
import static com.urrecliner.savephoto.Vars.typeNumber;
import static com.urrecliner.savephoto.Vars.utils;

public class TypeAdapter extends RecyclerView.Adapter<TypeAdapter.TypeHolder>  {

    private  ArrayList<TypeInfo> mData = null;

    public TypeAdapter(ArrayList<TypeInfo> typeInfos) {
        mData = typeInfos;
    }

    static class TypeHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        ImageView ivIcon;
        View viewLine;

        TypeHolder(View view) {
            super(view);
            this.viewLine = itemView.findViewById(R.id.type_layout);
            this.tvName = itemView.findViewById(R.id.typeName);
            this.ivIcon = itemView.findViewById(R.id.typeIcon);
            this.viewLine.setOnClickListener(view1 -> {
                typeNumber = getAdapterPosition();
                placeType = typeNames[typeNumber];
                typeAdapter.notifyDataSetChanged();
                ImageView iv = mActivity.findViewById(R.id.btnPlace);
                iv.setImageBitmap(utils.maskedIcon(typeIcons[typeNumber]));
            });
        }
    }

    @NonNull
    @Override
    public TypeHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.type_item, viewGroup, false);
        return new TypeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TypeHolder viewHolder, int position) {

        viewHolder.tvName.setText(typeNames[position]);
        viewHolder.ivIcon.setImageResource(typeIcons[position]);
        if (typeNumber == position) {
            viewHolder.tvName.setBackgroundColor(Color.LTGRAY);
        }
        else {
            viewHolder.tvName.setBackgroundColor(0x00000000);
        }
        viewHolder.ivIcon.setTag(""+position);
    }

    @Override
    public int getItemCount() {
        return (typeNames.length);
    }

}
