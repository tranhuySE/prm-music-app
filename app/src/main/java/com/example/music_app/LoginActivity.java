package com.example.music_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.music_app.entity.User;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private EditText inputUserName, inputPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputUserName = findViewById(R.id.input_UserName);
        inputPassword = findViewById(R.id.input_Password);
        btnLogin = findViewById(R.id.btn_Login);
        tvRegister = findViewById(R.id.tv_register);

        db = AppDatabase.getInstance(this);

        btnLogin.setOnClickListener(v -> {
            String username = inputUserName.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = db.userDao().login(username, password);
            if (user != null) {
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            }
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}
