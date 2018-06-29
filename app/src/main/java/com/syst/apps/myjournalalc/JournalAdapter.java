package com.syst.apps.myjournalalc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class JournalAdapter extends RecyclerView.Adapter<JournalViewHolder> {

    List<Journal> mList = new ArrayList<>();
    Context mContext;

    public JournalAdapter(Context mContext) {
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public JournalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_view, parent, false);
        return new JournalViewHolder(v, mContext);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalViewHolder holder, int position) {
        Journal j = mList.get(position);
        holder.bind(j);
    }

    public void clear(){
        mList.clear();
        notifyDataSetChanged();
    }

    public void addData(ArrayList<Journal> jlist){
        this.mList.addAll(jlist);
        notifyDataSetChanged();
    }

    public Journal getJournal(int pos){
        return mList.get(pos);
    }

    public void removeAt(int pos){
        mList.remove(pos);
        notifyItemRemoved(pos);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
