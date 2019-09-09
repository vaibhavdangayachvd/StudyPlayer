package com.example.studyplayer;

final public class Video {
    String id, title, description, filename, size, created;

    public Video(String id, String title, String description, String filename, String size, String created) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.filename = filename;
        this.size = size;
        this.created = created;
    }

    public String getId() {
        return id;
    }

    public String getCreated() {
        return created;
    }

    public String getDescription() {
        return description;
    }

    public String getFilename() {
        return filename;
    }

    public String getSize() {
        return size;
    }

    public String getTitle() {
        return title;
    }
}
