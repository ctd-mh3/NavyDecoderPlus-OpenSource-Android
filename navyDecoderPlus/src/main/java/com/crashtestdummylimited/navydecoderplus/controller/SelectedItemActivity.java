/*
 * This file is part of Navy Decoder Plus-Android.
 *
 * Navy Decoder Plus-Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Navy Decoder Plus-Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Navy Decoder Plus-Android.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2011-2024 Crash Test Dummy Limited, LLC
 */
package com.crashtestdummylimited.navydecoderplus.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.crashtestdummylimited.navydecoderplus.BuildConfig;
import com.crashtestdummylimited.navydecoderplus.R;
import com.crashtestdummylimited.navydecoderplus.databinding.FinalScreenSelectedItemBinding;
import com.crashtestdummylimited.navydecoderplus.model.db.DecodeDatabase;
import com.crashtestdummylimited.navydecoderplus.util.ActivityUiUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

/** Displays a word and its definition. */
public class SelectedItemActivity extends AppCompatActivity {

  // For Play Store In-App Review
  private static final String REVIEW_PREFS = "review_prefs";
  private static final String KEY_FIRST_LAUNCH_MS = "first_launch_ms";
  private static final String KEY_LAST_PROMPT_MS = "last_prompt_ms";
  private static final long MIN_INSTALL_AGE_MS = 3L * 24L * 60L * 60L * 1000L; // 3 days
  private static final long COOLDOWN_MS = 7L * 24L * 60L * 60L * 1000L; // 7 days

  private ReviewManager mReviewManager;
  private Handler mDebugReviewHandler;
  private Runnable mDebugReviewRunnable;

  // *************************************************************************
  //
  //  Overwritten to support menu
  //
  // *************************************************************************

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuOptions.onCreateOptionsMenu(this, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    MenuOptions.onOptionsItemSelected(this, item);
    return true;
  }

  // *************************************************************************
  //  End Menu Support Code
  // *************************************************************************

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!BuildConfig.DEBUG) {
      mReviewManager = ReviewManagerFactory.create(this);
    }

    FinalScreenSelectedItemBinding binding =
        FinalScreenSelectedItemBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);

    ActivityUiUtils.applyDefaultWindowInsets(this);
    ActivityUiUtils.setToolbarTitle(this, R.string.app_name, true);

    Uri uri = getIntent().getData();
    if (uri == null) {
      finish();
      return;
    }
    Cursor cursor = getContentResolver().query(uri, null, null, null, null);

    if (cursor == null) {
      finish();
    } else {
      Intent intent = getIntent();
      String categoryKey = intent.getStringExtra(MappingHelper.CATEGORY_KEY_IDENTIFIER);
      MappingHelper mappingHelper = MappingHelper.getInstance(getApplicationContext());
      // getSelectionText() returns "" via getOrDefault when no match — setText("") is safe.
      String selectionText = mappingHelper.getSelectionText(categoryKey);
      binding.decodeCategoryTextView.setText(selectionText);

      if (!cursor.moveToFirst()) {
        cursor.close();
        finish();
        return;
      }

      int codeIndex = cursor.getColumnIndexOrThrow(DecodeDatabase.KEY_CODE);
      int codeMeaningIndex = cursor.getColumnIndexOrThrow(DecodeDatabase.KEY_CODE_MEANING);
      int codeSourceIndex = cursor.getColumnIndexOrThrow(DecodeDatabase.KEY_CODE_SOURCE);
      binding.itemDecodedTextView.setText(cursor.getString(codeIndex));
      binding.decodedDescriptionTextView.setText(cursor.getString(codeMeaningIndex));
      binding.sourceDescriptionTextView.setText(cursor.getString(codeSourceIndex));

      cursor.close();

      tryRequestReviewIfAppropriate();
    }
  }

  public void tryRequestReviewIfAppropriate() {
    // In debug builds show a plain dialog to confirm the prompt fires at the right
    // time. FakeReviewManager completes silently without any visible UI, so it is
    // not useful for manual timing verification.
    if (BuildConfig.DEBUG) {
      mDebugReviewHandler = new Handler(Looper.getMainLooper());
      mDebugReviewRunnable =
          () -> {
            if (isFinishing()) return;
            new AlertDialog.Builder(this)
                .setTitle("[Debug] Review Prompt")
                .setMessage("In a production build the Play Store review dialog appears here.")
                .setPositiveButton("OK", null)
                .show();
          };
      mDebugReviewHandler.postDelayed(mDebugReviewRunnable, 500);
      return;
    }

    SharedPreferences prefs = getSharedPreferences(REVIEW_PREFS, Context.MODE_PRIVATE);
    long now = System.currentTimeMillis();

    // Record first launch timestamp; don't prompt on the very first run.
    long firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH_MS, 0L);
    if (firstLaunch == 0L) {
      prefs.edit().putLong(KEY_FIRST_LAUNCH_MS, now).apply();
      return;
    }

    // Enforce minimum install age before ever prompting.
    if (now - firstLaunch < MIN_INSTALL_AGE_MS) return;

    // Enforce cooldown between prompts.
    long lastPrompt = prefs.getLong(KEY_LAST_PROMPT_MS, 0L);
    if (now - lastPrompt < COOLDOWN_MS) return;

    // Record this attempt before launching to prevent repeated prompts if Play
    // suppresses the dialog without showing it.
    prefs.edit().putLong(KEY_LAST_PROMPT_MS, now).apply();

    promptInAppReview();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mDebugReviewHandler != null) {
      mDebugReviewHandler.removeCallbacks(mDebugReviewRunnable);
    }
  }

  private void promptInAppReview() {
    Task<ReviewInfo> request = mReviewManager.requestReviewFlow();
    request.addOnCompleteListener(
        requestTask -> {
          if (isFinishing()) return;
          if (requestTask.isSuccessful()) {
            ReviewInfo reviewInfo = requestTask.getResult();
            mReviewManager.launchReviewFlow(this, reviewInfo);
            // The API does not indicate whether the dialog was shown or a review
            // was submitted. Continue app flow regardless of the outcome.
          }
        });
  }
}
