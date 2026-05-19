package com.example.fishgame.repository;

import com.example.fishgame.entity.Score;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    @Query("""
            select s
            from Score s
            where s.id = (
                select min(s2.id)
                from Score s2
                where s2.username = s.username
                  and s2.score = (
                      select max(s3.score)
                      from Score s3
                      where s3.username = s.username
                  )
            )
            order by s.score desc
            """)
    List<Score> findUserBestScores(Pageable pageable);
}
