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
import com.crashtestdummylimited.navydecoderplus.model.db.DecodeDatabase;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Objects;

/**
 * Displays a word and its definition.
 */
public class SelectedItemActivity extends AppCompatActivity {

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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTheme(R.style.AppTheme);
    setContentView(R.layout.final_screen_selected_item);

    Uri mUri = getIntent().getData();
    Cursor mCursor = getContentResolver().query(Objects.requireNonNull(mUri), null, null, null, null);

    if (mCursor == null) {
      finish();
    } else {
      TextView mDecodeCategoryTextView = findViewById(R.id.decodeCategoryTextView);

      Intent mIntent = getIntent();
      String mDecodeCategory = mIntent.getStringExtra(MappingHelper.CATEGORY_KEY_IDENTIFIER);
      MappingHelper mMappingHelper = MappingHelper.getInstance(getApplicationContext());
      // TODO - What happens if no match
      String mSelectionText = mMappingHelper.getSelectionText(mDecodeCategory);
      mDecodeCategoryTextView.setText(mSelectionText);

      TextView mItemToDecodeTextView = findViewById(R.id.itemDecodedTextView);
      TextView mDecodedDescriptionTextView = findViewById(R.id.decodedDescriptionTextView);
      TextView mSourceDescriptionTextView = findViewById(R.id.sourceDescriptionTextView);

      mCursor.moveToFirst();

      // TODO         
      int mCodeIndex = mCursor.getColumnIndexOrThrow(DecodeDatabase.KEY_CODE);
      int mCodeMeaningIndex = mCursor.getColumnIndexOrThrow(DecodeDatabase.KEY_CODE_MEANING);
      int mCodeSourceIndex = mCursor.getColumnIndexOrThrow(DecodeDatabase.KEY_CODE_SOURCE);
      mItemToDecodeTextView.setText(mCursor.getString(mCodeIndex));
      mDecodedDescriptionTextView.setText(mCursor.getString(mCodeMeaningIndex));
      mSourceDescriptionTextView.setText(mCursor.getString(mCodeSourceIndex));

      mCursor.close();
    }
  }
}