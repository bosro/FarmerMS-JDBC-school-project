package com.example.jdbc_assignment.utils;

import com.example.jdbc_assignment.models.User;

public class SessionManager {
    private static User currentUser;

    private SessionManager() {
        // Private constructor to prevent instantiation
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clearSession() {
        currentUser = null;
    }

    public static int getCurrentUserId() {
        if (currentUser != null) {
            return currentUser.getId();
        }
        return -1;
    }
}