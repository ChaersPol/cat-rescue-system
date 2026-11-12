// 体重只有 kg 一个单位：输入框右侧固定显示「kg」，用户只填数字，库里统一存「3.5kg」。
//
// 为什么不让人自己写单位：同一个数写成「3.5」「3.5kg」「3.5 公斤」就是三套账，
// 卡片、列表、导出各显示各的，最后谁也不知道哪个是对的。
// 显示用 weightToInput（剥掉单位喂给输入框），保存用 weightToStore（补回单位），
// 展示用 weightText（历史裸数字也补上单位）。

// 只留数字和一个小数点：粘贴「3.5kg」「3.5 公斤」也能自动清干净
export function onlyDecimal(v) {
  const cleaned = String(v ?? '').replace(/[^\d.]/g, '')
  const i = cleaned.indexOf('.')
  return i < 0 ? cleaned : cleaned.slice(0, i + 1) + cleaned.slice(i + 1).replace(/\./g, '')
}

// 回填输入框：库里存的是「3.5kg」，输入框右侧已经有 kg，只给数字
export function weightToInput(v) {
  return onlyDecimal(v)
}

// 提交前：数字补上 kg。空值保持空（不能变成「kg」）
export function weightToStore(v) {
  const s = onlyDecimal(v)
  if (!s || s === '.') return ''
  // 「.5」补成「0.5kg」，不然导出/排序看起来像漏了一位
  return (s.startsWith('.') ? `0${s}` : s) + 'kg'
}

// 页面展示：已经是「3.5kg」的原样返回，裸数字补一个 kg
export function weightText(v) {
  const s = String(v ?? '').trim()
  if (!s) return ''
  return /kg$/i.test(s) ? s : `${s}kg`
}
