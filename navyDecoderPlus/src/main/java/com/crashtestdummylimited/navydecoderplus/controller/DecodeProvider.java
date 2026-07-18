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

import android.app.SearchManager;
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
  private static final int SEARCH_SUGGEST = 2;
  // Number of items above
  private static final int NUMBER_OF_BASE_TYPES = 3;
  // Special matcher code for the cross-category global search URI ("decodeData/all").
  // Must be outside the per-category range (max ~13 categories × 3 = 39).
  private static final int SEARCH_INFO_ALL = 1000;
  //    private static final int REFRESH_SHORTCUT = 1000;
  private static UriMatcher sURIMatcher;

  private static ArrayList<String> sOffsetMatcher;

  /** Builds up a UriMatcher for search suggestion and shortcut refresh queries. */
  private static UriMatcher buildUriMatcher() {
    ArrayList<String> offsetMatcherTemp = new ArrayList<>();

    UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Global cross-category search — must be registered before per-category entries so "all"
    // is not accidentally matched as a category path segment.
    matcher.addURI(AUTHORITY, "decodeData/all", SEARCH_INFO_ALL);

    MappingHelper mappingHelper = MappingHelper.getInstance();
    if (mappingHelper != null) {
      ArrayList<String> categoryIds = mappingHelper.getAllCategoryIdentifies();
      Iterator<String> iterator = categoryIds.iterator();

      int i = 0;
      while (iterator.hasNext()) {
        String categoryIdentifier = iterator.next();
        offsetMatcherTemp.add(i, categoryIdentifier);
        // to get decoded values...
        matcher.addURI(
            AUTHORITY, "decodeData/" + categoryIdentifier, SEARCH_INFO + NUMBER_OF_BASE_TYPES * i);
        matcher.addURI(
            AUTHORITY,
            "decodeData/" + categoryIdentifier + "/#",
            GET_DECODED_INFO + NUMBER_OF_BASE_TYPES * i);
        // to get suggestions...
        matcher.addURI(
            AUTHORITY,
            categoryIdentifier + "/" + SearchManager.SUGGEST_URI_PATH_QUERY,
            SEARCH_SUGGEST + NUMBER_OF_BASE_TYPES * i);
        matcher.addURI(
            AUTHORITY,
            categoryIdentifier + "/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*",
            SEARCH_SUGGEST + NUMBER_OF_BASE_TYPES * i);
        i++;
      }
    }

    /* The following are unused in this implementation, but if we include
    * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our suggestions table, we
    * could expect to receive refresh queries when a shortcutted suggestion is displayed in
    * Quick Search Box, in which case, the following Uris would be provided, and we
    * would return a cursor with a single item representing the refreshed suggestion data.
       matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT);
       matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", REFRESH_SHORTCUT);
    */
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
   * Handles all the decoder searches and suggestion queries from the Search Manager. When
   * requesting a specific item, the uri alone is required. When searching all of the decoder for
   * matches, the selectionArgs argument must carry the search query as the first element. All other
   * arguments are ignored.
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
      return mDecodeDatabase.getAllDecodeMatches(selectionArgs[0].toLowerCase());
    }

    int categoryIndex = matchCode / NUMBER_OF_BASE_TYPES;
    int matchType = matchCode - categoryIndex * NUMBER_OF_BASE_TYPES;

    if (sOffsetMatcher.isEmpty()) {
      return null;
    }

    String categoryIdentifier = sOffsetMatcher.get(categoryIndex);

    // Use the UriMatcher to see what kind of query we have and format the db query accordingly
    switch (matchType) {
      case SEARCH_SUGGEST:
        if (selectionArgs == null) {
          throw new IllegalArgumentException("selectionArgs must be provided for the Uri: " + uri);
        }
        return getSuggestions(categoryIdentifier, selectionArgs[0]);
      case SEARCH_INFO:
        if (selectionArgs == null) {
          throw new IllegalArgumentException("selectionArgs must be provided for the Uri: " + uri);
        }
        return search(categoryIdentifier, selectionArgs[0]);
      case GET_DECODED_INFO:
        return getDecodedInformation(categoryIdentifier, uri);
      default:
        throw new IllegalArgumentException("Unknown Uri: " + uri);
    }
  }

  private Cursor getSuggestions(String categoryIdentifier, String query) {
    query = query.toLowerCase();
    String[] columns =
        new String[] {
          BaseColumns._ID,
          DecodeDatabase.KEY_CODE,
          DecodeDatabase.KEY_CODE_MEANING,
          /* SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
          (only if you want to refresh shortcuts) */
          SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
        };

    return mDecodeDatabase.getDecodeMatches(categoryIdentifier, query, columns);
  }

  private Cursor search(String categoryIdentifier, String query) {
    query = query.toLowerCase();
    String[] columns =
        new String[] {BaseColumns._ID, DecodeDatabase.KEY_CODE, DecodeDatabase.KEY_CODE_MEANING};

    return mDecodeDatabase.getDecodeMatches(categoryIdentifier, query, columns);
  }

  private Cursor getDecodedInformation(String categoryIdentifier, Uri uri) {
    String rowId = uri.getLastPathSegment();
    String[] columns =
        new String[] {
          DecodeDatabase.KEY_CODE, DecodeDatabase.KEY_CODE_MEANING, DecodeDatabase.KEY_CODE_SOURCE
        };

    return mDecodeDatabase.getItemToDecode(categoryIdentifier, rowId, columns);
  }

  /*
      private Cursor refreshShortcut(Uri uri) {
        /* This won't be called with the current implementation, but if we include
         * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our suggestions table, we
         * could expect to receive refresh queries when a shortcutted suggestion is displayed in
         * Quick Search Box. In which case, this method will query the table for the specific
         * word, using the given item Uri and provide all the columns originally provided with the
         * suggestion query.
         *

        String rowId = uri.getLastPathSegment();
        String[] columns = new String[] {
            BaseColumns._ID,
            DecodeDatabase.KEY_CODE,
            DecodeDatabase.KEY_CODE_MEANING,
            SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID};

        return mDecodeDatabase.getItemToDecode("Enlisted Rating Codes", rowId, columns);
      }
  */

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
      case SEARCH_SUGGEST:
        return SearchManager.SUGGEST_MIME_TYPE;
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
