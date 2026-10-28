import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'
import globals from 'globals'

export default [
  { ignores: ['dist/**', 'node_modules/**'] },

  // 浏览器运行环境全局变量（window / localStorage / fetch 等）
  { languageOptions: { globals: { ...globals.browser } } },

  // JS 基础规则（no-undef / no-unused-vars 等）
  js.configs.recommended,

  // Vue 3 官方规则集（essential：优先级 A，拦截真实错误）
  ...pluginVue.configs['flat/essential'],

  {
    rules: {
      // 核心：关闭 v-model 不能带参数的报错，解决 directives require no argument
      'vue/no-v-model-argument': 'off',
      // 路由页面按单词命名（Articles.vue / Moods.vue 等），重命名会破坏路由，故关闭
      'vue/multi-word-component-names': 'off',
    },
  },
]
