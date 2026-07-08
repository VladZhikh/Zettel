package com.example.zettel;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start); // Наша новая разметка

        Button btnLevelA1 = findViewById(R.id.btnLevelA1);
        Button btnLevelA2 = findViewById(R.id.btnLevelA2);
        Button btnLevelB1 = findViewById(R.id.btnLevelB1);
        Button btnLevelB2 = findViewById(R.id.btnLevelB2);
        Button btnOpenTopics = findViewById(R.id.btnOpenTopics);
        Button btnStartQuiz = findViewById(R.id.btnStartQuiz);

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

    }

    private void startReviewSession(String difficulty) {
        Intent intent = new Intent(StartActivity.this, ReviewActivity.class);
        intent.putExtra("DIFFICULTY_LEVEL", difficulty);
        startActivity(intent);
    }
}

