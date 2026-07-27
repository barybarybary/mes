const GRADIENTS = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
  'linear-gradient(135deg, #fccb90 0%, #d57eeb 100%)',
  'linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%)',
  'linear-gradient(135deg, #0ea5e9 0%, #2dd4bf 100%)',
  'linear-gradient(135deg, #f97316 0%, #eab308 100%)',
  'linear-gradient(135deg, #8b5cf6 0%, #ec4899 100%)',
  'linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%)',
]

/**
 * 根据 id 或名称返回稳定的渐变色
 * @param {number|string} idOrName
 * @returns {string}
 */
export function placeholderGradient(idOrName) {
  const hash = typeof idOrName === 'number' ? idOrName : (idOrName || '').length
  return GRADIENTS[Math.abs(hash) % GRADIENTS.length]
}

/**
 * 获取名称的首字（用于占位图）
 * @param {string} name
 * @returns {string}
 */
export function placeholderChar(name) {
  return (name || '?').charAt(0)
}
