/*
  TiTiPet - đồng bộ schema với mã nguồn TiTi-Pet(6).rar.
  Chọn đúng database trong SSMS: TiTiPetDB_UC002_TEST hoặc TiTiPetDB.
  Sao lưu database và đóng ứng dụng trước khi chạy. Không chạy 01_create_tables.sql.
  Script chỉ bổ sung schema; không xóa bảng, hóa đơn, kho hay dữ liệu khách hàng.
  Nếu đã chạy 05_UC002_NangCap_DoiTra.sql, các cột đã có được giữ nguyên.

  LƯU Ý THỜI LƯỢNG:
  Khi tạo mới thoiLuongPhut, dùng 60 phút tạm thời để tương thích setter Java.
  Đây KHÔNG phải thời lượng nghiệp vụ được xác nhận. Chỉnh lại từng dịch vụ
  theo dữ liệu nhóm trước khi dùng chức năng đặt lịch / tính giờ trả thú cưng.

  SQL Server 2012+. Chạy toàn bộ file trong một cửa sổ truy vấn riêng.
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;

IF @@TRANCOUNT <> 0
    THROW 51000, N'Hãy chạy script trong cửa sổ không có transaction đang mở.', 1;

IF DB_NAME() NOT IN (N'TiTiPetDB', N'TiTiPetDB_UC002_TEST')
    THROW 51000, N'Chọn đúng database TiTiPetDB hoặc TiTiPetDB_UC002_TEST trong SSMS.', 1;

-- Từ chối schema cũ TITIPET; không âm thầm tạo một database khác.
IF OBJECT_ID(N'dbo.DichVuSpa', N'U') IS NULL
   OR OBJECT_ID(N'dbo.LichHen', N'U') IS NULL
   OR OBJECT_ID(N'dbo.DoiTra', N'U') IS NULL
   OR COL_LENGTH(N'dbo.DichVuSpa', N'donGiaCoBan') IS NULL
   OR COL_LENGTH(N'dbo.LichHen', N'maNVThoSpa') IS NULL
   OR COL_LENGTH(N'dbo.HoaDon', N'tienPhaiThu') IS NULL
   OR COL_LENGTH(N'dbo.CaLamViec', N'trangThai') IS NULL
    THROW 51000, N'Thiếu schema nền TiTiPet.sql. Không dùng bản 01_create_tables.sql cũ.', 1;

BEGIN TRY
    BEGIN TRANSACTION;

    -- Dùng SQL động để lệnh tham chiếu cột mới được biên dịch sau khi thêm cột.
    IF COL_LENGTH(N'dbo.DichVuSpa', N'thoiLuongPhut') IS NULL
    BEGIN
        EXEC(N'ALTER TABLE dbo.DichVuSpa ADD thoiLuongPhut INT NOT NULL
            CONSTRAINT DF_FixDevelop_DichVuSpa_ThoiLuong DEFAULT (60) WITH VALUES;');
        PRINT N'Đã thêm thoiLuongPhut = 60 phút TẠM THỜI. Cần chỉnh theo từng dịch vụ.';
    END;

    IF COL_LENGTH(N'dbo.LichHen', N'soLanDoiLich') IS NULL
        EXEC(N'ALTER TABLE dbo.LichHen ADD soLanDoiLich INT NOT NULL
            CONSTRAINT DF_FixDevelop_LichHen_SoLanDoi DEFAULT (0) WITH VALUES;');

    IF COL_LENGTH(N'dbo.DoiTra', N'loaiXuLy') IS NULL
        EXEC(N'ALTER TABLE dbo.DoiTra ADD loaiXuLy VARCHAR(4) NOT NULL
            CONSTRAINT DF_DoiTra_LoaiXuLy DEFAULT (''DOI'') WITH VALUES;');
    IF COL_LENGTH(N'dbo.DoiTra', N'tienHoan') IS NULL
        EXEC(N'ALTER TABLE dbo.DoiTra ADD tienHoan DECIMAL(18,2) NOT NULL
            CONSTRAINT DF_DoiTra_TienHoan DEFAULT (0) WITH VALUES;');
    IF COL_LENGTH(N'dbo.DoiTra', N'maCa') IS NULL
        EXEC(N'ALTER TABLE dbo.DoiTra ADD maCa VARCHAR(20) NULL;');
    IF COL_LENGTH(N'dbo.DoiTra', N'maHoaDonDieuChinh') IS NULL
        EXEC(N'ALTER TABLE dbo.DoiTra ADD maHoaDonDieuChinh VARCHAR(30) NULL;');
    IF COL_LENGTH(N'dbo.DoiTra', N'phuongThucXuLy') IS NULL
        EXEC(N'ALTER TABLE dbo.DoiTra ADD phuongThucXuLy VARCHAR(16) NULL;');
    IF COL_LENGTH(N'dbo.DoiTra', N'soLuongXuLy') IS NULL
        EXEC(N'ALTER TABLE dbo.DoiTra ADD soLuongXuLy INT NOT NULL
            CONSTRAINT DF_DoiTra_SoLuong DEFAULT (1) WITH VALUES;');

    -- Trả hàng không có sản phẩm thay thế. Giữ nguyên quy tắc FK của schema nền.
    IF EXISTS (SELECT 1 FROM sys.columns
               WHERE object_id = OBJECT_ID(N'dbo.DoiTra')
                 AND name = N'maSanPhamMoi' AND is_nullable = 0)
    BEGIN
        IF EXISTS (SELECT 1 FROM sys.foreign_keys
                   WHERE parent_object_id = OBJECT_ID(N'dbo.DoiTra')
                     AND name = N'FK_DoiTra_SP_Moi'
                     AND (delete_referential_action <> 0
                          OR update_referential_action <> 0
                          OR is_not_for_replication = 1))
            THROW 51000, N'FK_DoiTra_SP_Moi đã được tùy chỉnh; cần đối chiếu trước khi nâng cấp.', 1;

        IF EXISTS (SELECT 1 FROM sys.foreign_keys
                   WHERE parent_object_id = OBJECT_ID(N'dbo.DoiTra')
                     AND name = N'FK_DoiTra_SP_Moi')
            EXEC(N'ALTER TABLE dbo.DoiTra DROP CONSTRAINT FK_DoiTra_SP_Moi;');

        EXEC(N'ALTER TABLE dbo.DoiTra ALTER COLUMN maSanPhamMoi VARCHAR(20) NULL;');
    END;

    IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys
                   WHERE parent_object_id = OBJECT_ID(N'dbo.DoiTra')
                     AND name = N'FK_DoiTra_SP_Moi')
        EXEC(N'ALTER TABLE dbo.DoiTra WITH CHECK ADD CONSTRAINT FK_DoiTra_SP_Moi
            FOREIGN KEY (maSanPhamMoi) REFERENCES dbo.SanPham(maSanPham);');
    IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys
                   WHERE parent_object_id = OBJECT_ID(N'dbo.DoiTra')
                     AND name = N'FK_DoiTra_Ca')
        EXEC(N'ALTER TABLE dbo.DoiTra WITH CHECK ADD CONSTRAINT FK_DoiTra_Ca
            FOREIGN KEY (maCa) REFERENCES dbo.CaLamViec(maCa);');
    IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys
                   WHERE parent_object_id = OBJECT_ID(N'dbo.DoiTra')
                     AND name = N'FK_DoiTra_HDDC')
        EXEC(N'ALTER TABLE dbo.DoiTra WITH CHECK ADD CONSTRAINT FK_DoiTra_HDDC
            FOREIGN KEY (maHoaDonDieuChinh) REFERENCES dbo.HoaDon(maHoaDon);');

    IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
                   WHERE parent_object_id = OBJECT_ID(N'dbo.DichVuSpa')
                     AND name = N'CK_FixDevelop_DichVuSpa_ThoiLuong')
        EXEC(N'ALTER TABLE dbo.DichVuSpa WITH CHECK
            ADD CONSTRAINT CK_FixDevelop_DichVuSpa_ThoiLuong
            CHECK (thoiLuongPhut IS NOT NULL AND thoiLuongPhut > 0);');
    IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
                   WHERE parent_object_id = OBJECT_ID(N'dbo.LichHen')
                     AND name = N'CK_FixDevelop_LichHen_SoLanDoi')
        EXEC(N'ALTER TABLE dbo.LichHen WITH CHECK
            ADD CONSTRAINT CK_FixDevelop_LichHen_SoLanDoi
            CHECK (soLanDoiLich IS NOT NULL AND soLanDoiLich >= 0);');
    IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
                   WHERE parent_object_id = OBJECT_ID(N'dbo.DoiTra')
                     AND name = N'CK_DoiTra_LoaiXuLy')
        EXEC(N'ALTER TABLE dbo.DoiTra WITH CHECK ADD CONSTRAINT CK_DoiTra_LoaiXuLy
            CHECK (loaiXuLy IN (''DOI'', ''TRA''));');
    IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
                   WHERE parent_object_id = OBJECT_ID(N'dbo.DoiTra')
                     AND name = N'CK_DoiTra_TienHoan')
        EXEC(N'ALTER TABLE dbo.DoiTra WITH CHECK ADD CONSTRAINT CK_DoiTra_TienHoan
            CHECK (tienHoan >= 0 AND soLuongXuLy > 0);');
    IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
                   WHERE parent_object_id = OBJECT_ID(N'dbo.DoiTra')
                     AND name = N'CK_DoiTra_HinhThuc')
        EXEC(N'ALTER TABLE dbo.DoiTra WITH CHECK ADD CONSTRAINT CK_DoiTra_HinhThuc
            CHECK ((loaiXuLy = ''DOI'' AND maSanPhamMoi IS NOT NULL AND tienHoan = 0)
                OR (loaiXuLy = ''TRA'' AND maSanPhamMoi IS NULL AND chenhLech = 0));');
    IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
                   WHERE parent_object_id = OBJECT_ID(N'dbo.DoiTra')
                     AND name = N'CK_DoiTra_PhuongThuc')
        EXEC(N'ALTER TABLE dbo.DoiTra WITH CHECK ADD CONSTRAINT CK_DoiTra_PhuongThuc
            CHECK (phuongThucXuLy IS NULL
                   OR phuongThucXuLy IN (''TIEN_MAT'', ''CHUYEN_KHOAN''));');

    -- Giữ tên view tương thích với script UC002 đã gửi trước đó.
    IF OBJECT_ID(N'dbo.v_UC002_TienHoanTheoCa', N'V') IS NULL
        EXEC(N'CREATE VIEW dbo.v_UC002_TienHoanTheoCa AS
            SELECT CAST(NULL AS VARCHAR(20)) AS maCa WHERE 1 = 0;');
    EXEC(N'ALTER VIEW dbo.v_UC002_TienHoanTheoCa AS
        SELECT maCa,
            SUM(CASE WHEN phuongThucXuLy = ''TIEN_MAT'' THEN tienHoan ELSE 0 END) AS hoanTienMat,
            SUM(CASE WHEN phuongThucXuLy = ''CHUYEN_KHOAN'' THEN tienHoan ELSE 0 END) AS hoanChuyenKhoan
        FROM dbo.DoiTra WHERE loaiXuLy = ''TRA'' AND maCa IS NOT NULL
        GROUP BY maCa;');

    COMMIT TRANSACTION;
    PRINT N'Nâng cấp hoàn tất. Chạy tiếp 08_kiem_tra_fix_develop.sql trên cùng database.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
