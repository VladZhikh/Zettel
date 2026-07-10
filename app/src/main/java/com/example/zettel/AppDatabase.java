package com.example.zettel;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// ИСПРАВЛЕНО: Подняли версию базы данных с 1 на 2, так как изменилась структура таблицы Word
@Database(entities = {Word.class}, version = 2, exportSchema = false)
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
                            .allowMainThreadQueries()
                            // ИСПРАВЛЕНО: автоматически очистит и пересоздаст базу при изменении структуры полей
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
