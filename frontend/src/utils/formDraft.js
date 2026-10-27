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
 *     draft.begin()                          // 开始监听并自动保存
 *     open.value = true
 *   }
 *
 *   function submitForm() { ...成功后 draft.clear(); draft.pause() }
 *   function cancel() { draft.pause(); open.value = false }   // 取消不清草稿，下次打开还在
 */
const PREFIX = 'cat:form-draft:'

export function useFormDraft(key, form, options = {}) {
  const storageKey = PREFIX + key
  const delay = options.delay || 400
  let active = false
  let timer = null

  function begin() {
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
    if (!active) return
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      try {
        localStorage.setItem(storageKey, JSON.stringify(form.value))
      } catch (e) {
        // localStorage 满或被禁用时静默失败，不影响主流程
      }
    }, delay)
  }

  function restore() {
    try {
      const raw = localStorage.getItem(storageKey)
      if (!raw) return null
      const data = JSON.parse(raw)
      return data && typeof data === 'object' ? data : null
    } catch (e) {
      return null
    }
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

  // 关闭浏览器/刷新前提示，避免填了一半的内容直接丢掉
  if (typeof window !== 'undefined' && options.warnOnUnload !== false) {
    const handler = e => {
      if (!active || !has()) return
      e.preventDefault()
      e.returnValue = ''
      return ''
    }
    window.addEventListener('beforeunload', handler)
  }

  return { begin, pause, restore, clear, has }
}
