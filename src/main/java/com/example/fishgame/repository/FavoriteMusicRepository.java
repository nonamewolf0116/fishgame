package com.example.fishgame.repository;

import com.example.fishgame.entity.FavoriteMusicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteMusicRepository extends JpaRepository<FavoriteMusicEntity, Long> {

    List<FavoriteMusicEntity> findByUserId(Long userId);

    Optional<FavoriteMusicEntity> findByUserIdAndSongId(Long userId, String songId);

    void deleteByUserIdAndSongId(Long userId, String songId);
}
