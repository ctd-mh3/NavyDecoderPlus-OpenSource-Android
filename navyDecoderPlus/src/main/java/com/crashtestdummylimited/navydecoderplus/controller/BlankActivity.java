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

import com.crashtestdummylimited.navydecoderplus.R;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

// TODO: Look into doing it in this manner
//       http://mavistechchannel.wordpress.com/2011/08/13/blur-background-android-search-dialog/
public class BlankActivity extends AppCompatActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTheme(R.style.AppTheme);
    setContentView(R.layout.blank_screen);

    // This activity is only used to cover up the main screen when the user has selected an item
    //   that invokes the search functionality. When the user attempts to navigate back to the 
    //   to the main screen using the back button this screen should be gone from the stack.
    final SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
    Objects.requireNonNull(searchManager).setOnDismissListener(this::finish);

    // Activity is only started when the user wants to start a search. Therefore kick the search off.
    onSearchRequested();
  }

  @Override
  public boolean onSearchRequested() {

    // Grab info from the intent and pass to the search activity
    Intent mIntent = getIntent();
    String mDecodeCategory = mIntent.getStringExtra(MappingHelper.CATEGORY_KEY_IDENTIFIER);

    Bundle mDecodeData = new Bundle();
    mDecodeData.putString(MappingHelper.CATEGORY_KEY_IDENTIFIER, mDecodeCategory);
    startSearch(null, false, mDecodeData, false);
    return true;
  }
}