import { watch } from 'vue'

/**
 * 表单草稿缓存（需求文档 13：表单编辑页支持返回上一步，未保存内容临时缓存，防止误关闭丢失）
 *
 * 用法：
 *   const draft = useFormDraft('cat-profile-form', form)
 *
 *   function handleAdd() {
 *     reset()
 *     const saved = draft.restore()          // 有草稿就恢复
 *     if (saved) { form.value = saved; proxy.$modal.msgSuccess('已恢复上次未保存的内容') }
 *     draft.begin()                          // 开始监听并自动保存（新增模式）
 *     open.value = true
 *   }
 *
 *   function handleUpdate(row) {
 *     reset()
 *     getXxx(id).then(res => { form.value = res.data; draft.begin('edit'); open.value = true })
 *   }
 *
 *   function submitForm() { ...成功后 draft.clear(); draft.pause() }
 *   function cancel() { draft.pause(); open.value = false }   // 取消不清草稿，下次打开还在
 *
 * ⚠️ 草稿只服务于「新增」流程
 * 历史 bug：编辑记录时也调用了 begin()，watch 会把整条记录写进 localStorage，
 * 于是「编辑某条 → 点新增」时 restore() 把那条记录又填回新增表单（串数据）。
 * 因此：编辑模式用 begin('edit')，只表示"暂停保存"，既不写草稿也不会被 restore 取到。
 */
const PREFIX = 'cat:form-draft:'
const MODE_CREATE = 'create'
const MODE_EDIT = 'edit'

export function useFormDraft(key, form, options = {}) {
  const storageKey = PREFIX + key
  const delay = options.delay || 400
  let active = false
  let mode = MODE_CREATE
  let timer = null

  /**
   * @param {'create'|'edit'} nextMode 只有 create 才会真正落盘草稿
   */
  function begin(nextMode = MODE_CREATE) {
    mode = nextMode
    active = true
  }

  function pause() {
    active = false
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  function save() {
    if (!active || mode !== MODE_CREATE) return
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      try {
        localStorage.setItem(storageKey, JSON.stringify({ __mode: MODE_CREATE, data: form.value }))
      } catch (e) {
        // localStorage 满或被禁用时静默失败，不影响主流程
      }
    }, delay)
  }

  /**
   * 只返回新增流程的草稿；以下情况一律丢弃并清掉：
   *  1) 旧版本没有 __mode 标记的（可能已被编辑数据污染）
   *  2) 带主键的（新增草稿不可能有 catId/diagnosisId 这类 id，有就说明是编辑记录留下的）
   */
  function restore() {
    try {
      const raw = localStorage.getItem(storageKey)
      if (!raw) return null
      const payload = JSON.parse(raw)
      if (!payload || payload.__mode !== MODE_CREATE) {
        localStorage.removeItem(storageKey)
        return null
      }
      const data = payload.data
      if (!data || typeof data !== 'object' || hasRecordId(data)) {
        localStorage.removeItem(storageKey)
        return null
      }
      return data
    } catch (e) {
      return null
    }
  }

  /**
   * 判断草稿里是否带着某条记录的主键（xxxId）
   * 走的是「新增」流程的表单不该有主键；有就说明这份草稿是编辑时串出来的
   */
  function hasRecordId(data) {
    return Object.keys(data).some(k => {
      if (!/Id$/.test(k)) return false
      const v = data[k]
      return v !== undefined && v !== null && v !== ''
    })
  }

  function clear() {
    try {
      localStorage.removeItem(storageKey)
    } catch (e) {
      // ignore
    }
  }

  function has() {
    try {
      return !!localStorage.getItem(storageKey)
    } catch (e) {
      return false
    }
  }

  watch(form, save, { deep: true })

  // 关闭浏览器/刷新前提示，避免填了一半的内容直接丢掉（编辑模式不提示）
  if (typeof window !== 'undefined' && options.warnOnUnload !== false) {
    const handler = e => {
      if (!active || mode !== MODE_CREATE || !has()) return
      e.preventDefault()
      e.returnValue = ''
      return ''
    }
    window.addEventListener('beforeunload', handler)
  }

  return { begin, pause, restore, clear, has }
}
