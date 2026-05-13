import { apiClient } from '@/shared/api/client'
import type { AttendanceResponse, MarkAttendanceRequest } from './types'

export async function markManagerEventAttendance(
    eventId: number,
    payload: MarkAttendanceRequest,
): Promise<AttendanceResponse> {
    const response = await apiClient.post<AttendanceResponse>(`/manager/events/${eventId}/attendance`, payload)
    return response.data
}

export async function getManagerEventAttendance(eventId: number): Promise<AttendanceResponse[]> {
    const response = await apiClient.get<AttendanceResponse[]>(`/manager/events/${eventId}/attendance`)
    return response.data
}