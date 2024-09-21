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

import java.util.ArrayList;
import java.util.HashMap;

import com.crashtestdummylimited.navydecoderplus.R;

import android.content.Context;

public final class MappingHelper {

  public static final String CATEGORY_KEY_IDENTIFIER = "DECODE_CATEGORY_KEY";

  private final HashMap<String, String> SELECTION_TO_CATEGORY_KEY_IDENTIFIER_MAP;
  private final HashMap<String, String> CATEGORY_KEY_IDENTIFIER_TO_SELECTION_MAP;

  private static MappingHelper mInstance = null;

  // Singleton
  private MappingHelper() {
    // Exists only to defeat instantiation.
    SELECTION_TO_CATEGORY_KEY_IDENTIFIER_MAP = null;
    CATEGORY_KEY_IDENTIFIER_TO_SELECTION_MAP = null;
  }

  private MappingHelper(Context context) {
    SELECTION_TO_CATEGORY_KEY_IDENTIFIER_MAP = buildMap(context);
    CATEGORY_KEY_IDENTIFIER_TO_SELECTION_MAP = buildReverseMap(context);
  }

  public static MappingHelper getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new MappingHelper(context);
    }
    return mInstance;
  }

  // TODO- is this needed for non-activity classes? if so then this is not a great way to do this
  public static MappingHelper getInstance() {
    return mInstance;
  }

  private HashMap<String, String> buildMap(Context context) {
    HashMap<String, String> mMap = new HashMap<>();
    mMap.put(context.getString(R.string.categoryAqdCodes), "aqdcodes");
    mMap.put(context.getString(R.string.categoryEnlistedRatingCodes), "enlistedratingcodes");
    mMap.put(context.getString(R.string.categoryImsCodes), "imscodes");
    mMap.put(context.getString(R.string.categoryMasCodes), "mascodes");
    mMap.put(context.getString(R.string.categoryNecCodes), "neccodes");
    mMap.put(context.getString(R.string.categoryNavyReserveActivitiesCodes), "nracodes");
    mMap.put(context.getString(R.string.categoryNobcCodes), "nobccodes");
    mMap.put(context.getString(R.string.categoryOfficerBilletCodes), "officerbilletcodes");
    mMap.put(context.getString(R.string.categoryOfficerDesignatorCodes), "officerdesignatorcodes");
    mMap.put(context.getString(R.string.categoryOfficerPaygradeCodes), "officerpaygradecodes");
    mMap.put(context.getString(R.string.categoryRbscBilletCodes), "rbscbilletcodes");
    mMap.put(context.getString(R.string.categoryReserveUnitIdentificationCodes), "ruicodes");
    mMap.put(context.getString(R.string.categorySubspecialityCodes), "sspcodes");
    mMap.put(context.getString(R.string.categoryReserveProgramCodes), "rpcodes");

    return mMap;
  }

  private HashMap<String, String> buildReverseMap(Context context) {
    HashMap<String, String> mMap = new HashMap<>();
    mMap.put("aqdcodes", context.getString(R.string.categoryAqdCodes));
    mMap.put("enlistedratingcodes", context.getString(R.string.categoryEnlistedRatingCodes));
    mMap.put("imscodes", context.getString(R.string.categoryImsCodes));
    mMap.put("mascodes", context.getString(R.string.categoryMasCodes));
    mMap.put("neccodes", context.getString(R.string.categoryNecCodes));
    mMap.put("nracodes", context.getString(R.string.categoryNavyReserveActivitiesCodes));
    mMap.put("nobccodes", context.getString(R.string.categoryNobcCodes));
    mMap.put("officerbilletcodes", context.getString(R.string.categoryOfficerBilletCodes));
    mMap.put("officerdesignatorcodes", context.getString(R.string.categoryOfficerDesignatorCodes));
    mMap.put("officerpaygradecodes", context.getString(R.string.categoryOfficerPaygradeCodes));
    mMap.put("rbscbilletcodes", context.getString(R.string.categoryRbscBilletCodes));
    mMap.put("ruicodes", context.getString(R.string.categoryReserveUnitIdentificationCodes));
    mMap.put("sspcodes", context.getString(R.string.categorySubspecialityCodes));
    mMap.put("rpcodes", context.getString(R.string.categoryReserveProgramCodes));

    return mMap;
  }

  public String getCategoryIdentify(String key) {

    String mReturnValue;

    mReturnValue = SELECTION_TO_CATEGORY_KEY_IDENTIFIER_MAP.getOrDefault(key, "");

    return mReturnValue;
  }

  public String getSelectionText(String key) {

    String mReturnValue;

    mReturnValue = CATEGORY_KEY_IDENTIFIER_TO_SELECTION_MAP.getOrDefault(key, "");

    return mReturnValue;
  }

  public ArrayList<String> getAllCategoryIdentifies() {
    return new ArrayList<>(SELECTION_TO_CATEGORY_KEY_IDENTIFIER_MAP.values());
  }
}