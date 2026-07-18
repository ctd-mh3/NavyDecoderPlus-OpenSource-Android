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
package com.crashtestdummylimited.navydecoderplus;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.crashtestdummylimited.navydecoderplus.controller.Category;
import com.crashtestdummylimited.navydecoderplus.controller.MainCategoryAdapter;
import com.crashtestdummylimited.navydecoderplus.controller.MappingHelper;
import com.crashtestdummylimited.navydecoderplus.controller.MenuOptions;
import com.crashtestdummylimited.navydecoderplus.controller.RfasActivity;
import com.crashtestdummylimited.navydecoderplus.controller.SearchableDecoderActivity;
import com.crashtestdummylimited.navydecoderplus.util.AppUpdateChecker;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import java.util.ArrayList;
import java.util.List;

public class NavyDecoderPlus extends AppCompatActivity {

  // *************************************************************************
  //
  //  Overwritten to support menu
  //
  // *************************************************************************

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuOptions.onCreateOptionsMenu(this, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    MenuOptions.onOptionsItemSelected(this, item);
    return true;
  }

  // *************************************************************************
  //  End Menu Support Code
  // *************************************************************************

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main_screen);

    ViewCompat.setOnApplyWindowInsetsListener(
        getWindow().getDecorView().findViewById(android.R.id.content),
        (v, windowInsets) -> {
          Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
          return WindowInsetsCompat.CONSUMED;
        });

    // AppTheme extends Theme.Material3.DayNight which always provides an ActionBar; null check is
    // defensive only.
    if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.app_name);

    RecyclerView recyclerView = findViewById(R.id.mainItemToDecodeListView);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    Category[] categories = Category.values();
    // Position 0 is the global "Search All" entry; categories follow at positions 1..N.
    List<String> decodeOptions = new ArrayList<>(categories.length + 1);
    decodeOptions.add(getString(R.string.categorySearchAll));
    for (Category category : categories) {
      decodeOptions.add(getString(category.labelRes));
    }

    recyclerView.setAdapter(new MainCategoryAdapter(decodeOptions, this::onCategorySelected));

    MaterialDividerItemDecoration divider =
        new MaterialDividerItemDecoration(this, LinearLayoutManager.VERTICAL);
    divider.setDividerColorResource(this, R.color.listItemDivider);
    recyclerView.addItemDecoration(divider);
  }

  private void onCategorySelected(int position) {
    if (position == 0) {
      // Global search across all categories.
      Intent intent = new Intent(NavyDecoderPlus.this, SearchableDecoderActivity.class);
      intent.putExtra(
          MappingHelper.CATEGORY_KEY_IDENTIFIER, SearchableDecoderActivity.ALL_CATEGORIES_KEY);
      startActivity(intent);
      return;
    }

    Category category = Category.values()[position - 1];

    // RFAS categories use a spinner-based Activity instead of the standard search flow.
    if (category == Category.RFAS_ENLISTED || category == Category.RFAS_OFFICER) {
      String rfasType =
          (category == Category.RFAS_ENLISTED)
              ? RfasActivity.RFAS_TYPE_ENLISTED
              : RfasActivity.RFAS_TYPE_OFFICER;
      Intent intent = new Intent(NavyDecoderPlus.this, RfasActivity.class);
      intent.putExtra(RfasActivity.EXTRA_RFAS_TYPE, rfasType);
      startActivity(intent);
      return;
    }

    Intent intent = new Intent(NavyDecoderPlus.this, SearchableDecoderActivity.class);
    intent.putExtra(MappingHelper.CATEGORY_KEY_IDENTIFIER, category.key);
    startActivity(intent);
  }

  @Override
  protected void onResume() {
    super.onResume();
    AppUpdateChecker.checkForUpdate(this);
  }
}
