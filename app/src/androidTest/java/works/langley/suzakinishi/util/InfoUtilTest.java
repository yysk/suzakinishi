package works.langley.suzakinishi.util;

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

@SmallTest
public class InfoUtilTest {

    @Test
    public void testGetInfo() throws Exception {
        assertNotNull(InfoUtil.getInfo());
    }
}