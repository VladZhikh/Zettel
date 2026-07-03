package com.example.zettel;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {

    private final List<Word> words;
    private final OnSpeakClickListener speakClickListener;

    public interface OnSpeakClickListener {
        void onSpeakClick(String germanWord);
    }

    public WordAdapter(List<Word> words, OnSpeakClickListener speakClickListener) {
        this.words = words;
        this.speakClickListener = speakClickListener;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = words.get(position);
        holder.tvGerman.setText(word.getGermanWord());
        holder.tvRussian.setText(word.getRussianTranslation());

        // ЦВЕТОВАЯ ИНДИКАЦИЯ: Если слово выучено, красим карточку в зеленый
        if (word.isLearned()) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#E8F5E9")); // Светло-зеленый
            holder.tvGerman.setTextColor(Color.parseColor("#2E7D32")); // Темно-зеленый текст
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.tvGerman.setTextColor(Color.parseColor("#212529"));
        }

        // Клик по динамику передает текст слова наружу в Activity для озвучки
        holder.btnSpeak.setOnClickListener(v -> speakClickListener.onSpeakClick(word.getGermanWord()));
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvGerman, tvRussian;
        ImageButton btnSpeak;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.wordCardView);
            tvGerman = itemView.findViewById(R.id.tvGerman);
            tvRussian = itemView.findViewById(R.id.tvRussian);
            btnSpeak = itemView.findViewById(R.id.btnSpeak);
        }
    }
}

