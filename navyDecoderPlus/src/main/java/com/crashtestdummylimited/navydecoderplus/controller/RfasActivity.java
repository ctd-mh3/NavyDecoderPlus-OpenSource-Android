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

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.crashtestdummylimited.navydecoderplus.R;
import com.crashtestdummylimited.navydecoderplus.databinding.RfasScreenBinding;
import com.crashtestdummylimited.navydecoderplus.model.RFASEnlistedCodes;
import com.crashtestdummylimited.navydecoderplus.model.RFASOfficerCodes;
import com.crashtestdummylimited.navydecoderplus.model.RFASReferenceData;
import com.crashtestdummylimited.navydecoderplus.util.ActivityUiUtils;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class RfasActivity extends AppCompatActivity {

  public static final String EXTRA_RFAS_TYPE = "RFAS_TYPE";
  public static final String RFAS_TYPE_ENLISTED = "Enlisted";
  public static final String RFAS_TYPE_OFFICER = "Officer";

  private RfasScreenBinding mBinding;
  private RFASReferenceData mRfasReferenceData;

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
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    MenuOptions.onOptionsItemSelected(this, item);
    return true;
  }

  // *************************************************************************
  //  End Menu Support Code
  // *************************************************************************

  /**
   * Populates an exposed-dropdown field with the given options, pre-selects the first one (an
   * {@link MaterialAutoCompleteTextView} has no auto-select-position-0 behavior the way {@code
   * Spinner} did), and re-runs the decoder whenever the user picks a different option.
   */
  private void setupDropdownFromArray(MaterialAutoCompleteTextView dropdown, String[] options) {
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.rfas_dropdown_item, options);
    dropdown.setAdapter(adapter);
    dropdown.setText(options[0], false);
    dropdown.setOnItemClickListener((parent, view, position, id) -> updateDecodedResult());
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mBinding = RfasScreenBinding.inflate(getLayoutInflater());
    View view = mBinding.getRoot();
    setContentView(view);

    ActivityUiUtils.applyDefaultWindowInsets(this);
    ActivityUiUtils.setToolbarTitle(this, R.string.app_name, true);

    // Grab info from bundle to tell if enlisted or officer RFAS
    Intent intent = getIntent();
    String rfasType = intent.getStringExtra(EXTRA_RFAS_TYPE);

    if (rfasType == null) {
      finish();
      return;
    }

    // Setup all the dropdowns
    switch (rfasType) {
      case RFAS_TYPE_ENLISTED:
        mRfasReferenceData = new RFASEnlistedCodes();
        mBinding.rfasTopLevelDescription.setText(
            this.getString(R.string.rfasEnlistedTopLevelDescription));
        break;
      case RFAS_TYPE_OFFICER:
        mRfasReferenceData = new RFASOfficerCodes();
        mBinding.rfasTopLevelDescription.setText(
            this.getString(R.string.rfasOfficerTopLevelDescription));
        break;
      default:
        throw new IllegalArgumentException("Unknown RFAS type: " + rfasType);
    }

    setupDropdownFromArray(mBinding.rfasFirstCharacter, mRfasReferenceData.getFirstCharacterKeys());
    setupDropdownFromArray(
        mBinding.rfasSecondAndThirdCharacter, mRfasReferenceData.getSecondAndThirdCharacterKeys());
    setupDropdownFromArray(
        mBinding.rfasFourthCharacter, mRfasReferenceData.getFourthCharacterKeys());

    // Show the decode for the default (first) selections, mirroring the auto-fired initial
    // selection Spinner used to give for free.
    updateDecodedResult();
  }

  private void updateDecodedResult() {
    String firstCharacterKey = mBinding.rfasFirstCharacter.getText().toString();
    String firstCharacterValue = mRfasReferenceData.getFirstCharacterValue(firstCharacterKey);

    String secondAndThirdCharacterKey = mBinding.rfasSecondAndThirdCharacter.getText().toString();
    String secondAndThirdCharacterValue =
        mRfasReferenceData.getSecondAndThirdCharacterValue(secondAndThirdCharacterKey);

    String fourthCharacterKey = mBinding.rfasFourthCharacter.getText().toString();
    String fourthCharacterValue = mRfasReferenceData.getFourthCharacterValue(fourthCharacterKey);

    String resultString =
        firstCharacterValue + "\n" + secondAndThirdCharacterValue + "\n" + fourthCharacterValue;
    mBinding.rfasDecodeDescription.setText(resultString);

    mBinding.rfasSourceDescription.setText(mRfasReferenceData.getSourceInfo());
  }
}
