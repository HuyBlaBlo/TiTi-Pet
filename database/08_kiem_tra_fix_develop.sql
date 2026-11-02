/* Chỉ đọc dữ liệu / metadata; chạy trên database đã chọn ở bước 07. */
SET NOCOUNT ON;
SELECT DB_NAME() AS databaseDangKiemTra;

DECLARE @CanKiemTra TABLE (tenBang SYSNAME, tenCot SYSNAME);
INSERT INTO @CanKiemTra VALUES
    (N'DichVuSpa', N'thoiLuongPhut'),
    (N'LichHen', N'soLanDoiLich'),
    (N'DoiTra', N'loaiXuLy'),
    (N'DoiTra', N'tienHoan'),
    (N'DoiTra', N'maCa'),
    (N'DoiTra', N'maHoaDonDieuChinh'),
    (N'DoiTra', N'phuongThucXuLy'),
    (N'DoiTra', N'soLuongXuLy'),
    (N'DoiTra', N'maSanPhamMoi');

SELECT k.tenBang, k.tenCot,
       CASE WHEN c.column_id IS NULL THEN N'THIẾU' ELSE N'ĐÃ CÓ' END AS ketQua,
       TYPE_NAME(c.user_type_id) AS kieuDuLieu, c.max_length, c.is_nullable
FROM @CanKiemTra k
LEFT JOIN sys.columns c ON c.object_id = OBJECT_ID(N'dbo.' + k.tenBang)
                       AND c.name = k.tenCot
ORDER BY k.tenBang, k.tenCot;

IF EXISTS (SELECT 1 FROM @CanKiemTra
           WHERE COL_LENGTH(N'dbo.' + tenBang, tenCot) IS NULL)
    THROW 51000, N'Còn thiếu cột. Kiểm tra kết quả của script 07 trước khi chạy ứng dụng.', 1;
IF NOT EXISTS (SELECT 1 FROM sys.columns
               WHERE object_id = OBJECT_ID(N'dbo.DoiTra')
                 AND name = N'maSanPhamMoi' AND is_nullable = 1)
    THROW 51000, N'DoiTra.maSanPhamMoi chưa cho phép NULL để trả hàng.', 1;

-- SQL động để thông báo THIẾU ở trên vẫn chạy được khi schema chưa được nâng cấp.
EXEC(N'
    SELECT maDichVu, tenDichVu, thoiLuongPhut FROM dbo.DichVuSpa ORDER BY maDichVu;
    SELECT COUNT(*) AS soDichVuThoiLuongKhongHopLe
      FROM dbo.DichVuSpa WHERE thoiLuongPhut IS NULL OR thoiLuongPhut <= 0;
    SELECT COUNT(*) AS soLichHenSoLanDoiKhongHopLe
      FROM dbo.LichHen WHERE soLanDoiLich IS NULL OR soLanDoiLich < 0;
    SELECT TOP (10) maDoiTra, loaiXuLy, maHoaDon, maSanPhamCu, maSanPhamMoi,
           tienHoan, maCa, phuongThucXuLy, ngayDoi
      FROM dbo.DoiTra ORDER BY ngayDoi DESC;
');

-- Cùng phép tổng hợp dùng bởi TrangChuDAO sau khi sửa.
DECLARE @DauNgay DATETIME2 = CONVERT(DATE, SYSDATETIME());
DECLARE @NgaySau DATETIME2 = DATEADD(DAY, 1, @DauNgay);
SELECT
    (SELECT COUNT(*) FROM dbo.ThuCung) AS tongThuCung,
    (SELECT COUNT(*) FROM dbo.LichHen
        WHERE thoiGianHen >= @DauNgay AND thoiGianHen < @NgaySau
          AND trangThai <> 'HUY') AS lichHen,
    (SELECT COUNT(*) FROM dbo.LichHen
        WHERE thoiGianHen >= @DauNgay AND thoiGianHen < @NgaySau
          AND trangThai = 'CHO_TIEP_NHAN') AS lichCho,
    (SELECT COUNT(*) FROM dbo.HoaDon
        WHERE ngayLap >= @DauNgay AND ngayLap < @NgaySau
          AND trangThai = 'DA_THANH_TOAN') AS hoaDon,
    (SELECT COALESCE(SUM(tienPhaiThu), 0) FROM dbo.HoaDon
        WHERE ngayLap >= @DauNgay AND ngayLap < @NgaySau
          AND trangThai = 'DA_THANH_TOAN') AS doanhThu,
    (SELECT COUNT(*) FROM dbo.ChiTietHoaDon_DichVu
        WHERE trangThaiThucHien = 'DANG_LAM') AS dichVuDangLam,
    (SELECT COUNT(*) FROM dbo.ChiTietHoaDon_DichVu
        WHERE trangThaiThucHien = 'CHO_LAM') AS dichVuChoLam;

SELECT name, is_disabled, is_not_trusted FROM sys.foreign_keys
WHERE parent_object_id = OBJECT_ID(N'dbo.DoiTra');
SELECT name, is_disabled, is_not_trusted FROM sys.check_constraints
WHERE parent_object_id IN (OBJECT_ID(N'dbo.DoiTra'), OBJECT_ID(N'dbo.DichVuSpa'),
                          OBJECT_ID(N'dbo.LichHen'));
PRINT N'Kiểm tra xong. Các cột phải ĐÃ CÓ; số dữ liệu không hợp lệ = 0.';
PRINT N'Thời lượng 60 phút vừa thêm là giá trị tạm, cần đối chiếu từng dịch vụ.';
