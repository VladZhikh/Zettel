package com.example.zettel;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Указываем, какие таблицы будут в БД и версию схемы
@Database(entities = {Word.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // Абстрактный метод для получения DAO
    public abstract WordDao wordDao();

    // Переменная для хранения единственного экземпляра базы данных (Паттерн Singleton)
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "zettel_database" // Имя файла БД на телефоне
                            )
                            // Разрешаем запросы в главном потоке ТОЛЬКО для тестирования MVP!
                            // В будущем мы перенесем это в фоновый поток.
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
