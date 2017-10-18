package com.certoclav.app.monitor;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.certoclav.app.R;
import com.certoclav.app.database.ProtocolEntry;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ProtocolEntry} and makes a call to the
 * TODO: Replace the implementation with code for your data type.
 */
public class MyProtocolEntryRecyclerViewAdapter extends RecyclerView.Adapter<MyProtocolEntryRecyclerViewAdapter.ViewHolder> {

    private final List<ProtocolEntry> mValues;

    public MyProtocolEntryRecyclerViewAdapter(List<ProtocolEntry> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_protocolentry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.time.setText(holder.mItem.getTimeStampWithMin());
        holder.temperature.setText(mValues.get(position).getTemperature() + "");
        holder.mediaTemperature.setText(mValues.get(position).getMediaTemperature() + "");
        holder.pressure.setText(String.format("%.2f", mValues.get(position).getPressure()));

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView time;
        public final TextView temperature;
        public final TextView mediaTemperature;
        public final TextView pressure;
        public ProtocolEntry mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            time = (TextView) view.findViewById(R.id.time);
            temperature = (TextView) view.findViewById(R.id.temperature);
            mediaTemperature = (TextView) view.findViewById(R.id.mediaTemperature);
            pressure = (TextView) view.findViewById(R.id.pressure);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + temperature.getText() + "'";
        }
    }
}
