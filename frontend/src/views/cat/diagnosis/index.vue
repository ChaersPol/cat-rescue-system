<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="72px">
         <el-form-item label="猫咪" prop="catId">
            <cat-select v-model="queryParams.catId" placeholder="全部猫咪" style="width: 190px" />
         </el-form-item>
         <el-form-item label="来源基地" prop="sourceBaseId">
            <el-select v-model="queryParams.sourceBaseId" placeholder="全部来源基地" clearable filterable style="width: 180px" @change="handleQuery">
               <el-option v-for="b in allBases" :key="b.baseId" :label="b.baseName" :value="b.baseId" />
            </el-select>
         </el-form-item>
         <el-form-item label="治疗基地" prop="baseId">
            <el-select v-model="queryParams.baseId" placeholder="全部医疗基地" clearable filterable style="width: 180px" @change="onBaseFilterChange">
               <el-option v-for="b in baseOptions" :key="b.baseId" :label="baseOptionLabel(b)" :value="b.baseId" />
            </el-select>
         </el-form-item>
         <el-form-item label="所在病房" prop="wardId">
            <el-select v-model="queryParams.wardId" placeholder="全部病房" clearable filterable style="width: 190px" @change="handleQuery">
               <el-option-group v-for="group in wardGroups" :key="group.baseId" :label="group.baseName">
                  <el-option v-for="w in group.wards" :key="w.wardId" :label="wardOptionLabel(w)" :value="w.wardId" />
               </el-option-group>
               <!-- 早期记录没填病房，单独留给追溯用 -->
               <el-option label="未填病房" :value="0" />
            </el-select>
         </el-form-item>
         <el-form-item label="就诊类型" prop="visitType">
            <el-select v-model="queryParams.visitType" placeholder="全部" clearable style="width: 110px" @change="handleQuery">
               <el-option label="初诊" value="1" />
               <el-option label="复诊" value="2" />
            </el-select>
         </el-form-item>
         <el-form-item label="就诊日期">
            <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange" range-separator="-"
               start-placeholder="开始日期" end-placeholder="结束日期" style="width: 250px" />
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <!-- 当前筛选范围的统计：猫数按位置算、记录数按条件算，两个口径不同，这里分开写清楚 -->
      <el-alert v-if="statTip" type="info" :closable="false" show-icon class="mb8 scope-alert">
         <template #title>
            <span class="scope-main">{{ statTip }}</span>
            <span class="scope-note">{{ statNote }}</span>
         </template>
      </el-alert>

      <div class="table-toolbar">
         <div class="toolbar-left">
            <el-button v-if="canAddDiagnosis" type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:diagnosis:add']">新增</el-button>
            <template v-if="!mergeByCat">
               <el-button type="success" plain icon="Edit" v-if="$route.meta.canRemove" :disabled="single" @click="handleUpdate" v-hasPermi="['cat:diagnosis:edit']">修改</el-button>
               <el-button type="danger" plain icon="Delete" v-if="$route.meta.canRemove" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:diagnosis:remove']">删除</el-button>
               <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['cat:diagnosis:export']">导出</el-button>
            </template>
         </div>
         <div class="toolbar-right">
            <el-tooltip placement="top" content="关闭：一次就诊一行（查流水、统计医生工作量）；打开：一只猫一行，可展开看全部病史">
               <span class="switch-label">按猫合并</span>
            </el-tooltip>
            <el-switch v-model="mergeByCat" @change="onViewChange" />
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
         </div>
      </div>

      <!-- 流水视图：一次就诊一行 -->
      <el-table v-if="!mergeByCat" v-loading="loading" :data="list" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="50" align="center" v-if="$route.meta.canRemove" />
         <el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="56" align="center" />
         <el-table-column label="猫咪" align="left" prop="catId" min-width="180" :show-overflow-tooltip="true">
            <template #default="scope">
               {{ catLabel(scope.row.catId) }}
            </template>
         </el-table-column>
         <el-table-column label="就诊日期" align="center" prop="visitDate" width="166" :show-overflow-tooltip="true" />
         <el-table-column label="类型" align="center" prop="visitType" width="86">
            <template #default="scope">
               <el-tag v-if="scope.row.visitType" size="small" :type="visitTypeTag(scope.row.visitType)"
                  :effect="scope.row.visitType === '1' ? 'dark' : 'plain'">
                  {{ visitTypeLabel(scope.row.visitType) }}
               </el-tag>
               <el-tooltip v-else content="历史记录未标注类型，可在「修改」里补上">
                  <span class="muted">未标注</span>
               </el-tooltip>
            </template>
         </el-table-column>
         <el-table-column label="医生" align="center" prop="doctor" width="96" :show-overflow-tooltip="true" />
         <el-table-column label="来源基地" align="center" prop="sourceBaseId" min-width="140" :show-overflow-tooltip="true">
            <template #default="scope">
               <span v-if="scope.row.sourceBaseId">{{ baseLabel(scope.row.sourceBaseId) }}</span>
               <span v-else class="muted">—</span>
            </template>
         </el-table-column>
         <el-table-column label="治疗基地" align="center" prop="baseId" min-width="140" :show-overflow-tooltip="true">
            <template #default="scope">
               <span v-if="scope.row.baseId">{{ baseLabel(scope.row.baseId) }}</span>
               <span v-else class="muted">—</span>
            </template>
         </el-table-column>
         <el-table-column label="所在病房" align="center" prop="wardId" min-width="110" :show-overflow-tooltip="true">
            <template #default="scope">
               <span v-if="scope.row.wardId">{{ wardLabel(scope.row.wardId) }}</span>
               <span v-else class="muted">—</span>
            </template>
         </el-table-column>
         <el-table-column label="症状" align="center" prop="symptoms" min-width="130" :show-overflow-tooltip="true" />
         <el-table-column label="诊断结果" align="center" prop="diagnosisResult" min-width="130" :show-overflow-tooltip="true" />
         <el-table-column label="更正" align="center" width="76">
            <template #default="scope">
               <el-tooltip v-if="scope.row.correctionNote" :content="scope.row.correctionNote" placement="top">
                  <el-tag size="small" type="warning" effect="plain">已更正</el-tag>
               </el-tooltip>
               <span v-else class="muted">—</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="308" fixed="right" class-name="small-padding fixed-width" v-if="checkPermi(['cat:diagnosis:edit', 'cat:diagnosis:remove'])">
            <template #default="scope">
               <el-button link type="info" icon="Printer" @click="printCard(scope.row.catId)">信息卡</el-button>
               <el-button link type="warning" icon="EditPen" @click="handleCorrect(scope.row)" v-hasPermi="['cat:diagnosis:edit']">更正备注</el-button>
               <el-button v-if="isAdmin" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:diagnosis:edit']">修改</el-button>
               <el-button v-if="isAdmin" link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:diagnosis:remove']">删除</el-button>
            </template>
         </el-table-column>
         <template #empty>
            <div class="diag-empty">
               <template v-if="!loading && pendingCats.length">
                  <div class="diag-empty-title">
                     {{ emptyScopeLabel }}还有 {{ pendingCats.length }} 只猫没录过诊断
                  </div>
                  <div class="diag-empty-sub">猫在病房里、但还没有病历，通常就是还没接诊。点「接诊」直接登记本次诊断即可。</div>
                  <div class="diag-empty-cats">
                     <span v-for="c in pendingCats" :key="c.catId" class="diag-empty-cat">
                        <span class="diag-empty-name">{{ formatCatLabel(c.catName, c.archiveNo) }}</span>
                        <el-button link type="primary" @click="handleAddForCat(c)" v-hasPermi="['cat:diagnosis:add']">接诊</el-button>
                     </span>
                  </div>
               </template>
               <template v-else-if="!loading">
                  <div class="diag-empty-title">暂无诊断记录</div>
                  <div class="diag-empty-sub" v-if="stat.catCount > 0 && stat.recordCount === 0">
                     {{ stat.catCount }} 只猫算的是「此刻住在这里」的猫，记录按就诊时所在病房归属，<br />
                     所以它们的历史病历可能在其它病房下，换个病房或清空筛选能看到。
                  </div>
               </template>
            </div>
         </template>
      </el-table>

      <!-- 合并视图：一只猫一行，展开看全部病史 -->
      <el-table v-else ref="historyTable" v-loading="loading" :data="list" row-key="catId" @expand-change="onExpandChange">
         <el-table-column type="expand">
            <template #default="scope">
               <div class="history-panel" v-loading="historyLoading[scope.row.catId]">
                  <el-empty v-if="historyLoaded(scope.row.catId) && !historyList(scope.row.catId).length"
                     description="暂无诊断记录" :image-size="48" />
                  <el-timeline v-else>
                     <el-timeline-item v-for="h in historyList(scope.row.catId)" :key="h.diagnosisId"
                        :timestamp="h.visitDate" placement="top" :color="h.visitType === '1' ? '#e6a23c' : '#909399'">
                        <div class="history-item">
                           <div class="history-head">
                              <el-tag size="small" :type="visitTypeTag(h.visitType)" :effect="h.visitType === '1' ? 'dark' : 'plain'">
                                 {{ visitTypeLabel(h.visitType) }}
                              </el-tag>
                              <span class="history-doctor">医生：{{ h.doctor || '—' }}</span>
                              <span class="history-doctor">基地：{{ baseLabel(h.baseId) || '—' }}</span>
                              <span class="history-doctor">病房：{{ h.wardId ? wardLabel(h.wardId) : '—' }}</span>
                           </div>
                           <div class="history-line"><b>症状：</b>{{ h.symptoms || '—' }}</div>
                           <div class="history-line"><b>诊断：</b>{{ h.diagnosisResult || '—' }}</div>
                           <div class="history-line" v-if="h.checkItems"><b>检查及用药：</b>{{ h.checkItems }}</div>
                           <div class="history-line" v-if="h.medicalAdvice"><b>医嘱：</b>{{ h.medicalAdvice }}</div>
                           <div class="history-line correction" v-if="h.correctionNote"><b>更正：</b>{{ h.correctionNote }}</div>
                        </div>
                     </el-timeline-item>
                  </el-timeline>
               </div>
            </template>
         </el-table-column>
         <el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="56" align="center" />
         <el-table-column label="猫咪" align="left" prop="catId" min-width="180" :show-overflow-tooltip="true">
            <template #default="scope">{{ formatCatLabel(scope.row.catName, scope.row.archiveNo) }}</template>
         </el-table-column>
         <el-table-column label="来源基地" align="center" prop="sourceBaseId" min-width="140" :show-overflow-tooltip="true">
            <template #default="scope">
               <span v-if="scope.row.sourceBaseId">{{ baseLabel(scope.row.sourceBaseId) }}</span>
               <span v-else class="muted">—</span>
            </template>
         </el-table-column>
         <el-table-column label="当前病房" align="center" prop="wardId" min-width="120" :show-overflow-tooltip="true">
            <template #default="scope">
               <span v-if="scope.row.wardId">{{ wardLabel(scope.row.wardId) }}</span>
               <span v-else class="muted">—</span>
            </template>
         </el-table-column>
         <el-table-column label="就诊次数" align="center" prop="visitCount" width="130">
            <template #default="scope">
               <el-tag size="small" type="primary" effect="plain">{{ scope.row.visitCount }} 次</el-tag>
               <el-tooltip v-if="scope.row.initialCount > 1" :content="`有 ${scope.row.initialCount} 条记录标成了「初诊」，可能填错，点开可核对`" placement="top">
                  <el-tag size="small" type="danger" effect="plain" style="margin-left: 4px">初诊×{{ scope.row.initialCount }}</el-tag>
               </el-tooltip>
            </template>
         </el-table-column>
         <el-table-column label="首次就诊" align="center" prop="firstVisitDate" width="166" :show-overflow-tooltip="true" />
         <el-table-column label="最近就诊" align="center" prop="lastVisitTime" width="166" :show-overflow-tooltip="true" />
         <el-table-column label="最近诊断结果" align="left" prop="lastDiagnosisResult" min-width="180" :show-overflow-tooltip="true">
            <template #default="scope">
               <span v-if="scope.row.lastDiagnosisResult">{{ scope.row.lastDiagnosisResult }}</span>
               <span v-else class="muted">—</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="110" fixed="right">
            <template #default="scope">
               <el-button link type="primary" @click="toggleHistory(scope.row)">全部病史</el-button>
            </template>
         </el-table-column>
         <template #empty>
            <div class="diag-empty">
               <template v-if="!loading && pendingCats.length">
                  <div class="diag-empty-title">
                     {{ emptyScopeLabel }}还有 {{ pendingCats.length }} 只猫没录过诊断
                  </div>
                  <div class="diag-empty-sub">猫在病房里、但还没有病历，通常就是还没接诊。点「接诊」直接登记本次诊断即可。</div>
                  <div class="diag-empty-cats">
                     <span v-for="c in pendingCats" :key="c.catId" class="diag-empty-cat">
                        <span class="diag-empty-name">{{ formatCatLabel(c.catName, c.archiveNo) }}</span>
                        <el-button link type="primary" @click="handleAddForCat(c)" v-hasPermi="['cat:diagnosis:add']">接诊</el-button>
                     </span>
                  </div>
               </template>
               <template v-else-if="!loading">
                  <div class="diag-empty-title">暂无诊断记录</div>
                  <div class="diag-empty-sub" v-if="stat.catCount > 0 && stat.recordCount === 0">
                     {{ stat.catCount }} 只猫算的是「此刻住在这里」的猫，记录按就诊时所在病房归属，<br />
                     所以它们的历史病历可能在其它病房下，换个病房或清空筛选能看到。
                  </div>
               </template>
            </div>
         </template>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <el-dialog :title="title" v-model="open" :width="isNarrow ? '96%' : '880px'" append-to-body :top="isNarrow ? '2vh' : '6vh'">
         <!-- 手机上标签放输入框上方（顶部对齐），否则 4 个字的标签会在窄列里折成两行 -->
         <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" :label-position="isNarrow ? 'top' : 'right'">
            <el-form-item label="猫咪" prop="catId">
               <cat-select v-model="form.catId" @change="onCatChange" placeholder="请选择猫咪" />
            </el-form-item>
            <el-form-item label="就诊日期" prop="visitDate">
               <el-date-picker v-model="form.visitDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择就诊日期" style="width: 100%" />
            </el-form-item>
            <el-form-item label="就诊类型" prop="visitType">
               <el-radio-group v-model="form.visitType">
                  <el-radio-button value="1">初诊</el-radio-button>
                  <el-radio-button value="2">复诊</el-radio-button>
               </el-radio-group>
               <div class="form-tip" v-if="visitTypeHint">{{ visitTypeHint }}</div>
            </el-form-item>
            <el-form-item label="医生" prop="doctorUserId" :rules="isDoctor || form.diagnosisId ? undefined : doctorRequiredRule">
               <!-- 医生本人：只读显示当前登录账号，防止冒填别人的名字 -->
               <el-input v-if="isDoctor" :model-value="form.doctor" disabled placeholder="当前登录医生" />
               <!-- 超管 / 管理员：只能从系统里的医生账号中选 -->
               <el-select v-else v-model="form.doctorUserId" placeholder="请选择接诊医生" filterable clearable style="width: 100%"
                  @change="onDoctorChange">
                  <el-option v-for="d in doctorOptions" :key="d.userId" :label="doctorOptionLabel(d)" :value="d.userId" />
               </el-select>
               <div class="form-tip" v-if="isDoctor">取自当前登录账号，不能修改</div>
               <div class="form-tip" v-else>只能选系统里的医生账号，避免出现对不上人的名字</div>
               <div class="form-tip" v-if="!isDoctor && form.doctor && !form.doctorUserId">
                  原记录医生为「{{ form.doctor }}」（历史手填，未关联账号），重新选择后会被覆盖
               </div>
            </el-form-item>
            <!-- 来源基地跟随所选猫咪自动带出（猫咪归属基地），只读展示 -->
            <el-form-item label="来源基地">
               <el-input :model-value="sourceBaseName" disabled placeholder="选择猫咪后自动带出" />
               <div class="form-tip">猫咪从哪个基地送来，选了猫咪自动带出，改不了（要改请改档案的归属基地）</div>
            </el-form-item>
            <el-form-item label="治疗基地" prop="baseId">
               <el-select v-model="form.baseId" placeholder="请选择当前治疗基地（仅医疗基地）" clearable filterable style="width: 100%" @change="onFormBaseChange">
                  <el-option v-for="b in baseOptions" :key="b.baseId" :label="baseOptionLabel(b)" :value="b.baseId" />
                  <el-option v-if="legacyBase" :key="'legacy'" :label="`${legacyBase.baseName}（非医疗基地·历史记录）`"
                     :value="legacyBase.baseId" disabled />
               </el-select>
            </el-form-item>
            <el-form-item label="所在病房" prop="wardId">
               <el-select v-model="form.wardId" placeholder="请选择该基地下的病房" clearable filterable style="width: 100%"
                  :disabled="!form.baseId" :no-data-text="form.baseId ? '该基地还没有病房' : '请先选择治疗基地'">
                  <el-option v-for="w in formWardOptions" :key="w.wardId" :label="w.wardName" :value="w.wardId" />
               </el-select>
            </el-form-item>
            <el-form-item label="症状" prop="symptoms">
               <el-input v-model="form.symptoms" type="textarea" :rows="2" placeholder="主诉/症状" />
            </el-form-item>
            <el-form-item label="诊断结果" prop="diagnosisResult">
               <el-input v-model="form.diagnosisResult" type="textarea" :rows="2" placeholder="初步诊断结果" />
            </el-form-item>
            <el-form-item label="检查项目及用药" prop="checkItems" required>
               <!-- 不支持手写，整栏就是一个可点击的选择框：点一下打开目录弹窗 -->
               <div class="item-picker" :class="{ 'is-empty': !itemPickerText, 'is-legacy': isLegacyItems }"
                  @click="openItemPicker">
                  <el-icon class="item-picker-icon"><FirstAidKit /></el-icon>
                  <span v-if="itemPickerText" class="item-picker-text">{{ itemPickerText }}</span>
                  <span v-else class="item-picker-placeholder">点击从目录选择检查项目及用药（不支持手写）</span>
                  <span class="item-picker-action">
                     {{ form.items && form.items.length ? '重新选择' : '选择' }}
                     <el-icon><ArrowRight /></el-icon>
                  </span>
               </div>
               <div class="form-tip warn" v-if="isLegacyItems">
                  上面是历史记录里的手写内容，仅原样保留；要调整请点一下重新选
               </div>
               <div class="form-tip">
                  只能从目录选，关联物资的项目会<b>自动出库扣库存</b>，用药/疫苗/驱虫会<b>自动写台账</b>
               </div>
               <!-- 给药计划：只在选了「用药」类项目时出现，保存时据此生成用药打卡任务 -->
               <div class="rx-plan" v-if="drugRows.length">
                  <span class="rx-plan-title">给药计划</span>
                  <el-date-picker v-model="rxPlan.startDate" type="date" value-format="YYYY-MM-DD" size="small"
                     placeholder="开始日期" style="width: 138px" />
                  <span class="rx-plan-sep">起，连续</span>
                  <el-input-number v-model="rxPlan.days" :min="1" :max="60" size="small" controls-position="right"
                     style="width: 92px" />
                  <span class="rx-plan-sep">天，助理在「用药记录」按「天 × 时段」打卡</span>
               </div>
               <!-- 已选明细（只读汇总）：要改数量/给药方案/用法，点上面那栏的「重新选择」进弹窗改 -->
               <ul class="item-summary" v-if="form.items && form.items.length">
                  <li v-for="(it, idx) in form.items" :key="idx" class="item-summary-row">
                     <el-tag :type="itemTypeTag(it.itemType)" size="small" effect="plain" class="item-summary-type">
                        {{ itemTypeLabel(it.itemType) }}
                     </el-tag>
                     <span class="item-summary-name">{{ it.itemName }}</span>
                     <span class="item-summary-meta">×{{ qtyText(it) }}{{ it.unit || '' }}</span>
                     <span class="item-summary-meta" :class="{ 'short-stock': isShortStock(it) }">{{ stockText(it) }}</span>
                     <span v-if="String(it.itemType) === '2'" class="item-summary-meta"
                        :class="{ 'plan-missing': !(it.periods || []).length }">
                        给药 {{ drugPlanText(it) }}
                     </span>
                     <span v-if="it.usageNote" class="item-summary-note">{{ it.usageNote }}</span>
                  </li>
               </ul>
               <div class="form-tip" v-if="form.items && form.items.length">
                  合计 {{ itemTotal }} 元；保存后自动生成出库记录，台账里会标明「站内诊断」
               </div>
            </el-form-item>
            <el-form-item label="医嘱/预后" prop="medicalAdvice">
               <el-input v-model="form.medicalAdvice" type="textarea" :rows="2" placeholder="请输入医嘱/预后" />
            </el-form-item>
            <el-form-item label="检查照片" prop="checkPhotos">
               <image-upload v-model="form.checkPhotos" :limit="6" :fileSize="10" />
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
      <!-- 三级弹窗：单味药的给药方案（剂量 / 给药方式 / 时段），保存诊断时展开成用药打卡任务 -->
      <el-dialog title="给药方案" v-model="drugPlanOpen" :width="isNarrow ? '92%' : '480px'" append-to-body>
         <el-form label-width="86px">
            <el-form-item label="药品">
               <span class="drug-plan-name">{{ drugPlanRow.itemName }}</span>
            </el-form-item>
            <el-form-item label="剂量">
               <el-input v-model="drugPlanRow.dosage" placeholder="如 1/2 片、0.5ml" maxlength="50" />
            </el-form-item>
            <el-form-item label="给药方式">
               <el-select v-model="drugPlanRow.administration" style="width: 100%" placeholder="请选择">
                  <el-option v-for="d in cat_administration" :key="d.value" :label="d.label" :value="d.value" />
               </el-select>
            </el-form-item>
            <el-form-item label="给药时段">
               <el-select v-model="drugPlanRow.periods" multiple collapse-tags style="width: 100%" placeholder="至少选一个">
                  <el-option v-for="d in cat_time_period" :key="d.value" :label="d.label" :value="d.value" />
               </el-select>
            </el-form-item>
         </el-form>
         <div class="form-tip">时段 × 上面的连续天数 = 要打卡的次数，例如「早,晚」× 5 天 = 10 次。</div>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="saveDrugPlan">确 定</el-button>
            </div>
         </template>
      </el-dialog>
      <!-- 二级弹窗：从目录里挑检查项目及用药，仿「新增处方」的选药页 -->
      <el-dialog title="选择检查项目及用药" v-model="itemPickerOpen" :width="isNarrow ? '98%' : '1040px'" append-to-body :top="isNarrow ? '2vh' : '5vh'" :close-on-click-modal="false">
         <div class="picker-head">
            <el-input v-model="itemKeyword" placeholder="搜索项目或药品名称" clearable prefix-icon="Search" style="width: 240px" />
            <el-radio-group v-model="itemTypeFilter" size="small">
               <el-radio-button value="">全部</el-radio-button>
               <el-radio-button value="1">检查项目</el-radio-button>
               <el-radio-button value="2">用药</el-radio-button>
               <el-radio-button value="3">疫苗</el-radio-button>
               <el-radio-button value="4">驱虫</el-radio-button>
            </el-radio-group>
            <span class="picker-total">已选 {{ pickedItems.length }} 项，合计 {{ pickedTotal }} 元</span>
         </div>
         <div class="picker-body">
            <div class="picker-aside">
               <div class="picker-aside-title">项目分类</div>
               <el-tree
                  :data="catalogTree"
                  :props="{ label: 'itemName', children: 'children' }"
                  node-key="itemId"
                  highlight-current
                  default-expand-all
                  :expand-on-click-node="false"
                  @node-click="onPickCategory"
               />
            </div>
            <div class="picker-main">
               <div class="picker-path">
                  <span>当前：{{ currentCategoryName || '全部项目' }}</span>
                  <el-button v-if="currentCategoryId" link type="primary" @click="clearCategory">看全部</el-button>
               </div>
               <el-table :data="pickableItems" height="286" size="small" border v-loading="catalogLoading">
                  <el-table-column label="名称" prop="itemName" min-width="170" :show-overflow-tooltip="true" />
                  <el-table-column label="类型" width="90" align="center">
                     <template #default="scope">
                        <el-tag :type="itemTypeTag(scope.row.itemType)" size="small" effect="plain">{{ itemTypeLabel(scope.row.itemType) }}</el-tag>
                     </template>
                  </el-table-column>
                  <!-- 窄屏：库存/单价两列塞不下（会被固定列盖住），先收起来，库存在下方的已选明细里仍能看到 -->
                  <el-table-column v-if="!isNarrow" label="关联物资 / 库存" min-width="200" :show-overflow-tooltip="true">
                     <template #default="scope">
                        <span v-if="scope.row.materialId">{{ scope.row.materialName }}（{{ scope.row.stock }}{{ scope.row.unit || '' }}）</span>
                        <span v-else class="muted">不关联物资，不扣库存</span>
                     </template>
                  </el-table-column>
                  <el-table-column v-if="!isNarrow" label="单价" width="80" align="center">
                     <template #default="scope">{{ scope.row.price || 0 }}</template>
                  </el-table-column>
                  <!-- 窄屏下列会横向溢出，操作列固定右侧，保证「添加」始终可点 -->
                  <el-table-column label="操作" width="86" align="center" fixed="right">
                     <template #default="scope">
                        <el-button v-if="!isPicked(scope.row)" link type="primary" @click="pickItem(scope.row)">添加</el-button>
                        <el-tag v-else size="small" type="success">已添加</el-tag>
                     </template>
                  </el-table-column>
                  <template #empty>
                     <div class="muted">这个分类下没有可开单的项目，去「检查项目及用药」页面加一个吧</div>
                  </template>
               </el-table>
            </div>
         </div>
         <!-- 已选明细：数量 / 给药方案 / 用法都在这里改完，再点「加入本次诊断」 -->
         <div class="picked-area" v-if="pickedItems.length">
            <div class="picked-head">
               <span class="picked-label">已选明细（{{ pickedItems.length }}）</span>
               <span class="picked-hint">数量、给药方案、用法可以直接改；确认无误再点右下角「加入本次诊断」</span>
               <span class="picked-sum">合计 {{ pickedTotal }} 元</span>
            </div>
            <el-table :data="pickedItems" size="small" border class="picked-table" max-height="236">
               <el-table-column label="项目/药品" prop="itemName" :min-width="isNarrow ? 120 : 150" :show-overflow-tooltip="true" />
               <!-- 窄屏只留「名称 / 数量 / 给药方案 / 用法 / 删除」，类型和库存在这宽度下会把关键列挤出去 -->
               <el-table-column v-if="!isNarrow" label="类型" width="88" align="center">
                  <template #default="scope">
                     <el-tag :type="itemTypeTag(scope.row.itemType)" size="small" effect="plain">{{ itemTypeLabel(scope.row.itemType) }}</el-tag>
                  </template>
               </el-table-column>
               <el-table-column label="数量" :width="isNarrow ? 96 : 112" align="center">
                  <template #default="scope">
                     <el-input-number v-model="scope.row.quantity" size="small" controls-position="right" class="cell-input"
                        :min="isFractionUnit(scope.row.unit) ? 0.01 : 1"
                        :precision="isFractionUnit(scope.row.unit) ? 2 : 0"
                        :step="isFractionUnit(scope.row.unit) ? 0.1 : 1" />
                  </template>
               </el-table-column>
               <el-table-column v-if="!isNarrow" label="库存" width="76" align="center">
                  <template #default="scope">
                     <span v-if="scope.row.materialId" :class="{ 'short-stock': isShortStock(scope.row) }">
                        {{ scope.row.stock === null || scope.row.stock === undefined ? '—' : scope.row.stock }}
                     </span>
                     <span v-else class="muted">不扣</span>
                  </template>
               </el-table-column>
               <el-table-column label="给药方案" :width="isNarrow ? 118 : 132" align="center" class-name="plan-col">
                  <template #default="scope">
                     <el-button v-if="String(scope.row.itemType) === '2'" link type="primary"
                        :class="{ 'plan-missing': !(scope.row.periods || []).length }" @click="openDrugPlan(scope.row)">
                        {{ drugPlanText(scope.row) }}
                     </el-button>
                     <span v-else class="muted">—</span>
                  </template>
               </el-table-column>
               <el-table-column label="用法/说明" min-width="150">
                  <template #default="scope">
                     <el-input v-model="scope.row.usageNote" size="small" class="cell-input" placeholder="饭后服用" maxlength="200" />
                  </template>
               </el-table-column>
               <el-table-column label="操作" width="52" align="center" fixed="right">
                  <template #default="scope">
                     <el-button link type="danger" icon="Delete" @click="removePickedAt(scope.$index)" />
                  </template>
               </el-table-column>
            </el-table>
         </div>
         <div class="picked-empty" v-else>
            <span class="muted">还没有选择项目，点上面列表右边的「添加」加进来</span>
         </div>
         <template #footer>
            <div class="dialog-footer">
               <el-button @click="itemPickerOpen = false">取 消</el-button>
               <el-button type="primary" @click="confirmPick">加入本次诊断（{{ pickedItems.length }}）</el-button>
            </div>
         </template>
      </el-dialog>
      <!-- 更正备注：原始诊断不可修改，只能追加说明 -->
      <el-dialog title="追加更正备注" v-model="correctOpen" :width="isNarrow ? '92%' : '560px'" append-to-body>
         <el-alert type="warning" :closable="false" show-icon class="mb8"
            title="为保证病史真实，原始诊断内容不会被修改；更正内容会作为补充说明追加在记录上。" />
         <el-descriptions :column="1" border size="small" class="mb8">
            <el-descriptions-item label="就诊日期">{{ correctForm.visitDate }}</el-descriptions-item>
            <el-descriptions-item label="原诊断">{{ correctForm.diagnosisResult || '—' }}</el-descriptions-item>
            <el-descriptions-item label="既往更正">{{ correctForm.correctionNote || '—' }}</el-descriptions-item>
         </el-descriptions>
         <el-input v-model="correctForm.correctionNote" type="textarea" :rows="4" placeholder="请填写更正内容，例如：诊断结果应为「口炎」，原记录笔误" />
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitCorrect">提交更正</el-button>
               <el-button @click="correctOpen = false">取 消</el-button>
            </div>
         </template>
      </el-dialog>

   </div>
</template>

<script setup name="CatDiagnosis">
import { useCatCache, useBaseCache, useWardCache, formatCatLabel } from "@/utils/catCache"
import { getDeleteImpact } from "@/api/cat/deleteImpact"
import { deleteWithImpact } from "@/utils/deleteConfirm"
import { listCatDiagnosis, listCatDiagnosisSummary, getCatDiagnosis, delCatDiagnosis, addCatDiagnosis, updateCatDiagnosis, correctCatDiagnosis, getDiagnosisStat, listDiagnosisWardStats, getDiagnosisCatInfo, listDiagnosisDoctorOptions } from "@/api/cat/diagnosis"
import { listCatCheckItem } from "@/api/cat/checkitem"
import { useFormDraft } from "@/utils/formDraft"
import useUserStore from "@/store/modules/user"

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()
const { cat_time_period, cat_administration } = proxy.useDict('cat_time_period', 'cat_administration')
const { catLabel, reloadCatCache } = useCatCache()
const { baseMap, baseLabel, reloadBaseCache } = useBaseCache()
const { wardLabel, reloadWardCache } = useWardCache()

// 需求文档 6.2：站内诊断记录仅限于医疗基地，因此可选基地只取 base_type=2（医疗基地），
// 饲养基地不出现在可选项里。
const baseOptions = computed(() => {
  const list = Object.values(baseMap.value || {})
  return list
    .filter(item => String(item.baseType) === '2')
    .sort((a, b) => String(a.baseName || '').localeCompare(String(b.baseName || ''), 'zh'))
})

// 来源基地可以是任意基地（猫从饲养基地送来），所以这里用全量基地
const allBases = computed(() => {
  return Object.values(baseMap.value || {})
    .sort((a, b) => String(a.baseName || '').localeCompare(String(b.baseName || ''), 'zh'))
})

// 历史记录里存在早期录入在饲养基地的诊断（全部基地可选的年代）。
// 这类值不能悄悄丢掉，否则编辑时下拉显示成数字/空白；这里单独造一条禁用项用于回显原名。
const legacyBase = computed(() => {
  const id = form.value.baseId
  if (!id) {
    return null
  }
  if (baseOptions.value.some(item => item.baseId === id)) {
    return null
  }
  const base = (baseMap.value || {})[id]
  return base || { baseId: id, baseName: '#' + id }
})

function baseOptionLabel(base) {
  const typeLabel = String(base.baseType) === '2' ? '医疗基地' : '饲养基地'
  return `${base.baseName}（${typeLabel}）`
}

const list = ref([])
const open = ref(false)
// 手机/窄屏：弹窗固定 880/1040px 会在 412px 屏上横向溢出，这里按视口收窄（桌面端取值不变）
const isNarrow = ref(typeof window !== 'undefined' && window.innerWidth <= 768)
function syncNarrow() {
  isNarrow.value = window.innerWidth <= 768
}
onMounted(() => {
  syncNarrow()
  window.addEventListener('resize', syncNarrow)
})
onBeforeUnmount(() => window.removeEventListener('resize', syncNarrow))
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])
const correctOpen = ref(false)
const correctForm = ref({})
const userStore = useUserStore()
// 病历防篡改：只有内置超级管理员可以直接改/删历史诊断，其他角色只能追加更正备注
const isAdmin = computed(() => (userStore.roles || []).includes('admin'))
// 助理只记用药/疫苗/驱虫/拍照，诊断留给医生；后端也会拦，这里先把按钮藏掉
const isAssistant = computed(() => (userStore.roles || []).includes('assistant'))
const canAddDiagnosis = computed(() => !isAssistant.value || isAdmin.value || (userStore.roles || []).includes('manager'))
// 医生角色：新增诊断时「医生」一栏自动填本人、只读；超管/管理员则从医生账号下拉选
const isDoctor = computed(() => (userStore.roles || []).includes('doctor'))
// 接诊医生候选（只有非医生角色才需要）
const doctorOptions = ref([])
const doctorRequiredRule = [{ required: true, message: '请选择接诊医生', trigger: 'change' }]

function doctorOptionLabel(d) {
  return d.userName && d.userName !== d.nickName ? `${d.nickName}（${d.userName}）` : d.nickName
}

// 医生下拉换人：姓名快照跟着变，保证 doctor 与 doctor_user_id 始终一致
function onDoctorChange(userId) {
  const doc = doctorOptions.value.find(d => d.userId === userId)
  form.value.doctor = doc ? doc.nickName : undefined
}

function getDoctorOptions() {
  if (isDoctor.value) {
    return
  }
  // 每次打开弹窗都拉一次：可能刚在用户管理里新增了医生账号
  listDiagnosisDoctorOptions().then(res => {
    doctorOptions.value = res.data || []
  })
}

// ---------------- 开单：检查项目/用药目录（二级弹窗） ----------------
const itemPickerOpen = ref(false)
const itemKeyword = ref('')
const itemTypeFilter = ref('')
const catalogTree = ref([])
const catalogLeaves = ref([])
const catalogLoading = ref(false)
const currentCategoryId = ref(0)
const currentCategoryName = ref('')
// 弹窗里临时挑选的结果，点「加入本次诊断」才写回表单
const pickedItems = ref([])

const itemTypeDict = [
  { value: '1', label: '检查项目' },
  { value: '2', label: '用药' },
  { value: '3', label: '疫苗' },
  { value: '4', label: '驱虫' }
]

function itemTypeLabel(value) {
  const item = itemTypeDict.find(t => t.value === value)
  return item ? item.label : '—'
}

function itemTypeTag(value) {
  return { 1: 'primary', 2: 'warning', 3: 'success', 4: 'danger' }[value] || 'info'
}

/** 目录只有叶子能开单，分类节点（有子节点的）不展示为可选项 */
function collectLeaves(nodes, out = []) {
  (nodes || []).forEach(node => {
    const children = node.children || []
    if (children.length) {
      collectLeaves(children, out)
    } else {
      out.push(node)
    }
  })
  return out
}

function loadCatalog() {
  catalogLoading.value = true
  listCatCheckItem({ status: '0' })
    .then(res => {
      catalogTree.value = proxy.handleTree(res.data || [], 'itemId', 'parentId')
      catalogLeaves.value = collectLeaves(catalogTree.value)
    })
    .finally(() => {
      catalogLoading.value = false
    })
}

// 分类过滤靠 ancestors（顶级 ancestors 就是 "0"），点叶子节点时也能精确命中自己
const pickableItems = computed(() => {
  const keyword = (itemKeyword.value || '').trim()
  const categoryId = currentCategoryId.value
  return catalogLeaves.value.filter(item => {
    if (itemTypeFilter.value && item.itemType !== itemTypeFilter.value) return false
    if (keyword && !String(item.itemName || '').includes(keyword)) return false
    if (categoryId) {
      const ancestors = ',' + (item.ancestors || '') + ','
      if (item.itemId !== categoryId && !ancestors.includes(',' + categoryId + ',')) return false
    }
    return true
  })
})

const pickedTotal = computed(() =>
  pickedItems.value
    .reduce((sum, item) => sum + Number(item.quantity || 0) * Number(item.price || 0), 0)
    .toFixed(2)
)

// 表单里已选明细的合计
const itemTotal = computed(() =>
  (form.value.items || [])
    .reduce((sum, item) => sum + Number(item.quantity || 0) * Number(item.price || 0), 0)
    .toFixed(2)
)

function isPicked(row) {
  return pickedItems.value.some(item => item.itemId === row.itemId)
}

function pickItem(row) {
  if (isPicked(row)) return
  const isDrug = String(row.itemType) === '2'
  pickedItems.value.push({
    ...row,
    quantity: 1,
    usageNote: undefined,
    // 用药直接带默认给药方案，弹窗里就能看到「口服 · 早」，要改再点它
    dosage: undefined,
    administration: isDrug ? DRUG_DEFAULT.administration : undefined,
    periods: isDrug ? DRUG_DEFAULT.periods() : undefined
  })
}

/** 弹窗里删掉已选的一行（按位置删：同名不同 id 也不会删错） */
function removePickedAt(index) {
  pickedItems.value.splice(index, 1)
}

// 「检查项目及用药」那栏不支持手写，永远按已选明细自动拼接
function syncCheckItemsText() {
  const names = (form.value.items || []).map(item => item.itemName).join('、')
  form.value.checkItems = names || undefined
}

// 只读汇总用的两段小文案
function qtyText(row) {
  const n = Number(row.quantity || 0)
  return Number.isInteger(n) ? String(n) : String(Number(n.toFixed(2)))
}
function stockText(row) {
  if (!row.materialId) return '不扣库存'
  const stock = row.stock === null || row.stock === undefined ? '—' : row.stock
  return `库存 ${stock}`
}

function onPickCategory(node) {
  currentCategoryId.value = node.itemId
  currentCategoryName.value = node.itemName
}

function clearCategory() {
  currentCategoryId.value = 0
  currentCategoryName.value = ''
}

function openItemPicker() {
  // 把表单里已选的带进弹窗（含已经设好的给药方案），重复打开不会丢掉之前的改动
  pickedItems.value = (form.value.items || []).map(item => {
    const isDrug = String(item.itemType) === '2'
    return {
      ...item,
      quantity: Number(item.quantity || 1),
      dosage: item.dosage,
      administration: isDrug ? (item.administration || DRUG_DEFAULT.administration) : undefined,
      periods: isDrug ? [...(item.periods || DRUG_DEFAULT.periods())] : undefined
    }
  })
  itemKeyword.value = ''
  itemTypeFilter.value = ''
  currentCategoryId.value = 0
  currentCategoryName.value = ''
  loadCatalog()
  itemPickerOpen.value = true
}

function confirmPick() {
  // 用药必须有时段，否则助理那边没有打卡任务：就地拦下，指到具体哪味药
  const missing = pickedItems.value.filter(i => String(i.itemType) === '2' && !(i.periods || []).length)
  if (missing.length) {
    proxy.$modal.msgWarning(`「${missing[0].itemName}」还没设给药时段，点它那行的「给药方案」补上`)
    return
  }
  form.value.items = pickedItems.value.map(item => {
    const quantity = Number(item.quantity || 1)
    const price = Number(item.price || 0)
    const isDrug = String(item.itemType) === '2'
    return {
      itemId: item.itemId,
      itemName: item.itemName,
      itemType: item.itemType,
      materialId: item.materialId,
      unit: item.unit,
      price,
      quantity,
      usageNote: item.usageNote,
      amount: Number((quantity * price).toFixed(2)),
      stock: item.stock,
      // 给药方案只对「用药」有意义，值就存在明细行上，保存诊断时组装成处方
      dosage: isDrug ? item.dosage : undefined,
      administration: isDrug ? (item.administration || DRUG_DEFAULT.administration) : undefined,
      periods: isDrug ? [...(item.periods || [])] : undefined
    }
  })
  syncCheckItemsText()
  itemPickerOpen.value = false
}// 按重量/体积计量的物资（猫粮、猫砂之类）才需要小数，其余（次/盒/瓶/支/份…）一律整数
const FRACTION_UNITS = ['kg', 'g', 'mg', 'ml', 'l', '克', '千克', '公斤', '毫克', '毫升', '升']
function isFractionUnit(unit) {
  return FRACTION_UNITS.includes(String(unit || '').trim().toLowerCase())
}

// 开单数量超过库存时标红提示（仍然允许保存，先开单后补货的情况是存在的）
function isShortStock(row) {
  if (!row.materialId || row.stock === null || row.stock === undefined) return false
  return Number(row.stock) < Number(row.quantity || 0)
}

// 列表视图：关闭=一次就诊一行（流水），打开=一只猫一行（合并）
const mergeByCat = ref(false)
// 当前筛选范围的统计（猫数按档案当前所在病房/基地算，记录数按筛选条件算）
const stat = ref({ catCount: 0, recordCount: 0 })
// 该病房里「在册但一次都没接诊过」的猫：列表为空时用来说明「不是没数据，是还没接诊」
const pendingCats = ref([])
// 全部病房 + 每个病房当前在册猫数，供筛选与表单共用（一次拉齐，切换基地不再请求）
const wardStats = ref([])
const visitTypeHint = ref('')

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    catId: undefined,
    sourceBaseId: undefined,
    baseId: undefined,
    wardId: undefined,
    visitType: undefined,
    visitDate: undefined,
    doctor: undefined,
    symptoms: undefined,
    diagnosisResult: undefined
  },
  rules: {
    catId: [{ required: true, message: '请选择猫咪', trigger: 'change' }],
    visitDate: [{ required: true, message: '请选择就诊日期', trigger: 'change' }],
    visitType: [{ required: true, message: '请选择就诊类型（初诊/复诊）', trigger: 'change' }],
    baseId: [{ required: true, message: '请选择当前治疗基地', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)
// 表单草稿：填到一半误关闭不丢内容（需求文档 13）
const draft = useFormDraft('cat-diagnosis-form', form)

// 来源基地名称（只读展示）：从基地缓存翻译，找不到时回落到 #id
const sourceBaseName = computed(() => {
  const id = form.value.sourceBaseId
  return id ? baseLabel(id) : ''
})

// 表单里的病房下拉：只列当前所选治疗基地的病房
const formWardOptions = computed(() => {
  const baseId = form.value.baseId
  if (!baseId) {
    return []
  }
  return wardStats.value.filter(w => w.baseId === baseId)
})

// 筛选区的病房下拉：选了治疗基地就只列该基地的；没选就按基地分组全列
const wardGroups = computed(() => {
  const baseFilter = queryParams.value.baseId
  const groups = []
  const map = new Map()
  wardStats.value.forEach(w => {
    if (baseFilter && w.baseId !== baseFilter) return
    const key = w.baseId
    if (!map.has(key)) {
      const group = { baseId: key, baseName: w.baseName || '未归属基地', wards: [] }
      map.set(key, group)
      groups.push(group)
    }
    map.get(key).wards.push(w)
  })
  return groups
})

function wardOptionLabel(ward) {
  return `${ward.wardName}（${ward.catCount}）`
}

// 统计条：主句只放两个数字，口径说明单独一行小字，避免一句话太长没人读
const statTip = computed(() => {
  const cats = stat.value.catCount || 0
  const records = stat.value.recordCount || 0
  const w = queryParams.value.wardId
  const b = queryParams.value.baseId
  if (w === 0) {
    return `「未填病房」共 ${records} 条诊断记录`
  }
  if (w) {
    return `「${wardLabel(w)}」当前 ${cats} 只猫 · 诊断记录 ${records} 条`
  }
  if (b) {
    return `「${baseLabel(b)}」当前 ${cats} 只猫 · 诊断记录 ${records} 条`
  }
  return `当前 ${cats} 只猫 · 诊断记录 ${records} 条`
})

const statNote = computed(() => {
  if (queryParams.value.wardId === 0) {
    return '早期记录没填病房，这里只按记录统计，不计入具体病房的猫数'
  }
  return '猫数按猫咪此刻所在的病房统计（已放归 / 已领养 / 已离世不计）；记录数按就诊时的病房统计。'
})

// 空状态里指名道姓说清是哪个范围（病房 / 基地）
const emptyScopeLabel = computed(() => {
  const w = queryParams.value.wardId
  const b = queryParams.value.baseId
  if (w) {
    return `「${wardLabel(w)}」`
  }
  if (b) {
    return `「${baseLabel(b)}」`
  }
  return '当前范围'
})

// 合并视图里每只猫的完整病史：展开时才拉，拉过就缓存
const historyMap = reactive({})
const historyLoading = reactive({})

function historyList(catId) {
  return historyMap[catId] || []
}

function historyLoaded(catId) {
  return historyMap[catId] !== undefined
}

function onExpandChange(row, expandedRows) {
  const expanded = Array.isArray(expandedRows)
    ? expandedRows.some(item => String(item.catId) === String(row.catId))
    : !!expandedRows
  if (!expanded || historyLoaded(row.catId)) {
    return
  }
  loadHistory(row.catId)
}

function loadHistory(catId) {
  historyLoading[catId] = true
  listCatDiagnosis({ catId, pageNum: 1, pageSize: 200 })
    .then(res => {
      historyMap[catId] = res.rows || []
    })
    .finally(() => {
      historyLoading[catId] = false
    })
}

function toggleHistory(row) {
  const table = proxy.$refs.historyTable
  if (table) {
    table.toggleRowExpansion(row)
    return
  }
  // 没有表格 ref 时退化为直接加载，行为上不影响使用
  if (!historyLoaded(row.catId)) {
    loadHistory(row.catId)
  }
}

function visitTypeLabel(type) {
  if (type === '1') return '初诊'
  if (type === '2') return '复诊'
  return '未标注'
}

function visitTypeTag(type) {
  if (type === '1') return 'warning'
  if (type === '2') return 'info'
  return 'info'
}

// 把「未填病房」伪选项换成后端能识别的 wardUnassigned，其余条件原样传
function buildParams() {
  const params = proxy.addDateRange({ ...queryParams.value }, dateRange.value)
  if (params.wardId === 0) {
    delete params.wardId
    params.wardUnassigned = true
  }
  return params
}

function getList() {
  loading.value = true
  const params = buildParams()
  const request = mergeByCat.value ? listCatDiagnosisSummary(params) : listCatDiagnosis(params)
  request
    .then(response => {
      list.value = response.rows || []
      total.value = response.total || 0
    })
    .finally(() => {
      loading.value = false
    })
  getStat(params)
}

function getStat(params) {
  getDiagnosisStat(params).then(res => {
    const data = res.data || {}
    stat.value = { catCount: data.catCount || 0, recordCount: data.recordCount || 0 }
    pendingCats.value = data.pendingCats || []
  })
}

// 病房下拉的猫数：一次拉全部病房，切换基地时前端分组过滤即可
function getWardStats() {
  listDiagnosisWardStats({}).then(res => {
    wardStats.value = res.data || []
  })
}

function onViewChange() {
  queryParams.value.pageNum = 1
  // 视图切换后旧的展开缓存没意义了，清掉避免看到上一轮的病史
  Object.keys(historyMap).forEach(key => delete historyMap[key])
  getList()
}

function onBaseFilterChange() {
  // 换了治疗基地，原来的病房肯定不属于新基地，直接清掉
  queryParams.value.wardId = undefined
  handleQuery()
}

function cancel() {
  draft.pause()
  open.value = false
  reset()
}

function reset() {
  form.value = {
    catId: undefined,
    baseId: undefined,
    sourceBaseId: undefined,
    wardId: undefined,
    visitDate: undefined,
    visitType: undefined,
    // 医生本人录入时直接锁定为当前登录账号；其他角色留空必须选
    doctor: isDoctor.value ? userStore.nickName : undefined,
    doctorUserId: isDoctor.value ? Number(userStore.id) : undefined,
    symptoms: undefined,
    diagnosisResult: undefined,
    checkItems: undefined,
    medicalAdvice: undefined,
    checkPhotos: undefined,
    // 开单明细：从「选择项目/用药」弹窗选出来的（用药行的给药方案也存在这里）
    items: [],
    // 保存时由用药行自动组装，不再由医生单独填
    prescription: undefined,
    remark: undefined
  }
  rxPlan.startDate = todayStr()
  rxPlan.days = 1
  drugPlanOpen.value = false
  drugPlanTarget = null
  visitTypeHint.value = ''
  proxy.clearFormValidate("formRef")
}

// 选中猫咪：来源基地自动带出，就诊类型按「有没有看过病」预选，基地/病房尽量一起带上
function onCatChange(catId) {
  if (!catId) {
    form.value.sourceBaseId = undefined
    visitTypeHint.value = ''
    return
  }
  getDiagnosisCatInfo(catId).then(res => {
    const hint = res.data || {}
    form.value.sourceBaseId = hint.sourceBaseId || undefined
    form.value.visitType = hint.defaultVisitType || '1'
    visitTypeHint.value = hint.hasHistory
      ? `该猫在本站已就诊 ${hint.visitCount} 次，已默认选择「复诊」`
      : '该猫在本站还没有诊断记录，已默认选择「初诊」'
    // 治疗基地只能选医疗基地：猫咪当前就在医疗基地时直接带出，省一步选择
    if (!form.value.baseId && hint.baseId && baseOptions.value.some(b => b.baseId === hint.baseId)) {
      form.value.baseId = hint.baseId
    }
    // 病房必须属于所选治疗基地，对不上就留空让医生选
    const ward = hint.wardId ? wardStats.value.find(w => w.wardId === hint.wardId) : null
    form.value.wardId = ward && form.value.baseId && ward.baseId === form.value.baseId ? ward.wardId : undefined
  })
}

function onFormBaseChange() {
  // 治疗基地变了，原来选的病房大概率不属于新基地，清掉重新选
  const ward = form.value.wardId ? wardStats.value.find(w => w.wardId === form.value.wardId) : null
  if (!ward || ward.baseId !== form.value.baseId) {
    form.value.wardId = undefined
  }
}

// 打开更正弹窗
function handleCorrect(row) {
  correctForm.value = { ...row, correctionNote: '' }
  correctOpen.value = true
}

function submitCorrect() {
  if (!correctForm.value.correctionNote) {
    proxy.$modal.msgWarning('请填写更正内容')
    return
  }
  correctCatDiagnosis({
    diagnosisId: correctForm.value.diagnosisId,
    correctionNote: correctForm.value.correctionNote
  }).then(res => {
    proxy.$modal.msgSuccess(res.msg || '已追加更正备注')
    correctOpen.value = false
    dropHistory(correctForm.value.catId)
    getList()
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.diagnosisId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() {
  // 主数据（基地/病房/猫咪）可能刚在别的页签新增过，打开弹窗前强制拉一次，
  // 否则下拉里看不到新记录，用户会以为刚才没保存成功
  reloadBaseCache()
  reloadCatCache()
  reloadWardCache()
  getWardStats()
  getDoctorOptions()
  reset()
  const savedDraft = draft.restore()
  if (savedDraft) {
    form.value = { ...savedDraft }
    // 草稿可能是别的账号（或别的角色）留下的医生名，医生本人登录必须用本人身份
    if (isDoctor.value) {
      form.value.doctor = userStore.nickName
      form.value.doctorUserId = Number(userStore.id)
    }
  }
  draft.begin()
  open.value = true
  title.value = "添加站内诊断"
}

// 从空状态里点「接诊」：直接带着这只猫和它所在的基地/病房打开新增弹窗，省一遍选择
function handleAddForCat(cat) {
  handleAdd()
  // 草稿可能停在别的猫上；接诊针对的是一只确定的猫，直接覆盖
  form.value.catId = cat.catId
  form.value.baseId = cat.baseId || queryParams.value.baseId
  form.value.wardId = cat.wardId || queryParams.value.wardId
  form.value.visitDate = proxy.parseTime(new Date(), '{y}-{m}-{d}')
  // 带出来源基地，并按「有没有看过病」预选初诊/复诊
  onCatChange(cat.catId)
}

function handleUpdate(row) {
  reloadBaseCache()
  reloadCatCache()
  reloadWardCache()
  getWardStats()
  getDoctorOptions()
  reset()
  const id = row.diagnosisId || ids.value
  getCatDiagnosis(id).then(response => {
    form.value = response.data
    // 历史记录可能没有明细（老数据只有一段检查项目文字），统一成数组避免表格报错
    form.value.items = response.data.items || []
    // 已有处方回填到用药行上（剂量/给药方式/时段），医生在同一个表格里改
    rxPlan.startDate = response.data.visitDate || todayStr()
    rxPlan.days = 1
    applyPrescriptionToItems(response.data.prescription)
    visitTypeHint.value = form.value.visitType ? '' : '这条记录还没有就诊类型，请补上'
    draft.begin('edit')
    open.value = true
    title.value = "修改站内诊断"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      // 「检查项目及用药」只能从目录里选，不支持手写
      const hasItems = (form.value.items || []).length > 0
      if (form.value.diagnosisId == undefined) {
        if (!hasItems) {
          proxy.$modal.msgWarning('请点「选择检查项目及用药」从目录中选择，该栏不支持手写')
          return
        }
      } else if (!hasItems && !form.value.checkItems) {
        // 修改老记录时，原来那段手写文字允许保留；但不能把它清空后保存
        proxy.$modal.msgWarning('检查项目及用药不能为空，请从目录重新选择')
        return
      }
      // 开了用药就必须有给药方案：没有时段，助理那边就没有打卡任务
      const drugs = drugRows.value
      if (drugs.length) {
        const missing = drugs.filter(d => !(d.periods || []).length)
        if (missing.length) {
          proxy.$modal.msgWarning(`「${missing.map(d => d.itemName).join('、')}」还没设给药时段，请点「给药方案」设置`)
          return
        }
        if (!rxPlan.startDate) {
          proxy.$modal.msgWarning('请选择给药的开始日期')
          return
        }
        if (!rxPlan.days || rxPlan.days < 1) {
          proxy.$modal.msgWarning('给药天数至少 1 天')
          return
        }
      }
      // 把用药行的给药方案组装成处方，随诊断一起提交（后端据此展开成用药打卡任务）
      syncPrescription()
      if (form.value.diagnosisId != undefined) {
        updateCatDiagnosis(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          draft.pause()
          open.value = false
          dropHistory(form.value.catId)
          getList()
          askPrintCard(form.value.catId)
        })
      } else {
        addCatDiagnosis(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          draft.clear()
          draft.pause()
          open.value = false
          dropHistory(form.value.catId)
          getList()
          askPrintCard(form.value.catId)
        })
      }
    }
  })
}

// 这只猫刚改过病历，合并视图里缓存的病史要丢掉，展开时重新拉
function dropHistory(catId) {
  if (catId !== undefined && catId !== null) {
    delete historyMap[catId]
  }
}

/**
 * 打开这只猫的信息卡：只跳转定位，不自动调起打印，打不打印由用户自己点。
 * 卡片挂在笼子上，诊断完顺手打一张带走，不用再回猫咪档案里找。
 */
function printCard(catId) {
  if (!catId) return
  router.push({ path: '/profile/qrcard', query: { catIds: catId } })
}

/** 诊断保存成功后问一句：要不要现在打开信息卡页面 */
function askPrintCard(catId) {
  if (!catId) return
  proxy.$modal.confirm(
    '诊断已保存。是否现在打开这只猫的信息卡页面？（进去后自己点打印）',
    '信息卡',
    { confirmButtonText: '打开信息卡', cancelButtonText: '暂不打开', type: 'success' }
  ).then(() => printCard(catId)).catch(() => {})
}

function handleDelete(row) {
  // 表格行删除传 row，批量删除走勾选的 ids
  const delIds = row && row.diagnosisId ? [row.diagnosisId] : ids.value.slice()
  if (!delIds.length) {
    return
  }
  deleteWithImpact(proxy, {
    fetchImpact: () => getDeleteImpact('diagnosis', delIds),
    entityLabel: '诊断单',
    cascade: true,
    deleteFn: () => delCatDiagnosis(delIds),
    normalTip: names => `确认删除 ${names} ？删除后不可恢复。`,
    afterDelete: () => {
      if (row && row.catId) {
        dropHistory(row.catId)
      }
      getList()
      proxy.$modal.msgSuccess("删除成功")
    }
  })
}

function handleExport() {
  proxy.download("cat/diagnosis/export", { ...buildParams() }, `diagnosis_${new Date().getTime()}.xlsx`)
}

// 扫码页「+ 快速记一笔」带 add=1&catId=xx 进来：直接打开新增，猫咪已填好
function openQuickEntry() {
  const q = route.query || {}
  if (String(q.add) !== '1' || !q.catId) return
  handleAdd()
  form.value.catId = Number(q.catId)
  router.replace({ path: route.path, query: {} })
}

function todayStr() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// ---------------- 给药计划（已并进「检查项目及用药」） ----------------
// 用药行的给药方案（剂量/给药方式/时段）就存在明细行上，保存诊断时统一组装成 prescription 提交；
// 不再有单独的「用药处方」弹窗，医生只在一个地方选药 + 设频次。
const rxPlan = reactive({ startDate: todayStr(), days: 1 })
const drugRows = computed(() => (form.value.items || []).filter(i => String(i.itemType) === '2'))
const drugPlanOpen = ref(false)
const drugPlanRow = reactive({ itemName: '', dosage: undefined, administration: '口服', periods: [] })
let drugPlanTarget = null

// 「检查项目及用药」那一栏：有明细就列出药名，只有历史手写内容时原样显示那段文字
const itemPickerText = computed(() => {
  const items = form.value.items || []
  if (items.length) return items.map(i => i.itemName).filter(Boolean).join('、')
  return (form.value.checkItems || '').trim()
})
const isLegacyItems = computed(() => !(form.value.items || []).length && !!(form.value.checkItems || '').trim())

// 明细行上的默认值：新选的用药直接给「口服 + 早」，医生不用每次都点开设置
const DRUG_DEFAULT = { administration: '口服', periods: () => ['早'] }

function defaultDrugPlan() {
  return { dosage: undefined, administration: DRUG_DEFAULT.administration, periods: DRUG_DEFAULT.periods() }
}

function openDrugPlan(row) {
  drugPlanTarget = row
  drugPlanRow.itemName = row.itemName
  drugPlanRow.dosage = row.dosage
  drugPlanRow.administration = row.administration || DRUG_DEFAULT.administration
  drugPlanRow.periods = [...(row.periods || [])]
  drugPlanOpen.value = true
}

function saveDrugPlan() {
  if (!drugPlanTarget) {
    drugPlanOpen.value = false
    return
  }
  if (!drugPlanRow.periods.length) {
    proxy.$modal.msgWarning('请至少选一个给药时段，否则助理那边没有打卡任务')
    return
  }
  Object.assign(drugPlanTarget, {
    dosage: drugPlanRow.dosage,
    administration: drugPlanRow.administration,
    periods: [...drugPlanRow.periods]
  })
  drugPlanOpen.value = false
}

/** 表格里显示的方案摘要：没有时段时标红提示去设置 */
function drugPlanText(row) {
  const periods = (row.periods || []).join(',')
  if (!periods) {
    return '待设置时段'
  }
  const parts = []
  if (row.dosage) parts.push(row.dosage)
  if (row.administration) parts.push(row.administration)
  parts.push(periods)
  return parts.join(' · ')
}

/** 打开已有诊断时，把后端返回的处方回填到对应的用药行上 */
function applyPrescriptionToItems(p) {
  if (!p || !p.items || !p.items.length) {
    return
  }
  rxPlan.startDate = p.startDate || form.value.visitDate || todayStr()
  rxPlan.days = p.days || 1
  const rows = form.value.items || []
  const used = new Set()
  p.items.forEach(pi => {
    const row = rows.find((it, idx) =>
      String(it.itemType) === '2' && it.itemName === pi.drugName && !used.has(idx))
    if (!row) {
      return
    }
    used.add(rows.indexOf(row))
    const periods = Array.isArray(pi.periods) && pi.periods.length
      ? [...pi.periods]
      : String(pi.timePeriods || '').split(',').map(s => s.trim()).filter(Boolean)
    Object.assign(row, {
      dosage: pi.dosage,
      administration: pi.administration || DRUG_DEFAULT.administration,
      periods
    })
  })
}

/**
 * 保存前把用药行的给药方案组装成 prescription：
 * 没有用药行 → 不带处方（不动原来的处方）；
 * 有用药行 → 保留原 prescriptionId（后端按「改方留痕」更新，只从今天起生效）。
 */
function syncPrescription() {
  // 只把设了时段的用药带进处方：没时段的药不生成打卡任务（校验已挡住，这里再兜一层）
  const drugs = drugRows.value.filter(d => (d.periods || []).length)
  if (!drugs.length) {
    form.value.prescription = undefined
    return
  }
  const old = form.value.prescription || {}
  form.value.prescription = {
    prescriptionId: old.prescriptionId,
    catId: form.value.catId,
    diagnosisId: form.value.diagnosisId,
    doctor: old.doctor,
    doctorUserId: old.doctorUserId,
    startDate: rxPlan.startDate || form.value.visitDate || todayStr(),
    days: rxPlan.days || 1,
    remark: old.remark,
    items: drugs.map(d => ({
      drugName: d.itemName,
      dosage: d.dosage,
      administration: d.administration || DRUG_DEFAULT.administration,
      timePeriods: (d.periods || []).join(','),
      usageNote: d.usageNote
    }))
  }
}

// ⚠️ 这两行必须留在脚本最末尾：openQuickEntry → handleAdd → reset 会读写下面才用 const 声明的
// rxPlan / drugPlanOpen / drugPlanTarget，放前面会在 setup 阶段撞「暂时性死区」（Cannot access
// 'rxPlan' before initialization），整个页面直接白屏。
openQuickEntry()
watch(() => route.query, () => openQuickEntry())

getList()
getWardStats()
</script>

<style scoped>
.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.toolbar-left {
  display: flex;
  align-items: center;
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.switch-label {
  font-size: 13px;
  color: var(--el-text-color-regular);
}
/* 手机上工具栏分两行：按钮一行，合并开关一行，避免「按猫合并」被挤成竖排 */
@media screen and (max-width: 768px) {
  .table-toolbar {
    flex-direction: column;
    align-items: stretch;
    row-gap: 8px;
  }
  .toolbar-left {
    flex-wrap: wrap;
    gap: 6px;
  }
  .toolbar-left .el-button + .el-button {
    margin-left: 0;
  }
  .toolbar-right {
    justify-content: flex-end;
    flex-wrap: nowrap;
  }
  .switch-label {
    white-space: nowrap;
  }
}
.muted {
  color: var(--el-text-color-placeholder);
}
/* ---- 统计条：主句 + 小字口径说明 ---- */
.scope-main {
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.scope-note {
  display: block;
  margin-top: 2px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
/* ---- 空状态：说清「是还没接诊」而不是「数据没了」 ---- */
.diag-empty {
  padding: 18px 12px;
  line-height: 1.6;
}
.diag-empty-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-regular);
}
.diag-empty-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.diag-empty-cats {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 6px 18px;
  margin-top: 10px;
}
.diag-empty-cat {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--el-text-color-primary);
}
.form-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 18px;
  margin-top: 2px;
}
.form-tip.warn {
  color: var(--el-color-warning);
}
.history-panel {
  padding: 8px 16px 0 48px;
  background: var(--el-fill-color-lighter);
}
.history-item {
  padding-bottom: 4px;
}
.history-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}
.history-doctor {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.history-line {
  font-size: 13px;
  line-height: 20px;
  color: var(--el-text-color-regular);
}
.history-line.correction {
  color: var(--el-color-warning);
}

/* ---- 开单：检查项目/用药 ---- */
/* 整栏做成一个「假输入框」，点哪儿都能打开目录，样式跟 el-input 对齐 */
.item-picker {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  min-height: 32px;
  padding: 0 11px;
  box-sizing: border-box;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  background: var(--el-fill-color-blank);
  cursor: pointer;
  transition: border-color 0.2s;
  line-height: 1.4;
}
.item-picker:hover {
  border-color: var(--el-color-primary);
}
.item-picker:hover .item-picker-action {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.item-picker-icon {
  flex: 0 0 auto;
  font-size: 15px;
  color: var(--el-color-primary);
}
.item-picker-text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  color: var(--el-text-color-regular);
}
.item-picker-placeholder {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  color: var(--el-text-color-placeholder);
}
/* 老数据只有一段手写文字，跟可选的明细区分开 */
.item-picker.is-legacy .item-picker-text {
  color: var(--el-text-color-secondary);
  font-style: italic;
}
.item-picker-action {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 0 8px;
  height: 22px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  font-size: 12px;
  color: var(--el-text-color-regular);
  background: var(--el-fill-color-lighter);
  transition: all 0.2s;
}
.picked-table {
  margin-top: 8px;
}
/* 单元格左右内边距收窄到 8px：7 列合计能多出约 28px 给内容 */
.picked-table :deep(.cell) {
  padding-left: 8px;
  padding-right: 8px;
}
/* 表格内的输入框一律占满单元格，不能再写死像素宽——写死超出列宽就会被裁成「1 ⋯」 */
.picked-table :deep(.cell-input) {
  width: 100%;
}
/* 给药方案不换行，「口服 · 早,中,晚」也保持一行 */
.picked-table :deep(.plan-col .cell) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.short-stock {
  color: var(--el-color-danger);
  font-weight: 600;
}
/* 给药计划条：只在选了「用药」时出现，和上面的开单表贴在一起 */
.rx-plan {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
  padding: 6px 10px;
  border-radius: 4px;
  background: var(--el-fill-color-lighter);
}
.rx-plan-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-regular);
  margin-right: 4px;
}
.rx-plan-sep {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
/* 用药行还没设时段时，按警告色显示，提醒去点开设置 */
.plan-missing {
  color: var(--el-color-danger) !important;
}
.drug-plan-name {
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.picker-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}
.picker-total {
  margin-left: auto;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.picker-body {
  display: flex;
  gap: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 10px;
}
.picker-aside {
  width: 220px;
  flex: 0 0 220px;
  border-right: 1px solid var(--el-border-color-lighter);
  padding-right: 10px;
  max-height: 286px;
  overflow-y: auto;
}
.picker-aside-title {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}
.picker-main {
  flex: 1;
  min-width: 0;
}
.picker-path {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--el-text-color-regular);
  margin-bottom: 6px;
}
/* ---- 选药弹窗底部的已选明细（可编辑） ---- */
.picked-area {
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px dashed var(--el-border-color);
}
.picked-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.picked-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-regular);
  flex: 0 0 auto;
}
.picked-hint {
  flex: 1;
  min-width: 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.picked-sum {
  flex: 0 0 auto;
  font-size: 13px;
  font-weight: 600;
  color: var(--el-color-danger);
}
.picked-empty {
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px dashed var(--el-border-color);
  font-size: 12px;
}

/* ---- 主表单：已选明细只读汇总（改东西统一进弹窗） ---- */
.item-summary {
  list-style: none;
  margin: 8px 0 0;
  padding: 0;
  width: 100%;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  overflow: hidden;
}
.item-summary-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 6px 10px;
  font-size: 13px;
  line-height: 20px;
  color: var(--el-text-color-regular);
}
.item-summary-row + .item-summary-row {
  border-top: 1px solid var(--el-border-color-lighter);
}
.item-summary-row:nth-child(odd) {
  background: var(--el-fill-color-lighter);
}
.item-summary-type {
  flex: 0 0 auto;
}
.item-summary-name {
  font-weight: 600;
  color: var(--el-text-color-primary);
  min-width: 0;
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-summary-meta {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}
.item-summary-note {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color);
  border-radius: 3px;
  padding: 0 6px;
  max-width: 240px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ---------- 手机/窄屏（扫码后直接开诊断的场景） ---------- */
/* 弹窗宽度已在模板里按 isNarrow 收窄，这里只处理内部横向挤压 */
@media (max-width: 768px) {
  .picker-head {
    flex-wrap: wrap;
    gap: 8px;
  }
  /* 搜索框占满一行，类型按钮组另起一行；已选合计不再靠右挤 */
  .picker-head .el-input {
    width: 100% !important;
  }
  .picker-total {
    margin-left: 0;
  }
  /* 分类树改成上方横条，下面给表格留出宽度 */
  .picker-body {
    flex-direction: column;
    gap: 8px;
  }
  .picker-aside {
    width: 100%;
    flex: 0 0 auto;
    max-height: 108px;
    padding-right: 0;
    border-right: 0;
    border-bottom: 1px solid var(--el-border-color-lighter);
    padding-bottom: 6px;
  }
  /* 已选明细在手机上不再撑一屏，行内信息允许换行 */
  .item-summary-row {
    padding: 8px 10px;
  }
  .item-summary-name {
    max-width: 100%;
  }
  .item-summary-note {
    max-width: 100%;
  }
  .rx-plan {
    flex-wrap: wrap;
    row-gap: 6px;
  }
}
</style>
