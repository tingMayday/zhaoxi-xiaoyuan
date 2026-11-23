import { ElMessageBox } from 'element-plus';

/** 删除、完成这类操作先问一句。点取消或关闭时返回 false。 */
export async function confirmAction(message: string, title: string, confirmText: string) {
  try {
    await ElMessageBox.confirm(message, title, {
      confirmButtonText: confirmText,
      cancelButtonText: '取消',
      type: 'warning',
    });
    return true;
  } catch {
    return false;
  }
}
