import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import "../styles/SignUp.css";
import Button from "../components/Button";
import Navbar from "../components/Navbar";
import api from "../api/axios";

export default function SignUp() {
  const [form, setForm] = useState({
    email: "",
    password: "",
    confirmPassword: "",
    nickname: "",
  });
  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
  };

  const validate = () => {
    const emailOk = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim());
    if (!emailOk) return "올바른 이메일 형식을 입력해 주세요.";
    if (form.password.length < 8) return "비밀번호는 8자 이상이어야 합니다."; 
    if (form.password !== form.confirmPassword) return "비밀번호가 일치하지 않습니다.";
    return "";
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setErr("");


    const payload = {
      email: form.email.trim(),
      password: form.password,
      nickname: form.nickname.trim(),
    };

    setLoading(true);
    try {
      const res = await api.post("/api/v1/users/signup", payload);

      const created = res?.data;
      if (created?.id) {
        localStorage.setItem("userId", String(created.id));
      }
      if (created?.email) {
        localStorage.setItem("userEmail", created.email);
      }
      if (created?.nickname) {
        localStorage.setItem("nickname", created.nickname);
      }



      alert("회원가입이 완료되었습니다. 로그인 해주세요.");

      window.dispatchEvent(new Event("auth-changed"));
      navigate("/signin");
    } catch (error) {
      const status = error?.response?.status;
      if (status === 409) setErr("이미 등록된 이메일입니다.");
      else if (status === 400) setErr("입력값을 확인해 주세요.");
      else setErr("회원가입에 실패했습니다. 잠시 후 다시 시도해 주세요.");
      console.error("[signup error]", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app">
      <Navbar />
      <div className="signup">
        <div className="container signup-card">
          <h1>회원가입</h1>

          <form className="signup-form" onSubmit={onSubmit} noValidate>
            <label>
              이메일
              <input
                name="email"
                type="email"
                placeholder="example@email.com"
                value={form.email}
                onChange={onChange}
                autoComplete="email"
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
                autoComplete="new-password"
              />
            </label>

            <label>
              비밀번호 확인
              <input
                name="confirmPassword"
                type="password"
                placeholder="••••••••"
                value={form.confirmPassword}
                onChange={onChange}
                autoComplete="new-password"
              />
            </label>

            <label>
              닉네임
              <input
                name="nickname"
                type="text"
                placeholder="닉네임을 입력하세요"
                value={form.nickname}
                onChange={onChange}
                autoComplete="nickname"
              />
            </label>

            {err && <div className="error" role="alert">{err}</div>}

            <Button variant="primary" type="submit" disabled={loading}>
              {loading ? "가입 중..." : "가입하기"}
            </Button>
          </form>

          <div className="signup-extra">
            <Link to="/">← 홈으로</Link>
            <Link to="/login">로그인</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
