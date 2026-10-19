package ae.prayer.screensaver;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;

/** Shows the screensaver full screen so it can be tested. Press Back to exit. */
public class PreviewActivity extends Activity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private PrayerScreen screen;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            screen.update();
            handler.postDelayed(this, 1000 - (System.currentTimeMillis() % 1000));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        screen = new PrayerScreen(this);
        setContentView(screen.root);
        screen.root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(tick);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(tick);
        super.onPause();
    }
}
