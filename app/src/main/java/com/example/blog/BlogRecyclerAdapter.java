package com.example.blog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;


import java.util.Date;
import java.util.List;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    private List<BlogPost> blogList;
    public Context context;

    public BlogRecyclerAdapter(List<BlogPost> blogList) {
        this.blogList = blogList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.blog_list_item, viewGroup, false);
        context = viewGroup.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        String descData = blogList.get(i).getDesc();
        viewHolder.setDescText(descData);

        String imageUrl = blogList.get(i).getImage_url();
        viewHolder.setBlogImage(imageUrl);

        String userId = blogList.get(i).getUser_id();
        // user data

        long millisecond = blogList.get(i).getTimestamp().getTime();
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();
        viewHolder.setTime(dateString);
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView descView;
        private View mView;
        private ImageView blogImageView;
        private TextView blogDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDescText(String text) {
            descView = mView.findViewById(R.id.blogDescription);
            descView.setText(text);
        }

        public void setBlogImage(String downloadImage) {
            blogImageView = mView.findViewById(R.id.blogImage);
            Glide.with(context).load(downloadImage).into(blogImageView);
        }

        public void setTime(String date) {
            blogDate = mView.findViewById(R.id.blogDate);
            blogDate.setText(date);
        }
    }
}
