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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrefState implements OnSharedPreferenceChangeListener {

    private static final String FILE_NAME = "mPrefs";
    private static final String FOOD_STATE = "food";
    private static final String SORT = "sort";
    private static final String CAT_KEY_PREFIX = "cat:";

    private final Context mContext;
    private final SharedPreferences mPrefs;
    private PrefsListener mListener;

    PrefState(Context context) {
        mContext = context;
        mPrefs = mContext.getSharedPreferences(FILE_NAME, 0);
    }

    // Can also be used for renaming.
    void addCat(Cat cat) {
        mPrefs.edit()
              .putString(CAT_KEY_PREFIX + String.valueOf(cat.getSeed()), cat.getName())
              .apply();
    }

    void removeCat(Cat cat) {
        mPrefs.edit().remove(CAT_KEY_PREFIX + String.valueOf(cat.getSeed())).apply();
    }

    List<Cat> getCats() {
        final List<Cat> cats = new ArrayList<>();
        final Map<String, ?> map = mPrefs.getAll();
        for (String key : map.keySet()) {
            if (key.startsWith(CAT_KEY_PREFIX)) {
                final long seed = Long.parseLong(key.substring(CAT_KEY_PREFIX.length()));
                cats.add(new Cat(mContext, seed, String.valueOf(map.get(key))));
            }
        }
        return cats;
    }

    int getFoodState() {
        return mPrefs.getInt(FOOD_STATE, 0);
    }

    void setFoodState(int foodState) {
        mPrefs.edit().putInt(FOOD_STATE, foodState).apply();
    }

    @Sort
    int getSort() {
        return mPrefs.getInt(SORT, Sort.BODY_HUE);
    }

    void setSort(@Sort int sort) {
        mPrefs.edit().putInt(SORT, sort).apply();
    }

    void setListener(PrefsListener listener) {
        mListener = listener;
        if (mListener != null) {
            mPrefs.registerOnSharedPreferenceChangeListener(this);
        } else {
            mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mListener.onPrefsChanged();
    }

    interface PrefsListener {
        void onPrefsChanged();
    }
}
