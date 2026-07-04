import { ref } from 'vue'
import { driver } from 'driver.js'
import 'driver.js/dist/driver.css'

const TOUR_KEY = 'app-tour-shown'

function loadFromStorage(key, fallback) {
  try {
    const val = localStorage.getItem(key)
    if (val === null || val === undefined) return fallback
    return val === 'true'
  } catch { return fallback }
}

// 模块级单例 —— 跨组件共享
const tourShown = ref(loadFromStorage(TOUR_KEY, false))
let driverInstance = null

export function useAppTour() {
  function markTourShown() {
    tourShown.value = true
    localStorage.setItem(TOUR_KEY, String(true))
  }

  function resetTour() {
    tourShown.value = false
    localStorage.removeItem(TOUR_KEY)
  }

  function startTour(steps) {
    if (driverInstance) driverInstance.destroy()

    driverInstance = driver({
      showProgress: true,
      progressText: '{{current}} / {{total}}',
      animate: true,
      allowClose: true,
      overlayOpacity: 0.65,
      stagePadding: 10,
      stageRadius: 10,
      smoothScroll: true,
      disableActiveInteraction: false,
      showButtons: ['next', 'previous', 'close'],
      nextBtnText: '下一步',
      prevBtnText: '上一步',
      doneBtnText: '完成',
      popoverClass: 'tour-popover',

      onDestroyed: () => {
        markTourShown()
        driverInstance = null
      },

      steps,
    })

    driverInstance.drive()
    return driverInstance
  }

  return { tourShown, markTourShown, resetTour, startTour }
}
