import { ddayStr, isClosedUI } from "../utils/date";
import { Link } from "react-router-dom";  
import "../styles/PostCard.css";

export default function PostCard({ e, commentCount }) {  
  const d = ddayStr(e.deadline);
  const closed = isClosedUI(e.isClosed, e.deadline);

  return (
    <Link className="card" to={`/posts/${e.id}`}> 
      <div className="card-top">
        <span className={`badge ${e.type === "STUDY" ? "badge-study" : "badge-project"}`}>
          {e.type === "STUDY" ? "스터디" : "프로젝트"}
        </span>
        {d && <span className="badge">{d}</span>}
        {closed && <span className="badge muted">모집 마감</span>}
      </div>

      <h3 className="card-title">{e.title}</h3>
      <div className="card-sub">
        {(Array.isArray(e.position) ? e.position : [e.position])
              .filter(Boolean)
              .join(", ")}
      </div>
      <div className="card-lang">
        {e.language}
      </div>
        

  <hr className="card-divider" />
  

      <div className="card-foot">
        <span className="card-writer">{e.author}</span>
        <span>댓글 {commentCount}</span>
      </div>
    </Link>
  );
}
