package com.iremember.subscriber.iremembersubscriber.Fragments;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.iremember.subscriber.iremembersubscriber.R;
import com.iremember.subscriber.iremembersubscriber.Utils.PreferenceUtils;

import java.lang.reflect.Field;

public class SettingsMusicFragment extends Fragment {

    private Context mContext;
    private View mFragment;
    private MediaPlayer mMediaPlayer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFragment = inflater.inflate(R.layout.fragment_settings_music, container, false);
        mContext = mFragment.getContext();
        displaySongList();
        setCurrentSong();
        return mFragment;
    }

    @Override
    public void onStop() {
        super.onStop();
        stopMediaPlayer();
    }

    private void displaySongList() {
        TableLayout songList = mFragment.findViewById(R.id.song_table);
        View.OnClickListener playListener = new PlayClickListener();
        View.OnClickListener stopListener = new StopClickListener();
        View.OnClickListener selectListener = new SelectClickListener();
        Field[] songs = R.raw.class.getFields();

        for (int i = 0; i < songs.length; i++){
            View songItem = createSongItem(songs[i].getName(), playListener, stopListener, selectListener);
            View songSeparator = createSongSeparator();
            songList.addView(songItem);
            songList.addView(songSeparator);
        }
    }

    private View createSongSeparator() {
        View vSeparator = new View(mContext);
        vSeparator.setBackgroundColor(mContext.getResources().getColor(R.color.dark));
        vSeparator.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return vSeparator;
    }

    private View createSongItem(String songTitle,
                                View.OnClickListener playListener,
                                View.OnClickListener stopListener,
                                View.OnClickListener selectListener) {

        TextView vTitle = new TextView(mContext);
        vTitle.setTextSize(mContext.getResources().getDimension(R.dimen.textsize_small));
        vTitle.setText(songTitle);

        TextView vPlay = new TextView(mContext);
        vPlay.setOnClickListener(playListener);
        vPlay.setGravity(Gravity.CENTER);
        vPlay.setTextColor(mContext.getResources().getColor(R.color.orange));
        vPlay.setTextSize(mContext.getResources().getDimension(R.dimen.textsize_small));
        vPlay.setText("Spela");
        vPlay.setTag(songTitle);

        TextView vStop = new TextView(mContext);
        vStop.setOnClickListener(stopListener);
        vStop.setGravity(Gravity.CENTER);
        vStop.setTextColor(mContext.getResources().getColor(R.color.stop));
        vStop.setTextSize(mContext.getResources().getDimension(R.dimen.textsize_small));
        vStop.setText("Stop");
        vStop.setTag(songTitle);

        TextView vSelect = new TextView(mContext);
        vSelect.setOnClickListener(selectListener);
        vSelect.setGravity(Gravity.CENTER);
        vSelect.setTextColor(mContext.getResources().getColor(R.color.start));
        vSelect.setTextSize(mContext.getResources().getDimension(R.dimen.textsize_small));
        vSelect.setText("Välj");
        vSelect.setTag(songTitle);

        int rowPadding = (int) mContext.getResources().getDimension(R.dimen.padding_medium);

        TableRow row = new TableRow(mContext);
        row.setPadding(0, rowPadding, 0, rowPadding);
        row.addView(vTitle);
        row.addView(vPlay);
        row.addView(vStop);
        row.addView(vSelect);
        return row;
    }

    private void setCurrentSong() {
        String song = PreferenceUtils.readSongTitle(mContext);
        ((TextView) mFragment.findViewById(R.id.selected_song)).setText(song);
    }

    private void stopMediaPlayer() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void startMediaPlayer(String songTitle) {
        int source = mContext.getResources().getIdentifier(songTitle, "raw", mContext.getPackageName());
        mMediaPlayer = MediaPlayer.create(mContext, source);
        mMediaPlayer.start();
    }

    private class PlayClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            stopMediaPlayer();
            startMediaPlayer(view.getTag().toString());
        }
    }

    private class StopClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            stopMediaPlayer();
        }
    }

    private class SelectClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            PreferenceUtils.writeSongTitle(mContext, view.getTag().toString());
            PreferenceUtils.showUserConfirmation(mContext);
            setCurrentSong();
        }
    }
}
