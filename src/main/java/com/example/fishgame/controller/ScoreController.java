package com.example.fishgame.controller;

import com.example.fishgame.common.ApiResponse;
import com.example.fishgame.entity.Score;
import com.example.fishgame.repository.ScoreRepository;
import com.example.fishgame.service.LeaderboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ScoreController {

    private final ScoreRepository scoreRepository;
    private final LeaderboardService leaderboardService;

    public ScoreController(ScoreRepository scoreRepository, LeaderboardService leaderboardService) {
        this.scoreRepository = scoreRepository;
        this.leaderboardService = leaderboardService;
    }

    @PostMapping("/score")
    public ApiResponse<Score> uploadScore(@RequestBody Map<String, Object> request) {
        Object usernameValue = request.get("username");
        Object scoreValue = request.get("score");

        if (usernameValue == null || usernameValue.toString().trim().isEmpty()) {
            return ApiResponse.fail("username不能为空");
        }

        if (scoreValue == null) {
            return ApiResponse.fail("score不能为空");
        }

        Score score = new Score();
        score.setUsername(usernameValue.toString().trim());
        score.setScore(toInteger(scoreValue));
        score.setCreateTime(LocalDateTime.now());

        Score saved = scoreRepository.save(score);
        leaderboardService.evictLeaderboardCache();
        return ApiResponse.success("上传成功", saved);
    }

    @GetMapping("/leaderboard")
    public ApiResponse<List<Score>> leaderboard() {
        return ApiResponse.success("查询成功", leaderboardService.getLeaderboard());
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(value.toString());
    }
}
