<template>
  <div>
    <template v-for="item in menus">
    <el-sub-menu v-if="item.children?.length" :index="item.path || String(item.permId)" :key="item.permId + '-dir'">
      <template #title>
        <el-icon v-if="item.icon"><component :is="iconComponent(item.icon)" /></el-icon>
        <span>{{ item.permName }}</span>
      </template>
      <SideMenu :menus="item.children" />
    </el-sub-menu>
    <el-menu-item v-else-if="item.path" :index="item.path" :key="item.permId + '-item'">
      <el-icon v-if="item.icon"><component :is="iconComponent(item.icon)" /></el-icon>
      <span>{{ item.permName }}</span>
    </el-menu-item>
    </template>
  </div>
</template>

<script setup>
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

defineOptions({ name: 'SideMenu' })

defineProps({
  menus: { type: Array, default: () => [] },
})

const iconMap = Object.fromEntries(
  Object.entries(ElementPlusIconsVue).map(([key, val]) => [key.toLowerCase(), val])
)

function iconComponent(name) {
  if (!name) return null
  return iconMap[name.toLowerCase()] || null
}
</script>
