const API_BASE_URL = "https://fishgame-production-2c91.up.railway.app/api";

async function request(path, options = {}) {
    const response = await fetch(`${API_BASE_URL}${path}`, {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    });

    return response.json();
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

    const result = await fetch(`${API_BASE_URL}/score`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({
            username: username,
            score: score
        })
    }).then(response => response.json());

    if (!result.success) {
        throw new Error(result.message || "上传分数失败");
    }

    return result;
}

export async function getLeaderboard() {
    return request("/leaderboard");
}
