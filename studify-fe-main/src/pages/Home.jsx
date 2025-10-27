import { useMemo, useState, useEffect } from "react";
import Navbar from "../components/Navbar";
import Search from "../components/Search";
import Filters from "../components/Filters";
import PostCard from "../components/PostCard";
import "../App.css";
import "../styles/List.css";
import "../styles/Footer.css";
import Button from "../components/Button";
import { POSITIONS, TYPES } from "../mock/posts";
import { Link } from "react-router-dom";
import api from "../api/axios";

export default function Home() {
  const [q, setQ] = useState("");
  const [type, setType] = useState("ALL");
  const [position, setPosition] = useState("ALL");
  const [showOpenOnly, setShowOpenOnly] = useState(false);

  const [posts, setPosts] = useState([]); // ✅ 초기값: 빈 배열
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");

  // 직군명 통일
  const normalizePosKey = (raw) => {
    if (raw == null) return null;
    const s = String(raw).trim();

    const kor = { "백엔드": "BE", "프론트엔드": "FE", "디자이너": "DESIGNER", "안드로이드": "ANDROID", "웹": "WEB" };
    if (kor[s]) return kor[s];

    const valToKey = {
      be: "BE", fe: "FE", pm: "PM", designer: "DESIGNER",
      ai: "AI", android: "ANDROID", ios: "IOS", web: "WEB"
    };
    const low = s.toLowerCase();
    if (valToKey[low]) return valToKey[low];

    const up = s.toUpperCase();
    const keys = ["BE", "FE", "PM", "DESIGNER", "AI", "ANDROID", "IOS", "WEB", "ALL"];
    if (keys.includes(up)) return up;

    return null;
  };

  const normalizePosList = (v) => {
    const arr = Array.isArray(v)
      ? v
      : Array.isArray(v?.positions)
      ? v.positions
      : Array.isArray(v?.position)
      ? v.position
      : v?.positions ?? v?.position ?? v ?? [];

    const list = Array.isArray(arr) ? arr : [arr].filter(Boolean);
    const keys = list.map(normalizePosKey).filter(Boolean);
    return [...new Set(keys)];
  };

  // ✅ 게시글 불러오기
  useEffect(() => {
    (async () => {
      try {
        setLoading(true);
        const { data } = await api.get("/api/v1/posts");

        // ⚙️ 응답이 배열이 아닐 때 대비
        const postsArray = Array.isArray(data) ? data : data?.data ?? [];

        const normalized = (postsArray ?? []).map((p) => {
          const posKeys = normalizePosList(p.positions ?? p.position);
          return {
            ...p,
            id: p.postId,
            type: p.category?.toUpperCase(), // STUDY / PROJECT
            positions: posKeys,
            language: Array.isArray(p.techStack)
              ? p.techStack.join(", ")
              : (p.techStack ?? ""),
            isClosed: p.status === "CLOSED",
            author: p.nickname ?? "익명",
            commentCount: p.commentCount ?? 0,
          };
        });

        setPosts(normalized);
      } catch (e) {
        console.error(e);
        setErr(e?.response?.data?.message ?? "목록 조회 실패");
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const matchPosition = (postPos, selected) => {
    const sel = normalizePosKey(selected) || "ALL";
    if (sel === "ALL") return true;
    const keys = normalizePosList(postPos);
    return keys.includes(sel);
  };

  const isClosed = (p) => {
    if (p?.isClosed) return true;
    if (!p?.deadline) return false;
    const s = String(p.deadline).trim();
    const norm = s.replace(/\./g, "-");
    const d = /^\d{4}-\d{2}-\d{2}$/.test(norm) ? new Date(norm) : new Date(norm);
    if (isNaN(d)) return false;
    d.setHours(23, 59, 59, 999);
    return Date.now() > d.getTime();
  };

  // ✅ 안전 필터 처리
  const filtered = useMemo(() => {
    const safePosts = Array.isArray(posts) ? posts : [];
    const qLower = q.trim().toLowerCase();

    return safePosts.filter((p) => {
      const byType = type === "ALL" || p.type === type;
      const byPos = matchPosition(p.positions, position);
      const byQ =
        !qLower ||
        String(p.title ?? "").toLowerCase().includes(qLower) ||
        String(p.content ?? "").toLowerCase().includes(qLower);
      const openOnly = !showOpenOnly || !isClosed(p);
      return byType && byPos && byQ && openOnly;
    });
  }, [posts, q, type, position, showOpenOnly]);

  // ----- 페이징 -----
  const PAGE_SIZE = 15;
  const [page, setPage] = useState(1);
  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const pagedPosts = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  const goPage = (p) => {
    if (p < 1 || p > totalPages) return;
    setPage(p);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  useEffect(() => setPage(1), [q, type, position, showOpenOnly]);
  useEffect(() => {
    if (page > totalPages) setPage(totalPages || 1);
  }, [totalPages, page]);

  if (loading) return <div style={{ padding: 24 }}>로딩 중…</div>;
  if (err) return <div style={{ padding: 24, color: "red" }}>{err}</div>;

  return (
    <div className="app">
      <Navbar />

      <Search
        q={q}
        setQ={setQ}
        type={type}
        setType={setType}
        position={position}
        setPosition={setPosition}
        TYPES={TYPES}
        POSITIONS={POSITIONS}
      />

      <main className="container">
        <div
          className="list-head"
          style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}
        >
          <h2>게시글 목록</h2>
          <button
            onClick={() => setShowOpenOnly((v) => !v)}
            style={{
              padding: "6px 12px",
              borderRadius: 8,
              border: "1px solid #ddd",
              background: showOpenOnly ? "#7c3aed" : "#fff",
              color: showOpenOnly ? "#fff" : "#222",
              fontWeight: 600,
              cursor: "pointer",
            }}
            aria-pressed={showOpenOnly}
            aria-label="모집중만 토글"
            title={showOpenOnly ? "모집중만 보기 해제" : "모집중만 보기"}
          >
            {showOpenOnly ? "모집중만" : "전체 보기"}
          </button>
        </div>

        <Filters
          type={type}
          setType={setType}
          position={position}
          setPosition={setPosition}
          TYPES={TYPES}
          POSITIONS={POSITIONS}
        />

        <div className="grid">
          {pagedPosts.map((e) => (
            <PostCard
              key={e.id}
              e={{
                ...e,
                position: e.positions,
                isClosed: isClosed(e),
              }}
              commentCount={e.commentCount}
            />
          ))}
        </div>

        {totalPages > 1 && (
          <div
            style={{
              display: "flex",
              justifyContent: "center",
              gap: 8,
              margin: "32px 0",
            }}
          >
            <button
              onClick={() => goPage(page - 1)}
              disabled={page === 1}
              style={{
                padding: "6px 14px",
                borderRadius: 8,
                border: "1px solid #ddd",
                background: "#fff",
                cursor: page === 1 ? "not-allowed" : "pointer",
              }}
            >
              &lt;
            </button>
            {Array.from({ length: totalPages }, (_, i) => (
              <button
                key={i + 1}
                onClick={() => goPage(i + 1)}
                style={{
                  padding: "6px 14px",
                  borderRadius: 8,
                  border: "1px solid #ddd",
                  background: page === i + 1 ? "#7c3aed" : "#fff",
                  color: page === i + 1 ? "#fff" : "#222",
                  fontWeight: page === i + 1 ? 700 : 500,
                  cursor: "pointer",
                }}
                aria-current={page === i + 1 ? "page" : undefined}
              >
                {i + 1}
              </button>
            ))}
            <button
              onClick={() => goPage(page + 1)}
              disabled={page === totalPages}
              style={{
                padding: "6px 14px",
                borderRadius: 8,
                border: "1px solid #ddd",
                background: "#fff",
                cursor: page === totalPages ? "not-allowed" : "pointer",
              }}
            >
              &gt;
            </button>
          </div>
        )}
      </main>

      <footer className="footer">
        <div className="container footer-inner">
          <div className="footer-links">
            <a href="/members">만든 사람들</a>
          </div>
        </div>
      </footer>
    </div>
  );
}

function isClosed(post) {
  if (post.status === "closed") return true;
  if (post.deadline && new Date(post.deadline) < new Date()) return true;
  return false;
}
