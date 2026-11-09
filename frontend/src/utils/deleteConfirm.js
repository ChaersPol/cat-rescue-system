/**
 * 统一删除确认（RuoYi 自带 MessageBox 样式，不另造弹窗）
 *
 * 规则见 docs/CONVENTIONS.md 第十四节：
 *   删除会留下脏引用的数据 → blocked=true，只告知「还有谁在用」，确定按钮置灰不可点
 *   没人引用的数据         → blocked=false，普通确认，确定可点
 *
 * 用法：
 *   deleteConfirm(proxy, {
 *     blocked: occupied.length > 0,
 *     title: '无法删除',
 *     message: {
 *       summary: '以下基地还有数据在使用，不能删除：',
 *       items: [{ name: '北苑基地', detail: '4 只猫咪、3 间病房' }],
 *       tail: '请先处理这些引用后再删除基地。'
 *     },
 *     onConfirm: () => delCatXxx(ids)
 *   }).then(() => { ... })   // onConfirm 成功后才会走到这里
 *
 * message 也支持直接传字符串（按换行拆成「主问句 + 说明」），老调用方无需改动。
 */

/** 明细行最多展示多少条，超出的折成「另有 N 项」 */
const MAX_LINES = 8

const ICON_ERROR = '<svg class="del-msg__icon" viewBox="0 0 16 16" width="15" height="15" aria-hidden="true">'
  + '<circle cx="8" cy="8" r="7.2" fill="currentColor"/>'
  + '<path d="M8 4.1v4.5" stroke="#fff" stroke-width="1.8" stroke-linecap="round"/>'
  + '<circle cx="8" cy="11.4" r="1" fill="#fff"/></svg>'

const ICON_WARN = '<svg class="del-msg__icon" viewBox="0 0 16 16" width="15" height="15" aria-hidden="true">'
  + '<path d="M7.13 2.3a1 1 0 0 1 1.74 0l5.5 10a1 1 0 0 1-.87 1.5H2.5a1 1 0 0 1-.87-1.5z" fill="currentColor"/>'
  + '<path d="M8 5.9v3.3" stroke="#fff" stroke-width="1.6" stroke-linecap="round"/>'
  + '<circle cx="8" cy="11.5" r=".9" fill="#fff"/></svg>'

/** 名称来自数据库（用户可填），拼进 HTML 前必须转义 */
const ESCAPE_MAP = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }
function esc(value) {
  return String(value === null || value === undefined ? '' : value).replace(/[&<>"']/g, c => ESCAPE_MAP[c])
}

/** 「4 只猫咪、3 间病房」→ 一个个小标签 */
function renderFacts(detail, tone) {
  const parts = String(detail || '')
    .split(/[、,，;；]/)
    .map(s => s.trim())
    .filter(Boolean)
  if (!parts.length) {
    return ''
  }
  return `<span class="del-msg__facts">${parts
    .map(p => `<span class="del-msg__fact del-msg__fact--${tone}">${esc(p)}</span>`)
    .join('')}</span>`
}

/** 结构化 message → HTML */
function renderStructured(message) {
  const tone = message.tone === 'warning' ? 'warning' : 'error'
  const blocks = []

  if (message.lead) {
    blocks.push(`<p class="del-msg__lead">${esc(message.lead)}</p>`)
  }

  if (message.summary) {
    blocks.push(
      `<div class="del-msg__banner del-msg__banner--${tone}">`
      + (tone === 'warning' ? ICON_WARN : ICON_ERROR)
      + `<span>${esc(message.summary)}</span></div>`
    )
  }

  const items = (message.items || []).filter(Boolean)
  if (items.length) {
    const rows = items.slice(0, MAX_LINES).map(item =>
      `<li class="del-msg__item"><span class="del-msg__name">${esc(item.name)}</span>`
      + `${renderFacts(item.detail, tone)}</li>`
    )
    if (items.length > MAX_LINES) {
      rows.push(`<li class="del-msg__item"><span class="del-msg__name">另有 ${items.length - MAX_LINES} 项未显示</span></li>`)
    }
    blocks.push(`<ul class="del-msg__list">${rows.join('')}</ul>`)
  }

  if (message.tail) {
    blocks.push(`<p class="del-msg__tail">${esc(message.tail)}</p>`)
  }

  return `<div class="del-msg del-msg--${tone}">${blocks.join('')}</div>`
}

/** 纯字符串 message → 首行当主问句，其余当说明 */
function renderPlain(message) {
  const lines = String(message || '').split('\n').map(s => s.trim()).filter(Boolean)
  const lead = lines.shift() || ''
  const desc = lines.join('\n')
  return '<div class="del-msg del-msg--plain">'
    + (lead ? `<p class="del-msg__lead">${esc(lead)}</p>` : '')
    + (desc ? `<p class="del-msg__desc">${esc(desc)}</p>` : '')
    + '</div>'
}

/**
 * 把后端返回的引用明细拼成结构化正文
 * @param {string} summary 顶部提示条（「以下基地还有数据在使用，不能删除：」）
 * @param {Array<{name:string, detail:string}>} items 明细
 * @param {string} [tail] 底部建议
 * @param {{tone?:'error'|'warning', lead?:string}} [extra] tone 决定配色，lead 是上方主问句
 */
export function buildBlockedMessage(summary, items, tail, extra = {}) {
  return {
    tone: extra.tone || 'error',
    lead: extra.lead || '',
    summary,
    items: (items || []).filter(Boolean),
    tail
  }
}

/**
 * 打开删除确认
 * @param {object} proxy 组件实例（用 proxy.$modal，拿不到时回退 ElMessageBox）
 * @param {object} opts
 * @param {boolean} opts.blocked 是否「有依赖、不能删」
 * @param {string} opts.title 标题
 * @param {string|object} opts.message 正文（字符串或结构化对象）
 * @param {Function} [opts.onConfirm] 可以直接删时执行，返回 Promise
 * @returns {Promise<void>} 点确定并删除成功后 resolve；取消/关闭时 resolve（不抛错，调用方无需 catch）
 */
export function deleteConfirm(proxy, { blocked = false, title, message, onConfirm } = {}) {
  const html = (message && typeof message === 'object')
    ? renderStructured(message)
    : renderPlain(message)

  const options = {
    // 不用 Element 自带的大图标：它会占一列导致正文错位，改为正文里的彩色提示条
    type: '',
    customClass: blocked ? 'msgbox-delete msgbox-delete--blocked' : 'msgbox-delete msgbox-delete--normal',
    dangerouslyUseHTMLString: true,
    confirmButtonText: '确 定',
    cancelButtonText: blocked ? '关 闭' : '取 消',
    confirmButtonClass: blocked ? 'btn-confirm-blocked' : '',
    closeOnClickModal: false,
    // 兜底：即使绕过置灰样式点到确定，也不关闭弹窗、更不执行删除
    beforeClose: (action, instance, done) => {
      if (blocked && action === 'confirm') {
        return
      }
      done()
    }
  }

  const open = (proxy && proxy.$modal && proxy.$modal.confirm)
    ? proxy.$modal.confirm(html, title, options)
    : import('element-plus').then(m => m.ElMessageBox.confirm(html, title, options))

  return Promise.resolve(open)
    .then(() => {
      // blocked 时正常走不到这里；防御性再挡一次
      if (blocked) {
        return undefined
      }
      return onConfirm ? onConfirm() : undefined
    })
    .catch(() => undefined)
}

/**
 * 「先查引用 → 再决定弹哪种」的完整删除流程，页面里直接用这个即可
 *
 * @param {object} proxy 组件实例
 * @param {object} opts
 * @param {Function} opts.fetchImpact 返回 Promise<{data:[{id,name,refCount,detail}]}>
 * @param {string} opts.entityLabel 对象名（基地 / 物资分类 …），用于默认文案
 * @param {Function} opts.deleteFn 真正执行删除，返回 Promise
 * @param {Function} [opts.normalTip] (names) => 可删除时的正文
 * @param {Function} [opts.afterDelete] 删除成功后的回调（刷新列表、提示成功）
 * @returns {Promise<void>}
 */
export function deleteWithImpact(proxy, { fetchImpact, entityLabel = '数据', deleteFn, normalTip, cascade = false, afterDelete } = {}) {
  return Promise.resolve()
    .then(() => fetchImpact())
    .then(response => {
      const rows = (response && response.data) || []
      const hit = rows.filter(r => Number(r.refCount) > 0)
      const names = rows.map(r => r.name).filter(Boolean).join('、') || `所选${entityLabel}`

      // cascade：子记录属于它自己，删除时后端会一并清理，所以不拦，只把「会连带删掉什么」讲清楚
      const blocked = cascade ? false : hit.length > 0
      let message
      if (cascade) {
        message = hit.length
          ? buildBlockedMessage(
              '以下关联记录会一并清除，删除后不可恢复：',
              hit.map(r => ({ name: r.name, detail: r.detail })),
              null,
              { tone: 'warning', lead: `确认删除 ${names} ？` })
          : (normalTip ? normalTip(names) : `确认删除 ${names} ？此操作不可撤销。`)
      } else if (blocked) {
        message = buildBlockedMessage(
          `以下${entityLabel}还有数据在使用，不能删除：`,
          hit.map(r => ({ name: r.name, detail: r.detail })),
          `请先处理这些引用后再删除${entityLabel}。`)
      } else {
        message = normalTip ? normalTip(names) : `确认删除 ${names} ？此操作不可撤销。`
      }

      return deleteConfirm(proxy, {
        blocked,
        title: blocked ? '无法删除' : '删除确认',
        message,
        onConfirm: () => Promise.resolve(deleteFn()).then(() => {
          if (afterDelete) {
            afterDelete()
          }
        })
      })
    })
    .catch(() => undefined)
}
