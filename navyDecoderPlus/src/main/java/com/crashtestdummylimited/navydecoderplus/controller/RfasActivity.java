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
import com.crashtestdummylimited.navydecoderplus.R.string;
import com.crashtestdummylimited.navydecoderplus.databinding.RfasScreenBinding;
import com.crashtestdummylimited.navydecoderplus.model.RFASEnlistedCodes;
import com.crashtestdummylimited.navydecoderplus.model.RFASOfficerCodes;
import com.crashtestdummylimited.navydecoderplus.model.RFASReferenceData;

import android.content.Intent;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemSelectedListener;

import java.util.Objects;

public class RfasActivity extends AppCompatActivity {

  private RfasScreenBinding mBinding;
  private RFASReferenceData rfasReferenceData;

  //*************************************************************************
  //
  //  Overwritten to support menu
  //
  //*************************************************************************

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
  //*************************************************************************
  //  End Menu Support Code
  //*************************************************************************

  private void setupSpinnerFromArray(Spinner spinner, String[] stringArray, OnItemSelectedListener listener) {

    ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, stringArray);

    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

    spinner.setAdapter(adapter);

    spinner.setOnItemSelectedListener(listener);
  }

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTheme(R.style.AppTheme);

    mBinding = RfasScreenBinding.inflate(getLayoutInflater());
    View view = mBinding.getRoot();
    setContentView(view);

    // Grab info from bundle to tell if enlisted or officer RFAS
    Intent mIntent = getIntent();
    String mRfasType = mIntent.getStringExtra("RFAS_TYPE");

    // Setup all the spinners
    switch (Objects.requireNonNull(mRfasType)) {
      case "Enlisted":
        rfasReferenceData = new RFASEnlistedCodes();
        mBinding.rfasTopLevelDescription.setText(this.getString(string.rfasEnlistedTopLevelDescription));
        setupSpinnerFromArray(mBinding.rfasFirstCharacter, rfasReferenceData.getFirstCharacterKeys(), new RFASDecoderItemSelectedListener());
        setupSpinnerFromArray(mBinding.rfasSecondAndThirdCharacter, rfasReferenceData.getSecondAndThirdCharacterKeys(), new RFASDecoderItemSelectedListener());
        setupSpinnerFromArray(mBinding.rfasFourthCharacter, rfasReferenceData.getFourthCharacterKeys(), new RFASDecoderItemSelectedListener());
        break;
      case "Officer":
        rfasReferenceData = new RFASOfficerCodes();
        mBinding.rfasTopLevelDescription.setText(this.getString(string.rfasOfficerTopLevelDescription));
        setupSpinnerFromArray(mBinding.rfasFirstCharacter, rfasReferenceData.getFirstCharacterKeys(), new RFASDecoderItemSelectedListener());
        setupSpinnerFromArray(mBinding.rfasSecondAndThirdCharacter, rfasReferenceData.getSecondAndThirdCharacterKeys(), new RFASDecoderItemSelectedListener());
        setupSpinnerFromArray(mBinding.rfasFourthCharacter, rfasReferenceData.getFourthCharacterKeys(), new RFASDecoderItemSelectedListener());
        break;
      default:
        // TODO- Throw Exception
        break;
    }
  }

  private class RFASDecoderItemSelectedListener implements OnItemSelectedListener {

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

      String firstCharacterKey = mBinding.rfasFirstCharacter.getItemAtPosition(mBinding.rfasFirstCharacter.getSelectedItemPosition()).toString();
      String firstCharacterValue = rfasReferenceData.getFirstCharacterValue(firstCharacterKey);

      String secondAndThirdCharacterKey = mBinding.rfasSecondAndThirdCharacter.getItemAtPosition(mBinding.rfasSecondAndThirdCharacter.getSelectedItemPosition()).toString();
      String secondAndThirdCharacterValue = rfasReferenceData.getSecondAndThirdCharacterValue(secondAndThirdCharacterKey);

      String fourthCharacterKey = mBinding.rfasFourthCharacter.getItemAtPosition(mBinding.rfasFourthCharacter.getSelectedItemPosition()).toString();
      String fourthCharacterValue = rfasReferenceData.getFourthCharacterValue(fourthCharacterKey);


      String resultString = firstCharacterValue + "\n" + secondAndThirdCharacterValue + "\n" + fourthCharacterValue;
      mBinding.rfasDecodeDescription.setText(resultString);


      String sourceInfo = rfasReferenceData.getSourceInfo();
      mBinding.rfasSourceDescription.setText(sourceInfo);
    }

    public void onNothingSelected(AdapterView<?> parent) {
      // Do nothing.
    }
  }
}