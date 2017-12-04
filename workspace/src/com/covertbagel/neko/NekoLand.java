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

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class NekoLand extends Activity implements PrefState.PrefsListener {

    private static final String TAG = "NekoLand";
    private static final int STORAGE_PERM_REQUEST = 123;
    private static final int CAT_GEN = 50; // Set to 0 to disable, N > 0 to generate N cats.

    private static final int EXPORT_BITMAP_SIZE = 600;

    private PrefState mPrefs;
    @Sort private int mSort;
    private CatAdapter mAdapter;
    private Cat mPendingShareCat;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.neko_activity);
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(0);
        }
        mPrefs = new PrefState(this);
        mPrefs.setListener(this);
        mSort = mPrefs.getSort();
        mAdapter = new CatAdapter();
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.holder);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        updateCats();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPrefs.setListener(null);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        super.onCreateOptionsMenu(menu);
        new MenuInflater(this).inflate(R.menu.neko_activity, menu);
        final int checkedId;
        switch (mSort) {
        case Sort.CHRONOLOGICAL:
            checkedId = R.id.sort_chronological;
            break;
        case Sort.BODY_HUE:
            checkedId = R.id.sort_body_hue;
            break;
        case Sort.NAME:
            checkedId = R.id.sort_name;
            break;
        case Sort.LEVEL:
            checkedId = R.id.sort_level;
            break;
        default:
            return true;
        }
        final MenuItem menuItem = menu.findItem(checkedId);
        if (menuItem != null) {
            menuItem.setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int itemId = item.getItemId();
        @Sort final int newSort;
        if (itemId == R.id.sort_chronological) {
            newSort = Sort.CHRONOLOGICAL;
        } else if (itemId == R.id.sort_body_hue) {
            newSort = Sort.BODY_HUE;
        } else if (itemId == R.id.sort_name) {
            newSort = Sort.NAME;
        } else if (itemId == R.id.sort_level) {
            newSort = Sort.LEVEL;
        } else {
            return super.onOptionsItemSelected(item);
        }
        if (mSort != newSort) {
            mSort = newSort;
            invalidateOptionsMenu();
            mPrefs.setSort(newSort);
        }
        return true;
    }

    private void updateCats() {
        final List<Cat> cats;
        if (CAT_GEN > 0) {
            cats = new ArrayList<>(CAT_GEN);
            for (int i = 0; i < CAT_GEN; i++) {
                cats.add(Cat.create(this));
            }
        } else {
            cats = mPrefs.getCats();
        }
        switch (mSort) {
        case Sort.CHRONOLOGICAL:
            break; // No sorting necessary.
        case Sort.BODY_HUE:
            final float[] hsv = new float[3];
            cats.sort((Cat cat, Cat cat2) -> {
                Color.colorToHSV(cat.getBodyColor(), hsv);
                final float bodyH1 = hsv[0];
                Color.colorToHSV(cat2.getBodyColor(), hsv);
                final float bodyH2 = hsv[0];
                return Float.compare(bodyH1, bodyH2);
            });
            break;
        case Sort.NAME:
            //cats.sort(Comparator.comparing(Cat::getName));
            cats.sort((Cat cat, Cat cat2) -> cat.getName().compareTo(cat2.getName()));
            break;
        case Sort.LEVEL:
            //cats.sort(Comparator.comparingLong(Cat::getSeed));
            cats.sort((Cat cat, Cat cat2) -> Float.compare(cat.getSeed(), cat2.getSeed()));
            break;
        }
        mAdapter.setCats(cats.toArray(new Cat[cats.size()]));
    }

    private void onCatClick(Cat cat) {
        if (CAT_GEN > 0) {
            mPrefs.addCat(cat);
            new AlertDialog.Builder(NekoLand.this)
                    .setTitle("Cat added")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        } else {
            showNameDialog(cat);
        }
    }

    private void onCatRemove(Cat cat) {
        mPrefs.removeCat(cat);
    }

    private void showNameDialog(final Cat cat) {
        Context context = new ContextThemeWrapper(this,
                android.R.style.Theme_Material_Light_Dialog_NoActionBar);
        // TODO: Move to XML, add correct margins.
        View view = LayoutInflater.from(context).inflate(R.layout.edit_text, null);
        final EditText text = (EditText) view.findViewById(android.R.id.edit);
        text.setText(cat.getName());
        text.setSelection(cat.getName().length());
        final int size = context.getResources()
                .getDimensionPixelSize(android.R.dimen.app_icon_size);
        final Drawable catIcon = cat.createIcon(size, size).loadDrawable(this);
        new AlertDialog.Builder(context)
                .setTitle(" ")
                .setIcon(catIcon)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int which) -> {
                    cat.setName(text.getText().toString().trim());
                    mPrefs.addCat(cat);
                }).show();
    }

    @Override
    public void onPrefsChanged() {
        updateCats();
    }

    private class CatAdapter extends RecyclerView.Adapter<CatHolder> {

        private Cat[] mCats;

        void setCats(Cat[] cats) {
            mCats = cats;
            notifyDataSetChanged();
        }

        @Override
        public CatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CatHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cat_view, parent, false));
        }

        private void setContextGroupVisible(final CatHolder holder, boolean vis) {
            final View group = holder.contextGroup;
            if (vis && group.getVisibility() != View.VISIBLE) {
                group.setAlpha(0);
                group.setVisibility(View.VISIBLE);
                group.animate().alpha(1.0f).setDuration(333);
                final Runnable hideAction = () -> setContextGroupVisible(holder, false);
                group.setTag(hideAction);
                group.postDelayed(hideAction, 5000);
            } else if (!vis && group.getVisibility() == View.VISIBLE) {
                group.removeCallbacks((Runnable) group.getTag());
                group.animate().alpha(0f).setDuration(250).withEndAction(
                        () -> group.setVisibility(View.INVISIBLE));
            }
        }

        @Override
        public void onBindViewHolder(final CatHolder holder, final int position) {
            Context context = holder.itemView.getContext();
            final int size = context.getResources()
                    .getDimensionPixelSize(R.dimen.neko_display_size);
            holder.imageView.setImageIcon(mCats[position].createIcon(size, size));
            holder.textView.setText(mCats[position].getName());
            holder.itemView.setOnClickListener(
                    (View v) -> onCatClick(mCats[holder.getAdapterPosition()]));
            holder.itemView.setOnLongClickListener((View v) -> {
                setContextGroupVisible(holder, true);
                return true;
            });
            holder.delete.setOnClickListener((View v) -> {
                setContextGroupVisible(holder, false);
                new AlertDialog.Builder(NekoLand.this)
                    .setTitle(getString(R.string.confirm_delete, mCats[position].getName()))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(
                            android.R.string.ok,
                            (DialogInterface dialog, int which) ->
                                    onCatRemove(mCats[holder.getAdapterPosition()]))
                    .show();
            });
            holder.share.setOnClickListener((View v) -> {
                setContextGroupVisible(holder, false);
                Cat cat = mCats[holder.getAdapterPosition()];
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    mPendingShareCat = cat;
                    requestPermissions(
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            STORAGE_PERM_REQUEST);
                    return;
                }
                shareCat(cat);
            });
        }

        @Override
        public int getItemCount() {
            return mCats.length;
        }
    }

    private void shareCat(final Cat cat) {
        final File dir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                getString(R.string.directory_name));
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e(TAG, "save: error: can't create Pictures directory");
            return;
        }
        final File png = new File(dir, cat.getName().replaceAll("[/ #:]+", "_") + ".png");
        final Bitmap bitmap = cat.createBitmap(EXPORT_BITMAP_SIZE, EXPORT_BITMAP_SIZE);
        if (bitmap != null) {
            try {
                final OutputStream os = new FileOutputStream(png);
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
                os.close();
                MediaScannerConnection.scanFile(this, new String[] {png.toString()},
                        new String[] {"image/png"},
                        (String path, Uri uri) -> {
                            final Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                            intent.putExtra(Intent.EXTRA_SUBJECT, cat.getName());
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.setType("image/png");
                            startActivity(Intent.createChooser(intent, null));
                        });
            } catch (IOException e) {
                Log.e(TAG, "save: error: " + e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERM_REQUEST) {
            if (mPendingShareCat != null) {
                shareCat(mPendingShareCat);
                mPendingShareCat = null;
            }
        }
    }

    private static class CatHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView textView;
        private final View contextGroup;
        private final View delete;
        private final View share;

        CatHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(android.R.id.icon);
            textView = (TextView) itemView.findViewById(android.R.id.title);
            contextGroup = itemView.findViewById(R.id.contextGroup);
            delete = itemView.findViewById(android.R.id.closeButton);
            share = itemView.findViewById(android.R.id.shareText);
        }
    }
}
