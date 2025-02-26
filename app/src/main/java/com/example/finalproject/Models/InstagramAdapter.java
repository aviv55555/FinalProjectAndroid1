package com.example.finalproject.Models;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.Models.InstagramProfile;
import com.example.finalproject.R;

import java.util.ArrayList;
import java.util.List;

public class InstagramAdapter extends RecyclerView.Adapter<InstagramAdapter.ViewHolder> {
    private Context context;
    private List<InstagramProfile> profileList;

    public InstagramAdapter(List<InstagramProfile> profileList, Context context) {
        this.context = context;
        this.profileList = profileList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.famouschefs, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InstagramProfile profile = profileList.get(position);

        holder.imageViewProfile.setImageResource(profile.getImageResId());

        holder.imageViewProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(profile.getInstagramUrl()));
            intent.setPackage("com.instagram.android");
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(profile.getInstagramUrl())));
            }
        });
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProfile;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfile = itemView.findViewById(R.id.imageViewChef);
        }
    }
}