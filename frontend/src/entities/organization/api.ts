import { apiClient } from '@/shared/api/client'
import type {
    AssignOrganizationManagerRequest,
    CreateOrganizationRequest,
    OrganizationManagerDetailsResponse,
    OrganizationManagerResponse,
    OrganizationResponse,
    UpdateOrganizationRequest,
} from './types'

export async function getMyManagerOrganizations(): Promise<OrganizationResponse[]> {
    const response = await apiClient.get<OrganizationResponse[]>('/manager/organizations/my')
    return response.data
}

export async function getAdminOrganizations(): Promise<OrganizationResponse[]> {
    const response = await apiClient.get<OrganizationResponse[]>('/admin/organizations')
    return response.data
}

export async function createAdminOrganization(payload: CreateOrganizationRequest): Promise<OrganizationResponse> {
    const response = await apiClient.post<OrganizationResponse>('/admin/organizations', payload)
    return response.data
}

export async function updateAdminOrganization(
    id: number,
    payload: UpdateOrganizationRequest,
): Promise<OrganizationResponse> {
    const response = await apiClient.put<OrganizationResponse>(`/admin/organizations/${id}`, payload)
    return response.data
}

export async function deactivateAdminOrganization(id: number): Promise<OrganizationResponse> {
    const response = await apiClient.post<OrganizationResponse>(`/admin/organizations/${id}/deactivate`)
    return response.data
}

export async function getAdminOrganizationManagers(id: number): Promise<OrganizationManagerDetailsResponse[]> {
    const response = await apiClient.get<OrganizationManagerDetailsResponse[]>(`/admin/organizations/${id}/managers`)
    return response.data
}

export async function assignAdminOrganizationManager(
    organizationId: number,
    payload: AssignOrganizationManagerRequest,
): Promise<OrganizationManagerResponse> {
    const response = await apiClient.post<OrganizationManagerResponse>(
        `/admin/organizations/${organizationId}/managers`,
        payload,
    )
    return response.data
}

export async function removeAdminOrganizationManager(
    organizationId: number,
    userId: number,
): Promise<OrganizationManagerResponse> {
    const response = await apiClient.delete<OrganizationManagerResponse>(
        `/admin/organizations/${organizationId}/managers/${userId}`,
    )
    return response.data
}
