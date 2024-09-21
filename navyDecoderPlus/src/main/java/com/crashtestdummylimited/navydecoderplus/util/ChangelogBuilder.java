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
package com.crashtestdummylimited.navydecoderplus.util;

import java.io.IOException;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

import com.crashtestdummylimited.navydecoderplus.R;

/**
 * Changelog builder to create the changelog screen.
 */
public final class ChangelogBuilder {
  /**
   * LOG Constant.
   **/
  private static final String TAG = "ChangelogBuilder";

  /**
   * Private constructor.
   */
  private ChangelogBuilder() {
  }

  /**
   * Show the dialog only if not already shown for this version of the
   * application.
   *
   * @param context  the context
   * @param listener the listener to be set for the clickevent of the 'OK' button
   * @return the 'Changelog' dialog
   */
  public static AlertDialog create(final Context context, final Dialog.OnClickListener listener) {

    @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.changelog, null);
    WebView webView = view.findViewById(R.id.changelogcontent);

    try {
      webView.loadData(Objects.requireNonNull(DataLoader.loadData(context, R.raw.changelog)), "text/html", "UTF-8");
    } catch (IOException ioe) {
      Log.e(TAG, "Error reading changelog file!", ioe);
    }

    return new AlertDialog.Builder(context)
        .setTitle(
            context.getString(R.string.changelog_title) + "\n"
                + context.getString(R.string.app_name) + " v"
                + CommonUtilities.getActualVersionName(context)).setIcon(R.mipmap.ic_launcher)
        .setView(view).setPositiveButton(android.R.string.ok, listener).create();
  }
}
