# Quy Định Viết Code  Làm Việc Nhóm

Tất cả thành viên **BẮT BUỘC** đọc kỹ và tuân thủ các quy định dưới đây để code đồng nhất, dễ đọc, dễ fix bug và giao diện không bị vỡ trên các máy khác nhau.

## 1. Quy định về thiết kế Giao diện (GUI) - RẤT QUAN TRỌNG
Tuyệt đối tuân thủ việc co giãn nhé, vì độ phân giải màn hình của mỗi người là khác nhau

*   **KHÔNG sử dụng Absolute Layout:** Tuyệt đối **không** dùng `setLayout(null)` và **không** dùng `setBounds(x, y, width, height)` để gán cứng vị trí component
*   **Dùng Layout Manager:** Bắt buộc sử dụng `BorderLayout`, `GridLayout`, `BoxLayout`, hoặc `GridBagLayout` để các Panel và Button tự động co giãn theo cửa sổ
*   **Kích thước tương đối:** Dùng `setPreferredSize()`, `setMinimumSize()` thay vì fix cứng kích thước. Khung Frame chính phải dùng `pack()` và `setLocationRelativeTo(null)` để căn giữa
*   **Font và Màu sắc:** Không set font/color rải rác từng nút. Gom chung vào một file cấu hình hoặc để thư viện `FlatLaf` tự động quản lý ()
## 2. Quy định về viết Code Logic (BUS/SERVICE & DAO)
*   **Tuân thủ uyệt Đối:**
    *   **GUI:** Chứa nút bấm, bảng, sự kiện click. Tuyệt đối KHÔNG có câu lệnh `SELECT`, `INSERT` ở đây. Lấy dữ liệu qua BUS
    *   **BUS:** Nhận dữ liệu từ GUI, kiểm tra điều kiện (IF-ELSE, check rỗng, tính toán, xử lí)
    *   **DAO:** Chỉ chứa lệnh SQL để giao tiếp với DB và thao tác mảng
*   **Xử lý Ngoại lệ (Exception):**
    *   Tầng DAO nếu lỗi SQL thì `throw exception` lên tầng BUS.
    *   Tầng BUS bắt lỗi và `throw` câu thông báo tiếng Việt lên GUI.
    *   Tầng GUI dùng `JOptionPane` để hiện thông báo lỗi cho người dùng. (Code logic không được gọi trực tiếp `JOptionPane`).
*   **Quy tắc đặt tên:**
    *   Tên Class: `PascalCase` (Ví dụ: `HoaDonService`, `ChiTietHoaDonDAO`).
    *   Tên iến vààm: `camelCase` (Ví dụ: `tinhTongTien()`, `soLuongTon`).
    *   Tên hằng số và ENUM: `UPPER_SNAKE_CASE` (Ví dụ: `MAX_DISCOUNT_RATE = 0.5`, `DANG_THUC_HIEN`).

## 3. Quy trình làm việc với Git
Này hồi ý anh em nếu đơn giản thì không cần phân nhánh, muốn ít bị xung đột conflit thì phân ra
1.  **Anh ba đen tao** đây khởi tạo dự án, thiết lập khung và đẩy lên nhánh `main`
2.  **Tuyệt đối KHÔNG code trực tiếp trên nhánh `main`.**
3.  Khi bắt đầu làm chức năng của mình, hãy tạo nhánh mới từ `main`:
    `git checkout -b feature/<ten-chuc-nang>-<ten-nguoi-lam>`
    *(Ví dụ: `git checkout -b ThongKe/Huong_Deo`)*
4.  Trong quá trình làm, commit code thường xuyên với thông điệp rõ ràng:
    *(Ví dụ: `git commit -m "feat: hoàn thiện logic tính tiền hóa đơn"`)*
5.  Khi hoàn thành, tiến hành tạo **Pull Request (PR)** trên GitHub. Tao sẽ review code. Nếu logic ổn, code sạch, không vỡ UI, tao sẽ duyệt Merge vào nhánh `main`.
6.  Trước khi code tiếp phần mới, luôn nhớ chạy `git pull origin main` để cập nhật code mới nhất từ anh em
7.  Các tiền tố commit:
   - `feat`: thêm chức năng
   - `fix`: sửa lỗi
   - `style`: chỉnh sửa giao diện
   - `refactor`: tối ưu code
   - `chore`: thêm mấy cái file bal bla tào lao, file cấu hình đồ
   - `docs`: thêm tài liệu, file hướng dẫn rồi thêm vào thư mục docs 