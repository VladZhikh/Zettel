package com.example.zettel;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zettel.databinding.ActivityMainBinding;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AppDatabase db;
    private List<Word> wordList;
    private int currentWordIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Инициализируем базу данных
        db = AppDatabase.getInstance(this);

        // 2. Наполняем базу стартовыми словами (только при самом первом запуске)
        preloadWordsIfNeeded();

        // 3. Загружаем слова уровня A1 из категории "Транспорт" (для теста)
        wordList = db.wordDao().getWordsForLesson("A1", "Транспорт");

        // 4. Отображаем первое слово на экране, если список не пуст
        displayCurrentWord();

        // 5. Логика клика по карточке (показать перевод)
        binding.wordCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wordList != null && !wordList.isEmpty()) {
                    binding.tvTranslation.setVisibility(View.VISIBLE);
                }
            }
        });

        // 6. Логика клика по кнопке "Знаю"
        binding.btnKnow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wordList == null || wordList.isEmpty()) return;

                // Помечаем текущее слово в базе как выученное
                Word currentWord = wordList.get(currentWordIndex);
                currentWord.setLearned(true);
                db.wordDao().updateWord(currentWord);

                // Переходим к следующему слову
                currentWordIndex++;
                if (currentWordIndex < wordList.size()) {
                    displayCurrentWord();
                } else {
                    // Слова в этой теме закончились
                    binding.tvGermanWord.setText("Wunderbar!");
                    binding.tvTranslation.setText("Вы выучили все слова в этой теме!");
                    binding.tvTranslation.setVisibility(View.VISIBLE);
                    binding.btnKnow.setEnabled(false); // Отключаем кнопку
                }
            }
        });
    }

    // Метод для вывода текущего слова на экран
    private void displayCurrentWord() {
        if (wordList != null && !wordList.isEmpty() && currentWordIndex < wordList.size()) {
            Word word = wordList.get(currentWordIndex);
            binding.tvGermanWord.setText(word.getGermanWord());
            binding.tvTranslation.setText(word.getRussianTranslation());
            binding.tvTranslation.setVisibility(View.INVISIBLE); // Скрываем перевод для нового слова
        } else {
            binding.tvGermanWord.setText("Нет слов");
            binding.tvTranslation.setText("Добавьте слова в базу данных");
            binding.tvTranslation.setVisibility(View.VISIBLE);
        }
    }

    // Метод проверки и первичного наполнения базы данных
    private void preloadWordsIfNeeded() {
        // Проверяем, есть ли вообще слова в базе
        if (db.wordDao().getAllWords().isEmpty()) {
            // Если пусто — добавляем три стартовых слова для теста
            db.wordDao().insertWord(new Word("Das Auto", "Автомобиль", "Транспорт", "A1", "Самолет, Корабль, Поезд"));
            db.wordDao().insertWord(new Word("Der Zug", "Поезд", "Транспорт", "A1", "Велосипед, Автобус, Машина"));
            db.wordDao().insertWord(new Word("Das Fahrrad", "Велосипед", "Транспорт", "A1", "Трамвай, Самолет, Метро"));

            // Можно добавить слово из другой категории для проверки фильтра
            db.wordDao().insertWord(new Word("Удалить этот тест", "Тест", "Еда", "A1", "1, 2, 3"));
        }
    }
}
