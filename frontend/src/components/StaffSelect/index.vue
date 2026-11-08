<template>
  <!-- 本人录入：只读显示当前登录账号，防止冒填别人的名字 -->
  <el-input v-if="locked" :model-value="lockedDisplay" disabled :placeholder="placeholder" />
  <!-- 管理员/超管代录：只能从系统账号里选 -->
  <el-select
    v-else
    v-model="innerValue"
    filterable
    clearable
    :loading="loading"
    :placeholder="placeholder"
    style="width: 100%"
    @visible-change="handleVisibleChange"
  >
    <el-option v-for="s in options" :key="s.userId" :label="optionLabel(s)" :value="s.nickName" />
    <template #empty>
      <div class="staff-select-empty">暂无可选人员</div>
    </template>
  </el-select>
  <div class="form-tip" v-if="showLegacyTip">原记录为「{{ modelValue }}」（历史手填，未关联账号），重新选择后会被覆盖</div>
</template>

<script setup name="StaffSelect">
import { listCatStaffOptions } from '@/api/cat/staff'
import useUserStore from '@/store/modules/user'

/**
 * 人员选择器：用药执行人、疫苗负责人、回访人、医生、登记人这类「谁做的」字段统一用它。
 *
 * 规则与站内诊断的「接诊医生」一致：
 * 1. 登录人本身属于该角色范围（医生 / 助理）→ 锁定本人，只读；
 * 2. 超管 / 管理员 → 下拉选择系统账号，不再允许手打名字；
 * 3. scope=all 的通用字段（操作人、登记人）→ 普通员工锁本人，管理员可代选。
 *
 * 用法：<staff-select v-model="form.executor" scope="assistant" placeholder="请选择用药执行人" />
 */
const props = defineProps({
  modelValue: {
    type: String,
    default: undefined
  },
  // doctor：医生角色；assistant：助理角色；all：全部在职账号
  scope: {
    type: String,
    default: 'all'
  },
  placeholder: {
    type: String,
    default: '请选择人员'
  },
  // 是否提示「历史手填、重新选择后覆盖」
  legacyTip: {
    type: Boolean,
    default: true
  },
  // 是否允许锁本人。留痕类字段（执行人、医生）必须锁本人；
  // 主数据里的固定属性（如基地负责人）不是"谁操作的"，传 false 关掉锁人。
  lockSelf: {
    type: Boolean,
    default: true
  }
})
const emit = defineEmits(['update:modelValue', 'change'])

const userStore = useUserStore()
const options = ref([])
const loading = ref(false)
const loaded = ref(false)

const roles = computed(() => userStore.roles || [])

/** 是否只能记本人 */
const locked = computed(() => {
  if (!props.lockSelf) return false
  if (props.scope === 'doctor') return roles.value.includes('doctor')
  if (props.scope === 'assistant') return roles.value.includes('assistant')
  return !roles.value.includes('admin') && !roles.value.includes('manager')
})

const selfName = computed(() => userStore.nickName || userStore.name || '')

// 新增时表单为空 → 显示本人；编辑历史记录时保留原值（不把别人的记录改成自己）
const lockedDisplay = computed(() => props.modelValue || selfName.value)

const innerValue = computed({
  get: () => props.modelValue,
  set: (val) => {
    emit('update:modelValue', val)
    emit('change', val)
  }
})

function optionLabel(s) {
  return s.nickName && s.userName && s.nickName !== s.userName ? `${s.nickName}（${s.userName}）` : s.nickName || s.userName
}

/** 老记录里的名字不在账号列表中（如「助理小李」），提示管理员重选 */
const showLegacyTip = computed(() => {
  if (locked.value || !props.legacyTip) return false
  if (!props.modelValue || !loaded.value) return false
  return !options.value.some((s) => s.nickName === props.modelValue)
})

function loadOptions() {
  if (locked.value || loading.value) return
  loading.value = true
  listCatStaffOptions(props.scope)
    .then((res) => {
      options.value = res.data || []
      loaded.value = true
    })
    .finally(() => {
      loading.value = false
    })
}

function handleVisibleChange(visible) {
  if (visible) loadOptions()
}

// 锁本人时：表单为空就把值写成当前登录人，保证提交的名字与账号一致。
// 同时监听 modelValue：弹窗不销毁时新增会把它重置为空，这时要重新填回本人。
watch(
  [locked, () => props.modelValue],
  ([isLocked, val]) => {
    if (isLocked && selfName.value && !val) {
      emit('update:modelValue', selfName.value)
    }
  },
  { immediate: true }
)

onMounted(() => {
  if (!locked.value) loadOptions()
})
</script>

<style scoped>
.staff-select-empty {
  padding: 10px 0;
  text-align: center;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.form-tip {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--el-color-warning);
}
</style>
