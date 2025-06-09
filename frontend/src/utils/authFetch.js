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

  // Handle 401 by clearing all storage and resetting state
  if (res.status === 401) {
    // Clear all storage
    localStorage.clear()
    sessionStorage.clear()
    
    // Clear any cached authentication data
    if (typeof window !== 'undefined') {
      // Clear any authentication cookies by setting them to expire
      document.cookie = "accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;"
      document.cookie = "refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;"
    }
    
    throw new Error("Unauthorized")
  }
  
  return res
}