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
package com.crashtestdummylimited.navydecoderplus.model;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Shared multistep RFAS lookup implementation for {@link RFASEnlistedCodes}/{@link
 * RFASOfficerCodes} — both built the same 3-table-to-HashMap constructor and identical getters;
 * only the embedded data tables and source info differ, so both are now thin subclasses that just
 * supply their own tables.
 */
abstract class AbstractRfasCodes implements RFASReferenceData {

  private final HashMap<String, String> mFirstCharacterCodesHashMap;
  private final HashMap<String, String> mSecondAndThirdCharacterCodesHashMap;
  private final HashMap<String, String> mFourthCharacterCodesHashMap;
  private final String mSourceInfo;

  protected AbstractRfasCodes(
      String[][] firstCharCodeMeaningData,
      String[][] secondAndThirdCharCodeMeaningData,
      String[][] fourthCharCodeMeaningData,
      String sourceInfo) {
    mSourceInfo = sourceInfo;

    mFirstCharacterCodesHashMap = new HashMap<>(firstCharCodeMeaningData.length);
    for (String[] row : firstCharCodeMeaningData) {
      mFirstCharacterCodesHashMap.put(row[0], row[1]);
    }

    mSecondAndThirdCharacterCodesHashMap = new HashMap<>(secondAndThirdCharCodeMeaningData.length);
    for (String[] row : secondAndThirdCharCodeMeaningData) {
      mSecondAndThirdCharacterCodesHashMap.put(row[0], row[1]);
    }

    mFourthCharacterCodesHashMap = new HashMap<>(fourthCharCodeMeaningData.length);
    for (String[] row : fourthCharCodeMeaningData) {
      mFourthCharacterCodesHashMap.put(row[0], row[1]);
    }
  }

  @Override
  public String getSourceInfo() {
    return mSourceInfo;
  }

  @Override
  public String[] getFirstCharacterKeys() {
    String[] keys = mFirstCharacterCodesHashMap.keySet().toArray(new String[0]);
    Arrays.sort(keys);
    return keys;
  }

  @Override
  public String getFirstCharacterValue(String key) {
    if (mFirstCharacterCodesHashMap.containsKey(key)) {
      return "1st Element: " + mFirstCharacterCodesHashMap.get(key);
    }
    return "No match for code.";
  }

  @Override
  public String[] getSecondAndThirdCharacterKeys() {
    String[] keys = mSecondAndThirdCharacterCodesHashMap.keySet().toArray(new String[0]);
    Arrays.sort(keys);
    return keys;
  }

  @Override
  public String getSecondAndThirdCharacterValue(String key) {
    if (mSecondAndThirdCharacterCodesHashMap.containsKey(key)) {
      return "2nd Element: " + mSecondAndThirdCharacterCodesHashMap.get(key);
    }
    return "No match for code.";
  }

  @Override
  public String[] getFourthCharacterKeys() {
    String[] keys = mFourthCharacterCodesHashMap.keySet().toArray(new String[0]);
    Arrays.sort(keys);
    return keys;
  }

  @Override
  public String getFourthCharacterValue(String key) {
    if (mFourthCharacterCodesHashMap.containsKey(key)) {
      return "3rd Element: " + mFourthCharacterCodesHashMap.get(key);
    }
    return "No match for code.";
  }
}
