package com.example.music_app;

import android.os.Bundle;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword, edtConfirm;
    private MaterialButton btnRegister;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtUsername = findViewById(R.id.edt_Username);
        edtPassword = findViewById(R.id.edt_Password);
        edtConfirm = findViewById(R.id.edt_Confirm);
        btnRegister = findViewById(R.id.btn_Register);

        db = AppDatabase.getInstance(this);

        btnRegister.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirm = edtConfirm.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra trùng tên
            if (db.userDao().getUserByUsername(username) != null) {
                Toast.makeText(this, "Tên người dùng đã tồn tại", Toast.LENGTH_SHORT).show();
                return;
            }

            db.userDao().insert(new User(username, password));
            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
            finish(); // trở về login
        });
    }
}
