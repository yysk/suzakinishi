package works.langley.suzakinishi.util;

import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import timber.log.Timber;
import works.langley.suzakinishi.Constant;
import works.langley.suzakinishi.model.Info;

public final class InfoUtil {

    private InfoUtil() {
    }

    private static final String MAIN_URL = Constant.URL_RADIO;
    private static final String TAG_A = "a";
    private static final String TAG_REF = "ref";
    private static final String TAG_TITLE = "title";
    private static final String TAG_AUTHOR = "author";
    private static final String ATTR_HREF = "href";
    private static final String EXTENSION_WAX = ".wax";
    private static final String EXTENSION_WMA = ".wma";

    /**
     * HPから配信ファイルの情報を取得
     *
     * @return
     */
    public static Info getInfo() {
        try {
            Document doc = Jsoup.connect(MAIN_URL).timeout(10000).get();
            List<Element> elements = doc.getElementsByTag(TAG_A);

            for (Element element : elements) {
                String href = element.attr(ATTR_HREF);
                if (!TextUtils.isEmpty(href) && href.endsWith(EXTENSION_WAX)) {
                    Document document = Jsoup.parse(new URL(href).openStream(), "Shift_JIS", href);
                    Timber.d("url : %s", href);
                    Timber.d(document.html());

                    return new Info(getInfoTitle(document), getInfoAuthor(document), getElementAttribute(document));
                }
            }
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
        }
        return null;
    }

    private static String getInfoTitle(Document document) {
        return getElementText(document, TAG_TITLE);
    }

    private static String getInfoAuthor(Document document) {
        return getElementText(document, TAG_AUTHOR);
    }

    private static String getElementText(Document document, String tag) {
        List<Element> elements = document.getElementsByTag(tag);
        if (elements != null && !elements.isEmpty()) {
            return elements.get(0).ownText();
        }
        return null;
    }

    private static String getElementAttribute(Document document) {
        List<Element> elements = document.getElementsByTag(TAG_REF);
        if (elements != null && !elements.isEmpty()) {
            return elements.get(0).attr(ATTR_HREF);
        }
        return null;
    }
}