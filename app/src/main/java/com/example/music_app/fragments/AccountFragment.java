package com.example.music_app.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.music_app.AppDatabase;
import com.example.music_app.ListSongFavoriteActivity;
import com.example.music_app.LoginActivity;
import com.example.music_app.R;
import com.example.music_app.auth.SessionManager;
import com.example.music_app.entity.User;
import com.google.android.material.snackbar.Snackbar;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {
    private SessionManager sessionManager;
    private Button btnLoginLogout , btnChangePassword , btnFavoriteSong;
    private TextView tvUsername;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);

        sessionManager = new SessionManager(requireContext());
        btnLoginLogout = view.findViewById(R.id.btnLogout);
        tvUsername = view.findViewById(R.id.tvUsername);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnFavoriteSong = view.findViewById(R.id.btnFavoriteSongs);

        setupUI();

        btnLoginLogout.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                // Đăng xuất
                sessionManager.logout();
                setupUI(); // Cập nhật lại giao diện
            } else {
                // Chuyển sang Login
                startActivity(new Intent(requireContext(), LoginActivity.class));
            }
        });

        btnChangePassword.setOnClickListener(v -> {
            if (!sessionManager.isLoggedIn()) {
                Snackbar.make(view, "Vui lòng đăng nhập", Snackbar.LENGTH_SHORT).show();
                return;
            }

            View dialogView = inflater.inflate(R.layout.dialog_change_password, null);

            EditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
            EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
            EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Đổi mật khẩu")
                    .setView(dialogView)
                    .setNegativeButton("Hủy", null)
                    .create();

            dialog.setOnShowListener(dialogInterface -> {
                Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                if (btnPositive == null) {
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setText("Hủy");
                    // Thêm nút Xác nhận thủ công
                    LinearLayout layout = (LinearLayout) dialogView.getParent();
                    Button btnConfirm = new Button(requireContext());
                    btnConfirm.setText("Xác nhận");
                    layout.addView(btnConfirm);

                    btnConfirm.setOnClickListener(confirmView -> handleChangePassword(
                            etOldPassword, etNewPassword, etConfirmPassword, dialogView, dialog
                    ));
                } else {
                    btnPositive.setOnClickListener(view1 -> handleChangePassword(
                            etOldPassword, etNewPassword, etConfirmPassword, dialogView, dialog
                    ));
                }
            });

            // Thêm nút xác nhận thủ công (cách đơn giản hơn)
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Xác nhận", (dialogInterface, which) -> {});
            dialog.show();
        });

        btnFavoriteSong.setOnClickListener(v->{
            if (!sessionManager.isLoggedIn()) {
                Snackbar.make(view, "Vui lòng đăng nhập", Snackbar.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(requireContext(), ListSongFavoriteActivity.class));
        });


        return view;
    }

    private void handleChangePassword(EditText etOldPassword, EditText etNewPassword, EditText etConfirmPassword,
                                      View view, AlertDialog dialog) {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        String oldPass = etOldPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Snackbar.make(view, "Vui lòng nhập đầy đủ", Snackbar.LENGTH_SHORT).show();
            return; // không đóng dialog
        }

        if (!newPass.equals(confirmPass)) {
            Snackbar.make(view, "Mật khẩu xác nhận không khớp", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 5) {
            Snackbar.make(view, "Mật khẩu mới phải từ 5 ký tự", Snackbar.LENGTH_SHORT).show();
            return;
        }

        String username = sessionManager.getUsername();

        new Thread(() -> {
            User user = db.userDao().getUserByUsername(username);
            if (user != null && user.password.equals(oldPass)) {
                db.userDao().changePassword(username, newPass);

                requireActivity().runOnUiThread(() -> {
                    Snackbar.make(view, "Đổi mật khẩu thành công", Snackbar.LENGTH_SHORT).show();
                    sessionManager.logout();
                    view.postDelayed(() -> {
                        dialog.dismiss();
                        Intent intent = new Intent(requireContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }, 1500);
                });
            } else {
                requireActivity().runOnUiThread(() ->
                        Snackbar.make(view, "Mật khẩu cũ không đúng", Snackbar.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void setupUI() {
        if (sessionManager.isLoggedIn()) {
            tvUsername.setText(sessionManager.getUsername());
            btnLoginLogout.setText("Đăng xuất");
        } else {
            tvUsername.setText("Khách chưa đăng nhập");
            btnLoginLogout.setText("Đăng nhập");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupUI(); // Luôn cập nhật lại khi quay lại fragment
    }
}