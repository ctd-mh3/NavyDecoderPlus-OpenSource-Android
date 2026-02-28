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
import com.crashtestdummylimited.navydecoderplus.databinding.SearchScreenBinding;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

/**
 * The main activity for the decoder.
 * Displays search results triggered by the search dialog and handles
 * actions from search suggestions.
 */
public class SearchableDecoderActivity extends AppCompatActivity {

  private SearchScreenBinding mBinding;

  private String mDecodeCategory = "";

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

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mBinding = SearchScreenBinding.inflate(getLayoutInflater());
    View view = mBinding.getRoot();
    setContentView(view);

    ViewCompat.setOnApplyWindowInsetsListener(
        getWindow().getDecorView().findViewById(android.R.id.content),
        (v, windowInsets) -> {
          Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
          return WindowInsetsCompat.CONSUMED;
        });

    // AppTheme extends Theme.Material3.DayNight which always provides an ActionBar; null check is defensive only.
    if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.app_name);
    mBinding.searchScreenListView.setEmptyView(mBinding.searchScreenEmptyView);

    Intent mIntent = getIntent();

    // Save the information on what is being decoded
    Bundle mAppData = getIntent().getBundleExtra(SearchManager.APP_DATA);
    if (mAppData != null) {
      mDecodeCategory = mAppData.getString(MappingHelper.CATEGORY_KEY_IDENTIFIER);
    }

    if (Intent.ACTION_VIEW.equals(mIntent.getAction())) {

      // Handles a click on a search suggestion; launches activity to show
      //    the specifics of the item clicked
      Intent mItemIntent = new Intent(this, SelectedItemActivity.class);

      mItemIntent.putExtra(MappingHelper.CATEGORY_KEY_IDENTIFIER, mDecodeCategory);
      mItemIntent.setData(mIntent.getData());
      startActivity(mItemIntent);
      finish();
    } else if (Intent.ACTION_SEARCH.equals(mIntent.getAction())) {
      // Handles a search query
      String mQuery = mIntent.getStringExtra(SearchManager.QUERY);
      showResults(mQuery);
    }
  }

  /**
   * Searches the database and displays results for the given query.
   *
   * @param query The search query
   */
  private void showResults(String query) {
    mBinding.searchScreenEmptyView.setText(getString(R.string.no_results, query));

    Uri mUriWithPath = Uri.withAppendedPath(DecodeProvider.CONTENT_URI, mDecodeCategory);
    Cursor mCursor = getContentResolver().query(mUriWithPath, null, null, new String[]{query}, null);

    if (mCursor == null) {
      // Provider error: setEmptyView() only auto-fires once an adapter is attached,
      // so reveal the empty view manually for this edge case.
      mBinding.searchScreenEmptyView.setVisibility(View.VISIBLE);
      return;
    }

    ListView lvItems = mBinding.searchScreenListView;
    lvItems.setAdapter(new SearchResultsCursorAdapter(this, mCursor));

    lvItems.setOnItemClickListener((parent, view, position, id) -> {
      Intent mItemIntent = new Intent(getApplicationContext(), SelectedItemActivity.class);
      Uri itemUri = Uri.withAppendedPath(
          Uri.withAppendedPath(DecodeProvider.CONTENT_URI, mDecodeCategory),
          String.valueOf(id));
      mItemIntent.putExtra(MappingHelper.CATEGORY_KEY_IDENTIFIER, mDecodeCategory);
      mItemIntent.setData(itemUri);
      startActivity(mItemIntent);
    });

    mCursor.close();
  }
}