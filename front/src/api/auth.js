import http from './http'

export function login(data) {
  return http.post('/auth/login', data)
}

export function fetchInfo() {
  return http.get('/auth/info')
}

export function register(data) {
  return http.post('/auth/register', data)
}

export function sendVerificationCode(data) {
  return http.post('/auth/send-verification-code', data)
}

export function emailLogin(data) {
  return http.post('/auth/email-login', data)
}

export function changePassword(data) {
  return http.post('/auth/change-password', data)
}

export function updateProfile(data) {
  return http.put('/auth/profile', data)
}
