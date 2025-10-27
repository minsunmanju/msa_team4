import Select from "./Select";
import "../styles/Search.css";

export default function Filters({ type, setType, position, setPosition, TYPES, POSITIONS }) {
  return (
    <div className="filters" style={{ margin: '20px 0', display: 'flex', gap: '16px' }}>
      <Select label="구분" options={TYPES} value={type} onChange={setType} />
      <Select label="포지션" options={POSITIONS} value={position} onChange={setPosition} />
    </div>
  );
}