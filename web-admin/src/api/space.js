import request from '@/utils/request'

/**
 * 查询区域的空闲车位
 */
export function getAvailableSpaces(zoneId) {
  return request({
    url: `/api/v1/zones/${zoneId}/spaces/available`,
    method: 'get'
  })
}

/**
 * 查询车位分页列表
 */
export function querySpaces(params) {
  return request({
    url: '/api/v1/spaces',
    method: 'get',
    params
  })
}

/**
 * 创建车位
 */
export function createSpace(zoneId, data) {
  return request({
    url: `/api/v1/zones/${zoneId}/spaces`,
    method: 'post',
    data
  })
}

/**
 * 批量创建车位
 */
export function batchCreateSpaces(zoneId, data) {
  return request({
    url: `/api/v1/zones/${zoneId}/spaces/batch`,
    method: 'post',
    data
  })
}

/**
 * 更新车位状态
 */
export function updateSpaceStatus(spaceId, status) {
  return request({
    url: `/api/v1/spaces/${spaceId}/status`,
    method: 'put',
    data: { status }
  })
}

/**
 * 占用车位
 */
export function occupySpace(spaceId, plateNo) {
  return request({
    url: `/api/v1/spaces/${spaceId}/occupy`,
    method: 'post',
    data: { plateNo }
  })
}

/**
 * 释放车位
 */
export function releaseSpace(spaceId) {
  return request({
    url: `/api/v1/spaces/${spaceId}/release`,
    method: 'post'
  })
}

/**
 * 获取车位详情
 */
export function getSpaceDetail(spaceId) {
  return request({
    url: `/api/v1/spaces/${spaceId}`,
    method: 'get'
  })
}
