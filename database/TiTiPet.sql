/*
 TiTi Pet | 01 Schema chinh (SQL Server 2012+)
 14 bang cot loi (theo bao cao Nhom 06) + 2 bang mo rong DoiTra, LichSuNhapKho.
 2 views bao cao, FK, indexes va stored procedure chi doc.
 - Cai dat MOI: ket noi SSMS va chay tren SQL Server.
 - Database mac dinh: TiTiPetDB. MUON TAO DB TEST, thay DONG CREATE DATABASE
   va DONG USE TiTiPetDB trong file bang TiTiPetDB_FULL_TEST (xem README).
 - KHONG drop data / khong sua ma san pham / khong seed du lieu trong file nay.
 - Neu database dang co du lieu, backup truoc; danh gia schema cu truoc khi nang cap.
 - Khong dung script nay de tu dong dong bo tat ca bien the CSDL da duoc chinh sua thu cong.
 - Khong luu tai khoan/mat khau SQL trong file SQL.
*/
SET NOCOUNT ON;
GO
IF DB_ID(N'TiTiPetDB') IS NULL
BEGIN
    EXEC (N'CREATE DATABASE [TiTiPetDB]');
END;
GO
USE [TiTiPetDB];
GO

-- 01 / KhachHang
IF OBJECT_ID(N'dbo.KhachHang', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.KhachHang (
        maKH                VARCHAR(20)       NOT NULL,
        hoTen               NVARCHAR(120)     NOT NULL,
        soDienThoai         VARCHAR(20)       NOT NULL,
        hangTV              VARCHAR(12)       NOT NULL CONSTRAINT DF_KhachHang_hangTV DEFAULT ('THUONG'),
        diemTichLuy         INT               NOT NULL CONSTRAINT DF_KhachHang_diem DEFAULT (0),
        CONSTRAINT PK_KhachHang PRIMARY KEY (maKH),
        CONSTRAINT UQ_KhachHang_SDT UNIQUE (soDienThoai),
        CONSTRAINT CK_KhachHang_Diem CHECK (diemTichLuy >= 0),
        CONSTRAINT CK_KhachHang_Hang CHECK (hangTV IN ('THUONG','HANG_1','HANG_2','HANG_3'))
    );
END;
GO

-- 02 / NhanVien: 'matKhau' MUST contain a salted password hash, not plaintext.
IF OBJECT_ID(N'dbo.NhanVien', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.NhanVien (
        maNV                VARCHAR(20)       NOT NULL,
        tenDangNhap         VARCHAR(80)       NOT NULL,
        matKhau             NVARCHAR(255)     NOT NULL,
        hoTen               NVARCHAR(120)     NOT NULL,
        vaiTro              VARCHAR(12)       NOT NULL,
        dangHoatDong        BIT               NOT NULL CONSTRAINT DF_NhanVien_Active DEFAULT (1),
        CONSTRAINT PK_NhanVien PRIMARY KEY (maNV),
        CONSTRAINT UQ_NhanVien_TenDangNhap UNIQUE (tenDangNhap),
        CONSTRAINT CK_NhanVien_VaiTro CHECK (vaiTro IN ('THU_NGAN','THO_SPA','QUAN_LY'))
    );
END;
GO

-- 03 / DichVuSpa
IF OBJECT_ID(N'dbo.DichVuSpa', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.DichVuSpa (
        maDichVu            VARCHAR(20)       NOT NULL,
        tenDichVu           NVARCHAR(150)     NOT NULL,
        donGiaCoBan         DECIMAL(18,2)     NOT NULL CONSTRAINT DF_DichVuSpa_DonGia DEFAULT (0),
        thueSuat            DECIMAL(5,2)      NOT NULL CONSTRAINT DF_DichVuSpa_Thue DEFAULT (0),
        giaDaBaoGomThue     BIT               NOT NULL CONSTRAINT DF_DichVuSpa_GiaThue DEFAULT (0),
        loaiThuCungApDung   VARCHAR(10)       NOT NULL,
        thoiLuongPhut       INT               NOT NULL CONSTRAINT DF_DichVuSpa_ThoiLuong DEFAULT (60),
        dangHoatDong        BIT               NOT NULL CONSTRAINT DF_DichVuSpa_Active DEFAULT (1),
        CONSTRAINT PK_DichVuSpa PRIMARY KEY (maDichVu),
        CONSTRAINT CK_DichVuSpa_DonGia CHECK (donGiaCoBan >= 0),
        CONSTRAINT CK_DichVuSpa_Thue CHECK (thueSuat BETWEEN 0 AND 100),
        CONSTRAINT CK_DichVuSpa_ThoiLuong CHECK (thoiLuongPhut > 0),
        CONSTRAINT CK_DichVuSpa_Loai CHECK (loaiThuCungApDung IN ('CHO','MEO','CA_HAI'))
    );
END;
GO

-- 04 / SanPham: stock on hand maintained directly on product (no batch table).
IF OBJECT_ID(N'dbo.SanPham', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.SanPham (
        maSanPham           VARCHAR(20)       NOT NULL,
        tenSanPham          NVARCHAR(180)     NOT NULL,
        giaBan              DECIMAL(18,2)     NOT NULL,
        soLuongTonKho       INT               NOT NULL CONSTRAINT DF_SanPham_TonKho DEFAULT (0),
        maVach              VARCHAR(80)       NULL,
        nguongCanhBaoTon    INT               NOT NULL CONSTRAINT DF_SanPham_Nguong DEFAULT (0),
        thueSuat            DECIMAL(5,2)      NULL,
        loaiSanPham         VARCHAR(12)       NOT NULL,
        dangKinhDoanh       BIT               NOT NULL CONSTRAINT DF_SanPham_Active DEFAULT (1),
        CONSTRAINT PK_SanPham PRIMARY KEY (maSanPham),
        CONSTRAINT CK_SanPham_Gia CHECK (giaBan >= 0),
        CONSTRAINT CK_SanPham_Nguong CHECK (nguongCanhBaoTon >= 0),
        CONSTRAINT CK_SanPham_TonKho CHECK (soLuongTonKho >= 0),
        CONSTRAINT CK_SanPham_Thue CHECK (thueSuat IS NULL OR thueSuat BETWEEN 0 AND 100),
        CONSTRAINT CK_SanPham_Loai CHECK (loaiSanPham IN ('THUC_AN','PHU_KIEN'))
    );
END;
GO

-- 05 / CaLamViec
IF OBJECT_ID(N'dbo.CaLamViec', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.CaLamViec (
        maCa                VARCHAR(20)       NOT NULL,
        maNV                VARCHAR(20)       NOT NULL,
        thoiGianBatDau      DATETIME2(0)      NOT NULL,
        thoiGianKetThuc     DATETIME2(0)      NULL,
        tienDauCa           DECIMAL(18,2)     NOT NULL CONSTRAINT DF_CaLamViec_Tien DEFAULT (0),
        trangThai           VARCHAR(20)       NOT NULL CONSTRAINT DF_CaLamViec_TrangThai DEFAULT ('DANG_MO'),
        ghiChu              NVARCHAR(1000)    NULL,
        CONSTRAINT PK_CaLamViec PRIMARY KEY (maCa),
        CONSTRAINT CK_CaLamViec_ThoiGian CHECK (thoiGianKetThuc IS NULL OR thoiGianKetThuc > thoiGianBatDau),
        CONSTRAINT CK_CaLamViec_Tien CHECK (tienDauCa >= 0),
        CONSTRAINT CK_CaLamViec_TrangThai CHECK (trangThai IN ('DANG_MO','CHO_XAC_NHAN','DA_CHOT'))
    );
END;
GO

-- 06 / ThuCung
IF OBJECT_ID(N'dbo.ThuCung', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ThuCung (
        maThuCung           VARCHAR(20)       NOT NULL,
        maKH                VARCHAR(20)       NOT NULL,
        tenThuCung          NVARCHAR(120)     NOT NULL,
        loaiThuCung         VARCHAR(10)       NOT NULL,
        canNang             DECIMAL(8,2)      NOT NULL,
        tinhTrangLong       NVARCHAR(300)     NULL,
        ghiChu              NVARCHAR(1000)    NULL,
        CONSTRAINT PK_ThuCung PRIMARY KEY (maThuCung),
        CONSTRAINT UQ_ThuCung_maThuCung_maKH UNIQUE (maThuCung, maKH),
        CONSTRAINT CK_ThuCung_CanNang CHECK (canNang > 0),
        CONSTRAINT CK_ThuCung_Loai CHECK (loaiThuCung IN ('CHO','MEO'))
    );
END;
GO

-- 07 / PhuKien: 1-to-1 subtype of SanPham
IF OBJECT_ID(N'dbo.PhuKien', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PhuKien (
        maSanPham           VARCHAR(20)       NOT NULL,
        kichThuoc           NVARCHAR(50)      NULL,
        mauSac              NVARCHAR(80)      NULL,
        chatLieu            NVARCHAR(120)     NULL,
        CONSTRAINT PK_PhuKien PRIMARY KEY (maSanPham)
    );
END;
GO

-- 08 / ThucAn: 1-to-1 subtype. Ngay het han CHI thuoc ThucAn, KHONG nam tren SanPham.
IF OBJECT_ID(N'dbo.ThucAn', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ThucAn (
        maSanPham           VARCHAR(20)       NOT NULL,
        hanSuDung           DATE              NOT NULL,
        trongLuong          DECIMAL(10,3)     NOT NULL,
        CONSTRAINT PK_ThucAn PRIMARY KEY (maSanPham),
        CONSTRAINT CK_ThucAn_TrongLuong CHECK (trongLuong > 0)
    );
END;
GO

-- BangGiaSpa removed. Service base price is DichVuSpa.donGiaCoBan.

-- 10 / KhuyenMai: at most one promotion per invoice
IF OBJECT_ID(N'dbo.KhuyenMai', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.KhuyenMai (
        maKhuyenMai         VARCHAR(20)       NOT NULL,
        tenChuongTrinh      NVARCHAR(180)     NOT NULL,
        ngayBatDau          DATETIME2(0)      NOT NULL,
        ngayKetThuc         DATETIME2(0)      NOT NULL,
        giamTram            DECIMAL(5,2)      NOT NULL,
        trangThai           BIT               NOT NULL CONSTRAINT DF_KhuyenMai_TrangThai DEFAULT (1),
        phamVi              VARCHAR(15)       NOT NULL,
        maSanPhamApDung     VARCHAR(20)       NULL,
        maDichVuApDung      VARCHAR(20)       NULL,
        CONSTRAINT PK_KhuyenMai PRIMARY KEY (maKhuyenMai),
        CONSTRAINT CK_KhuyenMai_ThoiGian CHECK (ngayKetThuc > ngayBatDau),
        CONSTRAINT CK_KhuyenMai_Giam CHECK (giamTram BETWEEN 0 AND 100),
        CONSTRAINT CK_KhuyenMai_PhamVi CHECK (phamVi IN ('TOAN_DON','PHU_KIEN','THUC_AN','DICH_VU'))
    );
END;
GO

-- 11 / LichHen: specific pet, customer, service, technician
IF OBJECT_ID(N'dbo.LichHen', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.LichHen (
        maLichHen           VARCHAR(20)       NOT NULL,
        thoiGianHen         DATETIME2(0)      NOT NULL,
        thoiGianKetThuc     DATETIME2(0)      NULL,
        trangThai           VARCHAR(20)       NOT NULL CONSTRAINT DF_LichHen_TrangThai DEFAULT ('CHO_TIEP_NHAN'),
        maKH                VARCHAR(20)       NOT NULL,
        maThuCung           VARCHAR(20)       NOT NULL,
        maDichVu            VARCHAR(20)       NOT NULL,
        maNVThoSpa          VARCHAR(20)       NULL,
        ghiChu              NVARCHAR(1000)    NULL,
        CONSTRAINT PK_LichHen PRIMARY KEY (maLichHen),
        CONSTRAINT CK_LichHen_TrangThai CHECK (trangThai IN ('CHO_TIEP_NHAN','DA_TIEP_NHAN','HUY')),
        CONSTRAINT CK_LichHen_ThoiGian CHECK (thoiGianKetThuc IS NULL OR thoiGianKetThuc > thoiGianHen)
    );
END;
GO

-- 12 / HoaDon
IF OBJECT_ID(N'dbo.HoaDon', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.HoaDon (
        maHoaDon            VARCHAR(30)       NOT NULL,
        ngayLap             DATETIME2(0)      NOT NULL CONSTRAINT DF_HoaDon_NgayLap DEFAULT (SYSDATETIME()),
        maKH                VARCHAR(20)       NOT NULL,
        maNV                VARCHAR(20)       NOT NULL,
        maCa                VARCHAR(20)       NULL,
        maKhuyenMai         VARCHAR(20)       NULL,
        nguonDon            VARCHAR(12)       NOT NULL CONSTRAINT DF_HoaDon_Nguon DEFAULT ('TAI_QUAY'),
        maDonKenh           VARCHAR(70)       NULL,
        phuongThucThanhToan VARCHAR(16)       NULL,
        trangThai           VARCHAR(20)       NOT NULL CONSTRAINT DF_HoaDon_TrangThai DEFAULT ('CHO_THANH_TOAN'),
        tongTienHang        DECIMAL(18,2)     NOT NULL CONSTRAINT DF_HoaDon_TienHang DEFAULT (0),
        tongTienSpa         DECIMAL(18,2)     NOT NULL CONSTRAINT DF_HoaDon_TienSpa DEFAULT (0),
        tienGiamKM          DECIMAL(18,2)     NOT NULL CONSTRAINT DF_HoaDon_GiamKM DEFAULT (0),
        tienGiamHangTV      DECIMAL(18,2)     NOT NULL CONSTRAINT DF_HoaDon_GiamTV DEFAULT (0),
        soDiemSuDung        INT               NOT NULL CONSTRAINT DF_HoaDon_DiemDung DEFAULT (0),
        tienGiamDiem        DECIMAL(18,2)     NOT NULL CONSTRAINT DF_HoaDon_GiamDiem DEFAULT (0),
        tienPhaiThu         DECIMAL(18,2)     NOT NULL CONSTRAINT DF_HoaDon_PhaiThu DEFAULT (0),
        soDiemDuocTich      INT               NOT NULL CONSTRAINT DF_HoaDon_DiemTich DEFAULT (0),
        tongTienTruocThue   DECIMAL(18,2)     NOT NULL CONSTRAINT DF_HoaDon_TruocThue DEFAULT (0),
        tongTienThue        DECIMAL(18,2)     NOT NULL CONSTRAINT DF_HoaDon_TongThue DEFAULT (0),
        tongTien            DECIMAL(18,2)     NOT NULL CONSTRAINT DF_HoaDon_TongTien DEFAULT (0),
        CONSTRAINT PK_HoaDon PRIMARY KEY (maHoaDon),
        CONSTRAINT CK_HoaDon_Nguon CHECK (nguonDon IN ('TAI_QUAY','SHOPEE','TIKTOK')),
        CONSTRAINT CK_HoaDon_PhuongThuc CHECK (phuongThucThanhToan IS NULL OR phuongThucThanhToan IN ('TIEN_MAT','CHUYEN_KHOAN','QUA_SAN')),
        CONSTRAINT CK_HoaDon_TrangThai CHECK (trangThai IN ('CHO_THANH_TOAN','DA_THANH_TOAN','DANG_DOI_SOAT','HUY')),
        CONSTRAINT CK_HoaDon_TienKhongAm CHECK (
            tongTienHang >= 0 AND tongTienSpa >= 0 AND tienGiamKM >= 0 AND
            tienGiamHangTV >= 0 AND soDiemSuDung >= 0 AND tienGiamDiem >= 0 AND
            tienPhaiThu >= 0 AND soDiemDuocTich >= 0 AND tongTienTruocThue >= 0 AND
            tongTienThue >= 0 AND tongTien >= 0),
        CONSTRAINT CK_HoaDon_KM_HangTV CHECK (maKhuyenMai IS NULL OR tienGiamHangTV = 0)
    );
END;
GO

-- 13 / ChiTietHoaDon_SanPham: report specifies PK (maHoaDon, maSanPham).
IF OBJECT_ID(N'dbo.ChiTietHoaDon_SanPham', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ChiTietHoaDon_SanPham (
        maHoaDon            VARCHAR(30)       NOT NULL,
        maSanPham           VARCHAR(20)       NOT NULL,
        soLuong             INT               NOT NULL,
        donGia              DECIMAL(18,2)     NOT NULL,
        thanhTien           AS (CONVERT(DECIMAL(18,2), soLuong * donGia)) PERSISTED,
        thueSuat            DECIMAL(5,2)      NOT NULL CONSTRAINT DF_CTSP_Thue DEFAULT (0),
        giaDaBaoGomThue     BIT               NOT NULL CONSTRAINT DF_CTSP_DaThue DEFAULT (0),
        tienGiamPhanBo      DECIMAL(18,2)     NOT NULL CONSTRAINT DF_CTSP_Giam DEFAULT (0),
        giaTriSauGiam       AS (CONVERT(DECIMAL(18,2), soLuong * donGia - tienGiamPhanBo)) PERSISTED,
        tienTruocThue       DECIMAL(18,2)     NOT NULL CONSTRAINT DF_CTSP_TruocThue DEFAULT (0),
        tienThue            DECIMAL(18,2)     NOT NULL CONSTRAINT DF_CTSP_TienThue DEFAULT (0),
        tienSauThue         AS (CONVERT(DECIMAL(18,2), tienTruocThue + tienThue)) PERSISTED,
        CONSTRAINT PK_CTSP PRIMARY KEY (maHoaDon, maSanPham),
        CONSTRAINT CK_CTSP_SoLuong CHECK (soLuong > 0),
        CONSTRAINT CK_CTSP_Gia CHECK (donGia >= 0),
        CONSTRAINT CK_CTSP_Thue CHECK (thueSuat BETWEEN 0 AND 100),
        CONSTRAINT CK_CTSP_Giam CHECK (tienGiamPhanBo >= 0 AND tienGiamPhanBo <= soLuong * donGia),
        CONSTRAINT CK_CTSP_ThanhToan CHECK (tienTruocThue >= 0 AND tienThue >= 0)
    );
END;
GO

-- 14 / ChiTietHoaDon_DichVu:
-- Technical surrogate PK allows the same service/pet to occur on separate visits.
IF OBJECT_ID(N'dbo.ChiTietHoaDon_DichVu', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ChiTietHoaDon_DichVu (
        maChiTietDichVu     BIGINT            IDENTITY(1,1) NOT NULL,
        maHoaDon            VARCHAR(30)       NOT NULL,
        maDichVu            VARCHAR(20)       NOT NULL,
        maThuCung           VARCHAR(20)       NOT NULL,
        maNVThoSpa          VARCHAR(20)       NULL,
        trangThaiThucHien   VARCHAR(16)       NOT NULL CONSTRAINT DF_CTDV_TrangThai DEFAULT ('CHO_LAM'),
        soLuong             INT               NOT NULL CONSTRAINT DF_CTDV_SoLuong DEFAULT (1),
        donGia              DECIMAL(18,2)     NOT NULL,
        thanhTien           AS (CONVERT(DECIMAL(18,2), donGia)) PERSISTED,
        thueSuat            DECIMAL(5,2)      NOT NULL CONSTRAINT DF_CTDV_Thue DEFAULT (0),
        giaDaBaoGomThue     BIT               NOT NULL CONSTRAINT DF_CTDV_DaThue DEFAULT (0),
        tienGiamPhanBo      DECIMAL(18,2)     NOT NULL CONSTRAINT DF_CTDV_Giam DEFAULT (0),
        giaTriSauGiam       AS (CONVERT(DECIMAL(18,2), donGia - tienGiamPhanBo)) PERSISTED,
        tienTruocThue       DECIMAL(18,2)     NOT NULL CONSTRAINT DF_CTDV_TruocThue DEFAULT (0),
        tienThue            DECIMAL(18,2)     NOT NULL CONSTRAINT DF_CTDV_TienThue DEFAULT (0),
        tienSauThue         AS (CONVERT(DECIMAL(18,2), tienTruocThue + tienThue)) PERSISTED,
        CONSTRAINT PK_CTDV PRIMARY KEY (maChiTietDichVu),
        CONSTRAINT CK_CTDV_TrangThai CHECK (trangThaiThucHien IN ('CHO_LAM','DANG_LAM','HOAN_THANH','TU_CHOI')),
        CONSTRAINT CK_CTDV_1Luot CHECK (soLuong = 1),
        CONSTRAINT CK_CTDV_Gia CHECK (donGia >= 0),
        CONSTRAINT CK_CTDV_Thue CHECK (thueSuat BETWEEN 0 AND 100),
        CONSTRAINT CK_CTDV_Giam CHECK (tienGiamPhanBo >= 0 AND tienGiamPhanBo <= donGia),
        CONSTRAINT CK_CTDV_ThanhToan CHECK (tienTruocThue >= 0 AND tienThue >= 0)
    );
END;
GO

-- 15 / PhieuKetCa
IF OBJECT_ID(N'dbo.PhieuKetCa', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PhieuKetCa (
        maPhieu             VARCHAR(20)       NOT NULL,
        maNV                VARCHAR(20)       NOT NULL,
        maCa                VARCHAR(20)       NOT NULL,
        soTienBanDauCa      DECIMAL(18,2)     NOT NULL CONSTRAINT DF_PhieuKetCa_DauCa DEFAULT (0),
        tienMat             DECIMAL(18,2)     NOT NULL CONSTRAINT DF_PhieuKetCa_TienMat DEFAULT (0),
        tienCK              DECIMAL(18,2)     NOT NULL CONSTRAINT DF_PhieuKetCa_TienCK DEFAULT (0),
        tongTienKetCa       DECIMAL(18,2)     NOT NULL CONSTRAINT DF_PhieuKetCa_Tong DEFAULT (0),
        ngayLapPhieu        DATETIME2(0)      NOT NULL CONSTRAINT DF_PhieuKetCa_Ngay DEFAULT (SYSDATETIME()),
        trangThaiDuyet      VARCHAR(14)       NOT NULL CONSTRAINT DF_PhieuKetCa_Duyet DEFAULT ('CHO_DUYET'),
        maNVQuanLyDuyet     VARCHAR(20)       NULL,
        ngayDuyet           DATETIME2(0)      NULL,
        CONSTRAINT PK_PhieuKetCa PRIMARY KEY (maPhieu),
        CONSTRAINT UQ_PhieuKetCa_MaCa UNIQUE (maCa),
        CONSTRAINT CK_PhieuKetCa_Tien CHECK (soTienBanDauCa >= 0 AND tienMat >= 0 AND tienCK >= 0 AND tongTienKetCa >= 0),
        CONSTRAINT CK_PhieuKetCa_Duyet CHECK (trangThaiDuyet IN ('CHO_DUYET','DA_DUYET','TU_CHOI'))
    );
END;
GO

-- Schema compat for DB created before duration and import price were added.
IF COL_LENGTH(N'dbo.DichVuSpa', N'thoiLuongPhut') IS NULL
    EXEC(N'ALTER TABLE dbo.DichVuSpa ADD thoiLuongPhut INT NOT NULL CONSTRAINT DF_DichVuSpa_ThoiLuong DEFAULT (60) WITH VALUES;');
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE parent_object_id=OBJECT_ID(N'dbo.DichVuSpa') AND name=N'CK_DichVuSpa_ThoiLuong')
    ALTER TABLE dbo.DichVuSpa WITH CHECK ADD CONSTRAINT CK_DichVuSpa_ThoiLuong CHECK (thoiLuongPhut > 0);
GO
IF COL_LENGTH(N'dbo.SanPham', N'giaNhap') IS NULL
    EXEC(N'ALTER TABLE dbo.SanPham ADD giaNhap DECIMAL(18,2) NOT NULL CONSTRAINT DF_SanPham_GiaNhap DEFAULT (0) WITH VALUES;');
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE parent_object_id=OBJECT_ID(N'dbo.SanPham') AND name=N'CK_SanPham_GiaNhap')
    ALTER TABLE dbo.SanPham WITH CHECK ADD CONSTRAINT CK_SanPham_GiaNhap CHECK (giaNhap >= 0);
GO

-- Indexes and derived views for operational queries.
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.SanPham') AND name=N'UX_SanPham_MaVach')
    CREATE UNIQUE INDEX UX_SanPham_MaVach ON dbo.SanPham(maVach) WHERE maVach IS NOT NULL;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.ThuCung') AND name=N'IX_ThuCung_maKH')
    CREATE INDEX IX_ThuCung_maKH ON dbo.ThuCung(maKH);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.LichHen') AND name=N'IX_LichHen_ThoSpa_ThoiGian')
    CREATE INDEX IX_LichHen_ThoSpa_ThoiGian ON dbo.LichHen(maNVThoSpa,thoiGianHen);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.HoaDon') AND name=N'IX_HoaDon_maKH_NgayLap')
    CREATE INDEX IX_HoaDon_maKH_NgayLap ON dbo.HoaDon(maKH,ngayLap);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.ChiTietHoaDon_DichVu') AND name=N'IX_CTDV_HoaDon')
    CREATE INDEX IX_CTDV_HoaDon ON dbo.ChiTietHoaDon_DichVu(maHoaDon);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.ChiTietHoaDon_SanPham') AND name=N'IX_CTSP_SanPham')
    CREATE INDEX IX_CTSP_SanPham ON dbo.ChiTietHoaDon_SanPham(maSanPham);
GO

-- dbo.v_TonKhoSanPham: CREATE OR ALTER unsupported on older SQL Server versions.
IF OBJECT_ID(N'dbo.v_TonKhoSanPham', N'V') IS NULL
BEGIN
    EXEC(N'CREATE VIEW dbo.v_TonKhoSanPham AS SELECT CAST(0 AS INT) AS Placeholder;');
END;
GO
ALTER VIEW dbo.v_TonKhoSanPham
AS
SELECT sp.maSanPham, sp.tenSanPham, sp.loaiSanPham, sp.giaBan,
       sp.soLuongTonKho, sp.nguongCanhBaoTon,
       CASE WHEN sp.loaiSanPham = 'THUC_AN' THEN ta.hanSuDung ELSE CAST(NULL AS DATE) END AS hanSuDung,
       CASE WHEN sp.soLuongTonKho <= sp.nguongCanhBaoTon THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END AS canCanhBao,
       CASE WHEN sp.loaiSanPham = 'THUC_AN' AND
            (ta.maSanPham IS NULL OR ta.hanSuDung < CONVERT(date, GETDATE()))
            THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END AS daHetHan
FROM dbo.SanPham AS sp
LEFT JOIN dbo.ThucAn AS ta ON ta.maSanPham = sp.maSanPham;
GO

-- dbo.v_ChiTieuNamKhachHang: CREATE OR ALTER unsupported on older SQL Server versions.
IF OBJECT_ID(N'dbo.v_ChiTieuNamKhachHang', N'V') IS NULL
BEGIN
    EXEC(N'CREATE VIEW dbo.v_ChiTieuNamKhachHang AS SELECT CAST(0 AS INT) AS Placeholder;');
END;
GO
ALTER VIEW dbo.v_ChiTieuNamKhachHang
AS
SELECT kh.maKH, kh.hoTen, YEAR(hd.ngayLap) AS nam,
       SUM(hd.tienPhaiThu) AS tongChiTieuNam
FROM dbo.KhachHang AS kh
JOIN dbo.HoaDon AS hd ON hd.maKH = kh.maKH AND hd.trangThai = 'DA_THANH_TOAN'
GROUP BY kh.maKH, kh.hoTen, YEAR(hd.ngayLap);
GO

PRINT N'14 core tables and 2 views ready.';
GO
GO

-- ========= FOREIGN KEYS (included in this same schema file) =========
/* ============================================================================
   TiTi Pet: add FOREIGN KEY relationships in SQL Server.
   SAFE: does NOT delete data, does NOT turn off FK validation.
   Important for existing tables: remove orphan data before running.
   Run after 01_Tao_CSDL_TiTiPet.sql.
   ============================================================================ */
-- The DB is already selected at the top. Do not switch connections mid-script.
SET NOCOUNT ON;
GO

-- 01 Child -> Parent
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_CaLamViec_NhanVien' AND parent_object_id=OBJECT_ID(N'dbo.CaLamViec'))
    ALTER TABLE dbo.CaLamViec WITH CHECK ADD CONSTRAINT FK_CaLamViec_NhanVien FOREIGN KEY (maNV) REFERENCES dbo.NhanVien(maNV);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_ThuCung_KhachHang' AND parent_object_id=OBJECT_ID(N'dbo.ThuCung'))
    ALTER TABLE dbo.ThuCung WITH CHECK ADD CONSTRAINT FK_ThuCung_KhachHang FOREIGN KEY (maKH) REFERENCES dbo.KhachHang(maKH);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_PhuKien_SanPham' AND parent_object_id=OBJECT_ID(N'dbo.PhuKien'))
    ALTER TABLE dbo.PhuKien WITH CHECK ADD CONSTRAINT FK_PhuKien_SanPham FOREIGN KEY (maSanPham) REFERENCES dbo.SanPham(maSanPham);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_ThucAn_SanPham' AND parent_object_id=OBJECT_ID(N'dbo.ThucAn'))
    ALTER TABLE dbo.ThucAn WITH CHECK ADD CONSTRAINT FK_ThucAn_SanPham FOREIGN KEY (maSanPham) REFERENCES dbo.SanPham(maSanPham);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_KhuyenMai_SanPhamApDung' AND parent_object_id=OBJECT_ID(N'dbo.KhuyenMai'))
    ALTER TABLE dbo.KhuyenMai WITH CHECK ADD CONSTRAINT FK_KhuyenMai_SanPhamApDung FOREIGN KEY (maSanPhamApDung) REFERENCES dbo.SanPham(maSanPham);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_KhuyenMai_DichVuApDung' AND parent_object_id=OBJECT_ID(N'dbo.KhuyenMai'))
    ALTER TABLE dbo.KhuyenMai WITH CHECK ADD CONSTRAINT FK_KhuyenMai_DichVuApDung FOREIGN KEY (maDichVuApDung) REFERENCES dbo.DichVuSpa(maDichVu);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_LichHen_KhachHang' AND parent_object_id=OBJECT_ID(N'dbo.LichHen'))
    ALTER TABLE dbo.LichHen WITH CHECK ADD CONSTRAINT FK_LichHen_KhachHang FOREIGN KEY (maKH) REFERENCES dbo.KhachHang(maKH);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_LichHen_ThuCungDungChu' AND parent_object_id=OBJECT_ID(N'dbo.LichHen'))
    ALTER TABLE dbo.LichHen WITH CHECK ADD CONSTRAINT FK_LichHen_ThuCungDungChu FOREIGN KEY (maThuCung, maKH) REFERENCES dbo.ThuCung(maThuCung, maKH);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_LichHen_DichVuSpa' AND parent_object_id=OBJECT_ID(N'dbo.LichHen'))
    ALTER TABLE dbo.LichHen WITH CHECK ADD CONSTRAINT FK_LichHen_DichVuSpa FOREIGN KEY (maDichVu) REFERENCES dbo.DichVuSpa(maDichVu);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_LichHen_ThoSpa' AND parent_object_id=OBJECT_ID(N'dbo.LichHen'))
    ALTER TABLE dbo.LichHen WITH CHECK ADD CONSTRAINT FK_LichHen_ThoSpa FOREIGN KEY (maNVThoSpa) REFERENCES dbo.NhanVien(maNV);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_HoaDon_KhachHang' AND parent_object_id=OBJECT_ID(N'dbo.HoaDon'))
    ALTER TABLE dbo.HoaDon WITH CHECK ADD CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKH) REFERENCES dbo.KhachHang(maKH);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_HoaDon_NhanVien' AND parent_object_id=OBJECT_ID(N'dbo.HoaDon'))
    ALTER TABLE dbo.HoaDon WITH CHECK ADD CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (maNV) REFERENCES dbo.NhanVien(maNV);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_HoaDon_CaLamViec' AND parent_object_id=OBJECT_ID(N'dbo.HoaDon'))
    ALTER TABLE dbo.HoaDon WITH CHECK ADD CONSTRAINT FK_HoaDon_CaLamViec FOREIGN KEY (maCa) REFERENCES dbo.CaLamViec(maCa);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_HoaDon_KhuyenMai' AND parent_object_id=OBJECT_ID(N'dbo.HoaDon'))
    ALTER TABLE dbo.HoaDon WITH CHECK ADD CONSTRAINT FK_HoaDon_KhuyenMai FOREIGN KEY (maKhuyenMai) REFERENCES dbo.KhuyenMai(maKhuyenMai);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_CTSP_HoaDon' AND parent_object_id=OBJECT_ID(N'dbo.ChiTietHoaDon_SanPham'))
    ALTER TABLE dbo.ChiTietHoaDon_SanPham WITH CHECK ADD CONSTRAINT FK_CTSP_HoaDon FOREIGN KEY (maHoaDon) REFERENCES dbo.HoaDon(maHoaDon);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_CTSP_SanPham' AND parent_object_id=OBJECT_ID(N'dbo.ChiTietHoaDon_SanPham'))
    ALTER TABLE dbo.ChiTietHoaDon_SanPham WITH CHECK ADD CONSTRAINT FK_CTSP_SanPham FOREIGN KEY (maSanPham) REFERENCES dbo.SanPham(maSanPham);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_CTDV_HoaDon' AND parent_object_id=OBJECT_ID(N'dbo.ChiTietHoaDon_DichVu'))
    ALTER TABLE dbo.ChiTietHoaDon_DichVu WITH CHECK ADD CONSTRAINT FK_CTDV_HoaDon FOREIGN KEY (maHoaDon) REFERENCES dbo.HoaDon(maHoaDon);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_CTDV_DichVuSpa' AND parent_object_id=OBJECT_ID(N'dbo.ChiTietHoaDon_DichVu'))
    ALTER TABLE dbo.ChiTietHoaDon_DichVu WITH CHECK ADD CONSTRAINT FK_CTDV_DichVuSpa FOREIGN KEY (maDichVu) REFERENCES dbo.DichVuSpa(maDichVu);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_CTDV_ThuCung' AND parent_object_id=OBJECT_ID(N'dbo.ChiTietHoaDon_DichVu'))
    ALTER TABLE dbo.ChiTietHoaDon_DichVu WITH CHECK ADD CONSTRAINT FK_CTDV_ThuCung FOREIGN KEY (maThuCung) REFERENCES dbo.ThuCung(maThuCung);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_CTDV_ThoSpa' AND parent_object_id=OBJECT_ID(N'dbo.ChiTietHoaDon_DichVu'))
    ALTER TABLE dbo.ChiTietHoaDon_DichVu WITH CHECK ADD CONSTRAINT FK_CTDV_ThoSpa FOREIGN KEY (maNVThoSpa) REFERENCES dbo.NhanVien(maNV);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_PhieuKetCa_NhanVien' AND parent_object_id=OBJECT_ID(N'dbo.PhieuKetCa'))
    ALTER TABLE dbo.PhieuKetCa WITH CHECK ADD CONSTRAINT FK_PhieuKetCa_NhanVien FOREIGN KEY (maNV) REFERENCES dbo.NhanVien(maNV);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_PhieuKetCa_CaLamViec' AND parent_object_id=OBJECT_ID(N'dbo.PhieuKetCa'))
    ALTER TABLE dbo.PhieuKetCa WITH CHECK ADD CONSTRAINT FK_PhieuKetCa_CaLamViec FOREIGN KEY (maCa) REFERENCES dbo.CaLamViec(maCa);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_PhieuKetCa_QuanLyDuyet' AND parent_object_id=OBJECT_ID(N'dbo.PhieuKetCa'))
    ALTER TABLE dbo.PhieuKetCa WITH CHECK ADD CONSTRAINT FK_PhieuKetCa_QuanLyDuyet FOREIGN KEY (maNVQuanLyDuyet) REFERENCES dbo.NhanVien(maNV);
GO


-- Mo rong phuc vu UC002: thong tin doi/tra, luu vet tien hoan va ca doi soat.
IF OBJECT_ID(N'dbo.DoiTra', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.DoiTra (
        maDoiTra VARCHAR(20) NOT NULL CONSTRAINT PK_DoiTra PRIMARY KEY,
        maHoaDon VARCHAR(30) NOT NULL,
        maSanPhamCu VARCHAR(20) NOT NULL,
        maSanPhamMoi VARCHAR(20) NULL,
        maNV VARCHAR(20) NOT NULL,
        chenhLech DECIMAL(18,2) NOT NULL CONSTRAINT DF_DoiTra_ChenhLech DEFAULT (0),
        ngayDoi DATETIME2(0) NOT NULL CONSTRAINT DF_DoiTra_Ngay DEFAULT SYSDATETIME(),
        ghiChu NVARCHAR(1000) NULL,
        loaiXuLy VARCHAR(4) NOT NULL CONSTRAINT DF_DoiTra_Loai DEFAULT 'DOI',
        tienHoan DECIMAL(18,2) NOT NULL CONSTRAINT DF_DoiTra_TienHoan DEFAULT (0),
        maCa VARCHAR(20) NULL,
        maHoaDonDieuChinh VARCHAR(30) NULL,
        phuongThucXuLy VARCHAR(16) NULL,
        soLuongXuLy INT NOT NULL CONSTRAINT DF_DoiTra_SoLuong DEFAULT (1),
        CONSTRAINT UQ_DoiTra_LanDoi UNIQUE (maHoaDon, maSanPhamCu),
        CONSTRAINT CK_DoiTra_ThamSo CHECK (chenhLech >= 0 AND tienHoan >= 0 AND soLuongXuLy > 0),
        CONSTRAINT CK_DoiTra_Loai CHECK (loaiXuLy IN ('DOI','TRA')),
        CONSTRAINT CK_DoiTra_PhuongThuc CHECK (phuongThucXuLy IS NULL OR phuongThucXuLy IN ('TIEN_MAT','CHUYEN_KHOAN','QUA_SAN'))
    );
END;
GO
-- Compatibility upgrade: old exchange table has no return fields.
IF COL_LENGTH(N'dbo.DoiTra', N'loaiXuLy') IS NULL
    EXEC(N'ALTER TABLE dbo.DoiTra ADD loaiXuLy VARCHAR(4) NOT NULL CONSTRAINT DF_DoiTra_Loai DEFAULT (''DOI'') WITH VALUES;');
GO
IF COL_LENGTH(N'dbo.DoiTra', N'tienHoan') IS NULL
    EXEC(N'ALTER TABLE dbo.DoiTra ADD tienHoan DECIMAL(18,2) NOT NULL CONSTRAINT DF_DoiTra_TienHoan DEFAULT (0) WITH VALUES;');
GO
IF COL_LENGTH(N'dbo.DoiTra', N'maCa') IS NULL
    ALTER TABLE dbo.DoiTra ADD maCa VARCHAR(20) NULL;
GO
IF COL_LENGTH(N'dbo.DoiTra', N'maHoaDonDieuChinh') IS NULL
    ALTER TABLE dbo.DoiTra ADD maHoaDonDieuChinh VARCHAR(30) NULL;
GO
IF COL_LENGTH(N'dbo.DoiTra', N'phuongThucXuLy') IS NULL
    ALTER TABLE dbo.DoiTra ADD phuongThucXuLy VARCHAR(16) NULL;
GO
IF COL_LENGTH(N'dbo.DoiTra', N'soLuongXuLy') IS NULL
    EXEC(N'ALTER TABLE dbo.DoiTra ADD soLuongXuLy INT NOT NULL CONSTRAINT DF_DoiTra_SoLuong DEFAULT (1) WITH VALUES;');
GO
-- Old DoiTra may have an FK on replacement product: drop/recreate it ATOMICALLY
-- before changing NOT NULL -> NULL; never change existing row values.
IF EXISTS (SELECT 1 FROM sys.columns
    WHERE object_id=OBJECT_ID(N'dbo.DoiTra') AND name=N'maSanPhamMoi' AND is_nullable=0)
BEGIN
    BEGIN TRY
        BEGIN TRANSACTION;
        IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id=OBJECT_ID(N'dbo.DoiTra') AND name=N'FK_DoiTra_SP_Moi')
            ALTER TABLE dbo.DoiTra DROP CONSTRAINT FK_DoiTra_SP_Moi;
        ALTER TABLE dbo.DoiTra ALTER COLUMN maSanPhamMoi VARCHAR(20) NULL;
        ALTER TABLE dbo.DoiTra WITH CHECK ADD CONSTRAINT FK_DoiTra_SP_Moi
            FOREIGN KEY (maSanPhamMoi) REFERENCES dbo.SanPham(maSanPham);
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE parent_object_id=OBJECT_ID(N'dbo.DoiTra') AND name=N'CK_DoiTra_ThamSo')
    ALTER TABLE dbo.DoiTra WITH CHECK ADD CONSTRAINT CK_DoiTra_ThamSo CHECK (chenhLech>=0 AND tienHoan>=0 AND soLuongXuLy>0);
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE parent_object_id=OBJECT_ID(N'dbo.DoiTra') AND name=N'CK_DoiTra_Loai')
    ALTER TABLE dbo.DoiTra WITH CHECK ADD CONSTRAINT CK_DoiTra_Loai CHECK (loaiXuLy IN ('DOI','TRA'));
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id=OBJECT_ID(N'dbo.DoiTra') AND name=N'FK_DoiTra_HD')
    ALTER TABLE dbo.DoiTra WITH CHECK ADD CONSTRAINT FK_DoiTra_HD FOREIGN KEY (maHoaDon) REFERENCES dbo.HoaDon(maHoaDon);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id=OBJECT_ID(N'dbo.DoiTra') AND name=N'FK_DoiTra_SP_Cu')
    ALTER TABLE dbo.DoiTra WITH CHECK ADD CONSTRAINT FK_DoiTra_SP_Cu FOREIGN KEY (maSanPhamCu) REFERENCES dbo.SanPham(maSanPham);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id=OBJECT_ID(N'dbo.DoiTra') AND name=N'FK_DoiTra_SP_Moi')
    ALTER TABLE dbo.DoiTra WITH CHECK ADD CONSTRAINT FK_DoiTra_SP_Moi FOREIGN KEY (maSanPhamMoi) REFERENCES dbo.SanPham(maSanPham);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id=OBJECT_ID(N'dbo.DoiTra') AND name=N'FK_DoiTra_NV')
    ALTER TABLE dbo.DoiTra WITH CHECK ADD CONSTRAINT FK_DoiTra_NV FOREIGN KEY (maNV) REFERENCES dbo.NhanVien(maNV);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id=OBJECT_ID(N'dbo.DoiTra') AND name=N'FK_DoiTra_Ca')
    ALTER TABLE dbo.DoiTra WITH CHECK ADD CONSTRAINT FK_DoiTra_Ca FOREIGN KEY (maCa) REFERENCES dbo.CaLamViec(maCa);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE parent_object_id=OBJECT_ID(N'dbo.DoiTra') AND name=N'FK_DoiTra_HoaDonDieuChinh')
    ALTER TABLE dbo.DoiTra WITH CHECK ADD CONSTRAINT FK_DoiTra_HoaDonDieuChinh FOREIGN KEY (maHoaDonDieuChinh) REFERENCES dbo.HoaDon(maHoaDon);
GO

-- Mo rong phuc vu UC nhap kho: mot dong nhap, gia nhap va han ThucAn (neu co).
IF OBJECT_ID(N'dbo.LichSuNhapKho', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.LichSuNhapKho (
        maLichSu BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_LichSuNhapKho PRIMARY KEY,
        maSanPham VARCHAR(20) NOT NULL,
        soLuongNhap INT NOT NULL,
        giaNhap DECIMAL(18,2) NOT NULL,
        hanSuDungMoi DATE NULL,
        ngayNhap DATETIME2(0) NOT NULL CONSTRAINT DF_LSNK_NgayNhap DEFAULT SYSDATETIME(),
        ghiChu NVARCHAR(500) NULL,
        CONSTRAINT CK_LSNK_SoLuong CHECK (soLuongNhap > 0 AND giaNhap >= 0),
        CONSTRAINT FK_LSNK_SanPham FOREIGN KEY (maSanPham) REFERENCES dbo.SanPham(maSanPham)
    );
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.HoaDon') AND name=N'IX_HoaDon_NgayLap')
    CREATE INDEX IX_HoaDon_NgayLap ON dbo.HoaDon(ngayLap DESC) INCLUDE (maKH, maNV, trangThai, tienPhaiThu);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.DoiTra') AND name=N'IX_DoiTra_MaCa')
    CREATE INDEX IX_DoiTra_MaCa ON dbo.DoiTra(maCa, loaiXuLy);
GO

-- Thong ke doanh thu theo ca; KHONG co trigger tru kho/tich diem lan hai.
IF OBJECT_ID(N'dbo.usp_DoanhThuTheoCa', N'P') IS NULL
    EXEC(N'CREATE PROCEDURE dbo.usp_DoanhThuTheoCa AS SELECT 1;');
GO
ALTER PROCEDURE dbo.usp_DoanhThuTheoCa @maCa VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT maCa, phuongThucThanhToan, COUNT(*) AS soHoaDon,
           SUM(tienPhaiThu) AS doanhThu
    FROM dbo.HoaDon
    WHERE maCa=@maCa AND trangThai='DA_THANH_TOAN'
    GROUP BY maCa, phuongThucThanhToan;
END;
GO

PRINT N'TiTi Pet schema ready: 14 core tables, extension DoiTra / LichSuNhapKho, 2 views, FK and reporting procedure.';
SELECT DB_NAME() AS databaseName,
       (SELECT COUNT(*) FROM sys.tables WHERE schema_id=SCHEMA_ID('dbo')) AS soBang,
       (SELECT COUNT(*) FROM sys.views WHERE schema_id=SCHEMA_ID('dbo')) AS soView;
GO
