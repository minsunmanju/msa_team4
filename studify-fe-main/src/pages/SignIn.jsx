
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import "../styles/SignIn.css";
import Button from "../components/Button";
import Navbar from "../components/Navbar";
import api from "../api/axios";

function decodeJwtPayload(token = "") {
  try {
    const [, payload] = token.split(".");
    if (!payload) return null;
    const b64 = payload.replace(/-/g, "+").replace(/_/g, "/") + "===".slice((payload.length + 3) % 4);
    return JSON.parse(atob(b64));
  } catch {
    return null;
  }
}


async function resolveUserIdByEmail(email, token) {
  let page = 0;
  const PAGE_SIZE = 100;
  while (page < 50) {
    const res = await api.get("/studify/api/v1/users", {
      params: { page, size: PAGE_SIZE },
      headers: { Authorization: `Bearer ${token}` },
    });
    const list = Array.isArray(res?.data) ? res.data : (res?.data?.content || []);
    const me = list.find((u) => (u?.email || "").toLowerCase() === email.toLowerCase());
    if (me?.id) return me.id;

    const last = Array.isArray(res?.data) ? true : !!res?.data?.last;
    if (last || list.length < PAGE_SIZE) break;
    page += 1;
  }
  return null;
}

export default function SignIn() {
  const [form, setForm] = useState({ email: "", password: "" });
  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
  };

  const login = () => api.post("/api/v1/auth/login", form);

  const onSubmit = async (e) => {
    e.preventDefault();
    setErr("");
    setLoading(true);
    try {
      const res = await login();
      const { accessToken, refreshToken } = res.data;

      localStorage.setItem("accessToken", accessToken);
      localStorage.setItem("refreshToken", refreshToken);

      const payload = decodeJwtPayload(accessToken);
      const email = payload?.sub;

      const foundId = await resolveUserIdByEmail(email, accessToken);
      if (foundId) {
        localStorage.setItem("userId", String(foundId));
      }

      // 인증 상태 변경 이벤트 발생 (WebSocket 연결을 위해 필요)
      window.dispatchEvent(new Event("auth-changed"));

      // 로그인 성공 → 홈으로 이동
      navigate("/");
    } catch (error) {
      console.error("[login error]", error);
      setErr("로그인에 실패했습니다. 이메일/비밀번호를 확인하세요.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app">
      <Navbar />
      <div className="signin">
        <div className="container signin-card">
          <h1>로그인</h1>

          <form className="signin-form" onSubmit={onSubmit} noValidate>
            <label>
              이메일
              <input
                name="email"
                type="email"
                placeholder="rudals831@gmail.com"
                value={form.email}
                onChange={onChange}
                autoComplete="username"
              />
            </label>

            <label>
              비밀번호
              <input
                name="password"
                type="password"
                placeholder="••••••••"
                value={form.password}
                onChange={onChange}
                autoComplete="current-password"
              />
            </label>

            {err && <div className="error" role="alert">{err}</div>}

            <Button variant="primary" type="submit" disabled={loading}>
              {loading ? "로그인 중..." : "로그인"}
            </Button>
          </form>

          <div className="signin-extra">
            <Link to="/">← 홈으로</Link>
            <Link to="/signup">회원가입</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
