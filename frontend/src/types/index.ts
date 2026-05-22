/**
 * Shared TypeScript interfaces for User, Workspace, AgentResponse, and ContentVersion.
 */
export interface User {
  id: number
  username: string
  email: string
}

export interface Workspace {
  id: number
  userId: number
  title: string
  topic: string
  status: number
  createdAt: string
  updatedAt: string
}

export interface AgentResponse {
  agentRole: string
  input: string
  output: any
  tokensUsed: number
  durationMs: number
}

export interface ContentVersion {
  id: number
  workspaceId: number
  platform: string
  title: string
  body: string
  version: number
  isUserEdited: number
  createdAt: string
}
