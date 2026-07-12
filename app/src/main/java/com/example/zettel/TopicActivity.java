package com.example.zettel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
    private ArrayList<Topic> topics;
    private AppDatabase db;
    private Spinner spinnerLevel;
    private String currentSelectedLevel = "A1"; // Храним выбранный уровень

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Темы обучения");
        }

        rvTopics = findViewById(R.id.rvTopics);
        rvTopics.setLayoutManager(new LinearLayoutManager(this));
        spinnerLevel = findViewById(R.id.spinnerLevel);

        db = AppDatabase.getInstance(this);

        // Настройка выпадающего списка уровней сложности
        String[] levels = {"A1", "A2", "B1", "B2"};
        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(levelAdapter);

        // Слушатель выбора уровня сложности
        spinnerLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSelectedLevel = levels[position];
                if (adapter != null) {
                    // Передаем новый уровень в адаптер для пересчета шкал прогресса
                    adapter.setSelectedLevel(currentSelectedLevel);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Восстановление списка тем после переворота экрана
        if (savedInstanceState != null && savedInstanceState.containsKey("SAVED_TOPICS")) {
            topics = (ArrayList<Topic>) savedInstanceState.getSerializable("SAVED_TOPICS");
            if (savedInstanceState.containsKey("SAVED_LEVEL")) {
                currentSelectedLevel = savedInstanceState.getString("SAVED_LEVEL");
                // Устанавливаем сохраненный уровень в Спиннер
                for (int i = 0; i < levels.length; i++) {
                    if (levels[i].equals(currentSelectedLevel)) {
                        spinnerLevel.setSelection(i);
                        break;
                    }
                }
            }
            // Если восстановили из savedInstanceState, просто инициализируем адаптер
            adapter = new TopicAdapter(topics, topic -> { /* ваша логика клика */ });
            adapter.setSelectedLevel(currentSelectedLevel);
            rvTopics.setAdapter(adapter);
        } else {
            // ИСПРАВЛЕНО: Вместо захардкоженного списка загружаем все темы динамически из Room
            loadDynamicTopics();
        }

        // В самом конце onCreate динамические кнопки Назад и Теста (если они там прописаны)


        // Логика кнопки добавления нового слова (добавили поле выбора уровня)
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

            List<String> categoryNames = new ArrayList<>();
            for (Topic t : topics) {
                if (!t.getId().equals("review")) {
                    categoryNames.add(t.getNameRu());
                }
            }

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                    TopicActivity.this, android.R.layout.simple_spinner_item, categoryNames);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(spinnerAdapter);

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
                    // Создаем новое слово с привязкой к ТЕКУЩЕМУ выбранному уровню сложности!
                    Word newWord = new Word(german, russian, category, currentSelectedLevel, "");
                    new Thread(() -> {
                        db.wordDao().insertWord(newWord);
                        runOnUiThread(() -> {
                            Toast.makeText(TopicActivity.this, "Слово успешно добавлено!", Toast.LENGTH_SHORT).show();
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

        // --- ДИНАМИЧЕСКИЙ ВЫВОД КНОПКИ НАЗАД ВНИЗУ ЭКРАНА ---
        try {
            // Находим кнопку добавления слов, которая у вас точно есть на экране
            View anchorView = findViewById(R.id.btnAddWord);

            if (anchorView != null) {
                android.view.ViewGroup parentLayout = (android.view.ViewGroup) anchorView.getParent();

                if (parentLayout != null) {
                    // Создаем синюю кнопку программно
                    Button btnBackToMenu = new Button(this);

                    // Настраиваем текст и цвета
                    btnBackToMenu.setText("В главное меню");
                    btnBackToMenu.setTextColor(android.graphics.Color.WHITE);
                    btnBackToMenu.setBackgroundColor(android.graphics.Color.parseColor("#3F51B5")); // Синий цвет

                    // Задаем размеры и отступы (совместимо со всеми экранами)
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 24, 0, 0); // Отступ сверху, чтобы кнопки не слипались
                    btnBackToMenu.setLayoutParams(params);

                    // Вычисляем позицию кнопки добавления слов и вставляем синюю кнопку строго ПОД ней
                    int index = parentLayout.indexOfChild(anchorView);
                    parentLayout.addView(btnBackToMenu, index + 1);

                    // Логика нажатия: закрываем экран тем и безопасно возвращаемся на StartActivity
                    btnBackToMenu.setOnClickListener(vBack -> finish());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ----------------------------------------------------
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Принудительно пересканируем базу данных при каждом возврате на экран
        loadDynamicTopics();
        if (adapter != null) {
            adapter.setSelectedLevel(currentSelectedLevel); // Принудительно обновляем уровень при возврате
        } else {
            adapter = new TopicAdapter(topics, topic -> {
                if (topic.getId().equals("review")) {
                    Intent intent = new Intent(TopicActivity.this, ReviewActivity.class);
                    // Передаем уровень в экран общего списка, если понадобится фильтр
                    intent.putExtra("SELECTED_LEVEL", currentSelectedLevel);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(TopicActivity.this, MainActivity.class);
                    intent.putExtra("TOPIC_ID", topic.getId());
                    intent.putExtra("TOPIC_NAME_RU", topic.getNameRu());
                    intent.putExtra("SELECTED_LEVEL", currentSelectedLevel); // ТЕПЕРЬ ПЕРЕДАЕМ И ВЫБРАННЫЙ УРОВЕНЬ!
                    startActivity(intent);
                }
            });
            adapter.setSelectedLevel(currentSelectedLevel);
            rvTopics.setAdapter(adapter);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("SAVED_TOPICS", topics);
        outState.putString("SAVED_LEVEL", currentSelectedLevel);
    }
    // ОБРАБОТЧИК НАЖАТИЯ СИСТЕМНОЙ СТРЕЛКИ НАЗАД (В Action Bar)
    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Закрываем экран тем и безопасно возвращаемся в главное меню (StartActivity)
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void loadDynamicTopics() {
        // Если список еще не создан, создаем его. Если создан — очищаем от старых данных
        if (topics == null) {
            topics = new ArrayList<>();
        } else {
            topics.clear();
        }

        // Запускаем фоновый поток для чтения категорий из базы данных Room
        new Thread(() -> {
            // 1. Извлекаем из БД список всех уникальных текстовых категорий
            List<String> categoriesFromDb = db.wordDao().getUniqueCategories();

            if (categoriesFromDb != null) {
                for (String categoryName : categoriesFromDb) {
                    // Защита: пропускаем системное "Повторение", если оно вдруг затесалось в базу слов
                    if (categoryName.equalsIgnoreCase("Повторение слов") || categoryName.equalsIgnoreCase("Все слова (повторение)")) {
                        continue;
                    }

                    String topicId = categoryName.toLowerCase().trim();

                    String nameDe = "Kategorie: " + categoryName;

                    // Добавляем тему в список, используя конструктор с 3 параметрами
                    topics.add(new Topic(topicId, categoryName, nameDe));
                }
            }

            // 2. Всегда добавляем системную кнопку "Повторение слов" в самый конец списка
            topics.add(new Topic("review", "Повторение слов", "Wiederholung"));

            // 3. Обновляем адаптер в главном UI-потоке
            runOnUiThread(() -> {
                if (adapter == null) {
                    adapter = new TopicAdapter(topics, topic -> {
                        // Ваша логика клика по теме (Intent на карточки)
                        if (topic.getId().equals("review")) {
                            Intent intent = new Intent(TopicActivity.this, ReviewActivity.class);
                            intent.putExtra("DIFFICULTY_LEVEL", currentSelectedLevel);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(TopicActivity.this, ReviewActivity.class);
                            intent.putExtra("DIFFICULTY_LEVEL", currentSelectedLevel);
                            intent.putExtra("SELECTED_CATEGORY", topic.getNameRu());
                            startActivity(intent);
                        }
                    });
                    adapter.setSelectedLevel(currentSelectedLevel);
                    rvTopics.setAdapter(adapter);
                } else {
                    // Если адаптер уже был создан, просто принудительно обновляем списки и шкалы
                    adapter.setSelectedLevel(currentSelectedLevel);
                    adapter.notifyDataSetChanged();
                }
            });
        }).start();
    }

}


