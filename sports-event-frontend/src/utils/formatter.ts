/**
 * 格式化日期时间
 * @param dateString 日期字符串
 * @returns 格式化后的日期时间字符串
 */
export const formatDateTime = (dateString: string): string => {
  if (!dateString) return '';
  
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  });
};

/**
 * 格式化日期（仅显示年月日）
 * @param dateString 日期字符串
 * @returns 格式化后的日期字符串
 */
export const formatDate = (dateString: string): string => {
  if (!dateString) return '';
  
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  });
};

/**
 * 格式化文件大小
 * @param bytes 文件大小（字节）
 * @returns 格式化后的文件大小
 */
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B';
  
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

/**
 * 格式化金额
 * @param amount 金额
 * @param currency 货币符号，默认为 ￥
 * @returns 格式化后的金额
 */
export const formatCurrency = (amount: number, currency = '￥'): string => {
  return `${currency}${amount.toFixed(2)}`;
};

/**
 * 格式化手机号，隐藏中间4位
 * @param phone 手机号
 * @returns 格式化后的手机号
 */
export const formatPhone = (phone: string): string => {
  if (!phone || phone.length !== 11) return phone;
  
  return `${phone.substring(0, 3)}****${phone.substring(7)}`;
};

/**
 * 格式化计数（大于1000显示为k）
 * @param count 计数
 * @returns 格式化后的计数
 */
export const formatCount = (count: number): string => {
  if (count < 1000) return count.toString();
  
  return (count / 1000).toFixed(1) + 'k';
};

/**
 * 获取相对时间（几分钟前，几小时前，几天前等）
 * @param dateString 日期字符串
 * @returns 相对时间
 */
export const getRelativeTime = (dateString: string): string => {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  
  const diffSecs = Math.floor(diffMs / 1000);
  const diffMins = Math.floor(diffSecs / 60);
  const diffHours = Math.floor(diffMins / 60);
  const diffDays = Math.floor(diffHours / 24);
  
  if (diffSecs < 60) {
    return '刚刚';
  } else if (diffMins < 60) {
    return `${diffMins}分钟前`;
  } else if (diffHours < 24) {
    return `${diffHours}小时前`;
  } else if (diffDays < 30) {
    return `${diffDays}天前`;
  } else {
    return formatDate(dateString);
  }
}; 