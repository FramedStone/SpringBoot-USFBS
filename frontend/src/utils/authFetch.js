export async function authFetch(url, options = {}) {
  const { headers = {}, body, ...rest } = options

  const finalHeaders = { ...headers }
  // only apply JSON content-type when body is a plain object/string
  if (body != null && !(body instanceof FormData)) {
    finalHeaders["Content-Type"] = "application/json"
  }

  const res = await fetch(url, {
    ...rest,
    credentials: "include",
    headers: finalHeaders,
    body,
  })

  // bubble up 401 so ProtectedRoute.catch() can set isAuth=false
  if (res.status === 401) throw new Error("Unauthorized")
  return res
}