package com.mirea.kt.ribo;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvDisplay, tvHistory;
    private ImageButton btnMenu, btnShare;
    private String currentInput = "";
    private double firstNumber = 0;
    private String operator = "";
    private boolean waitingForSecondNumber = false;
    private boolean hasDecimal = false;
    private String history = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideNavigationBar();

        setContentView(R.layout.activity_main);
        initializeViews();
        setupMenuButton();
        setupShareButton();
        updateDisplay();
    }

    private void initializeViews() {
        tvDisplay = findViewById(R.id.tvDisplay);
        tvHistory = findViewById(R.id.tvHistory);
        btnMenu = findViewById(R.id.btnMenu);
        btnShare = findViewById(R.id.btnShare);
    }

    private void setupMenuButton() {
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuDialog();
            }
        });
    }

    private void setupShareButton() {
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Share.ShareResult result = Share.PrepareResult(tvDisplay.getText().toString(), tvHistory.getText().toString());

                if (result.success) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Результат вычислений");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, result.message);

                    try {
                        startActivity(Intent.createChooser(shareIntent, "Поделиться результатом"));

                        Logger.Info("Результаты успешно отправлены");
                    } catch (Exception e) {
                        showErrorMessage("Ошибка при отправке: " + e.toString());

                        Logger.Error("Ошибка при отправке: " + e.toString());
                    }
                } else {
                    showErrorMessage(result.message);

                    Logger.Error("Ошибка при отправке реультатов: " + result.message);
                }
            }
        });
    }

    public void onButtonClick(View view) {
        Logger.Info("Обработка ввода");

        ImageButton imageButton = (ImageButton) view;
        CharSequence description = imageButton.getContentDescription();

        if (description == null) return;

        String buttonText = description.toString();

        switch (buttonText) {
            case "0": case "1": case "2": case "3": case "4":
            case "5": case "6": case "7": case "8": case "9":
                Logger.Info("Пользователь ввел цифру");
                appendNumber(buttonText);
                break;

            case "Десятичная точка":
                Logger.Info("Пользователь ввел десятичное число");
                appendDecimal();
                break;

            case "Сложение":
                Logger.Info("Пользователь ввел знак сложения");
                setOperator("+");
                break;
            case "Вычитание":
                Logger.Info("Пользователь ввел знак вычитания");
                setOperator("-");
                break;
            case "Умножение":
                Logger.Info("Пользователь ввел знак умножения");
                setOperator("×");
                break;
            case "Деление":
                Logger.Info("Пользователь ввел знак деления");
                setOperator("÷");
                break;

            case "Равно":
                Logger.Info("Пользователь ввел знак равно");
                calculateResult();
                break;

            case "Очистить":
                Logger.Info("Пользователь ввел знак очистить");
                clearAll();
                break;

            case "Удалить":
                Logger.Info("Пользователь ввел знак удалить");
                backspace();
                break;

            case "Смена знака":
                Logger.Info("Пользователь ввел знак смены знака");
                changeSign();
                break;

            case "Квадрат":
                Logger.Info("Пользователь ввел знак возведение в квадрат");
                calculateSquare();
                break;

            case "Квадратный корень":
                Logger.Info("Пользователь ввел знак квадратного корня");
                calculateSquareRoot();
                break;

            case "Возведение в степень":
                Logger.Info("Пользователь ввел знак возведения в степень");
                setOperator("^");
                break;

            case "Процент":
                Logger.Info("Пользователь ввел знак процента");
                calculatePercentage();
                break;
        }
    }

    private void appendNumber(String number) {
        if (waitingForSecondNumber) {
            currentInput = number;
            waitingForSecondNumber = false;
            hasDecimal = false;
        } else {
            if (currentInput.equals("0")) {
                currentInput = number;
            } else {
                currentInput += number;
            }
        }
        updateDisplay();
    }

    private void appendDecimal() {
        if (waitingForSecondNumber) {
            currentInput = "0.";
            waitingForSecondNumber = false;
            hasDecimal = true;
        } else if (!hasDecimal) {
            if (currentInput.isEmpty()) {
                currentInput = "0.";
            } else {
                currentInput += ".";
            }
            hasDecimal = true;
        }
        updateDisplay();
    }

    private void setOperator(String newOperator) {
        if (!currentInput.isEmpty()) {
            if (!operator.isEmpty() && !waitingForSecondNumber) {
                calculateResult();
            }

            firstNumber = Double.parseDouble(currentInput);
            operator = newOperator;
            waitingForSecondNumber = true;
            hasDecimal = false;

            history = formatNumber(firstNumber) + " " + operator;
            tvHistory.setText(history);
        }
    }

    private void calculateResult() {
        if (operator.isEmpty() || waitingForSecondNumber || currentInput.isEmpty()) {
            return;
        }

        double secondNumber = Double.parseDouble(currentInput);
        double result = 0;
        boolean error = false;

        try {
            switch (operator) {
                case "+":
                    result = firstNumber + secondNumber;
                    break;
                case "-":
                    result = firstNumber - secondNumber;
                    break;
                case "×":
                    result = firstNumber * secondNumber;
                    break;
                case "÷":
                    if (secondNumber == 0) {
                        error = true;
                        tvDisplay.setText("Деление на ноль");
                        Logger.Error("Пользователь разделил на ноль");
                    } else {
                        result = firstNumber / secondNumber;
                    }
                    break;
                case "^":
                    result = Math.pow(firstNumber, secondNumber);
                    break;
            }

            if (!error) {
                history = formatNumber(firstNumber) + " " + operator +
                        " " + formatNumber(secondNumber) + " =";
                tvHistory.setText(history);

                currentInput = formatResult(result);
                operator = "";
                waitingForSecondNumber = true;
                hasDecimal = currentInput.contains(".");
                updateDisplay();

                Logger.Info("Пользователь получил результат выражения: " + history + result);
            }

        } catch (Exception e) {
            Logger.Error("Ошибка при рассчете выражения: " + history + ": " + e.toString());
            tvDisplay.setText("Ошибка");
        }
    }

    private void calculateSquare() {
        if (!currentInput.isEmpty()) {
            double number = Double.parseDouble(currentInput);
            double result = number * number;

            history = "sqr(" + formatNumber(number) + ") =";
            tvHistory.setText(history);

            currentInput = formatResult(result);
            updateDisplay();
        }
    }

    private void calculateSquareRoot() {
        if (!currentInput.isEmpty()) {
            double number = Double.parseDouble(currentInput);
            if (number < 0) {
                tvDisplay.setText("Ошибка");
                Logger.Error("Вычисление корня числа меньше 0");
                return;
            }

            double result = Math.sqrt(number);

            history = "√(" + formatNumber(number) + ") =";
            tvHistory.setText(history);

            currentInput = formatResult(result);
            updateDisplay();
        }
    }

    private void calculatePercentage() {
        if (!currentInput.isEmpty()) {
            double number = Double.parseDouble(currentInput);
            double result = number / 100;

            currentInput = formatResult(result);
            updateDisplay();
        }
    }

    private void changeSign() {
        if (!currentInput.isEmpty() && !currentInput.equals("0")) {
            if (currentInput.startsWith("-")) {
                currentInput = currentInput.substring(1);
            } else {
                currentInput = "-" + currentInput;
            }
            updateDisplay();
        }
    }

    private void backspace() {
        if (!currentInput.isEmpty()) {
            if (currentInput.length() == 1) {
                currentInput = "0";
                hasDecimal = false;
            } else {
                if (currentInput.charAt(currentInput.length() - 1) == '.') {
                    hasDecimal = false;
                }
                currentInput = currentInput.substring(0, currentInput.length() - 1);
            }
            updateDisplay();
        }
    }

    private void clearAll() {
        currentInput = "";
        firstNumber = 0;
        operator = "";
        waitingForSecondNumber = false;
        hasDecimal = false;
        history = "";
        tvHistory.setText("");
        updateDisplay();
    }

    private void updateDisplay() {
        if (currentInput.isEmpty()) {
            tvDisplay.setText("0");
        } else {
            String displayText = currentInput;
            if (displayText.contains(".")) {
                displayText = displayText.replaceAll("0*$", "");
                if (displayText.endsWith(".")) {
                    displayText = displayText.substring(0, displayText.length() - 1);
                }
            }
            tvDisplay.setText(displayText);
        }
    }

    private String formatResult(double result) {
        if (result == (long) result) {
            return String.valueOf((long) result);
        } else {
            String formatted = String.format("%.10f", result);
            formatted = formatted.replaceAll("0*$", "");
            if (formatted.endsWith(".")) {
                formatted = formatted.substring(0, formatted.length() - 1);
            }
            return formatted;
        }
    }

    private String formatNumber(double number) {
        if (number == (long) number) {
            return String.valueOf((long) number);
        } else {
            return String.valueOf(number);
        }
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showMenuDialog() {
        Logger.Info("Открытие диалогового окна меню");

        final Dialog menuDialog = new Dialog(this);
        menuDialog.setContentView(R.layout.dialog_menu);
        menuDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button btnAboutAuthor = menuDialog.findViewById(R.id.btnAboutAuthor);
        Button btnAboutUser = menuDialog.findViewById(R.id.btnAboutUser);
        Button btnExit = menuDialog.findViewById(R.id.btnExit);
        Button btnCancel = menuDialog.findViewById(R.id.btnCancel);

        btnAboutAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuDialog.dismiss();
                showAboutAuthorDialog();
            }
        });

        btnAboutUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuDialog.dismiss();
                showAboutUserDialog();
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuDialog.dismiss();
                Session.Clear();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuDialog.dismiss();
                Logger.Info("Закрытие диалогового окна меню");
            }
        });

        menuDialog.show();
    }

    private void showAboutAuthorDialog() {
        Logger.Info("Открытие диалогового окна об авторе");

        final Dialog aboutDialog = new Dialog(this);
        aboutDialog.setContentView(R.layout.dialog_about_author);
        aboutDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button btnClose = aboutDialog.findViewById(R.id.btnClose);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aboutDialog.dismiss();
                Logger.Info("Закрытие диалогового окна от авторе");
            }
        });

        aboutDialog.show();
    }

    private void showAboutUserDialog() {
        Logger.Info("Открытие диалогового окна о пользователе");

        final Dialog aboutDialog = new Dialog(this);
        aboutDialog.setContentView(R.layout.dialog_about_user);
        aboutDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView login = aboutDialog.findViewById(R.id.login);
        TextView password = aboutDialog.findViewById(R.id.password);
        TextView group = aboutDialog.findViewById(R.id.group);
        Button btnAboutTask = aboutDialog.findViewById(R.id.btnAboutTask);
        Button btnClose = aboutDialog.findViewById(R.id.btnClose);

        login.setText("Логин: " + Session.GetUserLogin());
        password.setText("Пароль: " + Session.GetUserPassword());
        group.setText("Группа: " + Session.GetUserGroup());

        btnAboutTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aboutDialog.dismiss();
                showAboutTaskDialog();
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aboutDialog.dismiss();
                Logger.Info("Закрытие диалогового окна о пользователе");
            }
        });

        aboutDialog.show();
    }

    private void showAboutTaskDialog() {
        Logger.Info("Открытие диалогового окна о задании");

        final Dialog aboutDialog = new Dialog(this);
        aboutDialog.setContentView(R.layout.dialog_about_task);
        aboutDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView variant = aboutDialog.findViewById(R.id.variant);
        TextView title = aboutDialog.findViewById(R.id.title);
        TextView task = aboutDialog.findViewById(R.id.task);
        Button btnClose = aboutDialog.findViewById(R.id.btnClose);

        variant.setText("Вариант: " + Session.GetUserVariant());
        title.setText("Название: " + Session.GetUserTitle());
        task.setText("Задание: " + Session.GetUserTask());

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aboutDialog.dismiss();
                Logger.Info("Закрытие диалогового окна о задании");
            }
        });

        aboutDialog.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentInput", currentInput);
        outState.putDouble("firstNumber", firstNumber);
        outState.putString("operator", operator);
        outState.putBoolean("waitingForSecondNumber", waitingForSecondNumber);
        outState.putBoolean("hasDecimal", hasDecimal);
        outState.putString("history", history);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentInput = savedInstanceState.getString("currentInput", "");
        firstNumber = savedInstanceState.getDouble("firstNumber", 0);
        operator = savedInstanceState.getString("operator", "");
        waitingForSecondNumber = savedInstanceState.getBoolean("waitingForSecondNumber", false);
        hasDecimal = savedInstanceState.getBoolean("hasDecimal", false);
        history = savedInstanceState.getString("history", "");
        tvHistory.setText(history);
        updateDisplay();
    }

    private void hideNavigationBar() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavigationBar();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideNavigationBar();
        }
    }
}