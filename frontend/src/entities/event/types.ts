export type EventStatus = 'DRAFT' | 'PUBLISHED' | 'CANCELLED' | 'COMPLETED'
export type EventRegistrationStatus = 'REGISTERED' | 'CANCELLED' | 'ATTENDED'

export interface EventResponse {
    id: number
    organizationId: number
    organizationName: string
    title: string
    description: string | null
    location: string | null
    startsAt: string
    endsAt: string
    pointsReward: number
    capacity: number | null
    status: EventStatus
    createdAt: string
    updatedAt: string
}

export interface EventRegistrationResponse {
    id: number
    eventId: number
    eventTitle: string
    userId: number
    userFirstName: string
    userLastName: string
    userEmail: string
    status: EventRegistrationStatus
    createdAt: string
    updatedAt: string
}

export interface CreateEventRequest {
    organizationId: number
    title: string
    description: string | null
    location: string | null
    startsAt: string
    endsAt: string
    pointsReward: number
    capacity: number | null
}

export interface UpdateEventRequest {
    title: string
    description: string | null
    location: string | null
    startsAt: string
    endsAt: string
    pointsReward: number
    capacity: number | null
}