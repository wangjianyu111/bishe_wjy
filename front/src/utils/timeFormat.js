/**
 * 时间格式化工具 — 基于浏览器本地时区
 * 统一处理所有界面时间显示，避免 UTC 时间显示不一致问题
 */

/**
 * 补零
 */
function pad(n) {
  return String(n).padStart(2, '0')
}

/**
 * 获取本地时区偏移后的 Date 对象
 * @param {string|Date} value - ISO 字符串或 Date 对象
 * @returns {Date}
 */
function toLocalDate(value) {
  if (!value) return null
  const d = new Date(value)
  return isNaN(d.getTime()) ? null : d
}

/**
 * 格式化日期时间为 YYYY-MM-DD HH:mm（本地时区）
 * @param {string|Date} value
 * @returns {string}
 */
export function formatDateTime(value) {
  const d = toLocalDate(value)
  if (!d) return '—'
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/**
 * 格式化日期（仅日期部分） YYYY-MM-DD（本地时区）
 * @param {string|Date} value
 * @returns {string}
 */
export function formatDate(value) {
  const d = toLocalDate(value)
  if (!d) return '—'
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/**
 * 格式化完整时间 YYYY-MM-DD HH:mm:ss（本地时区）
 * @param {string|Date} value
 * @returns {string}
 */
export function formatFullTime(value) {
  const d = toLocalDate(value)
  if (!d) return '—'
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

/**
 * 格式化相对时间（如"3分钟前"）
 * @param {string|Date} value
 * @returns {string}
 */
export function formatRelativeTime(value) {
  const d = toLocalDate(value)
  if (!d) return '—'
  const now = Date.now()
  const diff = now - d.getTime()
  const s = Math.floor(diff / 1000)
  if (s < 60) return '刚刚'
  const m = Math.floor(s / 60)
  if (m < 60) return `${m}分钟前`
  const h = Math.floor(m / 60)
  if (h < 24) return `${h}小时前`
  const D = Math.floor(h / 24)
  if (D < 30) return `${D}天前`
  return formatDateTime(value)
}
