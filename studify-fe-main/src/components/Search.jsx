import { useState, useEffect } from "react";
import { searchPosts, searchPostsByPosition } from "../api/post";
import "../styles/Search.css";

export default function Search({
  q, setQ, type, setType, position, setPosition, TYPES, POSITIONS, onSearchResult
}) {
  const [loading, setLoading] = useState(false);

  const handleSearch = async () => {
    if (!q.trim()) return;
    setLoading(true);
    try {
      const result = await searchPosts(q, "all");
      if (onSearchResult) onSearchResult(result);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    // position이 "ALL" 또는 "all"이 아니면 포지션 검색 API 호출
    if (position && position !== "ALL" && position !== "all") {
      (async () => {
        const result = await searchPostsByPosition(position);
        if (onSearchResult) onSearchResult(result);
      })();
    }
    // position이 "ALL" 또는 "all"이면 전체 목록을 다시 불러오거나, 기존 검색 유지
  }, [position]);

  return (
    <div className="search">
      <div className="container">
        <div className="search-banner">
          <h1 className="search-title">
            <span style={{ fontWeight: 'bold', fontFamily: 'cookierunfont, sans-serif' }}>
              IT 팀원 찾기는 스터디파이에서
            </span>
          </h1>
        </div>

        <div className="search-row">
          <div className="search-box">
            <span className="search-ico">🔎</span>
            <input
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="(예: 백엔드 )"
              onKeyDown={e => e.key === "Enter" && handleSearch()}
            />
            <button className="btn dark" onClick={handleSearch} disabled={loading}>
              {loading ? "검색 중..." : "검색"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
