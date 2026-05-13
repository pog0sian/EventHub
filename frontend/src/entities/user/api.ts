import { apiClient } from '@/shared/api/client'
import type { UserResponse } from '@/entities/auth/types'

export async function getAdminUsers(): Promise<UserResponse[]> {
    const response = await apiClient.get<UserResponse[]>('/admin/users')
    return response.data
}

export async function deactivateAdminUser(id: number): Promise<UserResponse> {
    const response = await apiClient.post<UserResponse>(`/admin/users/${id}/deactivate`)
    return response.data
}