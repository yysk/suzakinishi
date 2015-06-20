package works.langley.suzakinishi.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import works.langley.suzakinishi.event.BusProvider;
import works.langley.suzakinishi.event.VolumeChangeEvent;

public class VolumeChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
            BusProvider.getInstance().post(new VolumeChangeEvent());
        }
    }
}