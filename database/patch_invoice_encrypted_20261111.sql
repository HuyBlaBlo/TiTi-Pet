-- Chạy một lần trên cơ sở dữ liệu TiTiPet hiện có, trước khi thanh toán với bản vá mới.
-- Lưu snapshot GZIP + AES-256-GCM, định dạng .tpet (không phải PDF).
IF OBJECT_ID(N'dbo.HoaDonLuuTru', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.HoaDonLuuTru (
        maHoaDon VARCHAR(30) NOT NULL PRIMARY KEY,
        duLieuMaHoa VARBINARY(MAX) NOT NULL,
        ngayLuu DATETIME2(0) NOT NULL CONSTRAINT DF_HoaDonLuuTru_Ngay DEFAULT SYSDATETIME(),
        CONSTRAINT FK_HoaDonLuuTru_HoaDon FOREIGN KEY (maHoaDon)
            REFERENCES dbo.HoaDon(maHoaDon)
    );
END;
GO
