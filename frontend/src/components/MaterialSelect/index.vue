<template>
   <el-select v-model="innerValue" filterable clearable :loading="loading" :placeholder="placeholder"
      :disabled="disabled" :size="size" style="width: 100%" @change="handleChange">
      <el-option v-for="item in options" :key="item.materialId" :label="buildLabel(item)"
         :value="item.materialId">
         <span class="material-name">{{ item.materialName }}</span>
         <span class="material-spec" v-if="item.spec">{{ item.spec }}</span>
         <span class="material-stock" :class="{ empty: isLow(item) }">
            {{ isLow(item) ? '库存不足' : '库存' }} {{ formatStock(item.stock) }}{{ item.unit || '' }}
         </span>
      </el-option>
      <template #empty>
         <div class="material-empty">暂无可选物资，先到「物资管理」建档</div>
      </template>
   </el-select>
</template>

<script setup name="MaterialSelect">
import { listCatMaterial } from "@/api/cat/material"

/**
 * 物资选择器（用药 / 疫苗 / 驱虫 台账挂物资用）
 *
 * 用法：<material-select v-model="form.materialId" @change="onMaterialChange" />
 * 选中后把整条物资抛出来，父表单用来带出单位、判断库存够不够（不足只红字提示，不阻断保存）。
 */
const props = defineProps({
   modelValue: {
      type: [Number, String],
      default: undefined
   },
   placeholder: {
      type: String,
      default: "选择关联物资（可不选）"
   },
   disabled: {
      type: Boolean,
      default: false
   },
   size: {
      type: String,
      default: ""
   }
})
const emit = defineEmits(["update:modelValue", "change"])

const options = ref([])
const loading = ref(false)
const loaded = ref(false)

const innerValue = computed({
   get: () => (props.modelValue === undefined || props.modelValue === null || props.modelValue === ''
      ? undefined : props.modelValue),
   set: (val) => {
      emit("update:modelValue", val === undefined ? null : val)
   }
})

function buildLabel(item) {
   return item.spec ? `${item.materialName}（${item.spec}）` : item.materialName
}

function formatStock(stock) {
   const num = Number(stock)
   if (!Number.isFinite(num)) return "0"
   return Number.isInteger(num) ? String(num) : num.toFixed(2)
}

/** 库存为 0 或负数：选项标红，选中后父组件也据此提示 */
function isLow(item) {
   const num = Number(item.stock)
   return !Number.isFinite(num) || num <= 0
}

function handleChange(val) {
   const hit = options.value.find(item => String(item.materialId) === String(val))
   emit("change", hit || null)
}

function loadOptions() {
   if (loading.value) {
      return
   }
   loading.value = true
   listCatMaterial({ pageNum: 1, pageSize: 500 })
      .then(response => {
         options.value = response.rows || []
         loaded.value = true
      })
      .finally(() => {
         loading.value = false
      })
}

// 编辑历史记录时，物资可能不在前 500 条里，补一次单条查询，避免下拉显示成 id
watch(
   () => props.modelValue,
   (val) => {
      if (!loaded.value || val === undefined || val === null || val === '' || val === 0) {
         return
      }
      const exists = options.value.some(item => String(item.materialId) === String(val))
      if (!exists) {
         loadOptions()
      }
   }
)

onMounted(loadOptions)
</script>

<style scoped>
.material-name {
   color: var(--el-text-color-primary);
}

.material-spec {
   color: var(--el-text-color-secondary);
   font-size: 12px;
   margin-left: 6px;
}

.material-stock {
   color: #909399;
   font-size: 12px;
   float: right;
}

.material-stock.empty {
   color: #f56c6c;
}

.material-empty {
   padding: 10px 0;
   text-align: center;
   color: #909399;
   font-size: 13px;
}
</style>
