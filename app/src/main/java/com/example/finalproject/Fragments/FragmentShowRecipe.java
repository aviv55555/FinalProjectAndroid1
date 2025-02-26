package com.example.finalproject.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.finalproject.Models.RecipeDataModel;
import com.example.finalproject.R;
import com.example.finalproject.Services.RecipesServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class FragmentShowRecipe extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private TextView textRecipeName, valueIngredients, textIngredients, textInstructions, valueInstructions, valuePrepTime;
    private ImageView imageRecipe, imageCategory, imageDifficulty;
    private String recipeId;
    private OnItemDeletedListener listener;

    public FragmentShowRecipe() {
    }

    public static FragmentShowRecipe newInstance(String param1, String param2) {
        FragmentShowRecipe fragment = new FragmentShowRecipe();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_recipe, container, false);

        Button buttonCloseMenu = view.findViewById(R.id.buttonCloseMenu);
        LinearLayout bottomMenu = view.findViewById(R.id.bottomMenu);
        ImageButton buttonToggleMenu = view.findViewById(R.id.buttonToggleMenu);

        buttonToggleMenu.setVisibility(View.VISIBLE);
        bottomMenu.setVisibility(View.GONE);

        buttonToggleMenu.setOnClickListener(v -> {
            if (bottomMenu.getVisibility() == View.GONE) {
                bottomMenu.setVisibility(View.VISIBLE);

                Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
                bottomMenu.startAnimation(slideUp);

                buttonToggleMenu.setVisibility(View.GONE);
            } else {
                Animation slideDown = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
                bottomMenu.startAnimation(slideDown);

                slideDown.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        bottomMenu.setVisibility(View.GONE);
                        buttonToggleMenu.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
            }
        });
        buttonCloseMenu.setOnClickListener(v -> {
            Animation slideDown = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
            bottomMenu.startAnimation(slideDown);

            slideDown.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    bottomMenu.setVisibility(View.GONE);
                    buttonToggleMenu.setVisibility(View.VISIBLE);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        });

        if (getArguments() != null) {
            recipeId = getArguments().getString("recipe_id");
        }

        textRecipeName = view.findViewById(R.id.textRecipeName);
        textIngredients = view.findViewById(R.id.textIngredients);
        valueIngredients = view.findViewById(R.id.valueIngredients);
        textInstructions = view.findViewById(R.id.textInstructions);
        valueInstructions = view.findViewById(R.id.valueInstructions);
        valuePrepTime = view.findViewById(R.id.valuePrepTime);
        imageRecipe = view.findViewById(R.id.imageRecipe);
        imageCategory = view.findViewById(R.id.imageViewShowCategory);
        imageDifficulty = view.findViewById(R.id.imageViewShowDifficulty);


        if (recipeId != null) {
            DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);
            recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        RecipeDataModel recipe = snapshot.getValue(RecipeDataModel.class);
                        if (recipe != null) {
                            fillRecipeDetails(recipe);
                        }
                    } else {
                        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("favorites").child(recipeId);
                        favoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    RecipeDataModel recipe = snapshot.getValue(RecipeDataModel.class);
                                    if (recipe != null) {
                                        fillRecipeDetails(recipe);
                                    }
                                } else {
                                    loadRecipeFromServices(recipeId);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getContext(), "Error checking favorites", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error fetching recipe from Firebase", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            loadRecipeFromFavorites(recipeId);
        }

        Button deleteRecipe = view.findViewById(R.id.buttonDelete);
        deleteRecipe.setOnClickListener(v -> {
            if (recipeId != null) {
                removeRecipeFromFirebase(recipeId);
                if (listener != null) {
                    listener.onItemDeleted(recipeId);
                }
                Toast.makeText(getContext(), "Recipe deleted!", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).navigate(R.id.action_fragmentShowRecipe_to_fragmentMain);
            }
        });

        Button editRecipe = view.findViewById(R.id.buttonEdit);
        editRecipe.setOnClickListener(v -> {
            if (recipeId != null) {
                Bundle bundle = new Bundle();
                bundle.putString("recipe_id", recipeId);
                Navigation.findNavController(view).navigate(R.id.action_fragmentShowRecipe_to_fragmentEditRecipe, bundle);
            } else {
                Log.e("EditRecipe", "recipeId is null");
            }
        });

        Button timer = view.findViewById(R.id.buttonTimer);
        timer.setOnClickListener(v -> {
            if (recipeId != null) {
                Bundle bundle = new Bundle();
                bundle.putString("recipe_id", recipeId);
                Navigation.findNavController(view).navigate(R.id.action_fragmentShowRecipe_to_fragmentTimer, bundle);
            } else {
                Log.e("Timer", "recipeId is null");
            }
        });

        Button favoriteRecipe = view.findViewById(R.id.buttonFavorite);
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("favorites");

        favoritesRef.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    favoriteRecipe.setBackgroundResource(R.drawable.redheart);
                } else {
                    favoriteRecipe.setBackgroundResource(R.drawable.whiteheart);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error checking favorites", Toast.LENGTH_SHORT).show();
            }
        });

        favoriteRecipe.setOnClickListener(v -> {
            if (recipeId != null) {
                favoritesRef.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            favoritesRef.child(recipeId).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                                        favoriteRecipe.setBackgroundResource(R.drawable.whiteheart);
                                        Navigation.findNavController(view).navigate(R.id.action_fragmentShowRecipe_to_fragmentMain);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            RecipesServices recipesServices = new RecipesServices();
                            ArrayList<RecipeDataModel> recipes = recipesServices.getAllRecipes();

                            RecipeDataModel currentRecipe = recipesServices.getRecipeById(recipeId);
                            if (currentRecipe != null) {
                                favoritesRef.child(recipeId).setValue(currentRecipe)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                                            favoriteRecipe.setBackgroundResource(R.drawable.redheart);
                                            Navigation.findNavController(view).navigate(R.id.action_fragmentShowRecipe_to_fragmentMain);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Failed to add to favorites", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference("recipes");
                                recipesRef.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            RecipeDataModel currentRecipe = snapshot.getValue(RecipeDataModel.class);
                                            favoritesRef.child(recipeId).setValue(currentRecipe)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(getContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                                                        favoriteRecipe.setBackgroundResource(R.drawable.redheart);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(getContext(), "Failed to add to favorites", Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(getContext(), "Error fetching recipes", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error checking favorites", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return view;
    }
    private void loadRecipeFromServices(String recipeId) {
        RecipesServices services = new RecipesServices();
        ArrayList<RecipeDataModel> arrRecipes = services.getAllRecipes();

        for (RecipeDataModel recipe : arrRecipes) {
            if (recipe.getId().equals(recipeId)) {
                fillRecipeDetails(recipe);
                return;
            }
        }
    }
    private void loadRecipeFromFavorites(String recipeId) {
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("favorites").child(recipeId);

        favoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    RecipeDataModel recipe = snapshot.getValue(RecipeDataModel.class);
                    if (recipe != null) {
                        fillRecipeDetails(recipe);
                    }
                } else {
                    Toast.makeText(getContext(), "Recipe not found in favorites", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error checking favorites", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fillRecipeDetails(RecipeDataModel recipe) {
        if(recipe.getPrepMinutes().equals("0m") && recipe.getPrepHours().equals("0h"))
        {
            valuePrepTime.setVisibility(View.GONE);
        }
        else
        {
            valuePrepTime.setVisibility(View.VISIBLE);
        }
        if(recipe.getDifficulty().equals("")) {
            imageDifficulty.setVisibility(View.GONE);
        }
        else {
            imageDifficulty.setVisibility(View.VISIBLE);
        }
        switch (recipe.getDifficulty()) {
            case "Easy":
                imageDifficulty.setImageResource(R.drawable.easy);
                break;
            case "Medium":
                imageDifficulty.setImageResource(R.drawable.medium);
                break;
            case "Hard":
                imageDifficulty.setImageResource(R.drawable.hard);
                break;
            default:
                imageDifficulty.setVisibility(View.GONE);
                break;
        }

        switch (recipe.getCategory()) {
            case "Side Dishes":
                imageCategory.setImageResource(R.drawable.sidedishes);
                break;
            case "Main Course":
                imageCategory.setImageResource(R.drawable.maincourse);
                break;
            case "Desserts":
                imageCategory.setImageResource(R.drawable.dessert);
                break;
            case "Side":
                imageCategory.setImageResource(R.drawable.sidedishes);
                break;
            case "Dessert":
                imageCategory.setImageResource(R.drawable.dessert);
                break;
            case "Seafood":
                imageCategory.setImageResource(R.drawable.fish);
                break;
            case "Vegetarian":
                imageCategory.setImageResource(R.drawable.salad);
                break;
            case "Beef":
                imageCategory.setImageResource(R.drawable.beef);
                break;
            case "Chicken":
                imageCategory.setImageResource(R.drawable.chicken);
                break;
            default:
                imageCategory.setImageResource(R.drawable.maincourse);
                break;
        }
        textRecipeName.setText(recipe.getName());
        valueIngredients.setText(recipe.getIngredients());
        valueInstructions.setText(recipe.getInstructions());
        if (recipe.getPrepHours().equals("0h")) {
            valuePrepTime.setText(recipe.getPrepMinutes());
        } else if (recipe.getPrepMinutes().equals("0m")) {
            valuePrepTime.setText(recipe.getPrepHours());
        } else {
            valuePrepTime.setText(recipe.getPrepHours() + " " + recipe.getPrepMinutes());
        }
        if(getContext()!=null) {
            Glide.with(getContext())
                    .load(recipe.getImageUri())
                    .placeholder(R.drawable.icon)
                    .error(R.drawable.icon)
                    .into(imageRecipe);
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("recipes").child(recipe.getId());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(getView()!=null) {
                    Button deleteRecipe = getView().findViewById(R.id.buttonDelete);
                    Button editRecipe = getView().findViewById(R.id.buttonEdit);

                    if (snapshot.exists()) {
                        deleteRecipe.setVisibility(View.VISIBLE);
                        editRecipe.setVisibility(View.VISIBLE);
                    } else {
                        deleteRecipe.setVisibility(View.GONE);
                        editRecipe.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error checking if recipe exists in Firebase", error.toException());
            }
        });

        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("favorites").child(recipe.getId());
        favoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if(getView()!=null) {
                        Button deleteRecipe = getView().findViewById(R.id.buttonDelete);
                        Button editRecipe = getView().findViewById(R.id.buttonEdit);
                        deleteRecipe.setVisibility(View.VISIBLE);
                        editRecipe.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error checking if recipe exists in favorites", error.toException());
            }
        });
    }

    private void removeRecipeFromFirebase(String recipeId) {
        DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);
        DatabaseReference favoriteRef = FirebaseDatabase.getInstance().getReference("favorites").child(recipeId);

        recipeRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Firebase", "Recipe removed from recipes successfully.");
            } else {
                Log.e("FirebaseError", "Error removing recipe from recipes: " + task.getException());
            }

            favoriteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        favoriteRef.removeValue().addOnCompleteListener(favTask -> {
                            if (favTask.isSuccessful()) {
                                Log.d("Firebase", "Recipe removed from favorites successfully.");
                            } else {
                                Log.e("FirebaseError", "Error removing recipe from favorites: " + favTask.getException());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseError", "Error checking favorites: " + error.getMessage());
                }
            });
        });
    }
    public interface OnItemDeletedListener {
        void onItemDeleted(String recipeId);
    }
}