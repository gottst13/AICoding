<template>
  <el-tree
    :data="treeData"
    :props="defaultProps"
    node-key="id"
    default-expand-all
    :expand-on-click-node="false"
  >
    <span class="custom-tree-node" slot-scope="{ node, data }">
      <span>
        <i :class="getIconClass(data.zoneType)"></i>
        {{ node.label }} ({{ data.code }})
        <el-tag size="mini" v-if="data.floorLevel">{{ getFloorText(data.floorLevel) }}</el-tag>
        <el-tag size="mini" type="success" v-if="data.availableSpaces > 0">
          空位：{{ data.availableSpaces }}/{{ data.totalSpaces }}
        </el-tag>
        <el-tag size="mini" type="danger" v-else-if="data.totalSpaces > 0">已满</el-tag>
      </span>
      <span class="node-actions">
        <el-button type="text" size="mini" @click.stop="handleAdd(data)">
          添加
        </el-button>
        <el-button type="text" size="mini" @click.stop="handleEdit(data)">
          编辑
        </el-button>
        <el-button type="text" size="mini" @click.stop="handleDelete(data)">
          删除
        </el-button>
      </span>
    </span>
  </el-tree>
</template>

<script>
export default {
  name: 'ZoneTree',
  props: {
    treeData: {
      type: Array,
      default: () => []
    },
    parkingLotId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      defaultProps: {
        children: 'children',
        label: 'name'
      }
    }
  },
  methods: {
    getIconClass(zoneType) {
      return zoneType === 1 ? 'el-icon-office-building' : 'el-icon-location-outline'
    },
    getFloorText(floorLevel) {
      if (!floorLevel && floorLevel !== 0) return ''
      return floorLevel > 0 ? `${floorLevel}层` : `地下${Math.abs(floorLevel)}层`
    },
    handleAdd(data) {
      this.$emit('add', data)
    },
    handleEdit(data) {
      this.$emit('edit', data)
    },
    handleDelete(data) {
      this.$confirm(`确认删除区域 "${data.name}"？级联删除其下所有子区域和车位！`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$emit('delete', data)
      })
    }
  }
}
</script>

<style scoped>
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}

.node-actions {
  display: none;
}

.custom-tree-node:hover .node-actions {
  display: block;
}

.el-tree-node__content:hover {
  background-color: #f5f7fa;
}

.el-icon-office-building {
  color: #409EFF;
  margin-right: 5px;
}

.el-icon-location-outline {
  color: #67C23A;
  margin-right: 5px;
}
</style>
