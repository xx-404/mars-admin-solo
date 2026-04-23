DROP TABLE IF EXISTS `shop`;
CREATE TABLE `shop`
(
    `id`                bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`           bigint       NOT NULL COMMENT '用户ID',
    `shop_code`         varchar(100) NOT NULL COMMENT '店铺编码',
    `platform`          varchar(100) NOT NULL COMMENT '平台 PINDUODUO:拼多多;DOUYIN:抖音;XIAOHONGSHU:小红书',
    `shop_name`         varchar(100) NULL DEFAULT NULL COMMENT '店铺名称',
    `shop_id_external`  varchar(100) NULL DEFAULT NULL COMMENT '外部店铺id',
    `auth_token`        text COMMENT '授权token',
    `auth_status`       tinyint      NOT NULL DEFAULT 0 COMMENT '授权状态(0-未授权 1-已授权)',
    `activation_status` tinyint      NOT NULL DEFAULT 0 COMMENT '激活状态(0-未激活 1-已激活)',
    `activated_at`      datetime NULL COMMENT '激活时间',
    `expires_at`        datetime NULL COMMENT '过期时间',
    `remark`            text COMMENT '备注',
    `create_time`       datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_shop_code`(`shop_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '店铺' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `activation_code`;
CREATE TABLE `activation_code`
(
    `id`                bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `activation_code`   varchar(100) NOT NULL COMMENT '激活码',
    `duration_type`     varchar(32)  NOT NULL COMMENT '类型 日:DAY;月:MONTH;季:QUARTER;年:YEAR',
    `duration_days`     int          NOT NULL COMMENT '天数',
    `activation_status` tinyint      NOT NULL DEFAULT 0 COMMENT '激活状态(0-未激活 1-已激活)',
    `user_id`           bigint                DEFAULT NULL COMMENT '激活用户ID',
    `shop_id`           bigint                DEFAULT NULL COMMENT '激活店铺ID',
    `activated_at`      datetime NULL COMMENT '激活时间',
    `create_time`       datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_activation_code`(`activation_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '激活码' ROW_FORMAT = DYNAMIC;

-- --------------------------------------------------------
-- 抖音商品表
-- 基于 com.doudian.open.api.product_addV2.param.ProductAddV2Param 设计
-- --------------------------------------------------------

DROP TABLE IF EXISTS `douyin_product`;
CREATE TABLE `douyin_product`
(
    `id`                     bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `owner_id`               varchar(64)  NOT NULL COMMENT '店铺所有者 ID（关联店铺表）',
    `product_id`             varchar(64) DEFAULT NULL COMMENT '抖音商品 ID',
    `outer_product_id`       varchar(64) DEFAULT NULL COMMENT '外部商品 ID（商家自定义）',
    `out_product_id`         bigint(20) DEFAULT NULL COMMENT '外部商品 ID（数字类型）',

    -- 基本信息
    `product_type`           bigint(20) DEFAULT NULL COMMENT '商品类型：1-普通商品，2-电子面单商品',
    `category_leaf_id`       bigint(20) DEFAULT NULL COMMENT '叶子类目 ID',
    `name`                   varchar(200) NOT NULL COMMENT '商品名称',
    `pic`                    varchar(500) DEFAULT NULL COMMENT '商品主图 URL',
    `description`            text         DEFAULT NULL COMMENT '商品描述',
    `recommend_remark`       varchar(500) DEFAULT NULL COMMENT '推荐备注',

    -- 价格库存
    `pay_type`               bigint(20) DEFAULT NULL COMMENT '支付方式：0-全款，1-定金 + 尾款',
    `price`                  decimal(10, 2) DEFAULT '0.00' COMMENT '商品价格（元）',
    `original_price`         decimal(10, 2) DEFAULT '0.00' COMMENT '原价/划线价（元）',
    `cost_price`             decimal(10, 2) DEFAULT '0.00' COMMENT '成本价（元）',
    `stock_num`              int(11) DEFAULT '0' COMMENT '库存数量',
    `reduce_type`            bigint(20) DEFAULT NULL COMMENT '库存扣减方式：1-付款减库存，2-发货减库存',

    -- 物流信息
    `delivery_method`        int(11) DEFAULT NULL COMMENT '配送方式：1-快递，2-上门取件，3-商家自配',
    `freight_id`             bigint(20) DEFAULT NULL COMMENT '运费模板 ID',
    `weight`                 decimal(10, 2) DEFAULT NULL COMMENT '商品重量',
    `weight_unit`            bigint(20) DEFAULT NULL COMMENT '重量单位：1-kg，2-g，3-jin',
    `delivery_delay_day`     bigint(20) DEFAULT NULL COMMENT '发货延迟天数',

    -- 预售信息
    `presell_type`           bigint(20) DEFAULT NULL COMMENT '预售类型：0-非预售，1-全款预售，2-定金预售',
    `presell_delay`          bigint(20) DEFAULT NULL COMMENT '预售延迟天数',
    `presell_end_time`       varchar(32) DEFAULT NULL COMMENT '预售结束时间（时间戳）',
    `presell_config_level`   bigint(20) DEFAULT NULL COMMENT '预售配置等级',
    `presell_delivery_type`  bigint(20) DEFAULT NULL COMMENT '预售发货类型',

    -- 规格 SKU
    `spec_name`              varchar(100) DEFAULT NULL COMMENT '规格名称',
    `specs`                  json         DEFAULT NULL COMMENT '规格信息（JSON）',
    `spec_prices`            json         DEFAULT NULL COMMENT '规格价格（JSON）',
    `spec_pic`               json         DEFAULT NULL COMMENT '规格图片（JSON）',

    -- 限购设置
    `maximum_per_order`      bigint(20) DEFAULT NULL COMMENT '每单最大购买数量',
    `minimum_per_order`      bigint(20) DEFAULT NULL COMMENT '每单最小购买数量',
    `limit_per_buyer`        bigint(20) DEFAULT NULL COMMENT '每人限购数量',

    -- 商品属性
    `product_format_new`     json         DEFAULT NULL COMMENT '商品属性（JSON）',
    `spu_id`                 bigint(20) DEFAULT NULL COMMENT 'SPU ID',
    `standard_brand_id`      bigint(20) DEFAULT NULL COMMENT '标准品牌 ID',
    `quality_list`           json         DEFAULT NULL COMMENT '资质列表（JSON）',

    -- 其他设置
    `cdf_category`           varchar(64) DEFAULT NULL COMMENT '跨境购类目',
    `assoc_ids`              varchar(500) DEFAULT NULL COMMENT '关联商品 ID 列表',
    `mobile`                 varchar(20) DEFAULT NULL COMMENT '联系电话',
    `supply_7day_return`     bigint(20) DEFAULT NULL COMMENT '是否支持 7 天无理由：0-不支持，1-支持',
    `commit`                 tinyint(1) DEFAULT NULL COMMENT '是否提交审核',
    `remark`                 varchar(500) DEFAULT NULL COMMENT '商家备注',
    `need_check_out`         tinyint(1) DEFAULT NULL COMMENT '是否需要 checkout',
    `need_recharge_mode`     tinyint(1) DEFAULT NULL COMMENT '是否需要充值模式',

    -- 发货相关
    `appoint_delivery_day`   bigint(20) DEFAULT NULL COMMENT '预约发货天数',
    `third_url`              varchar(500) DEFAULT NULL COMMENT '第三方链接',
    `extra`                  json         DEFAULT NULL COMMENT '扩展信息（JSON）',
    `src`                    varchar(64) DEFAULT NULL COMMENT '商品来源',

    -- O2O 相关
    `poi_resource`           json         DEFAULT NULL COMMENT '门店资源（JSON）',
    `car_vin_code`           varchar(50) DEFAULT NULL COMMENT '汽车 VIN 码',

    -- 图片相关
    `white_background_pic_url` varchar(500) DEFAULT NULL COMMENT '白底图 URL',
    `long_pic_url`           varchar(500) DEFAULT NULL COMMENT '长图 URL',

    -- 账号相关
    `account_template_id`    varchar(64) DEFAULT NULL COMMENT '账号模板 ID',

    -- 状态字段
    `status`                 tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：0-草稿，1-审核中，2-已上架，3-已下架，4-审核驳回，5-已删除',
    `audit_status`           tinyint(4) DEFAULT '0' COMMENT '审核状态：0-待审核，1-审核通过，2-审核驳回',
    `audit_remark`           varchar(500) DEFAULT NULL COMMENT '审核驳回原因',

    -- 时间字段
    `create_time`            datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`            datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `publish_time`           datetime DEFAULT NULL COMMENT '上架时间',
    `audit_time`             datetime DEFAULT NULL COMMENT '审核时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_owner_product` (`owner_id`, `product_id`),
    KEY `idx_outer_product_id` (`outer_product_id`),
    KEY `idx_category` (`category_leaf_id`),
    KEY `idx_status` (`status`),
    KEY `idx_audit_status` (`audit_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '抖音商品表' ROW_FORMAT = DYNAMIC;

-- --------------------------------------------------------
-- 商品 SKU 表
-- --------------------------------------------------------

DROP TABLE IF EXISTS `douyin_product_sku`;
CREATE TABLE `douyin_product_sku`
(
    `id`           bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `product_id`   bigint(20)   NOT NULL COMMENT '商品 ID（关联 douyin_product.id）',
    `sku_id`       varchar(64)  NOT NULL COMMENT '抖音 SKU ID',
    `sku_name`     varchar(200) DEFAULT NULL COMMENT 'SKU 名称',
    `spec_detail`  json         DEFAULT NULL COMMENT '规格详情（JSON）',
    `price`        decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT 'SKU 价格（元）',
    `original_price` decimal(10, 2) DEFAULT '0.00' COMMENT '原价（元）',
    `cost_price`   decimal(10, 2) DEFAULT '0.00' COMMENT '成本价（元）',
    `stock_num`    int(11) DEFAULT '0' COMMENT '库存数量',
    `sku_pic`      varchar(500) DEFAULT NULL COMMENT 'SKU 图片 URL',
    `spec_codes`   varchar(500) DEFAULT NULL COMMENT '规格编码',
    `barcode`      varchar(100) DEFAULT NULL COMMENT '商品条码',
    `status`       tinyint(4)   NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time`  datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_sku_id` (`sku_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '抖音商品 SKU 表' ROW_FORMAT = DYNAMIC;

-- --------------------------------------------------------
-- 抖音运费模板表
-- 基于 com.doudian.open.api.freightTemplate_* 设计
-- --------------------------------------------------------

DROP TABLE IF EXISTS `douyin_freight_template`;
CREATE TABLE `douyin_freight_template`
(
    `id`                    bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `owner_id`              varchar(64)  NOT NULL COMMENT '店铺所有者ID（关联店铺表）',
    `freight_id`            bigint(20)   DEFAULT NULL COMMENT '抖音运费模板ID（外部ID）',

    -- 模板基本信息
    `template_name`         varchar(100) NOT NULL COMMENT '模板名称',
    `product_province`      bigint(20)   DEFAULT NULL COMMENT '发货省份ID',
    `product_province_name` varchar(50)  DEFAULT NULL COMMENT '发货省份名称',
    `product_city`          bigint(20)   DEFAULT NULL COMMENT '发货城市ID',
    `product_city_name`     varchar(50)  DEFAULT NULL COMMENT '发货城市名称',

    -- 计费设置
    `calculate_type`        bigint(20)   DEFAULT 1 COMMENT '计费类型: 1-按重量, 2-按件数',
    `transfer_type`         bigint(20)   DEFAULT 1 COMMENT '运送类型: 1-快递, 2-EMS, 3-平邮',
    `rule_type`             bigint(20)   DEFAULT 1 COMMENT '计费规则: 1-自定义, 2-卖家承担运费',
    `fixed_amount`          bigint(20)   DEFAULT NULL COMMENT '固定运费（单位: 分）',

    -- 计费规则列表 (JSON)
    `columns`               json         DEFAULT NULL COMMENT '计费规则列表（JSON格式）',

    -- 其他设置
    `upsert_transfer_rule`  tinyint(1)   DEFAULT NULL COMMENT '是否更新转运规则',

    -- 状态字段
    `status`                tinyint(4)   NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `sync_status`           tinyint(4)   DEFAULT 0 COMMENT '同步状态: 0-未同步, 1-已同步, 2-同步失败',
    `sync_time`             datetime     DEFAULT NULL COMMENT '最后同步时间',
    `sync_error`            varchar(500) DEFAULT NULL COMMENT '同步错误信息',

    -- 时间字段
    `create_time`           datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`           datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_owner_freight` (`owner_id`, `freight_id`),
    KEY `idx_template_name` (`template_name`),
    KEY `idx_status` (`status`),
    KEY `idx_sync_status` (`sync_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '抖音运费模板表' ROW_FORMAT = DYNAMIC;

