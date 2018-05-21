package com.kiddnation254.kiddnation254;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyViewHolder> {

    private Context mContext;
    private List<Comment> commentList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView username, commentBody, commentTime;
        public ImageView userImg;

        public MyViewHolder(View view) {
            super(view);
            username = (TextView) view.findViewById(R.id.username);
            commentBody = (TextView) view.findViewById(R.id.commentBody);
            userImg = (ImageView) view.findViewById(R.id.userImg);
            commentTime = (TextView) view.findViewById(R.id.commentDate);
        }
    }


    public CommentsAdapter(Context mContext, List<Comment> commentList) {
        this.mContext = mContext;
        this.commentList = commentList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.username.setText(comment.getUsername());
        holder.commentBody.setText(comment.getCommentBody());
        holder.commentTime.setText(comment.getCommentTime());

        // loading album cover using Glide library
        Picasso.with(mContext)
                .load(Constants.URL_USER_IMG + comment.getUserImg())
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(holder.userImg);

    }


    @Override
    public int getItemCount() {
        return commentList.size();
    }
}