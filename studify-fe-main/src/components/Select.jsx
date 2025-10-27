import "../styles/Select.css";

export default function Select({ label, options, value, onChange }) {
  return (
    <div className="pills">
      <span className="pills-label">{label}</span>
      <div className="pills-row">
        {options.map((e) => (
          <button
            key={e.key}
            onClick={() => onChange(e.key)}
            className={`pill ${value === e.key ? "active" : ""}`}>
            {e.label}
          </button>
        ))}
      </div>
    </div>
  );
}
