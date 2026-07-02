package com.example.zettel; // Укажите ваш пакет

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TopicActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        RecyclerView rvTopics = findViewById(R.id.rvTopics);
        rvTopics.setLayoutManager(new LinearLayoutManager(this));

        // Наш тестовый список тем
        List<Topic> topics = new ArrayList<>();
        topics.add(new Topic("transport", "Транспорт", "Der Transport"));
        topics.add(new Topic("food", "Еда", "Das Essen"));
        topics.add(new Topic("shopping", "Покупки", "Das Einkaufen"));
        topics.add(new Topic("family", "Семья", "Die Familie"));

        // Инициализируем адаптер и передаем логику клика
        TopicAdapter adapter = new TopicAdapter(topics, topic -> {
            // Переходим в MainActivity (экран с карточкой)
            Intent intent = new Intent(TopicActivity.this, MainActivity.class);
            // Передаем ID темы, чтобы MainActivity знала, какие слова показывать
            intent.putExtra("TOPIC_ID", topic.getId());
            startActivity(intent);
        });

        rvTopics.setAdapter(adapter);
    }
}

