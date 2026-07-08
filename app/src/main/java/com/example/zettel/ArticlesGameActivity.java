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

public class ArticlesGameActivity extends AppCompatActivity {

    private TextView tvScore, tvGameWord, tvWordTranslation;
    private Button btnDer, btnDie, btnDas, btnExitGame;

    private AppDatabase db;
    private TextToSpeech textToSpeech;

    private List<Word> nounWords = new ArrayList<>(); // Список существительных с артиклями
    private int currentWordIndex = 0;
    private int score = 0;

    private Word currentWord;
    private String correctArticle = ""; // Хранит правильный артикль ("der", "die" или "das")
    private boolean isAnswered = false; // Защита от спам-кликов

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles_game);

        // Инициализация UI
        tvScore = findViewById(R.id.tvScore);
        tvGameWord = findViewById(R.id.tvGameWord);
        tvWordTranslation = findViewById(R.id.tvWordTranslation);
        btnDer = findViewById(R.id.btnDer);
        btnDie = findViewById(R.id.btnDie);
        btnDas = findViewById(R.id.btnDas);
        btnExitGame = findViewById(R.id.btnExitGame);

        db = AppDatabase.getInstance(this);

        // Запуск TTS для озвучки правильного ответа
        initTextToSpeech();

        btnExitGame.setOnClickListener(v -> finish());

        // Загружаем существительные из базы
        loadNounsFromDatabase();
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.GERMAN);
            }
        });
    }

    private void speakGerman(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void loadNounsFromDatabase() {
        new Thread(() -> {
            List<Word> allWords = db.wordDao().getAllWordsForReview();
            nounWords = new ArrayList<>();

            if (allWords != null) {
                for (Word w : allWords) {
                    String gWord = (w.getGermanWord() != null) ? w.getGermanWord().trim().toLowerCase() : "";
                    // Проверяем, начинается ли слово с известного артикля
                    if (gWord.startsWith("der ") || gWord.startsWith("die ") || gWord.startsWith("das ")) {
                        nounWords.add(w);
                    }
                }
            }

            runOnUiThread(() -> {
                if (nounWords.size() < 3) {
                    Toast.makeText(this, "В базе слишком мало существительных с артиклями (Der, Die, Das)!", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                // Перемешиваем слова, чтобы игра каждый раз была уникальной
                Collections.shuffle(nounWords);

                // Ограничим одну сессию игры, например, 15 словами (или сколько есть в базе, если меньше)
                if (nounWords.size() > 15) {
                    nounWords = nounWords.subList(0, 15);
                }

                generateNextWord();
            });
        }).start();
    }

    private void generateNextWord() {
        isAnswered = false;
        resetButtonColors();

        tvScore.setText("Очки: " + score);

        currentWord = nounWords.get(currentWordIndex);
        String fullGermanWord = currentWord.getGermanWord().trim();

        // Перевод (подсказка для пользователя)
        tvWordTranslation.setText("(" + currentWord.getRussianTranslation() + ")");

        // Извлекаем артикль и чистое слово
        String lowerWord = fullGermanWord.toLowerCase();
        String cleanWord = fullGermanWord;

        if (lowerWord.startsWith("der ")) {
            correctArticle = "der";
            cleanWord = fullGermanWord.substring(4); // Отрезаем первые 4 символа "Der "
        } else if (lowerWord.startsWith("die ")) {
            correctArticle = "die";
            cleanWord = fullGermanWord.substring(4); // Отрезаем первые 4 символа "Die "
        } else if (lowerWord.startsWith("das ")) {
            correctArticle = "das";
            cleanWord = fullGermanWord.substring(4); // Отрезаем первые 4 символа "Das "
        }

        // Выводим «голую» основу существительного на экран
        tvGameWord.setText(cleanWord);

        // Вешаем клики на кнопки
        btnDer.setOnClickListener(v -> checkArticleAnswer("der", btnDer));
        btnDie.setOnClickListener(v -> checkArticleAnswer("die", btnDie));
        btnDas.setOnClickListener(v -> checkArticleAnswer("das", btnDas));
    }

    private void checkArticleAnswer(String chosenArticle, Button clickedButton) {
        if (isAnswered) return;
        isAnswered = true;

        // При ответе сразу озвучиваем слово целиком С АРТИКЛЕМ, чтобы тренировать слуховую память!
        speakGerman(currentWord.getGermanWord());

        if (chosenArticle.equals(correctArticle)) {
            // Верно: красим кнопку в зеленый и добавляем 10 очков
            clickedButton.setBackgroundColor(Color.parseColor("#4CAF50"));
            clickedButton.setTextColor(Color.WHITE);
            score += 10;
        } else {
            // Ошибка: выбранную в красный, а правильную подсвечиваем ее родным цветом
            clickedButton.setBackgroundColor(Color.parseColor("#F44336"));
            clickedButton.setTextColor(Color.WHITE);
            highlightCorrectButton();
        }

        tvScore.setText("Очки: " + score);

        // Задержка 1.5 секунды, чтобы увидеть результат, и идем дальше
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            currentWordIndex++;
            if (currentWordIndex < nounWords.size()) {
                generateNextWord();
            } else {
                showGameResults();
            }
        }, 1500);
    }

    private void highlightCorrectButton() {
        if (correctArticle.equals("der")) {
            btnDer.setBackgroundColor(Color.parseColor("#4CAF50"));
            btnDer.setTextColor(Color.WHITE);
        } else if (correctArticle.equals("die")) {
            btnDie.setBackgroundColor(Color.parseColor("#4CAF50"));
            btnDie.setTextColor(Color.WHITE);
        } else if (correctArticle.equals("das")) {
            btnDas.setBackgroundColor(Color.parseColor("#4CAF50"));
            btnDas.setTextColor(Color.WHITE);
        }
    }

    private void resetButtonColors() {
        // Возвращаем кнопкам их дефолтные яркие цвета из XML темы
        btnDer.setBackgroundColor(Color.parseColor("#2196F3"));
        btnDer.setTextColor(Color.WHITE);

        btnDie.setBackgroundColor(Color.parseColor("#E91E63"));
        btnDie.setTextColor(Color.WHITE);

        btnDas.setBackgroundColor(Color.parseColor("#4CAF50"));
        btnDas.setTextColor(Color.WHITE);
    }

    private void showGameResults() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Игра окончена! 🏁");

        int maxPossibleScore = nounWords.size() * 10;
        String msg = "Вы набрали: " + score + " из " + maxPossibleScore + " очков.\n\n";

        if (score == maxPossibleScore) {
            msg += "Отличный результат! Вы идеально знаете немецкие артикли! 🇩🇪💯";
        } else if (score >= maxPossibleScore / 2) {
            msg += "Хороший результат! Но есть куда расти. Потренируйтесь еще! 💪";
        } else {
            msg += "Артикли — сложная тема. Повторите карточки категорий и возвращайтесь! 📖";
        }

        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("В меню", (dialog, which) -> finish());
        builder.create().show();
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

