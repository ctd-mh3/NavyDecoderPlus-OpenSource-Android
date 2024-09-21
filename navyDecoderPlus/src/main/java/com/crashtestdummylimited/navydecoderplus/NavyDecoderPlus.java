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
import com.crashtestdummylimited.navydecoderplus.ui.AppRater;
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
import androidx.preference.PreferenceManager;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
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

    setTheme(R.style.AppTheme);

    setContentView(R.layout.main_screen);

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

    mListView.setAdapter(new ArrayAdapter<>(this, R.layout.main_screen_selection_list_item, mDecodeOptions));
    mListView.setOnItemClickListener((parent, view, position, id) -> {
      // Handle list selections here
      String mSelectedText = (String) ((TextView) view).getText();

      if (mSelectedText.equals(getString(R.string.categoryRfasEnlistedCodes))) {
        Intent mIntent = new Intent(NavyDecoderPlus.this, RfasActivity.class);
        // TODO- remove this hard coding
        mIntent.putExtra("RFAS_TYPE", "Enlisted");
        startActivity(mIntent);
      } else if (mSelectedText.equals(getString(R.string.categoryRfasOfficerCodes))) {
        Intent mIntent = new Intent(NavyDecoderPlus.this, RfasActivity.class);
        mIntent.putExtra("RFAS_TYPE", "Officer");
        startActivity(mIntent);
      } else {
        // TODO- Improve this
        Intent mIntent;

        if (mSelectedText.equals(getString(R.string.categoryAqdCodes))) {
          mIntent = new Intent(NavyDecoderPlus.this, BlankActivityAqdCodes.class);
        } else if (mSelectedText.equals(getString(R.string.categoryEnlistedRatingCodes))) {
          mIntent = new Intent(NavyDecoderPlus.this, BlankActivityEnlistedRatingCodes.class);
        } else if (mSelectedText.equals(getString(R.string.categoryImsCodes))) {
          mIntent = new Intent(NavyDecoderPlus.this, BlankActivityImsCodes.class);
        } else if (mSelectedText.equals(getString(R.string.categoryMasCodes))) {
          mIntent = new Intent(NavyDecoderPlus.this, BlankActivityMasCodes.class);
        } else if (mSelectedText.equals(getString(R.string.categoryNecCodes))) {
          mIntent = new Intent(NavyDecoderPlus.this, BlankActivityNecCodes.class);
        } else if (mSelectedText.equals(getString(R.string.categoryNavyReserveActivitiesCodes))) {
          mIntent = new Intent(NavyDecoderPlus.this, BlankActivityNraCodes.class);
        } else if (mSelectedText.equals(getString(R.string.categoryReserveUnitIdentificationCodes))) {
          mIntent = new Intent(NavyDecoderPlus.this, BlankActivityRuiCodes.class);
        } else if (mSelectedText.equals(getString(R.string.categoryNobcCodes))) {
          mIntent = new Intent(NavyDecoderPlus.this, BlankActivityNobcCodes.class);
        } else if (mSelectedText.equals(getString(R.string.categoryOfficerBilletCodes))) {
          mIntent = new Intent(NavyDecoderPlus.this, BlankActivityOfficerBilletCodes.class);
        } else if (mSelectedText.equals(getString(R.string.categoryOfficerDesignatorCodes))) {
          mIntent = new Intent(NavyDecoderPlus.this, BlankActivityOfficerDesignatorCodes.class);
        } else if (mSelectedText.equals(getString(R.string.categoryOfficerPaygradeCodes))) {
          mIntent = new Intent(NavyDecoderPlus.this, BlankActivityOfficerPaygradeCodes.class);
        } else if (mSelectedText.equals(getString(R.string.categoryRbscBilletCodes))) {
          mIntent = new Intent(NavyDecoderPlus.this, BlankActivityRbscBilletCodes.class);
        } else if (mSelectedText.equals(getString(R.string.categorySubspecialityCodes))) {
          mIntent = new Intent(NavyDecoderPlus.this, BlankActivitySspCodes.class);
        } else if (mSelectedText.equals(getString(R.string.categoryReserveProgramCodes))) {
          mIntent = new Intent(NavyDecoderPlus.this, BlankActivityRpCodes.class);
        } else {
          throw new IllegalArgumentException("Invalid value passed to onItemClick().");
        }


        MappingHelper mMappingHelper = MappingHelper.getInstance(getApplicationContext());

        // TODO - What happens if no match
        mIntent.putExtra(MappingHelper.CATEGORY_KEY_IDENTIFIER, mMappingHelper.getCategoryIdentify(mSelectedText));
        startActivity(mIntent);
      }
    });

    // For debugging
    //AppRater.showRateDialog(this, null);

    // For production
    AppRater.app_launched(this);

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

    AlertDialog changeLog = ChangelogBuilder.create(this, (dialogInterface, i) -> {
      // Mark this version as read
      sp.edit().putLong(LAST_VERSION_CODE_KEY, versionCode).apply();

      dialogInterface.dismiss();
    });

    changeLog.setOnShowListener(dialog -> {

      Button button = changeLog.getButton(DialogInterface.BUTTON_POSITIVE);
      button.setTextColor(ContextCompat.getColor(this, R.color.resultDescription_TextColor));
      button.setBackgroundColor(ContextCompat.getColor(this, R.color.resultDescription_Background));
    });

    changeLog.show();
  }

}