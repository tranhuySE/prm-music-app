// Xác định gói (package) nơi lớp Fragment này tồn tại.
package com.example.music_app.fragments;

// Nhập các lớp cần thiết từ thư viện Android và của dự án.
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_app.Adapter.SongAdapter;
import com.example.music_app.AppDatabase;
import com.example.music_app.DAO.SongDao;
import com.example.music_app.R;
import com.example.music_app.entity.Song;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * SearchFragment là một màn hình cho phép người dùng tìm kiếm bài hát
 * dựa trên tiêu đề hoặc tên nghệ sĩ.
 */
public class SearchFragment extends Fragment {

    // Khai báo các biến cho các thành phần giao diện và dữ liệu.
    private EditText edtSearch;         // Ô nhập liệu để người dùng gõ từ khóa tìm kiếm.
    private RecyclerView rvSongs;       // Danh sách để hiển thị kết quả tìm kiếm.
    private SongAdapter adapter;        // Adapter để kết nối dữ liệu với RecyclerView.
    private final List<Song> allSongs = new ArrayList<>(); // Danh sách chứa TẤT CẢ các bài hát, dùng làm nguồn để lọc.
    private final List<Song> filteredSongs = new ArrayList<>(); // Danh sách chỉ chứa các bài hát khớp với từ khóa tìm kiếm.
    private static final int REQUEST_CODE = 123; // Mã yêu cầu quyền, dùng để xác định kết quả trả về.
    private Context context;            // Ngữ cảnh của ứng dụng.

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // "Thổi phồng" layout XML để tạo giao diện.
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        context = getContext();

        // Ánh xạ các biến với View trong layout.
        edtSearch = view.findViewById(R.id.edtSearch);
        rvSongs = view.findViewById(R.id.rvSongs);
        // Thiết lập cách hiển thị cho RecyclerView (danh sách dọc).
        rvSongs.setLayoutManager(new LinearLayoutManager(context));

        // Kiểm tra xem ứng dụng đã có quyền truy cập bộ nhớ/media chưa.
        if (hasPermission()) {
            loadSongs(); // Nếu có quyền, tải danh sách bài hát ngay.
        } else {
            requestPermission(); // Nếu chưa, yêu cầu người dùng cấp quyền.
        }

        // Gắn một TextWatcher để lắng nghe sự kiện thay đổi văn bản trong ô tìm kiếm.
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            // Phương thức này được gọi mỗi khi người dùng gõ hoặc xóa một ký tự.
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Chuyển từ khóa người dùng nhập thành chữ thường và bỏ dấu để tìm kiếm không phân biệt hoa/thường và dấu.
                String keyword = removeAccent(s.toString().toLowerCase());
                // Xóa danh sách kết quả cũ.
                filteredSongs.clear();

                // Lặp qua danh sách TẤT CẢ các bài hát.
                for (Song song : allSongs) {
                    // Chuẩn bị tiêu đề và tên nghệ sĩ của bài hát để so sánh (cũng bỏ dấu và chuyển thành chữ thường).
                    String title = removeAccent(song.getTitle().toLowerCase());
                    String artist = removeAccent(song.getArtist().toLowerCase());
                    // Nếu tiêu đề hoặc nghệ sĩ chứa từ khóa tìm kiếm.
                    if (title.contains(keyword) || artist.contains(keyword)) {
                        // Thêm bài hát đó vào danh sách kết quả.
                        filteredSongs.add(song);
                    }
                }
                // Báo cho Adapter biết rằng dữ liệu đã thay đổi để nó cập nhật lại RecyclerView.
                adapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    // Phương thức kiểm tra quyền truy cập media.
    private boolean hasPermission() {
        // Từ Android 13 (TIRAMISU) trở lên, cần quyền READ_MEDIA_AUDIO.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Các phiên bản cũ hơn dùng quyền READ_EXTERNAL_STORAGE.
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    // Phương thức hiển thị hộp thoại yêu cầu quyền từ người dùng.
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_CODE);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    // Phương thức tải tất cả bài hát từ cơ sở dữ liệu.
    private void loadSongs() {
        // Xóa sạch dữ liệu cũ để chuẩn bị tải mới.
        allSongs.clear();
        filteredSongs.clear();

        // Tạo luồng nền để truy cập CSDL.
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            SongDao songDao = db.songDao();
            // Lấy tất cả bài hát từ CSDL.
            List<Song> allFromDB = songDao.getAllSongs();

            // Quay về luồng UI để cập nhật giao diện.
            requireActivity().runOnUiThread(() -> {
                // Thêm tất cả bài hát vào cả hai danh sách.
                allSongs.addAll(allFromDB);
                filteredSongs.addAll(allSongs); // Ban đầu, danh sách lọc hiển thị tất cả bài hát.
                // Khởi tạo Adapter với danh sách sẽ được hiển thị (filteredSongs).
                adapter = new SongAdapter(filteredSongs, context);
                // Gắn Adapter vào RecyclerView.
                rvSongs.setAdapter(adapter);
            });
        }).start();
    }

    /**
     * Phương thức tiện ích để loại bỏ dấu tiếng Việt khỏi một chuỗi.
     * @param input Chuỗi đầu vào (ví dụ: "Bài hát mới").
     * @return Chuỗi đã được bỏ dấu (ví dụ: "Bai hat moi").
     */
    public static String removeAccent(String input) {
        if (input == null) return "";
        // Chuẩn hóa chuỗi về dạng NFD (Canonical Decomposition), tách ký tự và dấu thành các phần riêng biệt.
        // Ví dụ: 'á' -> 'a' + '´'.
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        // Sử dụng biểu thức chính quy để xóa tất cả các ký tự dấu.
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    // Callback được gọi sau khi người dùng trả lời yêu cầu cấp quyền.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Kiểm tra xem kết quả này có phải dành cho yêu cầu của chúng ta không.
        if (requestCode == REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Nếu người dùng đồng ý cấp quyền.
            loadSongs();
        } else {
            // Nếu người dùng từ chối.
            Toast.makeText(context, "Từ chối quyền truy cập nhạc!", Toast.LENGTH_SHORT).show();
        }
    }
}