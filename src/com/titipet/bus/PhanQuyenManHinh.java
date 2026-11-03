package com.titipet.bus;

import com.titipet.entity.NhanVien;
import com.titipet.entity.enums.VaiTro;
import java.util.Objects;

/** kiểm tra quyền mở màn hình*/
public final class PhanQuyenManHinh {
    private final AuthService authService;

    public PhanQuyenManHinh(AuthService authService) {
        this.authService = Objects.requireNonNull(authService);
    }

    /**
     * truyền vào là 1 màn hinh được phân quyền
     * @param maManHinh
     * @return
     */
    public boolean duocTruyCap(String maManHinh) {
        VaiTro vaiTro = authService
                .getNhanVienDangNhap()
                .map(nhanVien ->nhanVien.getVaiTro())
                .orElse(null);
        return duocTruyCap(vaiTro, maManHinh);
    }

    public static boolean duocTruyCap(VaiTro vaiTro, String maManHinh) {
        if (vaiTro == null || maManHinh == null)
            return false;

        return switch (maManHinh) {
                    case "trang-chu" -> true;
                    case "chot-ca", "lich-hen", "tiep-nhan-spa", "ban-hang",
                         "hoa-don", "khach-hang", "thu-cung", "kho",
                         "dich-vu-san-pham", "khuyen-mai" ->
                            vaiTro == VaiTro.QUAN_LY || vaiTro == VaiTro.THU_NGAN;
                    // LichThoSpaPanel hiện đọc lịch mọi thợ. Chưa mở cho thợ khi chưa lọc dữ liệu.
                    case "dieu-phoi-spa", "thong-ke" -> vaiTro == VaiTro.QUAN_LY;
                    // Đổi trả chưa chốt nghiệp vụ; mã mới cũng phải khai báo quyền trước khi mở.
                    default -> false;
        };
    }
}
