package com.example.lkms.utils; // Tạo package 'utils'

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.InputStream;
import java.util.Collections;

public class DriveServiceHelper {

    private static final String TAG = "DriveServiceHelper";
    private final Drive driveService;

    /**
     * Interface callback để nhận kết quả (vì upload là bất đồng bộ)
     */
    public interface UploadCallback {
        void onSuccess(File file); // Trả về đối tượng File (chứa ID và webViewLink)
        void onError(Exception e);
    }

    /**
     * Khởi tạo Drive Service
     * @param context Context
     * @param account Tài khoản Google (đã đăng nhập và có scope)
     */
    public DriveServiceHelper(Context context, GoogleSignInAccount account) {
        GoogleAccountCredential credential = GoogleAccountCredential
                .usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());

        this.driveService = new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName("LKMS") // Tên app của bạn
                .build();
    }

    /**
     * Tải file lên Google Drive.
     * File sẽ được lưu vào một thư mục riêng của ứng dụng (an toàn).
     */
    public void uploadFile(Context context, Uri fileUri, String mimeType, UploadCallback callback) {
        new Thread(() -> {
            try {
                // Lấy tên file từ Uri
                String fileName = getFileNameFromUri(context, fileUri);

                File fileMetadata = new File();
                fileMetadata.setName(fileName);

                // Quan trọng: Set "appDataFolder" để lưu file vào
                // thư mục riêng của app, người dùng không thể thấy/xóa trực tiếp.
                // Nếu muốn lưu vào Drive chính của người dùng, dùng:
                // fileMetadata.setParents(Collections.singletonList("root"));
                fileMetadata.setParents(Collections.singletonList("appDataFolder"));


                ContentResolver resolver = context.getContentResolver();
                InputStream inputStream = resolver.openInputStream(fileUri);
                InputStreamContent mediaContent = new InputStreamContent(mimeType, inputStream);

                Log.d(TAG, "Đang tải file lên: " + fileName);

                // Thực hiện tải lên
                File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id, name, webViewLink, mimeType") // Các trường muốn nhận về
                        .execute();

                Log.d(TAG, "Tải file thành công! File ID: " + uploadedFile.getId());

                // Trả kết quả về Main Thread
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                        callback.onSuccess(uploadedFile)
                );

            } catch (Exception e) {
                Log.e(TAG, "Upload lỗi!", e);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                        callback.onError(e)
                );
            }
        }).start();
    }

    // Hàm trợ giúp (helper) để lấy tên file từ Uri
    private String getFileNameFromUri(Context context, Uri uri) {
        String fileName = "unknown_file";
        try (android.database.Cursor cursor = context.getContentResolver()
                .query(uri, null, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lấy tên file", e);
        }
        return fileName;
    }
}