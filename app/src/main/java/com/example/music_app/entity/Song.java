// Xác định gói (package) nơi lớp này tồn tại.
// Gói giúp tổ chức code thành các nhóm logic, ví dụ, 'entity' chứa các lớp định nghĩa cấu trúc dữ liệu.
package com.example.music_app.entity;

// Nhập các lớp Annotation cần thiết từ thư viện AndroidX Room.
// Annotation là các "thẻ đánh dấu" đặc biệt cung cấp thông tin cho trình biên dịch hoặc thư viện.
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/*
 * Annotation @Entity đánh dấu lớp POJO (Plain Old Java Object) này là một bảng trong cơ sở dữ liệu.
 * Room sẽ sử dụng lớp này để tạo ra một bảng SQL.
 *
 * Thuộc tính `tableName = "songs"` cho phép chúng ta đặt một tên tùy chỉnh cho bảng.
 * Nếu không có thuộc tính này, tên bảng sẽ mặc định là tên lớp (tức là "Song").
 * Việc đặt tên rõ ràng giúp tách biệt tên lớp trong Java và tên bảng trong CSDL.
 */
@Entity(tableName = "songs")
public class Song {

    /*
     * @PrimaryKey: Đây là annotation quan trọng nhất, chỉ định rằng trường 'path' là KHÓA CHÍNH của bảng.
     * Mỗi hàng trong bảng phải có một khóa chính duy nhất.
     * CSDL sử dụng khóa chính để tìm kiếm, cập nhật và xóa các hàng một cách cực kỳ hiệu quả.
     * Đối với ứng dụng nhạc, 'path' (đường dẫn file) là một lựa chọn hợp lý làm khóa chính cho nhạc offline,
     * vì không thể có hai file nhạc tồn tại ở cùng một đường dẫn chính xác.
     *
     * @NonNull: Annotation này là một ràng buộc, đảm bảo rằng giá trị của cột này trong CSDL không bao giờ được phép là NULL.
     * Nếu bạn cố gắng chèn một đối tượng Song với 'path' là null, Room sẽ báo lỗi.
     */
    @PrimaryKey
    @NonNull
    private String path;

    // Các trường private này sẽ được Room tự động ánh xạ thành các cột trong bảng "songs".
    // Tên cột sẽ trùng với tên trường theo mặc định.
    private String title;     // Cột "title" kiểu TEXT, lưu tên bài hát.
    private String artist;    // Cột "artist" kiểu TEXT, lưu tên nghệ sĩ.
    private long duration;    // Cột "duration" kiểu INTEGER, lưu thời lượng bài hát (thường tính bằng mili giây).
    private int playCount;    // Cột "playCount" kiểu INTEGER, lưu số lần bài hát đã được phát.

    /*
     * @Ignore: Đây là một annotation rất hữu ích, báo cho Room hãy "làm lơ" hoàn toàn trường này.
     * Trường được đánh dấu @Ignore sẽ KHÔNG được tạo thành một cột trong CSDL
     * và Room sẽ không cố gắng đọc hay ghi dữ liệu cho nó.
     *
     * TẠI SAO LẠI CẦN?
     * Nó dùng cho các dữ liệu chỉ có ý nghĩa tạm thời khi ứng dụng đang chạy (transient state).
     * Ví dụ: `isFavorite`. Trạng thái "yêu thích" phụ thuộc vào người dùng đang đăng nhập.
     * Thay vì lưu trực tiếp vào bảng "songs", cách thiết kế tốt là có một bảng riêng ("favorites")
     * để lưu cặp (userId, songPath). Khi hiển thị, ta sẽ truy vấn và gán trạng thái `true/false`
     * cho trường này. Vì vậy, `isFavorite` không phải là một thuộc tính cố hữu của bài hát,
     * mà là một trạng thái tạm thời liên quan đến người dùng.
     */
    @Ignore
    private boolean isFavorite;

    /*
     * Trường này sẽ được tạo thành cột "imgUrl" trong bảng "songs".
     * Mặc dù nó không nằm trong constructor chính mà Room sử dụng, nhưng vì nó không bị đánh dấu @Ignore,
     * Room vẫn sẽ cố gắng lưu và đọc nó từ CSDL. Room sẽ tìm một setter public (setImgUrl) hoặc
     * truy cập trực tiếp nếu trường là public để gán giá trị.
     */
    private String imgUrl;


    /* =================================================================================
     * PHẦN CONSTRUCTOR (Phương thức khởi tạo)
     * =================================================================================
     * Room cần biết cách tạo ra một đối tượng 'Song' từ dữ liệu nó đọc được trong một hàng của CSDL.
     * Nó sẽ tự động tìm một constructor phù hợp để làm việc này.
     */

    /**
     * CONSTRUCTOR CHÍNH DÀNH CHO ROOM.
     * Khi bạn thực hiện một câu lệnh SELECT, Room sẽ lấy dữ liệu từ các cột tương ứng
     * và gọi constructor này để "tái tạo" lại đối tượng Song trong bộ nhớ Java.
     * Nó phải chứa các tham số tương ứng với các cột trong bảng mà bạn muốn khởi tạo.
     */
    public Song(@NonNull String path, String title, String artist, long duration, int playCount) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.playCount = playCount;
    }

    /*
     * @Ignore trên constructor này báo cho Room bỏ qua nó.
     * Đây được gọi là CONSTRUCTOR TIỆN ÍCH, dành cho lập trình viên sử dụng.
     *
     * VÍ DỤ SỬ DỤNG:
     * Khi bạn lấy dữ liệu từ một API online (như Jamendo, Deezer), dữ liệu trả về có thể có cấu trúc khác.
     * Constructor này cho phép bạn tạo một đối tượng Song hoàn chỉnh từ dữ liệu API đó chỉ trong một bước,
     * bao gồm cả các trường bị @Ignore như `isFavorite` hay `imgUrl`, giúp code của bạn sạch sẽ hơn.
     */
    @Ignore
    public Song(String pathOrUrl, String title, String artist, long duration, boolean isFavorite, int playCount, String imgUrl) {
        this.path = pathOrUrl;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.isFavorite = isFavorite;
        this.playCount = playCount;
        this.imgUrl = imgUrl;
    }

    /**
     * Một constructor tiện ích khác, cũng được Room bỏ qua.
     * Nó gọi lại constructor chính thông qua `this(...)` để tránh lặp lại code,
     * sau đó chỉ gán thêm giá trị cho trường bị @Ignore.
     */
    @Ignore
    public Song(@NonNull String path, String title, String artist, long duration, boolean isFavorite, int playCount) {
        this(path, title, artist, duration, playCount);
        this.isFavorite = isFavorite;
    }

    /* =================================================================================
     * PHẦN GETTER VÀ SETTER
     * =================================================================================
     * Đây là các phương thức tuân thủ nguyên tắc Đóng gói (Encapsulation) trong Lập trình hướng đối tượng (OOP).
     * Các trường dữ liệu được để là 'private' để không thể truy cập trực tiếp từ bên ngoài.
     * Thay vào đó, việc truy cập (get) và thay đổi (set) giá trị phải thông qua các phương thức public này.
     * Điều này cho phép kiểm soát và xác thực dữ liệu nếu cần.
     */

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @NonNull
    public String getPath() {
        return path;
    }

    public void setPath(@NonNull String path) {
        this.path = path;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}