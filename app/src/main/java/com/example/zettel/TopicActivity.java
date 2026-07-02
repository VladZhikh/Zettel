package com.example.zettel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TopicActivity extends AppCompatActivity {

    private RecyclerView rvTopics;
    private TopicAdapter adapter;
    private List<Topic> topics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        rvTopics = findViewById(R.id.rvTopics);
        rvTopics.setLayoutManager(new LinearLayoutManager(this));

        // Инициализируем список тем один раз
        topics = new ArrayList<>();
        topics.add(new Topic("transport", "Транспорт", "Der Transport"));
        topics.add(new Topic("food", "Еда", "Das Essen"));
        topics.add(new Topic("shopping", "Покупки", "Das Einkaufen"));
        topics.add(new Topic("family", "Семья", "Die Familie"));
        topics.add(new Topic("review", "Повторение слов", "Wiederholung"));

        // Логика кнопки добавления слова (остается без изменений)
        Button btnAddWord = findViewById(R.id.btnAddWord);
        AppDatabase db = AppDatabase.getInstance(this);

        btnAddWord.setOnClickListener(v -> {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(TopicActivity.this);
            builder.setTitle("Добавить новое слово");
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_word, null);
            builder.setView(dialogView);

            android.widget.EditText etGerman = dialogView.findViewById(R.id.etGerman);
            android.widget.EditText etRussian = dialogView.findViewById(R.id.etRussian);
            android.widget.EditText etCategory = dialogView.findViewById(R.id.etCategory);

            builder.setPositiveButton("Сохранить", (dialog, which) -> {
                String german = etGerman.getText().toString().trim();
                String russian = etRussian.getText().toString().trim();
                String category = etCategory.getText().toString().trim();

                if (!german.isEmpty() && !russian.isEmpty() && !category.isEmpty()) {
                    Word newWord = new Word(german, russian, category, "A1", "");
                    new Thread(() -> {
                        db.wordDao().insertWord(newWord);
                        runOnUiThread(() -> {
                            Toast.makeText(TopicActivity.this, "Слово успешно добавлено!", Toast.LENGTH_SHORT).show();
                            // Принудительно обновляем список после добавления слова
                            onResume();
                        });
                    }).start();
                } else {
                    Toast.makeText(TopicActivity.this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Отмена", null);
            builder.create().show();
        });
    }

    // НОВЫЙ МЕТОД: Срабатывает КАЖДЫЙ РАЗ, когда мы возвращаемся на этот экран
    @Override
    protected void onResume() {
        super.onResume();

        // Пересоздаем адаптер, чтобы он заново пересчитал прогресс из базы данных
        adapter = new TopicAdapter(topics, topic -> {
            Intent intent = new Intent(TopicActivity.this, MainActivity.class);
            intent.putExtra("TOPIC_ID", topic.getId());
            startActivity(intent);
        });

        rvTopics.setAdapter(adapter);
    }
}
