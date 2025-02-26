package com.example.finalproject.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import com.example.finalproject.Models.RecipeAdapter;
import com.example.finalproject.Models.RecipeDataModel;
import com.example.finalproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class FragmentFavorites extends Fragment {

    private DatabaseReference databaseReference;
    private ArrayList<RecipeDataModel> favoriteRecipesList, filterList;
    private RecipeAdapter adapter;
    private RecyclerView recyclerView;
    private EditText searchBox;

    public FragmentFavorites() {
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerView = view.findViewById(R.id.resViewFavorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        favoriteRecipesList = new ArrayList<>();
        filterList = new ArrayList<>();
        adapter = new RecipeAdapter(favoriteRecipesList, getContext(), view, R.id.action_fragmentFavorites_to_fragmentShowRecipe);
        recyclerView.setAdapter(adapter);

        searchBox = view.findViewById(R.id.searchBox);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        databaseReference = FirebaseDatabase.getInstance().getReference("favorites");
        fetchFavoriteRecipes();

        return view;
    }
    private void filterList(String query) {
        ArrayList<RecipeDataModel> filtered = new ArrayList<>();
        if (query.isEmpty()) {
            filtered.addAll(favoriteRecipesList);
        } else {
            for (RecipeDataModel recipe : favoriteRecipesList) {
                if (recipe.getName().toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(recipe);
                }
            }
        }
        adapter.updateList(filtered);
    }
    private void fetchFavoriteRecipes() {
        if (databaseReference == null) {
            return;
        }
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteRecipesList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    RecipeDataModel recipe = ds.getValue(RecipeDataModel.class);
                    if (recipe != null) {
                        favoriteRecipesList.add(recipe);
                    }
                }
                adapter.updateList(new ArrayList<>(favoriteRecipesList));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load favorites", Toast.LENGTH_SHORT).show();
            }
        });
    }
}