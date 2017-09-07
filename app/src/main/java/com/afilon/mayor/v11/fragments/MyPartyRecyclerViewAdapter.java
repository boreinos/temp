package com.afilon.mayor.v11.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.fragments.PartyFragment.OnListFragmentInteractionListener;
import com.afilon.mayor.v11.model.Candidate;
import com.afilon.mayor.v11.utils.Consts;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MyPartyRecyclerViewAdapter extends RecyclerView.Adapter<MyPartyRecyclerViewAdapter.ViewHolder> {

    private final List<Candidate> mValues;
    private final OnListFragmentInteractionListener mListener;
    private DisplayImageOptions imageLoaderOptions;
    protected ImageLoader imageLoader;
    private String title;


    public MyPartyRecyclerViewAdapter(List<Candidate> items
            ,OnListFragmentInteractionListener listener
            ,String title) {

        mValues = items;
        mListener = listener;
        imageLoader = ImageLoader.getInstance();
        this.title=title;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.candidate_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.candidateName.setText(mValues.get(position).getCandidate_name());


        holder.candidateRank.setText(String.valueOf(mValues.get(position).getCandidate_order()));
        //---------------------------------------------------------------------------------------
        switch (title){
            case Consts.CROSS_VOTE_SUMMARY:
                holder.candidateVotes.setText(String.format(Locale.US,"%.3f",mValues.get(position).getCrossVote()));
                holder.typeVote.setText("VOTOS CRUSADO: ");

                holder.candidateMarksLabel.setText("MARCAS: ");
                //holder.candidateMarks.setText("0");
                holder.candidateMarks.setText(String.valueOf(mValues.get(position).getMarcas()));

                imageLoaderOptions = new DisplayImageOptions.Builder()
                        .showImageOnLoading(R.drawable.default_user)
                        .showImageForEmptyUri(R.drawable.default_user)
                        .showImageOnFail(R.drawable.default_user).cacheInMemory(true)
                        .cacheOnDisc(true).considerExifParams(true).build();
                imageLoader.displayImage(
                        "assets://drawable/"
                                + mValues.get(position)
                                .getCandidatePreferentialElectionID() + ".png",
                        holder.imageView, imageLoaderOptions, animateFirstListener);
                break;
            case Consts.TOTAL_VOTE_SUMMARY:
                holder.candidateVotes.setText(String.format(Locale.US,"%.3f",mValues.get(position).getVotesNumber()));
                holder.imageView.setVisibility(View.GONE);
                holder.typeVote.setText("VOTOS: ");

                if(Consts.LOCALE.contains("HON")){
                    holder.candidateVotes.setVisibility(View.GONE);
                    holder.typeVote.setVisibility(View.GONE);
                }

                holder.candidateMarksLabel.setText("MARCAS: ");
//                holder.candidateMarks.setText("0");
//                Random rn = new Random();
//                holder.candidateMarks.setText(String.valueOf(rn.nextInt(5 - 1 + 1) + 1));
                holder.candidateMarks.setText(String.valueOf(mValues.get(position).getMarksQty()));
                break;
        }



        //---------------------------------------------------------------------------------------
        if((position&1)!=0){
            //holder.mView.setBackgroundColor(Color.parseColor("#DADCF2"));
            holder.mView.setBackgroundColor(Color.parseColor("#E9EAF7"));
        }else{
            holder.mView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
        //---------------------------------------------------------------------------------------

//        holder.mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (null != mListener) {
//                    // Notify the active callbacks interface (the activity, if the
//                    // fragment is attached to one) that an item has been selected.
//                    mListener.onListFragmentInteraction(holder.mItem);
//                }
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView candidateName;
        public final TextView candidateVotes;
        public final TextView candidateRank;
        public final TextView typeVote;
        public final TextView candidateMarks;
        public final TextView candidateMarksLabel;
        public ImageView imageView;


        public Candidate mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            candidateName = (TextView) view.findViewById(R.id.candidateName);
            candidateVotes = (TextView) view.findViewById(R.id.candidateVotes);
            candidateRank = (TextView)view.findViewById(R.id.candidateRank);
            imageView = (ImageView)view.findViewById(R.id.image_icon);
            typeVote = (TextView)view.findViewById(R.id.votesLabel);
            candidateMarks = (TextView)view.findViewById(R.id.candidateMarks);
            candidateMarksLabel = (TextView)view.findViewById(R.id.marksLabel);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + candidateVotes.getText() + "'";
        }
    }


    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    public static class AnimateFirstDisplayListener extends
            SimpleImageLoadingListener {
        static final List<String> displayedImages = Collections
                .synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view,
                                      Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }
}
