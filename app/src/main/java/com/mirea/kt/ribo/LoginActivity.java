package com.mirea.kt.ribo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.IOException;
import android.os.AsyncTask;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private EditText etLogin, etPassword, etGroup;
    private Button btnLogin;
    private ImageButton btnTogglePassword;
    private ProgressDialog progressDialog;
    private boolean isPasswordVisible = false;

    private class LoginTask extends AsyncTask<String, Void, String> {
        private String login;
        private String password;
        private final String group = "RIBO-01-22";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();

            Logger.Info("Попытка входа в приложение");
        }

        @Override
        protected String doInBackground(String... params) {
            login = params[0];
            password = params[1];

            HttpURLConnection connection = null;
            OutputStream outputStream = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(Config.LOGIN_URL);
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                String boundary = "Boundary-" + System.currentTimeMillis();
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                String formData = buildFormData(boundary);

                outputStream = connection.getOutputStream();
                outputStream.write(formData.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    Logger.Info("Сервер вернул ответ");

                    return response.toString();
                } else {
                    Logger.Error("Ошибка от сервера при попытке входа: " + responseCode);

                    return "Error: " + responseCode;
                }

            } catch (Exception e) {
                Logger.Error("Ошибка при запросе на сервер: " + e.toString());

                return "Exception: " + e.getMessage();
            } finally {
                try {
                    if (outputStream != null) outputStream.close();
                    if (reader != null) reader.close();
                    if (connection != null) connection.disconnect();
                } catch (IOException e) {
                    Logger.Error("Ошибка при закрытии соединения: " + e.toString());

                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dismissProgressDialog();

            try {
                if (result.startsWith("Error:") || result.startsWith("Exception:")) {
                    showErrorMessage(result);
                    return;
                }

                JSONObject jsonResponse = new JSONObject(result);
                int resultCode = jsonResponse.getInt("result_code");

                if (resultCode == 1) {
                    if (!saveSessionData(jsonResponse)) {
                        Logger.Error("Не удалось войти в приложение");
                        return;
                    }

                    showSuccessMessage("Успешный вход!");

                    Logger.Info("Пользователь успешно вошел в систему");

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    String error = jsonResponse.getString("error");
                    showErrorMessage("Неверные данные для входа: " + error);

                    Logger.Error("Ошибка при попытке входа: " + error);
                }

            } catch (Exception e) {
                showErrorMessage("Ошибка парсинга ответа: " + e.getMessage());
            }
        }

        private String buildFormData(String boundary) {
            StringBuilder formData = new StringBuilder();

            formData.append("--").append(boundary).append("\r\n");
            formData.append("Content-Disposition: form-data; name=\"lgn\"\r\n\r\n");
            formData.append(login).append("\r\n");

            formData.append("--").append(boundary).append("\r\n");
            formData.append("Content-Disposition: form-data; name=\"pwd\"\r\n\r\n");
            formData.append(password).append("\r\n");

            formData.append("--").append(boundary).append("\r\n");
            formData.append("Content-Disposition: form-data; name=\"g\"\r\n\r\n");
            formData.append(group).append("\r\n");

            // Завершаем boundary
            formData.append("--").append(boundary).append("--\r\n");

            return formData.toString();
        }

        private boolean saveSessionData(JSONObject jsonResponse) {
            try {
                if (jsonResponse.has("title")) {
                    Session.SetUserTitle(jsonResponse.getString("title"));
                }
                if (jsonResponse.has("task")) {
                    Session.SetUserTask(jsonResponse.getString("task"));
                }
                if (jsonResponse.has("variant")) {
                    Session.SetUserVariant(jsonResponse.getInt("variant"));
                }

                Session.SetUserLogin(login);
                Session.SetUserGroup(group);
                Session.SetUserPassword(password);

            } catch (Exception e) {
                Logger.Error("Ошибка парсинга ответа: "+ e.toString());

                showErrorMessage("Ошибка парсинга ответа");
                return false;
            }

            return Session.Validate();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.Info("Запуск приложения 'Арифметический калькулятор (AriCalc)'");

        hideNavigationBar();

        setContentView(R.layout.activity_login);
        initializeViews();
        setupLoginButton();
        setupPasswordToggle();
    }

    private void initializeViews() {
        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
    }

    private void setupLoginButton() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
    }

    private void setupPasswordToggle() {
        btnTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        btnTogglePassword.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                togglePasswordVisibility();
                return true;
            }
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Скрываем пароль
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_off);
            btnTogglePassword.setContentDescription("Показать пароль");
            isPasswordVisible = false;
        } else {
            // Показываем пароль
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            btnTogglePassword.setImageResource(R.drawable.ic_visibility);
            btnTogglePassword.setContentDescription("Скрыть пароль");
            isPasswordVisible = true;
        }

        etPassword.setSelection(etPassword.getText().length());
    }

    private void attemptLogin() {
        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (validateInput(login, password)) {
            hideKeyboard();

            loginUser(login, password);
        }
    }

    private boolean validateInput(String login, String password) {
        boolean isValid = true;

        if (login.isEmpty()) {
            Logger.Error("Поле логин не заполнено");

            etLogin.setError("Введите логин");
            isValid = false;
        }

        if (password.isEmpty()) {
            Logger.Error("Поле пароль не заполнено");

            etPassword.setError("Введите пароль");
            isValid = false;
        }

        return isValid;
    }

    private void loginUser(String login, String password) {
        new LoginTask().execute(login, password);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Выполняется вход");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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