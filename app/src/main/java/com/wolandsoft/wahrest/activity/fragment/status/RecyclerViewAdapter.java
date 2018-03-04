/*
    Copyright 2018 Alexander Shulgin

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.wolandsoft.wahrest.activity.fragment.status;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wolandsoft.wahrest.R;
import com.wolandsoft.wahrest.event.OnStatusEventStorageChanges;
import com.wolandsoft.wahrest.event.StatusEvent;
import com.wolandsoft.wahrest.event.StatusEventStorage;
import com.wolandsoft.wahrest.common.LogEx;

/**
 * Adapter for {@link RecyclerView} component.
 *
 * @author Alexander Shulgin
 */
class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> implements OnStatusEventStorageChanges {
    private final StatusEventStorage mStatusEventStorage = StatusEventStorage.getInstance();

    RecyclerViewAdapter() {

    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LogEx.d("onCreateViewHolder(", parent, ",", viewType, ")");
        View card = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_status_include_card, parent, false);
        return new RecyclerViewHolder(card);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        LogEx.d("onBindViewHolder(", holder, ",", position, ")");
        final StatusEvent item = getItem(position);
        holder.mTxtTitle.setText(item.getName());
        holder.mTxtTitleSmall.setText(item.getDescription());
    }

    @Override
    public int getItemCount() {
        LogEx.d("getItemCount()");
        return mStatusEventStorage.size();
    }

    private StatusEvent getItem(int index) {
        LogEx.d("getItem(", index, ")");
        return  mStatusEventStorage.get(index);
    }

    public void onStatusEventAdded() {
        this.notifyItemRangeInserted(mStatusEventStorage.size() - 1,1);
    }
}
