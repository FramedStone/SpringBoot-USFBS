package com.usfbs.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PinataManifest {
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("startDate")
    private Long startDate;
    
    @JsonProperty("endDate")
    private Long endDate;
    
    @JsonProperty("fileCid")
    private String fileCid;

    public PinataManifest() {}

    public PinataManifest(String title, Long startDate, Long endDate, String fileCid) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.fileCid = fileCid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public String getFileCid() {
        return fileCid;
    }

    public void setFileCid(String fileCid) {
        this.fileCid = fileCid;
    }

    @Override
    public String toString() {
        return "PinataManifest{" +
                "title='" + title + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", fileCid='" + fileCid + '\'' +
                '}';
    }
}
