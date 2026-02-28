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

import com.crashtestdummylimited.navydecoderplus.controller.BlankActivityAqdCodes;
import com.crashtestdummylimited.navydecoderplus.controller.BlankActivityEnlistedRatingCodes;
import com.crashtestdummylimited.navydecoderplus.controller.BlankActivityImsCodes;
import com.crashtestdummylimited.navydecoderplus.controller.BlankActivityMasCodes;
import com.crashtestdummylimited.navydecoderplus.controller.BlankActivityNecCodes;
import com.crashtestdummylimited.navydecoderplus.controller.BlankActivityNobcCodes;
import com.crashtestdummylimited.navydecoderplus.controller.BlankActivityNraCodes;
import com.crashtestdummylimited.navydecoderplus.controller.BlankActivityOfficerBilletCodes;
import com.crashtestdummylimited.navydecoderplus.controller.BlankActivityOfficerDesignatorCodes;
import com.crashtestdummylimited.navydecoderplus.controller.BlankActivityOfficerPaygradeCodes;
import com.crashtestdummylimited.navydecoderplus.controller.BlankActivityRbscBilletCodes;
import com.crashtestdummylimited.navydecoderplus.controller.BlankActivityRpCodes;
import com.crashtestdummylimited.navydecoderplus.controller.BlankActivityRuiCodes;
import com.crashtestdummylimited.navydecoderplus.controller.BlankActivitySspCodes;
import com.crashtestdummylimited.navydecoderplus.controller.MappingHelper;
import com.crashtestdummylimited.navydecoderplus.controller.MenuOptions;
import com.crashtestdummylimited.navydecoderplus.controller.RfasActivity;
import com.crashtestdummylimited.navydecoderplus.util.ChangelogBuilder;
import com.crashtestdummylimited.navydecoderplus.util.CommonUtilities;

//import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class NavyDecoderPlus extends AppCompatActivity {

  private static final String TAG = NavyDecoderPlus.class.getSimpleName();

  /**
   * Key for latest version code preference.
   */
  private static final String LAST_VERSION_CODE_KEY = "last_version_code";

  //*************************************************************************
  //
  //  Overwritten to support menu
  //
  //*************************************************************************

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
  //*************************************************************************
  //  End Menu Support Code
  //*************************************************************************


  /**
   * Called when the activity is first created.
   */
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

    // Set Title Bar Title to official app name
    getSupportActionBar().setTitle(R.string.app_name);

    ListView mListView = findViewById(R.id.mainItemToDecodeListView);

    String[] mDecodeOptions = new String[]{
        this.getString(R.string.categoryAqdCodes),
        this.getString(R.string.categoryEnlistedRatingCodes),
        this.getString(R.string.categoryImsCodes),
        this.getString(R.string.categoryMasCodes),
        this.getString(R.string.categoryNecCodes),
        this.getString(R.string.categoryNavyReserveActivitiesCodes),
        this.getString(R.string.categoryNobcCodes),
        this.getString(R.string.categoryOfficerBilletCodes),
        this.getString(R.string.categoryOfficerDesignatorCodes),
        this.getString(R.string.categoryOfficerPaygradeCodes),
        this.getString(R.string.categoryRbscBilletCodes),
        this.getString(R.string.categoryReserveUnitIdentificationCodes),
        this.getString(R.string.categoryReserveProgramCodes),
        this.getString(R.string.categoryRfasEnlistedCodes),
        this.getString(R.string.categoryRfasOfficerCodes),
        this.getString(R.string.categorySubspecialityCodes)
    };

    mListView.setAdapter(new ArrayAdapter<>(this, R.layout.main_screen_selection_list_item, android.R.id.text1, mDecodeOptions));
    mListView.setOnItemClickListener((parent, view, position, id) -> {
      // RFAS categories use a spinner-based Activity instead of the standard search flow.
      if (position == 13) {
        Intent mIntent = new Intent(NavyDecoderPlus.this, RfasActivity.class);
        mIntent.putExtra("RFAS_TYPE", "Enlisted");
        startActivity(mIntent);
        return;
      }
      if (position == 14) {
        Intent mIntent = new Intent(NavyDecoderPlus.this, RfasActivity.class);
        mIntent.putExtra("RFAS_TYPE", "Officer");
        startActivity(mIntent);
        return;
      }

      Class<?> activityClass;
      switch (position) {
        case 0:  activityClass = BlankActivityAqdCodes.class; break;
        case 1:  activityClass = BlankActivityEnlistedRatingCodes.class; break;
        case 2:  activityClass = BlankActivityImsCodes.class; break;
        case 3:  activityClass = BlankActivityMasCodes.class; break;
        case 4:  activityClass = BlankActivityNecCodes.class; break;
        case 5:  activityClass = BlankActivityNraCodes.class; break;
        case 6:  activityClass = BlankActivityNobcCodes.class; break;
        case 7:  activityClass = BlankActivityOfficerBilletCodes.class; break;
        case 8:  activityClass = BlankActivityOfficerDesignatorCodes.class; break;
        case 9:  activityClass = BlankActivityOfficerPaygradeCodes.class; break;
        case 10: activityClass = BlankActivityRbscBilletCodes.class; break;
        case 11: activityClass = BlankActivityRuiCodes.class; break;
        case 12: activityClass = BlankActivityRpCodes.class; break;
        case 15: activityClass = BlankActivitySspCodes.class; break;
        default: throw new IllegalArgumentException("Invalid position: " + position);
      }

      MappingHelper mMappingHelper = MappingHelper.getInstance(getApplicationContext());
      Intent mIntent = new Intent(NavyDecoderPlus.this, activityClass);
      mIntent.putExtra(MappingHelper.CATEGORY_KEY_IDENTIFIER, mMappingHelper.getCategoryIdentify(mDecodeOptions[position]));
      startActivity(mIntent);
    });

    // For debugging
    //showChangelog();

    // For production
    // show changelog
    if (isUpdate()) {
      showChangelog();
    }

  }

  /**
   * checks if the app is started for the first time (after an update).
   *
   * @return <code>true</code> if this is the first start (after an update)
   * else <code>false</code>
   */
  private boolean isUpdate() {
    // Get the versionCode of the Package, which must be different
    // (incremented) in each release on the market in the
    // AndroidManifest.xml
    final long versionCode = CommonUtilities.getActualVersionCode(this);

    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    final long lastVersionCode = prefs.getLong(LAST_VERSION_CODE_KEY, 0);

    if (versionCode != lastVersionCode) {
      Log.i(TAG, "versionCode " + versionCode + " is different from the last known version "
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
    ChangelogBuilder.create(this, (dialogInterface, i) -> {
      // Mark this version as read
      sp.edit().putLong(LAST_VERSION_CODE_KEY, versionCode).apply();

      dialogInterface.dismiss();
    }).show();
  }

}