package com.example.fishgame.controller;

import com.example.fishgame.common.ApiResponse;
import com.example.fishgame.entity.Score;
import com.example.fishgame.repository.ScoreRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:63342")
@RequestMapping("/api")
public class ScoreController {

    private final ScoreRepository scoreRepository;

    public ScoreController(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
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

        return ApiResponse.success("上传成功", scoreRepository.save(score));
    }

    @GetMapping("/leaderboard")
    public ApiResponse<List<Score>> leaderboard() {
        return ApiResponse.success("查询成功", scoreRepository.findUserBestScores(PageRequest.of(0, 20)));
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(value.toString());
    }
}
