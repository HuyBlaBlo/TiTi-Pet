package com.titipet.tests;

import com.titipet.bus.ChuyenLichHenTiepNhanService;
import com.titipet.entity.LichHen;
import com.titipet.entity.KhachHang;
import com.titipet.entity.ThuCung;
import com.titipet.entity.NhanVien;
import com.titipet.entity.DichVuSpa;
import com.titipet.entity.enums.TrangThaiLichHen;
import java.time.LocalDate;

/** Smoke test chính sách chuyển màn hình, không cần SQL Server. */
public final class ChuyenLichHenTiepNhanSmokeTest {
    public static void main(String[] args) {
        LocalDate homNay = LocalDate.of(2026, 11, 4);
        LichHen lich = new LichHen();
        lich.setMaLichHen("LH001");
        lich.setThoiGianHen(homNay.atTime(14, 0));
        lich.setTrangThai(TrangThaiLichHen.CHO_TIEP_NHAN);
        KhachHang kh = new KhachHang(); kh.setMaKH("KH001");
        ThuCung tc = new ThuCung(); tc.setMaThuCung("TC001"); tc.setChuNuoi(kh);
        DichVuSpa dv = new DichVuSpa(); dv.setMaDichVu("DV001");
        NhanVien tho = new NhanVien(); tho.setMaNV("NV004");
        lich.setKhachHang(kh); lich.setThuCung(tc); lich.setDichVu(dv); lich.setThoSpa(tho);
        ChuyenLichHenTiepNhanService.kiemTraLichChoTiepNhan(lich, homNay);

        lich.setThoiGianHen(homNay.plusDays(1).atStartOfDay());
        phaiTuChoi(() -> ChuyenLichHenTiepNhanService.kiemTraLichChoTiepNhan(lich, homNay));
        lich.setThoiGianHen(homNay.atTime(14, 0));
        lich.setTrangThai(TrangThaiLichHen.DA_TIEP_NHAN);
        phaiTuChoi(() -> ChuyenLichHenTiepNhanService.kiemTraLichChoTiepNhan(lich, homNay));
        lich.setTrangThai(TrangThaiLichHen.DA_HUY);
        phaiTuChoi(() -> ChuyenLichHenTiepNhanService.kiemTraLichChoTiepNhan(lich, homNay));
        lich.setTrangThai(TrangThaiLichHen.CHO_TIEP_NHAN);
        lich.setThoSpa(null);
        phaiTuChoi(() -> ChuyenLichHenTiepNhanService.kiemTraLichChoTiepNhan(lich, homNay));
        System.out.println("PASS: chuyen lich hen sang tiep nhan - trang thai - ngay hen - thong tin");
    }

    private static void phaiTuChoi(Runnable run) {
        try { run.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Lich khong hop le nhung van cho tiep nhan");
    }
}
