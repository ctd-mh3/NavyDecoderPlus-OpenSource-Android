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

import com.crashtestdummylimited.navydecoderplus.R;

/**
 * Single source of truth for all decode categories. Each constant encodes the display order in the
 * main list (via ordinal), the internal DB/URI key, the string-resource label, the SQLite FTS table
 * name, and the BlankActivity subclass to launch for search.
 *
 * <p>RFAS categories ({@link #RFAS_ENLISTED}, {@link #RFAS_OFFICER}) are special-cased: they use
 * {@link RfasActivity} instead of the standard search flow, so {@code key}, {@code ftsTable}, and
 * {@code blankActivityClass} are all null for those entries.
 *
 * <p>Adding a new category requires: (1) a new constant here, (2) the two BlankActivity /
 * SearchableDecoderActivity subclass files, (3) a searchable XML resource, (4) manifest entries,
 * and (5) the SQL table + database rebuild. Nothing else needs to change in Java.
 */
public enum Category {
  AQD_CODES("aqdcodes", R.string.categoryAqdCodes, "FTS_aqd_codes", BlankActivityAqdCodes.class),
  ENLISTED_RATING_CODES(
      "enlistedratingcodes",
      R.string.categoryEnlistedRatingCodes,
      "FTS_enlisted_rating_codes",
      BlankActivityEnlistedRatingCodes.class),
  IMS_CODES("imscodes", R.string.categoryImsCodes, "FTS_ims_codes", BlankActivityImsCodes.class),
  MAS_CODES("mascodes", R.string.categoryMasCodes, "FTS_mas_codes", BlankActivityMasCodes.class),
  NEC_CODES("neccodes", R.string.categoryNecCodes, "FTS_nec_codes", BlankActivityNecCodes.class),
  NRA_CODES(
      "nracodes",
      R.string.categoryNavyReserveActivitiesCodes,
      "FTS_nra_codes",
      BlankActivityNraCodes.class),
  NOBC_CODES(
      "nobccodes", R.string.categoryNobcCodes, "FTS_nobc_codes", BlankActivityNobcCodes.class),
  OFFICER_BILLET_CODES(
      "officerbilletcodes",
      R.string.categoryOfficerBilletCodes,
      "FTS_officer_billet_codes",
      BlankActivityOfficerBilletCodes.class),
  OFFICER_DESIGNATOR_CODES(
      "officerdesignatorcodes",
      R.string.categoryOfficerDesignatorCodes,
      "FTS_officer_designator_codes",
      BlankActivityOfficerDesignatorCodes.class),
  OFFICER_PAYGRADE_CODES(
      "officerpaygradecodes",
      R.string.categoryOfficerPaygradeCodes,
      "FTS_officer_paygrade_codes",
      BlankActivityOfficerPaygradeCodes.class),
  RBSC_BILLET_CODES(
      "rbscbilletcodes",
      R.string.categoryRbscBilletCodes,
      "FTS_rbsc_billet_codes",
      BlankActivityRbscBilletCodes.class),
  RUI_CODES(
      "ruicodes",
      R.string.categoryReserveUnitIdentificationCodes,
      "FTS_rui_codes",
      BlankActivityRuiCodes.class),
  RP_CODES(
      "rpcodes", R.string.categoryReserveProgramCodes, "FTS_rp_codes", BlankActivityRpCodes.class),
  RFAS_ENLISTED(null, R.string.categoryRfasEnlistedCodes, null, null),
  RFAS_OFFICER(null, R.string.categoryRfasOfficerCodes, null, null),
  SSP_CODES(
      "sspcodes",
      R.string.categorySubspecialityCodes,
      "FTS_ssp_codes",
      BlankActivitySspCodes.class);

  /** Internal DB/URI key. Null for RFAS categories, which bypass the search flow. */
  public final String key;

  /** String resource ID for the display label shown in the main category list. */
  public final int labelRes;

  /** SQLite FTS virtual table name. Null for RFAS categories. */
  public final String ftsTable;

  /** BlankActivity subclass that triggers the search dialog. Null for RFAS categories. */
  public final Class<? extends BlankActivity> blankActivityClass;

  Category(
      String key,
      int labelRes,
      String ftsTable,
      Class<? extends BlankActivity> blankActivityClass) {
    this.key = key;
    this.labelRes = labelRes;
    this.ftsTable = ftsTable;
    this.blankActivityClass = blankActivityClass;
  }

  /** Returns the Category whose {@link #key} matches the given string, or null if not found. */
  public static Category fromKey(String key) {
    if (key == null) return null;
    for (Category c : values()) {
      if (key.equals(c.key)) return c;
    }
    return null;
  }
}
