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
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import com.crashtestdummylimited.navydecoderplus.R;


/**
 * Changelog builder to create the changelog screen.
 */
public final class ChangelogBuilder {
  /** LOG Constant. **/
  private static final String TAG = "ChangelogBuilder";

  /** Private constructor. */
  private ChangelogBuilder() {
  }

  /**
   * Show the dialog only if not already shown for this version of the
   * application.
   *
   * @param context
   *            the context
   * @param listener
   *            the listener to be set for the clickevent of the 'OK' button
   * @return the 'Changelog' dialog
   */
  public static androidx.appcompat.app.AlertDialog create(final Context context, final Dialog.OnClickListener listener) {

    @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.changelog, null);
    WebView webView = view.findViewById(R.id.changelogcontent);

    // Build CSS to match app theme colors (reads from resources so it stays in sync with theme changes)
    int bgColorInt   = ContextCompat.getColor(context, R.color.changeLogBackgroundColor);
    int textColorInt = ContextCompat.getColor(context, R.color.changeLogTextColor);
    String bgHex   = String.format(Locale.US, "#%06X", (0xFFFFFF & bgColorInt));
    String textHex = String.format(Locale.US, "#%06X", (0xFFFFFF & textColorInt));
    String css = "<style>"
        + "body{background-color:" + bgHex + ";color:" + textHex + ";margin:8px;padding:0;font-family:sans-serif;}"
        + "ul{padding-left:20px;margin:4px 0;}"
        + "li{margin-bottom:6px;}"
        + "</style>";

    try {
      String rawContent = DataLoader.loadData(context, R.raw.changelog);
      if (rawContent != null) {
        // loadDataWithBaseURL is used instead of loadData because loadData truncates content
        // at '#' characters, which breaks hex color codes in injected CSS.
        webView.loadDataWithBaseURL(null, css + rawContent, "text/html", "UTF-8", null);
      }
    } catch (IOException ioe) {
      Log.e(TAG, "Error reading changelog file!", ioe);
    }

    androidx.appcompat.app.AlertDialog.Builder alertDialog = new AlertDialog.Builder(context, R.style.ChangeDialogStyle);
    alertDialog.setView(view);
    alertDialog.setPositiveButton(android.R.string.ok, listener);

    // Custom title with larger text, padding and bold to match PayInfoBuilder styling
    int hPadPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
        context.getResources().getDisplayMetrics());
    int vPadPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14,
        context.getResources().getDisplayMetrics());
    TextView myMsg = new TextView(context);
    myMsg.setText(context.getString(R.string.changelog_title));
    myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
    myMsg.setTextColor(ContextCompat.getColor(context, R.color.changeLogTextColor));
    myMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
    myMsg.setTypeface(null, Typeface.BOLD);
    myMsg.setPadding(hPadPx, vPadPx, hPadPx, vPadPx);
    alertDialog.setCustomTitle(myMsg);

    return alertDialog.create();
  }
}