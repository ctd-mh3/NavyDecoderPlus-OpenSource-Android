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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.crashtestdummylimited.navydecoderplus.R;
import com.crashtestdummylimited.navydecoderplus.databinding.SearchScreenBinding;
import com.crashtestdummylimited.navydecoderplus.model.db.DecodeDatabase;

/** Displays search results for a specific decode category using an embedded SearchView. */
public class SearchableDecoderActivity extends AppCompatActivity {

  /**
   * Category key value that triggers a cross-category search across all FTS tables. Pass this as
   * the {@link MappingHelper#CATEGORY_KEY_IDENTIFIER} Intent extra to search everything.
   */
  public static final String ALL_CATEGORIES_KEY = "all";

  private SearchScreenBinding mBinding;

  private String mDecodeCategory = "";

  private SearchResultsCursorAdapter mAdapter;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuOptions.onCreateOptionsMenu(this, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    MenuOptions.onOptionsItemSelected(this, item);
    return true;
  }

  // *************************************************************************
  //  End Menu Support Code
  // *************************************************************************

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

    mDecodeCategory = getIntent().getStringExtra(MappingHelper.CATEGORY_KEY_IDENTIFIER);
    if (mDecodeCategory == null) {
      mDecodeCategory = ALL_CATEGORIES_KEY;
    }

    // Set toolbar title: category label for single-category, "Search All" for global.
    if (getSupportActionBar() != null) {
      if (ALL_CATEGORIES_KEY.equals(mDecodeCategory)) {
        getSupportActionBar().setTitle(R.string.categorySearchAll);
      } else {
        Category category = Category.fromKey(mDecodeCategory);
        getSupportActionBar()
            .setTitle(
                category != null ? getString(category.labelRes) : getString(R.string.app_name));
      }
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // Show prompt text until the user starts typing.
    mBinding.searchScreenEmptyView.setText(R.string.search_prompt);
    mBinding.searchScreenEmptyView.setVisibility(View.VISIBLE);
    mBinding.searchScreenListView.setEmptyView(mBinding.searchScreenEmptyView);

    mBinding.searchView.setOnQueryTextListener(
        new SearchView.OnQueryTextListener() {
          @Override
          public boolean onQueryTextSubmit(String query) {
            showResults(query);
            return true;
          }

          @Override
          public boolean onQueryTextChange(String newText) {
            if (newText.trim().isEmpty()) {
              if (mAdapter != null) {
                mAdapter.changeCursor(null);
              }
              mBinding.searchScreenEmptyView.setText(R.string.search_prompt);
            } else {
              showResults(newText.trim());
            }
            return true;
          }
        });

    // Focus the SearchView's inner EditText and show the soft keyboard immediately.
    // setIconified(false) routes focus to the inner EditText; post() defers until after layout.
    mBinding.searchView.setIconified(false);
    mBinding.searchView.post(
        () -> {
          EditText searchEditText =
              mBinding.searchView.findViewById(androidx.appcompat.R.id.search_src_text);
          if (searchEditText != null) {
            searchEditText.requestFocus();
            InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
              imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
            }
          }
        });
  }

  /**
   * Searches the database and displays results for the given query.
   *
   * @param query The search query
   */
  private void showResults(String query) {
    mBinding.searchScreenEmptyView.setText(getString(R.string.no_results, query));

    Uri uriWithPath =
        ALL_CATEGORIES_KEY.equals(mDecodeCategory)
            ? Uri.withAppendedPath(DecodeProvider.CONTENT_URI, "all")
            : Uri.withAppendedPath(DecodeProvider.CONTENT_URI, mDecodeCategory);

    Cursor mCursor =
        getContentResolver().query(uriWithPath, null, null, new String[] {query}, null);

    if (mCursor == null) {
      // Provider error: setEmptyView() only auto-fires once an adapter is attached,
      // so reveal the empty view manually for this edge case.
      mBinding.searchScreenEmptyView.setVisibility(View.VISIBLE);
      return;
    }

    ListView lvItems = mBinding.searchScreenListView;
    mAdapter = new SearchResultsCursorAdapter(this, mCursor);
    lvItems.setAdapter(mAdapter);

    lvItems.setOnItemClickListener(
        (parent, view, position, id) -> {
          String clickedCategory;
          long rowId;

          if (ALL_CATEGORIES_KEY.equals(mDecodeCategory)) {
            // Global search: rowids conflict across tables, so read both fields from the cursor.
            Object item = mAdapter.getItem(position);
            if (!(item instanceof Cursor)) return;
            Cursor c = (Cursor) item;
            int catCol = c.getColumnIndex(DecodeDatabase.KEY_CATEGORY_KEY);
            clickedCategory = catCol >= 0 ? c.getString(catCol) : "";
            rowId = c.getLong(c.getColumnIndexOrThrow(BaseColumns._ID));
          } else {
            clickedCategory = mDecodeCategory;
            rowId = id;
          }

          Intent mItemIntent = new Intent(getApplicationContext(), SelectedItemActivity.class);
          Uri itemUri =
              Uri.withAppendedPath(
                  Uri.withAppendedPath(DecodeProvider.CONTENT_URI, clickedCategory),
                  String.valueOf(rowId));
          mItemIntent.putExtra(MappingHelper.CATEGORY_KEY_IDENTIFIER, clickedCategory);
          mItemIntent.setData(itemUri);
          startActivity(mItemIntent);
        });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mAdapter != null) {
      mAdapter.changeCursor(null);
    }
  }
}
