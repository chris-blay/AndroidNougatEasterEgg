/*
 * Copyright (C) 2016 The Android Open Source Project
 * Copyright (C) 2017, 2018, 2019 Christopher Blay <chris.b.blay@gmail.com>
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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.List;
import java.util.Random;

import static android.app.NotificationManager.IMPORTANCE_DEFAULT;

public final class NekoService extends JobService {

    private static final int JOB_ID = 42;
    private static final int CAT_NOTIFICATION = 1;
    private static final long SECONDS = 1000;
    private static final long MINUTES = 60 * SECONDS;
    private static final long INTERVAL_FLEX = 5 * MINUTES;
    private static final float INTERVAL_JITTER_FRAC = 0.25f;
    private static final long[] PURR = {0, 40, 20, 40, 20, 40, 20, 40, 20, 40, 20, 40};
    private static final String CHANNEL_ID = "arrivals";
    private static final String CHANNEL_NAME = "Arrivals";
    private static final String TAG = "NekoService";
    static final Random RANDOM = new Random();

    @SuppressWarnings("ObsoleteSdkInt")
    @Override
    public boolean onStartJob(JobParameters params) {
        final PrefState prefs = new PrefState(this);
        final int food = prefs.getFoodState();
        if (food != 0) {
            prefs.setFoodState(0); // nom
            final Cat cat;
            final List<Cat> cats = prefs.getCats();
            final int[] probs = getResources().getIntArray(R.array.food_new_cat_prob);
            final float newCatProb = (float)((food < probs.length) ? probs[food] : 50) / 100f;
            if (cats.size() == 0 || RANDOM.nextFloat() <= newCatProb) {
                cat = Cat.create(this);
                prefs.addCat(cat);
            } else {
                cat = cats.get(RANDOM.nextInt(cats.size()));
            }
            final NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                if (Build.VERSION.SDK_INT >= 29) {
                    final NotificationChannel channel =
                            new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, IMPORTANCE_DEFAULT);
                    channel.setSound(null, null);
                    channel.setVibrationPattern(PURR);
                    manager.createNotificationChannel(channel);
                }
                manager.notify(CAT_NOTIFICATION, cat.buildNotification(this, CHANNEL_ID, PURR));
            } else {
                Log.w(TAG, "Null NotificationManager in onStartJob()");
            }
        }
        cancelJob(this);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    static void registerJobIfNeeded(Context context, long intervalMinutes) {
        final JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        if (jobScheduler == null) {
            Log.w(TAG, "Null JobScheduler in registerJobIfNeeded()");
            return;
        }
        if (jobScheduler.getPendingJob(JOB_ID) == null) {
            registerJob(context, intervalMinutes);
        }
    }

    public static void registerJob(Context context, long intervalMinutes) {
        final JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        if (jobScheduler == null) {
            Log.w(TAG, "Null JobScheduler in registerJob()");
            return;
        }
        jobScheduler.cancel(JOB_ID);
        long interval = intervalMinutes * MINUTES;
        final long jitter = (long)(INTERVAL_JITTER_FRAC * interval);
        interval += (long)(Math.random() * (2 * jitter)) - jitter;
        final JobInfo jobInfo =
                new JobInfo.Builder(JOB_ID, new ComponentName(context, NekoService.class))
                        .setPeriodic(interval, INTERVAL_FLEX).build();
        jobScheduler.schedule(jobInfo);
    }

    public static void cancelJob(Context context) {
        final JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        if (jobScheduler == null) {
            Log.w(TAG, "Null JobScheduler in cancelJob()");
            return;
        }
        jobScheduler.cancel(JOB_ID);
    }
}
