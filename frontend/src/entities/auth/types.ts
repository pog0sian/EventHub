export type RoleName = 'STUDENT' | 'ORG_MANAGER' | 'ADMIN'

export interface UserResponse {
    id: number
    email: string
    firstName: string
    lastName: string
    patronymic: string | null
    enabled: boolean
    createdAt: string
    updatedAt: string
}

export interface LoginRequest {
    email: string
    password: string
}

export interface RegisterRequest {
    email: string
    password: string
    firstName: string
    lastName: string
    patronymic?: string | null
}

export interface AuthResponse {
    accessToken: string
    tokenType: 'Bearer'
    user: UserResponse
    roles: RoleName[]
}

export interface CurrentUserResponse {
    user: UserResponse
    roles: RoleName[]
    activeRole: RoleName | null
}

export interface ApiErrorResponse {
    timestamp: string
    status: number
    error: string
    message: string
    fieldErrors: Array<{
        field: string
        message: string
    }>
}