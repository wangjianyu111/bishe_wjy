<template>
  <div class="guidance-relation-page">

    <!-- ====== 教师 / 管理员视图 ====== -->
    <template v-if="!isStudent">
      <el-card shadow="never" class="main-card">
        <template #header>
          <div class="card-header">
            <span>指导关系管理</span>
            <div class="header-right">
              <el-button v-if="hasPermission('guidance:relation:add')" type="primary" @click="openApplyDialog">
                <el-icon><Plus /></el-icon>发送申请
              </el-button>
              <el-button v-if="hasPermission('guidance:relation:add')" type="success" @click="openCreateGroupDialog">
                <el-icon><Plus /></el-icon>创建关系小组
              </el-button>
              <el-badge v-if="receivedCount > 0" :value="receivedCount" :max="99">
                <el-button @click="activeTab = 'apply-received'">
                  <el-icon><Bell /></el-icon>收到的申请
                </el-button>
              </el-badge>
            </div>
          </div>
        </template>

        <el-tabs v-model="activeTab" @tab-change="onTabChange">
          <!-- ====== 学生信息标签页 ====== -->
          <el-tab-pane label="学生信息" name="students">
            <div class="filter-bar">
              <el-form :inline="true">
                <el-form-item label="学年">
                  <el-input v-model="query.academicYear" placeholder="如 2024-2025" clearable style="width:140px" />
                </el-form-item>
                <el-form-item label="关键词">
                  <el-input v-model="query.keyword" placeholder="学生姓名/学号" clearable style="width:160px" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSearch">查询</el-button>
                  <el-button @click="handleReset">重置</el-button>
                </el-form-item>
              </el-form>
            </div>

            <!-- 学生卡片列表 -->
            <div v-loading="loading">
              <el-empty v-if="cardList.length === 0 && !loading" description="暂无指导学生" />
              <div v-else class="card-grid">
                <el-card
                  v-for="item in cardList"
                  :key="item.relationId"
                  class="info-card"
                  shadow="hover"
                >
                  <div class="card-body">
                    <div class="card-avatar">
                      <el-avatar :size="56" :style="{ backgroundColor: avatarColors[item.relationId % 6] }">
                        {{ (item.studentName || '').charAt(0) }}
                      </el-avatar>
                    </div>
                    <div class="card-info">
                      <div class="card-name">{{ item.studentName }}</div>
                      <div class="card-sub">学号：{{ item.studentNo || '—' }}</div>
                      <div class="card-sub">学校：{{ item.campusName || '—' }}</div>
                      <div class="card-sub">学年：{{ item.academicYear || '—' }}</div>
                      <div class="card-sub">
                        状态：
                        <el-tag v-if="item.status === 'ACTIVE'" type="success" size="small">有效</el-tag>
                        <el-tag v-else type="danger" size="small">已解除</el-tag>
                      </div>
                      <!-- 答辩小组信息 -->
                      <div v-if="item.defenseGroups && item.defenseGroups.length > 0" class="card-groups">
                        <div class="card-groups-label">所属答辩小组：</div>
                        <el-tag
                          v-for="g in item.defenseGroups"
                          :key="g.groupId"
                          size="small"
                          style="margin:2px"
                        >{{ g.groupName }}</el-tag>
                      </div>
                    </div>
                  </div>
                  <div class="card-footer">
                    <el-button type="primary" link size="small" @click="openDetail(item)">详情</el-button>
                    <el-button
                      v-if="hasPermission('guidance:relation:del') && item.status === 'ACTIVE'"
                      type="danger"
                      link
                      size="small"
                      @click="handleUnbindStudent(item)"
                    >解除</el-button>
                  </div>
                </el-card>
              </div>
              <div v-if="pagination.total > 0" class="pagination-wrap">
                <el-pagination
                  background
                  layout="total, prev, pager, next"
                  :total="pagination.total"
                  :current-page="pagination.current"
                  :page-size="pagination.size"
                  @current-change="loadStudentCards"
                />
              </div>
            </div>
          </el-tab-pane>

          <!-- ====== 关系小组标签页 ====== -->
          <el-tab-pane label="关系小组" name="groups">
            <el-tabs v-model="groupSubTab" @tab-change="onGroupTabChange">
              <el-tab-pane label="已创建" name="created">
                <div v-loading="groupLoading">
                  <el-empty v-if="createdGroups.length === 0 && !groupLoading" description="暂无已创建的小组" />
                  <div v-else class="card-grid">
                    <el-card
                      v-for="g in createdGroups"
                      :key="g.groupId"
                      class="info-card group-card"
                      shadow="hover"
                    >
                      <div class="card-body">
                        <div class="group-header">
                          <div class="group-name">{{ g.groupName }}</div>
                          <el-tag v-if="g.status === 'ACTIVE'" type="success" size="small">有效</el-tag>
                          <el-tag v-else size="small">已解散</el-tag>
                        </div>
                        <div class="card-sub">组长：{{ g.leaderName }} · {{ g.campusName }}</div>
                        <div class="card-sub">学年：{{ g.academicYear }}</div>
                        <div class="card-sub">
                          <span class="count-badge">教师 {{ g.teacherCount }}人</span>
                          <span class="count-badge">学生 {{ g.studentCount }}人</span>
                        </div>
                        <div class="group-teachers">
                          <div class="group-section-title">答辩教师</div>
                          <div class="member-list">
                            <span v-for="t in (g.teachers || [])" :key="t.userId" class="member-tag">
                              {{ t.userName }}<span class="teacher-no">({{ t.teacherNo || '无工号' }})</span>
                            </span>
                          </div>
                        </div>
                        <div class="group-students">
                          <div class="group-section-title">答辩学生</div>
                          <div class="member-list">
                            <span v-for="s in (g.students || [])" :key="s.studentId" class="member-tag">
                              {{ s.studentName }}<span class="stu-no">({{ s.studentNo }})</span>
                              <span v-if="s.topicName" class="topic-name">《{{ s.topicName }}》</span>
                            </span>
                            <span v-if="!g.students || g.students.length === 0" class="no-data">暂无学生</span>
                          </div>
                        </div>
                      </div>
                      <div class="card-footer">
                        <el-button type="primary" link size="small" @click="openGroupDetail(g)">详情</el-button>
                        <el-button
                          v-if="g.leaderId === currentUserId && g.status === 'ACTIVE'"
                          type="primary"
                          link
                          size="small"
                          @click="openEditGroupDialog(g)"
                        >编辑</el-button>
                        <el-button
                          v-if="hasPermission('guidance:relation:del') && g.status === 'ACTIVE'"
                          type="danger"
                          link
                          size="small"
                          @click="handleDissolveGroup(g)"
                        >解散</el-button>
                      </div>
                    </el-card>
                  </div>
                </div>
              </el-tab-pane>

              <el-tab-pane label="已加入" name="joined">
                <div v-loading="groupLoading">
                  <el-empty v-if="joinedGroups.length === 0 && !groupLoading" description="暂无加入的小组" />
                  <div v-else class="card-grid">
                    <el-card
                      v-for="g in joinedGroups"
                      :key="g.groupId"
                      class="info-card group-card"
                      shadow="hover"
                    >
                      <div class="card-body">
                        <div class="group-header">
                          <div class="group-name">{{ g.groupName }}</div>
                          <el-tag v-if="g.status === 'ACTIVE'" type="success" size="small">有效</el-tag>
                        </div>
                        <div class="card-sub">组长：{{ g.leaderName }} · {{ g.campusName }}</div>
                        <div class="card-sub">学年：{{ g.academicYear }}</div>
                        <div class="card-sub">
                          <span class="count-badge">教师 {{ g.teacherCount }}人</span>
                          <span class="count-badge">学生 {{ g.studentCount }}人</span>
                        </div>
                      </div>
                      <div class="card-footer">
                        <el-button type="primary" link size="small" @click="openGroupDetail(g)">详情</el-button>
                        <el-button
                          v-if="hasPermission('guidance:relation:del') && g.status === 'ACTIVE' && !g.isCreator"
                          type="warning"
                          link
                          size="small"
                          @click="handleQuitGroup(g)"
                        >退出</el-button>
                      </div>
                    </el-card>
                  </div>
                </div>
              </el-tab-pane>

              <!-- ====== 全部小组管理 ====== -->
              <el-tab-pane label="全部小组" name="all-groups">
                <div class="filter-bar">
                  <el-form :inline="true">
                    <el-form-item label="学年">
                      <el-input v-model="allGroupQuery.academicYear" placeholder="如 2024-2025" clearable style="width:140px" />
                    </el-form-item>
                    <el-form-item label="关键词">
                      <el-input v-model="allGroupQuery.keyword" placeholder="小组名称/组长" clearable style="width:160px" />
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" @click="loadAllGroups(1)">查询</el-button>
                      <el-button @click="allGroupQuery.academicYear='';allGroupQuery.keyword='';loadAllGroups(1)">重置</el-button>
                    </el-form-item>
                  </el-form>
                </div>
                <div v-loading="allGroupLoading">
                  <el-table :data="allGroupList" border stripe size="small">
                    <el-table-column prop="groupName" label="小组名称" min-width="180" show-overflow-tooltip />
                    <el-table-column prop="leaderName" label="组长" width="100" />
                    <el-table-column prop="campusName" label="学校" width="140" show-overflow-tooltip />
                    <el-table-column prop="academicYear" label="学年" width="120" />
                    <el-table-column label="教师" width="80" align="center">
                      <template #default="{ row }">{{ row.teachers?.length || 0 }}人</template>
                    </el-table-column>
                    <el-table-column label="学生" width="80" align="center">
                      <template #default="{ row }">{{ row.students?.length || 0 }}人</template>
                    </el-table-column>
                    <el-table-column label="状态" width="100" align="center">
                      <template #default="{ row }">
                        <el-tag v-if="row.status === 'ACTIVE'" type="success" size="small">有效</el-tag>
                        <el-tag v-else size="small">已解散</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="createTime" label="创建时间" width="170" />
                    <el-table-column label="操作" width="200" fixed="right">
                      <template #default="{ row }">
                        <el-button type="primary" link size="small" @click="openGroupDetail(row)">详情</el-button>
                        <el-button
                          v-if="row.leaderId === currentUserId"
                          type="primary"
                          link
                          size="small"
                          @click="openEditGroupDialog(row)"
                        >编辑</el-button>
                        <el-button
                          v-if="row.leaderId === currentUserId"
                          type="danger"
                          link
                          size="small"
                          @click="handleDissolveGroup(row)"
                        >解散</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  <div v-if="allGroupPagination.total > 0" class="pagination-wrap">
                    <el-pagination
                      background
                      layout="total, prev, pager, next"
                      :total="allGroupPagination.total"
                      :current-page="allGroupPagination.current"
                      :page-size="allGroupPagination.size"
                      @current-change="loadAllGroups"
                    />
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-tab-pane>

          <!-- ====== 收到的申请 ====== -->
          <el-tab-pane name="apply-received">
            <template #label>
              <span>收到的申请<span v-if="receivedCount > 0" class="apply-badge">{{ receivedCount }}</span></span>
            </template>
            <div v-loading="applyLoading">
              <el-empty v-if="receivedList.length === 0 && !applyLoading" description="暂无收到的申请" />
              <el-table v-else :data="receivedList" border stripe size="small">
                <el-table-column prop="fromUserName" label="申请人" width="100" />
                <el-table-column label="身份" width="80">
                  <template #default="{ row }">{{ row.fromUserType === 'STUDENT' ? '学生' : '教师' }}</template>
                </el-table-column>
                <el-table-column prop="fromUserId === row.fromUserId && row.fromUserType === 'STUDENT' ? '' : ''" label="学号/工号" width="120">
                  <template #default="{ row }">
                    <span v-if="row.fromUserType === 'STUDENT'">{{ getStudentNo(row.fromUserId) }}</span>
                    <span v-else>{{ getTeacherNo(row.fromUserId) }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="academicYear" label="学年" width="120" />
                <el-table-column prop="message" label="申请留言" min-width="150" show-overflow-tooltip />
                <el-table-column label="状态" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag v-if="row.status === 'PENDING'" type="warning" size="small">待处理</el-tag>
                    <el-tag v-else-if="row.status === 'APPROVED'" type="success" size="small">已同意</el-tag>
                    <el-tag v-else-if="row.status === 'REJECTED'" type="danger" size="small">已拒绝</el-tag>
                    <el-tag v-else size="small">{{ row.status }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="申请时间" width="170" />
                <el-table-column label="操作" width="160" fixed="right">
                  <template #default="{ row }">
                    <template v-if="row.status === 'PENDING'">
                      <el-button type="success" link size="small" @click="handleApply(row, 'APPROVED')">同意</el-button>
                      <el-button type="danger" link size="small" @click="handleApply(row, 'REJECTED')">拒绝</el-button>
                    </template>
                    <span v-else style="color:#909399;font-size:12px">
                      {{ row.handleByName ? '由' + row.handleByName + '处理' : '—' }}
                    </span>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="applyPagination.total > 0" class="pagination-wrap">
                <el-pagination
                  background
                  layout="total, prev, pager, next"
                  :total="applyPagination.total"
                  :current-page="applyPagination.current"
                  :page-size="applyPagination.size"
                  @current-change="loadReceivedApplies"
                />
              </div>
            </div>
          </el-tab-pane>

          <!-- ====== 发出的申请 ====== -->
          <el-tab-pane label="发出的申请" name="apply-sent">
            <div v-loading="applyLoading">
              <el-empty v-if="sentList.length === 0 && !applyLoading" description="暂无发出的申请" />
              <el-table v-else :data="sentList" border stripe size="small">
                <el-table-column prop="toUserName" label="接收方" width="100" />
                <el-table-column label="对方身份" width="80">
                  <template #default="{ row }">{{ row.toUserType === 'STUDENT' ? '学生' : '教师' }}</template>
                </el-table-column>
                <el-table-column prop="academicYear" label="学年" width="120" />
                <el-table-column prop="message" label="申请留言" min-width="150" show-overflow-tooltip />
                <el-table-column label="状态" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag v-if="row.status === 'PENDING'" type="warning" size="small">待处理</el-tag>
                    <el-tag v-else-if="row.status === 'APPROVED'" type="success" size="small">已同意</el-tag>
                    <el-tag v-else-if="row.status === 'REJECTED'" type="danger" size="small">已拒绝</el-tag>
                    <el-tag v-else size="small">{{ row.status }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="申请时间" width="170" />
                <el-table-column label="操作" width="120" fixed="right">
                  <template #default="{ row }">
                    <el-button
                      v-if="row.status === 'PENDING'"
                      type="danger"
                      link
                      size="small"
                      @click="handleCancelApply(row)"
                    >撤回</el-button>
                    <span v-else style="color:#909399;font-size:12px">—</span>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="applyPagination.total > 0" class="pagination-wrap">
                <el-pagination
                  background
                  layout="total, prev, pager, next"
                  :total="applyPagination.total"
                  :current-page="applyPagination.current"
                  :page-size="applyPagination.size"
                  @current-change="loadSentApplies"
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </template>

    <!-- ====== 学生视图 ====== -->
    <template v-else>
      <el-card shadow="never" class="main-card">
        <template #header>
          <div class="card-header">
            <span>我的指导关系</span>
            <div class="header-right">
              <el-button type="primary" @click="openApplyDialog">
                <el-icon><Plus /></el-icon>发送申请
              </el-button>
              <el-badge v-if="receivedCount > 0" :value="receivedCount" :max="99">
                <el-button @click="studentTab = 'apply-received'">
                  <el-icon><Bell /></el-icon>收到的申请
                </el-button>
              </el-badge>
            </div>
          </div>
        </template>

        <el-tabs v-model="studentTab" @tab-change="onStudentTabChange">
          <!-- 我的指导教师 -->
          <el-tab-pane label="我的指导教师" name="teacher">
            <div v-loading="loading">
              <template v-if="myTeacher">
                <el-card class="info-card teacher-card" shadow="hover">
                  <div class="card-body horizontal">
                    <div class="card-avatar">
                      <el-avatar :size="64" :style="{ backgroundColor: '#409eff' }">
                        {{ (myTeacher.teacherName || '').charAt(0) }}
                      </el-avatar>
                    </div>
                    <div class="card-info">
                      <div class="card-name">{{ myTeacher.teacherName }}</div>
                      <div class="card-sub">工号：{{ myTeacher.teacherNo || '—' }}</div>
                      <div class="card-sub">学校：{{ myTeacher.campusName || '—' }}</div>
                      <div class="card-sub">学年：{{ myTeacher.academicYear || '—' }}</div>
                      <div class="card-sub">
                        状态：
                        <el-tag v-if="myTeacher.status === 'ACTIVE'" type="success" size="small">有效</el-tag>
                        <el-tag v-else type="danger" size="small">已解除</el-tag>
                      </div>
                      <div v-if="myTeacher.defenseGroups && myTeacher.defenseGroups.length > 0" class="card-groups">
                        <div class="card-groups-label">所属答辩小组：</div>
                        <el-tag
                          v-for="g in myTeacher.defenseGroups"
                          :key="g.groupId"
                          size="small"
                          style="margin:2px"
                        >{{ g.groupName }}</el-tag>
                      </div>
                    </div>
                  </div>
                  <div class="card-footer">
                    <el-button
                      v-if="hasPermission('guidance:relation:del') && myTeacher.status === 'ACTIVE'"
                      type="danger"
                      @click="handleUnbindTeacher(myTeacher)"
                    ><el-icon><Close /></el-icon>解除绑定</el-button>
                  </div>
                </el-card>
              </template>
              <el-empty v-else description="暂无指导教师，请发送申请或等待教师邀请" />
            </div>
          </el-tab-pane>

          <!-- 答辩小组 -->
          <el-tab-pane label="我的答辩小组" name="groups">
            <div v-loading="groupLoading">
              <el-empty v-if="joinedGroups.length === 0 && !groupLoading" description="暂无加入的答辩小组" />
              <div v-else class="card-grid">
                <el-card
                  v-for="g in joinedGroups"
                  :key="g.groupId"
                  class="info-card group-card"
                  shadow="hover"
                >
                  <div class="card-body">
                    <div class="group-header">
                      <div class="group-name">{{ g.groupName }}</div>
                      <el-tag v-if="g.status === 'ACTIVE'" type="success" size="small">有效</el-tag>
                    </div>
                    <div class="card-sub">组长：{{ g.leaderName }} · {{ g.campusName }}</div>
                    <div class="card-sub">学年：{{ g.academicYear }}</div>
                    <div class="card-sub">
                      <span class="count-badge">教师 {{ g.teacherCount }}人</span>
                      <span class="count-badge">学生 {{ g.studentCount }}人</span>
                    </div>
                    <div class="group-teachers">
                      <div class="group-section-title">答辩教师</div>
                      <div class="member-list">
                        <span v-for="t in (g.teachers || [])" :key="t.userId" class="member-tag">
                          {{ t.userName }}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div class="card-footer">
                    <el-button type="primary" link size="small" @click="openGroupDetail(g)">详情</el-button>
                    <el-button
                      v-if="!g.isCreator && g.status === 'ACTIVE'"
                      type="warning"
                      link
                      size="small"
                      @click="handleQuitGroup(g)"
                    >退出小组</el-button>
                  </div>
                </el-card>
              </div>
            </div>
          </el-tab-pane>

          <!-- 收到的申请 -->
          <el-tab-pane name="apply-received">
            <template #label>
              <span>收到的申请<span v-if="receivedCount > 0" class="apply-badge">{{ receivedCount }}</span></span>
            </template>
            <div v-loading="applyLoading">
              <el-empty v-if="receivedList.length === 0 && !applyLoading" description="暂无收到的申请" />
              <el-table v-else :data="receivedList" border stripe size="small">
                <el-table-column prop="fromUserName" label="申请人" width="100" />
                <el-table-column label="身份" width="80">
                  <template #default="{ row }">{{ row.fromUserType === 'STUDENT' ? '学生' : '教师' }}</template>
                </el-table-column>
                <el-table-column prop="academicYear" label="学年" width="120" />
                <el-table-column prop="message" label="申请留言" min-width="150" show-overflow-tooltip />
                <el-table-column label="状态" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag v-if="row.status === 'PENDING'" type="warning" size="small">待处理</el-tag>
                    <el-tag v-else-if="row.status === 'APPROVED'" type="success" size="small">已同意</el-tag>
                    <el-tag v-else type="danger" size="small">已拒绝</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="申请时间" width="170" />
                <el-table-column label="操作" width="160" fixed="right">
                  <template #default="{ row }">
                    <template v-if="row.status === 'PENDING'">
                      <el-button type="success" link size="small" @click="handleApply(row, 'APPROVED')">同意</el-button>
                      <el-button type="danger" link size="small" @click="handleApply(row, 'REJECTED')">拒绝</el-button>
                    </template>
                    <span v-else style="color:#909399;font-size:12px">{{ row.handleByName ? '由' + row.handleByName + '处理' : '—' }}</span>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="applyPagination.total > 0" class="pagination-wrap">
                <el-pagination
                  background
                  layout="total, prev, pager, next"
                  :total="applyPagination.total"
                  :current-page="applyPagination.current"
                  :page-size="applyPagination.size"
                  @current-change="loadReceivedApplies"
                />
              </div>
            </div>
          </el-tab-pane>

          <!-- 发出的申请 -->
          <el-tab-pane label="发出的申请" name="apply-sent">
            <div v-loading="applyLoading">
              <el-empty v-if="sentList.length === 0 && !applyLoading" description="暂无发出的申请" />
              <el-table v-else :data="sentList" border stripe size="small">
                <el-table-column prop="toUserName" label="接收方" width="100" />
                <el-table-column label="对方身份" width="80">
                  <template #default="{ row }">{{ row.toUserType === 'STUDENT' ? '学生' : '教师' }}</template>
                </el-table-column>
                <el-table-column prop="academicYear" label="学年" width="120" />
                <el-table-column prop="message" label="申请留言" min-width="150" show-overflow-tooltip />
                <el-table-column label="状态" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag v-if="row.status === 'PENDING'" type="warning" size="small">待处理</el-tag>
                    <el-tag v-else-if="row.status === 'APPROVED'" type="success" size="small">已同意</el-tag>
                    <el-tag v-else type="danger" size="small">已拒绝</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="申请时间" width="170" />
                <el-table-column label="操作" width="120" fixed="right">
                  <template #default="{ row }">
                    <el-button v-if="row.status === 'PENDING'" type="danger" link size="small" @click="handleCancelApply(row)">撤回</el-button>
                    <span v-else style="color:#909399;font-size:12px">—</span>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="applyPagination.total > 0" class="pagination-wrap">
                <el-pagination
                  background
                  layout="total, prev, pager, next"
                  :total="applyPagination.total"
                  :current-page="applyPagination.current"
                  :page-size="applyPagination.size"
                  @current-change="loadSentApplies"
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </template>

    <!-- ====== 发送申请对话框 ====== -->
    <el-dialog v-model="applyDialogVisible" title="发送指导关系申请" width="500px" destroy-on-close>
      <el-form :model="applyForm" label-width="80px">
        <el-form-item label="申请对象">
          <el-select
            v-model="applyForm.toUserId"
            placeholder="输入姓名或学号/工号搜索"
            filterable
            remote
            :remote-method="searchTargetUser"
            :loading="userSearchLoading"
            style="width:100%"
          >
            <el-option
              v-for="u in targetUserOptions"
              :key="u.userId"
              :label="buildUserLabel(u)"
              :value="u.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学年">
          <el-input v-model="applyForm.academicYear" placeholder="如 2024-2025" style="width:100%" />
        </el-form-item>
        <el-form-item label="留言">
          <el-input
            v-model="applyForm.message"
            type="textarea"
            :rows="3"
            placeholder="可选：附上申请留言"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="applyLoading" @click="handleSendApply">发送申请</el-button>
      </template>
    </el-dialog>

    <!-- ====== 创建答辩小组对话框 ====== -->
    <el-dialog
      v-model="createGroupVisible"
      :title="isEditGroup ? '编辑答辩小组' : '创建答辩小组'"
      width="700px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-steps :active="createGroupStep" finish-status="success" style="margin-bottom:24px">
        <el-step title="选择答辩教师" />
        <el-step title="添加学生成员" />
      </el-steps>

      <!-- 步骤1：选教师 -->
      <div v-show="createGroupStep === 0">
        <el-form :inline="true" style="margin-bottom:12px">
          <el-form-item label="小组名称">
            <el-input v-model="createGroupForm.groupName" placeholder="请输入小组名称" style="width:200px" />
          </el-form-item>
          <el-form-item label="学年">
            <el-input v-model="createGroupForm.academicYear" placeholder="如 2024-2025" style="width:160px" />
          </el-form-item>
        </el-form>
        <el-form label-width="0">
          <el-form-item>
            <el-input
              v-model="teacherSearch"
              placeholder="输入工号或姓名搜索教师"
              clearable
              style="width:300px"
              @input="searchTeachers"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </el-form-item>
        </el-form>
        <el-table
          ref="teacherTableRef"
          :data="teacherOptions"
          border
          stripe
          size="small"
          max-height="260"
          @selection-change="onTeacherSelectionChange"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column prop="realName" label="姓名" width="100" />
          <el-table-column prop="teacherNo" label="工号" width="120" />
          <el-table-column prop="campusName" label="学校" show-overflow-tooltip />
        </el-table>
        <el-alert type="info" :closable="false" show-icon style="margin-top:12px">
          <template #title>已选择 {{ selectedTeachers.length }} 位教师（当前登录教师必须选中）</template>
        </el-alert>
      </div>

      <!-- 步骤2：加学生 -->
      <div v-show="createGroupStep === 1">
        <el-alert type="success" :closable="false" show-icon style="margin-bottom:16px">
          <template #title>已选择 {{ selectedTeachers.length }} 位答辩教师 · 学校：{{ currentCampus }}</template>
        </el-alert>
        <el-form label-width="0" inline style="margin-bottom:8px">
          <el-form-item label="输入学号">
            <el-input
              v-model="studentInput"
              placeholder="输入学生学号后回车添加"
              style="width:480px"
              @keydown.enter.prevent="addStudentsByInput"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="addStudentsByInput">添加</el-button>
          </el-form-item>
        </el-form>
        <div style="color:#909399;font-size:12px;margin-bottom:8px">支持批量粘贴多个学号，逗号、空格或换行分隔</div>
        <el-table :data="selectedStudents" border stripe size="small" max-height="280" empty-text="暂未添加学生">
          <el-table-column prop="studentNo" label="学号" width="140" />
          <el-table-column prop="studentName" label="姓名" width="120" />
          <el-table-column prop="campusName" label="学校" show-overflow-tooltip />
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ $index }">
              <el-button type="danger" link size="small" @click="removeStudent($index)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-alert v-if="addStudentError" type="warning" :closable="true" @close="addStudentError = ''" style="margin-top:8px">
          {{ addStudentError }}
        </el-alert>
      </div>

      <template #footer>
        <el-button v-if="createGroupStep > 0" @click="createGroupStep--">上一步</el-button>
        <el-button v-if="createGroupStep === 0 && selectedTeachers.length > 0" type="primary" @click="nextCreateGroupStep">下一步</el-button>
        <el-button v-if="createGroupStep === 1" type="primary" :loading="submitLoading" @click="handleCreateGroup">{{ isEditGroup ? '保存修改' : '确认创建' }}</el-button>
        <el-button @click="createGroupVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- ====== 详情对话框 ====== -->
    <el-dialog v-model="detailVisible" :title="detailType === 'relation' ? '指导关系详情' : '答辩小组详情'" width="640px" destroy-on-close>
      <!-- 指导关系详情 -->
      <template v-if="detailType === 'relation' && currentRow">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="指导教师">{{ currentRow.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="工号">{{ currentRow.teacherNo }}</el-descriptions-item>
          <el-descriptions-item label="学生">{{ currentRow.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentRow.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="学校">{{ currentRow.campusName }}</el-descriptions-item>
          <el-descriptions-item label="学年">{{ currentRow.academicYear }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="currentRow.status === 'ACTIVE'" type="success" size="small">有效</el-tag>
            <el-tag v-else type="danger" size="small">已解除</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建人">{{ currentRow.createByName }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ currentRow.createTime }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <!-- 答辩小组详情 -->
      <template v-if="detailType === 'group' && currentRow">
        <el-descriptions :column="2" border size="small" style="margin-bottom:16px">
          <el-descriptions-item label="小组名称" :span="2">{{ currentRow.groupName }}</el-descriptions-item>
          <el-descriptions-item label="组长">{{ currentRow.leaderName }}</el-descriptions-item>
          <el-descriptions-item label="学校">{{ currentRow.campusName }}</el-descriptions-item>
          <el-descriptions-item label="学年">{{ currentRow.academicYear }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="currentRow.status === 'ACTIVE'" type="success" size="small">有效</el-tag>
            <el-tag v-else size="small">已解散</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <div class="detail-section-title">答辩教师（{{ currentRow.teachers?.length || 0 }}人）</div>
        <el-table :data="currentRow.teachers" border size="small" max-height="200" style="margin-bottom:16px">
          <el-table-column prop="userName" label="姓名" width="120" />
          <el-table-column prop="teacherNo" label="工号" width="140" />
          <el-table-column label="角色">
            <template #default="{ row }">
              <el-tag v-if="row.userId === currentRow.leaderId" type="warning" size="small">组长</el-tag>
              <el-tag v-else size="small">成员</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div class="detail-section-title">答辩学生（{{ currentRow.students?.length || 0 }}人）</div>
        <el-table :data="currentRow.students" border size="small" max-height="200">
          <el-table-column prop="studentName" label="姓名" width="120" />
          <el-table-column prop="studentNo" label="学号" width="140" />
          <el-table-column label="课题" show-overflow-tooltip>
            <template #default="{ row }">{{ row.topicName || '—' }}</template>
          </el-table-column>
        </el-table>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Bell, Close, Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  fetchTeacherStudentPage,
  fetchTeacherCreatedGroups,
  fetchTeacherJoinedGroups,
  fetchStudentRelation,
  fetchStudentGroups,
  deleteGuidanceRelation,
  fetchRelationDetail,
  fetchDefenseGroupDetail,
  sendRelationApply,
  fetchReceivedApplies,
  fetchSentApplies,
  handleRelationApply,
  cancelRelationApply,
  createDefenseGroup,
  updateDefenseGroup,
  fetchTeacherDefenseGroupPage,
} from '@/api/guidance'
import { fetchUserList } from '@/api/system'

const store = useUserStore()

const isStudent = computed(() => store.user?.userType === 1)
const currentCampus = computed(() => store.user?.campusName || '')
const currentUserId = computed(() => store.user?.userId)

function hasPermission(code) {
  return store.hasPerm(code)
}

const avatarColors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#c71585']

// ====== 状态 ======
const activeTab = ref('students')
const studentTab = ref('teacher')
const groupSubTab = ref('created')
const loading = ref(false)
const groupLoading = ref(false)
const applyLoading = ref(false)
const submitLoading = ref(false)

// ====== 查询条件 ======
const query = reactive({
  academicYear: '',
  keyword: '',
})

// ====== 学生卡片列表 ======
const cardList = ref([])
const pagination = reactive({ total: 0, current: 1, size: 12 })

// ====== 答辩小组 ======
const createdGroups = ref([])
const joinedGroups = ref([])

// ====== 全部小组管理 ======
const allGroupLoading = ref(false)
const allGroupList = ref([])
const allGroupQuery = reactive({ academicYear: '', keyword: '' })
const allGroupPagination = reactive({ total: 0, current: 1, size: 10 })

// ====== 申请列表 ======
const receivedList = ref([])
const receivedCount = ref(0)
const sentList = ref([])
const applyPagination = reactive({ total: 0, current: 1, size: 10 })

// ====== 我的教师（学生视图） ======
const myTeacher = ref(null)

// ====== 详情 ======
const detailVisible = ref(false)
const detailType = ref('relation')
const currentRow = ref(null)

// ====== 发送申请 ======
const applyDialogVisible = ref(false)
const userSearchLoading = ref(false)
const targetUserOptions = ref([])
const applyForm = ref({
  toUserId: null,
  academicYear: '',
  message: '',
})

// ====== 创建答辩小组 ======
const createGroupVisible = ref(false)
const isEditGroup = ref(false)
const selectedGroupId = ref(null)
const createGroupStep = ref(0)
const createGroupForm = ref({ groupName: '', academicYear: '' })
const teacherSearch = ref('')
const teacherOptions = ref([])
const selectedTeachers = ref([])
const teacherTableRef = ref(null)
const studentInput = ref('')
const selectedStudents = ref([])
const addStudentError = ref('')

// ====== 用户缓存 ======
const userCache = ref({})

// ====== 加载教师学生卡片 ======
async function loadStudentCards(page) {
  loading.value = true
  try {
    const params = { current: page || pagination.current, size: pagination.size, ...query }
    const res = await fetchTeacherStudentPage(params)
    if (res) {
      cardList.value = res.records || []
      pagination.total = res.total || 0
      pagination.current = res.current || 1
      // 缓存用户信息
      for (const item of cardList.value) {
        if (item.studentId) userCache.value[item.studentId] = { studentNo: item.studentNo }
        if (item.teacherId) userCache.value[item.teacherId] = { teacherNo: item.teacherNo }
      }
    }
  } catch {} finally { loading.value = false }
}

// ====== 加载答辩小组 ======
async function loadCreatedGroups() {
  groupLoading.value = true
  try {
    const res = await fetchTeacherCreatedGroups(query.academicYear)
    createdGroups.value = res || []
  } catch {} finally { groupLoading.value = false }
}

async function loadJoinedGroups() {
  groupLoading.value = true
  try {
    const res = await fetchTeacherJoinedGroups(query.academicYear)
    joinedGroups.value = res || []
  } catch {} finally { groupLoading.value = false }
}

async function loadAllGroups(page) {
  allGroupLoading.value = true
  try {
    const res = await fetchTeacherDefenseGroupPage({
      current: page || allGroupPagination.current,
      size: allGroupPagination.size,
      ...allGroupQuery,
    })
    if (res) {
      allGroupList.value = res.records || []
      allGroupPagination.total = res.total || 0
      allGroupPagination.current = res.current || 1
    }
  } catch {} finally { allGroupLoading.value = false }
}

// ====== 加载学生视图 ======
async function loadStudentTeacher() {
  loading.value = true
  try {
    const res = await fetchStudentRelation(query.academicYear)
    myTeacher.value = res
  } catch {} finally { loading.value = false }
}

async function loadStudentGroups() {
  groupLoading.value = true
  try {
    const res = await fetchStudentGroups(query.academicYear)
    joinedGroups.value = res || []
  } catch {} finally { groupLoading.value = false }
}

// ====== 加载申请列表 ======
async function loadReceivedApplies(page) {
  applyLoading.value = true
  try {
    const params = { current: page || applyPagination.current, size: applyPagination.size }
    const res = await fetchReceivedApplies(params)
    if (res) {
      receivedList.value = res.records || []
      applyPagination.total = res.total || 0
      applyPagination.current = res.current || 1
      receivedCount.value = applyPagination.total
    }
  } catch {} finally { applyLoading.value = false }
}

async function loadSentApplies(page) {
  applyLoading.value = true
  try {
    const params = { current: page || applyPagination.current, size: applyPagination.size }
    const res = await fetchSentApplies(params)
    if (res) {
      sentList.value = res.records || []
      applyPagination.total = res.total || 0
      applyPagination.current = res.current || 1
    }
  } catch {} finally { applyLoading.value = false }
}

async function loadReceivedCount() {
  try {
    const res = await fetchReceivedApplies({ current: 1, size: 1 })
    receivedCount.value = res?.total || 0
  } catch { receivedCount.value = 0 }
}

// ====== 标签页切换 ======
function onTabChange(tab) {
  if (tab === 'students') loadStudentCards(1)
  else if (tab === 'groups') {
    if (groupSubTab.value === 'created') loadCreatedGroups()
    else loadJoinedGroups()
  } else if (tab === 'all-groups') loadAllGroups(1)
  else if (tab === 'apply-received') loadReceivedApplies(1)
  else if (tab === 'apply-sent') loadSentApplies(1)
}

function onGroupTabChange() {
  if (groupSubTab.value === 'created') loadCreatedGroups()
  else loadJoinedGroups()
}

function onStudentTabChange(tab) {
  if (tab === 'teacher') loadStudentTeacher()
  else if (tab === 'groups') loadStudentGroups()
  else if (tab === 'apply-received') loadReceivedApplies(1)
  else if (tab === 'apply-sent') loadSentApplies(1)
}

function handleSearch() {
  pagination.current = 1
  if (!isStudent.value) {
    if (activeTab.value === 'students') loadStudentCards(1)
    else if (activeTab.value === 'groups') {
      if (groupSubTab.value === 'created') loadCreatedGroups()
      else loadJoinedGroups()
    }
  }
}

function handleReset() {
  query.academicYear = ''
  query.keyword = ''
  pagination.current = 1
  handleSearch()
}

// ====== 详情 ======
async function openDetail(item) {
  try {
    const data = await fetchRelationDetail(item.relationId)
    currentRow.value = data
    detailType.value = 'relation'
    detailVisible.value = true
  } catch (e) { ElMessage.error(e.message || '获取详情失败') }
}

async function openGroupDetail(g) {
  try {
    const data = await fetchDefenseGroupDetail(g.groupId)
    currentRow.value = data
    detailType.value = 'group'
    detailVisible.value = true
  } catch (e) { ElMessage.error(e.message || '获取详情失败') }
}

// ====== 解除绑定 ======
async function handleUnbindStudent(item) {
  await ElMessageBox.confirm(
    `确定要解除与「${item.studentName}」的指导关系吗？系统将通知该学生。`,
    '解除确认', { type: 'warning' }
  )
  try {
    await deleteGuidanceRelation(item.relationId)
    ElMessage.success('已解除，系统将通知学生')
    loadStudentCards(1)
  } catch (e) { ElMessage.error(e.message || '操作失败') }
}

async function handleUnbindTeacher(item) {
  await ElMessageBox.confirm(
    '确定要解除与指导教师的绑定关系吗？系统将通知该教师。',
    '解除绑定', { type: 'warning' }
  )
  try {
    await deleteGuidanceRelation(item.relationId)
    ElMessage.success('已解除绑定，系统将通知教师')
    loadStudentTeacher()
  } catch (e) { ElMessage.error(e.message || '操作失败') }
}

// ====== 解散 / 退出答辩小组 ======
async function handleDissolveGroup(g) {
  await ElMessageBox.confirm(`确定要解散答辩小组「${g.groupName}」吗？解散后所有成员将被移除。`, '解散小组', { type: 'warning' })
  try {
    const { deleteDefenseGroup } = await import('@/api/guidance')
    await deleteDefenseGroup(g.groupId)
    ElMessage.success('小组已解散')
    loadCreatedGroups()
    loadAllGroups(1)
  } catch (e) { ElMessage.error(e.message || '操作失败') }
}

async function handleQuitGroup(g) {
  await ElMessageBox.confirm('确定要退出该答辩小组吗？', '退出小组', { type: 'warning' })
  try {
    const { deleteDefenseGroup } = await import('@/api/guidance')
    await deleteDefenseGroup(g.groupId)
    ElMessage.success('您已退出该小组')
    if (!isStudent.value) {
      if (groupSubTab.value === 'joined') loadJoinedGroups()
    } else {
      loadStudentGroups()
    }
  } catch (e) { ElMessage.error(e.message || '操作失败') }
}

// ====== 发送申请 ======
async function searchTargetUser(q) {
  if (!q || q.length < 1) { targetUserOptions.value = []; return }
  userSearchLoading.value = true
  try {
    const userType = isStudent.value ? 2 : 1
    const res = await fetchUserList({ userType, keyword: q, size: 20 })
    targetUserOptions.value = res?.records || []
  } catch { targetUserOptions.value = [] }
  finally { userSearchLoading.value = false }
}

function buildUserLabel(u) {
  if (u.userType === 1) return `${u.realName}（${u.studentNo || '无学号'}）`
  return `${u.realName}（${u.teacherNo || '无工号'}）`
}

function openApplyDialog() {
  applyForm.value = { toUserId: null, academicYear: currentCampus.value, message: '' }
  targetUserOptions.value = []
  applyDialogVisible.value = true
}

async function handleSendApply() {
  if (!applyForm.value.toUserId) { ElMessage.warning('请选择申请对象'); return }
  applyLoading.value = true
  try {
    await sendRelationApply(applyForm.value)
    ElMessage.success('申请已发送，请等待对方处理')
    applyDialogVisible.value = false
  } catch (e) { ElMessage.error(e.message || '发送失败') }
  finally { applyLoading.value = false }
}

// ====== 处理申请 ======
async function handleApply(row, status) {
  const action = status === 'APPROVED' ? '同意' : '拒绝'
  await ElMessageBox.confirm(`确定要${action}该申请吗？${status === 'APPROVED' ? '将建立指导关系。' : ''}`, `${action}申请`, { type: status === 'APPROVED' ? 'success' : 'warning' })
  try {
    await handleRelationApply({ applyId: row.applyId, status })
    ElMessage.success(`已${action}`)
    loadReceivedApplies(1)
    loadReceivedCount()
    if (!isStudent.value) loadStudentCards(1)
    else loadStudentTeacher()
  } catch (e) { ElMessage.error(e.message || '操作失败') }
}

// ====== 撤回申请 ======
async function handleCancelApply(row) {
  await ElMessageBox.confirm('确定要撤回该申请吗？', '撤回申请', { type: 'warning' })
  try {
    await cancelRelationApply(row.applyId)
    ElMessage.success('已撤回')
    loadSentApplies(1)
  } catch (e) { ElMessage.error(e.message || '操作失败') }
}

// ====== 创建答辩小组 ======
async function searchTeachers(q) {
  if (!q || q.length < 1) { teacherOptions.value = []; return }
  try {
    const res = await fetchUserList({ userType: 2, keyword: q, size: 50 })
    teacherOptions.value = res?.records || []
    await nextTick()
    if (teacherTableRef.value && teacherOptions.value.length > 0) {
      const idx = teacherOptions.value.findIndex(t => t.userId === currentUserId.value)
      if (idx >= 0) teacherTableRef.value.toggleRowSelection(teacherOptions.value[idx], true)
    }
  } catch { teacherOptions.value = [] }
}

function onTeacherSelectionChange(selection) {
  selectedTeachers.value = selection
}

function nextCreateGroupStep() {
  if (!createGroupForm.value.groupName) { ElMessage.warning('请输入小组名称'); return }
  if (!createGroupForm.value.academicYear) { ElMessage.warning('请输入学年'); return }
  const hasSelf = selectedTeachers.value.some(t => t.userId === currentUserId.value)
  if (!hasSelf) { ElMessage.warning('您必须将自己加入答辩小组'); return }
  createGroupStep.value = 1
}

async function addStudentsByInput() {
  if (!studentInput.value.trim()) return
  addStudentError.value = ''
  const tokens = studentInput.value.split(/[,\s\n]+/).map(s => s.trim()).filter(s => s.length > 0)
  if (tokens.length === 0) return
  const newStudents = []
  const already = []
  const notFound = []
  for (const no of tokens) {
    const exist = selectedStudents.value.find(s => s.studentNo === no)
    if (exist) { already.push(no); continue }
    try {
      const res = await fetchUserList({ userType: 1, studentNo: no, size: 1 })
      if (res?.records && res.records.length > 0) {
        const stu = res.records[0]
        if (stu.campusName !== currentCampus.value) { notFound.push(`${no}（不属于本校）`); continue }
        newStudents.push(stu)
      } else { notFound.push(`${no}（未找到）`) }
    } catch { notFound.push(`${no}（查询失败）`) }
  }
  for (const stu of newStudents) {
    if (!selectedStudents.value.find(s => s.userId === stu.userId)) {
      selectedStudents.value.push({ userId: stu.userId, studentName: stu.realName, studentNo: stu.studentNo, campusName: stu.campusName })
    }
  }
  studentInput.value = ''
  let msg = ''
  if (newStudents.length > 0) msg += `成功添加 ${newStudents.length} 名；`
  if (already.length > 0) msg += `${already.join(', ')} 已存在；`
  if (notFound.length > 0) msg += notFound.join(', ') + '。'
  if (msg) ElMessage.info(msg.trim())
}

function removeStudent(index) {
  selectedStudents.value.splice(index, 1)
}

function openCreateGroupDialog() {
  isEditGroup.value = false
  createGroupStep.value = 0
  createGroupForm.value = { groupName: '', academicYear: currentCampus.value }
  selectedTeachers.value = []
  selectedStudents.value = []
  teacherOptions.value = []
  teacherSearch.value = ''
  studentInput.value = ''
  addStudentError.value = ''
  createGroupVisible.value = true
}

function openEditGroupDialog(row) {
  isEditGroup.value = true
  selectedGroupId.value = row.groupId
  createGroupStep.value = 0
  createGroupForm.value = {
    groupName: row.groupName,
    academicYear: row.academicYear || '',
  }
  selectedTeachers.value = (row.teachers || []).map(t => ({
    userId: t.userId,
    realName: t.userName,
    teacherNo: t.teacherNo,
    campusName: row.campusName,
  }))
  selectedStudents.value = (row.students || []).map(s => ({
    userId: s.studentId,
    studentName: s.studentName,
    studentNo: s.studentNo,
    campusName: s.campusName || row.campusName,
  }))
  teacherSearch.value = ''
  teacherOptions.value = []
  addStudentError.value = ''
  createGroupVisible.value = true
}

async function handleCreateGroup() {
  submitLoading.value = true
  try {
    const payload = {
      groupName: createGroupForm.value.groupName,
      academicYear: createGroupForm.value.academicYear,
      teacherIds: selectedTeachers.value.map(t => t.userId),
      studentIds: selectedStudents.value.map(s => s.userId),
    }
    if (isEditGroup.value) {
      payload.groupId = selectedGroupId.value
      await updateDefenseGroup(payload)
      ElMessage.success('修改成功')
    } else {
      await createDefenseGroup(payload)
      ElMessage.success('创建成功')
    }
    createGroupVisible.value = false
    loadCreatedGroups()
    loadAllGroups(1)
  } catch (e) { ElMessage.error(e.message || '操作失败') }
  finally { submitLoading.value = false }
}

// ====== 工具方法 ======
function getStudentNo(userId) {
  return userCache.value[userId]?.studentNo || '—'
}
function getTeacherNo(userId) {
  return userCache.value[userId]?.teacherNo || '—'
}

// ====== 初始化 ======
onMounted(() => {
  if (isStudent.value) {
    loadStudentTeacher()
    loadReceivedCount()
  } else {
    loadStudentCards(1)
    loadReceivedCount()
  }
})
</script>

<style scoped>
.main-card {
  border-radius: 10px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.filter-bar {
  margin-bottom: 16px;
}
.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}
.info-card {
  border-radius: 10px;
}
.card-body {
  padding: 4px 0;
}
.card-body.horizontal {
  display: flex;
  align-items: center;
  gap: 20px;
}
.card-avatar {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;
}
.card-info {
  flex: 1;
}
.card-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}
.card-sub {
  font-size: 13px;
  color: #606266;
  margin-bottom: 3px;
  line-height: 1.4;
}
.card-groups {
  margin-top: 8px;
}
.card-groups-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.card-footer {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.group-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.count-badge {
  display: inline-block;
  background: #f0f9eb;
  color: #67c23a;
  border-radius: 4px;
  padding: 1px 6px;
  font-size: 12px;
  margin-right: 6px;
}
.group-section-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
  margin-top: 10px;
}
.member-list {
  font-size: 13px;
  color: #606266;
}
.member-tag {
  display: inline-block;
  background: #f4f4f5;
  border-radius: 4px;
  padding: 2px 8px;
  margin: 2px 4px 2px 0;
}
.teacher-no, .stu-no {
  color: #909399;
  font-size: 12px;
  margin-left: 2px;
}
.topic-name {
  color: #409eff;
  font-size: 12px;
  margin-left: 4px;
}
.no-data {
  color: #c0c4cc;
  font-style: italic;
}
.teacher-card {
  max-width: 500px;
}
.apply-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #f56c6c;
  color: #fff;
  border-radius: 10px;
  font-size: 10px;
  padding: 2px 5px;
  margin-left: 4px;
}
.detail-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
.group-card .card-body {
  padding: 0;
}
</style>
