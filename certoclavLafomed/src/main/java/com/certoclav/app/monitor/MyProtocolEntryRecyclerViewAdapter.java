package com.certoclav.app.monitor;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.certoclav.app.R;
import com.certoclav.app.database.Protocol;
import com.certoclav.app.database.ProtocolEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ProtocolEntry} and makes a call to the
 */
public class MyProtocolEntryRecyclerViewAdapter extends RecyclerView.Adapter<MyProtocolEntryRecyclerViewAdapter.ViewHolder> {

    private final List<ProtocolEntry> mValues;
    private Protocol protocol;

    public MyProtocolEntryRecyclerViewAdapter(List<ProtocolEntry> items, Protocol protocol) {
        mValues = new ArrayList<>();
        long lastdate = 0;
        for (ProtocolEntry entry : items) {
            if (lastdate < (entry.getTimestamp().getTime() - 19 * 1000)) {
                mValues.add(entry);
                lastdate = entry.getTimestamp().getTime();
            }
        }
        this.protocol = protocol;
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
        holder.time.setText(holder.mItem.getFormatedTime());
        holder.temperature.setText(roundFloat(mValues.get(position).getTemperature()) + "");

        holder.mediaTemperature.setVisibility(protocol.isContByFlexProbe1() ? View.VISIBLE : View.GONE);
        holder.mediaTemperature2.setVisibility(protocol.isContByFlexProbe2() ? View.VISIBLE : View.GONE);
        if (protocol.isContByFlexProbe2())
            holder.mediaTemperature2.setText(roundFloat(mValues.get(position).getMediaTemperature2()) + "");
        if (protocol.isContByFlexProbe1())
            holder.mediaTemperature.setText(roundFloat(mValues.get(position).getMediaTemperature()) + "");
//        holder.pressure.setText(roundFloat(mValues.get(position).getPressure()).toString());
        holder.pressure.setText(String.format(Locale.US, "%.2f", mValues.get(position).getPressure()));

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
        public final TextView mediaTemperature2;
        public final TextView pressure;
        public ProtocolEntry mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            time = (TextView) view.findViewById(R.id.time);
            temperature = (TextView) view.findViewById(R.id.temperature);
            mediaTemperature = (TextView) view.findViewById(R.id.mediaTemperature);
            mediaTemperature2 = (TextView) view.findViewById(R.id.mediaTemperature2);
            pressure = (TextView) view.findViewById(R.id.pressure);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + temperature.getText() + "'";
        }
    }


    private Double roundFloat(float f) {
        int tempnumber = (int) (f * 100);
        Double roundedfloat = (double) ((double) tempnumber / 100.0);
        return roundedfloat;
    }


}
