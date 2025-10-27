// Backup of axios.js with Vercel proxy logic (2025-10-27)
// This version forces '/api' on vercel.app so that requests are proxied via vercel.json.
import axios from "axios";

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
