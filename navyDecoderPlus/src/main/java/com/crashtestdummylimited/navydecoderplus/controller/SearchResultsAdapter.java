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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.crashtestdummylimited.navydecoderplus.R;
import java.util.Objects;

/**
 * RecyclerView adapter for search results, backed by an immutable {@link SearchResultItem} list.
 */
class SearchResultsAdapter extends ListAdapter<SearchResultItem, SearchResultsAdapter.ViewHolder> {

  interface OnResultClickListener {
    void onResultClick(@SuppressWarnings("unused") SearchResultItem item);
  }

  private static final DiffUtil.ItemCallback<SearchResultItem> DIFF_CALLBACK =
      new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(
            @NonNull SearchResultItem oldItem, @NonNull SearchResultItem newItem) {
          // rowids can collide across FTS tables, so identity requires category + id together.
          return oldItem.id == newItem.id
              && Objects.equals(oldItem.categoryKey, newItem.categoryKey);
        }

        @Override
        public boolean areContentsTheSame(
            @NonNull SearchResultItem oldItem, @NonNull SearchResultItem newItem) {
          return oldItem.id == newItem.id
              && Objects.equals(oldItem.categoryKey, newItem.categoryKey)
              && Objects.equals(oldItem.code, newItem.code)
              && Objects.equals(oldItem.meaning, newItem.meaning)
              && Objects.equals(oldItem.categoryLabel, newItem.categoryLabel);
        }
      };

  private final OnResultClickListener mListener;

  SearchResultsAdapter(OnResultClickListener listener) {
    super(DIFF_CALLBACK);
    mListener = listener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.search_result_list_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    SearchResultItem item = getItem(position);
    holder.itemToDecode.setText(item.code);
    holder.decodeInfo.setText(item.meaning);
    if (item.categoryLabel.isEmpty()) {
      holder.category.setVisibility(View.GONE);
    } else {
      holder.category.setText(item.categoryLabel);
      holder.category.setVisibility(View.VISIBLE);
    }
    holder.itemView.setOnClickListener(v -> mListener.onResultClick(item));
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final TextView itemToDecode;
    final TextView decodeInfo;
    final TextView category;

    ViewHolder(@NonNull View itemView) {
      super(itemView);
      itemToDecode = itemView.findViewById(R.id.searchScreenResultItemToDecodeTextView);
      decodeInfo = itemView.findViewById(R.id.searchScreenResultDecodeInfoTextView);
      category = itemView.findViewById(R.id.searchScreenResultCategoryTextView);
    }
  }
}
