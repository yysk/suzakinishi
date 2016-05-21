package works.langley.suzakinishi.util;

import android.content.Intent;
import android.net.Uri;

import works.langley.suzakinishi.Constant;

public final class IntentUtil {

    public static Intent createWebIntent() {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.URL_HOMEPAGE));
    }

    public static Intent createMailIntent() {
        return new Intent(Intent.ACTION_SENDTO, Uri.parse(Constant.URL_MAIL));
    }
}