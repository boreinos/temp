package com.afilon.mayor.v11.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.model.ActaEntry;

import java.util.List;

/**
 * Created by BReinosa on 8/7/2017.
 */
public class ActaAdapter extends ArrayAdapter<ActaEntry> {
    public ActaAdapter(Context context, int resource) {
        super(context, resource);
    }
    public ActaAdapter(Context context, int resource, List<ActaEntry> entries){
        super(context,resource,entries);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;
        if(v==null){
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.acta_entry, null);
        }
        ActaEntry entry = getItem(position);
        if(entry!=null){
            TextView nameView = (TextView) v.findViewById(R.id.name);
            TextView totalView = (TextView) v.findViewById(R.id.totalMarks);
            TextView totalVoteView = (TextView) v.findViewById(R.id.totalVotes);
            TextView planchaVoteView = (TextView) v.findViewById(R.id.planchavotes);
            TextView parcialVoteView = (TextView)v.findViewById(R.id.parcialvotes);
            TextView cruzadoVoteView = (TextView) v.findViewById(R.id.cruzadovotes);

            TextView planchaView= (TextView) v.findViewById(R.id.planchaMarks);
            TextView parcialView = (TextView) v.findViewById(R.id.parcialMarks);
            TextView cruzadoView = (TextView) v.findViewById(R.id.cruzadoMarks);

            nameView.setText(entry.getEntryName());
            totalView.setText(entry.getTotalMarcas());
            totalVoteView.setText(entry.getTotalVotes());
            planchaView.setText(entry.getPlanchaMarcas());
            parcialView.setText(entry.getParcialMarcas());
            cruzadoView.setText(entry.getCruzadoMarcas());
            planchaVoteView.setText(entry.getPlanchaVotes());
            parcialVoteView.setText(entry.getParcialVotes());
            cruzadoVoteView.setText(entry.getCruzadoVotes());

        }
        if (position % 2 == 0) {
            v.setBackgroundColor(Color.parseColor("#e2eefb"));
        } else {
            v.setBackgroundColor(Color.WHITE);
        }

        return v;
    }
}
