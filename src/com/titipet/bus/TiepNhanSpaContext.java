package com.titipet.bus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Thông tin tiếp nhận chuyển sang POS; chưa ghi xuống CSDL trước thanh toán. */
public record TiepNhanSpaContext(
        String maKH, String maThuCung, String maLichHen, String maNVThoSpa,
        BigDecimal canNangThucTe, String tinhTrangLong, String ghiChu,
        LocalDateTime thoiGianHen, LocalDateTime thoiGianKetThuc,
        List<DichVuDaChon> dichVu) {

    public record DichVuDaChon(String maDichVu, BigDecimal donGiaChot) { }

    public TiepNhanSpaContext {
        dichVu = dichVu == null ? List.of() : List.copyOf(dichVu);
    }
}
