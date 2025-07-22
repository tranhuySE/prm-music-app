// Xác định gói (package) nơi file này tồn tại.
package com.example.music_app.DAO;

// Nhập các Annotation cần thiết từ thư viện Room.
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

// Nhập lớp Entity 'Song' để có thể làm việc với nó.
import com.example.music_app.entity.Song;

// Nhập lớp List để xử lý danh sách.
import java.util.List;

/**
 * Annotation @Dao đánh dấu interface này là một Data Access Object.
 * Room sẽ tự động tạo một lớp triển khai (implementation) cho interface này
 * trong quá trình biên dịch. Bạn chỉ cần định nghĩa các phương thức trừu tượng
 * và chú thích chúng bằng các annotation của Room.
 */
@Dao
public interface SongDao {

    /**
     * @Insert: Annotation này đánh dấu phương thức dùng để chèn dữ liệu vào CSDL.
     *
     * onConflict = OnConflictStrategy.REPLACE: Đây là phần quan trọng, chỉ định chiến lược
     * xử lý khi có xung đột dữ liệu. Xung đột xảy ra khi bạn cố gắng chèn một hàng có
     * khóa chính (PrimaryKey) đã tồn tại trong bảng.
     * `REPLACE` có nghĩa là: Hàng cũ sẽ bị xóa và hàng mới sẽ được chèn vào thay thế.
     * Điều này rất hữu ích khi bạn muốn cập nhật thông tin một bài hát bằng cách chèn lại nó.
     *
     * @param songs: Danh sách các đối tượng Song cần chèn. Room đủ thông minh để
     * xử lý việc chèn cả một danh sách trong một giao dịch (transaction) hiệu quả.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Song> songs);

    /**
     * @Query: Annotation này cho phép bạn viết các câu lệnh SQL tùy chỉnh để đọc hoặc ghi dữ liệu.
     *
     * "SELECT * FROM songs": Đây là câu lệnh SQL.
     * - SELECT *: Chọn tất cả các cột.
     * - FROM songs: Từ bảng có tên là "songs".
     *
     * @return List<Song>: Room sẽ thực thi câu lệnh SELECT, sau đó tự động ánh xạ
     * mỗi hàng kết quả thành một đối tượng `Song` và trả về một danh sách các đối tượng đó.
     */
    @Query("SELECT * FROM songs")
    List<Song> getAllSongs();

    /**
     * @Query: Sử dụng @Query cho cả các lệnh UPDATE hoặc DELETE.
     *
     * "UPDATE songs SET playCount = playCount + 1 WHERE path = :path": Câu lệnh SQL để cập nhật.
     * - UPDATE songs: Cập nhật bảng "songs".
     * - SET playCount = playCount + 1: Đặt giá trị của cột "playCount" bằng giá trị hiện tại của nó cộng thêm 1.
     * - WHERE path = :path: Chỉ thực hiện việc cập nhật này trên hàng nào có cột "path" khớp với giá trị được cung cấp.
     *
     * :path: Đây là một tham số ràng buộc (bind parameter). Giá trị của nó sẽ được lấy từ
     * tham số `path` của phương thức `increasePlayCount` khi nó được gọi.
     *
     * @param path: Đường dẫn của bài hát có lượt nghe cần tăng.
     */
    @Query("UPDATE songs SET playCount = playCount + 1 WHERE path = :path")
    void increasePlayCount(String path);

    /**
     * @Query: Một câu lệnh SELECT phức tạp hơn.
     *
     * "SELECT * FROM songs ORDER BY playCount DESC LIMIT :limit": Câu lệnh SQL.
     * - SELECT * FROM songs: Chọn tất cả bài hát.
     * - ORDER BY playCount DESC: Sắp xếp kết quả theo cột "playCount". `DESC` (descending) có nghĩa là
     * sắp xếp giảm dần, vì vậy bài hát có lượt nghe cao nhất sẽ ở trên cùng.
     * - LIMIT :limit: Giới hạn số lượng hàng trả về. Ví dụ, nếu `limit` là 5, nó sẽ chỉ trả về 5 bài hát đầu tiên
     * sau khi đã sắp xếp.
     *
     * :limit: Tham số ràng buộc được lấy từ tham số `limit` của phương thức.
     *
     * @param limit: Số lượng bài hát top-hit mà bạn muốn lấy.
     * @return List<Song>: Trả về một danh sách các bài hát được nghe nhiều nhất.
     */
    @Query("SELECT * FROM songs ORDER BY playCount DESC LIMIT :limit")
    List<Song> getTopSongs(int limit);
}