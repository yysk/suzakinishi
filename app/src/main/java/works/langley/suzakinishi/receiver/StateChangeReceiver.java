package works.langley.suzakinishi.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import works.langley.suzakinishi.event.BusProvider;
import works.langley.suzakinishi.event.StateChangeEvent;

public class StateChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        BusProvider.getInstance().post(new StateChangeEvent(intent));
    }
}