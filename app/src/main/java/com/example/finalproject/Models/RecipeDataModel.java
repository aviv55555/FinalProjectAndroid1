package com.example.finalproject.Models;

public class RecipeDataModel {
    private String name;
    private String difficulty;
    private String id;
    private String category;
    private String ingredients;
    private String prepHours;
    private String prepMinutes;
    private String imageUri;
    private String instructions;
    private String source;

    public RecipeDataModel() {}

    public RecipeDataModel(String difficulty, String name, String id, String category, String ingredients, String instructions,
                           String prepHours, String prepMinutes, String imageUri) {
        this.difficulty = difficulty;
        this.name = name;
        this.id = id;
        this.category = category;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.prepHours = prepHours;
        this.prepMinutes = prepMinutes;
        this.imageUri = imageUri;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getCategory() { return category; }

    public void setCategory(String category) { this.category = category; }
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public String getPrepHours() { return prepHours; }
    public void setPrepHours(String prepHours) { this.prepHours = prepHours; }
    public String getPrepMinutes() { return prepMinutes; }
    public void setPrepMinutes(String prepMinutes) { this.prepMinutes = prepMinutes; }
    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
}
