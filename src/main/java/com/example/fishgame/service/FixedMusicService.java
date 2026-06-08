package com.example.fishgame.service;

import com.example.fishgame.dto.MusicIndex;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class FixedMusicService {

    private static final Logger log = LoggerFactory.getLogger(FixedMusicService.class);
    private static final String MANIFEST_PATH = "music_manifest.json";

    private final ObjectMapper objectMapper;
    private final List<MusicIndex> playlist = new ArrayList<>();
    private final Map<Integer, MusicIndex> byId = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public FixedMusicService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource(MANIFEST_PATH);
            try (InputStream is = resource.getInputStream()) {
                List<Map<String, Object>> rawList = objectMapper.readValue(is,
                        new TypeReference<List<Map<String, Object>>>() {});

                for (Map<String, Object> raw : rawList) {
                    int rawId = ((Number) raw.getOrDefault("id", 0)).intValue();
                    String title = (String) raw.getOrDefault("title", "Unknown");
                    String artist = (String) raw.getOrDefault("artist", "Unknown");
                    String file = (String) raw.getOrDefault("file", "");

                    if (file.isEmpty()) continue;

                    ClassPathResource cr = new ClassPathResource("static/music/" + file);
                    File prodFile = new File("/opt/fishgame/static/music/" + file);
                    if (!cr.exists() && !prodFile.exists()) {
                        log.warn("音乐文件未部署，将使用URL直出(可能404): {} (checked classpath:static/music/ and /opt/fishgame/static/music/)", file);
                    }

                    String indexId = "mi_" + idCounter.getAndIncrement();
                    String url = "/music/" + file;

                    MusicIndex entry = new MusicIndex(
                            indexId,
                            title,
                            artist,
                            "",
                            "local",
                            url,
                            ""
                    );
                    playlist.add(entry);
                    byId.put(rawId, entry);
                }
            }
            log.info("FixedMusicService initialized: {} songs loaded", playlist.size());
        } catch (Exception e) {
            log.error("Failed to load music manifest: {}", e.getMessage());
        }
    }

    public List<MusicIndex> getPlaylist() {
        return Collections.unmodifiableList(playlist);
    }

    public MusicIndex getById(String indexId) {
        for (MusicIndex m : playlist) {
            if (m.getId().equals(indexId)) return m;
        }
        return null;
    }

    public int size() {
        return playlist.size();
    }
}
