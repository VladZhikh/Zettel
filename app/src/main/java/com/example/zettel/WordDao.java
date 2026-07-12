package com.example.zettel;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface WordDao {

    // Добавить новое слово в базу данных
    @Insert
    void insertWord(Word word);

    // Обновить статус слова (например, когда пометили как "выучено")
    @Update
    void updateWord(Word word);

    // Удаление слов из базы данных
    @androidx.room.Delete
    void deleteWord(Word word);

    // Извлечь ВСЕ слова из базы данных
    @Query("SELECT * FROM word_table")
    List<Word> getAllWords();

    // Достаем слова конкретного уровня (A1/A2) и конкретной темы, которые еще НЕ выучены
    // ТЕПЕРЬ ДОСТАЕМ ВООБЩЕ ВСЕ СЛОВА ТЕМЫ (и выученные, и нет)
    @Query("SELECT * FROM word_table WHERE level = :selectedLevel AND category = :selectedCategory")
    List<Word> getWordsForLesson(String selectedLevel, String selectedCategory);

    // ТЕПЕРЬ ДЛЯ ПОВТОРЕНИЯ ЗАГРУЖАЕМ АБСОЛЮТНО ВСЕ СЛОВА ИЗ БАЗЫ
    @Query("SELECT * FROM word_table")
    List<Word> getAllWordsForReview();

    // Считаем общее количество слов в конкретной категории
    @Query("SELECT COUNT(*) FROM word_table WHERE category = :categoryName AND level = :level")
    int getTotalWordsCount(String categoryName, String level);


    // Считаем количество только выученных слов в конкретной категории
    @Query("SELECT COUNT(*) FROM word_table WHERE category = :categoryName AND level = :level AND isLearned = 1")
    int getLearnedWordsCount(String categoryName, String level);

    // Выбираем все темы, которые есть в базе
    @Query("SELECT DISTINCT category FROM word_table WHERE category IS NOT NULL AND category != ''")
    List<String> getUniqueCategories();
    // Считает ВСЕ слова конкретной темы и уровня
    @Query("SELECT COUNT(*) FROM word_table WHERE category = :category AND level = :level")
    int getCountAllWordsInTopic(String category, String level);

    // Считает только ВЫУЧЕННЫЕ слова конкретной темы и уровня
    @Query("SELECT COUNT(*) FROM word_table WHERE category = :category AND level = :level AND isLearned = 1")
    int getCountLearnedWordsInTopic(String category, String level);


}

