import { createContext, useContext, useState, useRef, useCallback } from "react";

const MAX_QUEUE_SIZE = 10;

const RequestQueueContext = createContext();

export function useRequestQueue() {
  return useContext(RequestQueueContext);
}

export function RequestQueueProvider({ children }) {
  const [queue, setQueue] = useState([]); // [{id, label, fn, status, result, error}]
  const [activeJob, setActiveJob] = useState(null);
  const queueRef = useRef(queue);

  // Add a job to the queue (max 10)
  const addJob = useCallback((label, fn) => {
    if (queueRef.current.length >= MAX_QUEUE_SIZE) {
      throw new Error("Queue is full. Please wait for some jobs to finish.");
    }
    const id = `${Date.now()}-${Math.random()}`;
    setQueue(q => [...q, { id, label, fn, status: "queued", result: null, error: null }]);
    return id;
  }, []);

  // Cancel a job by id (only if queued or error)
  const cancelJob = useCallback((id) => {
    setQueue(q => q.filter(j => j.id !== id || j.status === "running"));
  }, []);

  // Process jobs sequentially
  const processQueue = useCallback(async () => {
    if (activeJob || queueRef.current.length === 0) return;
    const job = queueRef.current[0];
    setActiveJob(job);
    setQueue(q => q.map(j => j.id === job.id ? { ...j, status: "running" } : j));
    try {
      const result = await job.fn();
      setQueue(q => q.map(j => j.id === job.id ? { ...j, status: "done", result } : j));
    } catch (error) {
      setQueue(q => q.map(j => j.id === job.id ? { ...j, status: "error", error } : j));
    } finally {
      setTimeout(() => {
        setQueue(q => q.slice(1));
        setActiveJob(null);
      }, 1000); // Short delay for UX
    }
  }, [activeJob]);

  // Watch for queue changes and process
  queueRef.current = queue;
  if (!activeJob && queue.length > 0) {
    processQueue();
  }

  return (
    <RequestQueueContext.Provider value={{ addJob, queue, activeJob, cancelJob }}>
      {children}
      <RequestQueueModal queue={queue} cancelJob={cancelJob} />
    </RequestQueueContext.Provider>
  );
}

// Modal UI
function RequestQueueModal({ queue, cancelJob }) {
  const running = queue.filter(j => j.status === "running" || j.status === "queued");
  if (running.length === 0) return null;
  return (
    <div style={{
      position: "fixed",
      bottom: 24,
      right: 24,
      zIndex: 9999,
      background: "#fff",
      border: "1px solid #e5e7eb",
      borderRadius: 8,
      boxShadow: "0 2px 12px rgba(0,0,0,0.12)",
      minWidth: 320,
      padding: 16,
      fontSize: 14
    }}>
      <div style={{ fontWeight: 600, marginBottom: 8 }}>Background Processes</div>
      <ul style={{ margin: 0, padding: 0, listStyle: "none" }}>
        {queue.map(job => (
          <li key={job.id} style={{ marginBottom: 6, color: job.status === "error" ? "#dc2626" : "#111827", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
            <span>
              {job.label} —
              {job.status === "queued" && <span style={{ color: "#f59e42" }}> Queued</span>}
              {job.status === "running" && <span style={{ color: "#3b82f6" }}> Running...</span>}
              {job.status === "done" && <span style={{ color: "#16a34a" }}> Done</span>}
              {job.status === "error" && <span style={{ color: "#dc2626" }}> Error</span>}
            </span>
            {(job.status === "queued" || job.status === "error") && (
              <button
                style={{
                  marginLeft: 12,
                  background: "#ef4444",
                  color: "#fff",
                  border: "none",
                  borderRadius: 4,
                  padding: "2px 8px",
                  cursor: "pointer",
                  fontSize: 12
                }}
                onClick={() => cancelJob(job.id)}
                aria-label="Cancel job"
              >
                Cancel
              </button>
            )}
          </li>
        ))}
      </ul>
      <div style={{ fontSize: 12, color: "#6b7280", marginTop: 8 }}>
        {queue.length}/{MAX_QUEUE_SIZE} in queue
      </div>
    </div>
  );
}