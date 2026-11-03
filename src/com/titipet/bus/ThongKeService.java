package com.titipet.bus;

import com.titipet.dao.ThongKeDAO;
import com.titipet.dao.ThongKeDAO.*;
import com.titipet.entity.enums.VaiTro;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Nghiệp vụ báo cáo: giới hạn quyền Quản lý, kiểm tra khoảng ngày và tạo gợi ý
 * theo NGƯỠNG có thể kiểm chứng; không dùng AI để tự suy đoán lợi nhuận.
 */
public final class ThongKeService {
    public record DeXuat(String mucDo, String tieuDe, String hanhDong, String canCu) {}
    public record BaoCao(LocalDate tuNgay, LocalDate denNgay,
            DuLieu duLieu, List<DeXuat> deXuat) {}

    private final AuthService authService;
    private final ThongKeDAO dao;

    public ThongKeService(AuthService authService) {
        this(authService, new ThongKeDAO());
    }

    ThongKeService(AuthService authService, ThongKeDAO dao) {
        this.authService = Objects.requireNonNull(authService, "Cần phiên đăng nhập.");
        this.dao = Objects.requireNonNull(dao);
    }

    public BaoCao layBaoCao(LocalDate tuNgay, LocalDate denNgay) {
        if (!authService.coVaiTro(VaiTro.QUAN_LY)) {
            throw new SecurityException("Chỉ Quản lý được xem thống kê doanh thu toàn cửa hàng.");
        }
        if (tuNgay == null || denNgay == null || tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Khoảng ngày thống kê không hợp lệ.");
        }
        if (ChronoUnit.DAYS.between(tuNgay, denNgay) > 365) {
            throw new IllegalArgumentException("Chỉ xem tối đa 366 ngày mỗi lần.");
        }
        try {
            DuLieu duLieu = dao.docBaoCao(tuNgay, denNgay);
            return new BaoCao(tuNgay, denNgay, duLieu, deXuat(duLieu));
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được dữ liệu thống kê SQL Server: "
                    + ex.getMessage(), ex);
        }
    }

    /** Các khuyến nghị chỉ gắn với số liệu có nguồn; không khẳng định nguyên nhân doanh thu. */
    public static List<DeXuat> deXuat(DuLieu d) {
        List<DeXuat> list = new ArrayList<>();
        if (d.tongQuan().soHoaDon() == 0) {
            list.add(new DeXuat("THÔNG TIN", "Chưa đủ giao dịch để đánh giá",
                    "Kiểm tra kết nối và thử mở rộng khoảng ngày; chưa nên kết luận nhu cầu đang giảm.",
                    "Trong khoảng đã chọn có 0 hóa đơn DA_THANH_TOAN."));
        }
        if (!d.canhBaoTon().isEmpty()) {
            CanhBaoTon dau = d.canhBaoTon().get(0);
            list.add(new DeXuat("ƯU TIÊN", "Rà soát hàng dưới ngưỡng tồn",
                    "Kiểm tra tồn kho thực tế, lập kế hoạch bổ sung các sản phẩm bán được; "
                            + "ưu tiên: " + dau.ten() + " (" + dau.ma() + ").",
                    d.canhBaoTon().size() + " sản phẩm đang kinh doanh có tồn kho không vượt ngưỡng cảnh báo."));
        }
        if (!d.hanThucAn().isEmpty()) {
            long hetHan = d.hanThucAn().stream()
                    .filter(x -> x.han().isBefore(LocalDate.now())).count();
            list.add(new DeXuat("ƯU TIÊN", "Kiểm tra hạn sử dụng thức ăn",
                    "Kiểm tra hạn in trên bao bì, tách hàng đã hết hạn; xử lý hàng gần hạn "
                            + "theo quy định cửa hàng, không tự ý bán hàng hết hạn.",
                    d.hanThucAn().size() + " mặt hàng còn tồn đã/sắp hết hạn trong 30 ngày, "
                            + hetHan + " mặt hàng có ngày hết hạn trước hôm nay."));
        }
        long caLech = d.phieuCa().stream()
                .filter(x -> x.chenhLech().compareTo(BigDecimal.ZERO) != 0).count();
        if (caLech > 0) {
            list.add(new DeXuat("ƯU TIÊN", "Đối chiếu các ca có chênh lệch",
                    "Kiểm đếm tiền tại quầy, đối chiếu giao dịch tiền mặt và phiếu trả hàng "
                            + "trước khi xác nhận sổ ca.",
                    caLech + " phiếu trong tối đa 100 phiếu gần nhất có thực đếm khác dự kiến."));
        }
        if (d.tongQuan().hoanChuaPhanLoai() > 0) {
            list.add(new DeXuat("ƯU TIÊN", "Bổ sung phương thức hoàn tiền",
                    "Đối chiếu phiếu đổi/trả và xác nhận hoàn tiền thuộc tiền mặt, chuyển khoản "
                            + "hay qua sàn để báo cáo tiền thu không bị lệch.",
                    d.tongQuan().hoanChuaPhanLoai() + " giao dịch trả hàng không có phương thức hoàn tiền."));
        }
        if (d.vanHanh().spaChoLam() > 0) {
            list.add(new DeXuat("THEO DÕI", "Điều phối dịch vụ Spa đang chờ",
                    "Xem hàng đợi, xác nhận lịch với khách và phân công thợ Spa theo năng lực.",
                    d.vanHanh().spaChoLam() + " dòng Spa đã thanh toán đang CHO_LAM (tại thời điểm xem)."));
        }
        if (d.vanHanh().tongLichHen() >= 5
                && d.vanHanh().lichHuy() * 100 >= d.vanHanh().tongLichHen() * 20) {
            list.add(new DeXuat("THEO DÕI", "Kiểm tra tình trạng hủy lịch",
                    "Rà soát các lịch hủy và chủ động nhắc hẹn; không quy kết nguyên nhân khi chưa khảo sát.",
                    d.vanHanh().lichHuy() + "/" + d.vanHanh().tongLichHen()
                            + " lịch có ngày hẹn trong kỳ đang mang trạng thái HUY."));
        }
        if (d.tongQuan().thuHoaDon().signum() > 0
                && d.tongQuan().tienHoan().multiply(BigDecimal.TEN)
                        .compareTo(d.tongQuan().thuHoaDon()) >= 0) {
            list.add(new DeXuat("THEO DÕI", "Rà soát tiền hoàn trả hàng",
                    "Kiểm tra lý do đổi/trả, sản phẩm bị trả và chất lượng tư vấn trước khi đề xuất biện pháp.",
                    "Tiền hoàn trong kỳ >= 10% tiền thu hóa đơn trong kỳ; "
                            + "hai tập giao dịch có thể khác ngày bán gốc."));
        }
        if (!d.sanPhamBanChay().isEmpty()) {
            SanPhamBanChay top = d.sanPhamBanChay().get(0);
            list.add(new DeXuat("GỢI Ý", "Theo dõi sản phẩm được mua nhiều",
                    "Kiểm tra tồn kho và nhịp nhập hàng cho " + top.ten()
                            + "; chỉ tăng lượng nhập sau khi đối chiếu sức mua và hàng trả.",
                    "Top theo số lượng bán của hóa đơn đã thanh toán: "
                            + top.soLuong() + " sản phẩm, chưa trừ đổi/trả."));
        }
        if (list.isEmpty()) {
            list.add(new DeXuat("GỢI Ý", "Duy trì kiểm tra vận hành định kỳ",
                    "Theo dõi doanh thu, lượng đặt Spa, hạn sử dụng và đối soát ca mỗi ngày.",
                    "Chưa có điều kiện cảnh báo nào đạt ngưỡng trong các chỉ số hiện có."));
        }
        return List.copyOf(list);
    }
}
