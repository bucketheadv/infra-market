package io.infra.market.vertx.entity

/**
 * 带时间戳的实体接口
 * 所有需要自动管理创建时间和更新时间的实体都应该实现此接口
 */
interface TimestampedEntity {
    var createTime: Long?
    var updateTime: Long?
}

