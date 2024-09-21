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
package com.crashtestdummylimited.navydecoderplus.ui;

import com.crashtestdummylimited.navydecoderplus.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.widget.AppCompatButton;

import android.view.ContextThemeWrapper;

import java.util.Objects;

// http://www.androidsnippets.com/prompt-engaged-users-to-rate-your-app-in-the-android-market-appirater

public class AppRater {
  private final static String APP_PNAME = "com.crashtestdummylimited.navydecoder";

  private final static int DAYS_UNTIL_PROMPT = 3;
  private final static int LAUNCHES_UNTIL_PROMPT = 4;

  public static void app_launched(Context mContext) {
    SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
    if (prefs.getBoolean("dontshowagain", false)) {
      return;
    }

    SharedPreferences.Editor editor = prefs.edit();

    // Increment launch counter
    long launch_count = prefs.getLong("launch_count", 0) + 1;
    editor.putLong("launch_count", launch_count);

    // Get date of first launch
    long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
    if (date_firstLaunch == 0) {
      date_firstLaunch = System.currentTimeMillis();
      editor.putLong("date_firstlaunch", date_firstLaunch);
    }

    // Wait at least n days before opening
    if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
      if (System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
        showRateDialog(mContext, editor);
      }
    }

    editor.apply();
  }

  @SuppressWarnings("WeakerAccess")
  public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {


    final ContextThemeWrapper ctw = new ContextThemeWrapper(mContext, R.style.DialogStyle);

    final AppCompatDialog dialog = new AppCompatDialog(ctw);

    dialog.setContentView(R.layout.rater_dialog);

    AppCompatButton button1 = dialog.findViewById(R.id.rateApp);
    Objects.requireNonNull(button1).setOnClickListener(v -> {
      if (editor != null) {
        editor.putBoolean("dontshowagain", true);
        editor.commit();
      }

      try {
        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
      } catch (Exception e) {
        // Do nothing.  If unable to find the market (emulator or maybe Kindle Fire) just ignore and move on
      }
      dialog.dismiss();
    });

    AppCompatButton button2 = dialog.findViewById(R.id.rateAppLater);
    Objects.requireNonNull(button2).setOnClickListener(v -> {
      //  Do not show the dialog right away on the next app opening,  reset the date to push off
      if (editor != null) {
        long date_firstLaunch = System.currentTimeMillis();
        editor.putLong("date_firstlaunch", date_firstLaunch);
        editor.commit();
      }

      dialog.dismiss();
    });

    AppCompatButton button3 = dialog.findViewById(R.id.rateNoThanksButton);
    Objects.requireNonNull(button3).setOnClickListener(v -> {
      if (editor != null) {
        editor.putBoolean("dontshowagain", true);
        editor.commit();
      }
      dialog.dismiss();
    });

    dialog.show();
  }
}