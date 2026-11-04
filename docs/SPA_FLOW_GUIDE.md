# TiTi Pet – Luồng Tiếp nhận Spa → POS → thợ Spa

## Phạm vi

Được xây dựng theo mã nguồn trong `TiTi-Pet(20261104-031944).rar`. Chỉ dùng **TiTiPetDB** hiện có; không tạo hoặc xóa database. Không thay `ConnectDB.java` và không đính kèm mật khẩu.

### Các màn hình đã kết nối

1. **Trang chủ → Tiếp nhận thú cưng** (mã menu `tiep-nhan-spa`). Thu ngân chọn lịch hẹn hoặc thêm khách vãng lai và thú cưng, cân nặng, tình trạng da/lông, chọn dịch vụ, chọn thợ.
2. **→ Tạo hóa đơn**: `TiepNhanSpaPanel` tạo `TiepNhanSpaContext` (chưa ghi DB) và nhờ `MainFrame` mở `BanHangPanel` với đúng khách, pet, dịch vụ, giá và phụ thu.
3. **→ POS thanh toán**: thu ngân chọn phương thức, xác nhận đã thu đủ 100%. Trong **một transaction** `BanHangGiaoDichDAO` tạo/tiếp nhận lịch hẹn, hóa đơn, chi tiết dịch vụ, mã thợ, trạng thái `CHO_LAM`, điểm và tồn kho (nếu có sản phẩm bán kèm); lỗi là rollback cả giao dịch.
4. **→ Điều phối Spa**: thợ đăng nhập bằng vai trò `THO_SPA`, chỉ xem dịch vụ đã thanh toán và được giao cho mình. Bấm **Bắt đầu làm → Hoàn thành → Đã bàn giao**. Quản lý có thể xem tất cả.

### Cấu trúc lưu trữ

Giữ bảng `LichHen`, `HoaDon`, `ChiTietHoaDon_DichVu` đang có. Bổ sung hai cột vào `ChiTietHoaDon_DichVu`:
- `maLichHen VARCHAR(20) NULL`: khóa ngoại đến `LichHen`, truy vết dịch vụ theo lượt tiếp nhận.
- `trangThaiBanGiao VARCHAR(20) NOT NULL DEFAULT ('CHUA_BAN_GIAO')`: quản lý bàn giao.

Bảng `HoaDon` **không** có cột `maLichHen`. Bộ code trước đó tham chiếu sai cột này. Mọi truy vấn bảng thợ giờ nối `ChiTietHoaDon_DichVu.maLichHen = LichHen.maLichHen` (LEFT JOIN cho hóa đơn Spa lập độc lập, chưa có lịch).

**Lưu ý:** `TiepNhanDAO.themPhieuTiepNhan` là mã cũ trỏ tới `PhieuTiepNhan`/`ChiTietTiepNhan` không có trong schema. Luồng mới **không gọi** phương thức này. Dữ liệu được lưu vào các bảng thực tế ở bước thanh toán.

### Bước 1 – Sao lưu và chạy migration SQL

SSMS kết nối đúng máy SQL Server, chạy `database/10_spa_flow_link.sql` trên **TiTiPetDB** đang có. Script idempotent (có thể chạy lại), không xóa dữ liệu cũ.

Nếu tạo DB mới từ đầu bằng `database/TiTiPet.sql` trong PR này thì hai cột đã được tích hợp, không cần migration thêm; dữ liệu seed từ repository hiện tại được giữ nguyên.

### Bước 2 – Áp dụng mã nguồn

Nếu dùng bản ZIP: giải nén và **chép đúng các file src, database, docs, tests** vào thư mục project trên nhánh feature riêng, không chép đè `ConnectDB.java`.

Nếu dùng bản patch:

```powershell
cd "F:\PTUD\Code_Final\TiTi-Pet"
git status --short
git switch develop
git pull --ff-only origin develop
git switch -c feature/spa/reception-pos-workflow
git apply --check "F:\PTUD\Code_Final\TiTiPet_Spa_ThanhToan_ChuyenTho_PR.patch"
git apply "F:\PTUD\Code_Final\TiTiPet_Spa_ThanhToan_ChuyenTho_PR.patch"
```

**Quan trọng:** Patch kiểm chứng trên đúng bản RAR ngày 04/11. Nếu `develop` mới hơn thì `git apply --check` có thể thất bại; không `--force`, không `reset --hard`. Dùng ZIP để đối chiếu và ghép file theo phiên bản hiện tại hoặc gửi lại code mới.

### Bước 3 – Build

```powershell
$files = @(Get-ChildItem ".\src" -Recurse -Filter "*.java" -File | ForEach-Object { $_.FullName })
New-Item -ItemType Directory -Force out-spa-flow | Out-Null
javac -encoding UTF-8 -cp "lib/*" -d out-spa-flow $files
javac -encoding UTF-8 -cp "out-spa-flow;lib/*" -d out-spa-flow .\tests\com\titipet\tests\TiepNhanSpaFlowSmokeTest.java
java -cp "out-spa-flow;lib/*" com.titipet.tests.TiepNhanSpaFlowSmokeTest
java --enable-native-access=ALL-UNNAMED -cp "out-spa-flow;lib/*" com.titipet.main.Main
```

Nếu không muốn dùng PowerShell nhiều dòng, mỗi lệnh trên có thể chạy độc lập.

### Bước 4 – Chạy thử bằng hai tài khoản

- Thu ngân: phải **Mở ca** trước khi thanh toán (luồng POS hiện có yêu cầu `CaLamViec.trangThai='DANG_MO'`).
- Đăng nhập thu ngân/quản lý → Trang chủ → Tiếp nhận thú cưng → chọn khách có lịch hoặc khách vãng lai → chọn thợ thật có vai trò `THO_SPA` → chọn dịch vụ → Tạo hóa đơn → POS thanh toán tiền mặt/chuyển khoản.
- Đăng xuất, đăng nhập **đúng nhân viên Spa đã chọn**, mở `Điều phối Spa` → thấy thẻ việc `CHO_LAM`; nhấn Bắt đầu làm, Hoàn thành và Đã bàn giao.
- Hóa đơn được tạo khi thanh toán thành công, không phải lúc mới nhấn Tạo hóa đơn. Bấm **Xóa giỏ** trên POS trước khi thanh toán sẽ hủy yêu cầu tiếp nhận, không ghi CSDL.

### SQL xác minh nghiệp vụ

```sql
USE TiTiPetDB;
GO
SELECT TOP (20)
    h.maHoaDon, h.ngayLap, h.trangThai AS trangThaiHoaDon,
    d.maChiTietDichVu, d.maLichHen, d.maThuCung, d.maNVThoSpa,
    d.trangThaiThucHien, d.trangThaiBanGiao
FROM dbo.ChiTietHoaDon_DichVu d
JOIN dbo.HoaDon h ON h.maHoaDon = d.maHoaDon
ORDER BY h.ngayLap DESC, d.maChiTietDichVu DESC;
```

### Lưu ý nghiệp vụ

- Nếu chọn nhiều dịch vụ cho cùng một thú cưng, các dòng chi tiết dịch vụ cùng trỏ về **một mã lịch hẹn**, ghi nhận `LichHen.maDichVu` là dịch vụ đầu tiên (dịch vụ chính).
- Giá dịch vụ đã chốt ở tiếp nhận, bao gồm phụ thu lông/da ở dòng dịch vụ đầu tiên. POS vẫn cho thêm sản phẩm bán kèm; không cho sửa danh sách dịch vụ Spa đã xác nhận trong yêu cầu này.
- Với hóa đơn Spa tạo trực tiếp tại POS không qua tiếp nhận, `maNVThoSpa` và `maLichHen` vẫn có thể NULL; chỉ giao việc cho thợ qua luồng tiếp nhận mới.
- Điều phối tải lại khi mở màn hình, không phải tự đẩy realtime tới một ứng dụng khác đang mở sẵn.
- Môi trường thực tế SQL Server, phân quyền đăng nhập và trải nghiệm UI **cần được kiểm thử trên máy người dùng**. Các kiểm tra tự động tại đây gồm build Java và smoke test logic không dùng SQL.
