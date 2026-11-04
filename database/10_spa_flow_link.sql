-- Chạy một lần trên TiTiPetDB hiện tại trước khi chạy luồng Spa.
-- Không xóa hay tạo mới database; có thể chạy lại nhiều lần.
USE TiTiPetDB;
GO
IF COL_LENGTH('dbo.ChiTietHoaDon_DichVu','maLichHen') IS NULL
    ALTER TABLE dbo.ChiTietHoaDon_DichVu ADD maLichHen VARCHAR(20) NULL;
IF COL_LENGTH('dbo.ChiTietHoaDon_DichVu','trangThaiBanGiao') IS NULL
    ALTER TABLE dbo.ChiTietHoaDon_DichVu ADD trangThaiBanGiao VARCHAR(20) NOT NULL
        CONSTRAINT DF_CTDV_BanGiao DEFAULT ('CHUA_BAN_GIAO') WITH VALUES;
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_CTDV_LichHen'
    AND parent_object_id = OBJECT_ID('dbo.ChiTietHoaDon_DichVu'))
    ALTER TABLE dbo.ChiTietHoaDon_DichVu WITH CHECK ADD CONSTRAINT FK_CTDV_LichHen
        FOREIGN KEY (maLichHen) REFERENCES dbo.LichHen(maLichHen);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_CTDV_LichHen'
    AND object_id = OBJECT_ID('dbo.ChiTietHoaDon_DichVu'))
    CREATE INDEX IX_CTDV_LichHen ON dbo.ChiTietHoaDon_DichVu(maLichHen);
GO
SELECT COL_LENGTH('dbo.ChiTietHoaDon_DichVu','maLichHen') AS maLichHen,
       COL_LENGTH('dbo.ChiTietHoaDon_DichVu','trangThaiBanGiao') AS trangThaiBanGiao;
GO
