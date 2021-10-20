package com.example.pictureplayer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private Context context;
    public Content[] contents;

    public RecyclerViewAdapter(Context context) {
        this.context = context;
        this.contents = new Content[0];
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerViewAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        View view = holder.itemView;
        Content content = contents[position];
        TextView t = view.findViewById(R.id.item_text);
        t.setText(content.name);
        ImageView i = view.findViewById(R.id.item_image);
        Glide.with(context).load(content.cover).into(i);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(context, MovieActivity.class);
                intent.putExtra("addr", content.video);
                intent.putExtra("title", content.name);
                intent.putExtra("cover", content.cover);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.contents.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ViewHolder(View view) {
            super(view);
        }
    }
}
