package com.example.fishgame.controller;

import com.example.fishgame.common.ApiResponse;
import com.example.fishgame.dto.MusicIndex;
import com.example.fishgame.service.FixedMusicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/music")
public class MusicController {

    private final FixedMusicService fixedMusicService;

    public MusicController(FixedMusicService fixedMusicService) {
        this.fixedMusicService = fixedMusicService;
    }

    @GetMapping("/playlist")
    public ApiResponse<List<MusicIndex>> getPlaylist() {
        return ApiResponse.success(fixedMusicService.getPlaylist());
    }

    @GetMapping("/play/{id}")
    public ApiResponse<MusicIndex> play(@PathVariable String id) {
        MusicIndex entry = fixedMusicService.getById(id);
        if (entry == null) {
            return ApiResponse.fail(404, "歌曲不存在");
        }
        return ApiResponse.success(entry);
    }
}
