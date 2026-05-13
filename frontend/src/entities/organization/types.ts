export interface OrganizationResponse {
    id: number
    name: string
    description: string | null
    contactEmail: string | null
    active: boolean
    createdAt: string
    updatedAt: string
}

export interface CreateOrganizationRequest {
    name: string
    description: string | null
    contactEmail: string | null
}

export interface UpdateOrganizationRequest {
    name: string
    description: string | null
    contactEmail: string | null
    active: boolean
}

export interface AssignOrganizationManagerRequest {
    userId: number
}

export interface OrganizationManagerResponse {
    id: number
    organizationId: number
    userId: number
    active: boolean
    createdAt: string
    updatedAt: string
}

export interface OrganizationManagerDetailsResponse {
    id: number
    organizationId: number
    userId: number
    userFirstName: string
    userLastName: string
    userEmail: string
    active: boolean
    createdAt: string
    updatedAt: string
}
