package works.langley.suzakinishi.util;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import works.langley.suzakinishi.R;

public final class ToastUtil {

    private ToastUtil() {
    }

    public static void showNetworkError(Context context) {
        show(context, R.string.error_no_network);
    }

    public static void showOtherError(Context context) {
        show(context, R.string.error_other);
    }

    public static void show(Context context, int resId) {
        if (context == null) {
            return;
        }
        show(context, context.getString(resId));
    }

    public static void show(Context context, String message) {
        if (context == null || TextUtils.isEmpty(message)) {
            return;
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}