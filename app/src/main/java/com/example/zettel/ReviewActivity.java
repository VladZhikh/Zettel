package com.example.zettel;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReviewActivity extends AppCompatActivity {

    private RecyclerView rvReviewWords;
    private WordAdapter adapter;
    private AppDatabase db;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Кнопка Назад
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvReviewWords = findViewById(R.id.rvReviewWords);
        rvReviewWords.setLayoutManager(new LinearLayoutManager(this));

        db = AppDatabase.getInstance(this);

        // Инициализируем голосовой движок
        initTextToSpeech();

        // 1. Считываем выбранный уровень сложности из Стартового окна
        String selectedDifficulty = null;
        if (getIntent() != null && getIntent().hasExtra("DIFFICULTY_LEVEL")) {
            selectedDifficulty = getIntent().getStringExtra("DIFFICULTY_LEVEL");
        }

        final String finalDifficulty = selectedDifficulty;

        // 2. Загружаем и фильтруем слова в фоновом потоке
        new Thread(() -> {
            List<Word> allWords = db.wordDao().getAllWordsForReview();
            List<Word> filteredWords = new ArrayList<>();

            if (allWords != null) {
                for (Word word : allWords) {
                    // Безопасно получаем уровень слова из базы данных (защита от null)
                    String wordLevel = (word.getLevel() != null) ? word.getLevel().trim() : "";

                    // Если уровень не был передан (например, зашли из другого места) — показываем всё.
                    // Если уровень передан — проверяем на точное совпадение (без учета регистра букв)
                    if (finalDifficulty == null || wordLevel.equalsIgnoreCase(finalDifficulty.trim())) {
                        filteredWords.add(word);
                    }
                }
            }

            // Передаем отфильтрованный список в адаптер на главном потоке интерфейса
            runOnUiThread(() -> {
                adapter = new WordAdapter(filteredWords, germanWord -> {
                    // Логика клика по динамику: озвучиваем слово
                    speakGerman(germanWord);
                });
                rvReviewWords.setAdapter(adapter);

                // Если слов этого уровня вдруг не нашлось в базе — покажем уведомление
                if (filteredWords.isEmpty() && finalDifficulty != null) {
                    Toast.makeText(ReviewActivity.this, "Пока нет слов для уровня " + finalDifficulty, Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }


    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.GERMAN);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(ReviewActivity.this, "Немецкий язык не поддерживается", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ReviewActivity.this, "Ошибка запуска TTS", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void speakGerman(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}

