package com.example.zettel; // Укажите ваш пакет

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

        // Получаем экземпляр базы данных
        AppDatabase db = AppDatabase.getInstance(holder.itemView.getContext());

        // Псевдо-тема "Повторение" не имеет фиксированной категории, для нее считаем общий прогресс всей базы
        if (topic.getId().equals("review")) {
            new Thread(() -> {
                int total = db.wordDao().getAllWords().size();
                int learned = 0;
                for (Word w : db.wordDao().getAllWords()) {
                    if (w.isLearned()) learned++;
                }

                int percent = (total > 0) ? (learned * 100 / total) : 0;

                // Передаем результат в главный UI-поток
                int finalLearned = learned;
                int finalTotal = total;
                holder.itemView.post(() -> {
                    holder.tvProgressPercent.setText(percent + "% (" + finalLearned + "/" + finalTotal + ")");
                    holder.topicProgressBar.setProgress(percent);
                });
            }).start();
        } else {
            // Для обычных категорий ("Транспорт", "Еда" и т.д.)
            new Thread(() -> {
                // Преобразуем ID темы в имя категории для БД, как в MainActivity
                String categoryName = "Транспорт";
                if (topic.getId().equals("food")) categoryName = "Еда";
                else if (topic.getId().equals("shopping")) categoryName = "Покупки";
                else if (topic.getId().equals("family")) categoryName = "Семья";

                // Тянем цифры из наших новых методов в WordDao
                int totalWords = db.wordDao().getTotalWordsCount(categoryName);
                int learnedWords = db.wordDao().getLearnedWordsCount(categoryName);

                // Высчитываем процент
                int progressPercent = (totalWords > 0) ? (learnedWords * 100 / totalWords) : 0;

                // Передаем изменения на экран
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
