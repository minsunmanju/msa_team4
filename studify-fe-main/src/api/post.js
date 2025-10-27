import axios from "./axios";

// 모집글 전체 조회
export const getPosts = async () => {
  const res = await axios.get("/api/v1/posts");
  return res.data;
};

// 키워드 모집글 검색
export const searchPosts = async (keyword, type = "all") => {
  const res = await axios.get("/api/v1/posts/search", {
    params: { keyword, type }
  });
  return res.data;
};

// 포지션 기반 모집글 검색
export const searchPostsByPosition = async (position) => {
  const res = await axios.get("/api/v1/posts/search/position", {
    params: { positions: position }
  });
  return res.data;
};

// 모집글 마감 API
export const closePost = async (postId) => {
  // PATCH /api/v1/posts/{postId}
  return axios.patch(`/api/v1/posts/${postId}`);
};

// 모집글 삭제 API
export const deletePost = async (postId) => {
  const token = localStorage.getItem("accessToken");
  return axios.delete(`/api/v1/posts/${postId}`, {
    headers: { Authorization: `Bearer ${token}` }
  });
};

// 모집글 수정 API
export const updatePost = async (postId, postData) => {
  return axios.put(`/api/v1/posts/${postId}`, postData);
};

// 모집글 생성 API
export const createPost = async (postData) => {
  return axios.post("/api/v1/posts", postData);
};