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
    // Only set JSON content-type for requests with a body (e.g., POST/PUT),
    // avoid setting it for simple GET requests to prevent CORS preflight.
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

    if (!result.success) {
        throw new Error(result.message || "注册失败");
    }

    return result;
}

export async function login(username, password) {
    const result = await request("/login", {
        method: "POST",
        body: JSON.stringify({ username, password })
    });

    if (!result.success) {
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

    if (!result.success) {
        throw new Error(result.message || "上传分数失败");
    }

    return result;
}

export async function getLeaderboard() {
    return request("/leaderboard");
}
