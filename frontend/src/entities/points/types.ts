export type PointTransactionType = 'ACCRUAL' | 'SPEND'

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