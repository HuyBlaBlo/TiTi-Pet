package com.titipet.tests;

import com.titipet.bus.BanHangService;
import com.titipet.dao.BanHangSanPhamDAO;
import com.titipet.entity.SanPhamBanHang;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Kiểm thử giỏ hàng độc lập.
 * Không cần kết nối SQL Server.
 */
public class BanHangServiceTest {

    private static final SanPhamBanHang PATE =
            new SanPhamBanHang(
                    "SP001",
                    "Pate cho mèo",
                    "893001",
                    new BigDecimal("20000"),
                    5
            );

    private static final SanPhamBanHang DAY =
            new SanPhamBanHang(
                    "SP002",
                    "Dây dắt",
                    "893002",
                    new BigDecimal("35000"),
                    8
            );

    /**
     * DAO mô phỏng cho kiểm thử.
     */
    private static class DAOTest
            extends BanHangSanPhamDAO {

        @Override
        public List<SanPhamBanHang> timSanPham(
                String tuKhoa
        ) throws SQLException {

            return List.of(PATE, DAY);
        }
    }

    private static void check(
            boolean dung,
            String thongBao
    ) {

        if (!dung) {
            throw new AssertionError(thongBao);
        }
    }

    private static void expectError(
            Runnable action
    ) {

        try {

            action.run();

            throw new AssertionError(
                    "Đáng lẽ phải báo lỗi"
            );

        } catch (IllegalArgumentException expected) {

            // Đã chặn đầu vào không hợp lệ.
        }
    }

    public static void main(String[] args) {

        BanHangService bus =
                new BanHangService(
                        new DAOTest()
                );

        check(
                bus.timSanPham("").size() == 2,
                "Không tải được sản phẩm test"
        );

        bus.themVaoGio(PATE, 2);

        bus.themVaoGio(PATE, 1);

        check(
                bus.layGioHang().size() == 1,
                "Chưa gộp hàng trùng mã"
        );

        check(
                bus.layGioHang()
                        .getFirst()
                        .soLuong() == 3,
                "Số lượng gộp sai"
        );

        bus.themVaoGio(DAY, 2);

        check(
                bus.tinhTongTienHang()
                        .compareTo(
                                new BigDecimal("130000")
                        ) == 0,
                "Tính tiền sai"
        );

        expectError(
                () -> bus.themVaoGio(PATE, 3)
        );

        expectError(
                () -> bus.themVaoGio(PATE, 0)
        );

        bus.capNhatSoLuong("SP001", 5);

        expectError(
                () -> bus.capNhatSoLuong(
                        "SP001", 6
                )
        );

        bus.xoaKhoiGio("SP002");

        check(
                bus.tinhTongTienHang()
                        .compareTo(
                                new BigDecimal("100000")
                        ) == 0,
                "Xóa dòng tính tiền sai"
        );

        bus.xoaToanBoGio();

        check(
                bus.layGioHang().isEmpty(),
                "Xóa giỏ không thành công"
        );

        check(
                bus.tinhTongTienHang()
                        .signum() == 0,
                "Giỏ rỗng không bằng 0"
        );

        System.out.println(
                "PASS: 10 checks POS logic"
        );
    }
}