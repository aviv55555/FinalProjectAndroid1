package com.example.finalproject.Activities;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.finalproject.Models.RecipeDataModel;
import com.example.finalproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            databaseRef = FirebaseDatabase.getInstance().getReference("recipes");
            return insets;
        });
    }

    public void funcAddRecipe(String recipeName, String ingredients, String instructions, String difficulty, String category, Uri imageUri, String prepHours, String prepMinutes, View view) {
        String recipeId = databaseRef.push().getKey();

        if (recipeId == null) {
            Toast.makeText(this, "Failed to generate recipe ID!", Toast.LENGTH_LONG).show();
            return;
        }

        RecipeDataModel recipe = new RecipeDataModel(difficulty, recipeName, recipeId, category, ingredients,instructions, prepHours, prepMinutes, imageUri.toString());

        databaseRef.child(recipeId).setValue(recipe)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseDebug", "Recipe added successfully!");
                    Toast.makeText(this, "Recipe added successfully!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigate((R.id.action_fragmentAddRecipes_to_fragmentMain));
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDebug", "Failed to add recipe!", e);
                    Toast.makeText(this, "Failed to add recipe! Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
           });
    }
}