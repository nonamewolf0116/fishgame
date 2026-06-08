package com.example.fishgame.service;

import com.example.fishgame.entity.FavoriteMusicEntity;
import com.example.fishgame.entity.User;
import com.example.fishgame.repository.FavoriteMusicRepository;
import com.example.fishgame.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FavoriteMusicService {

    private final FavoriteMusicRepository favoriteMusicRepository;
    private final UserRepository userRepository;

    public FavoriteMusicService(FavoriteMusicRepository favoriteMusicRepository,
                                UserRepository userRepository) {
        this.favoriteMusicRepository = favoriteMusicRepository;
        this.userRepository = userRepository;
    }

    public FavoriteMusicEntity addFavorite(String username, String songId, String songName,
                                           String artist, String coverUrl,
                                           String provider, String mediaId) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return null;

        if (favoriteMusicRepository.findByUserIdAndSongId(user.getId(), songId).isPresent()) {
            return null;
        }

        FavoriteMusicEntity entity = new FavoriteMusicEntity();
        entity.setUserId(user.getId());
        entity.setSongId(songId);
        entity.setSongName(songName != null ? songName : "");
        entity.setArtist(artist != null ? artist : "");
        entity.setCoverUrl(coverUrl != null ? coverUrl : "");
        entity.setProvider(provider != null ? provider : "youtube");
        entity.setMediaId(mediaId != null ? mediaId : "");
        entity.setCreatedAt(LocalDateTime.now());
        return favoriteMusicRepository.save(entity);
    }

    public boolean removeFavorite(String username, String songId) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;

        var opt = favoriteMusicRepository.findByUserIdAndSongId(user.getId(), songId);
        if (opt.isPresent()) {
            favoriteMusicRepository.delete(opt.get());
            return true;
        }
        return false;
    }

    public List<FavoriteMusicEntity> getFavorites(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return List.of();
        return favoriteMusicRepository.findByUserId(user.getId());
    }
}
