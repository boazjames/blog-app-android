package com.kiddnation254.kiddnation254;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
//        holder.body.setText(post.getBody());
        if (Build.VERSION.SDK_INT >= 26) {
            holder.body.setText(Html.fromHtml(limitStringLength(post.getBody(), 250), Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.body.setText(Html.fromHtml(post.getBody()));
        }
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

        Picasso.with(mContext)
                .load(post.getThumbnail())
                .placeholder(R.drawable.blog_placeholder)
                .error(R.drawable.blog_placeholder)
                .fit()
                .into(holder.thumbnail);

    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

    private String limitStringLength(String value, int length) {
        /*String[] regx = {"<p>", "</p>", "<b>", "</b>", "<h4>", "</h4>"};
        for (int i = 0; i < regx.length; i++) {
            String regX = regx[i];
            char[] ca = regX.toCharArray();
            for (char c : ca) {
                value = value.replace("" + c, "");
            }
        }*/

        StringBuilder buf = new StringBuilder(value);
        if (buf.length() > length) {
            buf.setLength(length);
            buf.append("...");
        }

        return buf.toString();
    }

}