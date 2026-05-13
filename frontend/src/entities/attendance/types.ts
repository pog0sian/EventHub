export interface MarkAttendanceRequest {
    userId: number
    attended: boolean
}

export interface AttendanceResponse {
    id: number
    eventId: number
    userId: number
    userFirstName: string
    userLastName: string
    userEmail: string
    attended: boolean
    markedByUserId: number
    markedAt: string
    createdAt: string
    updatedAt: string
}