package com.mirea.kt.ribo;

import android.content.Intent;
import android.widget.Toast;

public class Share {

    public static class ShareResult {
        public boolean success = false;
        public String message = "";
    }
    public static ShareResult PrepareResult(String displayText, String historyText) {
        ShareResult result = new ShareResult();

        if (displayText.equals("0") && historyText.isEmpty()) {
            result.message = "Нет результата для отправки";
            return result;
        }

        StringBuilder shareText = new StringBuilder();
        shareText.append("\n\nОтправлено из приложения Арифметический калькулятор✅\n");
        shareText.append("От пользователя ");
        shareText.append(Session.GetUserLogin());
        shareText.append("\nВыражение: ");

        if (!historyText.isEmpty()) {
            shareText.append(historyText.substring(0, historyText.length() - 2)).append("\n");
        }

        shareText.append("Ответ: ").append(displayText);

        result.success = true;
        result.message = shareText.toString();
        return result;
    }
}
