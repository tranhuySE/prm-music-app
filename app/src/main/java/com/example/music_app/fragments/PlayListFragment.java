// Xác định gói (package) nơi lớp Fragment này tồn tại.
package com.example.music_app.fragments;

// Nhập các lớp cần thiết từ thư viện Android và của dự án.
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.music_app.Adapter.PlaylistAdapter;
import com.example.music_app.AppDatabase;
import com.example.music_app.PlaylistDetailActivity;
import com.example.music_app.R;
import com.example.music_app.auth.SessionManager;
import com.example.music_app.entity.PlayList;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

// Định nghĩa lớp PlayListFragment, kế thừa từ lớp Fragment của Android.
public class PlayListFragment extends Fragment {

    // Khai báo các biến cho các thành phần giao diện và logic.
    private RecyclerView recyclerView; // Dùng để hiển thị danh sách các playlist.
    private PlaylistAdapter adapter;   // Adapter để cung cấp dữ liệu cho RecyclerView.
    private AppDatabase db;            // Đối tượng truy cập cơ sở dữ liệu.
    private FloatingActionButton btnAdd; // Nút "+" để thêm playlist mới.
    private SessionManager sessionManager; // Đối tượng quản lý phiên đăng nhập của người dùng.

    // Constructor rỗng là bắt buộc đối với các Fragment.
    public PlayListFragment() {
        // Required empty public constructor
    }

    /**
     * Phương thức newInstance là một "factory method", đây là cách làm tốt nhất để
     * tạo một instance mới của Fragment, đặc biệt khi cần truyền tham số.
     */
    public static PlayListFragment newInstance() {
        return new PlayListFragment();
    }

    // Phương thức vòng đời, được gọi khi Fragment được tạo.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Tránh khởi tạo các đối tượng cần Context ở đây (ví dụ: new SessionManager(getContext())),
        // vì getContext() có thể trả về null nếu Fragment chưa được gắn vào Activity.
    }

    // Phương thức vòng đời, được gọi để tạo và trả về giao diện (View) của Fragment.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // "Thổi phồng" (inflate) layout từ file XML để tạo ra các đối tượng View.
        View view = inflater.inflate(R.layout.fragment_play_list, container, false);
        // Ánh xạ các biến với các View trong layout thông qua ID.
        recyclerView = view.findViewById(R.id.recyclerPlaylists);
        btnAdd = view.findViewById(R.id.btnAdd);

        // Khởi tạo các đối tượng cần Context.
        // requireContext() sẽ trả về một Context không null hoặc báo lỗi, an toàn để dùng ở đây.
        db = AppDatabase.getInstance(requireContext());
        sessionManager = new SessionManager(requireContext());

        // Khởi tạo Adapter và định nghĩa hành động khi một item được click.
        // Lambda `playlist -> { ... }` là một callback được truyền vào adapter.
        adapter = new PlaylistAdapter(new ArrayList<>(), playlist -> {
            // Khi người dùng nhấn vào một playlist, đoạn code này sẽ chạy.
            // Tạo một Intent để mở màn hình chi tiết playlist.
            Intent intent = new Intent(getContext(), PlaylistDetailActivity.class);
            // Đính kèm dữ liệu (ID và tên của playlist) vào Intent.
            intent.putExtra("playlistId", playlist.getId());
            intent.putExtra("playlistName", playlist.getName());
            // Khởi chạy Activity mới và lắng nghe kết quả trả về thông qua 'launcher'.
            launcher.launch(intent);
        });

        // Gắn Adapter vừa tạo vào RecyclerView.
        recyclerView.setAdapter(adapter);
        // Thiết lập cách sắp xếp các item trong RecyclerView (ở đây là danh sách dọc).
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Tải dữ liệu playlist lần đầu tiên.
        loadPlaylists();

        // Gán sự kiện click cho nút FloatingActionButton.
        btnAdd.setOnClickListener(v -> showAddDialog());

        // Trả về View đã được cấu hình để hệ thống hiển thị.
        return view;
    }

    // Phương thức để tải danh sách các playlist từ cơ sở dữ liệu.
    private void loadPlaylists() {
        // Tạo một luồng nền mới để thực hiện truy vấn CSDL, tránh làm treo luồng UI.
        new Thread(() -> {
            // Lấy tên người dùng đang đăng nhập.
            String currentUser = sessionManager.getUsername();
            if (currentUser == null) return; // Nếu chưa đăng nhập thì dừng lại.
            // Lấy danh sách playlist của người dùng từ CSDL.
            List<PlayList> playlists = db.playlistDao().getPlaylistsByUser(currentUser);
            // Sau khi có dữ liệu, quay trở lại luồng UI để cập nhật giao diện.
            requireActivity().runOnUiThread(() -> adapter.setPlaylists(playlists));
        }).start();
    }

    // Phương thức hiển thị hộp thoại (dialog) để thêm playlist mới.
    private void showAddDialog() {
        // Tạo một ô nhập liệu (EditText) bằng code Java.
        final EditText input = new EditText(getContext());
        input.setHint("Tên playlist");

        // Sử dụng AlertDialog.Builder để xây dựng một hộp thoại.
        new AlertDialog.Builder(getContext())
                .setTitle("Tạo Playlist mới") // Đặt tiêu đề cho dialog.
                .setView(input) // Đặt EditText làm nội dung chính của dialog.
                .setPositiveButton("Tạo", (dialog, which) -> { // Nút xác nhận.
                    // Lấy tên người dùng nhập vào và loại bỏ khoảng trắng thừa.
                    String name = input.getText().toString().trim();
                    // Chỉ xử lý nếu tên không rỗng.
                    if (!name.isEmpty()) {
                        String username = sessionManager.getUsername();
                        if (username == null) return;
                        // Tạo một đối tượng PlayList mới.
                        PlayList newPlaylist = new PlayList(name, username);
                        // Tạo luồng nền mới để chèn playlist vào CSDL.
                        new Thread(() -> {
                            db.playlistDao().insertPlaylist(newPlaylist);
                            // Sau khi chèn xong, tải lại danh sách để hiển thị playlist mới.
                            loadPlaylists();
                        }).start();
                    }
                })
                .setNegativeButton("Hủy", null) // Nút hủy, không làm gì cả.
                .show(); // Hiển thị dialog.
    }

    /**
     * Khởi tạo ActivityResultLauncher. Đây là cách làm hiện đại và an toàn để
     * khởi chạy một Activity và nhận kết quả trả về, thay thế cho onActivityResult() đã cũ.
     */
    private final ActivityResultLauncher<Intent> launcher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // Callback này sẽ được gọi khi Activity được khởi chạy (PlaylistDetailActivity) kết thúc.
                // Kiểm tra xem kết quả có thành công (RESULT_OK) và có dữ liệu trả về không.
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    // Lấy ID của playlist đã bị xóa từ Intent trả về. Giá trị mặc định là -1 nếu không tìm thấy.
                    int deletedId = result.getData().getIntExtra("deletedPlaylistId", -1);
                    // Nếu nhận được một ID hợp lệ (khác -1), có nghĩa là một playlist đã bị xóa.
                    if (deletedId != -1) {
                        // Tải lại danh sách playlist để cập nhật giao diện (loại bỏ playlist đã xóa).
                        loadPlaylists();
                    }
                }
            });
}