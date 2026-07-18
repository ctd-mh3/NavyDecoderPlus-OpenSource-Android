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

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.crashtestdummylimited.navydecoderplus.R;
import com.crashtestdummylimited.navydecoderplus.databinding.SearchScreenBinding;
import com.crashtestdummylimited.navydecoderplus.model.db.DecodeDatabase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Displays search results for a specific decode category using an M3 outlined search field. */
public class SearchableDecoderActivity extends AppCompatActivity {

  /**
   * Category key value that triggers a cross-category search across all FTS tables. Pass this as
   * the {@link MappingHelper#CATEGORY_KEY_IDENTIFIER} Intent extra to search everything.
   */
  public static final String ALL_CATEGORIES_KEY = "all";

  private SearchScreenBinding mBinding;

  private String mDecodeCategory = "";

  private SearchResultsAdapter mAdapter;

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

    mBinding.searchScreenListView.setLayoutManager(new LinearLayoutManager(this));
    mAdapter = new SearchResultsAdapter(this::onResultClicked);
    mBinding.searchScreenListView.setAdapter(mAdapter);

    mBinding.searchEditText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            String newText = s.toString().trim();
            if (newText.isEmpty()) {
              mAdapter.submitList(Collections.emptyList());
              mBinding.searchScreenEmptyView.setText(R.string.search_prompt);
              mBinding.searchScreenEmptyView.setVisibility(View.VISIBLE);
            } else {
              showResults(newText);
            }
          }
        });
    mBinding.searchEditText.setOnEditorActionListener(
        (v, actionId, event) -> {
          if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            Editable text = mBinding.searchEditText.getText();
            showResults(text != null ? text.toString().trim() : "");
            return true;
          }
          return false;
        });

    // Focus the field and show the soft keyboard immediately.
    mBinding.searchEditText.requestFocus();
    mBinding.searchEditText.post(
        () ->
            new WindowInsetsControllerCompat(getWindow(), mBinding.searchEditText)
                .show(WindowInsetsCompat.Type.ime()));
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

    List<SearchResultItem> items = new ArrayList<>();
    // The cursor is read into a plain list and closed immediately rather than kept open across
    // query swaps, avoiding the StaleDataException risk a live, swapped-out Cursor used to carry.
    try (Cursor cursor =
        getContentResolver().query(uriWithPath, null, null, new String[] {query}, null)) {
      if (cursor != null) {
        int idIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID);
        int codeIndex = cursor.getColumnIndexOrThrow(DecodeDatabase.KEY_CODE);
        int meaningIndex = cursor.getColumnIndexOrThrow(DecodeDatabase.KEY_CODE_MEANING);
        // category_key column is only present in global (all-categories) search results.
        int categoryColIndex = cursor.getColumnIndex(DecodeDatabase.KEY_CATEGORY_KEY);
        MappingHelper mappingHelper = MappingHelper.getInstance();

        while (cursor.moveToNext()) {
          String categoryKey =
              categoryColIndex >= 0 ? cursor.getString(categoryColIndex) : mDecodeCategory;
          String categoryLabel =
              categoryColIndex >= 0 && mappingHelper != null
                  ? mappingHelper.getSelectionText(categoryKey)
                  : "";
          items.add(
              new SearchResultItem(
                  cursor.getLong(idIndex),
                  categoryKey,
                  cursor.getString(codeIndex),
                  cursor.getString(meaningIndex),
                  categoryLabel));
        }
      }
    }

    mAdapter.submitList(items);
    mBinding.searchScreenEmptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
  }

  private void onResultClicked(SearchResultItem item) {
    Intent itemIntent = new Intent(getApplicationContext(), SelectedItemActivity.class);
    Uri itemUri =
        Uri.withAppendedPath(
            Uri.withAppendedPath(DecodeProvider.CONTENT_URI, item.categoryKey),
            String.valueOf(item.id));
    itemIntent.putExtra(MappingHelper.CATEGORY_KEY_IDENTIFIER, item.categoryKey);
    itemIntent.setData(itemUri);
    startActivity(itemIntent);
  }
}
