package com.afilon.mayor.v11.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
//import com.afilon.mayor.v11.activities.CrossedVoteActivity.OnGridListener;
import com.afilon.mayor.v11.interfaces.OnGridListener;
import com.afilon.mayor.v11.model.CandidateCrossVote;
import com.afilon.mayor.v11.model.Party;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static com.afilon.mayor.v11.R.drawable.blue_x;
import static com.afilon.mayor.v11.R.drawable.match_x;

/**
 * Created by BReinosa on 6/14/2017.
 */
public class FlagAdapter extends RecyclerView.Adapter<FlagAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<Party> mPartyArrayList;
    private OnGridListener flagGrid;
    private boolean ignore = true;
    private boolean inReview = false;
    private boolean isGreen= false;
    private boolean attachListener = false;
    private int resId = blue_x;

    public FlagAdapter(Context context, ArrayList<Party> partyArrayList, OnGridListener flagGrid){
        this.mContext = context;
        this.mPartyArrayList = partyArrayList;
        this.flagGrid = flagGrid;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;
        if(Consts.LOCALE.contains("HON")){
            view = inflater.inflate(R.layout.crossvote_flags_gridview, parent, false);
        }else{
            view = inflater.inflate(R.layout.crossvote_flags_gridview_sv, parent, false);
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mPartyArrayList.get(position);
        int mismatches = holder.mItem.getMismatchQty();
        holder.mismatch.setText(String.valueOf(mismatches));
        holder.warning.setVisibility(mismatches>0? View.VISIBLE: View.INVISIBLE);
        if(isGreen){
            ((ImageView) holder.warning.findViewById(R.id.warning_triangle)).setImageResource(R.drawable.green_triangle);
            holder.mismatch.setTextColor(mContext.getResources().getColor(R.color.white));
        }else {
            ((ImageView) holder.warning.findViewById(R.id.warning_triangle)).setImageResource(R.drawable.yellow_triangle);
            holder.mismatch.setTextColor(mContext.getResources().getColor(R.color.black));

        }

//        holder.itemImage.setTag(flag.getParty_preferential_election_id());
        holder.itemImage.setImageResource(holder.mItem.getPartyDrawableId()); //set flagdrawable
        holder.itemTitle.setText(String.valueOf(holder.mItem.getPartyMarks()));  //set number of marks
        holder.itemCV.setText(String.format(Locale.US,"%.4f",holder.mItem.getBallotVotes()));
        //Testing this
        if(Consts.LOCALE.equals(Consts.HONDURAS)){
            holder.itemCV.setVisibility(View.GONE);
        }else{
            // El Salvador logic:
            if(!inReview){
                holder.locked.setImageResource(attachListener? R.color.transparent:R.color.faded);// is guy locked?
               // holder.warning.setVisibility(View.INVISIBLE);
            }else {
                // it is in review:
                holder.locked.setImageResource(holder.mItem.isMismatch() ? R.color.transparent:R.color.faded);// is guy locked?
               // holder.warning.setVisibility((holder.mItem.isMismatch()&& showWarning) ? View.VISIBLE : View.INVISIBLE);
            }
            holder.redImage.setImageResource(holder.mItem.isMarked() ? resId : R.color.transparent); // always show marks if marked
            if(resId==match_x && holder.mItem.isMarked()){
                holder.locked.setImageResource(R.color.transparent);
            }

        }

        holder.itemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Flag was touched: print the flag:
                if(Consts.LOCALE.equals(Consts.HONDURAS)){
                    if(!inReview && !ignore){
                        flagGrid.onPartyGridEvent(holder.mItem);
                    }
                }else if(Consts.LOCALE.equals(Consts.ELSALVADOR)){
                    if (attachListener) {
                        if(!inReview){
                            itemSelected(holder);
                        }else if(holder.mItem.isMismatch()) {
                            itemSelected(holder);
                        }
                    }
                }

            }
        });

    }
    private void itemSelected(ViewHolder holder){
        if(holder.mItem.isMarked()){
            holder.redImage.setImageResource(R.color.transparent);
            holder.mItem.setMark(false);
            flagGrid.onPartyGridEvent(holder.mItem);
        }else{
            holder.redImage.setImageResource(resId);
            holder.mItem.setMark(true);
            flagGrid.onPartyGridEvent(holder.mItem);
        }
    }

    @Override
    public int getItemCount() {
        return mPartyArrayList.size();

    }

    public void updatePartyData(ArrayList<Party> partyArrayList){
        for(int i= 0; i<mPartyArrayList.size(); i++){
            mPartyArrayList.set(i, partyArrayList.get(i));
        }
        this.notifyDataSetChanged();
//        mPartyArrayList =  partyArrayList;
    }

    public void attachListener(boolean attach) {
        attachListener = attach;
        this.notifyDataSetChanged();
    }
    public void allMatch() {
        for (int j = 0; j <mPartyArrayList.size(); j++) {
            mPartyArrayList.get(j).setMismatch(false);
        }
        this.notifyDataSetChanged();
    }

    public void unLockMisMatches(HashMap<String, Party> mismatches) {
        for (int j = 0; j < mPartyArrayList.size(); j++) {
            String partyId =mPartyArrayList.get(j).getParty_preferential_election_id();
            Party candidate = mismatches.get(partyId);
            if (candidate != null) {
                mPartyArrayList.get(j).setMismatch(true); //enable touch
                mPartyArrayList.get(j).setMark(false); // remove mark
            }
        }
    }
    public void removePartyMarks() {
        for (Party cand : mPartyArrayList) {
            cand.setMark(false);
        }
        this.notifyDataSetChanged();
    }

    public void setGreenWarning(boolean isGreen){
        this.isGreen = isGreen;
        this.notifyDataSetChanged();
    }

    public void ignoreTouch(boolean ignore){
        this.ignore = ignore;
        this.notifyDataSetChanged();
    }

    public void setReviewMode(boolean reviewMode){
        this.inReview = reviewMode;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public final View mView;

        public TextView itemTitle;
        public ImageView itemImage;
        public final ImageView redImage;
        public View warning;
        public TextView itemCV;
        public TextView mismatch;
        public final ImageView locked;

        public Party mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            redImage = (ImageView) mView.findViewById(R.id.redcross_image);
            itemImage = (ImageView) mView.findViewById(R.id.item_image);
            itemTitle = (TextView) mView.findViewById(R.id.item_title);
            itemCV = (TextView) mView.findViewById(R.id.item_cv);
            warning = mView.findViewById(R.id.mismatch_icon);
            mismatch =(TextView) mView.findViewById(R.id.mismatch_value);
            locked = (ImageView) mView.findViewById(R.id.fade);

        }
    }
}
