package com.example.zettel;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {

    private TextView tvQuizProgress, tvQuestionWord;
    private Button btnOption1, btnOption2, btnOption3, btnExitQuiz;

    private AppDatabase db;
    private TextToSpeech textToSpeech;

    private List<Word> allWords = new ArrayList<>();
    private List<Word> quizWords = new ArrayList<>(); // Список слов для текущей сессии теста

    private int currentQuestionIndex = 0;
    private int correctAnswersCount = 0; // СЧЕТЧИК ПРАВИЛЬНЫХ ОТВЕТОВ
    private final int TOTAL_QUESTIONS = 10; // Сколько вопросов будет в одном тесте

    private Word currentCorrectWord;
    private final Random random = new Random();
    private boolean isAnswered = false; // Защита от многократных кликов по кнопкам ответов

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Инициализация элементов интерфейса
        tvQuizProgress = findViewById(R.id.tvQuizProgress);
        tvQuestionWord = findViewById(R.id.tvQuestionWord);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnExitQuiz = findViewById(R.id.btnExitQuiz);

        db = AppDatabase.getInstance(this);

        // Инициализируем голосовой движок TTS
        initTextToSpeech();

        // Завершить тест по кнопке внизу
        btnExitQuiz.setOnClickListener(v -> finish());

        // Загружаем данные из базы Room в фоне
        loadWordsFromDatabase();
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.GERMAN);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(QuizActivity.this, "Немецкий язык не поддерживается", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(QuizActivity.this, "Ошибка запуска TTS", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void speakGerman(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void loadWordsFromDatabase() {
        new Thread(() -> {
            allWords = db.wordDao().getAllWordsForReview();

            runOnUiThread(() -> {
                if (allWords.size() < 3) {
                    Toast.makeText(this, "Нужно добавить хотя бы 3 слова в приложение, чтобы запустить тест!", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                // Перемешиваем и отбираем случайные 10 слов
                List<Word> shuffledList = new ArrayList<>(allWords);
                Collections.shuffle(shuffledList);

                int questionsCount = Math.min(TOTAL_QUESTIONS, shuffledList.size());
                quizWords = shuffledList.subList(0, questionsCount);

                // Запускаем первый вопрос
                generateQuestion();
            });
        }).start();
    }

    private void generateQuestion() {
        isAnswered = false;
        resetButtonStyles();

        tvQuizProgress.setText("Вопрос: " + (currentQuestionIndex + 1) + " / " + quizWords.size());
        currentCorrectWord = quizWords.get(currentQuestionIndex);

        // Выбираем направление перевода (50% шанс)
        boolean germanToRussian = random.nextBoolean();

        String questionText;
        String correctTranslation;

        if (germanToRussian) {
            questionText = currentCorrectWord.getGermanWord();
            correctTranslation = currentCorrectWord.getRussianTranslation();

            // ЕСЛИ ВОПРОС НА НЕМЕЦКОМ — ОЗВУЧИВАЕМ СРАЗУ ПРИ ПОЯВЛЕНИИ ВСПЛЫВАЮЩЕГО СЛОВА
            speakGerman(questionText);
        } else {
            questionText = currentCorrectWord.getRussianTranslation();
            correctTranslation = currentCorrectWord.getGermanWord();
        }

        tvQuestionWord.setText(questionText);

        // Собираем варианты ответов (1 правильный + 2 случайных неправильных)
        List<String> options = new ArrayList<>();
        options.add(correctTranslation);

        while (options.size() < 3) {
            Word randomWord = allWords.get(random.nextInt(allWords.size()));
            String wrongTranslation = germanToRussian ? randomWord.getRussianTranslation() : randomWord.getGermanWord();

            if (!options.contains(wrongTranslation) && wrongTranslation != null && !wrongTranslation.isEmpty()) {
                options.add(wrongTranslation);
            }
        }

        Collections.shuffle(options);

        btnOption1.setText(options.get(0));
        btnOption2.setText(options.get(1));
        btnOption3.setText(options.get(2));

        // Вешаем слушатели кликов, передавая также флаг germanToRussian, чтобы знать, когда озвучивать клик
        btnOption1.setOnClickListener(v -> checkAnswer(btnOption1, correctTranslation, !germanToRussian));
        btnOption2.setOnClickListener(v -> checkAnswer(btnOption2, correctTranslation, !germanToRussian));
        btnOption3.setOnClickListener(v -> checkAnswer(btnOption3, correctTranslation, !germanToRussian));
    }

    private void checkAnswer(Button selectedButton, String correctTranslation, boolean speakOptionOnCorrect) {
        if (isAnswered) return;
        isAnswered = true;

        String selectedText = selectedButton.getText().toString();

        if (selectedText.equals(correctTranslation)) {
            selectedButton.setBackgroundColor(Color.parseColor("#4CAF50"));
            selectedButton.setTextColor(Color.WHITE);
            correctAnswersCount++; // УВЕЛИЧИВАЕМ СЧЕТ ПРИ ПРАВИЛЬНОМ ОТВЕТЕ

            // Если мы переводили с русского на немецкий — озвучиваем выбранный правильный немецкий вариант
            if (speakOptionOnCorrect) {
                speakGerman(correctTranslation);
            }
        } else {
            selectedButton.setBackgroundColor(Color.parseColor("#F44336"));
            selectedButton.setTextColor(Color.WHITE);
            highlightCorrectAnswer(correctTranslation);

            // При ошибке тоже озвучим правильный немецкий вариант, чтобы пользователь запоминал на слух
            if (speakOptionOnCorrect) {
                speakGerman(correctTranslation);
            }
        }

        // Задержка в 1.5 секунды перед следующим вопросом
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            currentQuestionIndex++;
            if (currentQuestionIndex < quizWords.size()) {
                generateQuestion();
            } else {
                // ПОКАЗЫВАЕМ КРАСИВОЕ ОКНО РЕЗУЛЬТАТОВ ТЕСТА
                showQuizResults();
            }
        }, 1500);
    }

    private void showQuizResults() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Тест завершен! 🎉");

        String resultMessage = "Ваш результат: " + correctAnswersCount + " из " + quizWords.size() + " правильных ответов.\n\n";

        // Добавим подбадривающий текст в зависимости от успеха
        if (correctAnswersCount == quizWords.size()) {
            resultMessage += "Идеально! Отличный уровень немецкого! 🇩🇪";
        } else if (correctAnswersCount >= quizWords.size() / 2) {
            resultMessage += "Хороший результат! Продолжайте в том же духе! 💪";
        } else {
            resultMessage += "Не расстраивайтесь, повторите слова и попробуйте еще раз! 📖";
        }

        builder.setMessage(resultMessage);
        builder.setCancelable(false); // Чтобы нельзя было закрыть окно кликом мимо

        builder.setPositiveButton("В главное меню", (dialog, which) -> {
            finish(); // Закрываем тест и возвращаемся в StartActivity
        });

        builder.create().show();
    }

    private void highlightCorrectAnswer(String correctTranslation) {
        if (btnOption1.getText().toString().equals(correctTranslation)) {
            btnOption1.setBackgroundColor(Color.parseColor("#4CAF50"));
            btnOption1.setTextColor(Color.WHITE);
        } else if (btnOption2.getText().toString().equals(correctTranslation)) {
            btnOption2.setBackgroundColor(Color.parseColor("#4CAF50"));
            btnOption2.setTextColor(Color.WHITE);
        } else if (btnOption3.getText().toString().equals(correctTranslation)) {
            btnOption3.setBackgroundColor(Color.parseColor("#4CAF50"));
            btnOption3.setTextColor(Color.WHITE);
        }
    }

    private void resetButtonStyles() {
        int defaultBgColor = Color.parseColor("#E8EAF6");
        int defaultTextColor = Color.parseColor("#1A237E");

        btnOption1.setBackgroundColor(defaultBgColor);
        btnOption1.setTextColor(defaultTextColor);
        btnOption2.setBackgroundColor(defaultBgColor);
        btnOption2.setTextColor(defaultTextColor);
        btnOption3.setBackgroundColor(defaultBgColor);
        btnOption3.setTextColor(defaultTextColor);
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

