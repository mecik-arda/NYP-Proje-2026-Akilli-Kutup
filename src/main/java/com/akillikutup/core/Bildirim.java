package com.akillikutup.core;

import java.util.UUID;

public class Bildirim {
    private String id;
    private String type;
    private String icon;
    private String text;
    private String time;
    private boolean unread;

    public Bildirim() {
        this.id = UUID.randomUUID().toString();
    }

    public Bildirim(String type, String icon, String text, String time) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.icon = icon;
        this.text = text;
        this.time = time;
        this.unread = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public boolean isUnread() { return unread; }
    public void setUnread(boolean unread) { this.unread = unread; }
}
