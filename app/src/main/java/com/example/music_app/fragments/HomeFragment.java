// Xác định gói (package) nơi lớp này tồn tại, giúp tổ chức cấu trúc dự án.
package com.example.music_app.fragments;

// Nhập (import) các lớp cần thiết từ thư viện Android và các thư viện khác.
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.music_app.AppDatabase;
import com.example.music_app.DAO.SongDao;
import com.example.music_app.PlayerActivity;
import com.example.music_app.R;
import com.example.music_app.entity.Song;
import com.example.music_app.utils.SongUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

// Định nghĩa lớp HomeFragment, kế thừa từ lớp Fragment của Android.
// Fragment là một phần giao diện người dùng có thể tái sử dụng.
public class HomeFragment extends Fragment {

    // Khai báo các biến cho các thành phần giao diện (UI components).
    // Đây là các layout sẽ chứa các danh sách bài hát theo chiều ngang.
    private LinearLayout layoutHotSongs, layoutLatestSongs, layoutFavorite;

    // Khai báo các danh sách (List) để lưu trữ các đối tượng bài hát (Song).
    private List<Song> allSongs, hotSongs, newSongs, favoriteSong;

    // Khai báo ProgressBar để hiển thị trạng thái đang tải dữ liệu.
    private ProgressBar progressBar;

    // Khai báo biến Context để lưu trữ ngữ cảnh của ứng dụng.
    private Context context;

    // Đây là phương thức được gọi để tạo và trả về View (giao diện) cho Fragment.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // "Thổi phồng" (inflate) layout XML (fragment_home.xml) thành một đối tượng View trong Java.
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Lấy Context từ Fragment.
        context = getContext();

        // Ánh xạ các biến Java với các thành phần giao diện trong file XML thông qua ID của chúng.
        layoutHotSongs = view.findViewById(R.id.layoutHotSongs);
        layoutLatestSongs = view.findViewById(R.id.layoutLatestSongs);
        layoutFavorite = view.findViewById(R.id.layoutFavoriteSong);
        progressBar = view.findViewById(R.id.progressBar);

        // Khởi tạo danh sách allSongs để tránh lỗi NullPointerException.
        allSongs = new ArrayList<>();

        // Gọi phương thức để bắt đầu quá trình tải dữ liệu.
        loadData();

        // Trả về View đã được tạo để hệ thống hiển thị nó lên màn hình.
        return view;
    }

    // Phương thức chịu trách nhiệm tải dữ liệu bài hát.
    private void loadData() {
        // Hiển thị ProgressBar để thông báo cho người dùng rằng ứng dụng đang tải.
        progressBar.setVisibility(View.VISIBLE);

        // Tạo một luồng (Thread) mới để thực hiện các tác vụ nặng (I/O, network).
        // Điều này giúp tránh làm treo giao diện người dùng (UI Thread).
        new Thread(() -> {
            // Lấy một instance của cơ sở dữ liệu (sử dụng Room).
            AppDatabase db = AppDatabase.getInstance(context);
            // Lấy đối tượng DAO (Data Access Object) để tương tác với bảng 'song'.
            SongDao songDao = db.songDao();

            // Thử lấy tất cả bài hát từ cơ sở dữ liệu.
            List<Song> songsFromDb = songDao.getAllSongs();

            // Kiểm tra xem cơ sở dữ liệu có trống không (thường là lần đầu chạy ứng dụng).
            if (songsFromDb.isEmpty()) {
                // Nếu CSDL trống, thì quét các bài hát từ bộ nhớ của thiết bị.
                List<Song> songsFromDevice = SongUtils.getAllSongsFromDevice(context);
                // Chèn tất cả bài hát vừa quét được vào cơ sở dữ liệu để lần sau dùng lại.
                songDao.insertAll(songsFromDevice);
                // Gán danh sách bài hát từ thiết bị vào danh sách đang sử dụng.
                songsFromDb = songsFromDevice;
            }

            // Tạo một biến final để có thể truy cập trong lambda của runOnUiThread.
            List<Song> finalSongs = songsFromDb;

            // Gửi một tác vụ về cho luồng giao diện chính (UI Thread) để cập nhật giao diện.
            // Bắt buộc phải cập nhật UI trên luồng chính.
            requireActivity().runOnUiThread(() -> {
                // Xóa danh sách cũ và thêm tất cả các bài hát vừa tải được.
                allSongs.clear();
                allSongs.addAll(finalSongs);

                // Sử dụng các hàm tiện ích từ SongUtils để lấy các danh sách được phân loại.
                hotSongs = SongUtils.getTopPlayedSongs(context); // Lấy danh sách nghe nhiều.
                newSongs = SongUtils.getLatestSongs(allSongs);   // Lấy danh sách mới nhất.
                favoriteSong = SongUtils.getFavoriteList(context); // Lấy danh sách yêu thích.

                // Gọi phương thức để hiển thị từng danh sách lên giao diện.
                showHorizontalSongs(hotSongs, layoutHotSongs);
                showHorizontalSongs(newSongs, layoutLatestSongs);
                showHorizontalSongs(favoriteSong, layoutFavorite);

                // Sau khi đã hiển thị xong, ẩn ProgressBar đi.
                progressBar.setVisibility(View.GONE);
            });
        }).start(); // Bắt đầu thực thi luồng nền.
    }

    // Phương thức trợ giúp để hiển thị một danh sách bài hát vào một LinearLayout cho trước.
    private void showHorizontalSongs(List<Song> songs, LinearLayout parentLayout) {
        // Xóa tất cả các view con hiện có trong layout để tránh hiển thị trùng lặp khi làm mới.
        parentLayout.removeAllViews();

        // Lặp qua từng đối tượng Song trong danh sách được truyền vào.
        for (Song song : songs) {
            // "Thổi phồng" layout item_horizontal_song.xml để tạo view cho một bài hát.
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_horizontal_song, parentLayout, false);

            // Ánh xạ các thành phần trong item view.
            ImageView imgCover = itemView.findViewById(R.id.imgCover);
            TextView tvTitle = itemView.findViewById(R.id.tvTitle);

            // Thiết lập tiêu đề bài hát cho TextView.
            tvTitle.setText(song.getTitle());

            // Lấy ảnh bìa (cover) của bài hát từ đường dẫn file.
            Bitmap cover = SongUtils.getSongCover(song.getPath());
            if (cover != null) {
                // Nếu có ảnh bìa, hiển thị nó.
                imgCover.setImageBitmap(cover);
            } else {
                // Nếu không, hiển thị một ảnh mặc định.
                imgCover.setImageResource(R.drawable.default_cover);
            }

            // Thiết lập sự kiện onClick cho item view.
            itemView.setOnClickListener(v -> {
                // Tạo một Intent để chuyển sang PlayerActivity (màn hình chơi nhạc).
                Intent intent = new Intent(context, PlayerActivity.class);

                // Đính kèm (putExtra) các dữ liệu cần thiết vào Intent.
                intent.putExtra("song_title", song.getTitle()); // Tiêu đề
                intent.putExtra("song_artist", song.getArtist()); // Nghệ sĩ
                intent.putExtra("song_path", song.getPath()); // Đường dẫn file
                intent.putExtra("song_duration", song.getDuration()); // Thời lượng

                // Chuyển đổi cả danh sách bài hát thành một chuỗi JSON bằng thư viện Gson.
                // Điều này cho phép PlayerActivity biết danh sách phát hiện tại (để next/previous).
                intent.putExtra("song_list", new Gson().toJson(songs));

                // Gửi chỉ số (index) của bài hát đang được nhấn trong danh sách.
                intent.putExtra("current_index", songs.indexOf(song));

                // Bắt đầu PlayerActivity.
                startActivity(intent);
            });

            // Thêm item view (của một bài hát) vào layout cha (LinearLayout).
            parentLayout.addView(itemView);
        }
    }
}