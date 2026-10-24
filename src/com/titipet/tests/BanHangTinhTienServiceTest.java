package com.titipet.tests;

import com.titipet.bus.BanHangTinhTienService;
import com.titipet.entity.KhachHangBanHang;
import java.math.BigDecimal;

public class BanHangTinhTienServiceTest {
    private static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) throw new AssertionError("Expected " + expected + ", actual " + actual);
    }
    public static void main(String[] args) {
        BanHangTinhTienService s = new BanHangTinhTienService();
        KhachHangBanHang k = new KhachHangBanHang("K1", "Khách", "0123", "HANG_1", 12);
        var r = s.uocTinh(BigDecimal.valueOf(200000), k, 10);
        assertEquals(3, r.phanTramHang());
        assertEquals(new BigDecimal("6000"), r.giamHang());
        assertEquals(new BigDecimal("10000"), r.giamDiem());
        assertEquals(new BigDecimal("184000"), r.phaiThu());
        var max = s.uocTinh(BigDecimal.valueOf(1500), k, 12);
        assertEquals(1, max.diemSuDung());
        assertEquals(new BigDecimal("455"), max.phaiThu());
        assertEquals(new BigDecimal("5000"), s.tinhTienThua(new BigDecimal("35000"), "40.000"));
        assertEquals(new BigDecimal("-5000"), s.tinhTienThua(new BigDecimal("35000"), "30000"));
        try {
            s.tinhTienThua(new BigDecimal("35000"), "abc");
            throw new AssertionError("Phải chặn số tiền không hợp lệ");
        } catch (IllegalArgumentException expected) { /* pass */ }
        System.out.println("PASS: membership, points and cash change calculation");
    }
}
