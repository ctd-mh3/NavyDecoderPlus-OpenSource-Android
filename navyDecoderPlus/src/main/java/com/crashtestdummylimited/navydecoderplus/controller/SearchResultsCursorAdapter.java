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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cursoradapter.widget.CursorAdapter;
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

    // Extract properties from cursor
    String itemToDecodeString =
        cursor.getString(cursor.getColumnIndexOrThrow(DecodeDatabase.KEY_CODE));
    String itemInfoString =
        cursor.getString(cursor.getColumnIndexOrThrow(DecodeDatabase.KEY_CODE_MEANING));

    // Populate fields with extracted properties
    itemToDecode.setText(itemToDecodeString);
    itemInfo.setText(itemInfoString);
  }
}
