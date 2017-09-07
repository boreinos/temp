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
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.Utilities;

import java.util.ArrayList;
import java.util.HashMap;

import static com.afilon.mayor.v11.R.drawable.blue_x;
import static com.afilon.mayor.v11.R.drawable.match_x;

/**
 * Created by breinosa 6/6/2017.
 */
public class CrossVoteAdapter extends RecyclerView.Adapter<CrossVoteAdapter.ViewHolder> {
    private static final String CLASS_TAG = "CrossVoteAdapter";
    private Context mContext;
    private ArrayList<CandidateCrossVote> mCandidateCrossVote;
    private Utilities ah;
    private int mMaxCandidatesAllowed;
    private OnGridListener gridListener;
    private boolean ballotIsFull = false;
    private boolean attachListener = false;
    private boolean inReview = false;
    private boolean showWarning = true;
    private int resId = blue_x;


    public CrossVoteAdapter(Context context, ArrayList<CandidateCrossVote> candidateCrossVotes,
                            int mMaxCandidatesAllowed, OnGridListener listener) {
        this.mContext = context;
        this.mCandidateCrossVote = candidateCrossVotes;
        this.mMaxCandidatesAllowed = mMaxCandidatesAllowed;
        this.gridListener = listener;
    }

    public void setResId(int id){
        resId = id;
    }

    public void setReviewMode(boolean inReview){
        this.inReview = inReview;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ah = new Utilities(mContext);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = null;
//       Todo: uncomment
//        if(Consts.LOCALE.equals(Consts.HONDURAS)){
//            view = inflater.inflate(R.layout.crossvote_gridview_honduras, parent, false);
//        }else{
//            view = inflater.inflate(R.layout.crossvote_gridview, parent, false);
//        }
        view = inflater.inflate(R.layout.crossvote_gridview, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mCandidateCrossVote.get(position);
        holder.itemTitle.setText(holder.mItem.getCandidateName());
        holder.itemDetail.setText(holder.mItem.getCandidateId());
        holder.itemImage.setImageResource(holder.mItem.getCandidatePicture());
        holder.redImage.setVisibility(View.VISIBLE);
//        holder.redImage.bringToFront();

        if(!inReview){
            holder.locked.setImageResource(attachListener? R.color.transparent:R.color.faded);// is guy locked?
            holder.warning.setVisibility(View.INVISIBLE);
        }else {
            // it is in review:
            holder.locked.setImageResource(holder.mItem.isMismatch() ? R.color.transparent:R.color.faded);// is guy locked?
            holder.warning.setVisibility((holder.mItem.isMismatch()&& showWarning) ? View.VISIBLE : View.INVISIBLE);
        }
        holder.redImage.setImageResource(holder.mItem.isMarked() ? resId : R.color.transparent); // always show marks if marked

        if(resId==match_x && holder.mItem.isMarked()){
            holder.locked.setImageResource(R.color.transparent);
        }
        
        holder.redImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (attachListener) {
                    if(!inReview){
                        itemSelected(holder);
                    }else if(holder.mItem.isMismatch()) {
                        itemSelected(holder);
                    }
                }
            }
        });

        holder.mView.setVisibility(holder.mItem.getCandidateName().length() < 1 ? View.INVISIBLE : View.VISIBLE);
    }

    public void setShowWarning(boolean show){
        showWarning = show;
        this.notifyDataSetChanged();
    }

    private void itemSelected(ViewHolder holder){
        if (holder.mItem.isMarked()) {
            //If this the case, that means we want to remove the mark
            holder.redImage.setImageResource(R.color.transparent);
//            holder.redImage.bringToFront();
            mCandidateCrossVote.get(holder.getAdapterPosition()).setMark(false);
            gridListener.onCandidateGridEvent(holder.mItem); // update candidate info.
        } else {
            // determine that candidate can be selected
            if (!ballotIsFull) { //this flag is updated from fragment!
                // candidate was selected: from No mark to Mark!
                holder.redImage.setImageResource(resId);
//                holder.redImage.bringToFront();
                mCandidateCrossVote.get(holder.getAdapterPosition()).setMark(true);
                gridListener.onCandidateGridEvent(holder.mItem); // update candidate info.
            } else {
                ah.createCustomToast("LA CANTIDAD MAXIMA DE CANDIDATOS PARA ESTA ELECCION ES DE " + String.valueOf(mMaxCandidatesAllowed));
            }
        }
    }


    @Override
    public int getItemCount() {
        return mCandidateCrossVote.size();
    }

    public void ballotFull(boolean isfull) {
        this.ballotIsFull = isfull;
    }

    public void removeCandidateMarks() {
        for (CandidateCrossVote cand : mCandidateCrossVote) {
            cand.setMark(false);
        }
        this.notifyDataSetChanged();
    }

    public void attachListener(boolean attach) {
        attachListener = attach;
        this.notifyDataSetChanged();
    }

    public void markCandidatesFrom(String partyElectionID) {
        // problem is that we will iterate over 123 or so candidates. find a better way to find them.
        for (int i = 0; i < mCandidateCrossVote.size(); i++) {
            if (mCandidateCrossVote.get(i).getPartyElectionId().equals(partyElectionID)) {
                mCandidateCrossVote.get(i).setMark(true);
            }
        }
        this.notifyDataSetChanged();
    }

    public void candidatesWithPreviousMarks(HashMap<String, CandidateCrossVote> withMarks){
        if(withMarks ==null || withMarks.size()==0){
            return;
        }

        for (int j = 0; j < mCandidateCrossVote.size(); j++) {
            String candidateId = mCandidateCrossVote.get(j).getCandidatePrefElecId();
            CandidateCrossVote candidate = withMarks.get(candidateId);
            if (candidate != null) {
//                mCandidateCrossVote.get(j).setMismatch(true); //enable touch
                mCandidateCrossVote.get(j).setMark(true); // remove mark
            }
        }
        this.notifyDataSetChanged();
    }

    public void allMatch() {
        for (int j = 0; j < mCandidateCrossVote.size(); j++) {
            mCandidateCrossVote.get(j).setMismatch(false);
        }
        this.notifyDataSetChanged();
    }

    public void unLockMisMatches(HashMap<String, CandidateCrossVote> mismatches) {
        for (int j = 0; j < mCandidateCrossVote.size(); j++) {
            String candidateId = mCandidateCrossVote.get(j).getCandidatePrefElecId();
            CandidateCrossVote candidate = mismatches.get(candidateId);
            if (candidate != null) {
                mCandidateCrossVote.get(j).setMismatch(true); //enable touch
                mCandidateCrossVote.get(j).setMark(false); // remove mark
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public TextView itemDetail;
        public TextView itemTitle;
        public ImageView itemImage;
        public final ImageView redImage;
        public final ImageView blueImage;
        public ImageView warning;
        public final ImageView locked;



        public CandidateCrossVote mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            blueImage = (ImageView) mView.findViewById(R.id.bluecross_image);
            redImage = (ImageView) mView.findViewById(R.id.redcross_image);
            itemImage = (ImageView) mView.findViewById(R.id.item_image);
            itemTitle = (TextView) mView.findViewById(R.id.item_title);
            itemDetail = (TextView) mView.findViewById(R.id.item_detail);
            warning = (ImageView) mView.findViewById(R.id.warning_image);
            locked = (ImageView) mView.findViewById(R.id.fade);
        }
    }


}
