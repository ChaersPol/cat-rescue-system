<template>
  <el-select
    v-model="innerValue"
    filterable
    clearable
    :loading="loading"
    :placeholder="placeholder"
    :disabled="disabled"
    style="width: 100%"
    @visible-change="handleVisibleChange"
  >
    <el-option
      v-for="item in options"
      :key="item.catId"
      :label="buildLabel(item)"
      :value="item.catId"
    >
      <span class="cat-select-label">{{ item.archiveNo }}</span>
      <span class="cat-select-name">{{ item.catName }}</span>
      <span v-if="item.breed" class="cat-select-breed">{{ item.breed }}</span>
    </el-option>
    <template #empty>
      <div class="cat-select-empty">暂无可选猫咪</div>
    </template>
  </el-select>
</template>

<script setup name="CatSelect">
import { listCatProfile, getCatProfile } from "@/api/cat/profile"

/**
 * 猫咪选择器：用「档案编号 · 名字」展示，避免表单里只能看到一个数字 id。
 * 用法：<cat-select v-model="form.catId" />
 */
const props = defineProps({
  modelValue: {
    type: [Number, String],
    default: undefined
  },
  placeholder: {
    type: String,
    default: "请选择猫咪（可输入编号或名字搜索）"
  },
  disabled: {
    type: Boolean,
    default: false
  }
})
const emit = defineEmits(["update:modelValue"])

const options = ref([])
const loading = ref(false)
const loaded = ref(false)

const innerValue = computed({
  get: () => (props.modelValue === undefined || props.modelValue === null ? undefined : props.modelValue),
  set: (val) => emit("update:modelValue", val)
})

function buildLabel(item) {
  return `${item.archiveNo || ""} ${item.catName || ""}`.trim()
}

function loadOptions() {
  if (loading.value) {
    return
  }
  loading.value = true
  listCatProfile({ pageNum: 1, pageSize: 500 })
    .then(response => {
      options.value = response.rows || []
      loaded.value = true
      ensureSelectedPresent()
    })
    .finally(() => {
      loading.value = false
    })
}

// 编辑历史数据时，选中的猫可能不在默认列表里（已归档/已删除），单独补一条
function ensureSelectedPresent() {
  const id = innerValue.value
  if (id === undefined || id === null || id === "") {
    return
  }
  const exists = options.value.some(item => String(item.catId) === String(id))
  if (exists) {
    return
  }
  getCatProfile(id)
    .then(response => {
      if (response.data) {
        options.value = [response.data, ...options.value]
      }
    })
    .catch(() => {})
}

function handleVisibleChange(visible) {
  if (visible && !loaded.value) {
    loadOptions()
  }
}

watch(
  () => props.modelValue,
  () => {
    if (loaded.value) {
      ensureSelectedPresent()
    }
  }
)

onMounted(() => {
  loadOptions()
})
</script>

<style scoped>
.cat-select-label {
  color: #909399;
  font-size: 12px;
  margin-right: 8px;
}
.cat-select-name {
  color: #303133;
}
.cat-select-breed {
  color: #c0c4cc;
  font-size: 12px;
  float: right;
}
.cat-select-empty {
  padding: 10px 0;
  text-align: center;
  color: #909399;
  font-size: 13px;
}
</style>
