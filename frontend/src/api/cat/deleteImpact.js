import request from '@/utils/request'

/**
 * 删除前的引用检查（通用，见 docs/CONVENTIONS.md 第十四节）
 * 返回 [{ id, name, refCount, detail }]，没有引用的对象 refCount = 0
 *
 * @param {string} module base / wardtype / tag / feecategory / materialcategory / material / checkitem
 * @param {Array|string} ids
 */
export function getDeleteImpact(module, ids) {
  return request({
    url: '/cat/deleteImpact/' + module,
    method: 'get',
    params: { ids: Array.isArray(ids) ? ids.join(',') : ids }
  })
}
