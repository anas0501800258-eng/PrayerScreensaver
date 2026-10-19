package ae.prayer.screensaver;

/**
 * Prayer time calculation (PrayTimes.org algorithm) using the UAE / IACAD
 * parameters: Fajr 18.2°, Isha 18.2°, Asr standard (Shafi'i),
 * adjustments: Sunrise -3, Dhuhr +3, Asr +3, Maghrib +3 minutes.
 * Pure Java, no Android dependencies.
 */
public final class PrayerCalc {

    public static final int FAJR = 0, SUNRISE = 1, DHUHR = 2, ASR = 3, MAGHRIB = 4, ISHA = 5;

    private static final double FAJR_ANGLE = 18.2;
    private static final double ISHA_ANGLE = 18.2;
    private static final double RISE_SET_ANGLE = 0.833;
    private static final int[] ADJUST = {0, -3, 3, 3, 3, 0};

    private PrayerCalc() {}

    /** @return minutes after local midnight for Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha. */
    public static int[] compute(int year, int month, int day, double lat, double lng, double tz, int[] userOffsets) {
        double jDate = julian(year, month, day) - lng / (15 * 24.0);
        double[] t = {5 / 24.0, 6 / 24.0, 12 / 24.0, 13 / 24.0, 18 / 24.0, 18 / 24.0};

        double[] r = new double[6];
        r[FAJR] = sunAngleTime(jDate, FAJR_ANGLE, t[0], true, lat);
        r[SUNRISE] = sunAngleTime(jDate, RISE_SET_ANGLE, t[1], true, lat);
        r[DHUHR] = midDay(jDate, t[2]);
        r[ASR] = asrTime(jDate, 1, t[3], lat);
        r[MAGHRIB] = sunAngleTime(jDate, RISE_SET_ANGLE, t[4], false, lat);
        r[ISHA] = sunAngleTime(jDate, ISHA_ANGLE, t[5], false, lat);

        int[] out = new int[6];
        for (int i = 0; i < 6; i++) {
            double h = r[i] + tz - lng / 15.0;
            int extra = userOffsets != null && userOffsets.length == 6 ? userOffsets[i] : 0;
            out[i] = (int) Math.round(h * 60) + ADJUST[i] + extra;
        }
        return out;
    }

    private static double julian(int y, int m, int d) {
        if (m <= 2) { y -= 1; m += 12; }
        double a = Math.floor(y / 100.0);
        double b = 2 - a + Math.floor(a / 4.0);
        return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + d + b - 1524.5;
    }

    /** @return {declination, equation of time} */
    private static double[] sunPosition(double jd) {
        double D = jd - 2451545.0;
        double g = fixAngle(357.529 + 0.98560028 * D);
        double q = fixAngle(280.459 + 0.98564736 * D);
        double L = fixAngle(q + 1.915 * dsin(g) + 0.020 * dsin(2 * g));
        double e = 23.439 - 0.00000036 * D;
        double ra = darctan2(dcos(e) * dsin(L), dcos(L)) / 15.0;
        double eqt = q / 15.0 - fixHour(ra);
        double decl = darcsin(dsin(e) * dsin(L));
        return new double[]{decl, eqt};
    }

    private static double midDay(double jd, double t) {
        double eqt = sunPosition(jd + t)[1];
        return fixHour(12 - eqt);
    }

    private static double sunAngleTime(double jd, double angle, double t, boolean ccw, double lat) {
        double decl = sunPosition(jd + t)[0];
        double noon = midDay(jd, t);
        double T = darccos((-dsin(angle) - dsin(decl) * dsin(lat)) / (dcos(decl) * dcos(lat))) / 15.0;
        return noon + (ccw ? -T : T);
    }

    private static double asrTime(double jd, double factor, double t, double lat) {
        double decl = sunPosition(jd + t)[0];
        double angle = -darccot(factor + dtan(Math.abs(lat - decl)));
        return sunAngleTime(jd, angle, t, false, lat);
    }

    private static double dsin(double d) { return Math.sin(Math.toRadians(d)); }
    private static double dcos(double d) { return Math.cos(Math.toRadians(d)); }
    private static double dtan(double d) { return Math.tan(Math.toRadians(d)); }
    private static double darcsin(double x) { return Math.toDegrees(Math.asin(x)); }
    private static double darccos(double x) { return Math.toDegrees(Math.acos(x)); }
    private static double darctan2(double y, double x) { return Math.toDegrees(Math.atan2(y, x)); }
    private static double darccot(double x) { return Math.toDegrees(Math.atan(1 / x)); }
    private static double fixAngle(double a) { return fix(a, 360); }
    private static double fixHour(double a) { return fix(a, 24); }
    private static double fix(double a, double b) {
        a = a - b * Math.floor(a / b);
        return a < 0 ? a + b : a;
    }
}
