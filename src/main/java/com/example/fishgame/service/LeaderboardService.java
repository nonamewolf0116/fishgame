package com.example.fishgame.service;

import com.example.fishgame.entity.Score;
import com.example.fishgame.repository.ScoreRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class LeaderboardService {

    private static final String LEADERBOARD_CACHE_KEY = "leaderboard:top20";
    private static final Duration LEADERBOARD_TTL = Duration.ofMinutes(5);

    private final ScoreRepository scoreRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public LeaderboardService(ScoreRepository scoreRepository,
                              StringRedisTemplate redisTemplate,
                              ObjectMapper objectMapper) {
        this.scoreRepository = scoreRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Score> getLeaderboard() {
        String cached = redisTemplate.opsForValue().get(LEADERBOARD_CACHE_KEY);
        if (cached != null && !cached.isBlank()) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<Score>>() {});
            } catch (Exception ignored) {
                // ignore invalid cache data and refresh from DB
            }
        }

        List<Score> scores = scoreRepository.findUserBestScores(PageRequest.of(0, 20));
        try {
            String payload = objectMapper.writeValueAsString(scores);
            redisTemplate.opsForValue().set(LEADERBOARD_CACHE_KEY, payload, LEADERBOARD_TTL);
        } catch (Exception ignored) {
            // no-op, fallback to DB result
        }
        return scores;
    }

    public void evictLeaderboardCache() {
        redisTemplate.delete(LEADERBOARD_CACHE_KEY);
    }
}
