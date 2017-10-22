/*
 * Copyright (C) 2016 The Android Open Source Project
 * Copyright (C) 2017 Christopher Blay <chris.b.blay@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.covertbagel.neko;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.support.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.util.Random;

public class Cat extends Drawable {

    private static final long[] PURR = {0, 40, 20, 40, 20, 40, 20, 40, 20, 40, 20, 40};

    private Random mNotSoRandom;
    private Bitmap mBitmap;
    private long mSeed;
    private String mName;
    private int mBodyColor;

    private synchronized Random notSoRandom(long seed) {
        if (mNotSoRandom == null) {
            mNotSoRandom = new Random(seed);
        }
        return mNotSoRandom;
    }

    private static float frandrange(Random r, float a, float b) {
        return (b-a)*r.nextFloat() + a;
    }

    private static Object choose(Random r, Object...l) {
        return l[r.nextInt(l.length)];
    }

    private static int chooseP(Random r, int[] a) {
        int pct = r.nextInt(1000);
        final int stop = a.length-2;
        int i=0;
        while (i<stop) {
            pct -= a[i];
            if (pct < 0) break;
            i+=2;
        }
        return a[i+1];
    }

    private static final int[] P_BODY_COLORS = {
            180, 0xFF212121, // black
            180, 0xFFFFFFFF, // white
            140, 0xFF616161, // gray
            140, 0xFF795548, // brown
            100, 0xFF90A4AE, // steel
            100, 0xFFFFF9C4, // buff
            100, 0xFFFF8F00, // orange
              5, 0xFF29B6F6, // blue..?
              5, 0xFFFFCDD2, // pink!?
              5, 0xFFCE93D8, // purple?!?!?
              4, 0xFF43A047, // yeah, why not green
              1, 0,          // ?!?!?!
    };

    private static final int[] P_COLLAR_COLORS = {
            250, 0xFFFFFFFF,
            250, 0xFF000000,
            250, 0xFFF44336,
             50, 0xFF1976D2,
             50, 0xFFFDD835,
             50, 0xFFFB8C00,
             50, 0xFFF48FB1,
             50, 0xFF4CAF50,
    };

    private static final int[] P_BELLY_COLORS = {
            750, 0,
            250, 0xFFFFFFFF,
    };

    private static final int[] P_DARK_SPOT_COLORS = {
            700, 0,
            250, 0xFF212121,
             50, 0xFF6D4C41,
    };

    private static final int[] P_LIGHT_SPOT_COLORS = {
            700, 0,
            300, 0xFFFFFFFF,
    };

    private CatParts D;

    private static void tint(int color, Drawable ... ds) {
        for (Drawable d : ds) {
            if (d != null) {
                d.mutate().setTint(color);
            }
        }
    }

    private static boolean isDark(int color) {
        final int r = (color & 0xFF0000) >> 16;
        final int g = (color & 0x00FF00) >> 8;
        final int b = color & 0x0000FF;
        return (r + g + b) < 0x80;
    }

    Cat(Context context, long seed, String name) {
        D = new CatParts(context);
        mSeed = seed;
        setName(name);

        final Random nsr = notSoRandom(seed);

        // body color
        mBodyColor = chooseP(nsr, P_BODY_COLORS);
        if (mBodyColor == 0) mBodyColor = Color.HSVToColor(new float[] {
                nsr.nextFloat()*360f, frandrange(nsr,0.5f,1f), frandrange(nsr,0.5f, 1f)});

        tint(mBodyColor, D.body, D.head, D.leg1, D.leg2, D.leg3, D.leg4, D.tail,
                D.leftEar, D.rightEar, D.foot1, D.foot2, D.foot3, D.foot4, D.tailCap);
        tint(0x20000000, D.leg2Shadow, D.tailShadow);
        if (isDark(mBodyColor)) {
            tint(0xFFFFFFFF, D.leftEye, D.rightEye, D.mouth, D.nose);
        }
        tint(isDark(mBodyColor) ? 0xFFEF9A9A : 0x20D50000, D.leftEarInside, D.rightEarInside);

        tint(chooseP(nsr, P_BELLY_COLORS), D.belly);
        tint(chooseP(nsr, P_BELLY_COLORS), D.back);
        final int faceColor = chooseP(nsr, P_BELLY_COLORS);
        tint(faceColor, D.faceSpot);
        if (!isDark(faceColor)) {
            tint(0xFF000000, D.mouth, D.nose);
        }

        if (nsr.nextFloat() < 0.25f) {
            tint(0xFFFFFFFF, D.foot1, D.foot2, D.foot3, D.foot4);
        } else {
            if (nsr.nextFloat() < 0.25f) {
                tint(0xFFFFFFFF, D.foot1, D.foot3);
            } else if (nsr.nextFloat() < 0.25f) {
                tint(0xFFFFFFFF, D.foot2, D.foot4);
            } else if (nsr.nextFloat() < 0.1f) {
                tint(0xFFFFFFFF, (Drawable) choose(nsr, D.foot1, D.foot2, D.foot3, D.foot4));
            }
        }

        tint(nsr.nextFloat() < 0.333f ? 0xFFFFFFFF : mBodyColor, D.tailCap);

        final int capColor = chooseP(nsr, isDark(mBodyColor) ? P_LIGHT_SPOT_COLORS : P_DARK_SPOT_COLORS);
        tint(capColor, D.cap);

        final int collarColor = chooseP(nsr, P_COLLAR_COLORS);
        tint(collarColor, D.collar);
        tint((nsr.nextFloat() < 0.1f) ? collarColor : 0, D.bowtie);
    }

    static Cat create(Context context) {
        final long seed = Math.abs(NekoService.RANDOM.nextLong());
        return new Cat(context, seed, context.getString(
                R.string.default_cat_name, String.valueOf(seed % 1000)));
    }

    Notification.Builder buildNotification(Context context) {
        final Intent intent = new Intent(Intent.ACTION_MAIN)
                .setClass(context, NekoLand.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return new Notification.Builder(context)
                .setSmallIcon(Icon.createWithResource(context, R.drawable.stat_icon))
                .setLargeIcon(createNotificationLargeIcon(context))
                .setColor(getBodyColor())
                .setPriority(Notification.PRIORITY_LOW)
                .setContentTitle(context.getString(R.string.notification_title))
                .setShowWhen(true)
                .setCategory(Notification.CATEGORY_STATUS)
                .setContentText(getName())
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
                .setAutoCancel(true)
                .setVibrate(PURR);
    }

    long getSeed() {
        return mSeed;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        final int widthAndHeight = Math.min(canvas.getWidth(), canvas.getHeight());

        if (mBitmap == null
                || mBitmap.getWidth() != widthAndHeight || mBitmap.getHeight() != widthAndHeight) {
            mBitmap = Bitmap.createBitmap(widthAndHeight, widthAndHeight, Bitmap.Config.ARGB_8888);
            final Canvas bitCanvas = new Canvas(mBitmap);
            slowDraw(bitCanvas, 0, 0, widthAndHeight, widthAndHeight);
        }
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    private void slowDraw(Canvas canvas, int x, int y, int w, int h) {
        for (int i = 0; i < D.drawingOrder.length; i++) {
            final Drawable d = D.drawingOrder[i];
            if (d != null) {
                d.setBounds(x, y, x+w, y+h);
                d.draw(canvas);
            }
        }

    }

    Bitmap createBitmap(int w, int h) {
        if (mBitmap != null && mBitmap.getWidth() == w && mBitmap.getHeight() == h) {
            return mBitmap.copy(mBitmap.getConfig(), true);
        }
        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        slowDraw(new Canvas(result), 0, 0, w, h);
        return result;
    }

    private static Icon recompressIconBitmapIntoIcon(Bitmap bitmap) {
        final ByteArrayOutputStream ostream = new ByteArrayOutputStream(
                bitmap.getWidth() * bitmap.getHeight() * 2); // guess 50% compression
        final boolean ok = bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
        return ok ? Icon.createWithData(ostream.toByteArray(), 0, ostream.size()) : null;
    }

    private Icon createNotificationLargeIcon(Context context) {
        final Resources res = context.getResources();
        final int w = 2 * res.getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
        final int h = 2 * res.getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
        return recompressIconBitmapIntoIcon(createIconBitmap(w, h));
    }

    private Bitmap createIconBitmap(int w, int h) {
        final Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(result);
        final Paint pt = new Paint();
        float[] hsv = new float[3];
        Color.colorToHSV(mBodyColor, hsv);
        hsv[2] = (hsv[2] > 0.5f) ? (hsv[2] - 0.25f) : (hsv[2] + 0.25f);
        pt.setColor(Color.HSVToColor(hsv));
        final float r = w / 2;
        canvas.drawCircle(r, r, r, pt);
        final int m = w / 10;
        slowDraw(canvas, m, m, w - m - m, h - m - m);
        return result;
    }

    Icon createIcon(int w, int h) {
        return Icon.createWithBitmap(createIconBitmap(w, h));
    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    String getName() {
        return mName;
    }

    void setName(String name) {
        mName = name;
    }

    int getBodyColor() {
        return mBodyColor;
    }

    private static class CatParts {
        Drawable leftEar;
        Drawable rightEar;
        Drawable rightEarInside;
        Drawable leftEarInside;
        Drawable head;
        Drawable faceSpot;
        Drawable cap;
        Drawable mouth;
        Drawable body;
        Drawable foot1;
        Drawable leg1;
        Drawable foot2;
        Drawable leg2;
        Drawable foot3;
        Drawable leg3;
        Drawable foot4;
        Drawable leg4;
        Drawable tail;
        Drawable leg2Shadow;
        Drawable tailShadow;
        Drawable tailCap;
        Drawable belly;
        Drawable back;
        Drawable rightEye;
        Drawable leftEye;
        Drawable nose;
        Drawable bowtie;
        Drawable collar;
        Drawable[] drawingOrder;

        CatParts(Context context) {
            body = context.getDrawable(R.drawable.body);
            head = context.getDrawable(R.drawable.head);
            leg1 = context.getDrawable(R.drawable.leg1);
            leg2 = context.getDrawable(R.drawable.leg2);
            leg3 = context.getDrawable(R.drawable.leg3);
            leg4 = context.getDrawable(R.drawable.leg4);
            tail = context.getDrawable(R.drawable.tail);
            leftEar = context.getDrawable(R.drawable.left_ear);
            rightEar = context.getDrawable(R.drawable.right_ear);
            rightEarInside = context.getDrawable(R.drawable.right_ear_inside);
            leftEarInside = context.getDrawable(R.drawable.left_ear_inside);
            faceSpot = context.getDrawable(R.drawable.face_spot);
            cap = context.getDrawable(R.drawable.cap);
            mouth = context.getDrawable(R.drawable.mouth);
            foot4 = context.getDrawable(R.drawable.foot4);
            foot3 = context.getDrawable(R.drawable.foot3);
            foot1 = context.getDrawable(R.drawable.foot1);
            foot2 = context.getDrawable(R.drawable.foot2);
            leg2Shadow = context.getDrawable(R.drawable.leg2_shadow);
            tailShadow = context.getDrawable(R.drawable.tail_shadow);
            tailCap = context.getDrawable(R.drawable.tail_cap);
            belly = context.getDrawable(R.drawable.belly);
            back = context.getDrawable(R.drawable.back);
            rightEye = context.getDrawable(R.drawable.right_eye);
            leftEye = context.getDrawable(R.drawable.left_eye);
            nose = context.getDrawable(R.drawable.nose);
            collar = context.getDrawable(R.drawable.collar);
            bowtie = context.getDrawable(R.drawable.bowtie);
            drawingOrder = getDrawingOrder();
        }
        private Drawable[] getDrawingOrder() {
            return new Drawable[] {
                    collar,
                    leftEar, leftEarInside, rightEar, rightEarInside,
                    head,
                    faceSpot,
                    cap,
                    leftEye, rightEye,
                    nose, mouth,
                    tail, tailCap, tailShadow,
                    foot1, leg1,
                    foot2, leg2,
                    foot3, leg3,
                    foot4, leg4,
                    leg2Shadow,
                    body, belly,
                    bowtie
            };
        }
    }
}
