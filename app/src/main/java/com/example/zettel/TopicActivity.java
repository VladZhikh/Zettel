package com.example.zettel; // Укажите ваш пакет

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        RecyclerView rvTopics = findViewById(R.id.rvTopics);
        rvTopics.setLayoutManager(new LinearLayoutManager(this));


        // Наш тестовый список тем
        List<Topic> topics = new ArrayList<>();
        topics.add(new Topic("transport", "Транспорт", "Der Transport"));
        topics.add(new Topic("food", "Еда", "Das Essen"));
        topics.add(new Topic("shopping", "Покупки", "Das Einkaufen"));
        topics.add(new Topic("family", "Семья", "Die Familie"));
        // Добавляем новую псевдо-тему для повторения выученного:
        topics.add(new Topic("review", "Повторение слов", "Wiederholung"));

        // Инициализируем адаптер и передаем логику клика
        TopicAdapter adapter = new TopicAdapter(topics, topic -> {
            // Переходим в MainActivity (экран с карточкой)
            Intent intent = new Intent(TopicActivity.this, MainActivity.class);
            // Передаем ID темы, чтобы MainActivity знала, какие слова показывать
            intent.putExtra("TOPIC_ID", topic.getId());
            startActivity(intent);
        });

        rvTopics.setAdapter(adapter);
// Внутри onCreate в TopicActivity.java:

        Button btnAddWord = findViewById(R.id.btnAddWord);
        AppDatabase db = AppDatabase.getInstance(this); // Получаем доступ к базе

        btnAddWord.setOnClickListener(v -> {
            // Создаем диалоговое окно
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(TopicActivity.this);
            builder.setTitle("Добавить новое слово");

            // Инфлейтим разметку диалога
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
                    // Создаем новый объект Word. Проверьте ваш конструктор Word!
                    // Передаем (Немецкий, Русский, Категория, Уровень, Варианты ложных ответов)
                    Word newWord = new Word(german, russian, category, "A1", "");

                    // Сохраняем в базу данных в фоновом потоке, так как Room запрещает делать это в главном
                    new Thread(() -> {
                        db.wordDao().insertWord(newWord);
                        runOnUiThread(() -> Toast.makeText(TopicActivity.this, "Слово успешно добавлено!", Toast.LENGTH_SHORT).show());
                    }).start();

                } else {
                    Toast.makeText(TopicActivity.this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Отмена", null);
            builder.create().show();
        });
    }
}

