package com.education.database.schoolapp;

/**
 * Created by Sainath on 24-10-2015.
 */
public class MessageItem {
    public String msgTitle;
    public String msgDescription;
    public String msgFromTo;
    public String msgDate;
    public boolean msgAttachment = false;
    public short msgReadStatus = 0;
    public byte[] msgFromImage;
}
