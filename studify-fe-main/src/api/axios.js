// src/api/axios.js
import axios from "axios";

// 환경에 따라 기본 URL 설정
const getBaseURL = () => {
  if (process.env.NODE_ENV === 'production') {
    return 'https://msa-red.vercel.app';
  }
  // 개발 환경에서는 API Gateway 직접 호출
  return 'http://localhost:8080';
};

const api = axios.create({
  baseURL: getBaseURL(),
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
