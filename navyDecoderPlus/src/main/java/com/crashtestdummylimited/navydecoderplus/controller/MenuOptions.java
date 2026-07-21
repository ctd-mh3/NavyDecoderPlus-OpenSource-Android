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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.crashtestdummylimited.navydecoderplus.R;
import com.crashtestdummylimited.navydecoderplus.util.CommonUtilities;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

public class MenuOptions {

  // *************************************************************************
  //
  //  Overwritten to support menu
  //
  // *************************************************************************

  //	@Override
  public static void onCreateOptionsMenu(Activity activity, Menu menu) {
    MenuInflater inflater = activity.getMenuInflater();
    inflater.inflate(R.menu.menu, menu);
  }

  //	@Override
  public static void onOptionsItemSelected(Activity activity, MenuItem item) {
    int itemId = item.getItemId(); // For email
    // http://mobile.tutsplus.com/tutorials/android/android-email-intent/
    if (itemId == R.id.optionsMenuAbout) {
      String message =
          activity.getString(R.string.aboutApplicationPurpose)
              + "\n\n"
              + activity.getString(R.string.aboutDeveloper)
              + "\n\n"
              + activity.getString(R.string.aboutComments)
              + "\n\n"
              + activity.getString(R.string.aboutDisclaimer)
              + "\n\n"
              + activity.getString(R.string.aboutVersion)
              + CommonUtilities.getAppVersionName(activity);

      new MaterialAlertDialogBuilder(activity, R.style.MenuDialogStyle)
          .setTitle(R.string.aboutTitle)
          .setMessage(message)
          .setPositiveButton(R.string.ok, null)
          .show();
    } else if (itemId == R.id.optionsMenuOpenSource) {
      String message =
          activity.getString(R.string.opensourceNoticeLine1)
              + "\n\n"
              + activity.getString(R.string.opensourceNoticeLine2)
              + "\n\n"
              + activity.getString(R.string.opensourceNoticeLine3);

      new MaterialAlertDialogBuilder(activity, R.style.MenuDialogStyle)
          .setTitle(R.string.opensourceTitle)
          .setMessage(message)
          .setPositiveButton(R.string.ok, null)
          .show();
    } else if (itemId == R.id.optionsMenuRateApp) {
      ReviewManager reviewManager = ReviewManagerFactory.create(activity);
      reviewManager
          .requestReviewFlow()
          .addOnCompleteListener(
              task -> {
                if (task.isSuccessful()) {
                  ReviewInfo reviewInfo = task.getResult();
                  reviewManager.launchReviewFlow(activity, reviewInfo);
                } else {
                  openPlayStorePage(activity);
                }
              });
    } else if (itemId == R.id.optionsMenuShare) {
      String packageName = activity.getPackageName();
      String shareText =
          activity.getString(R.string.shareText)
              + "\nhttps://play.google.com/store/apps/details?id="
              + packageName;
      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.setType("text/plain");
      shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
      activity.startActivity(
          Intent.createChooser(shareIntent, activity.getString(R.string.shareChooserTitle)));
    } else if (itemId == R.id.optionsMenuEmailAuthor) {
      Intent emailIntent = new Intent(Intent.ACTION_SEND);
      emailIntent.setType("message/rfc822");
      emailIntent.putExtra(
          Intent.EXTRA_EMAIL,
          new String[] {activity.getString(R.string.authorEmailAddress)}); // recipients
      emailIntent.putExtra(
          Intent.EXTRA_SUBJECT,
          "Android-"
              + activity.getString(R.string.authorEmailSubject)
              + "(v"
              + CommonUtilities.getAppVersionName(activity)
              + ") Comment");

      // Always use string resources for UI text.
      String title = activity.getResources().getString(R.string.chooserTitle);
      // Create intent to show chooser
      Intent chooser = Intent.createChooser(emailIntent, title);

      // Try to invoke the intent.
      try {
        activity.startActivity(chooser);
      } catch (ActivityNotFoundException e) {
        new MaterialAlertDialogBuilder(activity, R.style.MenuDialogStyle)
            .setTitle(R.string.emailErrorTitle)
            .setMessage(R.string.emailErrorMessage)
            .setPositiveButton(R.string.ok, null)
            .show();
      }
    } else if (itemId == R.id.optionsMenuPrivacyPolicy) {
      activity.startActivity(
          new Intent(
              Intent.ACTION_VIEW, Uri.parse("https://crashtestdummylimited.com/page1.html")));
    }
  }

  // *************************************************************************
  //  End Menu Support Code
  // *************************************************************************

  private static void openPlayStorePage(Activity activity) {
    String packageName = activity.getPackageName();
    try {
      activity.startActivity(
          new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
    } catch (ActivityNotFoundException e) {
      activity.startActivity(
          new Intent(
              Intent.ACTION_VIEW,
              Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
    }
  }
}
