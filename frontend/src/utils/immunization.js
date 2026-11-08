/**
 * 免疫 / 驱虫周期规则（前端唯一来源）
 *
 * 与后端 CatImmunizationRule 保持同一套数值，改规则时两边一起改：
 * - 基础免疫（第一针 / 第二针 / 第三针）：针次间隔 21 天
 * - 狂犬、加强针：11 个月一个周期，到期进年度提醒
 * - 驱虫：体内、体外分开算，周期 30 天或 60 天，录入时选
 */

export const BASE_DOSE_INTERVAL_DAYS = 21
export const ANNUAL_INTERVAL_MONTHS = 11
export const BASE_DOSE_TOTAL = 3
export const DEWORM_CYCLE_OPTIONS = [30, 60]
export const DEWORM_DEFAULT_CYCLE = 30

/** 狂犬 / 加强针这类年度疫苗 */
export function isAnnual(type) {
  return !!type && (type.includes('狂犬') || type.includes('加强'))
}

/** 基础免疫针次（第一针 / 第二针 / 第三针） */
export function isBaseDose(type) {
  return !!type && !isAnnual(type) && type.includes('针')
}

/** 'YYYY-MM-DD' → Date（本地时区，不带时分秒，避免跨天算错） */
export function parseDate(value) {
  if (!value) return null
  const parts = String(value).slice(0, 10).split('-').map(Number)
  if (parts.length !== 3 || parts.some(n => isNaN(n))) return null
  return new Date(parts[0], parts[1] - 1, parts[2])
}

/** Date → 'YYYY-MM-DD' */
export function formatDate(date) {
  if (!(date instanceof Date) || isNaN(date.getTime())) return ''
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export function addDays(value, days) {
  const date = parseDate(value)
  if (!date) return ''
  date.setDate(date.getDate() + days)
  return formatDate(date)
}

export function addMonths(value, months) {
  const date = parseDate(value)
  if (!date) return ''
  date.setMonth(date.getMonth() + months)
  return formatDate(date)
}

/**
 * 下次接种日期：基础免疫按 21 天推下一针，狂犬 / 加强针按 11 个月推年度
 * @returns {string} 'YYYY-MM-DD'，无法推算时为空字符串
 */
export function calcVaccineNextDate(type, date) {
  if (!type || !date) return ''
  if (isAnnual(type)) return addMonths(date, ANNUAL_INTERVAL_MONTHS)
  if (isBaseDose(type)) return addDays(date, BASE_DOSE_INTERVAL_DAYS)
  return ''
}

/** 下次驱虫日期 = 基准日期 + 周期天数 */
export function calcDewormNextDate(date, cycleDays) {
  const cycle = Number(cycleDays) > 0 ? Number(cycleDays) : DEWORM_DEFAULT_CYCLE
  if (!date) return ''
  return addDays(date, cycle)
}

/** 距今天还有几天，负数表示已过期 */
export function daysUntil(value) {
  const target = parseDate(value)
  if (!target) return null
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  return Math.round((target - today) / 86400000)
}

/**
 * 到期文案与颜色：已过期红、今天/3 天内橙、其余灰
 * 3 天对应系统配置里「提前提醒天数」
 */
export function dueInfo(value) {
  const d = daysUntil(value)
  if (d === null) return { text: '', cls: '' }
  if (d < 0) return { text: `已过期 ${-d} 天`, cls: 'is-danger' }
  if (d === 0) return { text: '今天到期', cls: 'is-warning' }
  if (d <= 3) return { text: `${d} 天后`, cls: 'is-warning' }
  return { text: `${d} 天后`, cls: 'is-muted' }
}

/** 是否「该处理了」：已过期或 3 天内到期 */
export function isDueSoon(value) {
  const d = daysUntil(value)
  return d !== null && d <= 3
}

/** 从驱虫记录反推间隔天数（用于修改时回显周期选择） */
export function dewormCycleOf(dewormDate, nextDate) {
  const start = parseDate(dewormDate)
  const end = parseDate(nextDate)
  if (!start || !end) return DEWORM_DEFAULT_CYCLE
  const days = Math.round((end - start) / 86400000)
  return DEWORM_CYCLE_OPTIONS.includes(days) ? days : DEWORM_DEFAULT_CYCLE
}
