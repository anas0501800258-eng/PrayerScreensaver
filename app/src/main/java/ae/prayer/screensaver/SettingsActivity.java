package ae.prayer.screensaver;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

/** Settings: choose city, fine-tune each prayer by minutes, preview. */
public class SettingsActivity extends Activity {

    private LinearLayout body;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0F1522"));
        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        body.setPadding(dp(48), dp(32), dp(48), dp(48));
        scroll.addView(body);

        title("شاشة توقف مواقيت الصلاة", 30);

        Button preview = new Button(this);
        preview.setText("عرض الشاشة الآن");
        preview.setOnClickListener(v -> startActivity(new Intent(this, PreviewActivity.class)));
        body.addView(preview, wrap());

        title("المدينة", 22);
        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.VERTICAL);
        int selected = Settings.city(this);
        for (int i = 0; i < Settings.CITY_NAMES.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setId(1000 + i);
            rb.setText(Settings.CITY_NAMES[i]);
            rb.setTextColor(Color.WHITE);
            rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            group.addView(rb);
            if (i == selected) rb.setChecked(true);
        }
        group.setOnCheckedChangeListener((g, id) -> Settings.setCity(this, id - 1000));
        body.addView(group);

        title("تعديل الدقائق (لمطابقة تقويم الأوقاف)", 22);
        int[] off = Settings.offsets(this);
        for (int i = 0; i < 6; i++) addOffsetRow(i, off[i]);

        setContentView(scroll);
        preview.requestFocus();
    }

    private void addOffsetRow(int prayer, int initial) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        TextView name = label(PrayerScreen.PRAYER_NAMES[prayer]);
        row.addView(name, new LinearLayout.LayoutParams(dp(140), ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView value = label(fmt(initial));
        value.setGravity(Gravity.CENTER);
        final int[] cur = {initial};

        Button minus = new Button(this);
        minus.setText("−");
        minus.setOnClickListener(v -> {
            cur[0] = Math.max(-30, cur[0] - 1);
            Settings.setOffset(this, prayer, cur[0]);
            value.setText(fmt(cur[0]));
        });
        Button plus = new Button(this);
        plus.setText("+");
        plus.setOnClickListener(v -> {
            cur[0] = Math.min(30, cur[0] + 1);
            Settings.setOffset(this, prayer, cur[0]);
            value.setText(fmt(cur[0]));
        });

        row.addView(plus, wrap());
        row.addView(value, new LinearLayout.LayoutParams(dp(110), ViewGroup.LayoutParams.WRAP_CONTENT));
        row.addView(minus, wrap());
        body.addView(row);
    }

    private static String fmt(int m) {
        return (m > 0 ? "+" : "") + m + " د";
    }

    private void title(String s, float sp) {
        TextView t = label(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(Color.parseColor("#D9B65D"));
        LinearLayout.LayoutParams lp = wrap();
        lp.topMargin = dp(24);
        lp.bottomMargin = dp(8);
        body.addView(t, lp);
    }

    private TextView label(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(Color.WHITE);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        t.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG_RTL);
        return t;
    }

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
