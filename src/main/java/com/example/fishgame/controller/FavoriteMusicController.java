package com.example.fishgame.controller;

import com.example.fishgame.common.ApiResponse;
import com.example.fishgame.dto.AddFavoriteRequest;
import com.example.fishgame.entity.FavoriteMusicEntity;
import com.example.fishgame.service.FavoriteMusicService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/music/favorite")
public class FavoriteMusicController {

    private final FavoriteMusicService favoriteMusicService;

    public FavoriteMusicController(FavoriteMusicService favoriteMusicService) {
        this.favoriteMusicService = favoriteMusicService;
    }

    @PostMapping("/{songId}")
    public ApiResponse<FavoriteMusicEntity> addFavorite(
            Authentication auth, @PathVariable String songId,
            @RequestBody AddFavoriteRequest request) {
        if (auth == null) {
            return ApiResponse.fail("未登录");
        }
        String username = auth.getName();
        FavoriteMusicEntity entity = favoriteMusicService.addFavorite(
                username, songId, request.getSongName(),
                request.getArtist(), request.getCoverUrl(),
                request.getProvider(), request.getMediaId());
        if (entity == null) {
            return ApiResponse.fail("已收藏");
        }
        return ApiResponse.success("收藏成功", entity);
    }

    @DeleteMapping("/{songId}")
    public ApiResponse<Void> removeFavorite(Authentication auth, @PathVariable String songId) {
        if (auth == null) {
            return ApiResponse.fail("未登录");
        }
        String username = auth.getName();
        if (favoriteMusicService.removeFavorite(username, songId)) {
            return ApiResponse.success("取消收藏成功", null);
        }
        return ApiResponse.fail("收藏记录不存在");
    }

    @GetMapping("/list")
    public ApiResponse<List<FavoriteMusicEntity>> getFavorites(Authentication auth) {
        if (auth == null) {
            return ApiResponse.fail("未登录");
        }
        String username = auth.getName();
        List<FavoriteMusicEntity> favorites = favoriteMusicService.getFavorites(username);
        return ApiResponse.success(favorites);
    }
}
