package com.rbraithwaite.sleepapp.ui.session_archive;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.session_archive.data.UISleepSessionData;

import org.w3c.dom.Text;

import java.util.List;

public class SessionArchiveRecyclerViewAdapter extends RecyclerView.Adapter<SessionArchiveRecyclerViewAdapter.ViewHolder>
{
    // todo
    //  consider retaining a cache of retrieved data points, to speed things up?

    private static final String TAG = "RecyclerViewAdapter";

    // inspired by https://stackoverflow.com/a/45336315
    public interface LifeCycleOwnerProvider
    {
        public LifecycleOwner getLifeCycleOwner();
    }

    private LifeCycleOwnerProvider mLifeCycleOwnerProvider;
    private SessionArchiveFragmentViewModel mViewModel;
    private LiveData<List<Integer>> mSleepSessionDataIds;

    public SessionArchiveRecyclerViewAdapter(
            SessionArchiveFragmentViewModel viewModel,
            LifeCycleOwnerProvider lifeCycleOwnerProvider)
    {
        mViewModel = viewModel;
        mLifeCycleOwnerProvider = lifeCycleOwnerProvider;

        mSleepSessionDataIds = mViewModel.getAllSleepSessionDataIds();
        mSleepSessionDataIds.observe(
                mLifeCycleOwnerProvider.getLifeCycleOwner(),
                new Observer<List<Integer>>() {
                    @Override
                    public void onChanged(List<Integer> integers) {
                        notifyDataSetChanged();
                    }
                });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.session_archive_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        bindToViewModel(holder, position);
    }

    @Override
    public int getItemCount()
    {
        List<Integer> ids = mSleepSessionDataIds.getValue();
        if (ids == null) { return 0; }

        int itemCount = ids.size();
        Log.d(TAG, "getItemCount: itemCount is " + itemCount);

        return itemCount;
    }

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView startTime;
        TextView stopTime;
        TextView duration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.startTime = itemView.findViewById(R.id.session_archive_list_item_start_VALUE);
            this.stopTime = itemView.findViewById(R.id.session_archive_list_item_stop_VALUE);
            this.duration = itemView.findViewById(R.id.session_archive_list_item_duration_VALUE);
        }
    }

    private void bindToViewModel(final ViewHolder viewHolder, final int position)
    {
        LiveData<UISleepSessionData> uiSleepSessionData = mViewModel.getSleepSessionData(
                mSleepSessionDataIds.getValue().get(position));
        LifecycleOwner lifecycleOwner = mLifeCycleOwnerProvider.getLifeCycleOwner();

        // I thought the new Observer here might leak memory and cause duplicate updates, but
        // getSleepSession data provides a new livedata instance so I should be ok?
        uiSleepSessionData.observe(
                lifecycleOwner,
                new Observer<UISleepSessionData>() {
                    @Override
                    public void onChanged(UISleepSessionData uiSleepSessionData) {
                        viewHolder.startTime.setText(uiSleepSessionData.startTime);
                        viewHolder.stopTime.setText(uiSleepSessionData.endTime);
                        viewHolder.duration.setText(uiSleepSessionData.sessionDuration);
                        notifyItemChanged(position);
                    }
                });
    }
}
