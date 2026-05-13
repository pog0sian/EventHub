export type RewardPurchaseStatus = 'REQUESTED' | 'ISSUED' | 'CANCELLED'

export interface RewardResponse {
    id: number
    title: string
    description: string | null
    cost: number
    stock: number
    active: boolean
    createdAt: string
    updatedAt: string
}

export interface RewardPurchaseResponse {
    id: number
    userId: number
    rewardId: number
    rewardTitle: string
    cost: number
    status: RewardPurchaseStatus
    createdAt: string
    updatedAt: string
}
export interface CreateRewardRequest {
    title: string
    description: string | null
    cost: number
    stock: number
}

export interface UpdateRewardRequest {
    title: string
    description: string | null
    cost: number
    stock: number
    active: boolean
}