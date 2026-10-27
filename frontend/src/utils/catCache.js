import { ref } from 'vue'
import { listCatProfile } from '@/api/cat/profile'
import { listCatBase } from '@/api/cat/base'
import { listCatWard } from '@/api/cat/ward'

/**
 * 主数据缓存：把「猫 id / 基地 id / 病房 id」这类外键翻译成业务名称，
 * 避免列表里只显示一个数字，用户看不懂。
 *
 * 用法：
 *   import { useCatCache } from "@/utils/catCache"
 *   const { catLabel } = useCatCache()
 *   <span>{{ catLabel(scope.row.catId) }}</span>
 */

function createCache(loader, idKey, buildLabel) {
  const map = ref({})
  let pending = null
  let loaded = false

  function load(force) {
    if (loaded && !force) {
      return Promise.resolve(map.value)
    }
    if (pending) {
      return pending
    }
    pending = loader()
      .then(res => {
        const next = {}
        ;(res.rows || res.data || []).forEach(item => {
          next[item[idKey]] = item
        })
        map.value = next
        loaded = true
        return next
      })
      .catch(() => {
        pending = null
        return map.value
      })
      .finally(() => {
        pending = null
      })
    return pending
  }

  function label(id) {
    if (id === undefined || id === null || id === '') {
      return ''
    }
    const item = map.value[id]
    return item ? buildLabel(item) : '#' + id
  }

  return { map, load, label }
}

const catCache = createCache(
  () => listCatProfile({ pageNum: 1, pageSize: 500 }),
  'catId',
  c => `${c.archiveNo || ''} ${c.catName || ''}`.trim()
)

const baseCache = createCache(
  () => listCatBase({ pageNum: 1, pageSize: 500 }),
  'baseId',
  b => b.baseName || '#' + b.baseId
)

const wardCache = createCache(
  () => listCatWard({ pageNum: 1, pageSize: 500 }),
  'wardId',
  w => w.wardName || '#' + w.wardId
)

/** 猫咪：catLabel(catId) 得到「档案编号 名字」 */
export function useCatCache() {
  catCache.load()
  return { catMap: catCache.map, catLabel: catCache.label, reloadCatCache: () => catCache.load(true) }
}

/** 基地：baseLabel(baseId) 得到基地名称 */
export function useBaseCache() {
  baseCache.load()
  return { baseMap: baseCache.map, baseLabel: baseCache.label, reloadBaseCache: () => baseCache.load(true) }
}

/** 病房：wardLabel(wardId) 得到病房名称 */
export function useWardCache() {
  wardCache.load()
  return { wardMap: wardCache.map, wardLabel: wardCache.label, reloadWardCache: () => wardCache.load(true) }
}
