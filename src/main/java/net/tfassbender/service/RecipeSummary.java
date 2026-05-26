package net.tfassbender.service;

import java.util.List;

public record RecipeSummary(String name, String title, List<String> tags) {}
