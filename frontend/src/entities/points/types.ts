export type PointTransactionType = 'EVENT_ATTENDANCE' | 'REWARD_PURCHASE' | 'MANUAL_ADJUSTMENT'

export interface PointBalanceResponse {
    balance: number
}

export interface PointTransactionResponse {
    id: number
    userId: number
    eventId: number | null
    amount: number
    type: PointTransactionType
    description: string | null
    createdAt: string
}