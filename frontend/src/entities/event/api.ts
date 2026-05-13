import { apiClient } from '@/shared/api/client'
import type {
    CreateEventRequest,
    EventRegistrationResponse,
    EventResponse,
    UpdateEventRequest,
} from './types'

export async function getStudentEvents(): Promise<EventResponse[]> {
    const response = await apiClient.get<EventResponse[]>('/student/events')
    return response.data
}

export async function getStudentEventById(id: number): Promise<EventResponse> {
    const response = await apiClient.get<EventResponse>(`/student/events/${id}`)
    return response.data
}

export async function registerForEvent(id: number): Promise<EventRegistrationResponse> {
    const response = await apiClient.post<EventRegistrationResponse>(`/student/events/${id}/registrations`)
    return response.data
}

export async function cancelEventRegistration(id: number): Promise<void> {
    await apiClient.delete(`/student/events/${id}/registrations`)
}

export async function getMyEvents(): Promise<EventRegistrationResponse[]> {
    const response = await apiClient.get<EventRegistrationResponse[]>('/student/my-events')
    return response.data
}

export async function createManagerEvent(payload: CreateEventRequest): Promise<EventResponse> {
    const response = await apiClient.post<EventResponse>('/manager/events', payload)
    return response.data
}

export async function getManagerEventsByOrganization(organizationId: number): Promise<EventResponse[]> {
    const response = await apiClient.get<EventResponse[]>(`/manager/events/organization/${organizationId}`)
    return response.data
}

export async function getManagerEventById(id: number): Promise<EventResponse> {
    const response = await apiClient.get<EventResponse>(`/manager/events/${id}`)
    return response.data
}

export async function updateManagerEvent(id: number, payload: UpdateEventRequest): Promise<EventResponse> {
    const response = await apiClient.put<EventResponse>(`/manager/events/${id}`, payload)
    return response.data
}

export async function publishManagerEvent(id: number): Promise<EventResponse> {
    const response = await apiClient.post<EventResponse>(`/manager/events/${id}/publish`)
    return response.data
}

export async function cancelManagerEvent(id: number): Promise<EventResponse> {
    const response = await apiClient.post<EventResponse>(`/manager/events/${id}/cancel`)
    return response.data
}

export async function completeManagerEvent(id: number): Promise<EventResponse> {
    const response = await apiClient.post<EventResponse>(`/manager/events/${id}/complete`)
    return response.data
}

export async function getManagerEventRegistrations(id: number): Promise<EventRegistrationResponse[]> {
    const response = await apiClient.get<EventRegistrationResponse[]>(`/manager/events/${id}/registrations`)
    return response.data
}