import { saveAs } from 'file-saver'

/**
 * 格式化文件大小
 * @param size 文件大小（字节）
 */
export const formatSize = (size?: number): string => {
  if (!size) return '未知';
  if (size < 1024) return size + ' B';
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB';
  return (size / (1024 * 1024)).toFixed(2) + ' MB';
};


/**
 * 下载图片
 * @param url 图片下载地址
 * @param fileName 要保存为的文件名
 */
export function downloadImage(url?: string, fileName?: string): void {
  if (!url) {
    return;
  }
  saveAs(url, fileName);
}

