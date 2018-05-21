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

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.MyViewHolder> {

    private Context mContext;
    private List<Video> videoList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView videoTitle;
        public ImageView videoImg;
        public Button playVideo;

        public MyViewHolder(View view) {
            super(view);
            videoTitle = (TextView) view.findViewById(R.id.videoTitle);
            videoImg = (ImageView) view.findViewById(R.id.videoImage);
            playVideo = (Button) view.findViewById(R.id.playVideo);
        }
    }


    public VideosAdapter(Context mContext, List<Video> videoList) {
        this.mContext = mContext;
        this.videoList = videoList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Video video = videoList.get(position);
        holder.videoTitle.setText(video.getVideoTitle());

        holder.playVideo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, SingleVideoActivity.class);
                        intent.putExtra("videoId", video.getVideoCode());
                        intent.putExtra("videoTitle", video.getVideoTitle());
                        mContext.startActivity(intent);

                    }
                }
        );

        // loading album cover using Picasso library
        Picasso.with(mContext)
                .load("https://img.youtube.com/vi/" + video.getVideoCode() + "/maxresdefault.jpg")
                .placeholder(R.drawable.video_placeholder)
                .error(R.drawable.video_placeholder)
                .into(holder.videoImg);

    }


    @Override
    public int getItemCount() {
        return videoList.size();
    }
}