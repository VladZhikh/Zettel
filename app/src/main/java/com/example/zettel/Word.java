package com.example.zettel;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "word_table")
public class Word {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String germanWord;
    private String russianTranslation;
    private String category;
    private String level; // Наш уровень сложности (A1, A2, B1)
    private String wrongOptions; // 3 ложных ответа через запятую для будущих тестов
    private boolean isLearned;

    // Конструктор для создания объекта слова
    public Word(String germanWord, String russianTranslation, String category, String level, String wrongOptions) {
        this.germanWord = germanWord;
        this.russianTranslation = russianTranslation;
        this.category = category;
        this.level = level;
        this.wrongOptions = wrongOptions;
        this.isLearned = false; // По умолчанию слово не выучено
    }

    // Геттеры и сеттеры (необходимы для работы библиотеки Room)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getGermanWord() { return germanWord; }
    public void setGermanWord(String germanWord) { this.germanWord = germanWord; }

    public String getRussianTranslation() { return russianTranslation; }
    public void setRussianTranslation(String russianTranslation) { this.russianTranslation = russianTranslation; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getWrongOptions() { return wrongOptions; }
    public void setWrongOptions(String wrongOptions) { this.wrongOptions = wrongOptions; }

    public boolean isLearned() { return isLearned; }
    public void setLearned(boolean learned) { isLearned = learned; }
}
