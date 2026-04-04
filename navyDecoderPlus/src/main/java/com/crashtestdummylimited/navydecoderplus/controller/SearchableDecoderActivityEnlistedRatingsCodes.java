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

// Manifest anchor required by the Android Search framework. Each category needs its own class so
// AndroidManifest.xml can attach android.app.searchable metadata pointing to a per-category
// res/xml/searchable_*.xml. That XML encodes the category-specific suggestion URI path
// (searchSuggestPath), which is what makes autocomplete suggestions category-specific.
// Consolidating
// these into one class would break per-category suggestions. All search logic lives in
// SearchableDecoderActivity; all category data lives in Category.java.
public class SearchableDecoderActivityEnlistedRatingsCodes extends SearchableDecoderActivity {}
