package com.afilon.mayor.v11.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.model.DirectParty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BReinosa on 8/14/2017.
 */
public class PartyListAdapter  extends ArrayAdapter<DirectParty>{
    Context context;
    ArrayList<DirectParty> drawerItemList;
    int layoutResId;
    boolean firstEntry=true;
    boolean inReview = false;
    boolean saveMode = true;
    boolean correctMode = false;



    public PartyListAdapter(Context context, int resource) {
        super(context, resource);
    }
    public PartyListAdapter(Context context, int layoutResourceID, ArrayList<DirectParty> listItems) {
        super(context,layoutResourceID,listItems);
        this.context = context;
        layoutResId = layoutResourceID;
        this.drawerItemList = listItems;
    }
    public void setListener(PartyListListener listener){
        mListener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        PartyHolder drawerHolder;
        View view = convertView;
        if(view ==null){
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            drawerHolder = new PartyHolder();
            view = inflater.inflate(layoutResId, parent, false);
            drawerHolder.firstEntry = (CheckBox) view.findViewById(R.id.first_cb);
            drawerHolder.secondEntry = (CheckBox) view.findViewById(R.id.second_cb);
            drawerHolder.firstEntry.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(buttonView.isPressed() && isChecked){
                        selectParty(position);
                        mListener.onItemSelected();

                    }else if(buttonView.isPressed() && !isChecked){
                        drawerItemList.get(position).deselect();
                        mListener.onItemDeselected();
                    }
                }
            });

            drawerHolder.secondEntry.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(buttonView.isPressed() && isChecked){
                        confirmParty(position);
                        mListener.onItemSelected();
                    }else if(buttonView.isPressed() && !isChecked){
                        drawerItemList.get(position).unConfirm();
                        mListener.onItemDeselected();
                    }
                }
            });


            drawerHolder.partyFlag = (ImageView) view.findViewById(R.id.party_flag);
            drawerHolder.partyName =(TextView) view.findViewById(R.id.party_name);
            drawerHolder.currentVote = (TextView) view.findViewById(R.id.vote_tv);
            drawerHolder.currentAcc = (TextView) view.findViewById(R.id.acc_tv);
            drawerHolder.newAcc = (TextView) view.findViewById(R.id.new_acc_tv);
            view.setTag(drawerHolder);
        } else {
            drawerHolder = (PartyHolder) view.getTag();
        }

        DirectParty dItem = (DirectParty) this.drawerItemList.get(position);
        drawerHolder.firstEntry.setChecked(dItem.isSelected());
        drawerHolder.secondEntry.setChecked(dItem.isConfirmed());
        drawerHolder.partyFlag.setImageDrawable((view.getResources().getDrawable(dItem.getImgResId())));
        drawerHolder.partyName.setText(dItem.getParty_name());
        drawerHolder.currentVote.setText(dItem.getCurrentVote());
        drawerHolder.currentAcc.setText(dItem.getCurrentAcr());
        drawerHolder.newAcc.setText(dItem.getNewAcr());


        if(inReview){
            drawerHolder.firstEntry.setEnabled(false);
            drawerHolder.secondEntry.setEnabled(false);
            drawerHolder.firstEntry.setActivated(true);
            drawerHolder.secondEntry.setActivated(true);
            if(!dItem.match()){
                mListener.onMisMatch();
                drawerHolder.firstEntry.setBackgroundColor(view.getResources().getColor(R.color.red));
                drawerHolder.secondEntry.setBackgroundColor(view.getResources().getColor(R.color.red));
                //set vote to zero:
                drawerHolder.currentVote.setText("0");
            }
        }else if(saveMode){
            drawerHolder.currentVote.setText("0");
            drawerHolder.firstEntry.setEnabled(false);
            drawerHolder.firstEntry.setActivated(false);
            drawerHolder.secondEntry.setEnabled(false);
            drawerHolder.secondEntry.setActivated(false);
            drawerHolder.firstEntry.setBackgroundColor(view.getResources().getColor(R.color.transparent));
            drawerHolder.secondEntry.setBackgroundColor(view.getResources().getColor(R.color.transparent));

        }else if(correctMode){
            // only mis matches can be used:
            drawerHolder.firstEntry.setEnabled(false);
            drawerHolder.secondEntry.setEnabled(false);
            drawerHolder.firstEntry.setActivated(false);
            drawerHolder.secondEntry.setActivated(false);
            if(dItem.isEnable()){
                drawerHolder.firstEntry.setEnabled(firstEntry);
                drawerHolder.secondEntry.setEnabled(!firstEntry);
                drawerHolder.firstEntry.setActivated(firstEntry);
                drawerHolder.secondEntry.setActivated(!firstEntry);
                drawerHolder.firstEntry.setBackgroundColor(view.getResources().getColor(R.color.transparent));
                drawerHolder.secondEntry.setBackgroundColor(view.getResources().getColor(R.color.transparent));
            }
        }
        else{
            drawerHolder.firstEntry.setEnabled(firstEntry);
            drawerHolder.firstEntry.setActivated(firstEntry);
            drawerHolder.secondEntry.setEnabled(!firstEntry);
            drawerHolder.secondEntry.setActivated(!firstEntry);
            drawerHolder.firstEntry.setBackgroundColor(view.getResources().getColor(R.color.transparent));
            drawerHolder.secondEntry.setBackgroundColor(view.getResources().getColor(R.color.transparent));
        }

        return view;

    }
    private static class PartyHolder{
        CheckBox firstEntry;
        CheckBox secondEntry;
        ImageView partyFlag;
        TextView partyName;
        TextView currentVote;
        TextView currentAcc;
        TextView newAcc;
    }
    public void IsFirstEntry(boolean entry){
        firstEntry = entry;
        this.notifyDataSetChanged();
    }
    public void IsInReview(boolean review){
        this.inReview = review;
        this.notifyDataSetChanged();
    }
    public void saveMode(boolean mode){
        this.saveMode = mode;
        this.notifyDataSetChanged();
    }
    public void correctMode(boolean mode){
        this.inReview = false;
        this.saveMode=mode;
        this.correctMode = mode;
//        this.notifyDataSetChanged();
        clearAll();
    }

    private void transition(){
        for(int i=0; i<this.drawerItemList.size(); i++){
            drawerItemList.get(i).clearTempVote();
        }
    }


    private void selectParty(int position){
        drawerItemList.get(position).select();
        for(int i=0; i<this.drawerItemList.size(); i++){
            if(i==position) continue;
            drawerItemList.get(i).deselect();
        }
        this.notifyDataSetChanged();
    }

    private void confirmParty(int position){
        drawerItemList.get(position).confirm();
        for(int i=0; i<this.drawerItemList.size(); i++){
            if(i==position) continue;
            drawerItemList.get(i).unConfirm();
        }
        this.notifyDataSetChanged();

    }

    public void intiateFirstEntry(){
        firstEntry=true;
        saveMode=false;
        inReview=false;
        this.notifyDataSetChanged();
    }

    public void initiateReEntry(){
        saveMode=false;
        firstEntry=false;
        inReview=false;
        transition();
        this.notifyDataSetChanged();
    }

    public void clearAll(){
        for(int i=0; i<this.drawerItemList.size(); i++){
            drawerItemList.get(i).deselect();
            drawerItemList.get(i).unConfirm();
        }
        this.notifyDataSetChanged();
    }

    private List<Integer> compareCheckBoxes(){
        List<Integer> mismatches= new ArrayList<>();
        for(int i=0; i<this.drawerItemList.size(); i++){
            if(!drawerItemList.get(i).match()){
                mismatches.add(i);
            }
        }
        return  mismatches;
    }

    PartyListListener mListener;

    public interface PartyListListener{
        public void onItemSelected();
        public void onItemDeselected();
        public void onMisMatch();
        public void onMatch();
    }

}
