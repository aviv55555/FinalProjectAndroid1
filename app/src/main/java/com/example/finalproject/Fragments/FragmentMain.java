package com.example.finalproject.Fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.finalproject.Models.InstagramAdapter;
import com.example.finalproject.Models.InstagramProfile;
import com.example.finalproject.Models.RecipeAdapter;
import com.example.finalproject.Models.RecipeDataModel;
import com.example.finalproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FragmentMain extends Fragment {
    private DatabaseReference databaseReference;
    private ArrayList<RecipeDataModel> recipesList, filterList;
    private RecyclerView recyclerViewRecipes;
    private RecipeAdapter adapterRecipes;
    private EditText searchBox;
    private Spinner categorySpinner;
    private RadioGroup filterGroup;
    private boolean filterByCategory = false;
    private ImageButton searchButton;
    private String selectedCategory = "";

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        ImageButton buttonFav = view.findViewById(R.id.buttonFav);
        buttonFav.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_fragmentMain_to_fragmentFavorites));

        ImageButton buttonPlus = view.findViewById(R.id.buttonPlus);
        buttonPlus.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_fragmentMain_to_fragmentAddRecipes));

        ImageButton buttonExternal = view.findViewById(R.id.buttonGlobal);
        buttonExternal.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_fragmentMain_to_fragmentExtrnalRecipes));

        searchBox = view.findViewById(R.id.searchBox);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        filterGroup = view.findViewById(R.id.filterGroup);
        recyclerViewRecipes = view.findViewById(R.id.resViewRecipes);
        searchButton = view.findViewById(R.id.buttonSearch);

        recyclerViewRecipes.setBackgroundColor(Color.TRANSPARENT);

        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        ArrayList<InstagramProfile> instagramProfiles = new ArrayList<>();
        instagramProfiles.add(new InstagramProfile(R.drawable.segev, "https://www.instagram.com/segevmoshe"));
        instagramProfiles.add(new InstagramProfile(R.drawable.karin, "https://www.instagram.com/carine.goren"));
        instagramProfiles.add(new InstagramProfile(R.drawable.eyal, "https://www.instagram.com/eyaltomato"));
        instagramProfiles.add(new InstagramProfile(R.drawable.haim, "https://www.instagram.com/chef_haim_cohen"));
        instagramProfiles.add(new InstagramProfile(R.drawable.dudu, "https://www.instagram.com/duduoutmezgine"));
        instagramProfiles.add(new InstagramProfile(R.drawable.or, "https://www.instagram.com/or_shpitz"));
        instagramProfiles.add(new InstagramProfile(R.drawable.yossi, "https://www.instagram.com/yossi_shitrit77"));

        RecyclerView recyclerViewInstagram = view.findViewById(R.id.resViewFamous);
        recyclerViewInstagram.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        InstagramAdapter instagramAdapter = new InstagramAdapter(instagramProfiles, getContext());
        recyclerViewInstagram.setAdapter(instagramAdapter);

        recipesList = new ArrayList<>();
        filterList = new ArrayList<>();

        adapterRecipes = new RecipeAdapter(filterList, requireActivity(), view);
        recyclerViewRecipes.setAdapter(adapterRecipes);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.categories_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
                if (filterByCategory) {
                    filterList(selectedCategory);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        filterGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.filterCategory) {
                filterByCategory = true;
                categorySpinner.setVisibility(View.VISIBLE);
                searchBox.setVisibility(View.GONE);
                searchButton.setVisibility(View.GONE);
                filterList(selectedCategory);
            } else {
                filterByCategory = false;
                categorySpinner.setVisibility(View.GONE);
                searchBox.setVisibility(View.VISIBLE);
                searchButton.setVisibility(View.VISIBLE);
                filterList(searchBox.getText().toString());
            }
        });

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!filterByCategory) {
                    filterList(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchButton.setOnClickListener(v -> filterList(searchBox.getText().toString()));

        databaseReference = FirebaseDatabase.getInstance().getReference("recipes");
        fetchProductsFromFirebase();

        return view;
    }
    private void filterList(String query) {
        filterList.clear();
        if (query.isEmpty()) {
            filterList.addAll(recipesList);
        } else {
            String q = query.toLowerCase();
            for (RecipeDataModel recipe : recipesList) {
                if ((filterByCategory && recipe.getCategory().toLowerCase().contains(q)) ||
                        (!filterByCategory && (recipe.getName().toLowerCase().contains(q)
                                || recipe.getIngredients().toLowerCase().contains(q)
                                || recipe.getDifficulty().toLowerCase().contains(q)))) {
                    filterList.add(recipe);
                }
            }
        }
        adapterRecipes.notifyDataSetChanged();
    }
    private void fetchProductsFromFirebase() {
        if (databaseReference == null) {
            Log.e("FragmentMain", "DatabaseReference is null");
            return;
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipesList.clear();

                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    RecipeDataModel recipe = productSnapshot.getValue(RecipeDataModel.class);
                    if (recipe != null) {
                        recipesList.add(recipe);
                    }
                }
                filterList("");
                adapterRecipes.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", "Error: " + error.getMessage());
            }
        });
    }
}
