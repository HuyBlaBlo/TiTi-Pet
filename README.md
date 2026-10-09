# TiTiPet - Hệ Thống Quản Lý Cửa Hàng Thú Cưng và Spa

## Yêu cầu

- **Ngôn ngữ:** Java
- **Môi trường:** **JDK 25** (Chưa cài JDK 25 thì cài đi nha anh em)
- **Giao diện:** Java Swing + thư viện FlatLaf
- **CSDL:** Microsoft SQL Server (JDBC)

## Cấu trúc cây thư mục (Kiến trúc 3 lớp)

```
└── 📁TiTiPet
    └── 📁database
        ├── 01_create_tables.sql                        # DDL: Tạo bảng + PK/FK
        ├── 02_constraints.sql                          # CHECK, UNIQUE, NOT NULL (Nói chubng là để check ấy, này tao thêm kiểu về bảo mật hơn thôi)
        ├── 03_seed_data.sql                            # Dữ liệu mẫu (Nhân viên, Bảng giá Spa, Sản phẩm)
        ├── 04_procedures_triggers.sql                  # Trigger tự động trừ kho, tính điểm (Cái này cũng vậy)
    └── 📁docs
    └── 📁resources
        └── 📁icons                                     # Icon nút bấm, logo TiTi Pet
        └── 📁images                                    # Ảnh
        └── 📁reports                                   # Template in hóa đơn, nếu muốn xuất hóa đơn
    └── 📁src
        └── 📁com
            └── 📁titipet
                └── 📁bus                               # --- TẦNG 2: BUSINESS LOGIC / SERVICE ---
                    ├── AuthService.java
                    ├── BanHangService.java             # Đăng nhập, phân quyền
                    ├── ChotCaService.java              # Đối soát doanh thu tiền mặt/CK
                    ├── DoiTraService.java              # Kiểm tra điều kiện đổi trả
                    ├── KhachHangService.java           # Quản lý điểm, tính nâng cấp VIP (3%, 4%, 5%)
                    ├── KhoService.java                 # Cảnh báo cận date, sắp hết hàng
                    ├── LichHenService.java             # Check trùng lịch, hủy trước 1.5h
                    ├── SpaService.java                 # Tính giá Spa theo cân nặng, xếp lịch thợ
                    └── HoaDonService.java              # Gọi cả 2 DAO trên trong 1 transaction
                └── 📁connectDB
                    ├── ConnectDB.java                  # Kết nối với dbdb
                └── 📁dao
                    ├── CaLamViecDAO.java               # --- TẦNG 3: DATA ACCESS OBJECT (JDBC) ---
                    ├── ChiTietHoaDon_DichVuDAO.java
                    ├── ChiTietHoaDon_SanPhamDAO.java
                    ├── DichVuSpaDAO.java
                    ├── HoaDonDAO.java
                    ├── KhachHangDAO.java
                    ├── KhuyenMaiDAO.java
                    ├── LichHenDAO.java
                    ├── NhanVienDAO.java
                    ├── PhieuKetCaDAO.java
                    ├── SanPhamDAO.java
                    ├── ThuCungDAO.java
                └── 📁entity
                    └── 📁enums                         # --- ENTITY/DTO ---
                        ├── HangThanhVien.java
                        ├── PhuongThucThanhToan.java
                        ├── TrangThaiLichHen.java
                        └── VaiTro.java
                    ├── CaLamViec.java
                    ├── ChiTietHoaDon_DichVu.java
                    ├── ChiTietHoaDon_SanPham.java
                    ├── DichVuSpa.java
                    ├── HoaDon.java
                    ├── KhachHang.java
                    ├── KhuyenMai.java
                    ├── LichHen.java
                    ├── NhanVien.java
                    ├── PhieuKetCa.java
                    ├── PhuKien.java                    # Extend SanPham
                    ├── SanPham.java                    # Abstract class
                    ├── ThucAn.java                     # Extend SanPham
                    ├── ThuCung.java
                └── 📁gui                               # --- TẦNG 1: PRESENTATION ---
                    └── 📁component                     # Custom UI (Table, buttone, kiểu mấy cái dùng dùng tạo ra dùng đi dùng lại cho nó đồng bộ ấy)
                    └── 📁dialog                        #Mấy cửa số pop-up hiện lên để thêm
                        ├── ThemKhachHangDialog.java
                        ├── ThemThuCungDialog.java
                        ├── ChonKhuyenMaiDialog.java
                        ├──InHoaDonDialog.java
                    └── 📁panel
                        ├── BanHangPanel.java           # Quầy Thu ngân: Bán hàng + quét mã vạch
                        ├── ChotCaPanel.java            # Báo cáo doanh thu & chốt ca
                        ├── DichVuSpaPanel.java         # Quản lý bảng giá dịch vụ Spa
                        ├── DoiTraPanel.java            # Xử lý đổi trả hàng
                        ├── KhachHangPanel.java         # Quản lý khách hàng + Thẻ tích điểm
                        ├── KhoPanel.java               # Nhập kho và cảnh báo hạn sử dụng
                        ├── KhuyenMaiPanel.java         # Thiết lập chương trình ưu đãi
                        ├── LichHenPanel.java           # Quản lý và đặt lịch hẹn
                        ├── LichThoSpaPanel.java        # Màn hình dành riêng cho Thợ Spa
                        ├── SanPhamPanel.java           # Quản lý danh mục sản phẩm
                        ├── ThongKePanel.java           # Thống kê doanh thu, tồn kho
                        ├── ThuCungPanel.java           # Hồ sơ thú cưng
                        └── TiepNhanSpaPanel.java       # Quầy Thu ngân: Cân pet + Tiếp nhận Spa
                    ├── LoginFrame.java
                    ├── MainFrame.java
                └── 📁main
                    ├── Main.java
                └── 📁util
                    ├── FormatUtil.java                 # Format VND (100.000đ), LocalDateTime
                    ├── PasswordUtil.java               # Định dạng pasword
                    ├── RoundedPannel.java              #Bo trmàuòn các pannel, truyền vào là màu và độ bo tròn
                    ├── RoundedPannelLinear.java        # Bo tròn pannel truyền vào 2 màu linear (loanmg màu)
    ├── .classpath
    ├── .gitignore
    ├── .project
    ├── CONTRIBUTING.md
    ├── README.md
    └── TiTiPet.iml
```

## Phân công nhiệm vụ

Để tránh xung đột code (Merge Conflict) khi đẩy lên GitHub, làm **từ DAO -> BUS -> GUI** cho module mình phụ trách. Không ai đụng vào form hay file DAO của người khác.

**1.Anh Ba Đen**

- **Nhiệm vụ:** Setup bộ khung dự án ban đầu (Base code, ConnectDB, MainFrame).
- **Module phụ trách:** Chức năng Đăng nhập, Phân quyền, Thống kê doanh thu (Dashboard) và Quản lý nhân viên/ca trực.
- **Note:** duyệt code (Review Pull Request) cuối cùng trước khi gộp vào nhánh `main`.

**2.Chị Da Vàng**

- **Module phụ trách:** Quy trình Dịch vụ Spa.
- **Chi tiết:** Quản lý Lịch hẹn (LichHenDAO/BUS/Panel), Tiếp nhận thú cưng (chọn thợ, cân nặng), Cập nhật trạng thái Spa, Danh mục giá dịch vụ.

**3. Anh Da Trắng**

- **Module phụ trách:** Bán hàng và Thu ngân.
- **Chi tiết:** Lập Hóa đơn (gộp sản phẩm + dịch vụ Spa), Tính tiền, Chiết khấu, Đổi trả hàng, Chốt ca (kết sổ doanh thu).

**4. Chị Da Đỏ**

- **Module phụ trách:** Khách hàng, Thú cưng & Kho.
- **Chi tiết:** Quản lý thông tin Khách hàng (Thẻ tích điểm), Hồ sơ Thú cưng, Quản lý Nhập/Xuất kho sản phẩm, Khuyến mãi.

## Hướng dẫn Setup chạy dự án

1.  **Clone code:** `git clone https://github.com/HuyBlaBlo/TiTi-Pet.git` nhét cái link dự án nhóm vào.
2.  **Cài đặt DB:** Mở thư mục `database/`, chạy tuần tự 4 file SQL trên SSMS or Azure or DBeaver, thích dùng chó gì thì dùng
3.  **Cấu hình IDE (IntelliJ / Eclipse):**
    - Vào `Project Structure` -> Kiểm tra đúng môi trường **JDK 25**.
    - Quét chọn tất cả file `.jar` trong thư mục `lib/` -> Chọn **Add as Library** (hoặc _Add to Build Path_).
4.  Vào class `ConnectDB`, sửa lại thông tin tài khoản SQL Server của máy (Mật khẩu rồi tên đăng nhập).
5.  Chạy class `Main.java` để khởi động ứng dụng
