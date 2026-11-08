<template>
  <el-form-item label="所属基地" prop="catBaseId">
    <el-select
      v-model="baseValue"
      placeholder="全部基地"
      clearable
      filterable
      style="width: 170px"
      @change="handleBaseChange"
    >
      <el-option v-for="b in baseOptions" :key="b.baseId" :label="b.baseName" :value="b.baseId" />
    </el-select>
  </el-form-item>
  <el-form-item label="所属房间" prop="catWardId">
    <el-select
      v-model="wardValue"
      placeholder="全部房间"
      clearable
      filterable
      style="width: 170px"
      @change="handleWardChange"
    >
      <el-option-group v-for="g in wardGroups" :key="g.baseId" :label="g.baseName">
        <el-option v-for="w in g.wards" :key="w.wardId" :label="w.wardName" :value="w.wardId" />
      </el-option-group>
    </el-select>
  </el-form-item>
</template>

<script setup name="CatLocationFilter">
import { listCatBase } from "@/api/cat/base"
import { listCatWard } from "@/api/cat/ward"
import { onMasterDataChanged, MASTER_KIND } from "@/utils/catCache"

/**
 * 「猫现在在哪个基地 / 哪个房间」的通用筛选组件。
 *
 * 业务表（用药、疫苗、流转、出入库…）只存 cat_id，猫的位置随时会变，
 * 所以筛选交给后端用 cat_profile 的子查询做，这里只负责给两个下拉。
 *
 * 用法（放在 el-form 里，两个字段进 queryParams）：
 *   <cat-location v-model:base-id="queryParams.catBaseId"
 *                 v-model:ward-id="queryParams.catWardId"
 *                 @change="handleQuery" />
 */
const props = defineProps({
  baseId: {
    type: [Number, String],
    default: undefined
  },
  wardId: {
    type: [Number, String],
    default: undefined
  }
})
const emit = defineEmits(["update:baseId", "update:wardId", "change"])

const baseValue = computed({
  get: () => props.baseId,
  set: val => emit("update:baseId", val)
})
const wardValue = computed({
  get: () => props.wardId,
  set: val => emit("update:wardId", val)
})

const baseOptions = ref([])
const wardOptions = ref([])

/** 选了基地就只列该基地的病房，没选就按基地分组全列 */
const wardGroups = computed(() => {
  const groups = []
  const byBase = new Map()
  wardOptions.value.forEach(w => {
    if (!byBase.has(w.baseId)) {
      const base = baseOptions.value.find(b => b.baseId === w.baseId)
      const group = { baseId: w.baseId, baseName: base ? base.baseName : "未关联基地", wards: [] }
      byBase.set(w.baseId, group)
      groups.push(group)
    }
    byBase.get(w.baseId).wards.push(w)
  })
  if (props.baseId !== undefined && props.baseId !== null && props.baseId !== "") {
    return groups.filter(g => String(g.baseId) === String(props.baseId))
  }
  return groups
})

function loadBases() {
  listCatBase({ pageNum: 1, pageSize: 500, status: "0" }).then(res => {
    baseOptions.value = res.rows || res.data || []
  }).catch(() => {})
}

function loadWards() {
  listCatWard({ pageNum: 1, pageSize: 500, status: "0" }).then(res => {
    wardOptions.value = res.rows || res.data || []
  }).catch(() => {})
}

function handleBaseChange(val) {
  // 换基地时，原来选的房间不属于新基地就清掉，避免出现「基地A + 基地B的房间」这种空结果
  if (wardValue.value !== undefined && wardValue.value !== null && wardValue.value !== "") {
    const ward = wardOptions.value.find(w => String(w.wardId) === String(wardValue.value))
    if (val !== undefined && val !== null && val !== "" && (!ward || String(ward.baseId) !== String(val))) {
      wardValue.value = undefined
    }
  }
  emit("change")
}

function handleWardChange() {
  emit("change")
}

loadBases()
loadWards()
onMasterDataChanged([MASTER_KIND.BASE, MASTER_KIND.WARD], () => {
  loadBases()
  loadWards()
})
</script>
