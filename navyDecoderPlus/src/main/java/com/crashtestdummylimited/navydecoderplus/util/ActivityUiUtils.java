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

import android.app.Activity;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Common Activity setup shared across the app's four main Activities. */
public final class ActivityUiUtils {

  private ActivityUiUtils() {}

  /** Pads the content view for the system bars — required for edge-to-edge on API 36+. */
  public static void applyDefaultWindowInsets(Activity activity) {
    ViewCompat.setOnApplyWindowInsetsListener(
        activity.getWindow().getDecorView().findViewById(android.R.id.content),
        (v, windowInsets) -> {
          Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
          return WindowInsetsCompat.CONSUMED;
        });
  }

  /**
   * Sets the toolbar title and, when {@code showUp} is true, enables the Up button. AppTheme
   * extends Theme.Material3.DayNight which always provides an ActionBar; the null check is
   * defensive only.
   */
  public static void setToolbarTitle(
      AppCompatActivity activity, @StringRes int titleRes, boolean showUp) {
    if (activity.getSupportActionBar() != null) {
      activity.getSupportActionBar().setTitle(titleRes);
      if (showUp) {
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      }
    }
  }
}
