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
package com.crashtestdummylimited.navydecoderplus.model.db;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import androidx.annotation.NonNull;
import com.crashtestdummylimited.navydecoderplus.controller.Category;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Contains logic to return specific words from the dictionary, and load the dictionary table when
 * it needs to be created.
 */
public class DecodeDatabase {

  // Room DAOs are not used here. The database is a read-only pre-packaged asset, all 14 tables
  // are FTS3 virtual tables (Room only supports FTS4/FTS5), and the ContentProvider + Android
  // Search framework requires Cursor-returning queries — none of which fit Room's model.
  private static final String TAG = "DecodeDatabase";

  // The columns we'll include in the decode table
  public static final String KEY_CODE = SearchManager.SUGGEST_COLUMN_TEXT_1;
  public static final String KEY_CODE_MEANING = SearchManager.SUGGEST_COLUMN_TEXT_2;
  public static final String KEY_CODE_SOURCE = "source";
  // Present only in global (all-categories) search results; holds the category key for each row.
  public static final String KEY_CATEGORY_KEY = "category_key";

  private static final String DB_NAME = "navyDecoderDatabase.db";
  private static final String DB_NAME_IN_APK = "navyDecoderDatabase.sqlite3";

  private static final HashMap<String, String> COLUMN_MAP = buildColumnMap();

  // Cap search results to prevent loading an unbounded result set into memory. Navy codes are
  // specific enough that any match set larger than this is too broad to be useful.
  private static final int SEARCH_RESULT_LIMIT = 50;

  private static String sDatabaseFullPath;

  private final DecoderOpenHelper mDatabaseOpenHelper;

  /**
   * Constructor
   *
   * @param context The Context within which to work, used to create the DB
   */
  public DecodeDatabase(Context context) {

    mDatabaseOpenHelper = new DecoderOpenHelper(context);

    mDatabaseOpenHelper.createDataBase();
  }

  /**
   * Builds a map for all columns that may be requested, which will be given to the
   * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include all
   * columns, even if the value is the key. This allows the ContentProvider to request columns w/o
   * the need to know real column names and create the alias itself.
   */
  private static HashMap<String, String> buildColumnMap() {
    HashMap<String, String> map = new HashMap<>();
    map.put(KEY_CODE, KEY_CODE);
    map.put(KEY_CODE_MEANING, KEY_CODE_MEANING);
    map.put(KEY_CODE_SOURCE, KEY_CODE_SOURCE);
    map.put(BaseColumns._ID, "rowid AS " + BaseColumns._ID);
    map.put(
        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
        "rowid AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
    map.put(
        SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
        "rowid AS " + SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
    return map;
  }

  /**
   * Returns a Cursor positioned at the row specified by rowId
   *
   * @param rowId id of decode data to retrieve
   * @param columns The columns to include, if null then all are included
   * @return Cursor positioned to matching word, or null if not found.
   */
  public Cursor getItemToDecode(String decodeCategoryKey, String rowId, String[] columns) {

    Category category = Category.fromKey(decodeCategoryKey);
    String tableToQuery = category != null ? category.ftsTable : null;
    String selection = "rowid = ?";
    String[] selectionArgs = new String[] {rowId};

    return query(tableToQuery, selection, selectionArgs, columns);

    /* This builds a query that looks like:
     *     SELECT <columns> FROM <table> WHERE rowid = <rowId>
     */
  }

  /**
   * Returns a Cursor over all decode items that match the given query
   *
   * @param query The string to search for
   * @param columns The columns to include, if null then all are included
   * @return Cursor over all words that match, or null if none found.
   */
  public Cursor getDecodeMatches(String decodeCategoryKey, String query, String[] columns) {

    Category category = Category.fromKey(decodeCategoryKey);
    String tableToQuery = category != null ? category.ftsTable : null;

    // FTS5 treats several characters as query operators (- is NOT, " starts a phrase, etc.).
    // Replace any non-alphanumeric, non-space character with a space so that a search like
    // "E-6" becomes "e 6*" — FTS5 implicit AND — rather than "e NOT 6*".
    String sanitizedQuery = query.replaceAll("[^a-zA-Z0-9 ]", " ").trim().replaceAll("\\s+", " ");
    if (sanitizedQuery.isEmpty()) {
      return null;
    }

    //  Below code will only search the code column and not the entire table
    //  String selection = KEY_CODE + " MATCH ?";
    String selection = tableToQuery + " MATCH ?";
    String[] selectionArgs = new String[] {sanitizedQuery + "*"};

    return query(tableToQuery, selection, selectionArgs, columns);
  }

  /**
   * Returns a Cursor over decode items matching the given query across every searchable category.
   * Each row includes a {@link #KEY_CATEGORY_KEY} column identifying which category it came from.
   *
   * @param query The search query (will be sanitized and prefix-matched internally)
   * @return Cursor with columns _id, suggest_text_1, suggest_text_2, category_key; or null.
   */
  public Cursor getAllDecodeMatches(String query) {
    String sanitizedQuery = query.replaceAll("[^a-zA-Z0-9 ]", " ").trim().replaceAll("\\s+", " ");
    if (sanitizedQuery.isEmpty()) return null;

    String searchTerm = sanitizedQuery + "*";

    StringBuilder sql = new StringBuilder();
    ArrayList<String> args = getStrings(sql, searchTerm);
    sql.append(" LIMIT ").append(SEARCH_RESULT_LIMIT);

    String sqlStr = sql.toString();
    String[] argsArray = args.toArray(new String[0]);

    Cursor cursor;
    try {
      cursor = mDatabaseOpenHelper.getReadableDatabase().rawQuery(sqlStr, argsArray);
    } catch (Exception e) {
      Log.e(TAG, "getAllDecodeMatches failed: " + e.getMessage(), e);
      return null;
    }

    if (cursor.getCount() == 0) {
      cursor.close();
      return null;
    }
    // Cursor is returned at position -1 (before first row), as CursorAdapter expects.
    return cursor;
  }

  @NonNull
  private static ArrayList<String> getStrings(StringBuilder sql, String searchTerm) {
    ArrayList<String> args = new ArrayList<>();
    boolean first = true;

    for (Category c : Category.values()) {
      if (c.ftsTable == null) continue; // skip RFAS (no FTS table)
      if (!first) sql.append(" UNION ALL ");
      sql.append("SELECT rowid AS _id, suggest_text_1, suggest_text_2, '")
          .append(c.key)
          .append("' AS category_key FROM ")
          .append(c.ftsTable)
          .append(" WHERE ")
          .append(c.ftsTable)
          .append(" MATCH ?");
      args.add(searchTerm);
      first = false;
    }
    return args;
  }

  /**
   * Performs a database query.
   *
   * @param selection The selection clause
   * @param selectionArgs Selection arguments for "?" components in the selection
   * @param columns The columns to return
   * @return A Cursor over all rows matching the query
   */
  private Cursor query(
      String tableToQuery, String selection, String[] selectionArgs, String[] columns) {
    // SQLiteQueryBuilder wraps the WHERE clause in parentheses — "WHERE (table MATCH ?)" —
    // which breaks FTS5 on the SQLite versions bundled with Android 8-9 (< SQLite 3.28).
    // Use rawQuery() directly so the MATCH expression is unparenthesized.
    StringBuilder selectClause = new StringBuilder();
    for (int i = 0; i < columns.length; i++) {
      if (i > 0) selectClause.append(", ");
      String mapped = COLUMN_MAP.get(columns[i]);
      selectClause.append(mapped != null ? mapped : columns[i]);
    }

    String sql =
        "SELECT "
            + selectClause
            + " FROM "
            + tableToQuery
            + " WHERE "
            + selection
            + " LIMIT "
            + SEARCH_RESULT_LIMIT;

    Cursor cursor;
    try {
      cursor = mDatabaseOpenHelper.getReadableDatabase().rawQuery(sql, selectionArgs);
    } catch (Exception e) {
      Log.e(TAG, "rawQuery failed: " + e.getMessage(), e);
      return null;
    }

    if (cursor.getCount() == 0) {
      cursor.close();
      return null;
    }

    // Cursor is returned at position -1 (before first row), as CursorAdapter expects.
    return cursor;
  }

  /** This creates/opens the database. */
  private static class DecoderOpenHelper extends SQLiteOpenHelper {

    // Database Versions
    //
    //    1 = Original database
    //    2 = App v1.02 database
    //    3 = App v1.04 database
    //    4 = App v1.06 database (New NRA codes)
    //    5 = App v1.07 database (NAVADMIN 124/13)
    //    6 = App v1.08 database (Updated for MAS (13NOV2013) and RFAS (24JUL2013) codes)
    //    7 = App v1.11 database (Many changes)
    //    8 = App v1.12 database (NAVADMIN 106/16 and 107/16)
    //    9 = App v1.13 database (Updated one RUIC)
    //   10 = App v1.17 database
    //   11 = App v1.19 database (NECs updated)
    //   12 = App v1.20 database (fixed issues)
    //   13 = App v1.21 database (Updated RUICs)
    //   14 = App v1.24 database (Updated RUICs)
    //   Missed updating for v1.25
    //   15 = App v1.26 database (Updated NRAs)
    //   16 = App v1.27 database (Updated RUICs)
    //   17 = App v1.28 database (Updated NRAs)
    //   18 = App v1.29 database (Updated ratings)
    //   19 = App v1.31 database (Updated numerous items)
    //   20 = App v1.32 database (Corrected NOBCs)
    //   21 = App v1.33 database (Updates RUICs and enlisted ratings)
    //   22 = App v1.34 database (Updates RUICs, Added 737X)
    //   23 = App v1.36 database (Updates RUICs & NRAs, Disestablished of 6810 designator (NAVADMIN
    // 128/22))
    //   24 = App v1.37 database (Updates per NAVPERS documents)
    //   25 = App v1.38 database (Corrected SSP for 1950)
    //   26 = App v1.40 database (Added AQD. Updated designators and NOBCs.)
    //   27 = App v1.41 database (Updated MAS and IMS codes.)
    //   28 = App v1.45 database (Updated NOBCs, Officer Billets, Officer Designators, and SSPs)
    //   29 = App v1.46 database (Updated Enlisted rating codes, AQDs, SSPs, and NECs)
    private static final int DB_VERSION = 29;

    private final Context mContext;

    DecoderOpenHelper(final Context context) {
      super(context, DB_NAME, null, DB_VERSION);

      mContext = context;

      // Do this dynamically w/o hard coded package name.  Allows for this file to be used by
      //    free and paid version of the app.
      sDatabaseFullPath = String.valueOf(this.mContext.getDatabasePath(DB_NAME));
      // 20140101: Using the below code was preventing a database upgrade
      // sDatabaseFullPath = this.mContext.getApplicationInfo().dataDir + "/" +DB_NAME;
    }

    /** Creates an empty database on the system and rewrites it with your own database. */
    void createDataBase() {

      boolean dbExist = checkDataBase();

      // Added this to attempt to resolve
      //   "android.database.sqlite.SQLiteException: no such table:" error.
      //   Per:
      // http://www.anddev.org/networking-database-problems-f29/missing-table-in-sqlite-with-specific-version-of-desire-hd-t50364.html
      SQLiteDatabase dbRead;

      if (dbExist) {
        // Need to have the system call onUpgrade if the database in this apk is newer than
        //   the one in the DB_PATH directory.  onUpgrade should be called by the system
        //   if needed by a call to getWritableDatabase().
        // SQLiteDatabase db_Write = this.getWritableDatabase();

        // Debugging showed that on an upgrade to the database in the apk, that the above
        //   call to this.getWritableDatabase() only resulted in a call to onCreate().  No
        //   call to onUpgrade was performed.  In addition, a subsequent call to
        //   getVersion() returned the new DB_VERSION value in the latest apk.
        //
        // The below code manually determines the active database's version.  And if the
        //   latest installed database version is greater, it directly calls onUpgrade
        SQLiteDatabase dbRead2 =
            SQLiteDatabase.openDatabase(sDatabaseFullPath, null, SQLiteDatabase.OPEN_READONLY);
        int versionOfActiveDatabase = dbRead2.getVersion();
        Log.d(TAG, "In createDataBase(), database version is " + versionOfActiveDatabase);
        dbRead2.close();

        if (DB_VERSION > versionOfActiveDatabase) {
          Log.d(
              TAG,
              "createDataBase: upgrading from " + versionOfActiveDatabase + " to " + DB_VERSION);
          // Force call to upgrade the database.
          // onUpgrade does not use the db parameter (it calls mContext.deleteDatabase()),
          // so null is passed rather than a closed handle.
          onUpgrade(null, versionOfActiveDatabase, DB_VERSION);
        } else {
          Log.d(
              TAG,
              "createDataBase: version " + versionOfActiveDatabase + " is current, no copy needed");
        }
      }

      // Check to see if database still exists since on an upgrade the above code might delete the
      // DB
      dbExist = checkDataBase();
      //noinspection UnusedAssignment
      dbRead = null;

      if (!dbExist) {
        // By calling this method an empty database will be created into the default system path
        // of your application so we are going to be able to overwrite that database with our
        // database.
        // Change to attempt to fix "no such table" error
        dbRead = this.getReadableDatabase();
        dbRead.close();

        try {
          copyDataBase();

        } catch (IOException e) {
          throw new Error("Error copying database");
        }
      }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the
     * application.
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {

      SQLiteDatabase mCheckDB = null;

      try {
        // sDatabaseFullPath is already set via getDatabasePath() in the constructor.
        mCheckDB =
            SQLiteDatabase.openDatabase(sDatabaseFullPath, null, SQLiteDatabase.OPEN_READONLY);

      } catch (SQLiteException e) {
        // database doesn't exist yet.
      }

      if (mCheckDB != null) {
        mCheckDB.close();
      }

      return mCheckDB != null;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled. This is done by transferring
     * bytestream.
     */
    private void copyDataBase() throws IOException {

      try (InputStream mInput = mContext.getAssets().open(DB_NAME_IN_APK);
          OutputStream mOutput = Files.newOutputStream(Paths.get(sDatabaseFullPath))) {

        // transfer bytes from the inputfile to the outputfile
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0) {
          mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
      }

      // The below code is attempting to force the system to record the db version number
      try {
        SQLiteDatabase checkDB =
            SQLiteDatabase.openDatabase(sDatabaseFullPath, null, SQLiteDatabase.OPEN_READWRITE);

        // once the db has been copied, set the new version
        checkDB.setVersion(DB_VERSION);
        checkDB.close();
      } catch (SQLiteException e) {
        // database does not exist yet.
      }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      // All work is done in createDataBase() which must be called by clients
    }

    @Override
    /*
      * http://stackoverflow.com/questions/3505900/sqliteopenhelper-onupgrade-confusion-android
      *
      *
    Ok, before you run into bigger problems you should know that SQLite is limited on the ALTER TABLE command, it allows "add" and "rename" only no remove/drop which is done with recreation of the table.

    You should always have the new table creation query at hand, and use that for upgrade and transfer any existing data. Note: that the onUpgrade methods runs one for your sqlite helper object, and you need to handle all the tables in it.

    So what is recommended onUpgrade:

        beginTransaction
        run a table creation with if not exists (we are doing an upgrade, so the table might not exist yet, it will fail alter and drop)
        put in a list the existing columns List<String> columns = DBUtils.GetColumns(db, TableName);
        backup table (ALTER table " + TableName + " RENAME TO 'temp_" + TableName)
        create new table (the newest table creation schema)
        get the intersection with the new columns, this time columns taken from the upgraded table (columns.retainAll(DBUtils.GetColumns(db, TableName));)
        restore data (String cols = StringUtils.join(columns, ","); db.execSQL(String.format( "INSERT INTO %s (%s) SELECT %s from temp_%s", TableName, cols, cols, TableName)); )
        remove backup table (DROP table 'temp_" + TableName)
        setTransactionSuccessful

    (This doesn't handle table downgrade, if you rename a column, you don't get the existing data transfered as the column names do not match).
     */
    public void onUpgrade(SQLiteDatabase db, final int oldVersion, final int newVersion) {
      if (newVersion > oldVersion) {
        mContext.deleteDatabase(DB_NAME);
      }
    }
  }
}
