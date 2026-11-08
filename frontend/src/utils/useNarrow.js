import { computed, ref } from 'vue'

// 全站共用：<=768px 视为窄屏（手机）。
// 页面用它收起次要表格列、切换弹窗布局，保证手机上不用左右滑也能操作。
const viewportWidth = ref(typeof window === 'undefined' ? 1200 : window.innerWidth)

if (typeof window !== 'undefined') {
  window.addEventListener('resize', () => {
    viewportWidth.value = window.innerWidth
  }, { passive: true })
}

const isNarrow = computed(() => viewportWidth.value <= 768)

export function useNarrow() {
  return { isNarrow }
}
