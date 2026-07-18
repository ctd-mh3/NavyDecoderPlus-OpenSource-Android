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

import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

public class AppUpdateChecker {

  private static final String PREFS = "app_update_prefs";
  private static final String KEY_LAST_CHECK_MS = "last_update_check_ms";
  private static final long COOLDOWN_MS = 24L * 60L * 60L * 1000L;

  public static final int REQUEST_CODE = 900;

  public static void checkForUpdate(AppCompatActivity activity) {
    SharedPreferences prefs = activity.getSharedPreferences(PREFS, AppCompatActivity.MODE_PRIVATE);
    long now = System.currentTimeMillis();
    long lastCheck = prefs.getLong(KEY_LAST_CHECK_MS, 0L);

    if (now - lastCheck < COOLDOWN_MS) {
      return;
    }

    // Record now so a failed/offline check also respects the cooldown.
    prefs.edit().putLong(KEY_LAST_CHECK_MS, now).apply();

    AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(activity);
    appUpdateManager
        .getAppUpdateInfo()
        .addOnSuccessListener(
            info -> {
              if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                  && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                appUpdateManager.startUpdateFlow(
                    info, activity, AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build());
              }
            });
  }
}
