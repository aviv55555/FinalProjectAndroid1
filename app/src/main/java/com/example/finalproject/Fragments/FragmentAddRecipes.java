package com.example.finalproject.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalproject.Activities.MainActivity;
import com.example.finalproject.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class FragmentAddRecipes extends Fragment {
    private ImageView imageRecipe;
    private Uri imageUri;
    private ActivityResultLauncher<Intent> galleryLauncher;

    public FragmentAddRecipes() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_recipes, container, false);
        container.setBackgroundColor(Color.TRANSPARENT);
        imageRecipe = view.findViewById(R.id.buttonSelectImage);
        Button buttonAddRecipe = view.findViewById(R.id.buttonAddRecipe);

        requestGalleryPermission();

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        imageRecipe.setImageURI(imageUri);
                    }
                });

        imageRecipe.setOnClickListener(v -> openGallery());

        TextInputEditText editRecipeName = view.findViewById(R.id.editRecipeName);
        TextInputEditText editIngredients = view.findViewById(R.id.editIngredients);
        TextInputEditText editInstructions = view.findViewById(R.id.editInstructions);
        Spinner spinnerDifficulty = view.findViewById(R.id.spinnerDifficulty);
        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        Spinner spinnerHours = view.findViewById(R.id.spinnerHours);
        Spinner spinnerMinutes = view.findViewById(R.id.spinnerMinutes);

        buttonAddRecipe.setOnClickListener(v -> {
            if (editRecipeName.getText().length() == 0) {
                Toast.makeText(getContext(), "Please enter recipe name!", Toast.LENGTH_LONG).show();
                return;
            }
            if (editIngredients.getText().length() == 0) {
                Toast.makeText(getContext(), "Please enter ingredients list!", Toast.LENGTH_LONG).show();
                return;
            }
            if (editInstructions.getText().length() == 0) {
                Toast.makeText(getContext(), "Please enter instructions list!", Toast.LENGTH_LONG).show();
                return;
            }

            if (imageUri == null) {
                Toast.makeText(getContext(), "Please select an image for the recipe!", Toast.LENGTH_LONG).show();
                return;
            }

            String recipeName = editRecipeName.getText().toString().trim();
            String ingredients = editIngredients.getText().toString().trim();
            String instructions = editInstructions.getText().toString().trim();
            String difficultyLevel = spinnerDifficulty.getSelectedItem().toString();
            String category = spinnerCategory.getSelectedItem().toString();
            String prepHours = spinnerHours.getSelectedItem().toString();
            String prepMinutes = spinnerMinutes.getSelectedItem().toString();

            if (prepHours.equals("0h") && prepMinutes.equals("0m")) {
                Toast.makeText(getContext(), "Please enter preparation time!", Toast.LENGTH_LONG).show();
                return;
            }

            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                mainActivity.funcAddRecipe(recipeName, ingredients, instructions, difficultyLevel, category, imageUri, prepHours, prepMinutes, view);
            }
        });

        return view;
    }
    private void requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }
}