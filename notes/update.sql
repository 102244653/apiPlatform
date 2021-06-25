
#2021-04-07 陈景锋修改
ALTER TABLE `mingfeng_singlecase`
DROP COLUMN `url`,
DROP COLUMN `depend`,
DROP COLUMN `beforeMap`,
DROP COLUMN `afterMap`,
DROP COLUMN `serviceId`;

#2021-04-14 陈景锋修改
ALTER TABLE `mingfeng_singlereport`
MODIFY COLUMN `runner`  varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '执行人平台用户名' AFTER `res`,
ADD COLUMN `test_account_name`  varchar(255) NULL AFTER `runner`;

ALTER TABLE `mingfeng_singlereport`
ADD COLUMN `uid`  bigint(20) NULL AFTER `res`,
ADD COLUMN `test_account_id`  int(5) NULL AFTER `runner`;

#2021-04-15 陈景锋修改
drop table global;
CREATE TABLE `global_env` (
  `id` int(4) NOT NULL AUTO_INCREMENT,
  `plateForm` int(3) NOT NULL,
  `urlPre` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL COMMENT '自定义环境名称',
  `uid` bigint(20) NOT NULL,
  `status` int(3) NOT NULL COMMENT '0：停用 1：启用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

#2021-04-19 陈景锋修改
ALTER TABLE `mingfeng_singlereport`
ADD COLUMN `taskId`  varchar(200) NOT NULL AFTER `id`;

#2021-05-10 修改用例模块ID
ALTER TABLE `mingfeng_flowcase`
CHANGE COLUMN `kindDirId` `kindId` int(5) NOT NULL AFTER `method`;

ALTER TABLE `global_rule`
CHANGE COLUMN `desc` `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL AFTER `action`;

ALTER TABLE `mingfeng_flowcase`
CHANGE COLUMN `afterCheck` `afterDBCheck` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL AFTER `afterSql`;

ALTER TABLE `mingfeng_singlecase`
CHANGE COLUMN `afterCheck` `afterDBCheck` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'sql要检查的参数' AFTER `afterSql`;

#2021-05-12 新增数据库配置表
CREATE TABLE `global_dbConfig`  (
  `id` int(5) NOT NULL AUTO_INCREMENT COMMENT '序号',
  `title` varchar(255) NOT NULL COMMENT '别名',
  `platform` int(255) NOT NULL COMMENT '平台：1-SAAS，2-互联网',
  `ip` varchar(255) NOT NULL COMMENT '数据库地址',
  `port` int(255) NOT NULL COMMENT '端口号',
  `dbName` varchar(255) NOT NULL COMMENT '数据库名称',
  `userName` varchar(255) NOT NULL COMMENT '用户名',
  `passWord` varchar(255) NOT NULL COMMENT '密码',
  `status` int(255) NOT NULL COMMENT '状态：0-禁用，1-启用',
  PRIMARY KEY (`id`)
);

ALTER TABLE `global_env`
ADD COLUMN `remoteAddress` varchar(255) NULL AFTER `urlPre`;

ALTER TABLE `global_env`
CHANGE COLUMN `plateForm` `platForm` int(3) NOT NULL AFTER `id`;

#2021-05-13
ALTER TABLE `global_test_account`
ADD COLUMN `cookie` text NULL AFTER `loginDetail`;

#2021-05-17
ALTER TABLE `mingfeng_flowreport`
MODIFY COLUMN `testUser` varchar(255) NOT NULL AFTER `result`;

#2021-05-18 账号增加管理员标识
ALTER TABLE `global_user`
ADD COLUMN `admin` int(5) NOT NULL DEFAULT 0 COMMENT '是否为管理员账号：0-不是，1-是' AFTER `loginTime`;

#2021-05-28 增加报告统计表
CREATE TABLE `mingfeng_flowresult`  (
  `id` bigint(5) NOT NULL AUTO_INCREMENT,
  `taskId` varchar(200) NOT NULL,
  `totalLabel` integer(8) NULL,
  `labelPass` integer(8) NULL,
  `labelFail` integer(8) NULL,
  `totalCase` integer(8) NULL,
  `casePass` integer(8) NULL,
  `caseFail` int(8) NULL,
  `logs` text NULL ,
  `testUser` varchar(8) NOT NULL,
  `testTime` datetime(0) NOT NULL,
  PRIMARY KEY (`id`)
)

# 2021-06-1 用户头像字段修改
-- ALTER TABLE `global_user`
-- MODIFY COLUMN `icon` mediumblob NULL AFTER `phone`;

请求参数改为非必填
ALTER TABLE `mingfeng_singlecase`
MODIFY COLUMN `params` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '请求参数' AFTER `method`;

增加执行状态记录
ALTER TABLE `mingfeng_flowresult`
ADD COLUMN `runStatus` int(3) NOT NULL COMMENT '执行状态：0-未结束，1-已结束' AFTER `testTime`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`id`) USING BTREE;

#2021-6-22 修改报告结构为通用

ALTER TABLE mingfeng_flowresult RENAME TO mingfeng_result;
ALTER TABLE mingfeng_flowreport RENAME TO mingfeng_report;

ALTER TABLE `mingfeng_report`
    MODIFY COLUMN `taskId` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务ID' AFTER `id`,
    ADD COLUMN `caseType` int(5) NOT NULL COMMENT '用例类型：单接口-1，流程-2' AFTER `taskId`,
    MODIFY COLUMN `labelName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '流程用例标签名' AFTER `taskId`,
    MODIFY COLUMN `executeId` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '流程用例执行顺序' AFTER `labelName`,
    MODIFY COLUMN `kindName` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属模块' AFTER `executeId`,
    MODIFY COLUMN `tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '接口用例名' AFTER `kindName`,
    MODIFY COLUMN `method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '请求方式' AFTER `tag`,
    MODIFY COLUMN `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '测试地址' AFTER `method`,
    MODIFY COLUMN `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求参数' AFTER `url`,
    MODIFY COLUMN `testAccount` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '测试账号' AFTER `params`,
    MODIFY COLUMN `response` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '响应结果' AFTER `testAccount`,
    MODIFY COLUMN `errorLog` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '断言错误日志' AFTER `response`,
    MODIFY COLUMN `result` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '测试结果' AFTER `errorLog`,
    MODIFY COLUMN `testUser` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '执行人' AFTER `result`;


ALTER TABLE `mingfeng_result`
    ADD COLUMN `taskType` int(255) NOT NULL COMMENT '任务类型：0-混合，1-单接口，2-组合' AFTER `runStatus`;