package com.certoclav.app.monitor;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.certoclav.app.R;
import com.certoclav.app.database.ProtocolEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ProtocolEntry} and makes a call to the
 */
public class MyProtocolCertoTraceAdapter extends RecyclerView.Adapter<MyProtocolCertoTraceAdapter.ViewHolder> {

    private List<Pair<Integer, String>> mValues;

    public MyProtocolCertoTraceAdapter(String content) {
        mValues = new ArrayList<>();
        for (String item : content.split("\n")) {
            if (item.contains(" x ") && item.split(" x ").length == 2) {
                mValues.add(new Pair<>(Integer.valueOf(item.split(" x ")[0]), item.split(" x ")[1]));
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_protocol_certo_trace, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.count.setText(holder.mItem.first.toString());
        holder.name.setText(holder.mItem.second);

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView count;
        public final TextView name;
        public Pair<Integer, String> mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            count = (TextView) view.findViewById(R.id.count);
            name = (TextView) view.findViewById(R.id.name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + name.getText() + "'";
        }
    }

}
