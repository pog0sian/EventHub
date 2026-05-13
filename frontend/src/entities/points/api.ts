import { apiClient } from '@/shared/api/client'
import type { PointBalanceResponse, PointTransactionResponse } from './types'

export async function getPointBalance(): Promise<PointBalanceResponse> {
    const response = await apiClient.get<PointBalanceResponse>('/student/points/balance')
    return response.data
}

export async function getPointTransactions(): Promise<PointTransactionResponse[]> {
    const response = await apiClient.get<PointTransactionResponse[]>('/student/points/transactions')
    return response.data
}
