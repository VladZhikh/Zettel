package com.example.zettel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> {

    private final List<Topic> topics;
    private final OnTopicClickListener clickListener;
    private String selectedLevel = "A1"; // Уровень по умолчанию

    public interface OnTopicClickListener {
        void onTopicClick(Topic topic);
    }

    public TopicAdapter(List<Topic> topics, OnTopicClickListener clickListener) {
        this.topics = topics;
        this.clickListener = clickListener;
    }

    // НОВЫЙ МЕТОД: Позволяет менять уровень из Activity и обновлять шкалы
    public void setSelectedLevel(String level) {
        this.selectedLevel = level;
        notifyDataSetChanged();
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

        AppDatabase db = AppDatabase.getInstance(holder.itemView.getContext());

        if (topic.getId().equals("review")) {
            new Thread(() -> {
                // В режиме повторения считаем слова только ВЫБРАННОГО уровня
                int total = 0;
                int learned = 0;
                for (Word w : db.wordDao().getAllWords()) {
                    if (w.getLevel().equals(selectedLevel)) {
                        total++;
                        if (w.isLearned()) learned++;
                    }
                }

                int percent = (total > 0) ? (learned * 100 / total) : 0;

                int finalLearned = learned;
                int finalTotal = total;
                holder.itemView.post(() -> {
                    holder.tvProgressPercent.setText(percent + "% (" + finalLearned + "/" + finalTotal + ")");
                    holder.topicProgressBar.setProgress(percent);
                });
            }).start();
        } else {
            new Thread(() -> {
                String categoryName = "Транспорт";
                if (topic.getId().equals("food")) categoryName = "Еда";
                else if (topic.getId().equals("shopping")) categoryName = "Покупки";
                else if (topic.getId().equals("family")) categoryName = "Семья";

                // Передаем ТЕКУЩИЙ ВЫБРАННЫЙ уровень сложности в DAO
                int totalWords = db.wordDao().getTotalWordsCount(categoryName, selectedLevel);
                int learnedWords = db.wordDao().getLearnedWordsCount(categoryName, selectedLevel);

                int progressPercent = (totalWords > 0) ? (learnedWords * 100 / totalWords) : 0;

                holder.itemView.post(() -> {
                    holder.tvProgressPercent.setText(progressPercent + "%");
                    holder.topicProgressBar.setProgress(progressPercent);
                });
            }).start();
        }

        holder.itemView.setOnClickListener(v -> clickListener.onTopicClick(topic));
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    static class TopicViewHolder extends RecyclerView.ViewHolder {
        TextView tvNameRu, tvNameDe, tvProgressPercent;
        ProgressBar topicProgressBar;

        public TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameRu = itemView.findViewById(R.id.tvNameRu);
            tvNameDe = itemView.findViewById(R.id.tvNameDe);
            tvProgressPercent = itemView.findViewById(R.id.tvProgressPercent);
            topicProgressBar = itemView.findViewById(R.id.topicProgressBar);
        }
    }
}

