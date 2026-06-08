package com.example.fishgame.service.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class YouTubeProvider {

    private static final Logger log = LoggerFactory.getLogger(YouTubeProvider.class);
    private static final String PROVIDER = "youtube";
    private static final String SEARCH_API = "https://www.googleapis.com/youtube/v3/search";
    private static final int TIMEOUT_SECONDS = 10;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final List<YouTubeTrack> fallbackLibrary;

    public YouTubeProvider(@Value("${youtube.api.key}") String apiKey) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
        this.fallbackLibrary = buildFallbackLibrary();
    }

    public String getProviderName() {
        return PROVIDER;
    }

    public List<YouTubeSearchResult> search(String keyword, int limit) {
        if (apiKey == null || apiKey.isEmpty() || "test_placeholder_key".equals(apiKey)) {
            log.info("YouTube API key not configured, using fallback library");
            return searchFallback(keyword, limit);
        }

        List<YouTubeSearchResult> results = new ArrayList<>();
        try {
            String q = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = SEARCH_API + "?part=snippet&q=" + q
                    + "&key=" + apiKey
                    + "&type=video"
                    + "&videoCategoryId=10"
                    + "&maxResults=" + Math.min(limit, 50)
                    + "&regionCode=CN"
                    + "&fields=items(id(videoId),snippet(title,channelTitle,thumbnails(default(url),high(url))))";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "FishGame/1.0")
                    .GET()
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("YouTube API status: {}, falling back to local library", response.statusCode());
                return searchFallback(keyword, limit);
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode items = root.get("items");
            if (items == null || !items.isArray()) return searchFallback(keyword, limit);

            int idx = 0;
            for (JsonNode item : items) {
                idx++;
                JsonNode idNode = item.get("id");
                JsonNode snippet = item.get("snippet");
                if (idNode == null || snippet == null) {
                    log.debug("  YouTube API result[{}]: skipped (null id/snippet)", idx);
                    continue;
                }

                String videoId = idNode.get("videoId").asText();
                String title = snippet.get("title").asText();
                String channelTitle = snippet.get("channelTitle").asText();

                String cover = "";
                JsonNode thumbnails = snippet.get("thumbnails");
                if (thumbnails != null) {
                    JsonNode high = thumbnails.get("high");
                    if (high != null && high.has("url")) {
                        cover = high.get("url").asText();
                    } else {
                        JsonNode def = thumbnails.get("default");
                        if (def != null && def.has("url")) {
                            cover = def.get("url").asText();
                        }
                    }
                }

                log.debug("  YouTube API result[{}]: videoId={}, title='{}', artist='{}'", idx, videoId, title, channelTitle);
                results.add(new YouTubeSearchResult(videoId, title, channelTitle, cover));
            }
        } catch (Exception e) {
            log.error("YouTube API error: {}, falling back to local library", e.getMessage());
            return searchFallback(keyword, limit);
        }

        log.info("YouTubeProvider search: {} results for '{}'", results.size(), keyword);
        return results;
    }

    private List<YouTubeSearchResult> searchFallback(String keyword, int limit) {
        String kw = keyword.toLowerCase(Locale.ROOT).trim();
        List<YouTubeSearchResult> results = fallbackLibrary.stream()
                .filter(t -> kw.isEmpty()
                        || t.title.toLowerCase(Locale.ROOT).contains(kw)
                        || t.artist.toLowerCase(Locale.ROOT).contains(kw))
                .map(t -> new YouTubeSearchResult(t.videoId, t.title, t.artist, t.cover))
                .limit(limit)
                .collect(Collectors.toList());

        log.debug("searchFallback: keyword='{}', matched={} items", keyword, results.size());

        if (results.isEmpty()) {
            log.debug("searchFallback: no match, returning all {} items", Math.min(limit, fallbackLibrary.size()));
            results = fallbackLibrary.stream()
                    .map(t -> new YouTubeSearchResult(t.videoId, t.title, t.artist, t.cover))
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        for (int i = 0; i < results.size(); i++) {
            YouTubeSearchResult r = results.get(i);
            log.debug("  result[{}]: videoId={}, title='{}', artist='{}'", i, r.videoId, r.title, r.artist);
        }

        log.info("YouTubeProvider fallback: {} results for '{}'", results.size(), keyword);
        return results;
    }

    private List<YouTubeTrack> buildFallbackLibrary() {
        List<YouTubeTrack> list = new ArrayList<>();
        list.add(new YouTubeTrack("深海回响 - 纯音乐", "张宇", "jNQXAC9IVRw", "https://picsum.photos/seed/yt1/200/200"));
        list.add(new YouTubeTrack("碧波荡漾 - 轻音乐", "李悦", "dQw4w9WgXcQ", "https://picsum.photos/seed/yt2/200/200"));
        list.add(new YouTubeTrack("大鱼之旅 - 背景音乐", "王菲", "JGwWNGJdvx8", "https://picsum.photos/seed/yt3/200/200"));
        list.add(new YouTubeTrack("珊瑚之恋 - 钢琴曲", "周深", "kXYiU_JCYtU", "https://picsum.photos/seed/yt4/200/200"));
        list.add(new YouTubeTrack("浪花轻舞", "林俊杰", "09R8_2nJtjg", "https://picsum.photos/seed/yt5/200/200"));
        list.add(new YouTubeTrack("海底星空 - 冥想音乐", "邓紫棋", "fJ9rUzIMcZQ", "https://picsum.photos/seed/yt6/200/200"));
        list.add(new YouTubeTrack("飞鱼之梦", "陈奕迅", "hHW1oY_R83w", "https://picsum.photos/seed/yt7/200/200"));
        list.add(new YouTubeTrack("蓝色潮汐", "蔡依林", "OPf0YbXqDm0", "https://picsum.photos/seed/yt8/200/200"));
        list.add(new YouTubeTrack("水母漫舞", "五月天", "siCmMfwL6lY", "https://picsum.photos/seed/yt9/200/200"));
        list.add(new YouTubeTrack("鲸鱼之歌", "孙燕姿", "RgKAFK5djSk", "https://picsum.photos/seed/yt10/200/200"));
        list.add(new YouTubeTrack("Ocean Blue - Relaxing Music", "John Legend", "6BCxGzXQ1Ys", "https://picsum.photos/seed/yt11/200/200"));
        list.add(new YouTubeTrack("Deep Sea - Ambient", "Adele", "YQHsXMglC9A", "https://picsum.photos/seed/yt12/200/200"));
        list.add(new YouTubeTrack("Coral Reef - Nature Sounds", "Taylor Swift", "e-ORhEE9VVg", "https://picsum.photos/seed/yt13/200/200"));
        list.add(new YouTubeTrack("Moonlight Bay", "Ed Sheeran", "VGpYJ7_eGQU", "https://picsum.photos/seed/yt14/200/200"));
        list.add(new YouTubeTrack("Starfish - Ocean Waves", "Billie Eilish", "ePpPVE-GGJw", "https://picsum.photos/seed/yt15/200/200"));
        list.add(new YouTubeTrack("水舞 - 中国传统音乐", "中央民族乐团", "5ZdlFT7s6EE", "https://picsum.photos/seed/yt16/200/200"));
        list.add(new YouTubeTrack("海之诗 - 钢琴独奏", "李云迪", "4Tr0otuiQuU", "https://picsum.photos/seed/yt17/200/200"));
        list.add(new YouTubeTrack("Wave - Ocean Sound", "Martin Garrix", "9vE-rM6I-2Y", "https://picsum.photos/seed/yt18/200/200"));
        list.add(new YouTubeTrack("潮汐 - 古筝曲", "王中山", "5Y3fSwU1HVM", "https://picsum.photos/seed/yt19/200/200"));
        list.add(new YouTubeTrack("深海 - 交响乐", "久石让", "qLxGflvMZqk", "https://picsum.photos/seed/yt20/200/200"));
        return list;
    }

    private static class YouTubeTrack {
        final String title;
        final String artist;
        final String videoId;
        final String cover;

        YouTubeTrack(String title, String artist, String videoId, String cover) {
            this.title = title;
            this.artist = artist;
            this.videoId = videoId;
            this.cover = cover;
        }
    }

    public static class YouTubeSearchResult {
        public final String videoId;
        public final String title;
        public final String artist;
        public final String cover;

        public YouTubeSearchResult(String videoId, String title, String artist, String cover) {
            this.videoId = videoId;
            this.title = title;
            this.artist = artist;
            this.cover = cover;
        }
    }
}
