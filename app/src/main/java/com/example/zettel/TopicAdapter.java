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

    // Позволяет менять уровень из Activity и обновлять шкалы
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
            // РЕЖИМ 1: Общее повторение слов (считаем по всей базе для выбранного уровня)
            new Thread(() -> {
                int total = 0;
                int learned = 0;
                List<Word> allWords = db.wordDao().getAllWords();
                if (allWords != null) {
                    for (Word w : allWords) {
                        if (w.getLevel() != null && w.getLevel().equalsIgnoreCase(selectedLevel)) {
                            total++;
                            if (w.isLearned()) learned++;
                        }
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
            // РЕЖИМ 2: УНИВЕРСАЛЬНЫЙ (Для абсолютно любой стандартной или новой темы!)
            new Thread(() -> {
                // ИСПРАВЛЕНО: Никакого хардкода! Берём имя категории прямо из темы
                String categoryName = topic.getNameRu();

                // Делаем запросы в базу данных, используя наши новые методы из WordDao
                int totalWords = db.wordDao().getCountAllWordsInTopic(categoryName, selectedLevel);
                int learnedWords = db.wordDao().getCountLearnedWordsInTopic(categoryName, selectedLevel);

                int progressPercent = (totalWords > 0) ? (learnedWords * 100 / totalWords) : 0;

                holder.itemView.post(() -> {
                    // Добавим красивый вывод счетчика слов (например, "45% (5/11)") как на повторении
                    holder.tvProgressPercent.setText(progressPercent + "% (" + learnedWords + "/" + totalWords + ")");
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

