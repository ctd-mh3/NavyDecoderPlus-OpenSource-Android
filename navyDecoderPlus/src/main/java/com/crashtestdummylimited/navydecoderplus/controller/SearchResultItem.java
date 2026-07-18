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

/**
 * Row data for a single search result, snapshotted from a {@link android.database.Cursor}
 * immediately after querying so the cursor itself doesn't need to stay open across the
 * RecyclerView's lifetime.
 */
class SearchResultItem {

  final long id;

  /**
   * The FTS table/category key this row came from. For a single-category search this is the
   * screen's own category (the cursor doesn't carry a category column); for the global "search all"
   * query it comes from the row itself, since results span multiple tables.
   */
  final String categoryKey;

  final String code;
  final String meaning;

  /** Display label for {@link #categoryKey}; empty when not shown (single-category search). */
  final String categoryLabel;

  SearchResultItem(long id, String categoryKey, String code, String meaning, String categoryLabel) {
    this.id = id;
    this.categoryKey = categoryKey;
    this.code = code;
    this.meaning = meaning;
    this.categoryLabel = categoryLabel;
  }
}
