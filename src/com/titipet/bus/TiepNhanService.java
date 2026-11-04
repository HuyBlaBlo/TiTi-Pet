package com.titipet.bus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;

/** Kiểm tra yêu cầu tiếp nhận; dữ liệu chỉ được ghi khi POS thanh toán thành công. */
public class TiepNhanService {

    public TiepNhanSpaContext xacNhanDuLieu(TiepNhanSpaContext context) {
        if (context == null) throw new IllegalArgumentException("Chưa có thông tin tiếp nhận.");
        if (rong(context.maKH()) || rong(context.maThuCung()))
            throw new IllegalArgumentException("Cần chọn khách hàng và thú cưng.");
        if (rong(context.maNVThoSpa()))
            throw new IllegalArgumentException("Cần phân công thợ Spa trước khi lập hóa đơn.");
        if (context.canNangThucTe() == null || context.canNangThucTe().signum() <= 0)
            throw new IllegalArgumentException("Cân nặng thực tế phải lớn hơn 0.");
        if (rong(context.tinhTrangLong()))
            throw new IllegalArgumentException("Cần ghi nhận tình trạng lông/da.");
        if (context.thoiGianHen() == null || context.thoiGianKetThuc() == null ||
                !context.thoiGianKetThuc().isAfter(context.thoiGianHen()))
            throw new IllegalArgumentException("Giờ hẹn / giờ kết thúc không hợp lệ.");
        if (context.dichVu().isEmpty())
            throw new IllegalArgumentException("Cần chọn ít nhất một dịch vụ Spa.");
        var daCo = new HashSet<String>();
        for (TiepNhanSpaContext.DichVuDaChon dv : context.dichVu()) {
            if (dv == null || rong(dv.maDichVu()) || !daCo.add(dv.maDichVu()) ||
                    dv.donGiaChot() == null || dv.donGiaChot().signum() < 0)
                throw new IllegalArgumentException("Dịch vụ hoặc giá xác nhận không hợp lệ / bị trùng.");
        }
        return context;
    }

    private static boolean rong(String text) { return text == null || text.isBlank(); }
}
