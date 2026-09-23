package net.tfassbender.service;

public class RecipeNotFoundException extends RuntimeException {

    public RecipeNotFoundException(String name) {
        super("Recipe not found: " + name);
    }
}
