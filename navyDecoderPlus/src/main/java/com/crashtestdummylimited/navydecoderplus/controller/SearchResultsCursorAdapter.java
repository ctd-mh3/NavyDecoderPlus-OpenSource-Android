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
import android.database.Cursor;

import androidx.cursoradapter.widget.CursorAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crashtestdummylimited.navydecoderplus.R;
import com.crashtestdummylimited.navydecoderplus.model.db.DecodeDatabase;

class SearchResultsCursorAdapter extends CursorAdapter {
  public SearchResultsCursorAdapter(Context context, Cursor cursor) {
    super(context, cursor, 0);
  }

  // The newView method is used to inflate a new view and return it,
  // you don't bind any data to the view at this point.
  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    return LayoutInflater.from(context).inflate(R.layout.search_result_list_item, parent, false);
  }

  // The bindView method is used to bind all data to a given view
  // such as setting the text on a TextView.
  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    // Find fields to populate in inflated template
    TextView itemToDecode = view.findViewById(R.id.searchScreenResultItemToDecodeTextView);
    TextView itemInfo = view.findViewById(R.id.searchScreenResultDecodeInfoTextView);

    // Default to Error Message
    String itemToDecodeString = "Please Restart App";
    String itemInfoString = "Please Restart App";

    // 15JAN2023: Added to prevent the below crash which was expected to be caused by users moving
    //            from app and then back to app
    // Exception android.database.StaleDataException: Attempting to access a closed CursorWindow.Most probable cause: cursor is deactivated prior to calling this method.
    if (!cursor.isClosed()) {
      // Extract properties from cursor
      itemToDecodeString = cursor.getString(cursor.getColumnIndexOrThrow(DecodeDatabase.KEY_CODE));
      itemInfoString = cursor.getString(cursor.getColumnIndexOrThrow(DecodeDatabase.KEY_CODE_MEANING));
    }
    // Populate fields with extracted properties
    itemToDecode.setText(itemToDecodeString);
    itemInfo.setText(String.valueOf(itemInfoString));
  }
}