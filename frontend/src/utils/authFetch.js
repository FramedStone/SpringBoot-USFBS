export async function authFetch(url, options = {}) {
  const accessToken = localStorage.getItem("accessToken");
  const headers = {
    ...(options.headers || {}),
    Authorization: `Bearer ${accessToken}`,
    "Content-Type": "application/json",
  };
  return fetch(url, { ...options, headers });
}