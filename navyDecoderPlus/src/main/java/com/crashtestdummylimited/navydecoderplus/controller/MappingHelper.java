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
import java.util.ArrayList;

public final class MappingHelper {

  public static final String CATEGORY_KEY_IDENTIFIER = "DECODE_CATEGORY_KEY";

  private static volatile MappingHelper mInstance = null;

  private final Context mContext;

  private MappingHelper(Context context) {
    mContext = context.getApplicationContext();
  }

  public static MappingHelper getInstance(Context context) {
    if (mInstance == null) {
      synchronized (MappingHelper.class) {
        if (mInstance == null) {
          mInstance = new MappingHelper(context);
        }
      }
    }
    return mInstance;
  }

  // No-arg overload for callers (e.g. DecodeProvider) that don't have a Context at call time.
  // The singleton must already be initialized via getInstance(Context) before this is called.
  public static MappingHelper getInstance() {
    return mInstance;
  }

  /** Returns the display label for a given category key, or "" if not found. */
  public String getSelectionText(String key) {
    Category c = Category.fromKey(key);
    return c != null ? mContext.getString(c.labelRes) : "";
  }

  /** Returns all searchable category keys (excludes RFAS, which has no key). */
  public ArrayList<String> getAllCategoryIdentifiers() {
    ArrayList<String> keys = new ArrayList<>();
    for (Category c : Category.values()) {
      if (c.key != null) keys.add(c.key);
    }
    return keys;
  }
}
