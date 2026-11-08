/**
 * 病房类型标签颜色（cat_ward_type.tag_type）
 *
 * ⚠ 背景：Element Plus 的 el-tag 只认 primary / success / warning / danger / info
 *    这 5 种语义色，多传别的值不会报错，但会静默显示成蓝色（primary 默认样式）。
 *    所以紫色 / 粉色 / 黄色 / 黑色这几种「扩展色」走内联 CSS 变量覆盖，
 *    不写全局 class —— 内联变量优先级最高，不受样式文件加载顺序影响。
 *
 * 用法（三处固定写法）：
 *   <el-tag :type="tagTypeOf(row.tagType)" :style="tagStyleOf(row.tagType)" effect="light">
 */

/** 下拉可选颜色，顺序即下拉顺序 */
export const CAT_TAG_COLORS = [
  { value: 'primary', label: '蓝色（默认）' },
  { value: 'success', label: '绿色' },
  { value: 'warning', label: '橙色' },
  { value: 'danger', label: '红色（隔离/危险）' },
  { value: 'info', label: '灰色' },
  { value: 'purple', label: '紫色' },
  { value: 'pink', label: '粉色' },
  { value: 'yellow', label: '黄色' },
  { value: 'black', label: '黑色' },
]

/** 内置语义色（交给 el-tag 自己的 type 处理） */
const BUILTIN = ['primary', 'success', 'warning', 'danger', 'info']

/** 扩展色：浅底深字，和 element 的 light 效果观感一致；黑色反过来用深底白字 */
const EXT_COLORS = {
  purple: { bg: '#f4f0ff', border: '#e0d5ff', text: '#7c4dff' },
  pink: { bg: '#fff0f6', border: '#ffd6e7', text: '#eb2f96' },
  yellow: { bg: '#fffbe6', border: '#ffe58f', text: '#ad6800' },
  black: { bg: '#2f2f33', border: '#2f2f33', text: '#ffffff' },
}

/** 传给 el-tag 的 type；扩展色一律回落到 primary（真正的颜色由 tagStyleOf 覆盖） */
export function tagTypeOf(value) {
  if (!value) return 'primary'
  return BUILTIN.includes(value) ? value : 'primary'
}

/** 扩展色的内联样式；内置色返回 undefined，交给 element 主题 */
export function tagStyleOf(value) {
  const c = EXT_COLORS[value]
  if (!c) return undefined
  return {
    '--el-tag-bg-color': c.bg,
    '--el-tag-border-color': c.border,
    '--el-tag-text-color': c.text,
  }
}
