package com.example.zettel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TopicActivity extends AppCompatActivity {

    private RecyclerView rvTopics;
    private TopicAdapter adapter;
    private ArrayList<Topic> topics; // Привели к ArrayList для сохранения данных
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        rvTopics = findViewById(R.id.rvTopics);
        rvTopics.setLayoutManager(new LinearLayoutManager(this));

        db = AppDatabase.getInstance(this);

        // ПРОВЕРКА: Восстанавливаем список тем после поворота экрана
        if (savedInstanceState != null && savedInstanceState.containsKey("SAVED_TOPICS")) {
            topics = (ArrayList<Topic>) savedInstanceState.getSerializable("SAVED_TOPICS");
        } else {
            // Самый первый чистый запуск приложения
            topics = new ArrayList<>();
            topics.add(new Topic("transport", "Транспорт", "Der Transport"));
            topics.add(new Topic("food", "Еда", "Das Essen"));
            topics.add(new Topic("shopping", "Покупки", "Das Einkaufen"));
            topics.add(new Topic("family", "Семья", "Die Familie"));
            topics.add(new Topic("review", "Повторение слов", "Wiederholung"));
        }

        // Логика кнопки добавления слова и создания новой категории
        Button btnAddWord = findViewById(R.id.btnAddWord);
        btnAddWord.setOnClickListener(v -> {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(TopicActivity.this);
            builder.setTitle("Добавить новое слово");

            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_word, null);
            builder.setView(dialogView);

            android.widget.EditText etGerman = dialogView.findViewById(R.id.etGerman);
            android.widget.EditText etRussian = dialogView.findViewById(R.id.etRussian);
            android.widget.Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
            android.widget.ImageButton btnAddNewCategory = dialogView.findViewById(R.id.btnAddNewCategory);

            // Собираем список названий категорий для Спиннера (кроме Повторения)
            List<String> categoryNames = new ArrayList<>();
            for (Topic t : topics) {
                if (!t.getId().equals("review")) {
                    categoryNames.add(t.getNameRu());
                }
            }

            android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(
                    TopicActivity.this, android.R.layout.simple_spinner_item, categoryNames);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(spinnerAdapter);

            // Создание новой темы на лету
            btnAddNewCategory.setOnClickListener(vCat -> {
                androidx.appcompat.app.AlertDialog.Builder catBuilder = new androidx.appcompat.app.AlertDialog.Builder(TopicActivity.this);
                catBuilder.setTitle("Создать новую тему");

                android.widget.EditText etNewCat = new android.widget.EditText(TopicActivity.this);
                etNewCat.setHint("Название темы на русском");
                catBuilder.setView(etNewCat);

                catBuilder.setPositiveButton("Создать", (dialogCat, whichCat) -> {
                    String newCatName = etNewCat.getText().toString().trim();
                    if (!newCatName.isEmpty() && !categoryNames.contains(newCatName)) {
                        String newId = newCatName.toLowerCase().replaceAll("\\s+", "_");

                        // Вставляем новую тему перед "Повторением"
                        topics.add(topics.size() - 1, new Topic(newId, newCatName, ""));

                        categoryNames.add(newCatName);
                        spinnerAdapter.notifyDataSetChanged();
                        spinnerCategory.setSelection(categoryNames.size() - 1);

                        if (adapter != null) adapter.notifyDataSetChanged();
                    }
                });
                catBuilder.setNegativeButton("Отмена", null);
                catBuilder.create().show();
            });

            builder.setPositiveButton("Сохранить", (dialog, which) -> {
                String german = etGerman.getText().toString().trim();
                String russian = etRussian.getText().toString().trim();
                String category = (spinnerCategory.getSelectedItem() != null) ? spinnerCategory.getSelectedItem().toString() : "";

                if (!german.isEmpty() && !russian.isEmpty() && !category.isEmpty()) {
                    Word newWord = new Word(german, russian, category, "A1", "");
                    new Thread(() -> {
                        db.wordDao().insertWord(newWord);
                        runOnUiThread(() -> {
                            Toast.makeText(TopicActivity.this, "Слово успешно добавлено!", Toast.LENGTH_SHORT).show();
                            onResume(); // Пересчитываем прогресс шкал
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

    @Override
    protected void onResume() {
        super.onResume();

        // Всегда передаем актуальные ID и Имя темы при переходе
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            adapter = new TopicAdapter(topics, topic -> {
                Intent intent = new Intent(TopicActivity.this, MainActivity.class);
                intent.putExtra("TOPIC_ID", topic.getId());
                intent.putExtra("TOPIC_NAME_RU", topic.getNameRu()); // Важно для фильтрации новых тем
                startActivity(intent);
            });
            rvTopics.setAdapter(adapter);
        }
    }

    // Сохранение списка тем перед уничтожением экрана (поворот)
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("SAVED_TOPICS", topics);
    }
}

