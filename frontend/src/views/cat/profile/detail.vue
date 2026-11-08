<template>
  <div class="app-container archive-detail" v-loading="loading">
    <!-- 顶部档案头 -->
    <div class="archive-header">
      <div class="header-main">
        <div class="avatar" :class="{ 'has-img': !!avatarSrc }">
          <img v-if="avatarSrc" :src="avatarSrc" alt="" @error="avatarBroken = true" />
          <span v-else>{{ (profile.catName || '猫').charAt(0) }}</span>
        </div>
        <div class="header-info">
          <div class="name-row">
            <span class="cat-name">{{ profile.catName || '未命名' }}</span>
            <el-tag v-if="profile.alias" size="small" type="info" effect="plain">别名 {{ profile.alias }}</el-tag>
            <dict-tag :options="cat_status" :value="profile.status" />
            <el-tag v-if="profile.archived === '1'" size="small" type="warning" effect="dark">已归档</el-tag>
          </div>
          <div class="meta-row">
            <span class="meta">档案编号：{{ profile.archiveNo || '—' }}</span>
            <span class="meta">所在位置：{{ locationText }}</span>
            <span class="meta" v-if="profile.hospitalName">外送医院：{{ profile.hospitalName }}</span>
          </div>
          <div class="tag-row" v-if="tagNames.length">
            <el-tag v-for="t in tagNames" :key="t" size="small" type="danger" effect="light" class="tag-item">{{ t }}</el-tag>
          </div>
        </div>
      </div>
      <div class="header-actions">
        <el-button icon="Back" @click="goBack">返回列表</el-button>
        <!-- 手机扫码进来第一件事通常是改位置：护士把猫放进笼子顺手点一下 -->
        <el-button type="primary" plain icon="Location" @click="openQuickMove" v-hasPermi="['cat:profile:edit']">改位置</el-button>
        <el-button type="primary" plain icon="Document" @click="handleExportPdf">档案PDF</el-button>
        <el-button type="success" plain icon="Printer" @click="goPrint">打印信息卡</el-button>
        <el-button v-if="profile.archived !== '1'" type="warning" plain icon="FolderChecked" @click="handleArchive">归档</el-button>
        <el-button v-else type="info" plain icon="RefreshLeft" @click="handleUnarchive">取消归档</el-button>
      </div>
    </div>

    <el-row :gutter="16">
      <!-- 左：基础信息 + 流转轨迹 -->
      <el-col :xs="24" :md="15">
        <el-card shadow="never" class="block">
          <template #header>
            <div class="card-header">
              <span>基础档案</span>
              <el-button link type="primary" icon="Edit" @click="goEdit" v-hasPermi="['cat:profile:edit']">编辑档案</el-button>
            </div>
          </template>
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="品种">{{ profile.breed || '—' }}</el-descriptions-item>
            <el-descriptions-item label="性别"><dict-tag :options="cat_sex" :value="profile.sex" /></el-descriptions-item>
            <!-- 年龄按出生日期实时算，每次打开页面都是当前年龄 -->
            <el-descriptions-item label="年龄">{{ profile.ageText || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="出生日期">{{ birthText }}</el-descriptions-item>
            <el-descriptions-item label="体重">{{ profile.weight || '—' }}</el-descriptions-item>
            <el-descriptions-item label="来源"><dict-tag :options="cat_source" :value="profile.source" /></el-descriptions-item>
            <el-descriptions-item label="抓捕日期">{{ profile.captureDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="是否绝育"><dict-tag :options="cat_neutered" :value="profile.neutered" /></el-descriptions-item>
            <el-descriptions-item label="是否驱虫"><dict-tag :options="cat_yes_no" :value="profile.dewormed" /></el-descriptions-item>
            <el-descriptions-item label="是否疫苗"><dict-tag :options="cat_yes_no" :value="profile.vaccinated" /></el-descriptions-item>
            <el-descriptions-item label="抓捕/投喂点位" :span="3">{{ profile.capturePoint || '—' }}</el-descriptions-item>
            <el-descriptions-item label="标志性特征" :span="3">{{ profile.feature || '—' }}</el-descriptions-item>
            <el-descriptions-item label="性格/备注" :span="3">{{ [profile.personality, profile.remark].filter(Boolean).join('；') || '—' }}</el-descriptions-item>
            <el-descriptions-item label="登记人">{{ profile.creatorName || profile.createBy || '—' }}</el-descriptions-item>
            <el-descriptions-item label="建档时间" :span="2">{{ profile.createTime || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 相册：详情页里直接上传、编辑、删除照片视频，不用跳去相册管理页 -->
        <el-card shadow="never" class="block">
          <template #header>
            <div class="card-header">
              <span>照片相册（{{ photos.length }}）</span>
              <div class="card-actions">
                <el-button link type="primary" icon="Plus" @click="handlePhotoAdd" v-hasPermi="['cat:photo:add']">上传照片/视频</el-button>
                <el-button link type="primary" icon="Picture" @click="goPhoto">管理相册</el-button>
              </div>
            </div>
          </template>
          <el-empty v-if="!photos.length" description="暂无照片，点右上角「上传照片/视频」直接添加" :image-size="60" />
          <div v-else class="photo-grid">
            <div class="photo-item" v-for="p in photos" :key="p.photoId">
              <el-image
                v-if="p.fileType !== '2'"
                :src="resolveUrl(p.thumbUrl || p.fileUrl)"
                :preview-src-list="imagePreviewList"
                :initial-index="imagePreviewList.indexOf(resolveUrl(p.fileUrl))"
                fit="cover"
                preview-teleported
                class="photo"
              />
              <div v-else class="photo video" @click="openVideo(p)">
                <el-image v-if="p.thumbUrl" :src="resolveUrl(p.thumbUrl)" fit="cover" class="photo" />
                <el-icon class="play-icon"><VideoPlay /></el-icon>
              </div>
              <div class="photo-folder">{{ p.folder || '未分类' }}</div>
              <div class="photo-ops">
                <el-button link type="primary" @click.stop="handlePhotoEdit(p)" v-hasPermi="['cat:photo:edit']">编辑</el-button>
                <el-button link type="danger" @click.stop="handlePhotoDelete(p)" v-hasPermi="['cat:photo:remove']">删除</el-button>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 流转轨迹 -->
        <el-card shadow="never" class="block">
          <template #header>
            <div class="card-header">
              <span>流转轨迹（{{ flows.length }}）</span>
              <span class="form-tip">点某次流转的「看此前病历」，右侧时间线只看这次流转之前的记录</span>
            </div>
          </template>
          <el-empty v-if="!flows.length" description="暂无流转记录" :image-size="60" />
          <el-table v-else :data="flows" size="small" border>
            <el-table-column label="序号" type="index" :index="i => $seqIndex(i)" width="56" align="center" />
            <el-table-column label="时间" width="160" align="center">
              <template #default="scope">{{ scope.row.operateTime || '—' }}</template>
            </el-table-column>
            <el-table-column label="流转类型" width="110" align="center">
              <template #default="scope">
                <el-tag size="small" effect="plain">{{ scope.row.flowType || '—' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="转出 → 转入" min-width="180">
              <template #default="scope">
                {{ baseName(scope.row.fromBaseId) || '—' }} → {{ baseName(scope.row.toBaseId) || '—' }}
              </template>
            </el-table-column>
            <el-table-column label="原因 / 医院结论" min-width="180" :show-overflow-tooltip="true">
              <template #default="scope">
                {{ [scope.row.flowReason, scope.row.hospitalName, scope.row.hospitalResult].filter(Boolean).join(' / ') || '—' }}
              </template>
            </el-table-column>
            <el-table-column label="操作人" width="100" align="center" prop="operateBy" />
            <el-table-column label="操作" width="110" align="center">
              <template #default="scope">
                <el-button link type="primary" @click="filterByFlow(scope.row)">看此前病历</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 免疫与驱虫：先给概览（该不该补针一眼判断），再按时间点列出第几针 / 第几次 -->
        <el-card shadow="never" class="block">
          <template #header>
            <div class="card-header">
              <span>免疫与驱虫</span>
              <span class="form-tip" style="margin: 0">
                疫苗 {{ vaccineSummary.length }} 针 / 驱虫 {{ dewormSummary.length }} 次
              </span>
            </div>
          </template>

          <!-- 免疫概览：基础免疫进度、狂犬、加强针、上次体内外驱虫 -->
          <div class="imm-overview">
            <div class="imm-box">
              <div class="imm-label">基础免疫</div>
              <div class="imm-value">
                <b>{{ immuneOverview.baseDoseDone }}</b><span class="imm-total">/ {{ immuneOverview.baseDoseTotal }} 针</span>
              </div>
              <div class="imm-sub" :class="leftClass(immuneOverview.baseDoseNextDaysLeft)">
                {{ immuneOverview.baseDoseDone >= immuneOverview.baseDoseTotal
                    ? '三针已打完'
                    : (immuneOverview.baseDoseNextDate ? `下一针 ${immuneOverview.baseDoseNextDate}（${leftText(immuneOverview.baseDoseNextDaysLeft)}）` : '待接种下一针') }}
              </div>
            </div>

            <div class="imm-box">
              <div class="imm-label">狂犬</div>
              <div class="imm-value">{{ immuneOverview.rabiesNextDate || '未接种' }}</div>
              <div class="imm-sub" :class="leftClass(immuneOverview.rabiesDaysLeft)">
                {{ immuneOverview.rabiesNextDate
                    ? `上次 ${immuneOverview.rabiesLastDate || '—'} · ${leftText(immuneOverview.rabiesDaysLeft)}`
                    : '还没有狂犬接种记录' }}
              </div>
            </div>

            <div class="imm-box">
              <div class="imm-label">加强针</div>
              <div class="imm-value">{{ immuneOverview.boosterNextDate || '未接种' }}</div>
              <div class="imm-sub" :class="leftClass(immuneOverview.boosterDaysLeft)">
                {{ immuneOverview.boosterNextDate
                    ? `上次 ${immuneOverview.boosterLastDate || '—'} · ${leftText(immuneOverview.boosterDaysLeft)}`
                    : '每年打一次加强针，到期会提醒' }}
              </div>
            </div>

            <div class="imm-box">
              <div class="imm-label">上次体内驱虫</div>
              <div class="imm-value">{{ immuneOverview.lastInDate || '—' }}</div>
              <div class="imm-sub" :class="leftClass(immuneOverview.lastInDaysLeft)">
                {{ immuneOverview.lastInDate
                    ? `下次 ${immuneOverview.lastInNextDate || '—'} · ${leftText(immuneOverview.lastInDaysLeft)}`
                    : '还没有体内驱虫记录' }}
              </div>
            </div>

            <div class="imm-box">
              <div class="imm-label">上次体外驱虫</div>
              <div class="imm-value">{{ immuneOverview.lastOutDate || '—' }}</div>
              <div class="imm-sub" :class="leftClass(immuneOverview.lastOutDaysLeft)">
                {{ immuneOverview.lastOutDate
                    ? `下次 ${immuneOverview.lastOutNextDate || '—'} · ${leftText(immuneOverview.lastOutDaysLeft)}`
                    : '还没有体外驱虫记录' }}
              </div>
            </div>
          </div>

          <div class="imm-title">疫苗</div>
          <el-empty v-if="!vaccineSummary.length" description="暂无疫苗记录" :image-size="50" />
          <el-table v-else :data="vaccineSummary" size="small" border class="imm-table">
            <el-table-column label="针次" width="86" align="center">
              <template #default="scope">
                <el-tag size="small" :type="scope.row.no ? 'primary' : 'info'" effect="plain">{{ scope.row.doseLabel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="接种日期" prop="date" width="110" align="center" />
            <el-table-column label="疫苗" prop="type" min-width="110" :show-overflow-tooltip="true" />
            <el-table-column label="下次接种" width="150" align="center">
              <template #default="scope">
                <div>{{ scope.row.nextDate || '—' }}</div>
                <div v-if="scope.row.nextDate" class="due-tip" :class="leftClass(scope.row.daysLeft)">{{ leftText(scope.row.daysLeft) }}</div>
              </template>
            </el-table-column>
          </el-table>

          <div class="imm-title">驱虫</div>
          <el-empty v-if="!dewormSummary.length" description="暂无驱虫记录" :image-size="50" />
          <el-table v-else :data="dewormSummary" size="small" border class="imm-table">
            <el-table-column label="次数" width="96" align="center">
              <template #default="scope">
                <el-tag size="small" :type="scope.row.noLabel && scope.row.noLabel.indexOf('体外') === 0 ? 'primary' : 'success'" effect="plain">
                  {{ scope.row.noLabel }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="驱虫日期" prop="date" width="110" align="center" />
            <el-table-column label="驱虫药" prop="drug" min-width="110" :show-overflow-tooltip="true" />
            <el-table-column label="下次驱虫" width="150" align="center">
              <template #default="scope">
                <div>{{ scope.row.nextDate || '—' }}</div>
                <div v-if="scope.row.nextDate" class="due-tip" :class="leftClass(scope.row.daysLeft)">{{ leftText(scope.row.daysLeft) }}</div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右：二维码 + 时间线 -->
      <el-col :xs="24" :md="9">
        <el-card shadow="never" class="block">
          <template #header><span>专属二维码</span></template>
          <div class="qr-box">
            <img v-if="profile.qrCode" :key="qrTick" :src="profile.qrCode" class="qr-img" alt="档案二维码" />
            <el-empty v-else description="尚未生成二维码" :image-size="50" />
            <div v-if="profile.scanUrl" class="qr-url">手机扫这个码，打开这只猫的档案：{{ profile.scanUrl }}</div>
            <div class="qr-tip">档案里改了名字、状态、病历都不用重打这张码，扫码看到的永远是最新内容</div>
            <div class="qr-actions">
              <el-button size="small" icon="Download" @click="downloadQr" :disabled="!profile.qrCode">保存图片</el-button>
              <el-button size="small" icon="Refresh" @click="handleRegenQr" v-hasPermi="['cat:profile:edit']">更新二维码</el-button>
            </div>
            <div class="qr-help">码按当前访问地址自动生成，换端口/网址后刷新就跟着变；贴纸上的码固定为打印那一刻的地址</div>
          </div>
        </el-card>

        <!-- 信息卡留档：笼位上的纸质卡片，出院回收 / 中途手写改药时拍照存档 -->
        <el-card shadow="never" class="block">
          <template #header>
            <div class="card-header">
              <span>信息卡留档（{{ cardPhotos.length }}）</span>
              <div class="card-actions">
                <el-button link type="primary" icon="Plus" @click="handleCardAdd" v-hasPermi="['cat:photo:add']">上传信息卡</el-button>
                <el-button link type="primary" icon="Printer" @click="goPrint">打印</el-button>
              </div>
            </div>
          </template>
          <el-empty v-if="!cardPhotos.length" description="还没有信息卡照片。出院回收、或卡上手写改了用药时，拍一张传上来" :image-size="50" />
          <div v-else class="card-photo-grid">
            <div class="card-photo-item" v-for="p in cardPhotos" :key="p.photoId">
              <el-image
                :src="resolveUrl(p.thumbUrl || p.fileUrl)"
                :preview-src-list="cardPreviewList"
                :initial-index="cardPreviewList.indexOf(resolveUrl(p.fileUrl))"
                fit="cover"
                preview-teleported
                class="card-photo"
              />
              <div class="card-photo-meta">
                <span class="card-photo-date">{{ (p.createTime || '').slice(0, 16) }}</span>
                <span class="card-photo-note" :title="p.remark">{{ p.remark || '无备注' }}</span>
              </div>
            </div>
          </div>
          <div class="card-photo-tip" v-if="cardPhotos.length">
            按上传时间倒序。建议一张卡一次上传，别把两次内容拍在同一张里；卡上手写的内容请在备注里写清楚。
          </div>
        </el-card>

        <el-card shadow="never" class="block" ref="timelineCard">
          <template #header>
            <div class="card-header">
              <span>病历时间线（{{ filteredTimeline.length }}）</span>
              <div class="card-actions">
                <el-select v-model="flowFilter" size="small" placeholder="按流转轨迹筛选" clearable style="width: 230px">
                  <el-option label="全部病历" value="" />
                  <el-option
                    v-for="f in flows"
                    :key="f.flowId"
                    :label="`截至 ${shortTime(f.operateTime)} · ${f.flowType || '流转'}`"
                    :value="f.flowId"
                  />
                </el-select>
                <el-tooltip content="诊断、用药、疫苗、驱虫、转诊、流转、死亡等记录按时间倒序合并展示" placement="top">
                  <el-icon><QuestionFilled /></el-icon>
                </el-tooltip>
              </div>
            </div>
          </template>
          <!-- 选中某次流转后，明确告诉用户：这一段病历是在哪个基地产生的 -->
          <div class="stage-tip" v-if="activeFlow">
            <el-icon><Location /></el-icon>
            <span>
              正在看【{{ shortTime(activeFlow.operateTime) }} {{ activeFlow.flowType }}】之前的 {{ filteredTimeline.length }} 条病历；
              这一阶段病例产生于 <b>{{ stageBaseName }}</b>
              <template v-if="activeFlow.fromBaseId && activeFlow.toBaseId">
                （{{ baseName(activeFlow.fromBaseId) }} → {{ baseName(activeFlow.toBaseId) }}）
              </template>
            </span>
            <el-button link type="primary" @click="flowFilter = ''">显示全部</el-button>
          </div>
          <el-empty v-if="!filteredTimeline.length" description="暂无病历记录" :image-size="60" />
          <el-timeline v-else class="timeline">
            <el-timeline-item
              v-for="(item, idx) in filteredTimeline"
              :key="idx"
              :timestamp="formatTime(item.time)"
              placement="top"
              :color="typeColor(item.type)"
            >
              <div class="tl-card">
                <div class="tl-head">
                  <el-tag size="small" :type="typeTag(item.type)" effect="light">{{ item.type }}</el-tag>
                  <span class="tl-title">{{ item.title || '—' }}</span>
                  <el-tag v-if="item.baseName" size="small" type="info" effect="plain" class="tl-place">
                    {{ item.baseName }}
                  </el-tag>
                </div>
                <div class="tl-content" v-if="item.content">{{ item.content }}</div>
                <div class="tl-detail" v-if="item.detail">{{ item.detail }}</div>
                <div class="tl-detail" v-if="item.checkItems">检查项目及用药：{{ item.checkItems }}</div>
                <div class="tl-detail warn" v-if="item.adverseReaction">不良反应：{{ item.adverseReaction }}</div>
                <div class="tl-correction" v-if="item.correctionNote">
                  <el-icon><WarningFilled /></el-icon>
                  更正（{{ item.correctionBy }} {{ formatTime(item.correctionTime) }}）：{{ item.correctionNote }}
                </div>
                <div class="tl-photos" v-if="item.photos">
                  <template v-for="(u, i) in splitPhotos(item.photos)" :key="i">
                    <el-image
                      v-if="previewKind(u) === 'image'"
                      :src="resolveUrl(u)"
                      :preview-src-list="imageOnly(splitPhotos(item.photos))"
                      :initial-index="imageOnly(splitPhotos(item.photos)).indexOf(resolveUrl(u))"
                      fit="cover"
                      preview-teleported
                      class="tl-photo"
                    />
                    <div v-else-if="previewKind(u) === 'video'" class="tl-photo tl-video" @click="openVideoUrl(u)">
                      <video :src="resolveUrl(u)" preload="metadata" muted playsinline></video>
                      <span class="tl-play"><el-icon><VideoPlay /></el-icon></span>
                    </div>
                    <div v-else class="tl-photo tl-file" @click="openAttachment(u)" :title="pickFileName(u)">
                      <el-icon><Document /></el-icon>
                      <span>{{ fileExtLabel(u) }}</span>
                    </div>
                  </template>
                </div>
                <div class="tl-foot" v-if="item.operator">操作人：{{ item.operator }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <el-card shadow="never" class="block" v-if="adoption">
          <template #header><span>领养信息</span></template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="领养人">{{ adoption.adopterName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ adoption.adopterPhone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="领养日期">{{ adoption.adoptionDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="住址">{{ adoption.adopterAddress || '—' }}</el-descriptions-item>
            <el-descriptions-item label="领养协议">
              <el-button v-if="adoption.agreementUrl" link type="primary" @click="openAgreement">查看协议</el-button>
              <span v-else>未上传</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="never" class="block" v-if="death">
          <template #header><span>死亡登记</span></template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="死亡日期">{{ death.deathDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="死亡地点">{{ death.deathPlace || '—' }}</el-descriptions-item>
            <el-descriptions-item label="死亡类型"><dict-tag :options="cat_death_type" :value="death.deathType" /></el-descriptions-item>
            <el-descriptions-item label="死因">{{ death.deathReason || '—' }}</el-descriptions-item>
            <el-descriptions-item label="遗体处理">{{ death.bodyDisposal || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 相册上传 / 编辑：猫固定为当前档案，不用再选猫 -->
    <el-dialog :title="photoTitle" v-model="photoOpen" width="560px" append-to-body>
      <el-form ref="photoFormRef" :model="photoForm" :rules="photoRules" label-width="90px">
        <el-form-item label="相册分类" prop="folder">
          <el-select v-model="photoForm.folder" placeholder="请选择相册分类" filterable allow-create default-first-option style="width: 100%">
            <el-option v-for="d in cat_photo_folder" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件类型" prop="fileType">
          <el-radio-group v-model="photoForm.fileType" @change="onPhotoTypeChange">
            <el-radio label="1">图片</el-radio>
            <el-radio label="2">视频</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="photoForm.fileType === '2' ? '视频' : '照片'" prop="fileUrl">
          <image-upload v-if="photoForm.fileType !== '2'" v-model="photoForm.fileUrl" :limit="1" :file-size="10" />
          <file-upload
            v-else
            v-model="photoForm.fileUrl"
            :limit="1"
            :file-size="50"
            :file-type="photoVideoTypes"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="photoForm.remark" type="textarea" :rows="2" placeholder="选填，如拍摄部位、状态说明" />
        </el-form-item>
        <el-alert
          v-if="!photoForm.photoId"
          type="success"
          :closable="false"
          show-icon
          title="保存后系统会自动生成缩略图（视频抽封面帧），列表加载更快。"
        />
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitPhoto">确 定</el-button>
          <el-button @click="photoOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 信息卡留档：只传图片 + 一句备注，分类固定「信息卡」，不用再选 -->
    <el-dialog title="上传信息卡照片" v-model="cardOpen" width="520px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="信息卡照片" required>
          <image-upload v-model="cardForm.fileUrl" :limit="1" :file-size="10" />
          <div class="form-tip">手机端点这里可直接调起相机拍照；建议一张卡拍一次，别把两次内容拍在一张里</div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="cardForm.remark"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="如：2026-11-03 出院时回收，卡上手写「加喂阿莫西林 3 天」"
          />
          <div class="form-tip">卡上手写的内容请写在这里，否则将来搜不到、也算不进病历</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="cardSaving" @click="submitCard">存 档</el-button>
          <el-button @click="cardOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 改位置：扫一扫卡就能改，不用回电脑上开档案编辑 -->
    <el-dialog title="修改所在位置" v-model="moveOpen" width="480px" append-to-body>
      <el-alert v-if="String(profile.status) === '9'" type="warning" :closable="false" show-icon class="mb8"
        title="这只猫是「待安置」状态：选好病房保存后会自动转回「在养」。" />
      <el-form label-width="96px">
        <el-form-item label="当前">
          <span class="muted">{{ locationText }}</span>
        </el-form-item>
        <el-form-item label="所在基地" required>
          <el-select v-model="moveForm.baseId" placeholder="请选择基地" style="width: 100%" @change="moveForm.wardId = undefined">
            <el-option v-for="b in baseList" :key="b.baseId" :label="b.baseName" :value="b.baseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="所在病房">
          <el-select v-model="moveForm.wardId" placeholder="还没分房可以留空" clearable filterable style="width: 100%">
            <el-option v-for="w in moveWardList" :key="w.wardId" :label="w.wardName" :value="w.wardId" />
          </el-select>
        </el-form-item>
        <el-form-item label="实际转移时间">
          <el-date-picker v-model="moveForm.moveTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="不填按现在记" style="width: 100%" />
          <div class="form-tip">猫早就放进屋了、现在才来补录的，填当时的时间；系统会另记此刻的录入时间。</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="moveSaving" @click="submitQuickMove">保 存</el-button>
          <el-button @click="moveOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="videoOpen" title="视频预览" width="640px" append-to-body destroy-on-close>
      <video :src="videoUrl" controls autoplay style="width: 100%"></video>
    </el-dialog>
  </div>
</template>

<script setup name="CatProfileDetail">
import { getCatArchive, archiveCatProfile, unarchiveCatProfile, regenerateCatQrCode, updateCatProfile } from "@/api/cat/profile"
import { getCatPhoto, addCatPhoto, updateCatPhoto, delCatPhoto } from "@/api/cat/photo"
import { deleteUploadFile } from "@/api/common"
import { listCatBase } from "@/api/cat/base"
import { listCatWard } from "@/api/cat/ward"
import { resolveFileUrl, previewKind, fileName as pickFileName, fileExtLabel } from "@/utils/fileUrl"
import { onMasterDataChanged, MASTER_KIND } from "@/utils/catCache"

const { proxy } = getCurrentInstance()
const { cat_status, cat_sex, cat_source, cat_neutered, cat_yes_no, cat_death_type, cat_photo_folder } = useDict(
  "cat_status", "cat_sex", "cat_source", "cat_neutered", "cat_yes_no", "cat_death_type", "cat_photo_folder"
)

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const profile = ref({})
// 二维码刷新计数：作为 <img> 的 key，保证重新生成后一定重新渲染
const qrTick = ref(0)
const tagNames = ref([])
const photos = ref([])
const flows = ref([])
const timeline = ref([])
// 疫苗第几针 / 驱虫第几次的时间点
const vaccineSummary = ref([])
const dewormSummary = ref([])
// 免疫概览：基础免疫进度、狂犬/加强针到期、上次体内外驱虫
const immuneOverview = ref({ baseDoseDone: 0, baseDoseTotal: 3 })
// 头像：相册最新一张，取不到回退名字首字
const avatarUrl = ref('')
const avatarBroken = ref(false)
// 病历时间线按流转轨迹筛选：'' = 全部，否则是某条流转的 flowId
const flowFilter = ref('')
const timelineCard = ref(null)
const adoption = ref(null)
const death = ref(null)
const baseList = ref([])
const wardList = ref([])
const videoOpen = ref(false)
const videoUrl = ref("")

// 相册上传/编辑（在详情页直接管理图片视频）
const photoOpen = ref(false)
const photoTitle = ref("上传照片/视频")
const photoForm = ref({})
const photoVideoTypes = ['mp4', 'mov', 'm4v', 'avi', 'webm', 'mkv']
const photoRules = {
  folder: [{ required: true, message: '请选择相册分类', trigger: 'change' }],
  fileUrl: [{ required: true, message: '请上传照片或视频', trigger: 'change' }]
}
// 图片 / 视频各自记住已选文件：来回切换类型时不丢刚上传的文件
const photoSelected = { '1': undefined, '2': undefined }
const photoLastType = ref('1')

const catId = computed(() => route.params.catId)

const avatarSrc = computed(() => {
  if (avatarBroken.value || !avatarUrl.value) return ''
  return resolveUrl(avatarUrl.value)
})

// 当前选中的流转节点
const activeFlow = computed(() => {
  if (!flowFilter.value) return null
  return flows.value.find(f => String(f.flowId) === String(flowFilter.value)) || null
})

// 选中的这次流转之前，猫咪所在基地（转出基地就是这一阶段病例产生的地方）
const stageBaseName = computed(() => {
  const f = activeFlow.value
  if (!f) return ''
  return baseName(f.fromBaseId) || baseName(f.toBaseId) || '未记录基地'
})

// 时间线过滤：只看这次流转时间点之前的病历
const filteredTimeline = computed(() => {
  const f = activeFlow.value
  if (!f) return timeline.value
  const limit = timeOf(f.operateTime)
  if (limit === null) return timeline.value
  return timeline.value.filter(item => {
    const t = timeOf(item.time)
    // 没有时间的记录（极少）在阶段视图里不展示，避免归错阶段
    return t !== null && t <= limit
  })
})

function timeOf(value) {
  if (!value) return null
  if (typeof value === 'number') return value
  const t = new Date(String(value).replace(' ', 'T')).getTime()
  return isNaN(t) ? null : t
}

function shortTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').substring(0, 16)
}

// 点流转轨迹里的「看此前病历」：右侧时间线切成这一阶段，并滚到时间线卡片
function filterByFlow(row) {
  flowFilter.value = row.flowId
  if (timelineCard.value && timelineCard.value.$el) {
    timelineCard.value.$el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

const imagePreviewList = computed(() =>
  photos.value.filter(p => p.fileType !== '2').map(p => resolveUrl(p.fileUrl))
)

/**
 * 信息卡留档：相册里 folder =「信息卡」的照片，按上传时间倒序。
 * 卡片是实物证据（手写改药、出院回收），所以这里允许重复上传，一张卡一条记录，
 * 备注里写清楚"哪次回收 / 手写改了什么"。
 */
const cardPhotos = computed(() =>
  photos.value
    .filter(p => p.folder === '信息卡' && p.fileType !== '2')
    .slice()
    .sort((a, b) => String(b.createTime || '').localeCompare(String(a.createTime || '')))
)
const cardPreviewList = computed(() => cardPhotos.value.map(p => resolveUrl(p.fileUrl)))

const locationText = computed(() => {
  const b = baseName(profile.value.baseId)
  const w = wardName(profile.value.wardId)
  if (b && w) return b + ' / ' + w
  return b || w || '—'
})

// 出生日期：估算出来的（按录入年龄反推）标注「估算」，准确的直接显示
const birthText = computed(() => {
  const d = profile.value.birthDate
  if (!d) return '未知'
  return profile.value.birthEstimated === '0' ? d : d + '（估算）'
})

// 统一走 utils/fileUrl：拼环境前缀 + 编码中文文件名（照片名里常有中文，不编码会 404）
function resolveUrl(url) {
  return resolveFileUrl(url)
}

function baseName(id) {
  if (!id) return ''
  const b = baseList.value.find(x => x.baseId === id)
  return b ? b.baseName : '#' + id
}

function wardName(id) {
  if (!id) return ''
  const w = wardList.value.find(x => x.wardId === id)
  return w ? w.wardName : '#' + id
}

function formatTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').substring(0, 16)
}

function splitPhotos(str) {
  if (!str) return []
  return String(str).split(',').map(s => s.trim()).filter(Boolean)
}

/** 病历时间线里的资料可能是照片/视频/PDF，只有图片进 el-image 的相册 */
function imageOnly(list) {
  return (list || []).filter(u => previewKind(u) === 'image').map(u => resolveUrl(u))
}

const TYPE_COLORS = {
  '诊断': '#e6a23c', '用药': '#67c23a', '疫苗': '#409eff', '驱虫': '#909399',
  '转诊': '#f56c6c', '流转': '#6b5b95', '死亡': '#303133', 'TNR': '#13c2c2',
  '放归': '#00b96b', '领养': '#722ed1'
}
const TYPE_TAGS = {
  '诊断': 'warning', '用药': 'success', '疫苗': 'primary', '驱虫': 'info',
  '转诊': 'danger', '流转': '', '死亡': 'info', 'TNR': 'primary', '放归': 'success', '领养': 'danger'
}
function typeColor(type) { return TYPE_COLORS[type] || '#909399' }
function typeTag(type) { return TYPE_TAGS[type] || 'info' }

/** 到期还剩几天（负数=已过期），概览与表格共用 */
function leftText(days) {
  if (days === null || days === undefined) return '—'
  if (days < 0) return `已过期 ${-days} 天`
  if (days === 0) return '今天到期'
  if (days <= 3) return `还有 ${days} 天（临期）`
  return `还有 ${days} 天`
}

function leftClass(days) {
  if (days === null || days === undefined) return 'is-muted'
  if (days < 0) return 'is-danger'
  if (days <= 3) return 'is-warning'
  return 'is-muted'
}

function getData() {
  loading.value = true
  getCatArchive(catId.value).then(res => {
    const d = res.data || {}
    profile.value = d.profile || {}
    tagNames.value = d.tagNames || []
    photos.value = d.photos || []
    flows.value = d.flows || []
    timeline.value = d.timeline || []
    avatarUrl.value = d.avatar || ''
    vaccineSummary.value = d.vaccineSummary || []
    dewormSummary.value = d.dewormSummary || []
    immuneOverview.value = d.immuneOverview || { baseDoseDone: 0, baseDoseTotal: 3 }
    adoption.value = d.adoption || null
    death.value = d.death || null
    loading.value = false
  }).catch(() => { loading.value = false })
}

function getBaseList() {
  listCatBase({ pageNum: 1, pageSize: 500 }).then(res => { baseList.value = res.rows || [] })
}
function getWardList() {
  listCatWard({ pageNum: 1, pageSize: 500 }).then(res => { wardList.value = res.rows || [] })
}

function goBack() {
  router.push('/profile/profile')
}
function goEdit() {
  router.push({ path: '/profile/profile', query: { editCatId: catId.value } })
}
function goPhoto() {
  router.push({ path: '/profile/photo', query: { catId: catId.value } })
}
function goPrint() {
  router.push({ path: '/profile/qrcard', query: { catIds: catId.value } })
}

function downloadQr() {
  const src = profile.value.qrCode
  if (!src) return
  const a = document.createElement('a')
  a.href = src
  a.download = `二维码_${profile.value.archiveNo || profile.value.catId}.png`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

function handleRegenQr() {
  // 接口直接返回新二维码，就地替换，页面立即生效，不用整页重新拉档案
  regenerateCatQrCode(catId.value).then(res => {
    const data = res.data || {}
    if (data.qrCode) {
      profile.value.qrCode = data.qrCode
      profile.value.scanUrl = data.scanUrl
      qrTick.value++
    }
    if (data.changed === false) {
      proxy.$modal.msg('这个二维码已经是最新的，不用更新（它指向的档案网址没有变）')
    } else {
      proxy.$modal.msgSuccess('二维码已更新，现在指向新的档案网址')
    }
  })
}

function handleExportPdf() {
  proxy.downloadGet('cat/profile/pdf/' + catId.value, {}, `猫咪档案_${profile.value.archiveNo || catId.value}.pdf`)
}

function handleArchive() {
  proxy.$modal.confirm('归档后该猫咪不再出现在在养列表，可随时取消归档。是否继续？').then(() => {
    return archiveCatProfile(catId.value)
  }).then(() => {
    proxy.$modal.msgSuccess('已归档')
    getData()
  }).catch(() => {})
}

function handleUnarchive() {
  unarchiveCatProfile(catId.value).then(() => {
    proxy.$modal.msgSuccess('已取消归档')
    getData()
  })
}

function openVideo(p) {
  openVideoUrl(p.fileUrl)
}

// ---------- 相册：详情页直接上传 / 编辑 / 删除 ----------
function resetPhotoForm() {
  photoSelected['1'] = undefined
  photoSelected['2'] = undefined
  photoLastType.value = '1'
}

// ---- 信息卡留档：上传（复用相册接口，分类固定为「信息卡」） ----
const cardOpen = ref(false)
const cardForm = ref({})
const cardSaving = ref(false)

function handleCardAdd() {
  cardForm.value = { fileUrl: undefined, remark: undefined }
  cardOpen.value = true
}

function submitCard() {
  if (!cardForm.value.fileUrl) {
    proxy.$modal.msgWarning('请先拍照或选择信息卡照片')
    return
  }
  cardSaving.value = true
  addCatPhoto({
    catId: Number(catId.value),
    folder: '信息卡',
    fileType: '1',
    fileUrl: cardForm.value.fileUrl,
    remark: cardForm.value.remark
  }).then(() => {
    proxy.$modal.msgSuccess('信息卡已存档')
    cardOpen.value = false
    getData()
  }).finally(() => { cardSaving.value = false })
}

// ---- 改位置：手机扫码进来也能两下改完，保存时后端自动补一条「基地内安置」流转 ----
const moveOpen = ref(false)
const moveSaving = ref(false)
const moveForm = ref({ baseId: undefined, wardId: undefined, moveTime: undefined })

const moveWardList = computed(() => {
  if (!moveForm.value.baseId) return wardList.value
  return wardList.value.filter(w => w.baseId === moveForm.value.baseId)
})

function openQuickMove() {
  moveForm.value = {
    baseId: profile.value.baseId,
    wardId: profile.value.wardId,
    moveTime: undefined
  }
  moveOpen.value = true
}

function submitQuickMove() {
  if (!moveForm.value.baseId) {
    proxy.$modal.msgWarning('请选择所在基地')
    return
  }
  const payload = {
    catId: Number(catId.value),
    baseId: moveForm.value.baseId,
    wardId: moveForm.value.wardId || undefined,
    moveTime: moveForm.value.moveTime,
    status: profile.value.status
  }
  // 待安置的猫选好房间，就顺势转回「在养」
  if (String(profile.value.status) === '9' && moveForm.value.wardId) {
    payload.status = '1'
  }
  moveSaving.value = true
  updateCatProfile(payload).then(() => {
    proxy.$modal.msgSuccess('位置已更新，流转记录已补上')
    moveOpen.value = false
    getData()
  }).finally(() => { moveSaving.value = false })
}

function handlePhotoAdd() {
  resetPhotoForm()
  photoForm.value = {
    catId: Number(catId.value),
    folder: '救助时',
    fileType: '1',
    fileUrl: undefined,
    remark: undefined
  }
  photoTitle.value = '上传照片/视频'
  photoOpen.value = true
}

function handlePhotoEdit(p) {
  getCatPhoto(p.photoId).then(res => {
    resetPhotoForm()
    photoForm.value = { ...res.data }
    if (!photoForm.value.fileType) {
      photoForm.value.fileType = '1'
    }
    photoSelected[photoForm.value.fileType] = photoForm.value.fileUrl
    photoLastType.value = photoForm.value.fileType
    photoTitle.value = '修改照片/视频'
    photoOpen.value = true
  })
}

// 切换「图片 / 视频」时把另一类已选文件存起来，切回来还能还原
function onPhotoTypeChange(val) {
  photoSelected[photoLastType.value] = photoForm.value.fileUrl
  photoLastType.value = val
  photoForm.value.fileUrl = photoSelected[val]
  photoForm.value.thumbUrl = undefined
}

function submitPhoto() {
  proxy.$refs['photoFormRef'].validate(valid => {
    if (!valid) return
    const isEdit = photoForm.value.photoId != undefined
    const request = isEdit ? updateCatPhoto(photoForm.value) : addCatPhoto(photoForm.value)
    request.then(() => {
      proxy.$modal.msgSuccess(isEdit ? '修改成功' : '上传成功')
      photoOpen.value = false
      // 重新拉档案：相册、时间线里的检查照片都跟着更新
      getData()
    })
  })
}

function handlePhotoDelete(p) {
  proxy.$modal.confirm('删除后服务器上的文件也会一并清除，且无法恢复，是否继续？').then(() => {
    return delCatPhoto(p.photoId)
  }).then(() => {
    // 数据库删掉后再物理删文件，避免库里还留着死链接
    if (p.fileUrl) deleteUploadFile(p.fileUrl).catch(() => {})
    if (p.thumbUrl) deleteUploadFile(p.thumbUrl).catch(() => {})
    proxy.$modal.msgSuccess('删除成功')
    getData()
  }).catch(() => {})
}

function openVideoUrl(url) {
  videoUrl.value = resolveUrl(url)
  videoOpen.value = true
}

/** 时间线里的非图片资料：PDF 直接新窗口，Word/Excel 引导下载 */
function openAttachment(url) {
  if (!url) return
  const kind = previewKind(url)
  if (kind === 'office') {
    proxy.$modal.confirm(`${fileExtLabel(url)} 无法在浏览器里直接预览，点「确定」下载后用本机程序查看。`).then(() => {
      proxy.downloadGet("common/download/resource", { resource: url }, pickFileName(url) || "病历资料")
    }).catch(() => {})
    return
  }
  window.open(resolveUrl(url), '_blank')
}

function openAgreement() {
  const url = adoption.value && adoption.value.agreementUrl
  if (!url) return
  // Word / Excel 浏览器打不开，引导下载而不是开一个空白页
  if (previewKind(url) === 'office') {
    proxy.$modal.confirm(`${fileExtLabel(url)} 无法在浏览器里直接预览，点「确定」下载后用本机程序查看。`).then(() => {
      proxy.downloadGet("common/download/resource", { resource: url }, pickFileName(url) || "领养协议")
    }).catch(() => {})
    return
  }
  window.open(resolveUrl(url), '_blank')
}

getBaseList()
getWardList()
getData()

// 基地/病房在别的页签改名或新增后，本页的名称映射立即刷新（keep-alive 页面不会重新 setup）
onMasterDataChanged([MASTER_KIND.BASE, MASTER_KIND.WARD], () => {
  getBaseList()
  getWardList()
})
</script>

<style scoped>
.archive-detail :deep(.el-card__header) { padding: 10px 16px; }
.archive-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  flex-wrap: wrap; gap: 12px;
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px; padding: 16px 18px; margin-bottom: 14px;
}
.header-main { display: flex; gap: 14px; align-items: flex-start; }
.avatar {
  width: 54px; height: 54px; border-radius: 50%; flex-shrink: 0;
  background: linear-gradient(135deg, #4a90d9, #6b5b95);
  color: #fff; font-size: 24px; display: flex; align-items: center; justify-content: center;
}
/* 有照片时头像直接用相册最新一张，不再显示名字首字 */
.avatar.has-img {
  padding: 0; overflow: hidden;
  background: var(--el-fill-color-light);
  border: 2px solid var(--el-bg-color);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
}
.avatar img { width: 100%; height: 100%; object-fit: cover; display: block; }
.name-row { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-bottom: 6px; }
.cat-name { font-size: 20px; font-weight: 600; color: var(--el-text-color-primary); }
.meta-row { display: flex; gap: 16px; flex-wrap: wrap; font-size: 13px; color: var(--el-text-color-secondary); }
.tag-row { margin-top: 8px; display: flex; gap: 6px; flex-wrap: wrap; }
.header-actions { display: flex; gap: 8px; flex-wrap: wrap; }
.block { margin-bottom: 14px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-actions { display: flex; align-items: center; gap: 12px; }
.card-actions .el-button + .el-button { margin-left: 0; }
.qr-box { text-align: center; }
.qr-img { width: 160px; height: 160px; }
.qr-url { font-size: 12px; color: var(--el-text-color-secondary); word-break: break-all; margin-top: 6px; }
.qr-tip { font-size: 12px; color: var(--el-text-color-placeholder); margin: 8px 0; }
.qr-help { font-size: 12px; color: var(--el-text-color-placeholder); margin-top: 6px; }
.qr-actions { display: flex; gap: 8px; justify-content: center; }
.photo-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(96px, 1fr)); gap: 10px; }
.photo-item { text-align: center; }
.photo { width: 100%; height: 86px; border-radius: 6px; display: block; }
.photo.video { position: relative; cursor: pointer; background: #000; }
.play-icon { position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); font-size: 24px; color: #fff; }
.photo-folder { font-size: 12px; color: var(--el-text-color-placeholder); margin-top: 4px; }
/* 相册里每张图的操作：详情页直接改，不用跳去相册管理页 */
.photo-ops { display: flex; justify-content: center; gap: 8px; margin-top: 2px; }
.photo-ops .el-button + .el-button { margin-left: 0; }
.photo-ops :deep(.el-button) { font-size: 12px; }
/* 信息卡留档：右栏窄，固定两列，图下带时间和备注 */
.card-photo-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
.card-photo-item { min-width: 0; }
.card-photo { width: 100%; height: 96px; border-radius: 6px; display: block; cursor: zoom-in; }
.card-photo-meta { margin-top: 4px; line-height: 1.4; }
.card-photo-date { display: block; font-size: 11px; color: var(--el-text-color-placeholder); }
.card-photo-note {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 12px;
  color: var(--el-text-color-regular);
}
.card-photo-tip { margin-top: 10px; font-size: 12px; color: var(--el-text-color-placeholder); line-height: 1.6; }
.timeline { padding-left: 2px; max-height: 720px; overflow-y: auto; }
.tl-card { background: var(--el-fill-color-light); border-radius: 6px; padding: 8px 10px; }
.tl-head { display: flex; align-items: center; gap: 8px; }
.tl-head .tl-place { margin-left: auto; }
/* 按流转轨迹筛选时间线时的阶段说明 */
.stage-tip {
  display: flex; align-items: flex-start; gap: 6px; flex-wrap: wrap;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary-dark-2);
  border-radius: 6px; padding: 8px 10px;
  font-size: 12px; line-height: 1.6; margin-bottom: 10px;
}
.stage-tip .el-button { margin-left: auto; }
/* 免疫与驱虫时间点 */
.imm-title { font-size: 13px; font-weight: 600; margin: 4px 0 8px; color: var(--el-text-color-primary); }
.imm-table { margin-bottom: 14px; }
/* 免疫概览：五块并排，不点进列表就能判断这猫该不该补针 */
.imm-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(148px, 1fr));
  gap: 8px;
  margin-bottom: 14px;
}
.imm-box {
  padding: 8px 10px;
  border-radius: 6px;
  background: var(--el-fill-color-lighter);
  border-left: 3px solid var(--el-color-primary-light-5);
}
.imm-label { font-size: 12px; color: var(--el-text-color-secondary); margin-bottom: 2px; }
.imm-value { font-size: 15px; font-weight: 600; color: var(--el-text-color-primary); font-variant-numeric: tabular-nums; }
.imm-value b { font-size: 20px; }
.imm-total { font-size: 12px; font-weight: 400; color: var(--el-text-color-secondary); margin-left: 2px; }
.imm-sub { margin-top: 2px; font-size: 12px; line-height: 1.5; color: var(--el-text-color-secondary); }
.imm-sub.is-danger { color: var(--el-color-danger); }
.imm-sub.is-warning { color: var(--el-color-warning); }
.tl-title { font-size: 13px; font-weight: 600; color: var(--el-text-color-primary); }
.tl-content { font-size: 13px; color: var(--el-text-color-regular); margin-top: 4px; }
.tl-detail { font-size: 12px; color: var(--el-text-color-secondary); margin-top: 3px; }
.tl-detail.warn { color: var(--el-color-danger); }
.tl-correction {
  margin-top: 6px; font-size: 12px; color: var(--el-color-warning);
  background: var(--el-color-warning-light-9); border-radius: 4px; padding: 4px 6px;
  display: flex; gap: 4px; align-items: flex-start;
}
.tl-photos { display: flex; gap: 6px; flex-wrap: wrap; margin-top: 6px; }
.tl-photo { width: 60px; height: 60px; border-radius: 4px; }
.tl-video {
  position: relative; overflow: hidden; cursor: pointer; background: #000;
  video { display: block; width: 100%; height: 100%; object-fit: cover; }
}
.tl-play {
  position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
  font-size: 18px; color: #fff; background: rgba(0, 0, 0, 0.35);
}
.tl-file {
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 2px;
  cursor: pointer; font-size: 10px; line-height: 1.2; text-align: center; padding: 0 2px;
  color: var(--el-text-color-secondary); background: var(--el-fill-color);
  border: 1px solid var(--el-border-color-lighter);
  .el-icon { font-size: 16px; }
  &:hover { color: var(--el-color-primary); border-color: var(--el-color-primary); }
}
.tl-foot { font-size: 12px; color: var(--el-text-color-placeholder); margin-top: 4px; }
</style>
