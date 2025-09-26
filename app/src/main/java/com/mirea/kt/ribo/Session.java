package com.mirea.kt.ribo;

public class Session {
    private static String UserLogin = "";
    private static String UserGroup = "";
    private static String UserPassword = "";
    private static int UserVariant = 0;
    private static String UserTitle = "";
    private static String UserTask = "";

    public static boolean Validate() {
        if (!UserLogin.isEmpty() && !UserGroup.isEmpty() && !UserPassword.isEmpty() && UserVariant > 0 && !UserTitle.isEmpty() && !UserTask.isEmpty()) {
            return true;
        }
        return false;
    }

    public static void Clear() {
        UserLogin = "";
        UserGroup = "";
        UserPassword = "";
        UserVariant = -1;
        UserTitle = "";
        UserTask = "";
    }

    public static String GetUserLogin() {
        return UserLogin;
    }

    public static String GetUserGroup() {
        return UserGroup;
    }

    public static String GetUserPassword() {
        return UserPassword;
    }

    public static int GetUserVariant() {
        return UserVariant;
    }

    public static String GetUserTitle() {
        return UserTitle;
    }

    public static String GetUserTask() {
        return UserTask;
    }

    public static void SetUserLogin(String login) {
        UserLogin = login;
    }

    public static void SetUserGroup(String group) {
        UserGroup = group;
    }

    public static void SetUserPassword(String password) {
        UserPassword = password;
    }

    public static void SetUserVariant(int variant) {
        UserVariant = variant;
    }

    public static void SetUserTitle(String title) {
        UserTitle = title;
    }

    public static void SetUserTask(String task) {
        UserTask = task;
    }
}
