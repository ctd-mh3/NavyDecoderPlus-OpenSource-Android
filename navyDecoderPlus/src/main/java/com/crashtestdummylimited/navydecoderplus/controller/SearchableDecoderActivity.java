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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

/**
 * The main activity for the decoder.
 * Displays search results triggered by the search dialog and handles
 * actions from search suggestions.
 */
public class SearchableDecoderActivity extends AppCompatActivity {

  private TextView mTextView;
  private ListView mListView;

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
    setTheme(R.style.AppTheme);
    setContentView(R.layout.search_screen);

    mTextView = findViewById(R.id.searchScreenTextView);
    mListView = findViewById(R.id.searchScreenListView);

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

    Cursor mCursor;

    Uri mUriWithPath = Uri.withAppendedPath(DecodeProvider.CONTENT_URI, mDecodeCategory);
    mCursor = getContentResolver().query(mUriWithPath, null, null, new String[]{query}, null);

    if (mCursor == null) {
      // There are no results
      mTextView.setText(getString(R.string.no_results, query));
    } else {

      // Find ListView to populate
      ListView lvItems = findViewById(R.id.searchScreenListView);
      // Setup cursor adapter using cursor from last step
      SearchResultsCursorAdapter searchAdapter = new SearchResultsCursorAdapter(this, mCursor);
      // Attach cursor adapter to the ListView
      lvItems.setAdapter(searchAdapter);

      // Define the on-click listener for the list items
      mListView.setOnItemClickListener((parent, view, position, id) -> {
        // Build the Intent used to open SelectedItemActivity with a specific decodeItem Uri
        Intent mItemIntent = new Intent(getApplicationContext(), SelectedItemActivity.class);

        Uri mUriWithPath1 = Uri.withAppendedPath(DecodeProvider.CONTENT_URI, mDecodeCategory);

        mUriWithPath1 = Uri.withAppendedPath(mUriWithPath1, String.valueOf(id));
        mItemIntent.putExtra(MappingHelper.CATEGORY_KEY_IDENTIFIER, mDecodeCategory);
        mItemIntent.setData(mUriWithPath1);
        startActivity(mItemIntent);
      });
      mCursor.close();
    }
  }
}