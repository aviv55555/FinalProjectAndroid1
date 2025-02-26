package com.example.finalproject.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.finalproject.Models.RecipeAdapter;
import com.example.finalproject.Models.RecipeDataModel;
import com.example.finalproject.R;
import com.example.finalproject.Services.RecipesServices;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentExtrnalRecipes#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentExtrnalRecipes extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ArrayList<RecipeDataModel> dataSet;
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;

    public FragmentExtrnalRecipes() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentExtrnalRecipes.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentExtrnalRecipes newInstance(String param1, String param2) {
        FragmentExtrnalRecipes fragment = new FragmentExtrnalRecipes();
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
        View view = inflater.inflate(R.layout.fragment_extrnal_recipes, container, false);

        RecipesServices dataServices = new RecipesServices();
        dataSet = dataServices.getAllRecipes();

        if (dataSet == null) {
            dataSet = new ArrayList<>();
        }

        recyclerView = view.findViewById(R.id.resView);
        adapter = new RecipeAdapter(dataSet, getContext(), view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        return view;
    }
}