import http from './http'

export function listCampus() {
  return http.get('/system/campuses')
}

export function fetchCampusPage(params) {
  return http.get('/system/campuses/page', { params })
}

export function fetchCampusById(campusId) {
  return http.get(`/system/campuses/${campusId}`)
}

export function addCampus(data) {
  return http.post('/system/campuses', data)
}

export function updateCampus(data) {
  return http.put('/system/campuses', data)
}

export function deleteCampus(campusId) {
  return http.delete(`/system/campuses/${campusId}`)
}