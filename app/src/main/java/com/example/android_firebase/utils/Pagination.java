package com.example.android_firebase.utils;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class Pagination extends RecyclerView.OnScrollListener {
    private LinearLayoutManager linearLayoutManager;

    public Pagination(LinearLayoutManager linearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = linearLayoutManager.getChildCount();
        int totalItemCount = linearLayoutManager.getItemCount();
        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

        Toast.makeText(recyclerView.getContext(), "on scrolled", Toast.LENGTH_SHORT).show();

        if(isLoading() || !isLoadMore()) {
            Toast.makeText(recyclerView.getContext(), "stop on scroll", Toast.LENGTH_SHORT).show();
            return;
        }

        if(firstVisibleItemPosition >= 0 && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
            Toast.makeText(recyclerView.getContext(), "on scrolled infinity", Toast.LENGTH_SHORT).show();
            loadMoreItem();
        }
    }

    public abstract void loadMoreItem();
    public abstract boolean isLoading();
    public abstract boolean isLoadMore();
}
