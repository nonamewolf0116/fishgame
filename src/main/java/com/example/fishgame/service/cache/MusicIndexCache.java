package com.example.fishgame.service.cache;

import com.example.fishgame.dto.MusicIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MusicIndexCache {

    private static final Logger log = LoggerFactory.getLogger(MusicIndexCache.class);

    private final ConcurrentHashMap<String, MusicIndex> store = new ConcurrentHashMap<>();

    public void put(String id, MusicIndex entry) {
        store.put(id, entry);
    }

    public MusicIndex get(String id) {
        return store.get(id);
    }

    public List<MusicIndex> getAll() {
        return new ArrayList<>(store.values());
    }

    public int size() {
        return store.size();
    }

    public void clear() {
        store.clear();
        log.info("MusicIndexCache cleared");
    }

    public List<MusicIndex> getByKeyword(String keyword) {
        List<MusicIndex> results = new ArrayList<>();
        for (MusicIndex entry : store.values()) {
            if (keyword.equals(entry.getKeyword())) {
                results.add(entry);
            }
        }
        return results;
    }

    public void removeByKeyword(String keyword) {
        store.values().removeIf(e -> keyword.equals(e.getKeyword()));
    }
}
