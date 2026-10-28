import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isHttp, isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import useUserStore from '@/store/modules/user'
import useLockStore from '@/store/modules/lock'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

NProgress.configure({ showSpinner: false })

// 免登录白名单：登录/注册 + 手机扫码档案页（二维码贴纸扫开要能直接看档案，
// 不能因为手机上没有 token 就被弹到登录页）
const whiteList = ['/login', '/register', '/m', '/m/cat/*']

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

/**
 * 目录型路由（有 children 但自己不是页面）默认 redirect 是 'noRedirect'，
 * 直接访问目录地址会落到 404。这里递归改成跳到第一个子页面，避免
 * 「/profile」「/medical」「/system/log」这类地址一打开就是 404。
 */
const withFirstChildRedirect = (route) => {
  if (!route.children || !route.children.length) return route
  route.children.forEach(withFirstChildRedirect)
  if (route.redirect && route.redirect !== 'noRedirect') return route
  const first = route.children[0]
  const base = route.path.endsWith('/') ? route.path.slice(0, -1) : route.path
  const childPath = first.path.startsWith('/') ? first.path : `${base}/${first.path}`
  // 子级自身也是目录时，指到它的 redirect 上，逐层收敛到真正的页面
  route.redirect = !first.component && first.redirect && first.redirect !== 'noRedirect' ? first.redirect : childPath
  return route
}

router.beforeEach(async (to, from) => {
  NProgress.start()
  if (getToken()) {
    to.meta.title && useSettingsStore().setTitle(to.meta.title)
    const isLock = useLockStore().isLock
    if (to.path === '/login') {
      NProgress.done()
      return { path: '/' }
    }
    if (isWhiteList(to.path)) {
      return true
    }
    if (isLock && to.path !== '/lock') {
      NProgress.done()
      return { path: '/lock' }
    }
    if (!isLock && to.path === '/lock') {
      NProgress.done()
      return { path: '/' }
    }
    if (useUserStore().roles.length === 0) {
      isRelogin.show = true
      try {
        // 拉取user_info信息
        await useUserStore().getInfo()
        isRelogin.show = false
        // 根据roles权限生成可访问的路由
        const accessRoutes = await usePermissionStore().generateRoutes()
        accessRoutes.forEach(route => {
          if (!isHttp(route.path)) {
            router.addRoute(withFirstChildRedirect(route))
          }
        })
        // 重新导航到目标路由，确保动态路由已注册
        return { ...to, replace: true }
      } catch (err) {
        await useUserStore().logOut()
        ElMessage.error(err)
        return { path: '/' }
      }
    }
    // 静态详情页（分配用户、字典数据、调度日志等）没有后端下发的 meta，
    // 从 activeMenu 指向的主菜单继承「删除权限」，保证多选列判断与主列表一致
    if (to.meta.canRemove === undefined && to.meta.activeMenu) {
      const parent = router.resolve(to.meta.activeMenu)
      to.meta.canRemove = !!(parent.meta && parent.meta.canRemove)
    }
    return true
  } else {
    // 没有token
    if (isWhiteList(to.path)) {
      // 在免登录白名单，直接进入
      return true
    }
    NProgress.done()
    return `/login?redirect=${to.fullPath}` // 否则全部重定向到登录页
  }
})

router.afterEach(() => {
  NProgress.done()
})
