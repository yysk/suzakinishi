package works.langley.suzakinishi.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class AssetUtil {

    private AssetUtil() {
    }

    public static String readTextFromAssets(Context context, String target) throws IOException {
        AssetManager assetManager = context.getApplicationContext().getResources().getAssets();

        StringBuilder builder = new StringBuilder();

        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = assetManager.open(target);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String str;
            while ((str = bufferedReader.readLine()) != null) {
                builder.append(str).append("\n");
            }
        } finally {
            close(bufferedReader);
            close(inputStream);
        }
        return builder.toString();
    }

    public static void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignore) {
        }
    }
}