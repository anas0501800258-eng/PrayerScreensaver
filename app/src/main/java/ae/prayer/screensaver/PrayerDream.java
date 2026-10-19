package ae.prayer.screensaver;

import android.os.Handler;
import android.os.Looper;
import android.service.dreams.DreamService;

/** The Android screensaver (DreamService). */
public class PrayerDream extends DreamService {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private PrayerScreen screen;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (screen != null) screen.update();
            handler.postDelayed(this, 1000 - (System.currentTimeMillis() % 1000));
        }
    };

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        setScreenBright(true);
        screen = new PrayerScreen(this);
        setContentView(screen.root);
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
        handler.removeCallbacks(tick);
        handler.post(tick);
    }

    @Override
    public void onDreamingStopped() {
        handler.removeCallbacks(tick);
        super.onDreamingStopped();
    }

    @Override
    public void onDetachedFromWindow() {
        handler.removeCallbacks(tick);
        super.onDetachedFromWindow();
    }
}
