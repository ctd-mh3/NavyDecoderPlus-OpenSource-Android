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
package com.crashtestdummylimited.navydecoderplus.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import android.content.Context;

/**
 * Utility implementation for loading data which is pre-packages in the app.
 */
final class DataLoader {

  /**
   * Private constructor.
   */
  private DataLoader() {
  }

  /**
   * reads the specified file and returns its content as a String.
   *
   * @param context the context.
   * @return content of the resource as a String.
   * @throws IOException if errors occur while reading the resource file
   */
  public static String loadData(final Context context, final int resourceIdentifier)
      throws IOException {

    if (resourceIdentifier != 0) {
      InputStream inputStream = context.getApplicationContext().getResources()
          .openRawResource(resourceIdentifier);
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
      String line;
      StringBuilder data = new StringBuilder();

      while ((line = reader.readLine()) != null) {
        data.append(line);
      }

      reader.close();
      return data.toString();
    }

    return null;
  }
}
