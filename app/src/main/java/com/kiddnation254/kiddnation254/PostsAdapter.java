package com.kiddnation254.kiddnation254;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.MyViewHolder> {

    private Context mContext;
    private List<Post> postList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, body, author, postedAt;
        public ImageView thumbnail;
        public Button readMore;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.postTitle);
            body = (TextView) view.findViewById(R.id.postBody);
            thumbnail = (ImageView) view.findViewById(R.id.videoImage);
            author = (TextView) view.findViewById(R.id.author);
            postedAt = (TextView) view.findViewById(R.id.postedAt);
            readMore = (Button) view.findViewById(R.id.readMore);
        }
    }


    public PostsAdapter(Context mContext, List<Post> postList) {
        this.mContext = mContext;
        this.postList = postList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Post post = postList.get(position);
        holder.title.setText(post.getTitle());
        holder.body.setText(post.getBody());
        holder.author.setText(post.getAuthor());
        holder.postedAt.setText(post.getTime());
        holder.readMore.setTag(post.getPostId());

        holder.readMore.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, SinglePostActivity.class);
                        intent.putExtra("id", post.getPostId());
                        mContext.startActivity(intent);

                    }
                }
        );

        // loading album cover using Glide library
        Picasso.with(mContext)
                .load(post.getThumbnail())
                .placeholder(R.drawable.blog_placeholder)
                .error(R.drawable.blog_placeholder)
                .into(holder.thumbnail);

    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

}