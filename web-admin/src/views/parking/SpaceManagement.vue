<template>
  <div class="space-management">
    <el-card>
      <div slot="header" class="clearfix">
        <span>车位管理</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleRefresh">
          刷新
        </el-button>
      </div>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="车位编号">
          <el-input v-model="searchForm.spaceNo" placeholder="请输入车位编号" clearable></el-input>
        </el-form-item>
        <el-form-item label="车位类型">
          <el-select v-model="searchForm.spaceType" placeholder="请选择" clearable>
            <el-option label="小型车" :value="1"></el-option>
            <el-option label="大型车" :value="2"></el-option>
            <el-option label="无障碍" :value="3"></el-option>
            <el-option label="充电车位" :value="4"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="空闲" :value="1"></el-option>
            <el-option label="占用" :value="0"></el-option>
            <el-option label="锁定" :value="2"></el-option>
            <el-option label="预约" :value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增车位</el-button>
        <el-button type="success" @click="handleBatchAdd">批量导入</el-button>
        <el-button type="warning" @click="handleExport">导出</el-button>
      </div>

      <!-- 车位列表 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="spaceNo" label="车位编号" width="120"></el-table-column>
        <el-table-column prop="zoneName" label="所属区域" width="150"></el-table-column>
        <el-table-column prop="spaceType" label="车位类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="getSpaceTypeTag(scope.row.spaceType)">
              {{ getSpaceTypeText(scope.row.spaceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="occupiedByPlate" label="占用车牌" width="120"></el-table-column>
        <el-table-column prop="widthCm" label="宽度 (cm)" width="80"></el-table-column>
        <el-table-column prop="lengthCm" label="长度 (cm)" width="80"></el-table-column>
        <el-table-column label="操作" fixed="right" width="280">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.status === 1"
              type="primary"
              size="mini"
              @click="handleOccupy(scope.row)"
            >
              占用
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="success"
              size="mini"
              @click="handleRelease(scope.row)"
            >
              释放
            </el-button>
            <el-button type="info" size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="mini" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.pageNum"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
      >
      </el-pagination>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px" @close="handleDialogClose">
      <el-form ref="form" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="车位编号" prop="spaceNo">
          <el-input v-model="formData.spaceNo" placeholder="请输入车位编号" maxlength="20"></el-input>
        </el-form-item>
        <el-form-item label="所属区域" prop="zoneId">
          <el-select v-model="formData.zoneId" placeholder="请选择区域" style="width: 100%">
            <el-option
              v-for="zone in zoneOptions"
              :key="zone.id"
              :label="zone.name"
              :value="zone.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="车位类型" prop="spaceType">
          <el-radio-group v-model="formData.spaceType">
            <el-radio :label="1">小型车</el-radio>
            <el-radio :label="2">大型车</el-radio>
            <el-radio :label="3">无障碍</el-radio>
            <el-radio :label="4">充电车位</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="车位尺寸 (cm)">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-input v-model="formData.widthCm" placeholder="宽度"></el-input>
            </el-col>
            <el-col :span="8">
              <el-input v-model="formData.lengthCm" placeholder="长度"></el-input>
            </el-col>
            <el-col :span="8">
              <el-input v-model="formData.heightLimitCm" placeholder="限高"></el-input>
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </span>
    </el-dialog>

    <!-- 占用车位对话框 -->
    <el-dialog title="占用车位" :visible.sync="occupyDialogVisible" width="500px">
      <el-form ref="occupyForm" :model="occupyForm" label-width="100px">
        <el-form-item label="车牌号" required>
          <el-input v-model="occupyForm.plateNo" placeholder="请输入车牌号"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="occupyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmOccupy">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { querySpaces, createSpace, updateSpaceStatus, occupySpace, releaseSpace } from '@/api/space'
import { getZoneTree } from '@/api/zone'

export default {
  name: 'SpaceManagement',
  data() {
    return {
      searchForm: {
        spaceNo: '',
        spaceType: null,
        status: null
      },
      tableData: [],
      loading: false,
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0
      },
      dialogVisible: false,
      dialogTitle: '',
      submitting: false,
      formData: {},
      formRules: {
        spaceNo: [
          { required: true, message: '请输入车位编号', trigger: 'blur' }
        ],
        zoneId: [
          { required: true, message: '请选择所属区域', trigger: 'change' }
        ],
        spaceType: [
          { required: true, message: '请选择车位类型', trigger: 'change' }
        ]
      },
      zoneOptions: [],
      occupyDialogVisible: false,
      currentSpace: null,
      occupyForm: {}
    }
  },
  created() {
    this.loadZones()
    this.loadData()
  },
  methods: {
    async loadZones() {
      try {
        const res = await getZoneTree(1) // TODO: 从路由获取 parkingLotId
        this.zoneOptions = res.data || []
      } catch (error) {
        this.$message.error('加载区域失败：' + error.message)
      }
    },
    async loadData() {
      this.loading = true
      try {
        const params = {
          ...this.searchForm,
          pageNum: this.pagination.pageNum,
          pageSize: this.pagination.pageSize
        }
        const res = await querySpaces(params)
        this.tableData = res.data.list
        this.pagination.total = res.data.total
      } catch (error) {
        this.$message.error('加载数据失败：' + error.message)
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.pagination.pageNum = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = {
        spaceNo: '',
        spaceType: null,
        status: null
      }
      this.handleSearch()
    },
    handleRefresh() {
      this.loadData()
    },
    handleAdd() {
      this.formData = {
        spaceType: 1
      }
      this.dialogTitle = '新增车位'
      this.dialogVisible = true
    },
    handleBatchAdd() {
      this.$message.info('批量导入功能开发中...')
    },
    handleExport() {
      this.$message.info('导出功能开发中...')
    },
    handleEdit(row) {
      this.formData = { ...row }
      this.dialogTitle = '编辑车位'
      this.dialogVisible = true
    },
    handleDelete(row) {
      this.$confirm(`确认删除车位 "${row.spaceNo}"？`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$message.success('删除成功')
        this.loadData()
      })
    },
    handleOccupy(row) {
      this.currentSpace = row
      this.occupyForm = {}
      this.occupyDialogVisible = true
    },
    async handleConfirmOccupy() {
      if (!this.occupyForm.plateNo) {
        this.$message.warning('请输入车牌号')
        return
      }
      try {
        await occupySpace(this.currentSpace.id, this.occupyForm.plateNo)
        this.$message.success('占用成功')
        this.occupyDialogVisible = false
        this.loadData()
      } catch (error) {
        this.$message.error('占用失败：' + error.message)
      }
    },
    async handleRelease(row) {
      try {
        await releaseSpace(row.id)
        this.$message.success('释放成功')
        this.loadData()
      } catch (error) {
        this.$message.error('释放失败：' + error.message)
      }
    },
    handleSubmit() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return

        this.submitting = true
        try {
          await createSpace(this.formData.zoneId, this.formData)
          this.$message.success('创建成功')
          this.dialogVisible = false
          this.loadData()
        } catch (error) {
          this.$message.error('操作失败：' + error.message)
        } finally {
          this.submitting = false
        }
      })
    },
    handleDialogClose() {
      this.$refs.form.resetFields()
      this.formData = {}
    },
    handleSizeChange(val) {
      this.pagination.pageSize = val
      this.loadData()
    },
    handleCurrentChange(val) {
      this.pagination.pageNum = val
      this.loadData()
    },
    getSpaceTypeText(type) {
      const map = { 1: '小型车', 2: '大型车', 3: '无障碍', 4: '充电车位' }
      return map[type] || ''
    },
    getSpaceTypeTag(type) {
      const map = { 1: '', 2: 'warning', 3: 'success', 4: 'danger' }
      return map[type] || ''
    },
    getStatusText(status) {
      const map = { 0: '占用', 1: '空闲', 2: '锁定', 3: '预约' }
      return map[status] || ''
    },
    getStatusTag(status) {
      const map = { 0: 'danger', 1: 'success', 2: 'info', 3: 'warning' }
      return map[status] || ''
    }
  }
}
</script>

<style scoped>
.space-management {
  padding: 20px;
}

.clearfix::after {
  content: "";
  display: table;
  clear: both;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
