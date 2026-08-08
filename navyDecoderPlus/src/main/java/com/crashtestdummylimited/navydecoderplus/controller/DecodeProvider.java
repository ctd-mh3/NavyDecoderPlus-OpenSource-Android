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

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import androidx.annotation.NonNull;
import com.crashtestdummylimited.navydecoderplus.model.db.DecodeDatabase;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

/** Provides access to the dictionary database. */
public class DecodeProvider extends ContentProvider {

  // AUTHORITY uses mixed case (DecodeProvider) rather than all-lowercase. This cannot be changed
  // without breaking the ContentProvider contract for all existing installed devices.
  private static final String AUTHORITY =
      "com.crashtestdummylimited.navydecoderplus.DecodeProvider";
  // Per-category URIs are registered dynamically in buildUriMatcher() for each category identifier.
  public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/decodeData");

  // MIME types used for searching words or looking up a single definition
  private static final String WORDS_MIME_TYPE =
      ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.crashtestdummylimited.navydecoderplus";
  private static final String DEFINITION_MIME_TYPE =
      ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.crashtestdummylimited.navydecoderplus";

  private DecodeDatabase mDecodeDatabase;

  // UriMatcher stuff
  private static final int SEARCH_INFO = 0;
  private static final int GET_DECODED_INFO = 1;
  // Number of items above
  private static final int NUMBER_OF_BASE_TYPES = 2;
  // Special matcher code for the cross-category global search URI ("decodeData/all"). Derived
  // from Category.values().length rather than a bare literal so it can never collide with the
  // per-category range assigned in buildUriMatcher() (which covers at most Category.values()
  // categories, each using NUMBER_OF_BASE_TYPES matcher codes), even as categories are added.
  private static final int SEARCH_INFO_ALL = Category.values().length * NUMBER_OF_BASE_TYPES;
  private static UriMatcher sURIMatcher;

  private static ArrayList<String> sOffsetMatcher;

  /** Builds up a UriMatcher for per-category search and single-item lookup queries. */
  private static UriMatcher buildUriMatcher() {
    ArrayList<String> offsetMatcherTemp = new ArrayList<>();

    UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Global cross-category search — must be registered before per-category entries so "all"
    // is not accidentally matched as a category path segment.
    matcher.addURI(AUTHORITY, "decodeData/all", SEARCH_INFO_ALL);

    MappingHelper mappingHelper = MappingHelper.getInstance();
    if (mappingHelper != null) {
      ArrayList<String> categoryIds = mappingHelper.getAllCategoryIdentifiers();
      Iterator<String> iterator = categoryIds.iterator();

      int i = 0;
      while (iterator.hasNext()) {
        String categoryKey = iterator.next();
        offsetMatcherTemp.add(i, categoryKey);
        matcher.addURI(
            AUTHORITY, "decodeData/" + categoryKey, SEARCH_INFO + NUMBER_OF_BASE_TYPES * i);
        matcher.addURI(
            AUTHORITY,
            "decodeData/" + categoryKey + "/#",
            GET_DECODED_INFO + NUMBER_OF_BASE_TYPES * i);
        i++;
      }
    }

    sOffsetMatcher = offsetMatcherTemp;
    return matcher;
  }

  @Override
  public boolean onCreate() {
    mDecodeDatabase = new DecodeDatabase(getContext());
    MappingHelper.getInstance(getContext());
    sURIMatcher = buildUriMatcher();
    return true;
  }

  /**
   * Handles all the decoder searches. When requesting a specific item, the uri alone is required.
   * When searching all of the decoder for matches, the selectionArgs argument must carry the search
   * query as the first element. All other arguments are ignored.
   */
  @Override
  public Cursor query(
      @NonNull Uri uri,
      String[] projection,
      String selection,
      String[] selectionArgs,
      String sortOrder) {

    int matchCode = sURIMatcher.match(uri);

    // Match should always find a match, if not stop processing and return null for the cursor
    if (matchCode == -1) {
      return null;
    }

    // Handle global search before the per-category offset arithmetic.
    if (matchCode == SEARCH_INFO_ALL) {
      if (selectionArgs == null) {
        throw new IllegalArgumentException("selectionArgs must be provided for the Uri: " + uri);
      }
      return mDecodeDatabase.getAllDecodeMatches(selectionArgs[0].toLowerCase(Locale.ROOT));
    }

    int categoryIndex = matchCode / NUMBER_OF_BASE_TYPES;
    int matchType = matchCode - categoryIndex * NUMBER_OF_BASE_TYPES;

    if (sOffsetMatcher.isEmpty()) {
      return null;
    }

    String categoryKey = sOffsetMatcher.get(categoryIndex);

    // Use the UriMatcher to see what kind of query we have and format the db query accordingly
    switch (matchType) {
      case SEARCH_INFO:
        if (selectionArgs == null) {
          throw new IllegalArgumentException("selectionArgs must be provided for the Uri: " + uri);
        }
        return search(categoryKey, selectionArgs[0]);
      case GET_DECODED_INFO:
        return getDecodedInformation(categoryKey, uri);
      default:
        throw new IllegalArgumentException("Unknown Uri: " + uri);
    }
  }

  private Cursor search(String categoryKey, String query) {
    query = query.toLowerCase(Locale.ROOT);
    String[] columns =
        new String[] {BaseColumns._ID, DecodeDatabase.KEY_CODE, DecodeDatabase.KEY_CODE_MEANING};

    return mDecodeDatabase.getDecodeMatches(categoryKey, query, columns);
  }

  private Cursor getDecodedInformation(String categoryKey, Uri uri) {
    String rowId = uri.getLastPathSegment();
    String[] columns =
        new String[] {
          DecodeDatabase.KEY_CODE, DecodeDatabase.KEY_CODE_MEANING, DecodeDatabase.KEY_CODE_SOURCE
        };

    return mDecodeDatabase.getItemToDecode(categoryKey, rowId, columns);
  }

  /**
   * This method is required in order to query the supported types. It's also useful in our own
   * query() method to determine the type of Uri received.
   */
  @Override
  public String getType(@NonNull Uri uri) {

    int matchCode = sURIMatcher.match(uri);
    int categoryIndex = matchCode / NUMBER_OF_BASE_TYPES;
    int matchType = matchCode - categoryIndex * NUMBER_OF_BASE_TYPES;

    switch (matchType) {
      case SEARCH_INFO:
        return WORDS_MIME_TYPE;
      case GET_DECODED_INFO:
        return DEFINITION_MIME_TYPE;
      default:
        throw new IllegalArgumentException("Unknown URL " + uri);
    }
  }

  // Other required implementations...

  @Override
  public Uri insert(@NonNull Uri uri, ContentValues values) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int update(
      @NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException();
  }
}
