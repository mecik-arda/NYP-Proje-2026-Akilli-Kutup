package com.akillikutup.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
public class Bildirim {

    @Column(name = "bildirim_id", length = 36)
    private String id;

    @Column(name = "type")
    private String type;

    @Column(name = "icon")
    private String icon;

    @Column(name = "text", length = 1000)
    private String text;

    @Column(name = "time")
    private String time;

    @Column(name = "unread")
    private boolean unread;

    public Bildirim(String type, String icon, String text, String time) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.icon = icon;
        this.text = text;
        this.time = time;
        this.unread = true;
    }
}
