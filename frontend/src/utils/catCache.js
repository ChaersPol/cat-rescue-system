import { ref, computed } from 'vue'
import { listCatProfile } from '@/api/cat/profile'
import { listCatBase } from '@/api/cat/base'
import { listCatWard } from '@/api/cat/ward'
import { optionselectWardType } from '@/api/cat/wardtype'

/**
 * 主数据缓存：把「猫 id / 基地 id / 病房 id」这类外键翻译成业务名称，
 * 避免列表里只显示一个数字，用户看不懂。
 *
 * 用法：
 *   import { useCatCache } from "@/utils/catCache"
 *   const { catLabel } = useCatCache()
 *   <span>{{ catLabel(scope.row.catId) }}</span>
 *
 * ⚠️ 猫咪在所有列表里的展示写法固定为「名字（档案编号）」，例如「毛豆（CAT202610180024）」。
 * 只允许在 formatCatLabel 里定义，页面**不要自己拼字符串**，否则改格式要全站翻一遍。
 */

/**
 * 猫咪展示名：名字（档案编号）
 * 名字为空显示「未命名」，档案编号为空时只显示名字（不出现空括号）
 */
export function formatCatLabel(catName, archiveNo) {
  const name = String(catName === undefined || catName === null ? '' : catName).trim() || '未命名'
  const no = String(archiveNo === undefined || archiveNo === null ? '' : archiveNo).trim()
  return no ? `${name}（${no}）` : name
}

/**
 * 从页面自己已加载的下拉数据里解析猫咪展示名（避免再发一次请求）
 * @param {Array|Ref<Array>} options 形如 [{ catId, catName, archiveNo }]
 * @param {number|string} catId
 */
export function catLabelOf(options, catId) {
  if (catId === undefined || catId === null || catId === '') {
    return '-'
  }
  const list = Array.isArray(options) ? options : ((options && options.value) || [])
  const item = list.find(i => String(i.catId) === String(catId))
  return item ? formatCatLabel(item.catName, item.archiveNo) : '#' + catId
}

/**
 * 主数据缓存有效期（毫秒）
 *
 * ⚠️ 这个缓存是模块级单例，页面 keep-alive 后 setup 不会再跑第二遍。
 * 如果只认「loaded 过一次就永远用旧的」，就会出现这类问题：
 *   在「基地管理」新增了一个医疗基地 → 切到「站内诊断」点新增 → 下拉里没有新基地 → 强制刷新才出现。
 * 因此这里做三件事：
 *   1. 缓存带 TTL，过期自动重拉（兜底，任何路径都不会永久滞留旧数据）
 *   2. invalidateMasterData(kind) 广播，主数据增删改后立刻让全站缓存失效
 *   3. 页面打开新增/修改弹窗时显式 reload（见各页面 handleAdd/handleUpdate），确保下拉一定是最新的
 */
const DEFAULT_TTL = 60 * 1000

const MASTER_EVENT = 'cat:master-data-changed'

/** 主数据类型，用于广播失效范围 */
export const MASTER_KIND = {
  CAT: 'cat',
  BASE: 'base',
  WARD: 'ward',
  WARD_TYPE: 'wardType'
}

/**
 * 广播「某类主数据已变更」，让所有已挂载页面的缓存立刻失效并重新拉取。
 * 在基地、猫咪档案、病房、病房类型的增删改成功后调用。
 * @param {'cat'|'base'|'ward'|'wardType'|'all'} kind
 */
export function invalidateMasterData(kind = 'all') {
  if (typeof window === 'undefined') {
    return
  }
  window.dispatchEvent(new CustomEvent(MASTER_EVENT, { detail: { kind } }))
}

function subscribeMasterData(kinds, handler) {
  if (typeof window === 'undefined') {
    return () => {}
  }
  const listener = e => {
    const kind = e && e.detail ? e.detail.kind : 'all'
    if (kind === 'all' || kinds.indexOf(kind) >= 0) {
      handler()
    }
  }
  window.addEventListener(MASTER_EVENT, listener)
  return () => window.removeEventListener(MASTER_EVENT, listener)
}

/**
 * 订阅主数据变更（给自己维护下拉的组件用，例如 CatSelect）。
 * @param {'cat'|'base'|'ward'|'wardType'|Array} kind
 * @param {Function} handler
 * @returns {Function} 取消订阅
 */
export function onMasterDataChanged(kind, handler) {
  const kinds = Array.isArray(kind) ? kind : [kind]
  return subscribeMasterData(kinds, handler)
}

function createCache(loader, idKey, buildLabel, kinds, ttl = DEFAULT_TTL) {
  const map = ref({})
  let pending = null
  let loadedAt = 0

  function isStale() {
    return !loadedAt || Date.now() - loadedAt >= ttl
  }

  function load(force) {
    if (!force && !isStale()) {
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
        loadedAt = Date.now()
        return next
      })
      .catch(() => {
        // 失败不写 loadedAt，下次访问会重试，避免一次网络抖动把缓存锁死
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

  // 主数据变更后立刻重拉，已挂载的列表/下拉无需刷新页面就能看到新数据
  subscribeMasterData(kinds, () => {
    load(true)
  })

  return { map, load, label, invalidate: () => { loadedAt = 0 } }
}

const catCache = createCache(
  () => listCatProfile({ pageNum: 1, pageSize: 500 }),
  'catId',
  c => formatCatLabel(c.catName, c.archiveNo),
  [MASTER_KIND.CAT]
)

const baseCache = createCache(
  () => listCatBase({ pageNum: 1, pageSize: 500 }),
  'baseId',
  b => b.baseName || '#' + b.baseId,
  [MASTER_KIND.BASE]
)

const wardCache = createCache(
  () => listCatWard({ pageNum: 1, pageSize: 500 }),
  'wardId',
  w => w.wardName || '#' + w.wardId,
  [MASTER_KIND.WARD]
)

/** 猫咪：catLabel(catId) 得到「名字（档案编号）」 */
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

/**
 * 病房类型（字典，来自「基础信息 → 病房类型」）
 * 返回有序数组 + 按 id 取名称/取标签颜色，供病房列表与表单使用。
 * 增删改类型后调用 reloadWardTypeCache() 让其它页面拿到最新值。
 */
const wardTypeState = ref([])
let wardTypePending = null
let wardTypeLoadedAt = 0

function loadWardTypes(force) {
  if (!force && wardTypeLoadedAt && Date.now() - wardTypeLoadedAt < DEFAULT_TTL) {
    return Promise.resolve(wardTypeState.value)
  }
  if (wardTypePending) {
    return wardTypePending
  }
  wardTypePending = optionselectWardType()
    .then(res => {
      wardTypeState.value = (res && res.data) || []
      wardTypeLoadedAt = Date.now()
      return wardTypeState.value
    })
    .catch(() => wardTypeState.value)
    .finally(() => {
      wardTypePending = null
    })
  return wardTypePending
}

subscribeMasterData([MASTER_KIND.WARD_TYPE], () => {
  loadWardTypes(true)
})

export function useWardTypeCache() {
  loadWardTypes()
  const wardTypeMap = computed(() => {
    const map = {}
    wardTypeState.value.forEach(t => {
      map[t.typeId] = t
    })
    return map
  })
  /** 类型名称：优先按 id 查字典，查不到回落到病房表里的冗余名称 */
  function wardTypeLabel(typeId, fallbackName) {
    const item = wardTypeMap.value[typeId]
    if (item) {
      return item.typeName
    }
    return fallbackName || ''
  }
  /** 标签颜色（element-plus 的 tag type） */
  function wardTypeTag(typeId) {
    const item = wardTypeMap.value[typeId]
    return (item && item.tagType) || 'primary'
  }
  return {
    wardTypeList: wardTypeState,
    wardTypeMap,
    wardTypeLabel,
    wardTypeTag,
    reloadWardTypeCache: () => loadWardTypes(true)
  }
}
