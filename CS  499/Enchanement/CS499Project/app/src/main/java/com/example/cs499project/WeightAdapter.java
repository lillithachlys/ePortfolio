package com.example.cs499project;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.WeightViewHolder> {
    public interface OnWeightDeleteListener {
        void onDeleteWeight(Weight weight);
    }
    private List<Weight> weightList;
    private final OnWeightDeleteListener deleteListener;
    public WeightAdapter(
            List<Weight> weightList,
            OnWeightDeleteListener deleteListener) {

        this.weightList = weightList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public WeightViewHolder onCreateViewHolder(
            @NonNull
            ViewGroup parent,
            int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weight_item,
                        parent,
                        false);
        return new WeightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull
            WeightViewHolder holder,
            int position) {
        Weight weight = weightList.get(position);
        holder.dayText.setText(weight.day);
        holder.weightText.setText(weight.weightValue);
        Button deleteButton =
                holder.itemView.findViewById(
                        R.id.deleteWeightButton
                );
        deleteButton.setOnClickListener(v ->
                deleteListener.onDeleteWeight(weight)
        );
    }

    @Override
    public int getItemCount() {
        return weightList.size();
    }

    // update list with new weights
    public void updateWeights(List<Weight> newWeights) {
        weightList = newWeights;
        notifyDataSetChanged();
    }

    public static class WeightViewHolder extends RecyclerView.ViewHolder {
        TextView dayText;
        TextView weightText;

        public WeightViewHolder(
                @NonNull
                View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.dayText);
            weightText = itemView.findViewById(R.id.weightText);
        }
    }
}