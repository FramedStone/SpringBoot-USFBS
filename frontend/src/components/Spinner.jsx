/**
 * Simple loading spinner for auth and data loading states.
 */
export default function Spinner() {
  return (
    <div style={{ display: "flex", justifyContent: "center", marginTop: "3rem" }}>
      <div className="spinner" style={{ fontSize: 32, color: "#556EE6" }}>
        <span className="visually-hidden">Loading...</span>
        <svg width="40" height="40" viewBox="0 0 40 40">
          <circle
            cx="20"
            cy="20"
            r="16"
            stroke="#556EE6"
            strokeWidth="4"
            fill="none"
            strokeDasharray="80"
            strokeDashoffset="60"
            strokeLinecap="round"
          >
            <animateTransform
              attributeName="transform"
              type="rotate"
              from="0 20 20"
              to="360 20 20"
              dur="1s"
              repeatCount="indefinite"
            />
          </circle>
        </svg>
      </div>
    </div>
  );
}