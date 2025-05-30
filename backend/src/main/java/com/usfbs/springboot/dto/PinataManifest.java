package com.usfbs.springboot.dto;

public class PinataManifest {
    
    private String title;
    private String fileCid;

    // Default constructor needed for JSON deserialization
    public PinataManifest() { }

    public PinataManifest(String title, String fileCid) {
        this.title   = title;
        this.fileCid = fileCid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileCid() {
        return fileCid;
    }

    public void setFileCid(String fileCid) {
        this.fileCid = fileCid;
    }
}
