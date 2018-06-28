package com.syst.apps.myjournalalc;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class JournalViewHolder extends RecyclerView.ViewHolder {

    TextView jourSum, jourDay,jourMth;
    Journal j;
    public JournalViewHolder(View itemView, final Context c) {
        super(itemView);
        jourSum = itemView.findViewById(R.id.dsum);
        jourDay = itemView.findViewById(R.id.ddy);
        jourMth = itemView.findViewById(R.id.dmth);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent crEntryAct = new Intent(c, CreateEntryActivity.class);
                crEntryAct.putExtra(CreateEntryActivity.JOURNAL, j);
                c.startActivity(crEntryAct);
            }
        });
    }

    void bind(Journal journal){
        if(journal != null){
            j = journal;
            jourSum.setText(journal.getJsum());
            jourDay.setText(journal.getJday());
            jourMth.setText(journal.getJmth());
        }
    }
}
