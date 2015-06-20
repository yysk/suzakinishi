package works.langley.suzakinishi.event;

import android.content.Intent;

public class StateChangeEvent {

    private Intent mIntent;

    public StateChangeEvent(Intent intent) {
        mIntent = intent;
    }

    public Intent getIntent() {
        return mIntent;
    }
}
