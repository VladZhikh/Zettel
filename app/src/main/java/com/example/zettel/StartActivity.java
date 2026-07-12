package com.example.zettel;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class StartActivity extends AppCompatActivity {
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start); // Наша новая разметка
        db = AppDatabase.getInstance(this);

       // Наполняем базу из JSON сразу при запуске приложения в фоновом потоке
        new Thread(this::preloadWordsIfNeeded).start();

        Button btnLevelA1 = findViewById(R.id.btnLevelA1);
        Button btnLevelA2 = findViewById(R.id.btnLevelA2);
        Button btnLevelB1 = findViewById(R.id.btnLevelB1);
        Button btnLevelB2 = findViewById(R.id.btnLevelB2);
        Button btnOpenTopics = findViewById(R.id.btnOpenTopics);
        Button btnStartQuiz = findViewById(R.id.btnStartQuiz);
        Button btnStartArticlesGame = findViewById(R.id.btnStartArticlesGame);


        // Кнопки уровней ведем на ReviewActivity (или куда вам захочется позже)
        btnLevelA1.setOnClickListener(v -> startReviewSession("A1"));
        btnLevelA2.setOnClickListener(v -> startReviewSession("A2"));
        btnLevelB1.setOnClickListener(v -> startReviewSession("B1"));
        btnLevelB2.setOnClickListener(v -> startReviewSession("B2"));

        // Нижняя кнопка открывает экран ТЕМ (TopicActivity)
        btnOpenTopics.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, TopicActivity.class);
            startActivity(intent);
        });
        // Кнопка запуска тестирования с главного экрана (по уровням)
        btnStartQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, QuizActivity.class);
            intent.putExtra("MODE", "BY_LEVEL");
            // Передаем текущий уровень. Если у вас на главном экране есть переменная выбранного уровня,
            // подставьте её вместо "A1" (например: currentSelectedLevel)
            intent.putExtra("DIFFICULTY_LEVEL", "A1");
            startActivity(intent);
        });
        btnStartArticlesGame.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, ArticlesGameActivity.class);
            startActivity(intent);
        });


    }

    private void startReviewSession(String difficulty) {
        Intent intent = new Intent(StartActivity.this, ReviewActivity.class);
        intent.putExtra("DIFFICULTY_LEVEL", difficulty);
        startActivity(intent);
    }
    private void preloadWordsIfNeeded() {
        try {
            WordDao dao = db.wordDao();

            // 1. Открываем и читаем наш файл words.json из папки assets
            java.io.InputStream inputStream = getAssets().open("words.json");
            java.io.InputStreamReader reader = new java.io.InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8);

            // 2. Распаковываем JSON в список объектов Java
            com.google.gson.Gson gson = new com.google.gson.Gson();
            java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<Word>>(){}.getType();
            List<Word> wordsFromJson = gson.fromJson(reader, listType);

            reader.close();

            if (wordsFromJson != null && !wordsFromJson.isEmpty()) {
                // 3. Вытаскиваем слова, которые УЖЕ РЕАЛЬНО ЕСТЬ в базе данных, чтобы избежать дубликатов
                List<Word> currentWordsInDb = dao.getAllWords();

                // 4. Собираем немецкие слова в HashSet для моментального поиска в памяти
                java.util.HashSet<String> existingGermanKeys = new java.util.HashSet<>();
                if (currentWordsInDb != null) {
                    for (Word w : currentWordsInDb) {
                        if (w.getGermanWord() != null) {
                            existingGermanKeys.add(w.getGermanWord().trim().toLowerCase());
                        }
                    }
                }

                // 4. Оборачиваем в транзакцию Room для мгновенной пакетной вставки
                db.runInTransaction(() -> {
                    for (Word word : wordsFromJson) {
                        if (word.getGermanWord() != null) {
                            String jsonWordKey = word.getGermanWord().trim().toLowerCase();

                            if (!existingGermanKeys.contains(jsonWordKey)) {
                                dao.insertWord(word);
                            }
                        }
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("Zettel_JSON", "Ошибка при импорте JSON на старте: " + e.getMessage());
        }
    }

}

