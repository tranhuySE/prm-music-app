// Xác định gói (package) của lớp tiện ích.
package com.example.music_app.utils;

// Nhập các lớp cần thiết từ Android SDK và các thư viện khác.
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.music_app.AppDatabase;
import com.example.music_app.DAO.SongDao;
import com.example.music_app.DAO.SongFavoriteDao;
import com.example.music_app.auth.SessionManager;
import com.example.music_app.entity.Song;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// Lớp tiện ích chứa các phương thức tĩnh liên quan đến việc xử lý bài hát.
public class SongUtils {

    /**
     * Lấy danh sách các bài hát được nghe nhiều nhất từ cơ sở dữ liệu.
     * @param context Ngữ cảnh của ứng dụng, cần thiết để truy cập CSDL.
     * @return Một danh sách (List) các đối tượng Song.
     */
    public static List<Song> getTopPlayedSongs(Context context) {
        // Lấy instance của cơ sở dữ liệu Room.
        AppDatabase db = AppDatabase.getInstance(context);
        // Lấy đối tượng DAO để tương tác với bảng 'songs'.
        SongDao songDao = db.songDao();
        // Gọi phương thức trong DAO để lấy 3 bài hát có playCount cao nhất.
        return songDao.getTopSongs(3);
    }

    /**
     * Lấy danh sách các bài hát mới nhất từ một danh sách cho trước.
     * Trong trường hợp này, "mới nhất" được hiểu là các bài hát cuối cùng trong danh sách.
     * @param all Danh sách tất cả các bài hát.
     * @return Một danh sách con chứa 5 bài hát cuối cùng.
     */
    public static List<Song> getLatestSongs(List<Song> all) {
        int size = all.size();
        // Sử dụng subList để cắt ra một danh sách con.
        // Math.max(0, size - 5) để đảm bảo không bị lỗi nếu danh sách có ít hơn 5 bài hát.
        return all.subList(Math.max(0, size - 5), size);
    }

    /**
     * Lấy danh sách các bài hát yêu thích của người dùng đang đăng nhập.
     * @param context Ngữ cảnh của ứng dụng.
     * @return Danh sách các bài hát yêu thích.
     */
    public static List<Song> getFavoriteList(Context context){
        AppDatabase db = AppDatabase.getInstance(context);
        // SessionManager dùng để quản lý phiên đăng nhập và lấy thông tin người dùng hiện tại.
        SessionManager sessionManager = new SessionManager(context);
        String currentUser = sessionManager.getUsername();
        // Lấy DAO cho các bài hát yêu thích.
        SongFavoriteDao songFavoriteDao = db.songFavoriteDao();
        // Trả về danh sách bài hát yêu thích cho người dùng cụ thể.
        return songFavoriteDao.getFavoritesForUser(currentUser);
    }

    /**
     * Trích xuất ảnh bìa (album art) được nhúng trong một file nhạc.
     * @param path Đường dẫn đến file nhạc (có thể là file local hoặc URL).
     * @return Một đối tượng Bitmap nếu có ảnh bìa, ngược lại trả về null.
     */
    public static Bitmap getSongCover(String path) {
        // MediaMetadataRetriever là một lớp của Android chuyên dùng để lấy metadata (dữ liệu mô tả) từ file media.
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            // Thiết lập nguồn dữ liệu cho retriever.
            retriever.setDataSource(path);
            // Trích xuất dữ liệu ảnh bìa dưới dạng một mảng byte.
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                // Nếu có dữ liệu ảnh, giải mã mảng byte thành một đối tượng Bitmap.
                return BitmapFactory.decodeByteArray(art, 0, art.length);
            }
        } catch (Exception e) {
            // Bắt các ngoại lệ có thể xảy ra (ví dụ: file không tồn tại, định dạng không hỗ trợ).
            // Không làm gì cả, chỉ đơn giản là sẽ trả về null ở cuối.
        }
        // Nếu không có ảnh bìa hoặc có lỗi, trả về null.
        return null;
    }

    /**
     * Quét bộ nhớ thiết bị để lấy tất cả các file nhạc.
     * @param context Ngữ cảnh của ứng dụng.
     * @return Một danh sách tất cả các bài hát tìm thấy trên thiết bị.
     */
    public static List<Song> getAllSongsFromDevice(Context context) {
        List<Song> songList = new ArrayList<>();

        // Yêu cầu hệ thống quét lại các thư mục nhạc và download để cập nhật MediaStore.
        // Điều này đảm bảo các file nhạc mới tải về sẽ được tìm thấy.
        MediaScannerConnection.scanFile(
                context,
                new String[]{"/sdcard/Music", "/sdcard/Download"},
                null,
                null
        );

        // Uri trỏ đến bảng chứa thông tin các file audio trong bộ nhớ ngoài của thiết bị.
        Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        // Mệnh đề 'WHERE' của câu truy vấn: chỉ lấy các file được đánh dấu là nhạc (IS_MUSIC != 0).
        // Điều này giúp loại bỏ các file âm thanh khác như nhạc chuông, thông báo...
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        // Mệnh đề 'SELECT' của câu truy vấn: chỉ định các cột (thông tin) chúng ta muốn lấy.
        String[] projection = {
                MediaStore.Audio.Media.DATA,       // Đường dẫn đầy đủ của file
                MediaStore.Audio.Media.TITLE,      // Tiêu đề bài hát
                MediaStore.Audio.Media.ARTIST,     // Tên nghệ sĩ
                MediaStore.Audio.Media.DURATION    // Thời lượng bài hát
        };

        // Sử dụng try-with-resources để đảm bảo Cursor được đóng tự động sau khi dùng xong.
        try (Cursor cursor = context.getContentResolver().query(collection, projection, selection, null, null)) {
            if (cursor != null) {
                // Lấy chỉ số (index) của từng cột để tăng hiệu suất khi lặp.
                int pathColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

                // Lặp qua từng hàng (từng bài hát) trong kết quả truy vấn.
                while (cursor.moveToNext()) {
                    // Lấy dữ liệu từ các cột của hàng hiện tại.
                    String path = cursor.getString(pathColumn);
                    String title = cursor.getString(titleColumn);
                    String artist = cursor.getString(artistColumn);
                    long duration = cursor.getLong(durationColumn);

                    // Tạo một đối tượng Song mới với dữ liệu vừa lấy được.
                    Song song = new Song(path, title, artist, duration, 0); // Giả sử playCount ban đầu là 0
                    // Thêm bài hát vào danh sách.
                    songList.add(song);
                }
            }
        }

        // Trả về danh sách các bài hát đã tìm thấy.
        return songList;
    }


    /*
     * CÁC PHƯƠNG THỨC LẤY NHẠC TỪ API ONLINE (Ví dụ)
     * Các phương thức này hiện không được dùng trong HomeFragment, nhưng là ví dụ về cách lấy nhạc từ mạng.
     */

    /**
     * Lấy danh sách bài hát từ API của Deezer (ví dụ).
     * @return Danh sách bài hát.
     */
    public static List<Song> getAllSongsFromDeezer() {
        List<Song> songList = new ArrayList<>();
        try {
            // 1. Tạo URL tới API.
            URL url = new URL("https://api.deezer.com/search?q=nhac%20tre&output=json");
            // 2. Mở kết nối HTTP.
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // 3. Đọc dữ liệu trả về từ API.
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            // 4. Phân tích (parse) chuỗi JSON.
            JSONObject jsonResponse = new JSONObject(jsonBuilder.toString());
            JSONArray dataArray = jsonResponse.getJSONArray("data");

            // 5. Lặp qua kết quả và tạo đối tượng Song.
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject item = dataArray.getJSONObject(i);

                String title = item.getString("title");
                String artist = item.getJSONObject("artist").getString("name");
                String previewUrl = item.getString("preview"); // Deezer cung cấp link preview 30s
                long duration = 30_000; // Thời lượng là 30 giây (30000ms)

                Song song = new Song(previewUrl, title, artist, duration, 0);
                songList.add(song);
            }
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console để debug.
        }
        return songList;
    }

    /**
     * Lấy danh sách bài hát từ API của Jamendo (ví dụ).
     * @return Danh sách bài hát.
     */
    public static List<Song> getAllSongsFromJamendo() {
        List<Song> songList = new ArrayList<>();
        String clientId = "99d59977"; // Client ID để xác thực với API Jamendo.
        try {
            // 1. Xây dựng URL API với các tham số cần thiết.
            String apiUrl = "https://api.jamendo.com/v3.0/tracks/?" +
                    "client_id=" + clientId +
                    "&format=json&limit=20&fuzzytags=pop&audioformat=mp32";

            URL url = new URL(apiUrl);
            // 2. Mở kết nối.
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // 3. Đọc dữ liệu trả về.
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            // 4. Phân tích chuỗi JSON.
            JSONObject jsonResponse = new JSONObject(jsonBuilder.toString());
            JSONArray dataArray = jsonResponse.getJSONArray("results");

            // 5. Lặp qua kết quả và tạo đối tượng Song.
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject item = dataArray.getJSONObject(i);

                String title = item.getString("name");
                String artist = item.getString("artist_name");
                String audio = item.getString("audio"); // Link MP3 đầy đủ
                String image = item.getString("album_image"); // Link ảnh bìa
                int duration = item.getInt("duration"); // Thời lượng (tính bằng giây)

                // Tạo đối tượng Song
                Song song = new Song(audio, title, artist, (long) duration * 1000, 0); // Chuyển sang mili giây
                song.setImgUrl(image); // Gán thêm URL ảnh bìa
                songList.add(song);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return songList;
    }
}