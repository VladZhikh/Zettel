package com.example.zettel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> {

    private final List<Topic> topics;
    private final OnTopicClickListener clickListener;

    // Интерфейс для обработки нажатия в Activity
    public interface OnTopicClickListener {
        void onTopicClick(Topic topic);
    }

    public TopicAdapter(List<Topic> topics, OnTopicClickListener clickListener) {
        this.topics = topics;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topic, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        Topic topic = topics.get(position);
        holder.tvNameRu.setText(topic.getNameRu());
        holder.tvNameDe.setText(topic.getNameDe());

        holder.itemView.setOnClickListener(v -> clickListener.onTopicClick(topic));
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    static class TopicViewHolder extends RecyclerView.ViewHolder {
        TextView tvNameRu, tvNameDe;

        public TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameRu = itemView.findViewById(R.id.tvNameRu);
            tvNameDe = itemView.findViewById(R.id.tvNameDe);
        }
    }
}