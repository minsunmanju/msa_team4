// src/pages/WritePage.jsx
import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "../styles/Write.css";
import { TYPES } from "../mock/posts"; // POSITIONS는 사용 안 함
import Select from "react-select";
import Navbar from "../components/Navbar";
import api from "../api/axios";

export default function WritePage() {
  const nav = useNavigate();
  const { id } = useParams();
  const isEdit = Boolean(id);

  const [form, setForm] = useState({
    title: "",
    type: "STUDY",     // STUDY | PROJECT (백엔드 category는 소문자: study/project)
    position: [],      // ["be","fe",...]
    deadline: "",      // yyyy-MM-dd
    contact: "",       // (백엔드 DTO엔 없음 → 필요시 별도 필드로 확장)
    kakaoLink: "",     // (백엔드 DTO엔 없음 → 필요시 별도 필드로 확장)
    content: "",
    recruitCount: "",  // recruitmentCount
    method: "",        // meetingType
    period: "",        // duration
    language: ""       // techStack(콤마로 받아 분해)
  });
  const [saving, setSaving] = useState(false);
  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(isEdit);

  // ====== 수정 모드: 상세 조회 ======
  useEffect(() => {
    if (!isEdit) return;
    (async () => {
      try {
        setLoading(true);
        // 백엔드 컨트롤러: GET /api/v1/posts/{postId}
        const { data } = await api.get(`/api/v1/posts/${id}`);
        const backend = data?.data ?? data ?? {};

        // 서버 스키마 추정에 맞춰 폼 변환
        const positions = Array.isArray(backend.position)
          ? backend.position
          : Array.isArray(backend.positions)
            ? backend.positions
            : [];

        // deadline은 ISO → yyyy-MM-dd로 잘라서 세팅
        const deadline = backend.deadline
          ? String(backend.deadline).slice(0, 10)
          : "";

        setForm({
          title: backend.title ?? "",
          type: (backend.type ?? backend.category ?? "STUDY").toString().toUpperCase(),
          position: positions, // 이미 ["be","fe"]일 것
          deadline,
          contact: backend.contact ?? "",     // 백엔드 DTO에 없으면 화면용만 유지
          kakaoLink: backend.kakaoLink ?? "", // 동일
          content: backend.content ?? "",
          recruitCount: backend.recruitmentCount?.toString() ?? backend.recruitCount?.toString() ?? "",
          method: backend.meetingType ?? backend.method ?? "",
          period: backend.duration ?? backend.period ?? "",
          language: Array.isArray(backend.techStack) ? backend.techStack.join(", ") : (backend.language ?? "")
        });
      } catch (e) {
        console.error(e);
        setErr("게시글 정보를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    })();
  }, [isEdit, id]);

  // ====== 공통 입력 ======
  const onChange = (e) => {
    const { name, value } = e.target;
    if (name === "position") return; // react-select가 처리
    setForm((f) => ({ ...f, [name]: value }));
  };

  // ====== 제출 ======
  const submit = async (e) => {
    e.preventDefault();
    setErr("");

    if (!form.title.trim()) {
      setErr("제목을 입력해 주세요.");
      return;
    }
    if (form.position.length === 0) {
      setErr("모집 분야를 선택해 주세요.");
      return;
    }

    // 백엔드 DTO(PostRequestDTO)에 맞춘 payload
    const payload = {
      title: form.title.trim(),
      content: form.content.trim(),
      category: form.type.toLowerCase(), // "study" | "project"
      recruitmentCount: form.recruitCount ? Number(form.recruitCount) : null,
      techStack: form.language
        ? form.language.split(",").map((s) => s.trim()).filter(Boolean)
        : [],
      status: "open", // 새 글 기본 값
      deadline: form.deadline ? `${form.deadline}T00:00:00` : null,
      meetingType: form.method.trim(),
      duration: form.period.trim(),
      position: form.position, // ["be","fe",...]
      // authorId는 서버에서 인증 사용자로 결정하므로 불필요
    };

    try {
      setSaving(true);
      if (isEdit) {
        // 백엔드 컨트롤러: PUT /api/v1/posts/{postId}
        await api.put(`/api/v1/posts/${id}`, payload);
        nav(`/posts/${id}`);
      } else {
        // 백엔드 컨트롤러: POST /api/v1/posts
        const { data } = await api.post(`/api/v1/posts`, payload);
        const newId = data?.data?.postId ?? data?.postId;
        nav(newId ? `/posts/${newId}` : "/");
      }
    } catch (e) {
      console.error(e);
      // 응답에 메시지가 있으면 보여주기
      const msg = e?.response?.data?.message || "등록/수정 중 오류가 발생했습니다.";
      setErr(msg);
    } finally {
      setSaving(false);
    }
  };

  // ====== 서버 enum 값(소문자) ======
  const positionOptions = [
    { value: "be", label: "백엔드" },
    { value: "fe", label: "프론트엔드" },
    { value: "pm", label: "PM" },
    { value: "designer", label: "디자이너" },
    { value: "ai", label: "AI" },
    { value: "android", label: "안드로이드" },
    { value: "ios", label: "iOS" },
    { value: "web", label: "웹" },
  ];

  return (
    <div className="write">
      <Navbar />
      <div className="container write-card">
        <div className="write-head">
          <h1>{isEdit ? "게시글 수정" : "팀원 모집하기"}</h1>
        </div>

        {loading ? (
          <div style={{ padding: 24 }}>불러오는 중...</div>
        ) : (
          <form className="write-form" onSubmit={submit} noValidate>
            <label>
              제목
              <input
                name="title"
                placeholder="예: 팀원 모집"
                value={form.title}
                onChange={onChange}
                disabled={saving}
              />
            </label>

            <div className="row2">
              <label>
                구분
                <select
                  name="type"
                  value={form.type}
                  onChange={onChange}
                  disabled={saving}
                >
                  {TYPES.filter((t) => t.key !== "ALL").map((t) => (
                    <option key={t.key} value={t.key}>{t.label}</option>
                  ))}
                </select>
              </label>

              <label>
                모집 분야
                <Select
                  classNamePrefix="react-select"
                  isMulti
                  options={positionOptions}
                  value={positionOptions.filter(opt => form.position.includes(opt.value))}
                  onChange={(selected) =>
                    setForm((f) => ({
                      ...f,
                      position: (selected ?? []).map((opt) => opt.value),
                    }))
                  }
                  isDisabled={saving}
                  placeholder="포지션 선택"
                  closeMenuOnSelect={false}
                />
              </label>
            </div>

            <label>
              마감일
              <input
                type="date"
                name="deadline"
                value={form.deadline}
                onChange={onChange}
                disabled={saving}
              />
            </label>

            <label>
              연락 방법
              <input
                name="contact"
                value={form.contact}
                onChange={onChange}
                disabled={saving}
              />
            </label>

            <label>
              오카방 링크
              <input
                name="kakaoLink"
                value={form.kakaoLink}
                onChange={onChange}
                disabled={saving}
              />
            </label>

            <label>
              소개글
              <textarea
                name="content"
                value={form.content}
                onChange={onChange}
                rows={8}
                disabled={saving}
              />
            </label>

            <label>
              모집 인원
              <input
                name="recruitCount"
                value={form.recruitCount}
                onChange={onChange}
                disabled={saving}
                placeholder="예: 1"
                inputMode="numeric"
              />
            </label>

            <label>
              진행 방식
              <input
                name="method"
                value={form.method}
                onChange={onChange}
                disabled={saving}
                placeholder="예: 온/오프라인"
              />
            </label>

            <label>
              예상 기간
              <input
                name="period"
                value={form.period}
                onChange={onChange}
                disabled={saving}
                placeholder="예: 장기"
              />
            </label>

            <label>
              기술 스택
              <input
                name="language"
                value={form.language}
                onChange={onChange}
                disabled={saving}
                placeholder="예: Java, React"
              />
            </label>

            {err && <div className="error">{err}</div>}

            <div className="actions">
              <button
                type="button"
                className="btn ghost"
                onClick={() => nav(-1)}
                disabled={saving}
              >
                취소
              </button>
              <button type="submit" className="btn primary" disabled={saving}>
                {saving ? (isEdit ? "수정 중..." : "등록 중...") : (isEdit ? "저장" : "등록")}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
