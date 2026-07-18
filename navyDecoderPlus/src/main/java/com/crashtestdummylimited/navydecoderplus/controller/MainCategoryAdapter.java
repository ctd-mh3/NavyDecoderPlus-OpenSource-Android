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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.crashtestdummylimited.navydecoderplus.R;
import java.util.List;

/** RecyclerView adapter for the main screen's category list. */
public class MainCategoryAdapter extends RecyclerView.Adapter<MainCategoryAdapter.ViewHolder> {

  public interface OnCategoryClickListener {
    void onCategoryClick(int position);
  }

  private final List<String> mLabels;
  private final OnCategoryClickListener mListener;

  public MainCategoryAdapter(List<String> labels, OnCategoryClickListener listener) {
    mLabels = labels;
    mListener = listener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.main_screen_selection_list_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    holder.textView.setText(mLabels.get(position));
    holder.itemView.setOnClickListener(
        v -> mListener.onCategoryClick(holder.getBindingAdapterPosition()));
  }

  @Override
  public int getItemCount() {
    return mLabels.size();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final TextView textView;

    ViewHolder(@NonNull View itemView) {
      super(itemView);
      textView = itemView.findViewById(android.R.id.text1);
    }
  }
}
