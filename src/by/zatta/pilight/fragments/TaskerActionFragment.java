/******************************************************************************************
 * 
 * Copyright (C) 2013 Zatta
 * 
 * This file is part of pilight for android.
 * 
 * pilight for android is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * 
 * pilight for android is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along 
 * with pilightfor android.
 * If not, see <http://www.gnu.org/licenses/>
 * 
 * Copyright (c) 2013 pilight project
 ********************************************************************************************/

package by.zatta.pilight.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import com.devadvance.circularseekbar.CircularSeekBar;
import by.zatta.pilight.R;
import by.zatta.pilight.model.DeviceEntry;
import by.zatta.pilight.model.SettingEntry;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardGridView;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.CardView;

public class TaskerActionFragment extends BaseFragment {

	private static final String TAG = "TaskerActionFragment";
	private boolean forceList;
	static CardArrayAdapter mCardArrayAdapter;
	static CardGridArrayAdapter mCardGridArrayAdapter;
	Bundle localeBundle;
	CardView resultCardView;
	static List<DeviceEntry> mDevices = new ArrayList<DeviceEntry>();

	public static TaskerActionFragment newInstance(Bundle localeBundle, List<DeviceEntry> list) {
		TaskerActionFragment f = new TaskerActionFragment();
		Bundle args = new Bundle();
		args.putParcelableArrayList("config", (ArrayList<? extends Parcelable>) list);
		args.putBundle("localeBundle", localeBundle);
		f.setArguments(args);
		return f;
	}

	@Override
	public int getTitleResourceId() {
		return R.string.title_list_base;
	}

	@Override
	public String getName() {
		return "piTasker";
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDevices = getArguments().getParcelableArrayList("config");
		localeBundle = getArguments().getBundle("localeBundle");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		forceList = prefs.getBoolean("forceList", false);
		setRetainInstance(false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (forceList)
			return inflater.inflate(R.layout.devicelist_layout, container, false);
		else return inflater.inflate(R.layout.devicegrid_layout, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		resultCardView = (CardView) getActivity().findViewById(R.id.cvResult);
		

		if (savedInstanceState == null) {
			if (localeBundle != null) {
				String[] mExtra = localeBundle.getStringArray("Extra");
				String action = "";
				for (String s : mExtra){
					action = action + s;
				}
				makeAction(action);
			}
		}
		initCards();
	}

	private void initCards() {
		Log.v(TAG, "initCards");
		ArrayList<Card> cards = new ArrayList<Card>();
		for (DeviceEntry device : mDevices) {
			Card card = null;

			if (device.getType() == 1)
				card = new ListSwitchCard(getActivity().getApplicationContext(), device);
			else if (device.getType() == 2)
				card = new ListDimmerCard(getActivity().getApplicationContext(), device);
			else if (device.getType() == 4)
				card = new ListRelayCard(getActivity().getApplicationContext(), device);
			else if (device.getType() == 6) card = new ListContactCard(getActivity().getApplicationContext(), device);

			for (SettingEntry sentry : device.getSettings()) { // don't show readonly devices, it's useless
				if (sentry.getKey().equals("sett_readonly") && sentry.getValue().equals("1")) card = null;
			}

			if (!(card == null)) {
				cards.add(card);
			}
		}

		if (forceList) {
			mCardArrayAdapter = new CardArrayAdapter(getActivity(), cards);
			mCardArrayAdapter.setInnerViewTypeCount(4);

			CardListView listView = (CardListView) getActivity().findViewById(R.id.carddemo_list_base1);
			if (listView != null) {
				listView.setAdapter(mCardArrayAdapter);
			}
		} else {
			mCardGridArrayAdapter = new CardGridArrayAdapter(getActivity(), cards);
			mCardGridArrayAdapter.setInnerViewTypeCount(2);

			CardGridView gridView = (CardGridView) getActivity().findViewById(R.id.carddemo_grid_base);
			if (gridView != null) {
				gridView.setAdapter(mCardGridArrayAdapter);
			}
		}

	}

	private void makeAction(String action) {
		//TODO make a actionButton for accepting the desired action appear when this is called. 
		resultCardView.setVisibility(View.VISIBLE);
		Card card = new ResultCard(getActivity().getApplicationContext(), action);
		if (resultCardView.getCard()!=null)
			resultCardView.replaceCard(card);
		else 
			resultCardView.setCard(card);
		
	}

	/*
	 * CARDS START FROM HERE: ListDimmerCard ******************************************************************************
	 */
	public class ListDimmerCard extends Card {
		protected String who;
		protected boolean mState;
		protected boolean readwrite = true;
		protected int mSeekValue;
		protected int minSeekValue;
		protected int maxSeekValue;
		protected String mTitleHeader;
		protected String mTitleMain;
		protected CircularSeekBar mSeekBar;
		protected ToggleButton mToggle;
		protected TextView mTitleMainView;
		protected CompoundButton.OnCheckedChangeListener toggleListener = new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				String action = "\"state\":\"off\"";
				if (isChecked) action = "\"state\":\"on\"";
				mState = isChecked;
				makeAction(who + action);
				// deviceListListener.deviceListListener(ConnectionService.MSG_SWITCH_DEVICE, who + action);
			}
		};
		protected CircularSeekBar.OnCircularSeekBarChangeListener seekListener = new CircularSeekBar.OnCircularSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(CircularSeekBar seekBar) {
				mTitleMainView.setText(mTitleMain);
				mToggle.setText(Integer.toString(mSeekValue));
				String action = "\"state\":\"on\",\"values\":{\"dimlevel\":" + String.valueOf(mSeekValue) + "}";
				makeAction(who + action);
				// deviceListListener.deviceListListener(ConnectionService.MSG_SWITCH_DEVICE, who + action);
			}

			@Override
			public void onProgressChanged(CircularSeekBar seekBar, int progress, boolean fromUser) {
				mSeekValue = progress + minSeekValue;
				mToggle.setText(Integer.toString(mSeekValue));
				mTitleMainView.setText("Dimmer setting: " + Integer.toString(mSeekValue));
				mToggle.getBackground().setAlpha((int) ((float) (mSeekValue + 1) / (maxSeekValue + 1) * 80) + 70);
			}
		};

		public ListDimmerCard(Context context, DeviceEntry entry) {
			super(context, R.layout.dimmercard_inner);
			who = "\"device\":\"" + entry.getNameID() + "\",\"location\":\"" + entry.getLocationID() + "\",";
			for (SettingEntry sentry : entry.getSettings()) {
				if (sentry.getKey().equals("name")) mTitleHeader = sentry.getValue();
				if (sentry.getKey().equals("locationName")) mTitleMain = sentry.getValue();
				if (sentry.getKey().equals("dimlevel")) mSeekValue = Integer.valueOf(sentry.getValue());
				if (sentry.getKey().equals("state")) {
					if (sentry.getValue().equals("on")) mState = true;
					if (sentry.getValue().equals("off")) mState = false;
				}
				if (sentry.getKey().equals("sett_min")) minSeekValue = Integer.valueOf(sentry.getValue());
				if (sentry.getKey().equals("sett_max")) maxSeekValue = Integer.valueOf(sentry.getValue());
				if (sentry.getKey().equals("sett_readonly") && sentry.getValue().equals("1")) readwrite = false;
			}
			init();
		}

		private void init() {
			setTitle(mTitleMain);
			// Create a CardHeader
			CardHeader header = new CardHeader(getContext());
			header.setTitle(mTitleHeader);
			addCardHeader(header);
		}

		@Override
		public void setupInnerViewElements(ViewGroup parent, View view) {
			// Retrieve elements
			mTitleMainView = (TextView) parent.findViewById(R.id.card_main_inner_simple_title);
			mSeekBar = (CircularSeekBar) parent.findViewById(R.id.circularSeekBar1);
			mToggle = (ToggleButton) parent.findViewById(R.id.card_inner_tb);

			if (mTitleMainView != null) mTitleMainView.setText(mTitleMain);

			if (mToggle != null) {
				mToggle.setChecked(mState);
				mToggle.setOnCheckedChangeListener(toggleListener);
				mToggle.setText(Integer.toString(mSeekValue));
				mToggle.getBackground().setAlpha((int) ((float) (mSeekValue + 1) / (maxSeekValue + 1) * 80) + 70);
			}

			if (mSeekBar != null) {
				mSeekBar.setMax(maxSeekValue - minSeekValue);
				mSeekBar.setProgress(mSeekValue - minSeekValue);
				mSeekBar.setOnSeekBarChangeListener(seekListener);
			}
			mToggle.setClickable(readwrite);
			mSeekBar.setClickable(readwrite);
			if (!readwrite) mToggle.setAlpha((float) 0.5);
			if (!readwrite) mSeekBar.setAlpha((float) 0.5);
		}

		public void update(DeviceEntry entry) {
			mToggle.setOnCheckedChangeListener(null);
			mSeekBar.setOnSeekBarChangeListener(null);
			for (SettingEntry sentry : entry.getSettings()) {
				if (sentry.getKey().equals("state")) {
					if (sentry.getValue().equals("on")) mState = true;
					if (sentry.getValue().equals("off")) mState = false;
					mToggle.setChecked(mState);
				}
				if (sentry.getKey().equals("dimlevel")) {
					mSeekValue = Integer.valueOf(sentry.getValue());
					this.mSeekBar.setProgress(mSeekValue - minSeekValue);
				}
			}
			mSeekBar.setOnSeekBarChangeListener(seekListener);
			mToggle.setText(Integer.toString(mSeekValue));
			mToggle.setOnCheckedChangeListener(toggleListener);
		}
	}

	/*
	 * LISTSWITCHCARD ****************************************************************************************************
	 */
	public class ListSwitchCard extends Card {
		protected String who;
		protected String mValue;
		protected String mTitleHeader;
		protected String mTitleMain;
		protected TextView mTitleMainView;
		protected ToggleButton mToggle;
		protected boolean mState;
		protected boolean readwrite = true;
		protected CompoundButton.OnCheckedChangeListener toggleListener = new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				String action = "\"state\":\"off\"";
				if (isChecked) action = "\"state\":\"on\"";
				mState = isChecked;
				makeAction(who + action);
				// deviceListListener.deviceListListener(ConnectionService.MSG_SWITCH_DEVICE, who + action);
			}
		};

		public ListSwitchCard(Context context, DeviceEntry entry) {
			super(context, R.layout.switchcard_inner);
			who = "\"device\":\"" + entry.getNameID() + "\",\"location\":\"" + entry.getLocationID() + "\",";
			for (SettingEntry sentry : entry.getSettings()) {
				if (sentry.getKey().equals("name")) mTitleHeader = sentry.getValue();
				if (sentry.getKey().equals("locationName")) mTitleMain = sentry.getValue();
				if (sentry.getKey().equals("state")) {
					if (sentry.getValue().equals("on")) mState = true;
					if (sentry.getValue().equals("off")) mState = false;
				}
				if (sentry.getKey().equals("sett_readonly") && sentry.getValue().equals("1")) readwrite = false;
			}
			init();
		}

		private void init() {
			setTitle(mTitleMain);
			// Create a CardHeader
			CardHeader header = new CardHeader(getContext());
			header.setTitle(mTitleHeader);
			addCardHeader(header);
		}

		@Override
		public void setupInnerViewElements(ViewGroup parent, View view) {
			// Retrieve elements
			mTitleMainView = (TextView) parent.findViewById(R.id.card_main_inner_simple_title);
			mToggle = (ToggleButton) parent.findViewById(R.id.card_inner_tb);

			if (mTitleMainView != null) mTitleMainView.setText(mTitleMain);
			if (mToggle != null) {
				mToggle.setChecked(mState);
				mToggle.setOnCheckedChangeListener(toggleListener);
			}
			mToggle.setClickable(readwrite);
			if (!readwrite) mToggle.setAlpha((float) 0.5);

		}

		public void update(DeviceEntry entry) {
			mToggle.setOnCheckedChangeListener(null);
			for (SettingEntry sentry : entry.getSettings()) {
				if (sentry.getKey().equals("state")) {
					if (sentry.getValue().equals("on")) mState = true;
					if (sentry.getValue().equals("off")) mState = false;
					mToggle.setChecked(mState);
				}
			}
			mToggle.setOnCheckedChangeListener(toggleListener);
		}
	}

	/*
	 * LISTRELAYCARD ****************************************************************************************************
	 */
	public class ListRelayCard extends Card {
		protected String who;
		protected String mValue;
		protected String mTitleHeader;
		protected String mTitleMain;
		protected TextView mTitleMainView;
		protected ToggleButton mToggle;
		protected boolean mState;
		protected boolean readwrite = true;

		protected CompoundButton.OnCheckedChangeListener toggleListener = new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				String action = "\"state\":\"off\"";
				if (isChecked) action = "\"state\":\"on\"";
				mState = isChecked;
				makeAction(who + action);
				// deviceListListener.deviceListListener(ConnectionService.MSG_SWITCH_DEVICE, who + action);
			}
		};

		public ListRelayCard(Context context, DeviceEntry entry) {
			super(context, R.layout.relaycard_inner);
			who = "\"device\":\"" + entry.getNameID() + "\",\"location\":\"" + entry.getLocationID() + "\",";
			for (SettingEntry sentry : entry.getSettings()) {
				if (sentry.getKey().equals("name")) mTitleHeader = sentry.getValue();
				if (sentry.getKey().equals("locationName")) mTitleMain = sentry.getValue();
				if (sentry.getKey().equals("state")) {
					if (sentry.getValue().equals("on")) mState = true;
					if (sentry.getValue().equals("off")) mState = false;
				}
				if (sentry.getKey().equals("sett_readonly") && sentry.getValue().equals("1")) readwrite = false;
			}
			init();
		}

		private void init() {
			setTitle(mTitleMain);
			// Create a CardHeader
			CardHeader header = new CardHeader(getContext());
			header.setTitle(mTitleHeader);
			addCardHeader(header);
		}

		@Override
		public void setupInnerViewElements(ViewGroup parent, View view) {
			// Retrieve elements
			mTitleMainView = (TextView) parent.findViewById(R.id.card_main_inner_simple_title);
			mToggle = (ToggleButton) parent.findViewById(R.id.card_inner_tb);

			if (mTitleMainView != null) mTitleMainView.setText(mTitleMain);
			if (mToggle != null) {
				mToggle.setChecked(mState);
				mToggle.setOnCheckedChangeListener(toggleListener);
			}
			mToggle.setClickable(readwrite);
			if (!readwrite) mToggle.setAlpha((float) 0.5);
		}

		public void update(DeviceEntry entry) {
			mToggle.setOnCheckedChangeListener(null);
			for (SettingEntry sentry : entry.getSettings()) {
				if (sentry.getKey().equals("state")) {
					if (sentry.getValue().equals("on")) mState = true;
					if (sentry.getValue().equals("off")) mState = false;
					mToggle.setChecked(mState);
				}
			}
			mToggle.setOnCheckedChangeListener(toggleListener);
		}
	}

	/*
	 * LISTCONTACTCARD ****************************************************************************************************
	 */
	public class ListContactCard extends Card {
		protected String who;
		protected String mValue;
		protected String mTitleHeader;
		protected String mTitleMain;
		protected TextView mTitleMainView;
		protected ToggleButton mToggle;
		protected boolean mState;
		protected boolean readwrite = true;

		protected CompoundButton.OnCheckedChangeListener toggleListener = new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				String action = "\"state\":\"closed\"";
				if (isChecked) action = "\"state\":\"opened\"";
				mState = isChecked;
				makeAction(who + action);
				// deviceListListener.deviceListListener(ConnectionService.MSG_SWITCH_DEVICE, who + action);
			}
		};

		public ListContactCard(Context context, DeviceEntry entry) {
			super(context, R.layout.contactcard_inner);
			who = "\"device\":\"" + entry.getNameID() + "\",\"location\":\"" + entry.getLocationID() + "\",";
			for (SettingEntry sentry : entry.getSettings()) {
				if (sentry.getKey().equals("name")) mTitleHeader = sentry.getValue();
				if (sentry.getKey().equals("locationName")) mTitleMain = sentry.getValue();
				if (sentry.getKey().equals("state")) {
					if (sentry.getValue().equals("opened")) mState = true;
					if (sentry.getValue().equals("closed")) mState = false;
				}
				if (sentry.getKey().equals("sett_readonly") && sentry.getValue().equals("1")) readwrite = false;
			}
			init();
		}

		private void init() {
			setTitle(mTitleMain);
			// Create a CardHeader
			CardHeader header = new CardHeader(getContext());
			header.setTitle(mTitleHeader);
			addCardHeader(header);
		}

		@Override
		public void setupInnerViewElements(ViewGroup parent, View view) {
			// Retrieve elements
			mTitleMainView = (TextView) parent.findViewById(R.id.card_main_inner_simple_title);
			mToggle = (ToggleButton) parent.findViewById(R.id.card_inner_tb);

			if (mTitleMainView != null) mTitleMainView.setText(mTitleMain);
			if (mToggle != null) {
				mToggle.setChecked(mState);
				mToggle.setOnCheckedChangeListener(toggleListener);
			}
			mToggle.setClickable(readwrite);
			// if (!readwrite) mToggle.setAlpha((float) 0.5); // I don't think we need this for the contacts
		}

		public void update(DeviceEntry entry) {
			mToggle.setOnCheckedChangeListener(null);
			for (SettingEntry sentry : entry.getSettings()) {
				if (sentry.getKey().equals("state")) {
					if (sentry.getValue().equals("opened")) mState = true;
					if (sentry.getValue().equals("closed")) mState = false;
					mToggle.setChecked(mState);
				}
			}
			mToggle.setOnCheckedChangeListener(toggleListener);
		}
	}

	/*
	 * RESULTCARD ****************************************************************************************************
	 */
	public class ResultCard extends Card {

		public ResultCard(Context context, String who) {
			//TODO make the card look pretty
			super(context, R.layout.resultcard_inner);
			setTitle(who);
			// Create a CardHeader
			CardHeader header = new CardHeader(getContext());
			header.setTitle("Action");
			addCardHeader(header);
		}
	}
}
