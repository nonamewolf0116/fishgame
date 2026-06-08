package com.example.fishgame.service;

import com.example.fishgame.dto.MusicIndex;
import com.example.fishgame.service.cache.MusicIndexCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MusicPlayService {

    private static final Logger log = LoggerFactory.getLogger(MusicPlayService.class);

    private final MusicIndexCache cache;

    public MusicPlayService(MusicIndexCache cache) {
        this.cache = cache;
    }

    public MusicIndex getPlayInfo(String id) {
        MusicIndex entry = cache.get(id);
        if (entry == null) {
            log.warn("MusicPlayService: id={} not found in cache", id);
            return null;
        }
        return entry;
    }
}
