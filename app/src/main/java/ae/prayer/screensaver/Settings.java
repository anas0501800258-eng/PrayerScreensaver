package ae.prayer.screensaver;

import android.content.Context;
import android.content.SharedPreferences;

/** Cities and saved user settings. */
public final class Settings {

    public static final String[] CITY_NAMES = {
            "أبوظبي", "دبي", "الشارقة", "عجمان", "أم القيوين", "رأس الخيمة",
            "الفجيرة", "العين", "خورفكان", "كلباء", "الذيد"
    };
    public static final double[][] CITY_COORDS = {
            {24.4539, 54.3773}, {25.2048, 55.2708}, {25.3463, 55.4209}, {25.4052, 55.5136},
            {25.5647, 55.5552}, {25.8007, 55.9762}, {25.1288, 56.3265}, {24.2075, 55.7447},
            {25.3392, 56.3555}, {25.0730, 56.3550}, {25.2885, 55.8810}
    };
    public static final int DEFAULT_CITY = 2; // الشارقة
    public static final double TZ_OFFSET = 4.0; // توقيت الإمارات
    public static final String TZ_ID = "Asia/Dubai";

    private static final String PREFS = "prayer_prefs";

    private Settings() {}

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static int city(Context c) {
        int i = prefs(c).getInt("city", DEFAULT_CITY);
        return (i < 0 || i >= CITY_NAMES.length) ? DEFAULT_CITY : i;
    }

    public static void setCity(Context c, int i) {
        prefs(c).edit().putInt("city", i).apply();
    }

    public static int[] offsets(Context c) {
        SharedPreferences p = prefs(c);
        int[] o = new int[6];
        for (int i = 0; i < 6; i++) o[i] = p.getInt("off" + i, 0);
        return o;
    }

    public static void setOffset(Context c, int prayer, int minutes) {
        prefs(c).edit().putInt("off" + prayer, minutes).apply();
    }
}
