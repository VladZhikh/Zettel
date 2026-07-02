package com.example.zettel;

import android.os.Bundle;
import android.speech.tts.TextToSpeech; // Добавили импорт TTS
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zettel.databinding.ActivityMainBinding;
import java.util.List;
import java.util.Locale; // Добавили импорт локали

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AppDatabase db;
    private List<Word> wordList;
    private int currentWordIndex = 0;

    // 1. Объявляем переменную для движка озвучки
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Инициализируем Text-to-Speech на немецком языке
        initTextToSpeech();

        db = AppDatabase.getInstance(this);
        preloadWordsIfNeeded();

        // 3. Получаем ID или название темы, переданное из TopicActivity
        String selectedTopicId = getIntent().getStringExtra("TOPIC_ID");

        // Переводим ID в понятное базе данных название категории
        String categoryName = "Транспорт"; // По умолчанию
        if (selectedTopicId != null) {
            if (selectedTopicId.equals("food")) categoryName = "Еда";
            else if (selectedTopicId.equals("shopping")) categoryName = "Покупки";
            else if (selectedTopicId.equals("family")) categoryName = "Семья";
        }

        // 4. Загружаем слова динамически на основе выбранной категории
        wordList = db.wordDao().getWordsForLesson("A1", categoryName);

        displayCurrentWord();

        // 5. Логика клика по карточке (показ перевода + ОЗВУЧКА)
        binding.wordCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wordList != null && !wordList.isEmpty() && currentWordIndex < wordList.size()) {
                    binding.tvTranslation.setVisibility(View.VISIBLE);

                    // Озвучиваем текущее немецкое слово при открытии карточки
                    String wordToSpeak = wordList.get(currentWordIndex).getGermanWord();
                    speakGerman(wordToSpeak);
                }
            }
        });

        // 6. Логика кнопки "Знаю"
        binding.btnKnow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wordList == null || wordList.isEmpty()) return;

                Word currentWord = wordList.get(currentWordIndex);
                currentWord.setLearned(true);
                db.wordDao().updateWord(currentWord);

                currentWordIndex++;
                if (currentWordIndex < wordList.size()) {
                    displayCurrentWord();
                } else {
                    binding.tvGermanWord.setText("Wunderbar!");
                    binding.tvTranslation.setText("Вы выучили все слова в этой теме!");
                    binding.tvTranslation.setVisibility(View.VISIBLE);
                    binding.btnKnow.setEnabled(false);
                }
            }
        });
    }

    // Метод для инициализации TTS движка
    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.GERMAN); // Ставим немецкий
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(MainActivity.this, "Немецкий язык не поддерживается устройством", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Ошибка запуска голосового движка", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Метод для воспроизведения звука
    private void speakGerman(String text) {
        if (textToSpeech != null) {
            // Очищаем предыдущую очередь звуков и произносим новое слово
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void displayCurrentWord() {
        if (wordList != null && !wordList.isEmpty() && currentWordIndex < wordList.size()) {
            Word word = wordList.get(currentWordIndex);
            binding.tvGermanWord.setText(word.getGermanWord());
            binding.tvTranslation.setText(word.getRussianTranslation());
            binding.tvTranslation.setVisibility(View.INVISIBLE);
        } else {
            binding.tvGermanWord.setText("Нет слов");
            binding.tvTranslation.setText("В этой теме пока пусто");
            binding.tvTranslation.setVisibility(View.VISIBLE);
        }
    }

    private void preloadWordsIfNeeded() {
        if (db.wordDao().getAllWords().isEmpty()) {
            db.wordDao().insertWord(new Word("Das Auto", "Автомобиль", "Транспорт", "A1", "Самолет, Корабль, Поезд"));
            db.wordDao().insertWord(new Word("Der Zug", "Поезд", "Транспорт", "A1", "Велосипед, Автобус, Машина"));
            db.wordDao().insertWord(new Word("Das Fahrrad", "Велосипед", "Транспорт", "A1", "Трамвай, Самолет, Метро"));

            // Добавим тесты для других категорий, чтобы проверить работу переключения тем!
            db.wordDao().insertWord(new Word("Das Essen", "Еда", "Еда", "A1", "Яблоко, Хлеб"));
            db.wordDao().insertWord(new Word("Das Spielzeug","Игрушка", "Семья", "A1", "Мама, Папа"));
        }
    }

    // Обязательно освобождаем ресурсы при выходе с экрана карточек
    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}

