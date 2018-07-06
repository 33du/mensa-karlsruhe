package com.oops.mensa;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.oops.mensa.database.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.MyViewHolder> {

    private Context mContext;
    private List<Review> reviewList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView text;
        public ImageView imageView;
        public TextView time;

        public MyViewHolder(View view) {
            super(view);
            text = view.findViewById(R.id.text);
            imageView = view.findViewById(R.id.image_view);
            time = view.findViewById(R.id.time);
        }
    }


    public ReviewAdapter(Context mContext, List<Review> reviewList) {
        this.mContext = mContext;
        this.reviewList = reviewList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.text.setText(review.text);
        holder.time.setText(review.pub_time);
        holder.imageView.setVisibility(View.GONE);

        if (review.image_url != null) {
            holder.imageView.layout(0, 0, 0, 0);
            Glide.with(mContext).load(Uri.parse(review.image_url)).into(holder.imageView);
            holder.imageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }
}
