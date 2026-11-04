package com.titipet.tests;

import com.titipet.bus.TiepNhanService;
import com.titipet.bus.TiepNhanSpaContext;
import com.titipet.bus.PhanQuyenManHinh;
import com.titipet.entity.enums.VaiTro;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Kiểm tra validate và phân quyền mà không cần SQL Server. */
public class TiepNhanSpaFlowSmokeTest {
    public static void main(String[] args) {
        TiepNhanService service = new TiepNhanService();
        LocalDateTime start = LocalDateTime.now();
        TiepNhanSpaContext good = new TiepNhanSpaContext("KH001", "TC001", "LH001", "NV004",
            new BigDecimal("3.5"), "Bình thường", "Chăm sóc nhẹ", start, start.plusMinutes(90),
            List.of(new TiepNhanSpaContext.DichVuDaChon("DV001", new BigDecimal("120000"))));
        service.xacNhanDuLieu(good);
        assertTrue(PhanQuyenManHinh.duocTruyCap(VaiTro.THO_SPA, "dieu-phoi-spa"));
        assertTrue(!PhanQuyenManHinh.duocTruyCap(VaiTro.THO_SPA, "ban-hang"));
        assertTrue(PhanQuyenManHinh.duocTruyCap(VaiTro.THU_NGAN, "tiep-nhan-spa"));
        expectFailure(() -> service.xacNhanDuLieu(new TiepNhanSpaContext("KH001", "TC001",
            null, "NV004", BigDecimal.ZERO, "Bình thường", "", start, start.plusMinutes(10),good.dichVu())));
        expectFailure(() -> service.xacNhanDuLieu(new TiepNhanSpaContext("KH001", "TC001",
            null, "NV004", new BigDecimal("1.2"), "Bình thường", "", start, start,
            good.dichVu())));
        expectFailure(() -> service.xacNhanDuLieu(new TiepNhanSpaContext("KH001", "TC001",
            null, "NV004", new BigDecimal("1.2"), "Bình thường", "", start,
            start.plusMinutes(5), List.of(good.dichVu().get(0), good.dichVu().get(0)))));
        System.out.println("PASS: nhan Spa - gia - thoi gian - phan quyen - trung dich vu");
    }
    private static void expectFailure(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException e) { return; }
        throw new AssertionError("Thiếu validation nghiệp vụ");
    }
    private static void assertTrue(boolean value) { if (!value) throw new AssertionError("Sai phân quyền"); }
}
