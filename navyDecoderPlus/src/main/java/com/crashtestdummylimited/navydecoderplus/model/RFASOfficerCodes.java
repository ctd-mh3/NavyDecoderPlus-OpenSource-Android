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

public class RFASOfficerCodes extends AbstractRfasCodes {

  private static final String[][] FIRST_CHAR_CODE_MEANING_DATA = {
    {"S", "O6-W1 (Exact paygrade match only)"},
    {"M", "O6-O3 (Medical designators only)"},
    {"I", "O4-O1"},
    {"K", "O3-O1"},
    {"P", "O4-O3"},
    {"X", "O4-W1"},
    {"W", "W5-W1"}
  };

  private static final String[][] SECOND_AND_THIRD_CHAR_CODE_MEANING_DATA = {
    {"AA", "Must match designator and any coded SSP or AQD"},
    {
      "AB",
      "Must match designator. If AQD and/or SSP coded, member must earn AQD and/or SSP within three years."
    },
    {"AC", "Must match designator"},
    {"AJ", "1XXX"},
    {"AK", "1XXX and coded SSP or AQD"},
    {"AL", "1XXX, 6XXX, 7XXX"},
    {"AM", "1XXX, 6XXX, 7XXX and coded SSP or AQD"},
    {"AQ", "1050, 11XX, 13XX"},
    {"AR", "11XX, 13XX and coded SSP or AQD"},
    {"EB", "112X, 62XX, 713X"},
    {"ED", "110X, 111X, 112X, 62XX, 72XX"},
    {"EF", "110X, 111X, 112X, 62XX, 72XX and any coded SSP or AQD"},
    {"FE", "131X, 132X"},
    {"FF", "131X, 132X and coded SSP or AQD"},
    {"FK", "130X, 131X, 132X"},
    {"FL", "130X, 131X, 132X and coded SSP or AQD"},
    {"GU", "110X, 111X, 112X, 144X, 613X, 614X, 618X, 623X, 626X, 713X"},
    {
      "HB",
      "13XX, 150X, 151X, 152X, 633X, 733X; With requisite Engineering System Development (for 151X billet) or Aviation Maintenance (for 152X billet) background/experience."
    },
    {"JQ", "200X, 210X, 220X, 230X, 270X, 290X and must match any coded SSP or AQD"},
    {"JS", "200X, 210X, 220X, 230X, 270X, 290X"},
    {"KP", "310X, 651X, 751X"},
    {"KQ", "310X, 651X, 751X and coded SSP or AQD"},
    {"LB", "510X, 653X, 753X"},
    {"LC", "510X, 653X, 753X and coded SSP or AQD"},
    {
      "LW",
      "6XXX, 7XXX within skill categories (2nd and 3rd digit of designator match, i.e. 611X can fill 711X billet or vice versa; 633X and 734X are considered equivalent skill categories)."
    },
    {
      "LX",
      "6XXX, 7XXX within skill categories (2nd and 3rd digit of designator match, i.e. 611X can fill 711X billet or vice versa; 633X and 734X are considered equivalent skill categories) and coded SSP or AQD"
    },
    {"MO", "11XX, 166X"},
    {"MP", "111X, 166X and coded SSP or AQD"},
    {"OM", "181X, 781X"},
    {"ON", "183X, 683X, 783X"},
    {"OP", "182X, 682X, 782X"},
    {"OQ", "181X, 781X and coded SSP or AQD"},
    {"OR", "18XX, 682X, 683X, 781X, 782X, 783X or any designator holding a VSX AQD"},
    {"OS", "Any designator. Must hold a VSX AQD (Space Cadre)"},
    {"OT", "183X, 683X, 783X and coded SSP or AQD"},
    {"OU", "182X, 682X, 782X and coded SSP or AQD"},
    {
      "OY",
      "Any designator. Must hold VSX AQD at or higher than the billet AQD requirement, or the member must sign a NAVPERS 1070/613 and earn the required AQD within three years."
    },
    {"OZ", "Any designator. Must hold a VSX AQD at or higher than the billet AQD requirement."},
    {"SO", "113X, 114X"},
    {"SP", "113X, 114X and coded SSP or AQD"},
    {"SU", "1XXX, Staff Corps (NEPLO Only)"},
    {"SV", "1XXX, Staff Corps (NEPLO Only). Must hold JN1 AQD"}
  };

  private static final String[][] FOURTH_CHAR_CODE_MEANING_DATA = {
    {"E", "Either Gender"},
    {"R", "Billet is eligible for IDT-R"}
  };

  public RFASOfficerCodes() {
    super(
        FIRST_CHAR_CODE_MEANING_DATA,
        SECOND_AND_THIRD_CHAR_CODE_MEANING_DATA,
        FOURTH_CHAR_CODE_MEANING_DATA,
        "RESFOR N123 (21 JUL 2023)");
  }
}
