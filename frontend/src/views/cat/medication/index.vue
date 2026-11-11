<template>
   <div class="app-container medication-page">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
         <el-form-item label="猫咪" prop="catId">
            <cat-select v-model="queryParams.catId" placeholder="全部猫咪" style="width: 190px" @change="handleQuery" />
         </el-form-item>
         <!-- 助理负责人：按病房授权反查猫，跟「执行人」不是一回事（执行人不一定是负责人） -->
         <el-form-item label="助理负责人" prop="assistantUserId" label-width="96px">
            <el-select v-model="queryParams.assistantUserId" placeholder="全部助理" clearable filterable
               style="width: 160px" @change="handleQuery">
               <el-option v-for="s in assistantOptions" :key="s.userId" :label="staffLabel(s)" :value="s.userId" />
            </el-select>
         </el-form-item>
         <cat-location-filter
           v-model:base-id="queryParams.catBaseId"
           v-model:ward-id="queryParams.catWardId"
           @change="handleQuery" />
         <el-form-item label="时间段" prop="timePeriod">
            <el-select v-model="queryParams.timePeriod" placeholder="全部时段" clearable style="width: 110px" @change="handleQuery">
               <el-option v-for="d in cat_time_period" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
         </el-form-item>
         <el-form-item label="用药日期">
            <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange" range-separator="-"
               start-placeholder="开始日期" end-placeholder="结束日期" style="width: 250px" @change="handleQuery" />
         </el-form-item>
         <el-form-item label="完成状态" prop="execStatus">
            <el-select v-model="queryParams.execStatus" placeholder="全部" clearable style="width: 110px" @change="handleQuery">
               <el-option label="未完成" value="0" />
               <el-option label="已完成" value="1" />
            </el-select>
         </el-form-item>
         <el-form-item label="来源" prop="sourceType">
            <el-select v-model="queryParams.sourceType" placeholder="全部来源" clearable style="width: 130px" @change="handleQuery">
               <el-option label="手动录入" value="1" />
               <el-option label="站内诊断/处方" value="2" />
            </el-select>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <div class="table-toolbar">
         <div class="toolbar-left">
            <el-button v-if="canEditPlan" type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:medication:add']">新增</el-button>
            <el-button icon="Download" plain @click="handleExport" v-hasPermi="['cat:medication:export']">导出明细</el-button>
            <span class="toolbar-tip">导出按<b>每条药</b>算，用于统计药品消耗</span>
         </div>
         <div class="toolbar-right">
            <el-tooltip placement="top" content="合并：一只猫 + 一天 + 一个时间段一行，点开看每味药；明细：一个药一行，统计药品消耗按条算">
               <el-icon class="hint-icon"><QuestionFilled /></el-icon>
            </el-tooltip>
            <el-radio-group v-model="viewMode" size="small" @change="onViewChange">
               <el-radio-button value="group">按次合并</el-radio-button>
               <el-radio-button value="detail">药品明细</el-radio-button>
            </el-radio-group>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
         </div>
      </div>

      <!-- ==================== 手机端：卡片（一屏看全，不用左右滑） ==================== -->
      <div v-if="isNarrow && viewMode === 'group'" class="card-list" v-loading="loading">
         <el-empty v-if="!list.length" description="没有符合条件的用药记录" :image-size="70" />
         <div v-for="row in list" :key="groupKey(row)" class="med-card" :class="{ done: isDone(row) }">
            <div class="card-head" @click="toggleExpand(row)">
               <div class="card-cat">
                  <div class="card-name">{{ catLabel(row.catId) }}</div>
                  <div class="card-sub">{{ row.useDate }} · {{ periodLabel(row.timePeriod) }}</div>
               </div>
               <el-tag :type="isDone(row) ? 'success' : 'warning'" effect="dark" size="small">
                  {{ isDone(row) ? '已完成' : '未完成' }}
               </el-tag>
            </div>
            <div class="card-meta">
               <span>{{ row.drugCount }} 种药</span>
               <span v-if="row.executor">执行人 {{ row.executor }}</span>
               <span v-if="isDone(row) && row.execTime">{{ timeOnly(row.execTime) }} 完成</span>
            </div>
            <div class="card-drugs">{{ row.drugNames }}</div>
            <div class="card-actions">
               <el-button v-if="isDone(row)" size="small" @click="openExec(row, 'photos')">补照片</el-button>
               <el-button v-if="isDone(row)" size="small" type="warning" plain @click="revoke(row)">撤回</el-button>
               <el-button v-else size="small" type="primary" @click="openExec(row, 'complete')">完成</el-button>
               <el-button size="small" link @click="toggleExpand(row)">{{ isExpanded(row) ? '收起' : '看明细' }}</el-button>
            </div>
            <div v-if="isExpanded(row)" class="card-detail">
               <div v-if="detailLoading[groupKey(row)]" class="muted">加载中…</div>
               <template v-else>
                  <div v-for="d in detailOf(row)" :key="d.medicationId" class="detail-line">
                     <span class="detail-drug">{{ d.drugName }}</span>
                     <span class="detail-dosage">{{ d.dosage || '—' }}</span>
                     <span class="detail-way">{{ adminLabel(d.administration) }}</span>
                  </div>
                  <div class="card-photos" v-if="row.photos">
                     <div v-for="(p, i) in photoList(row.photos)" :key="i" class="photo-cell">
                        <el-image :src="p" :preview-src-list="photoList(row.photos)" :initial-index="i"
                           fit="cover" class="card-photo" preview-teleported />
                        <span class="shot-time">{{ shotTime(p, row.photoTime) }}</span>
                     </div>
                  </div>
                  <div class="card-note" v-if="row.adverseReaction"><b>不良反应：</b>{{ row.adverseReaction }}</div>
               </template>
            </div>
         </div>
         <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>

      <!-- ==================== 电脑端：合并视图 ==================== -->
      <el-table v-else-if="viewMode === 'group'" v-loading="loading" :data="list" :row-key="groupKey" :expand-row-keys="expandedKeys" @expand-change="onRowExpand">
         <el-table-column type="expand">
            <template #default="scope">
               <div class="group-detail" v-loading="detailLoading[groupKey(scope.row)]">
                  <el-table :data="detailOf(scope.row)" size="small" border class="detail-table">
                     <el-table-column label="药品名称" prop="drugName" min-width="160" :show-overflow-tooltip="true" />
                     <el-table-column label="剂量" prop="dosage" width="120" :show-overflow-tooltip="true" />
                     <el-table-column label="给药方式" width="100" align="center">
                        <template #default="d">{{ adminLabel(d.row.administration) }}</template>
                     </el-table-column>
                     <el-table-column label="关联物资" min-width="160">
                        <template #default="d">
                           <template v-if="d.row.materialName">
                              {{ d.row.materialName }}
                              <span class="muted" v-if="d.row.stockQty">× {{ d.row.stockQty }}{{ materialUnits[d.row.materialId] || '' }}</span>
                           </template>
                           <span class="muted" v-else>未关联（不扣库存）</span>
                        </template>
                     </el-table-column>
                     <el-table-column label="备注" prop="remark" min-width="180" :show-overflow-tooltip="true" />
                  </el-table>
                  <div class="exec-block">
                     <div class="exec-line" v-if="scope.row.adverseReaction"><b>不良反应：</b>{{ scope.row.adverseReaction }}</div>
                     <div class="exec-line" v-if="scope.row.photos">
                        <b>用药照片：</b>
                        <div class="photo-wrap">
                           <div v-for="(p, i) in photoList(scope.row.photos)" :key="i" class="photo-cell">
                              <el-image :src="p" :preview-src-list="photoList(scope.row.photos)" :initial-index="i"
                                 fit="cover" class="group-photo" preview-teleported />
                              <span class="shot-time">{{ shotTime(p, scope.row.photoTime) }}</span>
                           </div>
                        </div>
                     </div>
                     <div class="exec-line muted" v-if="isDone(scope.row)">
                        由 {{ scope.row.executor || '—' }} 于 {{ scope.row.execTime || '—' }} 标记完成
                     </div>
                     <div class="exec-line muted" v-if="!isDone(scope.row) && scope.row.execUpdateTime">
                        最近修改：{{ scope.row.execUpdateTime }}
                     </div>
                  </div>
               </div>
            </template>
         </el-table-column>
         <el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="56" align="center" />
         <el-table-column label="猫咪" prop="catId" min-width="190" :show-overflow-tooltip="true">
            <template #default="scope">{{ catLabel(scope.row.catId) }}</template>
         </el-table-column>
         <el-table-column label="所属基地" width="140" :show-overflow-tooltip="true">
            <template #default="scope">{{ catBaseName(scope.row.catId) }}</template>
         </el-table-column>
         <el-table-column label="所属房间" width="110" :show-overflow-tooltip="true">
            <template #default="scope">{{ catWardName(scope.row.catId) }}</template>
         </el-table-column>
         <el-table-column label="用药日期" prop="useDate" width="110" align="center" />
         <el-table-column label="时间段" width="80" align="center">
            <template #default="scope">{{ periodLabel(scope.row.timePeriod) }}</template>
         </el-table-column>
         <el-table-column label="药品" min-width="220" :show-overflow-tooltip="true">
            <template #default="scope">
               <el-tag size="small" type="primary" effect="plain">{{ scope.row.drugCount }} 种</el-tag>
               <span class="drug-names">{{ scope.row.drugNames }}</span>
            </template>
         </el-table-column>
         <el-table-column label="执行人" prop="executor" width="100" align="center">
            <template #default="scope">
               <span v-if="scope.row.executor">{{ scope.row.executor }}</span>
               <span v-else class="muted">—</span>
            </template>
         </el-table-column>
         <el-table-column label="完成状态" width="110" align="center">
            <template #default="scope">
               <el-tooltip v-if="isDone(scope.row)" :content="`${scope.row.executor || '—'} 于 ${scope.row.execTime || '—'} 完成`" placement="top">
                  <el-tag size="small" type="success" effect="dark">已完成</el-tag>
               </el-tooltip>
               <el-tag v-else size="small" type="warning" effect="plain">未完成</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="操作" width="268" fixed="right" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
               <!-- 助理：完成 / 补照片 / 撤回 -->
               <template v-if="isAssistant && !isManager">
                  <el-button v-if="!isDone(scope.row)" link type="primary" icon="Check" @click="openExec(scope.row, 'complete')">完成</el-button>
                  <el-button v-if="isDone(scope.row)" link type="warning" icon="Camera" @click="openExec(scope.row, 'photos')">补照片</el-button>
                  <el-button v-if="isDone(scope.row)" link type="info" icon="RefreshLeft" @click="revoke(scope.row)">撤回</el-button>
               </template>
               <!-- 医生 / 管理员：改方案、撤销完成、删整组、打信息卡 -->
               <template v-else>
                  <el-button link type="primary" icon="Edit" @click="openPlan(scope.row)" v-hasPermi="['cat:medication:edit']">修改</el-button>
                  <el-button v-if="isDone(scope.row)" link type="warning" icon="RefreshLeft" @click="revoke(scope.row)">撤销完成</el-button>
                  <el-button v-else link type="danger" icon="Delete" @click="removeGroup(scope.row)" v-hasPermi="['cat:medication:remove']">删除</el-button>
                  <el-button link type="info" icon="Printer" @click="printCard(scope.row.catId)">信息卡</el-button>
               </template>
            </template>
         </el-table-column>
      </el-table>

      <!-- ==================== 药品明细视图（原样保留，统计药品消耗按条算） ==================== -->
      <el-table v-else v-loading="loading" :data="list">
         <el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="56" align="center" />
         <el-table-column label="猫咪" align="center" prop="catId" width="210" :show-overflow-tooltip="true">
            <template #default="scope">{{ catLabel(scope.row.catId) }}</template>
         </el-table-column>
         <el-table-column label="所属基地" width="150" :show-overflow-tooltip="true">
           <template #default="scope">{{ catBaseName(scope.row.catId) }}</template>
         </el-table-column>
         <el-table-column label="所属房间" width="110" :show-overflow-tooltip="true">
           <template #default="scope">{{ catWardName(scope.row.catId) }}</template>
         </el-table-column>
         <el-table-column label="来源" align="center" prop="sourceType" width="128">
            <template #default="scope">
               <el-tag :type="scope.row.sourceType === '2' ? 'primary' : 'info'" size="small" effect="plain">
                  {{ scope.row.sourceType === '2' ? '站内诊断/处方' : '手动录入' }}
               </el-tag>
            </template>
         </el-table-column>
         <el-table-column label="用药日期" align="center" prop="useDate" :show-overflow-tooltip="true" />
         <el-table-column label="时间段" align="center" width="80">
            <template #default="scope">{{ periodLabel(scope.row.timePeriod) }}</template>
         </el-table-column>
         <el-table-column label="药品名称" align="center" prop="drugName" :show-overflow-tooltip="true" />
         <el-table-column label="剂量" align="center" prop="dosage" :show-overflow-tooltip="true" />
         <el-table-column label="给药方式" align="center" width="100">
            <template #default="scope">{{ adminLabel(scope.row.administration) }}</template>
         </el-table-column>
         <el-table-column label="执行人" align="center" prop="executor" width="100" />
         <el-table-column label="不良反应" align="center" prop="adverseReaction" :show-overflow-tooltip="true" min-width="140" />
      </el-table>

      <pagination v-if="!isNarrow" v-show="total > 0" :total="total"
         v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <!-- 执行打卡：助理「完成 / 补照片」 -->
      <el-dialog :title="execTitle" v-model="execOpen" width="560px" append-to-body>
         <el-alert v-if="execMode === 'complete'" type="warning" :closable="false" show-icon class="mb8"
            title="必须上传本次用药照片才能标记完成；完成后整组药一起算完成。" />
         <el-form ref="execFormRef" :model="execForm" label-width="90px">
            <el-form-item label="猫咪">
               <el-input :model-value="execCatText" disabled />
            </el-form-item>
            <el-form-item label="用药">
               <el-input :model-value="execScopeText" disabled />
            </el-form-item>
            <el-form-item label="执行人" prop="executor">
               <staff-select v-model="execForm.executor" scope="all" placeholder="请选择执行人" />
            </el-form-item>
            <el-form-item label="用药照片" prop="photos">
               <image-upload v-model="execForm.photos" :limit="5" :fileSize="10"
                  show-upload-time :fallback-upload-time="execForm.photoTime || ''" />
               <div class="form-tip" v-if="execForm.photoTime">上次上传：{{ execForm.photoTime }}</div>
            </el-form-item>
            <el-form-item label="不良反应" prop="adverseReaction">
               <el-input v-model="execForm.adverseReaction" type="textarea" :rows="2" placeholder="记录不良反应，无则留空" />
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitExec(true)">标记完成</el-button>
               <el-button @click="submitExec(false)">仅保存</el-button>
               <el-button @click="execOpen = false">取 消</el-button>
            </div>
         </template>
      </el-dialog>

      <!-- 医生改这一组的用药方案（上下两段都能编辑） -->
      <el-dialog title="修改用药方案" v-model="planOpen" width="940px" append-to-body>
         <el-alert type="info" :closable="false" show-icon class="mb8"
            title="这里改的只是这一次用药。长期调整请到站内诊断里改处方，那边会留痕且只从改动当天起生效；已完成打卡的组不能改。" />
         <el-form ref="planFormRef" :model="planForm" label-width="90px">
            <el-form-item label="猫咪">
               <el-input :model-value="planCatText" disabled />
            </el-form-item>
            <el-form-item label="用药时间">
               <el-input :model-value="planScopeText" disabled />
            </el-form-item>
            <el-form-item label="医生方案">
               <el-table :data="planForm.details" size="small" border class="plan-table">
                  <el-table-column label="药品名称" min-width="150">
                     <template #default="scope">
                        <el-input v-model="scope.row.drugName" size="small" placeholder="药品名称" />
                     </template>
                  </el-table-column>
                  <el-table-column label="剂量" width="120">
                     <template #default="scope">
                        <el-input v-model="scope.row.dosage" size="small" placeholder="如 1/2 片" />
                     </template>
                  </el-table-column>
                  <el-table-column label="给药方式" width="120">
                     <template #default="scope">
                        <el-select v-model="scope.row.administration" size="small" style="width: 100%">
                           <el-option v-for="d in cat_administration" :key="d.value" :label="d.label" :value="d.value" />
                        </el-select>
                     </template>
                  </el-table-column>
                  <el-table-column label="关联物资" min-width="180">
                     <template #default="scope">
                        <material-select v-model="scope.row.materialId" size="small"
                           @change="m => onPlanMaterialChange(scope.row, m)" />
                     </template>
                  </el-table-column>
                  <el-table-column label="用量" width="100">
                     <template #default="scope">
                        <el-input-number v-model="scope.row.stockQty" size="small" :min="0" :precision="2"
                           :disabled="!scope.row.materialId" controls-position="right" style="width: 100%" />
                     </template>
                  </el-table-column>
                  <el-table-column label="操作" width="60" align="center">
                     <template #default="scope">
                        <el-button link type="danger" icon="Delete" @click="planForm.details.splice(scope.$index, 1)" />
                     </template>
                  </el-table-column>
               </el-table>
               <el-button class="mt6" size="small" plain icon="Plus" @click="addPlanRow">再加一味药</el-button>
            </el-form-item>
            <el-form-item label="执行情况">
               <staff-select v-model="planForm.executor" scope="all" placeholder="执行人（可留空，助理打卡时自动记本人）" />
               <el-input v-model="planForm.adverseReaction" type="textarea" :rows="2" class="mt6" placeholder="不良反应，无则留空" />
               <image-upload v-model="planForm.photos" :limit="5" :fileSize="10" class="mt6"
                  show-upload-time :fallback-upload-time="planForm.photoTime || ''" />
               <div class="form-tip" v-if="planForm.photos">
                  图片下方标的是上传时间，用来区分是哪一次用药拍的；这组用药时间是 {{ planScopeText }}
               </div>
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitPlan">确 定</el-button>
               <el-button @click="planOpen = false">取 消</el-button>
            </div>
         </template>
      </el-dialog>

      <!-- 医生补录单次用药（原来的新增表单，仅医生/管理员可见） -->
      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="猫咪" prop="catId">
               <cat-select v-model="form.catId" placeholder="请选择猫咪" />
            </el-form-item>
            <el-form-item label="用药日期" prop="useDate">
               <el-row :gutter="10" style="width: 100%">
                  <el-col :span="14">
                     <el-date-picker v-model="form.useDate" type="date" value-format="YYYY-MM-DD" placeholder="开始用药日期" style="width: 100%" />
                  </el-col>
                  <el-col :span="10">
                     <el-input-number v-model="batchDays" :min="1" :max="60" :disabled="form.medicationId != undefined" controls-position="right" style="width: 100%" />
                  </el-col>
               </el-row>
               <div class="form-tip">第二个数字是<b>「连续天数」</b>：填 <b>7</b> 表示从开始日期起连续录入 <b>7</b> 天，同一天同一种药不会重复生成。</div>
            </el-form-item>
            <el-form-item label="时间段" prop="timePeriod">
               <el-select v-model="form.timePeriod" placeholder="请选择时间段" style="width: 100%">
                  <el-option v-for="d in cat_time_period" :key="d.value" :label="d.label" :value="d.value" />
               </el-select>
            </el-form-item>
            <el-form-item label="药品名称" prop="drugName">
               <el-input v-model="form.drugName" placeholder="请输入药品名称" />
            </el-form-item>
            <el-form-item label="剂量" prop="dosage">
               <el-input v-model="form.dosage" placeholder="请输入剂量" />
            </el-form-item>
            <el-form-item label="给药方式" prop="administration">
               <el-select v-model="form.administration" placeholder="请选择给药方式" style="width: 100%">
                  <el-option v-for="d in cat_administration" :key="d.value" :label="d.label" :value="d.value" />
               </el-select>
            </el-form-item>
            <el-form-item label="关联物资" prop="materialId">
               <material-select v-model="form.materialId" @change="onFormMaterialChange" />
               <div class="form-tip">
                  挂了物资，保存后自动生成一张<b>出库单</b>并扣库存；不选就不动库存。「剂量」是每次吃多少，这里的「用量」是从库房领了多少。
               </div>
            </el-form-item>
            <el-form-item label="本次用量" prop="stockQty" v-if="form.materialId">
               <el-input-number v-model="form.stockQty" :min="0" :precision="2" :step="1" controls-position="right" style="width: 100%" />
               <div class="form-tip" v-if="formMaterialStock !== null">
                  当前库存 <b>{{ formMaterialStock }}{{ formMaterialUnit }}</b>
                  <span v-if="form.stockQty > formMaterialStock" class="stock-warn">
                     · 本次用量超出库存，保存后库存会变成负数（允许，事后入库补齐即可）
                  </span>
                  <span v-else> · 扣完剩 {{ roundStock(formMaterialStock - (form.stockQty || 0)) }}{{ formMaterialUnit }}</span>
                  <span v-if="batchDays > 1"> · 连续 {{ batchDays }} 天<b>只扣这一次</b>（一次发药）</span>
               </div>
            </el-form-item>
            <el-form-item label="执行人" prop="executor">
               <staff-select v-model="form.executor" scope="all" placeholder="可留空，助理打卡时记本人" />
            </el-form-item>
            <el-form-item label="不良反应" prop="adverseReaction">
               <el-input v-model="form.adverseReaction" type="textarea" :rows="2" placeholder="记录不良反应，无则留空" />
            </el-form-item>
            <el-form-item label="用药照片" prop="photos">
               <image-upload v-model="form.photos" :limit="5" :fileSize="10" show-upload-time />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
               <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitForm">确 定</el-button>
               <el-button @click="cancel">取 消</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="CatMedication">
import { useCatCache } from "@/utils/catCache"
import {
  listCatMedication, addCatMedication, addCatMedicationBatch,
  listCatMedicationGroup, getCatMedicationGroupDetails, saveCatMedicationGroup,
  saveCatMedicationExec, delCatMedication
} from "@/api/cat/medication"
import { listCatStaffOptions } from "@/api/cat/staff"
import { listCatMaterial } from "@/api/cat/material"
import { uploadTimeLabel } from "@/utils/photoTime"
import { resolveFileUrl } from "@/utils/fileUrl"
import useUserStore from "@/store/modules/user"

const { proxy } = getCurrentInstance()
const { cat_time_period, cat_administration } = proxy.useDict('cat_time_period', 'cat_administration')
const { catLabel, catBaseName, catWardName } = useCatCache()
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const roles = computed(() => userStore.roles || [])
const isManager = computed(() => roles.value.includes('admin') || roles.value.includes('manager'))
const isDoctor = computed(() => roles.value.includes('doctor'))
const isAssistant = computed(() => roles.value.includes('assistant'))
/** 医生方案（新增/改药）只有医生和管理员能碰，后端也拦 */
const canEditPlan = computed(() => isDoctor.value || isManager.value)

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])
const batchDays = ref(1)
// 选中物资的库存快照：用来即时提示「扣完还剩多少」，库存不足只提示不阻断
const formMaterialStock = ref(null)
const formMaterialUnit = ref('')

function onFormMaterialChange(material) {
   formMaterialStock.value = material ? Number(material.stock) : null
   formMaterialUnit.value = material ? (material.unit || '') : ''
   form.value.materialName = material ? material.materialName : undefined
   if (!material) {
      // 传 0 而不是 undefined：让后端知道是「主动取消关联」，否则动态 update 写不进 null
      form.value.materialId = 0
      form.value.stockQty = undefined
   } else if (!form.value.stockQty) {
      form.value.stockQty = 1
   }
}

function onPlanMaterialChange(row, material) {
   row.materialName = material ? material.materialName : undefined
   if (!material) {
      row.materialId = 0
      row.stockQty = undefined
   } else if (!row.stockQty) {
      row.stockQty = 1
   }
}

function roundStock(value) {
   const num = Number(value)
   if (!Number.isFinite(num)) return 0
   return Number.isInteger(num) ? num : Number(num.toFixed(2))
}

/** 提交前统一处理物资三件套：空值一律转 0，后端据此判断「取消关联」 */
function withMaterial(row) {
   const empty = row.materialId === undefined || row.materialId === null || row.materialId === ''
   return {
      ...row,
      materialId: empty ? 0 : row.materialId,
      materialName: empty ? undefined : row.materialName,
      stockQty: empty ? undefined : row.stockQty
   }
}
// group：同一只猫 + 同一天 + 同一时间段 一行（默认）；detail：一个药一行
const viewMode = ref('group')

// 窄屏（手机）用卡片，不再把电脑表格缩到手机上
const isNarrow = ref(false)
function syncNarrow() {
  isNarrow.value = typeof window !== 'undefined' && window.innerWidth < 768
}

const assistantOptions = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    catBaseId: undefined,
    catWardId: undefined,
    assistantUserId: undefined,
    sourceType: undefined,
    catId: undefined,
    useDate: undefined,
    timePeriod: undefined,
    execStatus: undefined,
    drugName: undefined,
    dosage: undefined,
  },
  rules: {
    catId: [{ required: true, message: '请选择猫咪', trigger: 'change' }],
    useDate: [{ required: true, message: '请选择用药日期', trigger: 'change' }],
    drugName: [{ required: true, message: '请输入药品名称', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

// ---------------- 展示辅助 ----------------
function periodLabel(value) {
  if (!value) return '未分时段'
  const hit = (cat_time_period.value || []).find(d => d.value === value)
  return hit ? hit.label : value
}
function adminLabel(value) {
  if (!value) return '—'
  const hit = (cat_administration.value || []).find(d => d.value === value)
  return hit ? hit.label : value
}
function staffLabel(s) {
  return s.nickName && s.userName && s.nickName !== s.userName ? `${s.nickName}（${s.userName}）` : (s.nickName || s.userName)
}
function groupKey(row) {
  return `${row.catId}_${row.useDate}_${row.timePeriod || ''}`
}

/** 跳到这只猫的信息卡：只负责定位到它，打不打印由用户自己点（不自动弹打印对话框） */
function printCard(catId) {
  if (!catId) return
  router.push({ path: '/profile/qrcard', query: { catIds: catId } })
}
function isDone(row) {
  return row.execStatus === '1'
}
/**
 * 照片地址列表
 * 库里存的是相对路径（/profile/upload/...），必须补上接口前缀才能访问；
 * 漏了这一步浏览器会去前端端口要图，直接 404 显示「加载失败」。
 * 文件名含中文，统一交给 resolveFileUrl 编码。
 */
function photoList(photos) {
  return String(photos || '')
    .split(',')
    .map(p => p && p.trim())
    .filter(Boolean)
    .map(resolveFileUrl)
}
/**
 * 单张照片的上传时间（MM-DD HH:mm）
 * 上传接口给文件命名时会带上 yyyyMMddHHmmss，所以同一组里不同时间传的图能分别标出来；
 * 老图片/演示图片没有时间戳，退回这组记录里的上传时间
 */
function shotTime(url, fallback) {
  return uploadTimeLabel(url, fallback)
}
function timeOnly(text) {
  return String(text || '').slice(11, 16) || text
}

// ---------------- 列表 ----------------
function buildParams() {
  return proxy.addDateRange({ ...queryParams.value }, dateRange.value)
}

function getList() {
  loading.value = true
  const params = buildParams()
  const request = viewMode.value === 'group' ? listCatMedicationGroup(params) : listCatMedication(params)
  request.then(response => {
    list.value = response.rows || []
    total.value = response.total || 0
    expandedKeys.value = []
  }).finally(() => {
    loading.value = false
  })
}

function onViewChange() {
  queryParams.value.pageNum = 1
  getList()
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  queryParams.value.assistantUserId = undefined
  queryParams.value.execStatus = undefined
  queryParams.value.timePeriod = undefined
  handleQuery()
}

function getAssistantOptions() {
  listCatStaffOptions('assistant').then(res => {
    assistantOptions.value = res.data || []
  })
}

// ---------------- 展开明细 ----------------
const expandedKeys = ref([])
const detailMap = reactive({})
const detailLoading = reactive({})

function isExpanded(row) {
  return expandedKeys.value.includes(groupKey(row))
}
function detailOf(row) {
  return detailMap[groupKey(row)] || []
}
function loadDetails(row) {
  const key = groupKey(row)
  if (detailMap[key]) return
  detailLoading[key] = true
  getCatMedicationGroupDetails({ catId: row.catId, useDate: row.useDate, timePeriod: row.timePeriod || '' })
    .then(res => {
      detailMap[key] = res.data || []
    })
    .finally(() => {
      detailLoading[key] = false
    })
}

function toggleExpand(row) {
  const key = groupKey(row)
  if (isExpanded(row)) {
    expandedKeys.value = expandedKeys.value.filter(k => k !== key)
  } else {
    expandedKeys.value = [...expandedKeys.value, key]
    loadDetails(row)
  }
}

// 电脑端 el-table 的展开箭头（展开行不需要额外拉数据也能看到药名，这里仍然加载以便显示备注与照片）
function onRowExpand(row, expandedRows) {
  const expanded = Array.isArray(expandedRows)
    ? expandedRows.some(item => groupKey(item) === groupKey(row))
    : !!expandedRows
  expandedKeys.value = Array.isArray(expandedRows) ? expandedRows.map(groupKey) : (expanded ? [groupKey(row)] : [])
  if (expanded) loadDetails(row)
}

// ---------------- 执行打卡 ----------------
const execOpen = ref(false)
const execMode = ref('complete')
const execFormRef = ref(null)
const execForm = reactive({})
const execRow = ref({})
const execTitle = computed(() => (execMode.value === 'complete' ? '用药打卡 · 标记完成' : '用药打卡 · 补照片 / 修改执行情况'))
const execCatText = computed(() => `${catLabel(execRow.value.catId) || ''} · ${execRow.value.useDate || ''}`)
const execScopeText = computed(() => `${periodLabel(execRow.value.timePeriod)} · ${execRow.value.drugCount || 0} 种药：${execRow.value.drugNames || ''}`)

function openExec(row, mode) {
  execRow.value = row
  execMode.value = mode
  Object.keys(execForm).forEach(k => delete execForm[k])
  Object.assign(execForm, {
    executor: row.executor || userStore.nickName,
    photos: row.photos || '',
    adverseReaction: row.adverseReaction || '',
    photoTime: row.photoTime
  })
  execOpen.value = true
}

function submitExec(complete) {
  const payload = {
    catId: execRow.value.catId,
    useDate: execRow.value.useDate,
    timePeriod: execRow.value.timePeriod || '',
    executor: execForm.executor,
    photos: execForm.photos,
    adverseReaction: execForm.adverseReaction,
    status: complete ? '1' : undefined
  }
  if (complete) {
    if (!execForm.photos) {
      proxy.$modal.msgWarning('请先上传本次用药照片，再点「标记完成」')
      return
    }
  }
  saveCatMedicationExec(payload).then(res => {
    proxy.$modal.msgSuccess(complete ? '已标记完成' : '已保存')
    execOpen.value = false
    // 展开缓存里的状态跟着变，避免还要手动刷新
    delete detailMap[groupKey(execRow.value)]
    getList()
  })
}

function revoke(row) {
  const tip = isManager.value || isDoctor.value ? '撤销完成' : '撤回'
  proxy.$modal.confirm(`确认${tip}「${catLabel(row.catId)} ${row.useDate} ${periodLabel(row.timePeriod)}」这一组用药的完成状态吗？照片会保留，修改时间会记录。`)
    .then(() => saveCatMedicationExec({
      catId: row.catId,
      useDate: row.useDate,
      timePeriod: row.timePeriod || '',
      status: '0'
    }))
    .then(() => {
      proxy.$modal.msgSuccess('已撤回，可重新打卡')
      getList()
    })
    .catch(() => {})
}

// ---------------- 医生改这一组的方案 ----------------
const planOpen = ref(false)
const planFormRef = ref(null)
const planForm = reactive({ details: [], executor: undefined, adverseReaction: undefined, photos: undefined, photoTime: '' })
const planRow = ref({})
const planCatText = computed(() => catLabel(planRow.value.catId) || '')
const planScopeText = computed(() => `${planRow.value.useDate || ''} · ${periodLabel(planRow.value.timePeriod)}`)

function openPlan(row) {
  planRow.value = row
  planForm.details = []
  planForm.executor = row.executor || undefined
  planForm.adverseReaction = row.adverseReaction || undefined
  planForm.photos = row.photos || undefined
  planForm.photoTime = row.photoTime || ''
  planOpen.value = true
  getCatMedicationGroupDetails({ catId: row.catId, useDate: row.useDate, timePeriod: row.timePeriod || '' })
    .then(res => {
      planForm.details = (res.data || []).map(d => ({
        medicationId: d.medicationId,
        drugName: d.drugName,
        dosage: d.dosage,
        administration: d.administration,
        remark: d.remark,
        materialId: d.materialId || undefined,
        materialName: d.materialName || undefined,
        stockQty: d.stockQty || undefined
      }))
      if (!planForm.details.length) addPlanRow()
    })
}

function addPlanRow() {
  planForm.details.push({ drugName: '', dosage: '', administration: '口服', materialId: undefined, stockQty: undefined })
}

function submitPlan() {
  if (!planForm.details.length) {
    proxy.$modal.msgWarning('请至少保留一种药品')
    return
  }
  saveCatMedicationGroup({
    catId: planRow.value.catId,
    useDate: planRow.value.useDate,
    timePeriod: planRow.value.timePeriod || '',
    executor: planForm.executor,
    adverseReaction: planForm.adverseReaction,
    photos: planForm.photos,
    details: planForm.details.map(withMaterial)
  }).then(() => {
    proxy.$modal.msgSuccess('修改成功')
    planOpen.value = false
    getList()
  })
}

function removeGroup(row) {
  proxy.$modal.confirm(`确认删除「${catLabel(row.catId)} ${row.useDate} ${periodLabel(row.timePeriod)}」这一组的 ${row.drugCount} 条用药记录吗？`)
    .then(() => getCatMedicationGroupDetails({ catId: row.catId, useDate: row.useDate, timePeriod: row.timePeriod || '' }))
    .then(res => delCatMedication((res.data || []).map(d => d.medicationId).join(',')))
    .then(() => {
      proxy.$modal.msgSuccess('删除成功')
      getList()
    })
    .catch(() => {})
}

// ---------------- 医生补录单次用药（原新增表单） ----------------
function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    catId: undefined,
    useDate: undefined,
    timePeriod: undefined,
    drugName: undefined,
    dosage: undefined,
    administration: undefined,
    executor: undefined,
    adverseReaction: undefined,
    photos: undefined,
    remark: undefined,
    materialId: undefined,
    materialName: undefined,
    stockQty: undefined
  }
  formMaterialStock.value = null
  formMaterialUnit.value = ''
  batchDays.value = 1
  proxy.clearFormValidate("formRef")
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "添加用药记录"
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (batchDays.value > 1) {
        addCatMedicationBatch(withMaterial(form.value), batchDays.value).then(res => {
          proxy.$modal.msgSuccess(res.msg || "批量录入成功")
          open.value = false
          getList()
        })
      } else {
        addCatMedication(withMaterial(form.value)).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleExport() {
  // 导出永远走明细接口：统计药品消耗要按条算，不能按合并后的组算
  proxy.download("cat/medication/export", { ...buildParams() }, `medication_${new Date().getTime()}.xlsx`)
}

// 扫码页「+ 快速记一笔」带 add=1&catId=xx 进来：直接打开新增，猫咪已填好
function openQuickEntry() {
  const q = route.query || {}
  if (String(q.add) !== '1' || !q.catId) return
  if (!canEditPlan.value) {
    proxy.$modal.msgWarning('助理不能新增用药记录，请由医生在站内诊断里开处方')
    router.replace({ path: route.path, query: {} })
    return
  }
  handleAdd()
  form.value.catId = Number(q.catId)
  router.replace({ path: route.path, query: {} })
}

/**
 * 物资 id → 单位（片/支/瓶）。用药明细里的「本次用量」只有数字，
 * 不带上单位就看不出扣了 1 片还是 1 盒；台账本身不存单位，按物资现取。
 */
const materialUnits = ref({})
function getMaterialUnits() {
  listCatMaterial({ pageNum: 1, pageSize: 999 }).then(res => {
    const map = {}
    ;(res.rows || []).forEach(m => { map[m.materialId] = m.unit || '' })
    materialUnits.value = map
  })
}

onMounted(() => {
  syncNarrow()
  window.addEventListener('resize', syncNarrow)
  getAssistantOptions()
  getList()
  getMaterialUnits()
  openQuickEntry()
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', syncNarrow)
})
watch(() => route.query, () => openQuickEntry())
</script>

<style scoped>
.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  flex-wrap: wrap;
  gap: 8px;
}
.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.toolbar-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.hint-icon {
  font-size: 15px;
  color: var(--el-text-color-secondary);
  cursor: help;
}
.muted {
  color: var(--el-text-color-placeholder);
}
.form-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 18px;
  margin-top: 2px;
}
.drug-names {
  margin-left: 6px;
  font-size: 13px;
}
.group-detail {
  padding: 8px 16px 8px 48px;
  background: var(--el-fill-color-lighter);
}
.detail-table {
  margin-bottom: 8px;
}
.exec-block {
  font-size: 13px;
  line-height: 22px;
}
.exec-line {
  margin-bottom: 4px;
}
.photo-wrap {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 6px;
  vertical-align: middle;
  margin-left: 6px;
}
.group-photo {
  width: 76px;
  height: 76px;
  border-radius: 4px;
}
/* 一张图 + 它自己的上传时间，图片和文字对齐成一列 */
.photo-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.shot-time {
  font-size: 11px;
  line-height: 14px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}
.plan-table {
  width: 100%;
}
.mt6 {
  margin-top: 6px;
}

/* ---------------- 手机卡片 ---------------- */
.card-list {
  min-height: 200px;
}
.med-card {
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
  background: var(--el-bg-color);
}
.med-card.done {
  border-color: var(--el-color-success-light-5);
  background: var(--el-color-success-light-9);
}
.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  cursor: pointer;
}
.card-name {
  font-size: 16px;
  font-weight: 600;
  line-height: 22px;
}
.card-sub {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}
.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 12px;
  color: var(--el-text-color-regular);
  margin-top: 8px;
}
.card-drugs {
  font-size: 13px;
  color: var(--el-text-color-regular);
  margin-top: 6px;
  line-height: 20px;
}
.card-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  flex-wrap: wrap;
}
.card-detail {
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px dashed var(--el-border-color);
  font-size: 13px;
}
.detail-line {
  display: flex;
  gap: 8px;
  line-height: 22px;
}
.detail-drug {
  font-weight: 600;
}
.detail-dosage {
  color: var(--el-text-color-regular);
}
.detail-way {
  color: var(--el-text-color-secondary);
}
.card-photos {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-top: 6px;
}
.card-photo {
  width: 68px;
  height: 68px;
  border-radius: 4px;
}
.card-note {
  margin-top: 6px;
  color: var(--el-color-warning);
}
</style>
