package com.example.finalproject.Models;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.finalproject.Fragments.FragmentShowRecipe;
import com.example.finalproject.R;
import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.MyViewHolder> {
    private ArrayList<RecipeDataModel> dataSet;
    private Context context;
    private View fragmentView;

    public RecipeAdapter(ArrayList<RecipeDataModel> dataSet, Context context, View fragmentView) {
        this.dataSet = dataSet;
        this.context = context;
        this.fragmentView = fragmentView;
    }
    public RecipeAdapter(ArrayList<RecipeDataModel> favoriteRecipesList, Context context, View view, int action_fragmentFavorites_to_fragmentShowRecipe) {
        this.context = context;
        this.dataSet = favoriteRecipesList;
        this.fragmentView = null;
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewRecipeName, textViewRecipeHours, textViewRecipeMinutes, textViewRecipeDifficulty, textViewRecipeCategory;
        ImageView imageViewRecipe, imageViewDifficulty, imageViewCategory, imageViewTimer;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewRecipeName = itemView.findViewById(R.id.textViewRecipeName);
            textViewRecipeCategory = itemView.findViewById(R.id.textViewRecipeCategory);
            textViewRecipeDifficulty = itemView.findViewById(R.id.textViewRecipeDifficulty);
            textViewRecipeHours = itemView.findViewById(R.id.textViewHoursTime);
            textViewRecipeMinutes = itemView.findViewById(R.id.textViewMinutesTime);
            imageViewRecipe = itemView.findViewById(R.id.imageView);
            imageViewDifficulty = itemView.findViewById(R.id.imageViewDifficulty);
            imageViewCategory = itemView.findViewById(R.id.imageViewCategory);
            imageViewTimer = itemView.findViewById(R.id.imageViewClock);
        }
    }

    @NonNull
    @Override
    public RecipeAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardrow, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeAdapter.MyViewHolder holder, int position) {
        RecipeDataModel currentRecipe = dataSet.get(position);

        holder.itemView.setBackgroundColor(Color.TRANSPARENT);

        holder.textViewRecipeName.setText(currentRecipe.getName());
        holder.textViewRecipeCategory.setText(currentRecipe.getCategory());
        holder.textViewRecipeDifficulty.setText(currentRecipe.getDifficulty());

        String prepHours = currentRecipe.getPrepHours();
        String prepMinutes = currentRecipe.getPrepMinutes();

        holder.textViewRecipeHours.setVisibility(prepHours.equals("0h") ? View.GONE : View.VISIBLE);
        holder.textViewRecipeHours.setText(prepHours);

        holder.textViewRecipeMinutes.setVisibility(prepMinutes.equals("0m") ? View.GONE : View.VISIBLE);
        holder.textViewRecipeMinutes.setText(prepMinutes);

        if (prepHours.equals("0h") && prepMinutes.equals("0m")) {
            holder.imageViewTimer.setVisibility(View.GONE);
        } else {
            holder.imageViewTimer.setVisibility(View.VISIBLE);
        }
        switch (currentRecipe.getDifficulty()) {
            case "Easy":
                holder.imageViewDifficulty.setImageResource(R.drawable.easy);
                break;
            case "Medium":
                holder.imageViewDifficulty.setImageResource(R.drawable.medium);
                break;
            case "Hard":
                holder.imageViewDifficulty.setImageResource(R.drawable.hard);
                break;
            default:
                holder.imageViewDifficulty.setVisibility(fragmentView.GONE);
                break;
        }

        switch (currentRecipe.getCategory()) {
            case "Side Dishes":
                holder.imageViewCategory.setImageResource(R.drawable.sidedishes);
                break;
            case "Main Course":
                holder.imageViewCategory.setImageResource(R.drawable.maincourse);
                break;
            case "Desserts":
                holder.imageViewCategory.setImageResource(R.drawable.dessert);
                break;
            case "Side":
                holder.imageViewCategory.setImageResource(R.drawable.sidedishes);
                break;
            case "Dessert":
                holder.imageViewCategory.setImageResource(R.drawable.dessert);
                break;
            case "Seafood":
                holder.imageViewCategory.setImageResource(R.drawable.fish);
                break;
            case "Vegetarian":
                holder.imageViewCategory.setImageResource(R.drawable.salad);
                break;
            case "Beef":
                holder.imageViewCategory.setImageResource(R.drawable.beef);
                break;
            case "Chicken":
                holder.imageViewCategory.setImageResource(R.drawable.chicken);
                break;
            default:
                holder.imageViewCategory.setImageResource(R.drawable.maincourse);
                break;
        }

        Glide.with(holder.itemView.getContext())
                .load(currentRecipe.getImageUri())
                .placeholder(R.drawable.icon)
                .error(R.drawable.icon)
                .into(holder.imageViewRecipe);

        holder.itemView.setOnClickListener(v -> {
            if (currentRecipe.getId() != null) {
                Bundle bundle = new Bundle();
                bundle.putString("recipe_id", currentRecipe.getId());

                View navView = (fragmentView != null) ? fragmentView : holder.itemView;
                NavController navController = Navigation.findNavController(navView);
                int currentDestinationId = navController.getCurrentDestination().getId();

                int actionId;
                if (currentDestinationId == R.id.fragmentMain) {
                    actionId = R.id.action_fragmentMain_to_fragmentShowRecipe;
                } else if (currentDestinationId == R.id.fragmentFavorites) {
                    actionId = R.id.action_fragmentFavorites_to_fragmentShowRecipe;
                } else if (currentDestinationId == R.id.fragmentExtrnalRecipes) {
                    bundle.putString("recipe_id", currentRecipe.getId());
                    bundle.putString("name", currentRecipe.getName());
                    bundle.putString("category", currentRecipe.getCategory());
                    bundle.putString("ingredients", currentRecipe.getIngredients());
                    bundle.putString("instructions", currentRecipe.getInstructions());
                    bundle.putString("difficulty", currentRecipe.getDifficulty());
                    bundle.putString("prep_hours", currentRecipe.getPrepHours());
                    bundle.putString("prep_minutes", currentRecipe.getPrepMinutes());
                    bundle.putString("image_url", currentRecipe.getImageUri());

                    actionId = R.id.action_fragmentExtrnalRecipes_to_fragmentShowRecipe;
                } else {
                    Log.e("RecipeAdapter", "Unknown navigation source: " + currentDestinationId);
                    return;
                }

                navController.navigate(actionId, bundle);
            } else {
                Log.e("RecipeAdapter", "Recipe ID is null");
            }
        });
    }
    public void updateList(List<RecipeDataModel> newList) {
        this.dataSet = new ArrayList<>(newList);
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
