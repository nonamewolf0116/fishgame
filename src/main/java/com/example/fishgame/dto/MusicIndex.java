package com.example.fishgame.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MusicIndex {
    private String id;
    private String title;
    private String artist;
    private String cover;
    private String provider;
    private String mediaId;
    private String keyword;
}
