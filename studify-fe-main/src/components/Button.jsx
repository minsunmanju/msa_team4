import React from "react";
import "../styles/Button.css";

export default function Button({ children, onClick, variant = "default", type = "button" }) {
  return (
    <button className={`btn ${variant}`} type={type} onClick={onClick}>
      {children}
    </button>
  );
}