package com.example.finalproject.Services;

import android.os.StrictMode;
import android.util.Log;

import com.example.finalproject.Models.RecipeDataModel;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class RecipesServices {
    private ArrayList<RecipeDataModel> arrRecipes;
    public RecipesServices() {
        arrRecipes = new ArrayList<>();
    }
    public ArrayList<RecipeDataModel> getAllRecipes() {
        URL url;
        String sURL = "https://www.themealdb.com/api/json/v1/1/search.php?s=";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            url = new URL(sURL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        try {
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();
            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
            JsonObject obj = root.getAsJsonObject();
            JsonArray mealsArray = obj.getAsJsonArray("meals");

            for (int i = 0; i < mealsArray.size(); i++) {
                JsonObject meal = mealsArray.get(i).getAsJsonObject();

                JsonElement idMeal = meal.get("idMeal");
                JsonElement strMeal = meal.get("strMeal");
                JsonElement strCategory = meal.get("strCategory");
                JsonElement strInstructions = meal.get("strInstructions");
                JsonElement strMealThumb = meal.get("strMealThumb");

                String nameS = (strMeal != null && !strMeal.isJsonNull()) ? strMeal.getAsString() : "Not Available in API";
                String categoryS = (strCategory != null && !strCategory.isJsonNull()) ? strCategory.getAsString() : "Not Available in API";
                String instructionsS = (strInstructions != null && !strInstructions.isJsonNull()) ? strInstructions.getAsString() : "Not Available in API";
                String imageThumb = (strMealThumb != null && !strMealThumb.isJsonNull()) ? strMealThumb.getAsString() : null;

                StringBuilder ingredients = new StringBuilder();
                for (int j = 1; j <= 20; j++) {
                    JsonElement measure = meal.get("strMeasure" + j);
                    JsonElement ingredient = meal.get("strIngredient" + j);

                    if (ingredient != null && !ingredient.isJsonNull() && !ingredient.getAsString().isEmpty()) {
                        String ingredientStr = ingredient.getAsString();
                        String measureStr = (measure != null && !measure.isJsonNull()) ? measure.getAsString() : "";
                        ingredients.append(measureStr).append(" ").append(ingredientStr).append("\n");
                    }
                }

                RecipeDataModel recipe = new RecipeDataModel(
                        "" ,nameS, idMeal.toString(), categoryS, ingredients.toString(), instructionsS, "0h", "0m", imageThumb);

                arrRecipes.add(recipe);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return arrRecipes;
    }
    public RecipeDataModel getRecipeById(String id) {
        for (int i = 0; i < arrRecipes.size(); i++) {
            if (arrRecipes.get(i).getId().equals(id)) {
                return arrRecipes.get(i);
            }
        }
        return null;
    }
}