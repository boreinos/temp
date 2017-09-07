package com.afilon.mayor.v11.fragments;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.model.Candidate;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

@SuppressLint("DefaultLocale")
public class NoCheckboxArrayAdapter extends ArrayAdapter<Candidate> {

	private final Activity context;
	private final List<Candidate> candidateList;
	private DisplayImageOptions imageLoaderOptions;
	protected ImageLoader imageLoader;

	public NoCheckboxArrayAdapter(Activity context,
								  List<Candidate> candidatesList) {
		super(context, R.layout.custom_nocheckbox_list_item, candidatesList);
		this.context = context;
		this.candidateList = candidatesList;

		imageLoader = ImageLoader.getInstance();
	}

	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

	static class ViewHolder {
		public TextView candidateName;
		public TextView candidateNumber;
		public ImageView imageView;
		public TextView partyName;
		public TextView numberOfVotes_tv;
		public TextView totalMarks;

	}
	private String formatFloat(float value){
		return String.format(Locale.US,"%.3f",value);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		imageLoaderOptions = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.default_user)
				.showImageForEmptyUri(R.drawable.default_user)
				.showImageOnFail(R.drawable.default_user).cacheInMemory(true)
				.cacheOnDisc(true).considerExifParams(true).build();

		if (convertView == null) {

			LayoutInflater inflator = context.getLayoutInflater();
			convertView = inflator.inflate(
					R.layout.custom_nocheckbox_list_item, null);

			final ViewHolder viewHolder = new ViewHolder();

			viewHolder.candidateName = (TextView) convertView
					.findViewById(R.id.txt_name);
			viewHolder.candidateNumber = (TextView) convertView
					.findViewById(R.id.txt_candidate_nmb);
			viewHolder.imageView = (ImageView) convertView
					.findViewById(R.id.image_icon);
			viewHolder.partyName = (TextView) convertView
					.findViewById(R.id.txt_party_name);
			viewHolder.numberOfVotes_tv = (TextView) convertView
					.findViewById(R.id.numberOfvotes_tv);

			viewHolder.totalMarks = (TextView) convertView.findViewById(R.id.totalMarks);

/*			viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					ImageView imageView = (ImageView) v;

					Candidate getPosition = (Candidate) imageView.getTag();
					Utilities ah = new Utilities(context);
					ah.createCustomToast(getPosition.getCandidate_name(),
							"is selected");

				}
			});*/

			convertView.setTag(viewHolder);
			viewHolder.imageView.setTag(candidateList.get(position));

		} else {

			((ViewHolder) convertView.getTag()).imageView.setTag(candidateList
					.get(position));
		}

		ViewHolder viewHolder = (ViewHolder) convertView.getTag();
//		imageLoader.displayImage(
//				"assets://drawable/"
//						+ candidateList.get(position)
//								.getCandidatePreferentialElectionID() + ".png",
//				viewHolder.imageView, imageLoaderOptions, animateFirstListener);

		//CARLOS: passing party name as part of the file name
		// since the app is showing the wrong party picture when using PreferentialElectionID.
		if (candidateList.get(position).getCandidateID().equals("0")) {
			imageLoader.displayImage(
					"assets://drawable/" + candidateList.get(position)
							.getCandidate_name().toLowerCase()
							+  ".png",
					viewHolder.imageView, imageLoaderOptions, animateFirstListener);
		}else {
			imageLoader.displayImage(
					"assets://drawable/"
							+ candidateList.get(position)
							.getCandidatePreferentialElectionID() + ".png",
					viewHolder.imageView, imageLoaderOptions, animateFirstListener);
		}


		if (candidateList.get(position).getCandidateID().equals("0")) {
			viewHolder.candidateName.setText("Bandera "
					+ candidateList.get(position).getCandidate_name());
			viewHolder.candidateNumber.setText("");
			viewHolder.partyName.setText("");
		} else {
			viewHolder.candidateName.setText(candidateList.get(position)
					.getCandidate_name());
			viewHolder.candidateNumber.setText("Candidato Numero: "
					+ candidateList.get(position).getCandidateID());
		}
		viewHolder.partyName.setText("");
		viewHolder.numberOfVotes_tv.setText(formatFloat(candidateList.get(
				position).getVotesNumber()));

		//CARLOS: 2016-09-19
		if (candidateList.get(position).getCandidateID().equals("0")) {
			viewHolder.totalMarks.setText("");
		} else {
			viewHolder.totalMarks.setText(String.valueOf(candidateList.get(position).getMarcas()));
		}

//		Log.e("CANDIDATE ADPT",Float.toString(candidateList.get(
//				position).getVotesNumber()) );
		return convertView;
	}

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