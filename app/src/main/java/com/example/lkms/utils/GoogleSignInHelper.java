package com.example.lkms.utils; // Giả sử bạn có package 'auth'

import android.content.Context;

import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes; // <-- IMPORT QUAN TRỌNG

public class GoogleSignInHelper {

    /**
     * Yêu cầu quyền truy cập Google Drive của người dùng.
     * Đây là "scope" (phạm vi) mà chúng ta cần.
     * DriveScopes.DRIVE_FILE: Cho phép app tạo và quản lý các file MÀ NÓ TẠO RA.
     * Đây là quyền an toàn nhất, app sẽ không thấy các file khác của người dùng.
     */
    public static final Scope DRIVE_SCOPE = new Scope(DriveScopes.DRIVE_FILE);

    /**
     * Lấy GoogleSignInClient đã được cấu hình với quyền truy cập Drive.
     *
     * HÃY GỌI HÀM NÀY trong LoginActivity của bạn, thay vì hàm cũ.
     */
    public static GoogleSignInClient getClient(Context context) {
        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("YOUR_SERVER_CLIENT_ID.apps.googleusercontent.com") // Thay bằng Web Client ID của bạn

                // ===== YÊU CẦU THÊM QUYỀN TRUY CẬP DRIVE =====
                .requestScopes(DRIVE_SCOPE)

                .build();

        return GoogleSignIn.getClient(context, gso);
    }

    /**
     * Kiểm tra xem người dùng đã cấp quyền Drive chưa.
     */
    public static boolean hasDriveScope(GoogleSignInAccount account) {
        if (account == null) return false;
        return account.getGrantedScopes().contains(DRIVE_SCOPE);
    }

    /**
     * Nếu người dùng đã đăng nhập nhưng chưa cấp quyền,
     * bạn có thể gọi hàm này để yêu cầu thêm quyền.
     */
    public static void requestDriveScope(Fragment fragment) {
        if (fragment.getContext() == null) return;
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(fragment.getContext());
        if (account != null && !hasDriveScope(account)) {
            // Yêu cầu thêm quyền
            GoogleSignIn.requestPermissions(
                    fragment,
                    1001, // Mã request code (bạn tự định nghĩa)
                    account,
                    DRIVE_SCOPE);
        }
    }
}