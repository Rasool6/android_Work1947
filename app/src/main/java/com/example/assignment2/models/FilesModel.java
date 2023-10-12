package com.example.assignment2.models;

public class FilesModel {

    public String fileName;
    public String path;
    public String lastModified;

    public FilesModel(String fileName, String path, String lastModified) {
        this.fileName = fileName;
        this.path = path;
        this.lastModified = lastModified;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
}
