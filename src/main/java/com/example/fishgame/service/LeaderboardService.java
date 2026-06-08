package com.example.fishgame.service;

import com.example.fishgame.entity.Score;
import com.example.fishgame.repository.ScoreRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaderboardService {

    private final ScoreRepository scoreRepository;

    public LeaderboardService(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    public List<Score> getLeaderboard() {
        try {
            return scoreRepository.findUserBestScores(PageRequest.of(0, 20));
        } catch (Exception e) {
            return List.of();
        }
    }

    public void evictLeaderboardCache() {
        // No Redis cache configured; nothing to evict.
    }
}
