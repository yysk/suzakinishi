package works.langley.suzakinishi.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import works.langley.suzakinishi.service.MusicPlayerService;

/**
 * リモートコントロールからブロードキャストされるマルチメディアイベントを受け取るレシーバ。
 */
public class MusicPlayerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent newIntent = new Intent(context, MusicPlayerService.class);
        if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            // イヤホンコードが外れた時
            newIntent.setAction(MusicPlayerService.ACTION_PAUSE);
            context.startService(newIntent);
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent == null || keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                return;
            }

            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    newIntent.setAction(MusicPlayerService.ACTION_PAUSE);
                    context.startService(newIntent);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    newIntent.setAction(MusicPlayerService.ACTION_PLAY);
                    context.startService(newIntent);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    newIntent.setAction(MusicPlayerService.ACTION_PAUSE);
                    context.startService(newIntent);
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    newIntent.setAction(MusicPlayerService.ACTION_STOP);
                    context.startService(newIntent);
                    break;
            }
        }
    }
}
