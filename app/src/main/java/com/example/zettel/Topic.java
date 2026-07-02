package com.example.zettel;

public class Topic {
    private final String id;
    private final String nameRu;
    private final String nameDe;

    public Topic(String id, String nameRu, String nameDe) {
        this.id = id;
        this.nameRu = nameRu;
        this.nameDe = nameDe;
    }

    public String getId() { return id; }
    public String getNameRu() { return nameRu; }
    public String getNameDe() { return nameDe; }
}
