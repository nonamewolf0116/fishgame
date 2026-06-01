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
        return scoreRepository.findUserBestScores(PageRequest.of(0, 20));
    }

    public void evictLeaderboardCache() {
        // No Redis cache configured; nothing to evict.
    }
}
