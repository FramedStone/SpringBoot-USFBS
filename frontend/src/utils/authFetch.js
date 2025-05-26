export async function authFetch(url, options = {}) {
  return fetch(url, {
    ...options,
    credentials: "include", // send cookies
    headers: {
      ...(options.headers || {}),
      "Content-Type": "application/json",
    },
  });
}