package com.usfbs.springboot.dto;

public class AnnouncementItem {
  private final String ipfsHash;
  private final String title;
  private final String fileCid;
  private final long startDate;
  private final long endDate;

  public AnnouncementItem(
      String ipfsHash,
      String title,
      String fileCid,
      long startDate,
      long endDate
  ) {
    this.ipfsHash = ipfsHash;
    this.title    = title;
    this.fileCid  = fileCid;
    this.startDate= startDate;
    this.endDate  = endDate;
  }

  public String getIpfsHash() { return ipfsHash; }
  public String getTitle()    { return title; }
  public String getFileCid()  { return fileCid; }
  public long   getStartDate(){ return startDate; }
  public long   getEndDate()  { return endDate; }
}