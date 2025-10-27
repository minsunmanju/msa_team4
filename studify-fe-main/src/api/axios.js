// src/api/axios.js
import axios from "axios";

// Simple env-based baseURL
// - Local: set REACT_APP_API_URL in .env (e.g., http://localhost:8080)
// - Deploy: set REACT_APP_API_URL in Vercel, or it will fall back to '/api'
const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || '/api',
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
