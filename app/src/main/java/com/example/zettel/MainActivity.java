package com.example.zettel;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zettel.databinding.ActivityMainBinding;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AppDatabase db;
    private List<Word> wordList;
    private int currentWordIndex = 0;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Красивая стрелочка назад (наш ImageButton)
        if (binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> {
                setResult(RESULT_OK);
                finish();
            });
        }

        // Инициализация голосового движка TTS
        initTextToSpeech();

        db = AppDatabase.getInstance(this);
        preloadWordsIfNeeded();

        // 1. Принимаем ID темы и прямое РУССКОЕ название темы из TopicActivity
        String selectedTopicId = getIntent().getStringExtra("TOPIC_ID");
        String selectedCategoryName = getIntent().getStringExtra("TOPIC_NAME_RU");

        // 2. Универсальная загрузка слов на основе переданных данных
        if (selectedTopicId != null && selectedTopicId.equals("review")) {
            // Режим Повторения: подгружаем ВСЕ слова базы данных
            wordList = db.wordDao().getAllWordsForReview();
        } else {
            // Динамические категории: берем то имя, которое пришло. Если пусто — ставим "Транспорт"
            String categoryName = (selectedCategoryName != null) ? selectedCategoryName : "Транспорт";
            wordList = db.wordDao().getWordsForLesson("A1", categoryName);
        }

        // Выводим первое слово на экран
        displayCurrentWord();

        // Клик по карточке: показываем перевод + запускаем ОЗВУЧКУ
        binding.wordCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wordList != null && !wordList.isEmpty() && currentWordIndex < wordList.size()) {
                    binding.tvTranslation.setVisibility(View.VISIBLE);

                    String wordToSpeak = wordList.get(currentWordIndex).getGermanWord();
                    speakGerman(wordToSpeak);
                }
            }
        });

        // Клик по кнопке "Знаю" с круговой логикой и задержкой
        binding.btnKnow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wordList == null || wordList.isEmpty()) return;

                Word currentWord = wordList.get(currentWordIndex);
                currentWord.setLearned(true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        db.wordDao().updateWord(currentWord);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                displayCurrentWord();
                                binding.btnKnow.setEnabled(false);

                                binding.wordCard.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        currentWordIndex++;

                                        if (currentWordIndex < wordList.size()) {
                                            displayCurrentWord();
                                        } else {
                                            currentWordIndex = 0;
                                            displayCurrentWord();
                                            Toast.makeText(MainActivity.this, "Вы просмотрели все слова! Повторяем сначала.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }, 600);
                            }
                        });
                    }
                }).start();
            }
        });
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.GERMAN);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(MainActivity.this, "Немецкий язык не поддерживается", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Ошибка запуска TTS", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void speakGerman(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void displayCurrentWord() {
        if (wordList != null && !wordList.isEmpty() && currentWordIndex < wordList.size()) {
            Word word = wordList.get(currentWordIndex);
            binding.tvGermanWord.setText(word.getGermanWord());
            binding.tvTranslation.setText(word.getRussianTranslation());
            binding.tvTranslation.setVisibility(View.INVISIBLE);
            binding.btnKnow.setEnabled(true);

            // Перекраска карточки, если слово уже выучено
            if (word.isLearned()) {
                binding.wordCard.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"));
                binding.tvGermanWord.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
            } else {
                binding.wordCard.setBackgroundColor(android.graphics.Color.WHITE);
                binding.tvGermanWord.setTextColor(android.graphics.Color.parseColor("#212529"));
            }

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
            db.wordDao().insertWord(new Word("Das Essen", "Еда", "Еда", "A1", "Яблоко, Хлеб"));
            db.wordDao().insertWord(new Word("Die Familie", "Семья", "Семья", "A1", "Мама, Папа"));
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



