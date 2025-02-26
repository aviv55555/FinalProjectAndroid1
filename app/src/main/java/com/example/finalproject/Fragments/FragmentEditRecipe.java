package com.example.finalproject.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.finalproject.Models.RecipeDataModel;
import com.example.finalproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;
public class FragmentEditRecipe extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int PICK_IMAGE_REQUEST = 1;

    private String mParam1;
    private String mParam2;
    private String recipeId;
    private EditText editRecipeName, editIngredients, editInstructions;
    private Spinner spinnerCategory, spinnerDifficulty, spinnerPrepTimeHours, spinnerPrepTimeMinutes;
    private ImageView buttonSelectImage;
    private Uri imageUri;
    private String imageUrl;
    private RecipeDataModel recipe;
    private Boolean flag = false;

    public FragmentEditRecipe() {
    }

    public static FragmentEditRecipe newInstance(String param1, String param2) {
        FragmentEditRecipe fragment = new FragmentEditRecipe();
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
        if (getArguments() != null) {
            recipeId = getArguments().getString("recipe_id");
            Log.d("FragmentEditRecipe", "Received Recipe ID: " + recipeId);
        } else {
            Log.e("FragmentEditRecipe", "No recipe_id received");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_recipe, container, false);

        editRecipeName = view.findViewById(R.id.editRecipeName);
        editIngredients = view.findViewById(R.id.editIngredients);
        editInstructions = view.findViewById(R.id.editInstructions);
        spinnerCategory = view.findViewById(R.id.editSpinnerCategory);
        spinnerDifficulty = view.findViewById(R.id.editSpinnerDifficulty);
        spinnerPrepTimeHours = view.findViewById(R.id.editSpinnerHours);
        spinnerPrepTimeMinutes = view.findViewById(R.id.editSpinnerMinutes);
        buttonSelectImage = view.findViewById(R.id.buttonSelectImage);

        if (recipeId != null) {
            getRecipeDataFromFavoritesOrRecipes(recipeId);
        }

        buttonSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        Button buttonSave = view.findViewById(R.id.buttonSaveRecipe);
        buttonSave.setOnClickListener(v -> {
            if (imageUri != null) {
                imageUrl = imageUri.toString();
                updateRecipeImageInFirebase(view);
            } else {
                updateRecipeInFirebase(view);
            }
        });

        return view;
    }
    private void getRecipeDataFromFavoritesOrRecipes(String recipeId) {
        DatabaseReference favoritesReference = FirebaseDatabase.getInstance().getReference("favorites").child(recipeId);
        favoritesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipe = snapshot.getValue(RecipeDataModel.class);
                if (recipe != null) {
                    fillRecipeFields(recipe);
                } else {
                    DatabaseReference recipesReference = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);
                    recipesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            recipe = snapshot.getValue(RecipeDataModel.class);
                            if (recipe != null) {
                                fillRecipeFields(recipe);
                            } else {
                                Toast.makeText(getContext(), "Recipe not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Failed to load recipe data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load favorite recipe data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillRecipeFields(RecipeDataModel recipe) {
        editRecipeName.setText(recipe.getName());
        spinnerCategory.setSelection(getCategoryPosition(recipe.getCategory()));
        editIngredients.setText(recipe.getIngredients());
        editInstructions.setText(recipe.getInstructions());
        spinnerDifficulty.setSelection(getDifficultyPosition(recipe.getDifficulty()));
        spinnerPrepTimeHours.setSelection(getHoursPosition(recipe.getPrepHours()));
        spinnerPrepTimeMinutes.setSelection(getMinutesPosition(recipe.getPrepMinutes()));

        if (recipe.getImageUri() != null && !recipe.getImageUri().isEmpty()) {
            Glide.with(getContext()).load(recipe.getImageUri()).into(buttonSelectImage);
            imageUrl = recipe.getImageUri();
        }
    }

    private int getCategoryPosition(String category) {
        String[] categories = getResources().getStringArray(R.array.recipe_categories);
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(category)) {
                return i;
            }
        }
        return 0;
    }

    private int getDifficultyPosition(String difficulty) {
        String[] difficulties = getResources().getStringArray(R.array.difficulty_levels);
        for (int i = 0; i < difficulties.length; i++) {
            if (difficulties[i].equals(difficulty)) {
                return i;
            }
        }
        return 0;
    }

    private int getHoursPosition(String hour) {
        String[] hours = getResources().getStringArray(R.array.hours_array);
        for (int i = 0; i < hours.length; i++) {
            if (hours[i].equals(hour)) {
                return i;
            }
        }
        return 0;
    }

    private int getMinutesPosition(String minute) {
        String[] minutes = getResources().getStringArray(R.array.minutes_array);
        for (int i = 0; i < minutes.length; i++) {
            if (minutes[i].equals(minute)) {
                return i;
            }
        }
        return 0;
    }

    private void updateRecipeImageInFirebase(View view) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (imageUri != null) {
                        imageUrl = imageUri.toString();
                    }

                    databaseReference.child("imageUri").setValue(imageUrl)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Image updated successfully!", Toast.LENGTH_SHORT).show();
                                updateRecipeInFirebase(view);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to update image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("favorites").child(recipeId);
                    databaseReference.child("imageUri").setValue(imageUrl)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Image updated successfully!", Toast.LENGTH_SHORT).show();
                                updateRecipeInFirebase(view);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to update image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateRecipeInFirebase(View view) {
        String updatedName = editRecipeName.getText().toString().trim();
        String updatedCategory = spinnerCategory.getSelectedItem().toString();
        String updatedIngredients = editIngredients.getText().toString().trim();
        String updatedInstructions = editInstructions.getText().toString().trim();
        String updatedDifficulty = spinnerDifficulty.getSelectedItem().toString();
        String updatedHours = spinnerPrepTimeHours.getSelectedItem().toString();
        String updatedMinutes = spinnerPrepTimeMinutes.getSelectedItem().toString();

        recipe.setName(updatedName);
        recipe.setCategory(updatedCategory);
        recipe.setIngredients(updatedIngredients);
        recipe.setInstructions(updatedInstructions);
        recipe.setDifficulty(updatedDifficulty);
        recipe.setPrepHours(updatedHours);
        recipe.setPrepMinutes(updatedMinutes);
        recipe.setImageUri(imageUrl);

        DatabaseReference recipesReference = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);
        DatabaseReference favoritesReference = FirebaseDatabase.getInstance().getReference("favorites").child(recipeId);

        recipesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean existsInRecipes = snapshot.exists();

                if (existsInRecipes) {
                    recipesReference.setValue(recipe)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("UpdateRecipe", "Recipe updated in recipes table successfully.");
                                } else {
                                    Log.e("UpdateRecipe", "Failed to update recipe in recipes table.");
                                }

                                updateRecipeInFavorites(view);
                            });
                } else {
                    updateRecipeInFavorites(view);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UpdateRecipe", "Failed to check if recipe exists in recipes table.");
                Toast.makeText(getContext(), "Failed to update recipe", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecipeInFavorites(View view) {
        DatabaseReference favoritesReference = FirebaseDatabase.getInstance().getReference("favorites").child(recipeId);
        favoritesReference.setValue(recipe)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("UpdateRecipe", "Recipe updated in favorites table successfully.");
                        Toast.makeText(getContext(), "The recipe was updated successfully!", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(view).navigate(R.id.action_fragmentEditRecipe_to_fragmentMain);
                    } else {
                        Log.e("UpdateRecipe", "Failed to update recipe in favorites table.");
                        Toast.makeText(getContext(), "Failed to update recipe in favorites", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(getContext()).load(imageUri).into(buttonSelectImage);
        }
    }
}