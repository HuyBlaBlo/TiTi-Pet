/*
    Tai khoan dang nhap:
    - admin/Admin@123      (QUAN_LY)
    - thungan/ThuNgan@123    (THU_NGAN)
    - thospa/ThoSpa@123     (THO_SPA)
    - locked/Locked@123     (bi khoa, dung de test dang nhap that bai)

    Script co the chay lai. Cac ban ghi co ma *_TEST_* se duoc cap nhat.
*/

USE TiTiPetDB;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    /* 1. Nhan vien - mat khau da bam BCrypt cost 12 */
    MERGE dbo.NhanVien AS target
    USING (VALUES
        ('NV_TEST_QL', 'admin',
         '$2a$12$.djq6oBiz9DVfdAcKBJKaOw3dY4SrbhlMNum/TBdus9CAsaBqhpnO',
         N'Nguyễn Minh Quản', 'QUAN_LY', CAST(1 AS BIT)),
        ('NV_TEST_TN', 'thungan',
         '$2a$12$rJbf2YSDbVuHs.k6hhwGbefEdhubhnK3eIjAzQgh4PmmoZv4nS9jW',
         N'Trần Thị Ngân', 'THU_NGAN', CAST(1 AS BIT)),
        ('NV_TEST_SPA', 'thospa',
         '$2a$12$bNoIqj0Ij8Kktu6rl1jGo.Sws17jka58YmsnHpITMp1GEl9gcsjTa',
         N'Lê Hoàng Spa', 'THO_SPA', CAST(1 AS BIT)),
        ('NV_TEST_LOCK', 'locked',
         '$2a$12$30RHRSmlOvTw.x8yigTbyuoOi4BzLdg399/2JwIoqjSJ3/9ExBD52',
         N'Tài Khoản Bị Khóa', 'THU_NGAN', CAST(0 AS BIT))
    ) AS source(maNV, tenDangNhap, matKhau, hoTen, vaiTro, dangHoatDong)
    ON target.maNV = source.maNV
    WHEN MATCHED THEN UPDATE SET
        tenDangNhap = source.tenDangNhap,
        matKhau = source.matKhau,
        hoTen = source.hoTen,
        vaiTro = source.vaiTro,
        dangHoatDong = source.dangHoatDong
    WHEN NOT MATCHED THEN INSERT
        (maNV, tenDangNhap, matKhau, hoTen, vaiTro, dangHoatDong)
        VALUES (source.maNV, source.tenDangNhap, source.matKhau,
                source.hoTen, source.vaiTro, source.dangHoatDong);

    /* 2. Khach hang - du 4 hang thanh vien */
    MERGE dbo.KhachHang AS target
    USING (VALUES
        ('KH_TEST_01', N'Nguyễn An', '0901000001', 'THUONG', 20),
        ('KH_TEST_02', N'Trần Bình',  '0901000002', 'HANG_1', 150),
        ('KH_TEST_03', N'Lê Chi',     '0901000003', 'HANG_2', 450),
        ('KH_TEST_04', N'Phạm Dũng',  '0901000004', 'HANG_3', 900)
    ) AS source(maKH, hoTen, soDienThoai, hangTV, diemTichLuy)
    ON target.maKH = source.maKH
    WHEN MATCHED THEN UPDATE SET
        hoTen = source.hoTen,
        soDienThoai = source.soDienThoai,
        hangTV = source.hangTV,
        diemTichLuy = source.diemTichLuy
    WHEN NOT MATCHED THEN INSERT
        (maKH, hoTen, soDienThoai, hangTV, diemTichLuy)
        VALUES (source.maKH, source.hoTen, source.soDienThoai,
                source.hangTV, source.diemTichLuy);

    /* 3. Dich vu Spa */
    MERGE dbo.DichVuSpa AS target
    USING (VALUES
        ('DV_TEST_TAM', N'Tắm vệ sinh', 150000.00, 5.00, CAST(1 AS BIT), 'CA_HAI', CAST(1 AS BIT)),
        ('DV_TEST_CAT', N'Cắt tỉa lông', 250000.00, 5.00, CAST(1 AS BIT), 'CA_HAI', CAST(1 AS BIT)),
        ('DV_TEST_MONG', N'Cắt móng', 70000.00, 5.00, CAST(1 AS BIT), 'CA_HAI', CAST(1 AS BIT)),
        ('DV_TEST_OFF', N'Dịch vụ tạm ngưng', 100000.00, 5.00, CAST(1 AS BIT), 'CHO', CAST(0 AS BIT))
    ) AS source(maDichVu, tenDichVu, donGiaCoBan, thueSuat,
                giaDaBaoGomThue, loaiThuCungApDung, dangHoatDong)
    ON target.maDichVu = source.maDichVu
    WHEN MATCHED THEN UPDATE SET
        tenDichVu = source.tenDichVu,
        donGiaCoBan = source.donGiaCoBan,
        thueSuat = source.thueSuat,
        giaDaBaoGomThue = source.giaDaBaoGomThue,
        loaiThuCungApDung = source.loaiThuCungApDung,
        dangHoatDong = source.dangHoatDong
    WHEN NOT MATCHED THEN INSERT
        (maDichVu, tenDichVu, donGiaCoBan, thueSuat,
         giaDaBaoGomThue, loaiThuCungApDung, dangHoatDong)
        VALUES (source.maDichVu, source.tenDichVu, source.donGiaCoBan,
                source.thueSuat, source.giaDaBaoGomThue,
                source.loaiThuCungApDung, source.dangHoatDong);

    /* 4. San pham - co du hang, sap het, het han va ngung kinh doanh */
    MERGE dbo.SanPham AS target
    USING (VALUES
        ('SP_TEST_TA01', N'Hạt cho chó 1 kg', 120000.00, 30, '893TEST000001', 5, 5.00, 'THUC_AN', CAST(1 AS BIT)),
        ('SP_TEST_TA02', N'Pate cho mèo', 45000.00, 3, '893TEST000002', 5, 5.00, 'THUC_AN', CAST(1 AS BIT)),
        ('SP_TEST_TA03', N'Bánh thưởng đã hết hạn', 60000.00, 8, '893TEST000003', 3, 5.00, 'THUC_AN', CAST(1 AS BIT)),
        ('SP_TEST_PK01', N'Dây dắt thú cưng', 175000.00, 15, '893TEST000004', 4, 8.00, 'PHU_KIEN', CAST(1 AS BIT)),
        ('SP_TEST_PK02', N'Vòng cổ chuông', 85000.00, 2, '893TEST000005', 5, 8.00, 'PHU_KIEN', CAST(1 AS BIT)),
        ('SP_TEST_OFF', N'Sản phẩm ngừng kinh doanh', 50000.00, 0, '893TEST000006', 2, 8.00, 'PHU_KIEN', CAST(0 AS BIT))
    ) AS source(maSanPham, tenSanPham, giaBan, soLuongTonKho, maVach,
                nguongCanhBaoTon, thueSuat, loaiSanPham, dangKinhDoanh)
    ON target.maSanPham = source.maSanPham
    WHEN MATCHED THEN UPDATE SET
        tenSanPham = source.tenSanPham,
        giaBan = source.giaBan,
        soLuongTonKho = source.soLuongTonKho,
        maVach = source.maVach,
        nguongCanhBaoTon = source.nguongCanhBaoTon,
        thueSuat = source.thueSuat,
        loaiSanPham = source.loaiSanPham,
        dangKinhDoanh = source.dangKinhDoanh
    WHEN NOT MATCHED THEN INSERT
        (maSanPham, tenSanPham, giaBan, soLuongTonKho, maVach,
         nguongCanhBaoTon, thueSuat, loaiSanPham, dangKinhDoanh)
        VALUES (source.maSanPham, source.tenSanPham, source.giaBan,
                source.soLuongTonKho, source.maVach, source.nguongCanhBaoTon,
                source.thueSuat, source.loaiSanPham, source.dangKinhDoanh);

    MERGE dbo.ThucAn AS target
    USING (VALUES
        ('SP_TEST_TA01', DATEADD(DAY, 180, CONVERT(date, SYSDATETIME())), CAST(1.000 AS DECIMAL(10,3))),
        ('SP_TEST_TA02', DATEADD(DAY, 90, CONVERT(date, SYSDATETIME())), CAST(0.085 AS DECIMAL(10,3))),
        ('SP_TEST_TA03', DATEADD(DAY, -10, CONVERT(date, SYSDATETIME())), CAST(0.200 AS DECIMAL(10,3)))
    ) AS source(maSanPham, hanSuDung, trongLuong)
    ON target.maSanPham = source.maSanPham
    WHEN MATCHED THEN UPDATE SET
        hanSuDung = source.hanSuDung,
        trongLuong = source.trongLuong
    WHEN NOT MATCHED THEN INSERT (maSanPham, hanSuDung, trongLuong)
        VALUES (source.maSanPham, source.hanSuDung, source.trongLuong);

    MERGE dbo.PhuKien AS target
    USING (VALUES
        ('SP_TEST_PK01', N'M', N'Đỏ', N'Vải nylon'),
        ('SP_TEST_PK02', N'S', N'Xanh', N'Vải và kim loại'),
        ('SP_TEST_OFF', N'L', N'Đen', N'Nhựa')
    ) AS source(maSanPham, kichThuoc, mauSac, chatLieu)
    ON target.maSanPham = source.maSanPham
    WHEN MATCHED THEN UPDATE SET
        kichThuoc = source.kichThuoc,
        mauSac = source.mauSac,
        chatLieu = source.chatLieu
    WHEN NOT MATCHED THEN INSERT (maSanPham, kichThuoc, mauSac, chatLieu)
        VALUES (source.maSanPham, source.kichThuoc, source.mauSac, source.chatLieu);

    /* 5. Thu cung */
    MERGE dbo.ThuCung AS target
    USING (VALUES
        ('TC_TEST_01', 'KH_TEST_01', N'Milo', 'CHO', 8.50, N'Bình thường', N'Dễ gần'),
        ('TC_TEST_02', 'KH_TEST_02', N'Mimi', 'MEO', 3.20, N'Lông hơi rối', N'Sợ tiếng máy sấy'),
        ('TC_TEST_03', 'KH_TEST_03', N'Bông', 'CHO', 15.00, N'Lông dài', NULL),
        ('TC_TEST_04', 'KH_TEST_04', N'Mướp', 'MEO', 4.10, N'Bình thường', NULL)
    ) AS source(maThuCung, maKH, tenThuCung, loaiThuCung,
                canNang, tinhTrangLong, ghiChu)
    ON target.maThuCung = source.maThuCung
    WHEN MATCHED THEN UPDATE SET
        maKH = source.maKH,
        tenThuCung = source.tenThuCung,
        loaiThuCung = source.loaiThuCung,
        canNang = source.canNang,
        tinhTrangLong = source.tinhTrangLong,
        ghiChu = source.ghiChu
    WHEN NOT MATCHED THEN INSERT
        (maThuCung, maKH, tenThuCung, loaiThuCung,
         canNang, tinhTrangLong, ghiChu)
        VALUES (source.maThuCung, source.maKH, source.tenThuCung,
                source.loaiThuCung, source.canNang,
                source.tinhTrangLong, source.ghiChu);

    /* 6. Ca lam viec */
    MERGE dbo.CaLamViec AS target
    USING (VALUES
        ('CA_TEST_OPEN', 'NV_TEST_TN',
         DATEADD(HOUR, 8, CAST(CONVERT(date, SYSDATETIME()) AS datetime2)),
         CAST(NULL AS datetime2), 1000000.00, 'DANG_MO', N'Ca đang mở để test bán hàng'),
        ('CA_TEST_DONE', 'NV_TEST_TN',
         DATEADD(HOUR, 8, CAST(DATEADD(DAY, -1, CONVERT(date, SYSDATETIME())) AS datetime2)),
         DATEADD(HOUR, 17, CAST(DATEADD(DAY, -1, CONVERT(date, SYSDATETIME())) AS datetime2)),
         1000000.00, 'DA_CHOT', N'Ca đã chốt để test lịch sử')
    ) AS source(maCa, maNV, thoiGianBatDau, thoiGianKetThuc,
                tienDauCa, trangThai, ghiChu)
    ON target.maCa = source.maCa
    WHEN MATCHED THEN UPDATE SET
        maNV = source.maNV,
        thoiGianBatDau = source.thoiGianBatDau,
        thoiGianKetThuc = source.thoiGianKetThuc,
        tienDauCa = source.tienDauCa,
        trangThai = source.trangThai,
        ghiChu = source.ghiChu
    WHEN NOT MATCHED THEN INSERT
        (maCa, maNV, thoiGianBatDau, thoiGianKetThuc,
         tienDauCa, trangThai, ghiChu)
        VALUES (source.maCa, source.maNV, source.thoiGianBatDau,
                source.thoiGianKetThuc, source.tienDauCa,
                source.trangThai, source.ghiChu);

    /* 7. Khuyen mai */
    MERGE dbo.KhuyenMai AS target
    USING (VALUES
        ('KM_TEST_ALL', N'Giảm 10% toàn đơn',
         DATEADD(DAY, -7, SYSDATETIME()), DATEADD(DAY, 30, SYSDATETIME()),
         10.00, CAST(1 AS BIT), 'TOAN_DON', NULL, NULL),
        ('KM_TEST_FOOD', N'Giảm 15% hạt cho chó',
         DATEADD(DAY, -7, SYSDATETIME()), DATEADD(DAY, 30, SYSDATETIME()),
         15.00, CAST(1 AS BIT), 'THUC_AN', 'SP_TEST_TA01', NULL),
        ('KM_TEST_SPA', N'Giảm 20% dịch vụ tắm',
         DATEADD(DAY, -7, SYSDATETIME()), DATEADD(DAY, 30, SYSDATETIME()),
         20.00, CAST(1 AS BIT), 'DICH_VU', NULL, 'DV_TEST_TAM'),
        ('KM_TEST_OLD', N'Khuyến mãi đã kết thúc',
         DATEADD(DAY, -60, SYSDATETIME()), DATEADD(DAY, -30, SYSDATETIME()),
         5.00, CAST(0 AS BIT), 'TOAN_DON', NULL, NULL)
    ) AS source(maKhuyenMai, tenChuongTrinh, ngayBatDau, ngayKetThuc,
                giamTram, trangThai, phamVi, maSanPhamApDung, maDichVuApDung)
    ON target.maKhuyenMai = source.maKhuyenMai
    WHEN MATCHED THEN UPDATE SET
        tenChuongTrinh = source.tenChuongTrinh,
        ngayBatDau = source.ngayBatDau,
        ngayKetThuc = source.ngayKetThuc,
        giamTram = source.giamTram,
        trangThai = source.trangThai,
        phamVi = source.phamVi,
        maSanPhamApDung = source.maSanPhamApDung,
        maDichVuApDung = source.maDichVuApDung
    WHEN NOT MATCHED THEN INSERT
        (maKhuyenMai, tenChuongTrinh, ngayBatDau, ngayKetThuc,
         giamTram, trangThai, phamVi, maSanPhamApDung, maDichVuApDung)
        VALUES (source.maKhuyenMai, source.tenChuongTrinh,
                source.ngayBatDau, source.ngayKetThuc, source.giamTram,
                source.trangThai, source.phamVi,
                source.maSanPhamApDung, source.maDichVuApDung);

    /* 8. Lich hen */
    MERGE dbo.LichHen AS target
    USING (VALUES
        ('LH_TEST_01', DATEADD(DAY, 1, SYSDATETIME()),
         DATEADD(MINUTE, 60, DATEADD(DAY, 1, SYSDATETIME())),
         'CHO_TIEP_NHAN', 'KH_TEST_01', 'TC_TEST_01', 'DV_TEST_TAM',
         'NV_TEST_SPA', N'Lịch hẹn sắp tới'),
        ('LH_TEST_02', DATEADD(DAY, -1, SYSDATETIME()),
         DATEADD(MINUTE, 90, DATEADD(DAY, -1, SYSDATETIME())),
         'DA_TIEP_NHAN', 'KH_TEST_02', 'TC_TEST_02', 'DV_TEST_CAT',
         'NV_TEST_SPA', N'Đã tiếp nhận'),
        ('LH_TEST_03', DATEADD(DAY, 2, SYSDATETIME()),
         DATEADD(MINUTE, 30, DATEADD(DAY, 2, SYSDATETIME())),
         'CHO_TIEP_NHAN', 'KH_TEST_03', 'TC_TEST_03', 'DV_TEST_MONG',
         NULL, N'Chưa xếp thợ'),
        ('LH_TEST_04', DATEADD(DAY, -2, SYSDATETIME()),
         DATEADD(MINUTE, 60, DATEADD(DAY, -2, SYSDATETIME())),
         'HUY', 'KH_TEST_04', 'TC_TEST_04', 'DV_TEST_TAM',
         'NV_TEST_SPA', N'Khách đã hủy')
    ) AS source(maLichHen, thoiGianHen, thoiGianKetThuc, trangThai,
                maKH, maThuCung, maDichVu, maNVThoSpa, ghiChu)
    ON target.maLichHen = source.maLichHen
    WHEN MATCHED THEN UPDATE SET
        thoiGianHen = source.thoiGianHen,
        thoiGianKetThuc = source.thoiGianKetThuc,
        trangThai = source.trangThai,
        maKH = source.maKH,
        maThuCung = source.maThuCung,
        maDichVu = source.maDichVu,
        maNVThoSpa = source.maNVThoSpa,
        ghiChu = source.ghiChu
    WHEN NOT MATCHED THEN INSERT
        (maLichHen, thoiGianHen, thoiGianKetThuc, trangThai,
         maKH, maThuCung, maDichVu, maNVThoSpa, ghiChu)
        VALUES (source.maLichHen, source.thoiGianHen,
                source.thoiGianKetThuc, source.trangThai, source.maKH,
                source.maThuCung, source.maDichVu,
                source.maNVThoSpa, source.ghiChu);

    /* 9. Hoa don */
    MERGE dbo.HoaDon AS target
    USING (VALUES
        ('HD_TEST_01', DATEADD(DAY, -1, SYSDATETIME()),
         'KH_TEST_02', 'NV_TEST_TN', 'CA_TEST_DONE', NULL,
         'TAI_QUAY', NULL, 'TIEN_MAT', 'DA_THANH_TOAN',
         350000.00, 150000.00, 0.00, 15000.00, 0, 0.00,
         485000.00, 48, 476190.47, 23809.53, 500000.00),
        ('HD_TEST_02', DATEADD(DAY, -1, SYSDATETIME()),
         'KH_TEST_01', 'NV_TEST_TN', 'CA_TEST_DONE', 'KM_TEST_ALL',
         'SHOPEE', 'SHOPEE-TEST-001', 'CHUYEN_KHOAN', 'DA_THANH_TOAN',
         240000.00, 0.00, 24000.00, 0.00, 0, 0.00,
         216000.00, 21, 205714.29, 10285.71, 216000.00),
        ('HD_TEST_03', SYSDATETIME(),
         'KH_TEST_03', 'NV_TEST_TN', 'CA_TEST_OPEN', NULL,
         'TAI_QUAY', NULL, NULL, 'CHO_THANH_TOAN',
         0.00, 250000.00, 0.00, 0.00, 0, 0.00,
         250000.00, 0, 238095.24, 11904.76, 250000.00),
        ('HD_TEST_04', DATEADD(DAY, -3, SYSDATETIME()),
         'KH_TEST_04', 'NV_TEST_TN', NULL, NULL,
         'TIKTOK', 'TIKTOK-TEST-001', NULL, 'HUY',
         85000.00, 0.00, 0.00, 0.00, 0, 0.00,
         0.00, 0, 0.00, 0.00, 0.00)
    ) AS source(
        maHoaDon, ngayLap, maKH, maNV, maCa, maKhuyenMai,
        nguonDon, maDonKenh, phuongThucThanhToan, trangThai,
        tongTienHang, tongTienSpa, tienGiamKM, tienGiamHangTV,
        soDiemSuDung, tienGiamDiem, tienPhaiThu, soDiemDuocTich,
        tongTienTruocThue, tongTienThue, tongTien
    )
    ON target.maHoaDon = source.maHoaDon
    WHEN MATCHED THEN UPDATE SET
        ngayLap = source.ngayLap,
        maKH = source.maKH,
        maNV = source.maNV,
        maCa = source.maCa,
        maKhuyenMai = source.maKhuyenMai,
        nguonDon = source.nguonDon,
        maDonKenh = source.maDonKenh,
        phuongThucThanhToan = source.phuongThucThanhToan,
        trangThai = source.trangThai,
        tongTienHang = source.tongTienHang,
        tongTienSpa = source.tongTienSpa,
        tienGiamKM = source.tienGiamKM,
        tienGiamHangTV = source.tienGiamHangTV,
        soDiemSuDung = source.soDiemSuDung,
        tienGiamDiem = source.tienGiamDiem,
        tienPhaiThu = source.tienPhaiThu,
        soDiemDuocTich = source.soDiemDuocTich,
        tongTienTruocThue = source.tongTienTruocThue,
        tongTienThue = source.tongTienThue,
        tongTien = source.tongTien
    WHEN NOT MATCHED THEN INSERT
        (maHoaDon, ngayLap, maKH, maNV, maCa, maKhuyenMai,
         nguonDon, maDonKenh, phuongThucThanhToan, trangThai,
         tongTienHang, tongTienSpa, tienGiamKM, tienGiamHangTV,
         soDiemSuDung, tienGiamDiem, tienPhaiThu, soDiemDuocTich,
         tongTienTruocThue, tongTienThue, tongTien)
        VALUES
        (source.maHoaDon, source.ngayLap, source.maKH, source.maNV,
         source.maCa, source.maKhuyenMai, source.nguonDon,
         source.maDonKenh, source.phuongThucThanhToan, source.trangThai,
         source.tongTienHang, source.tongTienSpa, source.tienGiamKM,
         source.tienGiamHangTV, source.soDiemSuDung,
         source.tienGiamDiem, source.tienPhaiThu,
         source.soDiemDuocTich, source.tongTienTruocThue,
         source.tongTienThue, source.tongTien);

    /* Chi tiet test duoc tao lai de tranh trung khoa chinh */
    DELETE FROM dbo.ChiTietHoaDon_SanPham
    WHERE maHoaDon LIKE 'HD_TEST_%';

    INSERT INTO dbo.ChiTietHoaDon_SanPham
        (maHoaDon, maSanPham, soLuong, donGia, thueSuat,
         giaDaBaoGomThue, tienGiamPhanBo, tienTruocThue, tienThue)
    VALUES
        ('HD_TEST_01', 'SP_TEST_PK01', 2, 175000.00, 5.00,
         1, 0.00, 333333.33, 16666.67),
        ('HD_TEST_02', 'SP_TEST_TA01', 2, 120000.00, 5.00,
         1, 24000.00, 205714.29, 10285.71),
        ('HD_TEST_04', 'SP_TEST_PK02', 1, 85000.00, 8.00,
         1, 0.00, 0.00, 0.00);

    DELETE FROM dbo.ChiTietHoaDon_DichVu
    WHERE maHoaDon LIKE 'HD_TEST_%';

    INSERT INTO dbo.ChiTietHoaDon_DichVu
        (maHoaDon, maDichVu, maThuCung, maNVThoSpa,
         trangThaiThucHien, soLuong, donGia, thueSuat,
         giaDaBaoGomThue, tienGiamPhanBo, tienTruocThue, tienThue)
    VALUES
        ('HD_TEST_01', 'DV_TEST_TAM', 'TC_TEST_02', 'NV_TEST_SPA',
         'HOAN_THANH', 1, 150000.00, 5.00, 1, 0.00, 142857.14, 7142.86),
        ('HD_TEST_03', 'DV_TEST_CAT', 'TC_TEST_03', 'NV_TEST_SPA',
         'CHO_LAM', 1, 250000.00, 5.00, 1, 0.00, 238095.24, 11904.76);

    /* 10. Phieu ket ca */
    MERGE dbo.PhieuKetCa AS target
    USING (VALUES
        ('PKC_TEST_01', 'NV_TEST_TN', 'CA_TEST_DONE',
         1000000.00, 485000.00, 216000.00, 701000.00,
         DATEADD(DAY, -1, SYSDATETIME()), 'DA_DUYET',
         'NV_TEST_QL', DATEADD(DAY, -1, SYSDATETIME()))
    ) AS source(
        maPhieu, maNV, maCa, soTienBanDauCa, tienMat, tienCK,
        tongTienKetCa, ngayLapPhieu, trangThaiDuyet,
        maNVQuanLyDuyet, ngayDuyet
    )
    ON target.maPhieu = source.maPhieu
    WHEN MATCHED THEN UPDATE SET
        maNV = source.maNV,
        maCa = source.maCa,
        soTienBanDauCa = source.soTienBanDauCa,
        tienMat = source.tienMat,
        tienCK = source.tienCK,
        tongTienKetCa = source.tongTienKetCa,
        ngayLapPhieu = source.ngayLapPhieu,
        trangThaiDuyet = source.trangThaiDuyet,
        maNVQuanLyDuyet = source.maNVQuanLyDuyet,
        ngayDuyet = source.ngayDuyet
    WHEN NOT MATCHED THEN INSERT
        (maPhieu, maNV, maCa, soTienBanDauCa, tienMat, tienCK,
         tongTienKetCa, ngayLapPhieu, trangThaiDuyet,
         maNVQuanLyDuyet, ngayDuyet)
        VALUES
        (source.maPhieu, source.maNV, source.maCa,
         source.soTienBanDauCa, source.tienMat, source.tienCK,
         source.tongTienKetCa, source.ngayLapPhieu,
         source.trangThaiDuyet, source.maNVQuanLyDuyet,
         source.ngayDuyet);

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

PRINT N'Đã tạo/cập nhật dữ liệu test TiTiPet.';
PRINT N'Tài khoản: admin, thungan, thospa; xem mật khẩu ở đầu file.';
GO

/* Kiem tra nhanh, khong hien thi password hash */
SELECT maNV, tenDangNhap, hoTen, vaiTro, dangHoatDong
FROM dbo.NhanVien
WHERE maNV LIKE 'NV_TEST_%'
ORDER BY maNV;

SELECT
    (SELECT COUNT(*) FROM dbo.KhachHang WHERE maKH LIKE 'KH_TEST_%') AS soKhachHang,
    (SELECT COUNT(*) FROM dbo.ThuCung WHERE maThuCung LIKE 'TC_TEST_%') AS soThuCung,
    (SELECT COUNT(*) FROM dbo.SanPham WHERE maSanPham LIKE 'SP_TEST_%') AS soSanPham,
    (SELECT COUNT(*) FROM dbo.LichHen WHERE maLichHen LIKE 'LH_TEST_%') AS soLichHen,
    (SELECT COUNT(*) FROM dbo.HoaDon WHERE maHoaDon LIKE 'HD_TEST_%') AS soHoaDon;
GO
