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

    private TextView tvGameWord, tvWordTranslation, tvLiveResult;
    private Button btnDer, btnDie, btnDas, btnExitGame;

    private AppDatabase db;
    private TextToSpeech textToSpeech;

    private List<Word> nounWords = new ArrayList<>();
    private int currentWordIndex = 0;
    private int correctAnswersCount = 0; // Счётчик правильных ответов вместо очков

    private Word currentWord;
    private String correctArticle = "";
    private boolean isAnswered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles_game);

        // Инициализация UI
        tvGameWord = findViewById(R.id.tvGameWord);
        tvWordTranslation = findViewById(R.id.tvWordTranslation);
        tvLiveResult = findViewById(R.id.tvLiveResult); // Наше новое поле внизу
        btnDer = findViewById(R.id.btnDer);
        btnDie = findViewById(R.id.btnDie);
        btnDas = findViewById(R.id.btnDas);
        btnExitGame = findViewById(R.id.btnExitGame);

        db = AppDatabase.getInstance(this);

        initTextToSpeech();

        btnExitGame.setOnClickListener(v -> finish());

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
                    if (gWord.startsWith("der ") || gWord.startsWith("die ") || gWord.startsWith("das ")) {
                        nounWords.add(w);
                    }
                }
            }

            runOnUiThread(() -> {
                if (nounWords.size() < 3) {
                    Toast.makeText(this, "В базе слишком мало существительных с артиклями!", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                Collections.shuffle(nounWords);

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

        // ИСПРАВЛЕНО: Показываем текущий прогресс в текстовом поле внизу (например: Результат: 2 из 5)
        tvLiveResult.setText("Результат: " + correctAnswersCount + " из " + currentWordIndex);

        currentWord = nounWords.get(currentWordIndex);
        String fullGermanWord = currentWord.getGermanWord().trim();

        tvWordTranslation.setText("(" + currentWord.getRussianTranslation() + ")");

        String lowerWord = fullGermanWord.toLowerCase();
        String cleanWord = fullGermanWord;

        if (lowerWord.startsWith("der ")) {
            correctArticle = "der";
            cleanWord = fullGermanWord.substring(4);
        } else if (lowerWord.startsWith("die ")) {
            correctArticle = "die";
            cleanWord = fullGermanWord.substring(4);
        } else if (lowerWord.startsWith("das ")) {
            correctArticle = "das";
            cleanWord = fullGermanWord.substring(4);
        }

        tvGameWord.setText(cleanWord);

        btnDer.setOnClickListener(v -> checkArticleAnswer("der", btnDer));
        btnDie.setOnClickListener(v -> checkArticleAnswer("die", btnDie));
        btnDas.setOnClickListener(v -> checkArticleAnswer("das", btnDas));
    }

    private void checkArticleAnswer(String chosenArticle, Button clickedButton) {
        if (isAnswered) return;
        isAnswered = true;

        speakGerman(currentWord.getGermanWord());

        if (chosenArticle.equals(correctArticle)) {
            // ИСПРАВЛЕНО: Красим только одну выбранную кнопку в зеленый
            clickedButton.setBackgroundColor(Color.parseColor("#4CAF50"));
            clickedButton.setTextColor(Color.WHITE);
            correctAnswersCount++;
        } else {
            // ИСПРАВЛЕНО: Выбранную ошибочную кнопку в красный, а правильную подсвечиваем нейтрально-зеленым
            clickedButton.setBackgroundColor(Color.parseColor("#F44336"));
            clickedButton.setTextColor(Color.WHITE);
            highlightCorrectButton();
        }

        // Обновляем текущий живой результат сразу после нажатия кнопки
        tvLiveResult.setText("Результат: " + correctAnswersCount + " из " + (currentWordIndex + 1));

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
        // ИСПРАВЛЕНО: Сбрасываем цвета кнопок к красивому нейтральному стилю (как в XML)
        int defaultBgColor = Color.parseColor("#E8EAF6");
        int defaultTextColor = Color.parseColor("#1A237E");

        btnDer.setBackgroundColor(defaultBgColor);
        btnDer.setTextColor(defaultTextColor);
        btnDie.setBackgroundColor(defaultBgColor);
        btnDie.setTextColor(defaultTextColor);
        btnDas.setBackgroundColor(defaultBgColor);
        btnDas.setTextColor(defaultTextColor);
    }

    private void showGameResults() {
        // В конце игры также дублируем финальный текст в нижнее поле
        tvLiveResult.setText("Итог: " + correctAnswersCount + " из " + nounWords.size());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Игра окончена! 🏁");

        String msg = "Ваш общий результат: " + correctAnswersCount + " из " + nounWords.size() + " правильных ответов.\n\n";

        if (correctAnswersCount == nounWords.size()) {
            msg += "Идеально! Вы потрясающе чувствуете немецкие артикли! 🇩🇪💯";
        } else if (correctAnswersCount >= nounWords.size() / 2) {
            msg += "Хороший результат! Повторите карточки и попробуйте улучшить счет! 💪";
        } else {
            msg += "Артикли — коварная тема. Почаще заглядывайте сюда для практики! 📖";
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


