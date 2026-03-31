import request from '@/utils/request'

/**
 * 获取停车场区域树形结构
 */
export function getZoneTree(parkingLotId) {
  return request({
    url: `/api/v1/parking-lots/${parkingLotId}/zones/tree`,
    method: 'get'
  })
}

/**
 * 创建区域
 */
export function createZone(parkingLotId, data) {
  return request({
    url: `/api/v1/parking-lots/${parkingLotId}/zones`,
    method: 'post',
    data
  })
}

/**
 * 更新区域
 */
export function updateZone(parkingLotId, id, data) {
  return request({
    url: `/api/v1/parking-lots/${parkingLotId}/zones/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除区域
 */
export function deleteZone(parkingLotId, id) {
  return request({
    url: `/api/v1/parking-lots/${parkingLotId}/zones/${id}`,
    method: 'delete'
  })
}

/**
 * 获取区域详情
 */
export function getZoneDetail(parkingLotId, id) {
  return request({
    url: `/api/v1/parking-lots/${parkingLotId}/zones/${id}`,
    method: 'get'
  })
}
