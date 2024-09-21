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

import java.util.Objects;

import com.crashtestdummylimited.navydecoderplus.R;
import com.crashtestdummylimited.navydecoderplus.util.CommonUtilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.widget.AppCompatButton;

public class MenuOptions {

  //*************************************************************************
  //
  //  Overwritten to support menu
  //
  //*************************************************************************

  //	@Override
  public static void onCreateOptionsMenu(Activity activity, Menu menu) {
    MenuInflater inflater = activity.getMenuInflater();
    inflater.inflate(R.menu.menu, menu);
  }

  //	@Override
  public static void onOptionsItemSelected(Activity activity, MenuItem item) {
    int itemId = item.getItemId();// For email http://mobile.tutsplus.com/tutorials/android/android-email-intent/
    if (itemId == R.id.optionsMenuAbout) {
      final ContextThemeWrapper ctw = new ContextThemeWrapper(activity, R.style.DialogStyle);

      final AppCompatDialog dialog = new AppCompatDialog(ctw);

      dialog.setContentView(R.layout.about_screen);

      String message = activity.getString(R.string.aboutApplicationPurpose) +
          "\n\n" +
          activity.getString(R.string.aboutDeveloper) +
          "\n\n" +
          activity.getString(R.string.aboutComments) +
          "\n\n" +
          activity.getString(R.string.aboutDisclaimer) +
          "\n\n" +
          activity.getString(R.string.aboutVersion) +
          CommonUtilities.getAppVersionName(activity);

      TextView textView = dialog.findViewById(R.id.aboutInfo);

      Objects.requireNonNull(textView).setText(message);

      AppCompatButton button1 = dialog.findViewById(R.id.dismissAboutDialogButton);
      Objects.requireNonNull(button1).setOnClickListener(v -> dialog.dismiss());

      dialog.show();
    } else if (itemId == R.id.optionsMenuOpenSource) {
      final ContextThemeWrapper ctw = new ContextThemeWrapper(activity, R.style.DialogStyle);

      final AppCompatDialog dialog = new AppCompatDialog(ctw);

      dialog.setContentView(R.layout.opensource_screen);

      String message = activity.getString(R.string.opensourceNoticeLine1) +
          "\n\n" +
          activity.getString(R.string.opensourceNoticeLine2) +
          "\n\n" +
          activity.getString(R.string.opensourceNoticeLine3);

      TextView textView = dialog.findViewById(R.id.openSourceInfo);

      Objects.requireNonNull(textView).setText(message);

      AppCompatButton button1 = dialog.findViewById(R.id.dismissOpenSourceDialogButton);
      Objects.requireNonNull(button1).setOnClickListener(v -> dialog.dismiss());

      dialog.show();
    } else if (itemId == R.id.optionsMenuEmailAuthor) {
      Intent emailIntent = new Intent(Intent.ACTION_SEND);
      emailIntent.setType("message/rfc822");
      emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{activity.getString(R.string.authorEmailAddress)}); // recipients
      emailIntent.putExtra(Intent.EXTRA_SUBJECT,
          activity.getString(R.string.authorEmailSubject) +
              "(" + CommonUtilities.getAppVersionName(activity) + ")");

      // Always use string resources for UI text.
      String title = activity.getResources().getString(R.string.chooserTitle);
      // Create intent to show chooser
      Intent chooser = Intent.createChooser(emailIntent, title);

      // Try to invoke the intent.
      try {
        activity.startActivity(chooser);
      } catch (ActivityNotFoundException e) {
        // Define what your app should do if no activity can handle the intent.
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(activity);
        alertDialog2.setTitle(activity.getString(R.string.emailErrorTitle));
        alertDialog2.setMessage(R.string.emailErrorMessage);
        alertDialog2.setPositiveButton("OK", (dialog, which) -> {
        });
        alertDialog2.show();
      }
    }
  }
  //*************************************************************************
  //  End Menu Support Code
  //*************************************************************************
}