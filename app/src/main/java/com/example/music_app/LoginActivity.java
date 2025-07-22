package com.example.music_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.music_app.auth.SessionManager;
import com.example.music_app.entity.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class LoginActivity extends AppCompatActivity {

    private EditText inputUserName, inputPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister;
    private ImageView btnBack;
    private AppDatabase db;
    private SessionManager sessionManager;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();

        db = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);

        btnLogin.setOnClickListener(v -> handleLogin());
        btnBack.setOnClickListener(v -> finish());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void initViews() {
        inputUserName = findViewById(R.id.input_UserName);
        inputPassword = findViewById(R.id.input_Password);
        btnLogin = findViewById(R.id.btn_Login);
        tvRegister = findViewById(R.id.tv_register);
        btnBack = findViewById(R.id.btn_back);
        rootView = findViewById(android.R.id.content); // Dùng cho Snackbar
    }

    private void handleLogin() {
        String username = inputUserName.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showSnackbar("Vui lòng nhập đầy đủ");
            return;
        }

        new Thread(() -> {
            User user = db.userDao().login(username, password);
            runOnUiThread(() -> {
                if (user != null) {
                    sessionManager.saveLoginSession(user.username);
                    showSnackbar("Đăng nhập thành công");

                    new Handler().postDelayed(() -> {
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    }, 800);
                } else {
                    showSnackbar("Sai tài khoản hoặc mật khẩu");
                }
            });
        }).start();
    }

    private void showSnackbar(String message) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
    }
}
