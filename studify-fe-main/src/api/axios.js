// src/api/axios.js
import axios from "axios";

// Vercel(https)에서 http 백엔드를 직접 호출하면 혼합 콘텐츠로 차단됩니다.
// 배포 환경(vercel.app)에서는 항상 '/api'로 호출해 vercel.json 리라이트를 타도록 하고,
// 로컬 개발에서는 REACT_APP_API_URL(예: http://localhost:8080) 값을 사용합니다.
const isVercel = typeof window !== 'undefined' && /\.vercel\.app$/.test(window.location.hostname);
const baseURL = isVercel ? '/api' : (process.env.REACT_APP_API_URL || '/api');

const api = axios.create({
  baseURL,
  withCredentials: true,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
