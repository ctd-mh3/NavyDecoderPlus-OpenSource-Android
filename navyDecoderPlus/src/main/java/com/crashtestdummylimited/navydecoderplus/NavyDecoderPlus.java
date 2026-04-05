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
package com.crashtestdummylimited.navydecoderplus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;
import com.crashtestdummylimited.navydecoderplus.controller.Category;
import com.crashtestdummylimited.navydecoderplus.controller.MappingHelper;
import com.crashtestdummylimited.navydecoderplus.controller.MenuOptions;
import com.crashtestdummylimited.navydecoderplus.controller.RfasActivity;
import com.crashtestdummylimited.navydecoderplus.controller.SearchableDecoderActivity;
import com.crashtestdummylimited.navydecoderplus.util.ChangelogBuilder;
import com.crashtestdummylimited.navydecoderplus.util.CommonUtilities;

public class NavyDecoderPlus extends AppCompatActivity {

  private static final String TAG = NavyDecoderPlus.class.getSimpleName();

  /** Key for latest version code preference. */
  private static final String LAST_VERSION_CODE_KEY = "last_version_code";

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
    MenuOptions.onOptionsItemSelected(this, item);
    return true;
  }

  // *************************************************************************
  //  End Menu Support Code
  // *************************************************************************

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main_screen);

    ViewCompat.setOnApplyWindowInsetsListener(
        getWindow().getDecorView().findViewById(android.R.id.content),
        (v, windowInsets) -> {
          Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
          return WindowInsetsCompat.CONSUMED;
        });

    // AppTheme extends Theme.Material3.DayNight which always provides an ActionBar; null check is
    // defensive only.
    if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.app_name);

    ListView mListView = findViewById(R.id.mainItemToDecodeListView);

    Category[] categories = Category.values();
    // Position 0 is the global "Search All" entry; categories follow at positions 1..N.
    String[] mDecodeOptions = new String[categories.length + 1];
    mDecodeOptions[0] = getString(R.string.categorySearchAll);
    for (int i = 0; i < categories.length; i++) {
      mDecodeOptions[i + 1] = getString(categories[i].labelRes);
    }

    mListView.setAdapter(
        new ArrayAdapter<>(
            this, R.layout.main_screen_selection_list_item, android.R.id.text1, mDecodeOptions));
    mListView.setOnItemClickListener(
        (parent, view, position, id) -> {
          if (position == 0) {
            // Global search across all categories.
            Intent mIntent = new Intent(NavyDecoderPlus.this, SearchableDecoderActivity.class);
            mIntent.putExtra(
                MappingHelper.CATEGORY_KEY_IDENTIFIER,
                SearchableDecoderActivity.ALL_CATEGORIES_KEY);
            startActivity(mIntent);
            return;
          }

          Category category = Category.values()[position - 1];

          // RFAS categories use a spinner-based Activity instead of the standard search flow.
          if (category == Category.RFAS_ENLISTED || category == Category.RFAS_OFFICER) {
            String rfasType =
                (category == Category.RFAS_ENLISTED)
                    ? RfasActivity.RFAS_TYPE_ENLISTED
                    : RfasActivity.RFAS_TYPE_OFFICER;
            Intent mIntent = new Intent(NavyDecoderPlus.this, RfasActivity.class);
            mIntent.putExtra(RfasActivity.EXTRA_RFAS_TYPE, rfasType);
            startActivity(mIntent);
            return;
          }

          Intent mIntent = new Intent(NavyDecoderPlus.this, SearchableDecoderActivity.class);
          mIntent.putExtra(MappingHelper.CATEGORY_KEY_IDENTIFIER, category.key);
          startActivity(mIntent);
        });

    // For debugging
    // showChangelog();

    // For production
    // show changelog
    if (isUpdate()) {
      showChangelog();
    }
  }

  /**
   * checks if the app is started for the first time (after an update).
   *
   * @return <code>true</code> if this is the first start (after an update) else <code>false</code>
   */
  private boolean isUpdate() {
    // Get the versionCode of the Package, which must be different
    // (incremented) in each release on the market in the
    // AndroidManifest.xml
    final long versionCode = CommonUtilities.getActualVersionCode(this);

    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    final long lastVersionCode = prefs.getLong(LAST_VERSION_CODE_KEY, 0);

    if (versionCode != lastVersionCode) {
      Log.i(
          TAG,
          "versionCode "
              + versionCode
              + " is different from the last known version "
              + lastVersionCode);
      return true;
    } else {
      Log.i(TAG, "versionCode " + versionCode + " is already known");
      return false;
    }
  }

  private void showChangelog() {
    final long versionCode = CommonUtilities.getActualVersionCode(this);
    final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    ChangelogBuilder.create(
            this,
            (dialogInterface, i) -> {
              // Mark this version as read
              sp.edit().putLong(LAST_VERSION_CODE_KEY, versionCode).apply();

              dialogInterface.dismiss();
            })
        .show();
  }
}
