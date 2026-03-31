<template>
  <div class="zone-management">
    <el-card>
      <div slot="header" class="clearfix">
        <span>区域管理</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleRefresh">
          刷新
        </el-button>
      </div>

      <!-- 树形结构 -->
      <ZoneTree
        v-if="treeData.length > 0"
        :tree-data="treeData"
        :parking-lot-id="parkingLotId"
        @add="handleAdd"
        @edit="handleEdit"
        @delete="handleDelete"
      />
      <el-empty v-else description="暂无区域数据">
        <el-button type="primary" @click="handleAdd(null)">创建第一个区域</el-button>
      </el-empty>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form ref="form" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="区域名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入区域名称" maxlength="50"></el-input>
        </el-form-item>
        <el-form-item label="区域编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入区域编码（大写字母、数字）" maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="区域类型" prop="zoneType">
          <el-radio-group v-model="formData.zoneType">
            <el-radio :label="1">主区域</el-radio>
            <el-radio :label="2">子区域</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="区域分类" prop="zoneCategory">
          <el-select v-model="formData.zoneCategory" placeholder="请选择区域分类">
            <el-option label="商场区" :value="1"></el-option>
            <el-option label="办公区" :value="2"></el-option>
            <el-option label="酒店区" :value="3"></el-option>
            <el-option label="住宅区" :value="4"></el-option>
            <el-option label="充电区" :value="5"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="楼层" prop="floorLevel">
          <el-input-number v-model="formData.floorLevel" :min="-10" :max="100" :step="1"></el-input-number>
          <span style="margin-left: 10px; color: #999;">负数表示地下楼层</span>
        </el-form-item>
        <el-form-item label="独立出口" prop="hasIndependentExit">
          <el-switch v-model="formData.hasIndependentExit"></el-switch>
        </el-form-item>
        <el-form-item label="备注" prop="config">
          <el-input
            v-model="formData.configText"
            type="textarea"
            :rows="3"
            placeholder='JSON 格式，如：{"min_height": 2.0}'
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import ZoneTree from '@/components/ZoneTree'
import { getZoneTree, createZone, updateZone, deleteZone } from '@/api/zone'

export default {
  name: 'ZoneManagement',
  components: {
    ZoneTree
  },
  data() {
    return {
      parkingLotId: 1, // TODO: 从路由参数或用户选择获取
      treeData: [],
      dialogVisible: false,
      dialogTitle: '',
      submitting: false,
      formData: {},
      formRules: {
        name: [
          { required: true, message: '请输入区域名称', trigger: 'blur' },
          { max: 50, message: '长度不能超过 50 个字符', trigger: 'blur' }
        ],
        code: [
          { required: true, message: '请输入区域编码', trigger: 'blur' },
          { pattern: /^[A-Z0-9_-]+$/, message: '只能包含大写字母、数字、下划线和短横线', trigger: 'blur' }
        ],
        zoneType: [
          { required: true, message: '请选择区域类型', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.loadZoneTree()
  },
  methods: {
    async loadZoneTree() {
      try {
        const res = await getZoneTree(this.parkingLotId)
        this.treeData = res.data || []
      } catch (error) {
        this.$message.error('加载区域树失败：' + error.message)
      }
    },
    handleRefresh() {
      this.loadZoneTree()
    },
    handleAdd(parentData) {
      this.formData = {
        zoneType: parentData ? 2 : 1,
        parentZoneId: parentData ? parentData.id : null,
        hasIndependentExit: false
      }
      this.dialogTitle = parentData ? `添加子区域到 "${parentData.name}"` : '添加主区域'
      this.dialogVisible = true
    },
    handleEdit(data) {
      this.formData = {
        ...data,
        configText: data.config ? JSON.stringify(data.config) : ''
      }
      this.dialogTitle = `编辑区域 "${data.name}"`
      this.dialogVisible = true
    },
    async handleDelete(data) {
      try {
        await deleteZone(this.parkingLotId, data.id)
        this.$message.success('删除成功')
        this.loadZoneTree()
      } catch (error) {
        this.$message.error('删除失败：' + error.message)
      }
    },
    handleSubmit() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return

        this.submitting = true
        try {
          const data = {
            ...this.formData,
            config: this.formData.configText ? JSON.parse(this.formData.configText) : null
          }
          delete data.configText

          if (this.formData.id) {
            await updateZone(this.parkingLotId, this.formData.id, data)
            this.$message.success('更新成功')
          } else {
            await createZone(this.parkingLotId, data)
            this.$message.success('创建成功')
          }
          this.dialogVisible = false
          this.loadZoneTree()
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
    }
  }
}
</script>

<style scoped>
.zone-management {
  padding: 20px;
}

.clearfix::after {
  content: "";
  display: table;
  clear: both;
}
</style>
