package ae.prayer.screensaver;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/** The full-screen view shared by the screensaver and the preview screen. */
public class PrayerScreen {

    static final String[] PRAYER_NAMES = {"الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء"};
    private static final String[] DAYS = {"الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"};
    private static final String[] MONTHS = {"يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
            "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"};
    private static final String[] HIJRI_MONTHS = {"محرم", "صفر", "ربيع الأول", "ربيع الآخر",
            "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"};

    private static final int WHITE = Color.parseColor("#F5F1E8");
    private static final int MUTED = Color.parseColor("#9AA4B2");
    private static final int GOLD = Color.parseColor("#D9B65D");

    private final Context ctx;
    public final FrameLayout root;
    private final LinearLayout content;
    private final TextView clock, gregDate, hijriDate, next, city;
    private final LinearLayout[] cells = new LinearLayout[6];
    private final TextView[] names = new TextView[6];
    private final TextView[] times = new TextView[6];

    public PrayerScreen(Context ctx) {
        this.ctx = ctx;

        root = new FrameLayout(ctx);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#0A0F1A"), Color.parseColor("#101827"), Color.parseColor("#0A0F1A")});
        root.setBackground(bg);

        content = new LinearLayout(ctx);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        root.addView(content, clp);

        clock = text(92, WHITE, true);
        clock.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        content.addView(clock);

        gregDate = text(24, WHITE, false);
        content.addView(gregDate);

        hijriDate = text(20, MUTED, false);
        content.addView(hijriDate);

        next = text(28, GOLD, true);
        LinearLayout.LayoutParams nlp = wrap();
        nlp.topMargin = dp(22);
        nlp.bottomMargin = dp(22);
        content.addView(next, nlp);

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.leftMargin = dp(56);
        rlp.rightMargin = dp(56);
        content.addView(row, rlp);

        for (int i = 0; i < 6; i++) {
            LinearLayout cell = new LinearLayout(ctx);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(0, dp(12), 0, dp(12));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.leftMargin = dp(5);
            lp.rightMargin = dp(5);
            names[i] = text(20, MUTED, false);
            names[i].setText(PRAYER_NAMES[i]);
            times[i] = text(32, WHITE, false);
            cell.addView(names[i]);
            cell.addView(times[i]);
            cells[i] = cell;
            row.addView(cell, lp);
        }

        city = text(16, MUTED, false);
        LinearLayout.LayoutParams cylp = wrap();
        cylp.topMargin = dp(20);
        content.addView(city, cylp);
    }

    /** Refresh everything; call once per second. */
    public void update() {
        int cityIdx = Settings.city(ctx);
        double[] ll = Settings.CITY_COORDS[cityIdx];
        int[] off = Settings.offsets(ctx);

        TimeZone tz = TimeZone.getTimeZone(Settings.TZ_ID);
        Calendar now = Calendar.getInstance(tz);
        int y = now.get(Calendar.YEAR), m = now.get(Calendar.MONTH) + 1, d = now.get(Calendar.DAY_OF_MONTH);
        int h = now.get(Calendar.HOUR_OF_DAY), min = now.get(Calendar.MINUTE), sec = now.get(Calendar.SECOND);
        int nowSec = h * 3600 + min * 60 + sec;

        int[] today = PrayerCalc.compute(y, m, d, ll[0], ll[1], Settings.TZ_OFFSET, off);

        // Next prayer (sunrise is shown but not counted as a prayer)
        int nextIdx = -1;
        int targetSec = 0;
        for (int i = 0; i < 6; i++) {
            if (i == PrayerCalc.SUNRISE) continue;
            if (today[i] * 60 > nowSec) { nextIdx = i; targetSec = today[i] * 60; break; }
        }
        if (nextIdx == -1) {
            Calendar tmr = (Calendar) now.clone();
            tmr.add(Calendar.DAY_OF_MONTH, 1);
            int[] t2 = PrayerCalc.compute(tmr.get(Calendar.YEAR), tmr.get(Calendar.MONTH) + 1,
                    tmr.get(Calendar.DAY_OF_MONTH), ll[0], ll[1], Settings.TZ_OFFSET, off);
            nextIdx = PrayerCalc.FAJR;
            targetSec = 24 * 3600 + t2[PrayerCalc.FAJR] * 60;
        }
        int left = targetSec - nowSec;

        // Clock
        String hm = String.format(Locale.US, "%d:%02d", h % 12 == 0 ? 12 : h % 12, min);
        String suffix = h < 12 ? "ص" : "م";
        SpannableString clk = new SpannableString(hm + " " + suffix);
        clk.setSpan(new RelativeSizeSpan(0.35f), hm.length(), clk.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        clock.setText(clk);

        gregDate.setText(DAYS[now.get(Calendar.DAY_OF_WEEK) - 1] + "، " + d + " " + MONTHS[m - 1] + " " + y);
        hijriDate.setText(hijri(now));

        next.setText(PRAYER_NAMES[nextIdx] + " بعد " + String.format(Locale.US, "%d:%02d:%02d",
                left / 3600, (left % 3600) / 60, left % 60));

        for (int i = 0; i < 6; i++) {
            times[i].setText(fmt12(today[i]));
            styleCell(i, i == nextIdx);
        }

        city.setText(Settings.CITY_NAMES[cityIdx] + " · توقيت الإمارات");

        // Slow drift to protect the screen from burn-in
        double t = (h * 60 + min) / 3.0;
        content.setTranslationX((float) (dp(36) * Math.sin(t * 0.9)));
        content.setTranslationY((float) (dp(22) * Math.cos(t * 0.6)));
    }

    private void styleCell(int i, boolean highlight) {
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(16));
        if (highlight) {
            g.setColor(Color.argb(40, 217, 182, 93));
            g.setStroke(dp(2), GOLD);
            names[i].setTextColor(GOLD);
            times[i].setTextColor(GOLD);
        } else {
            g.setColor(Color.argb(18, 255, 255, 255));
            names[i].setTextColor(MUTED);
            times[i].setTextColor(WHITE);
        }
        cells[i].setBackground(g);
    }

    private static String fmt12(int minutes) {
        minutes = ((minutes % 1440) + 1440) % 1440;
        int h = minutes / 60, m = minutes % 60;
        return String.format(Locale.US, "%d:%02d", h % 12 == 0 ? 12 : h % 12, m);
    }

    private static String hijri(Calendar now) {
        try {
            android.icu.util.IslamicCalendar ic = new android.icu.util.IslamicCalendar(
                    android.icu.util.TimeZone.getTimeZone(Settings.TZ_ID));
            ic.setCalculationType(android.icu.util.IslamicCalendar.CalculationType.ISLAMIC_UMALQURA);
            ic.setTimeInMillis(now.getTimeInMillis());
            int hd = ic.get(android.icu.util.Calendar.DAY_OF_MONTH);
            int hm = ic.get(android.icu.util.Calendar.MONTH);
            int hy = ic.get(android.icu.util.Calendar.YEAR);
            return hd + " " + HIJRI_MONTHS[hm] + " " + hy + " هـ";
        } catch (Throwable e) {
            return "";
        }
    }

    private TextView text(float sp, int color, boolean bold) {
        TextView tv = new TextView(ctx);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        tv.setTextColor(color);
        tv.setGravity(Gravity.CENTER);
        tv.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG_RTL);
        if (bold) tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setIncludeFontPadding(false);
        tv.setLayoutParams(wrap());
        return tv;
    }

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int v) {
        return Math.round(v * ctx.getResources().getDisplayMetrics().density);
    }
}
