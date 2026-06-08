package com.example.fishgame.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddFavoriteRequest {
    private String songId;
    private String songName;
    private String artist;
    private String coverUrl;
    private String provider;
    private String mediaId;
}
