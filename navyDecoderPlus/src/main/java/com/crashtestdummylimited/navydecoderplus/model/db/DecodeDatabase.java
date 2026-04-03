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
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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

  private static final String DB_NAME = "navyDecoderDatabase.db";
  private static final String DB_NAME_IN_APK = "navyDecoderDatabase.sqlite3";

  private static final String FTS_AQD_CODES_VIRTUAL_TABLE = "FTS_aqd_codes";
  private static final String FTS_ENLISTED_RATING_CODES_VIRTUAL_TABLE = "FTS_enlisted_rating_codes";
  private static final String FTS_IMS_CODES_VIRTUAL_TABLE = "FTS_ims_codes";
  private static final String FTS_MAS_CODES_VIRTUAL_TABLE = "FTS_mas_codes";
  private static final String FTS_NEC_CODES_VIRTUAL_TABLE = "FTS_nec_codes";
  private static final String FTS_NRA_CODES_VIRTUAL_TABLE = "FTS_nra_codes";
  private static final String FTS_RUI_CODES_VIRTUAL_TABLE = "FTS_rui_codes";
  private static final String FTS_NOBC_CODES_VIRTUAL_TABLE = "FTS_nobc_codes";
  private static final String FTS_OFFICER_BILLET_CODES_VIRTUAL_TABLE = "FTS_officer_billet_codes";
  private static final String FTS_OFFICER_DESIGNATOR_CODES_VIRTUAL_TABLE =
      "FTS_officer_designator_codes";
  private static final String FTS_OFFICER_PAYGRADE_CODES_VIRTUAL_TABLE =
      "FTS_officer_paygrade_codes";
  private static final String FTS_RBSC_BILLET_CODES_VIRTUAL_TABLE = "FTS_rbsc_billet_codes";
  private static final String FTS_SSP_CODES_VIRTUAL_TABLE = "FTS_ssp_codes";
  private static final String FTS_RP_CODES_VIRTUAL_TABLE = "FTS_rp_codes";

  private static final HashMap<String, String> COLUMN_MAP = buildColumnMap();
  private static final HashMap<String, String> SELECTION_TO_TABLE_MAP = buildSelectionToTableMap();

  private static String mDataBaseFullPathWithFileName;

  private final DecoderOpenHelper mDatabaseOpenHelper;

  /**
   * Constructor
   *
   * @param context The Context within which to work, used to create the DB
   */
  public DecodeDatabase(Context context) {

    mDatabaseOpenHelper = new DecoderOpenHelper(context);

    mDatabaseOpenHelper.createDataBase();

    mDatabaseOpenHelper.openDataBase();
  }

  /**
   * Builds a map for all columns that may be requested, which will be given to the
   * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include all
   * columns, even if the value is the key. This allows the ContentProvider to request columns w/o
   * the need to know real column names and create the alias itself.
   */
  private static HashMap<String, String> buildColumnMap() {
    HashMap<String, String> mMap = new HashMap<>();
    mMap.put(KEY_CODE, KEY_CODE);
    mMap.put(KEY_CODE_MEANING, KEY_CODE_MEANING);
    mMap.put(KEY_CODE_SOURCE, KEY_CODE_SOURCE);
    mMap.put(BaseColumns._ID, "rowid AS " + BaseColumns._ID);
    mMap.put(
        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
        "rowid AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
    mMap.put(
        SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
        "rowid AS " + SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
    return mMap;
  }

  private static HashMap<String, String> buildSelectionToTableMap() {
    HashMap<String, String> mMap = new HashMap<>();

    mMap.put("aqdcodes", FTS_AQD_CODES_VIRTUAL_TABLE);
    mMap.put("enlistedratingcodes", FTS_ENLISTED_RATING_CODES_VIRTUAL_TABLE);
    mMap.put("imscodes", FTS_IMS_CODES_VIRTUAL_TABLE);
    mMap.put("mascodes", FTS_MAS_CODES_VIRTUAL_TABLE);
    mMap.put("neccodes", FTS_NEC_CODES_VIRTUAL_TABLE);
    mMap.put("nracodes", FTS_NRA_CODES_VIRTUAL_TABLE);
    mMap.put("ruicodes", FTS_RUI_CODES_VIRTUAL_TABLE);
    mMap.put("nobccodes", FTS_NOBC_CODES_VIRTUAL_TABLE);
    mMap.put("officerbilletcodes", FTS_OFFICER_BILLET_CODES_VIRTUAL_TABLE);
    mMap.put("officerdesignatorcodes", FTS_OFFICER_DESIGNATOR_CODES_VIRTUAL_TABLE);
    mMap.put("officerpaygradecodes", FTS_OFFICER_PAYGRADE_CODES_VIRTUAL_TABLE);
    mMap.put("rbscbilletcodes", FTS_RBSC_BILLET_CODES_VIRTUAL_TABLE);
    mMap.put("sspcodes", FTS_SSP_CODES_VIRTUAL_TABLE);
    mMap.put("rpcodes", FTS_RP_CODES_VIRTUAL_TABLE);

    return mMap;
  }

  /**
   * Returns a Cursor positioned at the row specified by rowId
   *
   * @param rowId id of decode data to retrieve
   * @param columns The columns to include, if null then all are included
   * @return Cursor positioned to matching word, or null if not found.
   */
  public Cursor getItemToDecode(String decodeCategoryKey, String rowId, String[] columns) {

    //    	String mTableToQuery = getTableNameBasedOnDecodeCategory(decodeCategoryKey);
    String mTableToQuery = SELECTION_TO_TABLE_MAP.get(decodeCategoryKey);
    String mSelection = "rowid = ?";
    String[] mSelectionArgs = new String[] {rowId};

    return query(mTableToQuery, mSelection, mSelectionArgs, columns);

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

    //   	String mTableToQuery = getTableNameBasedOnDecodeCategory(decodeCategoryKey);
    String mTableToQuery = SELECTION_TO_TABLE_MAP.get(decodeCategoryKey);

    //  Below code will only search the code column and not the entire table
    //  String selection = KEY_CODE + " MATCH ?";
    String mSelection = mTableToQuery + " MATCH ?";
    String[] mSelectionArgs = new String[] {query + "*"};

    return query(mTableToQuery, mSelection, mSelectionArgs, columns);
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
    /* The SQLiteBuilder provides a map for all possible columns requested to
     * actual columns in the database, creating a simple column alias mechanism
     * by which the ContentProvider does not need to know the real column names
     */
    SQLiteQueryBuilder mBuilder = new SQLiteQueryBuilder();
    mBuilder.setTables(tableToQuery);
    mBuilder.setProjectionMap(COLUMN_MAP);

    Cursor mCursor =
        mBuilder.query(
            mDatabaseOpenHelper.getReadableDatabase(),
            columns,
            selection,
            selectionArgs,
            null,
            null,
            null);

    if (mCursor == null) {
      return null;
    } else if (!mCursor.moveToFirst()) {
      mCursor.close();
      return null;
    }

    return mCursor;
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
    private static final int DB_VERSION = 27;

    private final Context mContext;
    private SQLiteDatabase mDatabase;

    DecoderOpenHelper(final Context context) {
      super(context, DB_NAME, null, DB_VERSION);

      Log.d(TAG, "DecoderOpenHelper.DataBaseHelper constructor");

      mContext = context;

      // Do this dynamically w/o hard coded package name.  Allows for this file to be used by
      //    free and paid version of the app.
      mDataBaseFullPathWithFileName = String.valueOf(this.mContext.getDatabasePath(DB_NAME));
      // 20140101: Using the below code was preventing a database upgrade
      // mDataBaseFullPathWithFileName = this.mContext.getApplicationInfo().dataDir + "/" +DB_NAME;
    }

    /** Creates a empty database on the system and rewrites it with your own database. */
    void createDataBase() {

      Log.d(TAG, "DecoderOpenHelper.createDataBase");

      boolean dbExist = checkDataBase();

      // Added this to attempt to resolve
      //   "android.database.sqlite.SQLiteException: no such table:" error.
      //   Per:
      // http://www.anddev.org/networking-database-problems-f29/missing-table-in-sqlite-with-specific-version-of-desire-hd-t50364.html
      SQLiteDatabase db_Read;

      if (dbExist) {
        Log.d(TAG, "DecoderOpenHelper.createDataBase db exists");
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
        SQLiteDatabase db_Read2 =
            SQLiteDatabase.openDatabase(
                mDataBaseFullPathWithFileName, null, SQLiteDatabase.OPEN_READONLY);
        int versionOfActiveDatabase = db_Read2.getVersion();
        Log.d(TAG, "In createDataBase(), database version is " + versionOfActiveDatabase);
        db_Read2.close();

        if (DB_VERSION > versionOfActiveDatabase) {
          // Force call to upgrade the database
          // SQLiteDatabase parameter is not used so passing in referenced to closed db not an issue
          onUpgrade(db_Read2, versionOfActiveDatabase, DB_VERSION);
        }
      }

      // Check to see if database still exists since on an upgrade the above code might deleted the
      // DB
      dbExist = checkDataBase();
      //noinspection UnusedAssignment
      db_Read = null;

      if (!dbExist) {
        Log.d(TAG, "DecoderOpenHelper.createDataBase finally creating database");

        // By calling this method an empty database will be created into the default system path
        // of your application so we are going to be able to overwrite that database with our
        // database.
        // Change to attempt to fix "no such table" error
        db_Read = this.getReadableDatabase();
        db_Read.close();

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
        // mDataBaseFullPathWithFileName is already set via getDatabasePath() in the constructor.
        mCheckDB =
            SQLiteDatabase.openDatabase(
                mDataBaseFullPathWithFileName, null, SQLiteDatabase.OPEN_READONLY);
        Log.d(TAG, "In checkDataBase(), database version is " + mCheckDB.getVersion());

      } catch (SQLiteException e) {
        Log.d(TAG, "DecoderOpenHelper.checkDataBase database does not exit");
        // database does't exist yet.
      }

      if (mCheckDB != null) {
        Log.d(TAG, "DecoderOpenHelper.checkDataBase closing database");
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

      // Open your local db as the input stream
      InputStream mInput = mContext.getAssets().open(DB_NAME_IN_APK);

      // Open the empty db as the output stream
      OutputStream mOutput = Files.newOutputStream(Paths.get(mDataBaseFullPathWithFileName));

      // transfer bytes from the inputfile to the outputfile
      byte[] mBuffer = new byte[1024];
      int mLength;

      while ((mLength = mInput.read(mBuffer)) > 0) {
        mOutput.write(mBuffer, 0, mLength);
      }

      // The below code is attempting to force the system to record the db version number
      SQLiteDatabase checkDB; // get a reference to the db..

      try {
        checkDB =
            SQLiteDatabase.openDatabase(
                mDataBaseFullPathWithFileName, null, SQLiteDatabase.OPEN_READWRITE);

        // once the db has been copied, set the new version..
        checkDB.setVersion(DB_VERSION);
        checkDB.close();
      } catch (SQLiteException e) {
        // database does not exist yet.
      }

      // Close the streams
      mOutput.flush();
      mOutput.close();
      mInput.close();
    }

    void openDataBase() throws SQLException {
      Log.d(TAG, "DecoderOpenHelper.openDataBase");
      mDatabase =
          SQLiteDatabase.openDatabase(
              mDataBaseFullPathWithFileName, null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public synchronized void close() {

      if (mDatabase != null) {
        mDatabase.close();
      }

      super.close();
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
    Ok, before you run into bigger problems you should know that SQLite is limited on the ALTER TABLE command, it allows add and rename only no remove/drop which is done with recreation of the table.

    You should always have the new table creation query at hand, and use that for upgrade and transfer any existing data. Note: that the onUpgrade methods runs one for your sqlite helper object and you need to handle all the tables in it.

    So what is recommended onUpgrade:

        beginTransaction
        run a table creation with if not exists (we are doing an upgrade, so the table might not exists yet, it will fail alter and drop)
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

      Log.v(TAG, "Attempting to Upgrade database.");

      if (newVersion > oldVersion) {
        Log.v(
            TAG,
            "Upgrading database from version "
                + oldVersion
                + " to "
                + newVersion
                + ", which will destroy all old data");
        if (mContext.deleteDatabase(DB_NAME)) {
          Log.v(TAG, "Deleted old database");
        } else {
          Log.v(TAG, "Unable to deleted old database!");
        }
      }
    }
  }
}
