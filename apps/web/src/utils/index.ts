/** 页面共用的工具函数。 */

// 金额换算、日期文案、月度差额
export { centsFromYuan, centsFromYuanAllowZero, dateLabel, deltaText, yuan } from './format';

// 表单草稿：写入并恢复 localStorage
export { bindDraft } from './draft';

// 删除、完成前的二次确认
export { confirmAction } from './confirm';
