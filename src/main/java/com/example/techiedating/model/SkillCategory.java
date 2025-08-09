package com.example.techiedating.model;

public enum SkillCategory {
    // Technical Skills
    PROGRAMMING_LANGUAGES("Programming Languages"),
    WEB_DEVELOPMENT("Web Development"),
    MOBILE_DEVELOPMENT("Mobile Development"),
    DATABASES("Databases"),
    DEVOPS("DevOps & Cloud"),
    DATA_SCIENCE("Data Science & AI"),
    GAME_DEVELOPMENT("Game Development"),
    EMBEDDED_SYSTEMS("Embedded Systems"),
    CYBERSECURITY("Cybersecurity"),
    BLOCKCHAIN("Blockchain"),
    
    // Design & Creative
    UI_UX_DESIGN("UI/UX Design"),
    GRAPHIC_DESIGN("Graphic Design"),
    GAME_DESIGN("Game Design"),
    VIDEO_EDITING("Video Editing"),
    
    // Business & Management
    PROJECT_MANAGEMENT("Project Management"),
    PRODUCT_MANAGEMENT("Product Management"),
    BUSINESS_ANALYSIS("Business Analysis"),
    DIGITAL_MARKETING("Digital Marketing"),
    
    // Soft Skills
    LEADERSHIP("Leadership"),
    COMMUNICATION("Communication"),
    TEAMWORK("Teamwork"),
    PROBLEM_SOLVING("Problem Solving"),
    
    // Other
    LANGUAGES("Languages"),
    OTHER("Other");

    private final String displayName;

    SkillCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
