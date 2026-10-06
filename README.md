# TiTiPet - Hệ Thống Quản Lý Cửa Hàng Thú Cưng & Spa

Dự án phần mềm quản lý nội bộ cửa hàng thú cưng TiTiPet. Phát triển bằng Java Swing thuần, kiến trúc 3 lớp, thao tác với CSDL SQL Server qua JDBC.

## Yêu cầu hệ thống (Tech Stack)
*   **Ngôn ngữ:** Java 
*   **Môi trường:** **JDK 25**
*   **Giao diện:** Java Swing + thư viện FlatLaf
*   **CSDL:** Microsoft SQL Server (JDBC)

## Cấu trúc cây thư mục (Kiến trúc 3 lớp)
```
TiTiPet/
├── lib/                                    # Thư mục chứa các thư viện .jar tải về
│   ├── mssql-jdbc-12.x.x.jre17.jar         # Driver kết nối Microsoft SQL Server
│   ├── flatlaf-3.x.jar                     # Thư viện giao diện Swing hiện đại
│   ├── jcalendar-1.4.jar                   # Component chọn ngày tháng cho Swing
│   └── .....   
├── README.md
├── .gitignore
├── database/
│   ├── 01_create_tables.sql                # DDL: Tạo bảng + PK/FK
│   ├── 02_constraints.sql                  # CHECK, UNIQUE, NOT NULL (Nói chubng là để check ấy, này tao thêm kiểu về bảo mật hơn thôi)
│   ├── 03_seed_data.sql                    # Dữ liệu mẫu (Nhân viên, Bảng giá Spa, Sản phẩm)
│   └── 04_procedures_triggers.sql         # Trigger tự động trừ kho, tính điểm (Cái này cũng vậy)
├── docs/                                   # Sơ đồ UML, EER, báo cáo đồ án
└── src/
├── com/titipet/
│   ├── main/
│   │   └── Main.java                   # Khởi chạy ứng dụng với LookAndFeel của FlatLaf nhìn cho đẹp
│   │
│   ├── connectDB/
│   │   └── ConnectDB.java              # Quản lý kết nối JDBC SQL Server
│   │
│   ├── entity/                         # --- ENTITY/DTO --- (DTO là data tranfer object là đối tượng trả về cho giao diện đó, nay không cần cũng được)
│   │   ├── NhanVien.java
│   │   ├── KhachHang.java
│   │   ├── ThuCung.java
│   │   ├── SanPham.java                # Abstract class
│   │   ├── ThucAn.java                 # Extends SanPham
│   │   ├── PhuKien.java                # Extends SanPham
│   │   ├── DichVuSpa.java
│   │   ├── LichHen.java
│   │   ├── HoaDon.java
│   │   ├── ChiTietHoaDon.java          # Này tao nghĩ là cần tách ra vì mình có nhắc đến hóa đơn gộp của sản phẩm và hóa đơn ấy
│   │   ├── KhuyenMai.java
│   │   ├── CaLamViec.java
│   │   ├── PhieuKetCa.java
│   │   └── enums/
│   │       ├── VaiTro.java             # THU_NGAN, THO_SPA, QUAN_LY
│   │       ├── TrangThaiLichHen.java   # CHO_TIEP_NHAN, DA_TIEP_NHAN, DANG_LAM, HOAN_THANH, DA_HUY
│   │       ├── HangThanhVien.java      # DONG, BAC, VANG
│   │       └── PhuongThucThanhToan.java # TIEN_MAT, CHUYEN_KHOAN
│   │
│   ├── dao/                            # --- TẦNG 3: DATA ACCESS OBJECT (JDBC) ---
│   │   ├── NhanVienDAO.java
│   │   ├── KhachHangDAO.java
│   │   ├── ThuCungDAO.java
│   │   ├── SanPhamDAO.java
│   │   ├── DichVuSpaDAO.java
│   │   ├── LichHenDAO.java
│   │   ├── HoaDonDAO.java
│   │   ├── ChiTietHoaDonDAO.java
│   │   ├── KhuyenMaiDAO.java
│   │   ├── CaLamViecDAO.java
│   │   └── PhieuKetCaDAO.java
│   │
│   ├── bus/                            # --- TẦNG 2: BUSINESS LOGIC / SERVICE ---
│   │   ├── AuthService.java            # Đăng nhập, phân quyền, lưu session
│   │   ├── BanHangService.java         # Lập bill, tính tiền, trừ kho, tích điểm VIP
│   │   ├── SpaService.java             # Tính giá Spa theo cân nặng, xếp lịch thợ
│   │   ├── LichHenService.java         # Check trùng lịch, hủy trước 1.5h
│   │   ├── KhachHangService.java       # Quản lý điểm, tính nâng cấp VIP (3%, 4%, 5%)
│   │   ├── DoiTraService.java          # Kiểm tra điều kiện đổi trả
│   │   ├── KhoService.java             # Cảnh báo cận date, sắp hết hàng
│   │   └── ChotCaService.java          # Đối soát doanh thu tiền mặt/CK
│   │
│   ├── gui/                            # --- TẦNG 1: PRESENTATION ---
│   │   ├── LoginFrame.java             # Màn hình đăng nhập
│   │   ├── MainFrame.java              # Main Dashboard
│   │   ├── panel/
│   │   │   ├── BanHangPanel.java        # Quầy Thu ngân: Bán hàng + Quét mã vạch
│   │   │   ├── TiepNhanSpaPanel.java    # Quầy Thu ngân: Cân pet + Tiếp nhận Spa
│   │   │   ├── LichHenPanel.java        # Quản lý & đặt lịch hẹn
│   │   │   ├── KhachHangPanel.java      # Quản lý khách hàng + Thẻ tích điểm
│   │   │   ├── ThuCungPanel.java        # Hồ sơ thú cưng
│   │   │   ├── SanPhamPanel.java        # Quản lý danh mục sản phẩm
│   │   │   ├── KhoPanel.java            # Nhập kho & cảnh báo hạn sử dụng
│   │   │   ├── DichVuSpaPanel.java      # Quản lý bảng giá dịch vụ Spa
│   │   │   ├── KhuyenMaiPanel.java      # Thiết lập chương trình ưu đãi
│   │   │   ├── DoiTraPanel.java         # Xử lý đổi trả hàng
│   │   │   ├── ChotCaPanel.java         # Báo cáo doanh thu & chốt ca
│   │   │   ├── ThongKePanel.java        # Thống kê doanh thu, tồn kho
│   │   │   └── LichThoSpaPanel.java     # Màn hình dành riêng cho Thợ Spa
│   │   ├── dialog/
│   │   │   ├── ThemKhachHangDialog.java
│   │   │   ├── ThemThuCungDialog.java
│   │   │   ├── ChonKhuyenMaiDialog.java
│   │   │   └── InHoaDonDialog.java
│   │   └── component/                  # Custom UI Components (Table, Button... kiểu là các cái dùng chung dùng đi dùng lại, mình làm ra đây để tái sử ) 
│   │
│   └── util/                           # --- TIỆN ÍCH HỖ TRỢ ---
│       ├── FormatUtil.java             # Format VND (100.000đ), LocalDateTime
│       ├── PasswordUtil.java           # Hash mật khẩu
│       ├── RoundedPanel.java           # Bo tròn các panel
│       └── SessionManager.java         # Lưu thông tin nhân viên đăng nhập
│
└── resources/                          # --- ASSETS ---
        ├── icons/                          # Icon nút bấm, logo TiTi Pet
        ├── images/                          # Ảnh
        └── reports/                        # Template in hóa đơn / Báo cáo
```
## Phân công nhiệm vụ
Để tránh xung đột code (Merge Conflict) khi đẩy lên GitHub, làm **từ DAO -> BUS -> GUI** cho module mình phụ trách. Không ai đụng vào form hay file DAO của người khác.

**1.Anh Ba Đen**
*   **Nhiệm vụ:** Setup bộ khung dự án ban đầu (Base code, ConnectDB, MainFrame).
*   **Module phụ trách:** Chức năng Đăng nhập, Phân quyền, Thống kê doanh thu (Dashboard) và Quản lý nhân viên/ca trực.
*   **Note:**  duyệt code (Review Pull Request) cuối cùng trước khi gộp vào nhánh `main`.

**2.Chị Da Vàng**
*   **Module phụ trách:** Quy trình Dịch vụ Spa.
*   **Chi tiết:** Quản lý Lịch hẹn (LichHenDAO/BUS/Panel), Tiếp nhận thú cưng (chọn thợ, cân nặng), Cập nhật trạng thái Spa, Danh mục giá dịch vụ.

**3. Anh Da Trắng**
*   **Module phụ trách:** Bán hàng và Thu ngân.
*   **Chi tiết:** Lập Hóa đơn (gộp sản phẩm + dịch vụ Spa), Tính tiền, Chiết khấu, Đổi trả hàng, Chốt ca (kết sổ doanh thu).

**4. Chị Da Đỏ**
*   **Module phụ trách:** Khách hàng, Thú cưng & Kho.
*   **Chi tiết:** Quản lý thông tin Khách hàng (Thẻ tích điểm), Hồ sơ Thú cưng, Quản lý Nhập/Xuất kho sản phẩm, Khuyến mãi.

## Hướng dẫn Setup chạy dự án
1.  **Clone code:** `git clone <link-repo>` nhét cái link dự án nhóm vào.
2.  **Cài đặt DB:** Mở thư mục `database/`, chạy tuần tự 4 file SQL trên SSMS or Azure or DBeaver, thích dùng chó gì thì dùng 
3.  **Cấu hình IDE (IntelliJ / Eclipse):**
    *   Vào `Project Structure` -> Kiểm tra đúng môi trường **JDK 25**.
    *   Quét chọn tất cả file `.jar` trong thư mục `lib/` -> Chọn **Add as Library** (hoặc *Add to Build Path*).
4.  Vào class `ConnectDB`, sửa lại thông tin tài khoản SQL Server của máy (Mật khẩu rồi tên đăng nhập).
5.  Chạy class `Main.java` để khởi động ứng dụng