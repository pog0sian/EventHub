import { apiClient } from '@/shared/api/client'
import type {
    CreateRewardRequest,
    RewardPurchaseResponse,
    RewardResponse,
    UpdateRewardRequest,
} from './types'


export async function getStudentRewards(): Promise<RewardResponse[]> {
    const response = await apiClient.get<RewardResponse[]>('/student/rewards')
    return response.data
}

export async function purchaseReward(id: number): Promise<RewardPurchaseResponse> {
    const response = await apiClient.post<RewardPurchaseResponse>(`/student/rewards/${id}/purchase`)
    return response.data
}

export async function getMyRewardPurchases(): Promise<RewardPurchaseResponse[]> {
    const response = await apiClient.get<RewardPurchaseResponse[]>('/student/rewards/purchases')
    return response.data
}
export async function getAdminRewards(): Promise<RewardResponse[]> {
    const response = await apiClient.get<RewardResponse[]>('/admin/rewards')
    return response.data
}

export async function createAdminReward(payload: CreateRewardRequest): Promise<RewardResponse> {
    const response = await apiClient.post<RewardResponse>('/admin/rewards', payload)
    return response.data
}

export async function updateAdminReward(id: number, payload: UpdateRewardRequest): Promise<RewardResponse> {
    const response = await apiClient.put<RewardResponse>(`/admin/rewards/${id}`, payload)
    return response.data
}

export async function deactivateAdminReward(id: number): Promise<RewardResponse> {
    const response = await apiClient.post<RewardResponse>(`/admin/rewards/${id}/deactivate`)
    return response.data
}

export async function getAdminRewardPurchases(): Promise<RewardPurchaseResponse[]> {
    const response = await apiClient.get<RewardPurchaseResponse[]>('/admin/rewards/purchases')
    return response.data
}

export async function getRequestedAdminRewardPurchases(): Promise<RewardPurchaseResponse[]> {
    const response = await apiClient.get<RewardPurchaseResponse[]>('/admin/rewards/purchases/requested')
    return response.data
}

export async function issueAdminRewardPurchase(id: number): Promise<RewardPurchaseResponse> {
    const response = await apiClient.post<RewardPurchaseResponse>(`/admin/rewards/purchases/${id}/issue`)
    return response.data
}