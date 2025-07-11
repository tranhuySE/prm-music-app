package com.example.music_app;

import android.content.Intent;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputUserName = findViewById(R.id.input_UserName);
        inputPassword = findViewById(R.id.input_Password);
        btnLogin = findViewById(R.id.btn_Login);
        tvRegister = findViewById(R.id.tv_register);
        btnBack = findViewById(R.id.btn_back);
        View rootView = findViewById(android.R.id.content); // Dùng cho Snackbar

        db = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);

        btnLogin.setOnClickListener(v -> {
            String username = inputUserName.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Snackbar.make(rootView, "Vui lòng nhập đầy đủ", Snackbar.LENGTH_SHORT).show();
                return;
            }

            // Truy vấn DB phải chạy trên background thread
            new Thread(() -> {
                User user = db.userDao().login(username, password);

                runOnUiThread(() -> {
                    if (user != null) {
                        sessionManager.saveLoginSession(user.username); // Lưu trạng thái
                        Snackbar.make(rootView, "Đăng nhập thành công", Snackbar.LENGTH_SHORT).show();

                        new android.os.Handler().postDelayed(() -> {
                            Intent intent = new Intent(this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }, 800); // 800 milliseconds delay

                    } else {
                        Snackbar.make(rootView, "Sai tài khoản hoặc mật khẩu", Snackbar.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        btnBack.setOnClickListener(v->{
            finish();
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}
