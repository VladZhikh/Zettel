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

    // Извлечь ВСЕ слова из базы данных
    @Query("SELECT * FROM word_table")
    List<Word> getAllWords();

    // Самый главный запрос для карточек Zettel:
    // Достаем слова конкретного уровня (A1/A2) и конкретной темы, которые еще НЕ выучены
    @Query("SELECT * FROM word_table WHERE level = :selectedLevel AND category = :selectedCategory AND isLearned = 0")
    List<Word> getWordsForLesson(String selectedLevel, String selectedCategory);

    // Дополнительный запрос для режима повторения (вытягиваем выученные слова)
    @Query("SELECT * FROM word_table WHERE level = :selectedLevel AND isLearned = 1")
    List<Word> getWordsForReview(String selectedLevel);
}

