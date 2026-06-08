const API_BASE_URL = "/api";

async function parseResponse(response) {
    const text = await response.text();
    let data = null;

    if (text) {
        try {
            data = JSON.parse(text);
        } catch (err) {
            throw new Error(`请求失败: ${response.status} ${response.statusText}`);
        }
    }

    if (!response.ok) {
        if (data && data.message) {
            throw new Error(data.message);
        }
        throw new Error(`请求失败: ${response.status} ${response.statusText}`);
    }

    return data;
}

async function request(path, options = {}) {
    const headers = {
        ...(options.headers || {})
    };

    const method = (options.method || 'GET').toUpperCase();
    if (options.body && !headers['Content-Type'] && !headers['content-type']) {
        headers['Content-Type'] = 'application/json';
    }

    const response = await fetch(`${API_BASE_URL}${path}`, {
        headers,
        ...options,
        method
    });

    return parseResponse(response);
}

export async function register(username, password) {
    const result = await request("/register", {
        method: "POST",
        body: JSON.stringify({ username, password })
    });

    if (result.code !== 200) {
        throw new Error(result.message || "注册失败");
    }

    return result;
}

export async function login(username, password) {
    const result = await request("/login", {
        method: "POST",
        body: JSON.stringify({ username, password })
    });

    if (result.code !== 200) {
        throw new Error(result.message || "登录失败");
    }

    if (result.data) {
        localStorage.setItem("token", result.data.token);
        localStorage.setItem("username", result.data.username);
    }

    return result;
}

export async function uploadScore(score) {
    const token = localStorage.getItem("token");
    const username = localStorage.getItem("username");

    if (!token) {
        throw new Error("未登录或令牌已失效，请重新登录");
    }

    const response = await fetch(`${API_BASE_URL}/score`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({
            username: username,
            score: score
        })
    });

    const result = await parseResponse(response);

    if (result.code !== 200) {
        throw new Error(result.message || "上传分数失败");
    }

    return result;
}

export async function getLeaderboard() {
    return request("/leaderboard");
}

export async function searchMusic(keyword, offset = 0, limit = 20) {
    return request(`/music/search?keyword=${encodeURIComponent(keyword)}&offset=${offset}&limit=${limit}`);
}

export async function getPlayUrl(songId) {
    return request(`/music/play/${songId}`);
}

export async function getFavorites() {
    const token = localStorage.getItem("token");
    if (!token) return { code: 401, message: "未登录", data: [] };
    const response = await fetch(`${API_BASE_URL}/music/favorite/list`, {
        headers: { "Authorization": `Bearer ${token}` }
    });
    return parseResponse(response);
}

export async function addFavorite(songId, track) {
    const token = localStorage.getItem("token");
    const response = await fetch(`${API_BASE_URL}/music/favorite/${songId}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify(track)
    });
    return parseResponse(response);
}

export async function removeFavorite(songId) {
    const token = localStorage.getItem("token");
    const response = await fetch(`${API_BASE_URL}/music/favorite/${songId}`, {
        method: "DELETE",
        headers: { "Authorization": `Bearer ${token}` }
    });
    return parseResponse(response);
}
