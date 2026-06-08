package com.example.fishgame.service;

import com.example.fishgame.dto.MusicIndex;
import com.example.fishgame.service.cache.MusicIndexCache;
import com.example.fishgame.service.provider.YouTubeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class MusicSearchService {

    private static final Logger log = LoggerFactory.getLogger(MusicSearchService.class);

    private final YouTubeProvider youTubeProvider;
    private final MusicIndexCache cache;
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public MusicSearchService(YouTubeProvider youTubeProvider, MusicIndexCache cache) {
        this.youTubeProvider = youTubeProvider;
        this.cache = cache;
    }

    public List<MusicIndex> search(String keyword, int limit) {
        log.info("MusicSearchService search: keyword={}, limit={}", keyword, limit);

        cache.removeByKeyword(keyword);

        String kw = keyword.toLowerCase(Locale.ROOT).trim();

        List<YouTubeProvider.YouTubeSearchResult> rawResults = new ArrayList<>();
        try {
            List<YouTubeProvider.YouTubeSearchResult> yt = youTubeProvider.search(keyword, limit);
            rawResults.addAll(yt);
            log.info("YouTube returned {} raw results", yt.size());
        } catch (Exception e) {
            log.warn("YouTube search failed: {}", e.getMessage());
        }

        List<YouTubeProvider.YouTubeSearchResult> cleaned = cleanResults(rawResults, kw, limit);

        log.debug("=== cleanResults output: {} items ===", cleaned.size());
        for (YouTubeProvider.YouTubeSearchResult r : cleaned) {
            log.debug("  kept: [{}] {} - {} (mediaId={})", r.videoId, r.title, r.artist, r.videoId);
        }

        List<MusicIndex> finalResults = new ArrayList<>();
        for (YouTubeProvider.YouTubeSearchResult raw : cleaned) {
            String indexId = "mi_" + idCounter.getAndIncrement();
            MusicIndex entry = new MusicIndex(
                    indexId,
                    raw.title,
                    raw.artist,
                    raw.cover,
                    "youtube",
                    raw.videoId,
                    keyword
            );
            cache.put(indexId, entry);
            finalResults.add(entry);
        }

        log.info("MusicSearchService final: {} indexed results for '{}'", finalResults.size(), keyword);
        return finalResults;
    }

    private List<YouTubeProvider.YouTubeSearchResult> cleanResults(
            List<YouTubeProvider.YouTubeSearchResult> raw, String keyword, int limit) {
        if (raw == null || raw.isEmpty()) {
            log.warn("cleanResults: raw is empty, nothing to clean");
            return new ArrayList<>();
        }

        log.debug("cleanResults: raw={} items, keyword='{}', limit={}", raw.size(), keyword, limit);

        // --- debug: print all raw items ---
        for (int i = 0; i < raw.size(); i++) {
            YouTubeProvider.YouTubeSearchResult r = raw.get(i);
            log.debug("  raw[{}]: videoId={}, title='{}', artist='{}'", i, r.videoId, r.title, r.artist);
        }

        Set<String> seenVideoIds = new HashSet<>();
        List<YouTubeProvider.YouTubeSearchResult> cleaned = new ArrayList<>();

        for (YouTubeProvider.YouTubeSearchResult r : raw) {
            if (cleaned.size() >= limit) break;

            // --- null/invalid guard ---
            if (r == null) { log.debug("  skip: null item"); continue; }
            if (r.videoId == null || r.videoId.isEmpty()) { log.debug("  skip: videoId is null/empty"); continue; }
            if (r.title == null || r.title.isEmpty()) { log.debug("  skip: title is null/empty, videoId={}", r.videoId); continue; }
            if (r.artist == null || r.artist.isEmpty()) { log.debug("  skip: artist is null/empty, videoId={}", r.videoId); continue; }

            // --- dedup ---
            if (seenVideoIds.contains(r.videoId)) { log.debug("  skip (duplicate): videoId={}", r.videoId); continue; }
            seenVideoIds.add(r.videoId);

            // --- block-list (soft) ---
            String titleLower = r.title.toLowerCase(Locale.ROOT);
            if (titleLower.contains("remix") || titleLower.contains("live")
                    || titleLower.contains("compilation") || titleLower.contains("karaoke")) {
                log.debug("  skip (blocked): videoId={}, title='{}'", r.videoId, r.title);
                continue;
            }

            // --- keyword matching: prefer but NOT require ---
            boolean keywordMatch = keyword.isEmpty()
                    || titleLower.contains(keyword)
                    || r.artist.toLowerCase(Locale.ROOT).contains(keyword);

            if (!keywordMatch) {
                log.debug("  keep (no keyword match, but valid): videoId={}, title='{}', artist='{}'",
                        r.videoId, r.title, r.artist);
            } else {
                log.debug("  keep (keyword match): videoId={}, title='{}'", r.videoId, r.title);
            }

            cleaned.add(r);
        }

        // --- safety fallback: if everything was filtered, return top N raw ---
        if (cleaned.isEmpty() && !raw.isEmpty()) {
            log.warn("cleanResults: all {} items were filtered! Falling back to top {} raw items with dedup + null guard only",
                    raw.size(), limit);

            seenVideoIds.clear();
            for (YouTubeProvider.YouTubeSearchResult r : raw) {
                if (cleaned.size() >= limit) break;
                if (r == null || r.videoId == null || r.videoId.isEmpty()) continue;
                if (r.title == null || r.title.isEmpty()) continue;
                if (seenVideoIds.contains(r.videoId)) continue;
                seenVideoIds.add(r.videoId);
                cleaned.add(r);
            }
            log.warn("cleanResults: fallback produced {} items", cleaned.size());
        }

        // --- ensure minimum results by appending from raw if still empty ---
        if (cleaned.isEmpty() && !raw.isEmpty()) {
            log.warn("cleanResults: fallback also empty! Forcing top {} raw items", limit);
            seenVideoIds.clear();
            int count = 0;
            for (YouTubeProvider.YouTubeSearchResult r : raw) {
                if (count >= limit) break;
                if (r == null || r.videoId == null || r.videoId.isEmpty()) continue;
                if (r.title == null || r.title.isEmpty()) continue;
                if (seenVideoIds.contains(r.videoId)) continue;
                seenVideoIds.add(r.videoId);
                cleaned.add(r);
                count++;
            }
        }

        log.info("cleanResults: {} items after cleaning (from {})", cleaned.size(), raw.size());
        return cleaned;
    }
}
