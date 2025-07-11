package com.example.music_app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.music_app.entity.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword, edtConfirm;
    private MaterialButton btnRegister,btnbackRegister;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtUsername = findViewById(R.id.edt_Username);
        edtPassword = findViewById(R.id.edt_Password);
        edtConfirm = findViewById(R.id.edt_Confirm);
        btnRegister = findViewById(R.id.btn_Register);
        btnbackRegister = findViewById(R.id.btn_RegisterBack);
        View rootview = findViewById(android.R.id.content);

        db = AppDatabase.getInstance(this);

        btnRegister.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirm = edtConfirm.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Snackbar.make(rootview, "Vui lòng nhập đủ thông tin", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 5) {
                Snackbar.make(rootview, "Mật khẩu phải có ít nhất 5 ký tự", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirm)) {
                Snackbar.make(rootview, "Mật khẩu không khớp", Snackbar.LENGTH_SHORT).show();
                return;
            }
            // Kiểm tra trùng tên
            if (db.userDao().getUserByUsername(username) != null) {
                Snackbar.make(rootview, "Tên người dùng đã tồn tại", Snackbar.LENGTH_SHORT).show();
                return;
            }
            db.userDao().insert(new User(username, password));
            Snackbar.make(rootview, "Đăng kí thành công", Snackbar.LENGTH_SHORT).show();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                finish(); // trở về Login
            }, 800);
        });

        btnbackRegister.setOnClickListener(v->{
            finish();
        });
    }
}
