/** 全局 TypeScript 类型定义：User、Workspace、AgentResponse、ContentVersion */

/** 用户信息 */
export interface User {
  id: number
  username: string
  email: string
}

/** 工作区 */
export interface Workspace {
  id: number
  userId: number
  title: string
  topic: string
  status: number
  createdAt: string
  updatedAt: string
}

/** 策略结果 */
export interface StrategyResult {
  angles: string[]
  keywords: string[]
  structure: {
    title: string
    sections: string[]
  }
  publishPlan: string[]
}

/** 内容结果 */
export interface ContentResult {
  title: string
  body: string
  tags: string[]
  seoDesc?: string
}

/** 平台适配结果 - 公众号 */
export interface WechatResult {
  title: string
  body: string
  coverSuggestion: string
}

/** 平台适配结果 - 小红书 */
export interface XiaohongshuResult {
  title: string
  body: string
  hashtags: string[]
}

/** 平台适配结果 - 抖音 */
export interface DouyinResult {
  title: string
  hook: string
  script: string
  hashtags: string[]
  duration: string
}

/** 平台适配结果集合 */
export interface PlatformResults {
  wechat?: WechatResult | string
  xiaohongshu?: XiaohongshuResult | string
  douyin?: DouyinResult | string
}

/** Agent 执行响应 */
export interface AgentResponse {
  agentRole: string
  input: string
  output: string | PlatformResults
  tokensUsed: number
  durationMs: number
}

/** 内容版本 */
export interface ContentVersion {
  id: number
  workspaceId: number
  agentExecutionId?: number
  platform: string
  title: string
  body: string
  version: number
  isUserEdited: number
  createdAt: string
}

/** Agent 执行记录 */
export interface AgentExecution {
  id: number
  workspaceId: number
  agentRole: string
  inputPrompt: string
  outputContent: string
  platform?: string
  tokensUsed: number
  createdAt: string
}

/** API 响应封装 */
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

/** 竞品分析结果 */
export interface CompetitorAnalysis {
  id: number
  workspaceId: number
  competitorName: string
  platforms: string
  analysisResult: string
  status: number
  createdAt: string
}

/** 媒体资源 */
export interface MediaAsset {
  id: number
  workspaceId: number
  assetType: string
  title: string
  content: string
  version: number
  createdAt: string
}
