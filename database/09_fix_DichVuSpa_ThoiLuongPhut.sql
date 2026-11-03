/*
 * TiTi Pet - Bo sung cot DichVuSpa.thoiLuongPhut tren database hien co.
 * Chi sua schema thoiLuongPhut, khong INSERT/UPDATE/DELETE du lieu nghiep vu.
 * Chi chay sau khi backup va duoc nguoi quan tri database dong y.
 * Gia tri 60 phut chi la MAC DINH TAM THOI (dong bo 07_fix_develop_schema.sql),
 * khong phai gia thoi luong thuc te. Can doi chieu tung dich vu truoc dat lich.
 * SQL Server 2012+.
 */
SET NOCOUNT ON;
SET XACT_ABORT ON;

-- KHONG co lenh USE: nguoi chay phai chon dung database truoc trong SSMS.
IF DB_NAME() NOT IN (N'TiTiPetDB_UC002_TEST', N'TiTiPetDB')
    THROW 51000, N'Khong dung database TiTi Pet - dung lai.', 1;

IF @@TRANCOUNT <> 0
    THROW 51001, N'Hay chay trong query window khong co transaction mo.', 1;

IF OBJECT_ID(N'dbo.DichVuSpa', N'U') IS NULL
    THROW 51002, N'Khong tim thay dbo.DichVuSpa. Kiem tra schema nen.', 1;

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH(N'dbo.DichVuSpa', N'thoiLuongPhut') IS NULL
    BEGIN
        -- Dynamic SQL: dam bao SQL Server bien dich sau khi cot duoc tao.
        EXEC(N'ALTER TABLE dbo.DichVuSpa
            ADD thoiLuongPhut INT NOT NULL
            CONSTRAINT DF_DichVuSpa_ThoiLuong DEFAULT (60) WITH VALUES;');
        PRINT N'Da them cot thoiLuongPhut (mac dinh tam thoi 60 phut).';
    END
    ELSE
        PRINT N'Cot thoiLuongPhut da ton tai: khong tao lai, khong ghi de du lieu.';

    -- Neu cot da co nhung sai kieu hoac cho NULL, dung lai de kiem tra thu cong.
    IF EXISTS (
        SELECT 1 FROM sys.columns
        WHERE object_id = OBJECT_ID(N'dbo.DichVuSpa')
          AND name = N'thoiLuongPhut'
          AND (system_type_id <> 56 OR is_nullable <> 0)
    )
        THROW 51003, N'Cot thoiLuongPhut phai co kieu INT NOT NULL. Khong tu dong sua du lieu.', 1;

    -- Kiem tra du lieu hien co, khong tu sua gia tri 0/NULL.
    EXEC(N'IF EXISTS (
            SELECT 1 FROM dbo.DichVuSpa
            WHERE thoiLuongPhut IS NULL OR thoiLuongPhut <= 0
        ) THROW 51004, N''Co thoiLuongPhut khong hop le, can kiem tra thu cong.'', 1;');

    -- Giu nguyen CHECK cu tu script 07 / script nguoi dung da tao.
    IF NOT EXISTS (
        SELECT 1 FROM sys.check_constraints
        WHERE parent_object_id = OBJECT_ID(N'dbo.DichVuSpa')
          AND name IN (
              N'CK_DichVuSpa_ThoiLuong',
              N'CK_FixDevelop_DichVuSpa_ThoiLuong',
              N'CK_DichVu_ThoiLuong'
          )
    )
        EXEC(N'ALTER TABLE dbo.DichVuSpa WITH CHECK
            ADD CONSTRAINT CK_DichVuSpa_ThoiLuong
            CHECK (thoiLuongPhut IS NOT NULL AND thoiLuongPhut > 0);');

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;

-- Kiem tra sau migration (chi SELECT).
SELECT DB_NAME() AS tenDatabase,
       c.name AS tenCot,
       TYPE_NAME(c.user_type_id) AS kieuDuLieu,
       c.is_nullable AS choPhepNull
FROM sys.columns c
WHERE c.object_id = OBJECT_ID(N'dbo.DichVuSpa')
  AND c.name = N'thoiLuongPhut';

EXEC(N'SELECT maDichVu, tenDichVu, thoiLuongPhut
       FROM dbo.DichVuSpa ORDER BY maDichVu;');
