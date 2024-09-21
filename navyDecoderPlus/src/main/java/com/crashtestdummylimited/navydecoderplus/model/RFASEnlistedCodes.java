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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class RFASEnlistedCodes implements RFASReferenceData {

  private final HashMap<String, String> mFirstCharacterCodesHashMap;
  private final HashMap<String, String> mSecondAndThirdCharacterCodesHashMap;
  private final HashMap<String, String> mFourthCharacterCodesHashMap;

  public RFASEnlistedCodes() {
    String[][] FIRST_CHAR_CODE_MEANING_DATA = {
        {"M", "Command Master Chief Billet"},
        {"9", "E9 - (E8 and E9 (E9 Authorized Rating))"},
        {"S", "Command Senior Chief Billet (E8 Only)"},
        {"8", "E8 - (E7 through E9 (E8 Authorized Rating))"},
        {"7", "E7 - (E7 and E8 (E7 Authorized Rating))"},
        {"6", "E6 - (E5 and E6 (E6 Authorized Rating))"},
        {"Z", "E5 through E8 requiring NEC (CNAFR Only)"},
        {"J", "E5 and E6 - (E5 or E6 Authorized Rating)"},
        {"5", "E5 - (E4 through E6 (E5 Authorized Rating))"},
        {"4", "E1 through E5 - (E4 Authorized Rating)"},
        {"3", "E1 through E3 - (E1, E2, E3, E4 Authorized Rating)"},
        {"N", "E1 through E6 meeting horizontal AB or BB RFAS"}
    };
    mFirstCharacterCodesHashMap = new HashMap<>(FIRST_CHAR_CODE_MEANING_DATA.length);
    String[][] SECOND_AND_THIRD_CHAR_CODE_MEANING_DATA = {
        {"AA", "Must match billet rating"},
        {"AB", "Any source rate of the required NEC earned within time period designated by program manager"},
        {"AC", "BM, OS, QM"},
        {"AD", "EM, GSE"},
        {"AE", "EN, GSM, MM, MMA, MMW, TM"},
        {"AF", "DC, HT, MR"},
        {"AG", "AD, AE, AF, AM, AME, AO, AT, AV, AZ, PR"},
        {"AH", "AB, ABH, ABF"},
        {"AI", "AB, ABF, ABH, AC, AG"},
        {"AJ", "ET, ETR, ETV, FC, FT, STG, STS"},
        {"AM", "ET, ETV, ETR, IT, ITS"},
        {"AN", "Any Airman rating except AB (All), AC, AG, AW (All)"},
        {"AP", "Any Constructionman rating"},
        {"AR", "LN, MC, PS, RP, YN, YNS"},
        {"AS", "CS, CSS, LS, LSS, RS"},
        {"AT", "EOD, ND, SB, SO"},
        {"AU", "GM, MN"},
        {"AV", "EOD holding NEC 5337, ET, ETR, ETV, FC, MN, OS, STG, STS"},
        {"AZ", "Any AW rating"},
        {"BB", "Any source rating of the required NEC and must hold the NEC or component NEC per NAVPERS 18068F, Chapter IV"},
        {"BD", "ET, ETR, ETV, FC, FT, IT, ITS"},
        {"CC", "Must match rate and NEC per NAVPERS 18068F"},
        {"CD", "AG, CTI, CTR, CTT, CWT (Legacy CTN), IS, IT, ITS"},
        {"CN", "CTI, CTR, CTT, CWT (Legacy CTN)"},
        {"CS", "ET, ETR, ETV, FC, FT, IT, ITS, MT, OS, QM, STG, STS"},
        {"DM", "AD, AF, AM"},
        {"ET", "AE, AT, AV"},
        {"FN", "Any Fireman rating"},
        {"GS", "MA or any rate holding 815A NEC (9545 Legacy NEC)"},
        {"SF", "BM, DC, EM, EN, ET, ETV, ETR, FC, GM, GSE, GSM, HT, IC, MN, MM, MMA, MMW, MR, OS, QM, STG, STS, TM"},
        {"SN", "Any Seaman Rating"}
    };
    mSecondAndThirdCharacterCodesHashMap = new HashMap<>(SECOND_AND_THIRD_CHAR_CODE_MEANING_DATA.length);
    String[][] FOURTH_CHAR_CODE_MEANING_DATA = {
        {"E", "Either Gender"},
        {"R", "Billet is eligible for IDT-R"}
    };
    mFourthCharacterCodesHashMap = new HashMap<>(FOURTH_CHAR_CODE_MEANING_DATA.length);

    for (String[] aFIRST_CHAR_CODE_MEANING_DATA : FIRST_CHAR_CODE_MEANING_DATA) {
      mFirstCharacterCodesHashMap.put(aFIRST_CHAR_CODE_MEANING_DATA[0], aFIRST_CHAR_CODE_MEANING_DATA[1]);
    }
    for (String[] aSECOND_AND_THIRD_CHAR_CODE_MEANING_DATA : SECOND_AND_THIRD_CHAR_CODE_MEANING_DATA) {
      mSecondAndThirdCharacterCodesHashMap.put(aSECOND_AND_THIRD_CHAR_CODE_MEANING_DATA[0], aSECOND_AND_THIRD_CHAR_CODE_MEANING_DATA[1]);
    }

    for (String[] aFOURTH_CHAR_CODE_MEANING_DATA : FOURTH_CHAR_CODE_MEANING_DATA) {
      mFourthCharacterCodesHashMap.put(aFOURTH_CHAR_CODE_MEANING_DATA[0], aFOURTH_CHAR_CODE_MEANING_DATA[1]);
    }

  }

  @Override
  public String getSourceInfo() {
    return "RESFOR N123 (21 JUL 2023)";
  }

  @Override
  public String getCode() {

    return "Reserve Functional Area and Sex Code";
  }

  @Override
  public String[] getFirstCharacterKeys() {

    //TO-DO:  All of this is likely not needed.  We know the # of keys so
    //        we should just be able to create a static array of that
    //        size and then copy the keys over as we iterator through them
    Iterator<String> iterator = mFirstCharacterCodesHashMap.keySet().iterator();

    ArrayList<String> mArrayList = new ArrayList<>();

    while (iterator.hasNext()) {
      mArrayList.add(iterator.next());
    }

    Collections.sort(mArrayList);

    String[] stringArray = new String[mArrayList.size()];
    stringArray = mArrayList.toArray(stringArray);

    return stringArray;
  }

  @Override
  public String getFirstCharacterValue(String key) {

    String returnValue;

    if (mFirstCharacterCodesHashMap.containsKey(key)) {
      returnValue = "1st Element: " + mFirstCharacterCodesHashMap.get(key);
    } else {
      returnValue = "No match for code.";
    }

    return returnValue;
  }

  @Override
  public String[] getSecondAndThirdCharacterKeys() {

    //TO-DO:  All of this is likely not needed.  We know the # of keys so
    //        we should just be able to create a static array of that
    //        size and then copy the keys over as we iterator through them
    Iterator<String> iterator = mSecondAndThirdCharacterCodesHashMap.keySet().iterator();

    ArrayList<String> mArrayList = new ArrayList<>();

    while (iterator.hasNext()) {
      mArrayList.add(iterator.next());
    }

    Collections.sort(mArrayList);

    String[] stringArray = new String[mArrayList.size()];
    stringArray = mArrayList.toArray(stringArray);

    return stringArray;
  }

  @Override
  public String getSecondAndThirdCharacterValue(String key) {

    String returnValue;

    if (mSecondAndThirdCharacterCodesHashMap.containsKey(key)) {
      returnValue = "2nd Element: " + mSecondAndThirdCharacterCodesHashMap.get(key);
    } else {
      returnValue = "No match for code.";
    }

    return returnValue;
  }

  @Override
  public String[] getFourthCharacterKeys() {

    //TO-DO:  All of this is likely not needed.  We know the # of keys so
    //        we should just be able to create a static array of that
    //        size and then copy the keys over as we iterator through them
    Iterator<String> iterator = mFourthCharacterCodesHashMap.keySet().iterator();

    ArrayList<String> mArrayList = new ArrayList<>();

    while (iterator.hasNext()) {
      mArrayList.add(iterator.next());
    }

    Collections.sort(mArrayList);

    String[] stringArray = new String[mArrayList.size()];
    stringArray = mArrayList.toArray(stringArray);

    return stringArray;
  }

  @Override
  public String getFourthCharacterValue(String key) {

    String returnValue;

    if (mFourthCharacterCodesHashMap.containsKey(key)) {
      returnValue = "3rd Element: " + mFourthCharacterCodesHashMap.get(key);
    } else {
      returnValue = "No match for code.";
    }

    return returnValue;
  }
}