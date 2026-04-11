import http from './http'

// ---------- 课题 ----------
export function fetchTopicPage(params) {
  return http.get('/project/topics', { params })
}

export function fetchTopicById(topicId) {
  return http.get(`/project/topics/${topicId}`)
}

export function addTopic(data) {
  return http.post('/project/topics', data)
}

export function updateTopic(data) {
  return http.put('/project/topics', data)
}

export function deleteTopic(topicId) {
  return http.delete(`/project/topics/${topicId}`)
}

export function toggleTopicStatus(topicId) {
  return http.put(`/project/topics/toggle-status/${topicId}`)
}

export function exportTopic(params) {
  return http.get('/project/topics/export', { params, responseType: 'blob' })
}

export function importTopic(formData) {
  return http.post('/project/topics/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// ---------- 选题申请 ----------
export function fetchCampuses() {
  return http.get('/project/selections/campuses')
}

export function fetchTeachers(params) {
  return http.get('/project/selections/teachers', { params })
}

export function fetchTopicBank(params) {
  return http.get('/project/selections/topic-bank', { params })
}

export function applySelection(data) {
  return http.post('/project/selections/apply', data)
}

export function fetchMySelection() {
  return http.get('/project/selections/my')
}

export function withdrawSelection(selectionId) {
  return http.put(`/project/selections/withdraw/${selectionId}`)
}

// ---------- 选题管理 ----------
export function fetchSelectionPage(params) {
  return http.get('/project/selections', { params })
}

export function approveSelection(data) {
  return http.put('/project/selections/approve', data)
}

export function rejectSelection(data) {
  return http.put('/project/selections/reject', data)
}

export function fetchTeacherApprovals(params) {
  return http.get('/project/selections/teacher-approvals', { params })
}

// ---------- 项目进度 ----------
export function fetchMyProgressPage(params) {
  return http.get('/project/progress/my', { params })
}

export function fetchProgressBySelection(selectionId) {
  return http.get(`/project/progress/selection/${selectionId}`)
}

export function addProgress(data) {
  return http.post('/project/progress', data)
}

export function updateProgress(data) {
  return http.put('/project/progress', data)
}

export function deleteProgress(progressId) {
  return http.delete(`/project/progress/${progressId}`)
}

export function fetchAdminProgressPage(params) {
  return http.get('/project/progress/admin/page', { params })
}

export function fetchTeacherProgressPage(params) {
  return http.get('/project/progress/teacher/page', { params })
}

// ---------- 模板文件 ----------
export function fetchTemplatePage(params) {
  return http.get('/doc/template', { params })
}

export function uploadTemplate(formData) {
  return http.post('/doc/template', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function updateTemplate(data) {
  return http.put('/doc/template', data)
}

export function deleteTemplate(templateId) {
  return http.delete(`/doc/template/${templateId}`)
}

export function downloadTemplate(templateId) {
  return http.get(`/doc/template/download/${templateId}`, { responseType: 'blob' })
}

export function fetchTemplateDetail(templateId) {
  return http.get(`/doc/template/${templateId}`)
}

// ---------- 开题报告 ----------
export function fetchMyProposal() {
  return http.get('/project/proposal/my')
}

export function fetchMyProposalList() {
  return http.get('/project/proposal/my/list')
}

export function submitProposal(data) {
  return http.post('/project/proposal', data)
}

export function recallProposal(proposalId) {
  return http.put(`/project/proposal/recall/${proposalId}`)
}

export function fetchAdminProposalPage(params) {
  return http.get('/project/proposal/admin/page', { params })
}

export function fetchTeacherProposalPage(params) {
  return http.get('/project/proposal/teacher/page', { params })
}

export function reviewProposal(data) {
  return http.put('/project/proposal/review', data)
}

export function fetchProposalDetail(proposalId) {
  return http.get(`/project/proposal/${proposalId}`)
}

// ---------- 中期检查 ----------
export function fetchMyMidTerm() {
  return http.get('/project/midterm/my')
}

export function fetchMyMidTermList() {
  return http.get('/project/midterm/my/list')
}

export function submitMidTerm(data) {
  return http.post('/project/midterm', data)
}

export function recallMidTerm(midId) {
  return http.put(`/project/midterm/recall/${midId}`)
}

export function fetchAdminMidTermPage(params) {
  return http.get('/project/midterm/admin/page', { params })
}

export function fetchTeacherMidTermPage(params) {
  return http.get('/project/midterm/teacher/page', { params })
}

export function reviewMidTerm(data) {
  return http.put('/project/midterm/review', data)
}

export function fetchMidTermDetail(midId) {
  return http.get(`/project/midterm/${midId}`)
}

// ---------- 文件 ----------
export function downloadDocFile(fileId) {
  return http.get(`/doc/file/download/${fileId}`, { responseType: 'blob' })
}

export function deleteDocFile(fileId) {
  return http.delete(`/doc/file/${fileId}`)
}

// ---------- 论文文档 ----------
export function fetchMyThesis() {
  return http.get('/project/thesis/my')
}

export function fetchMyThesisList() {
  return http.get('/project/thesis/my/list')
}

export function submitThesis(formData) {
  return http.post('/project/thesis', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function recallThesis(thesisId) {
  return http.put(`/project/thesis/recall/${thesisId}`)
}

export function fetchAdminThesisPage(params) {
  return http.get('/project/thesis/admin/page', { params })
}

export function fetchTeacherThesisPage(params) {
  return http.get('/project/thesis/teacher/page', { params })
}

export function reviewThesis(data) {
  return http.put('/project/thesis/review', data)
}

export function fetchThesisDetail(thesisId) {
  return http.get(`/project/thesis/${thesisId}`)
}

// ---------- 文档版本管理 ----------
export function fetchMyVersionList(selectionId) {
  return http.get('/project/doc-version/my/list', { params: { selectionId } })
}

export function submitVersion(formData) {
  return http.post('/project/doc-version', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function deleteVersion(versionId) {
  return http.delete(`/project/doc-version/${versionId}`)
}

export function fetchAdminVersionPage(params) {
  return http.get('/project/doc-version/admin/page', { params })
}

export function fetchTeacherVersionPage(params) {
  return http.get('/project/doc-version/teacher/page', { params })
}

export function fetchVersionDetail(versionId) {
  return http.get(`/project/doc-version/${versionId}`)
}

// ---------- 文档归档管理 ----------
export function fetchMyArchiveList(selectionId) {
  return http.get('/project/doc-archive/my/list', { params: { selectionId } })
}

export function checkArchiveSubmit(selectionId, stageName) {
  return http.get('/project/doc-archive/check-submit', { params: { selectionId, stageName } })
}

export function submitArchive(formData) {
  return http.post('/project/doc-archive', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function recallArchive(archiveId) {
  return http.put(`/project/doc-archive/recall/${archiveId}`)
}

export function fetchAdminArchivePage(params) {
  return http.get('/project/doc-archive/admin/page', { params })
}

export function fetchTeacherArchivePage(params) {
  return http.get('/project/doc-archive/teacher/page', { params })
}

export function reviewArchive(data) {
  return http.put('/project/doc-archive/review', data)
}

export function fetchArchiveDetail(archiveId) {
  return http.get(`/project/doc-archive/${archiveId}`)
}

// ---------- 成果提交管理 ----------
export function fetchMyAchievement(selectionId) {
  return http.get('/achievement/submit/my', { params: { selectionId } })
}

export function submitAchievement(formData) {
  return http.post('/achievement/submit', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function deleteAchievement(submitId) {
  return http.delete(`/achievement/submit/${submitId}`)
}

export function fetchAdminAchievementPage(params) {
  return http.get('/achievement/submit/admin/page', { params })
}

export function fetchTeacherAchievementPage(params) {
  return http.get('/achievement/submit/teacher/page', { params })
}

export function fetchAchievementDetail(submitId) {
  return http.get(`/achievement/submit/${submitId}`)
}
