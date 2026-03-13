export enum UserRole {
  OWNER = 'OWNER',
  ADMIN = 'ADMIN',
  USER = 'USER'
}

export interface UserDto {
  id: string;
  telegramId: number;
  username?: string;
  firstName: string;
  lastName?: string;
  role: UserRole;
  avatarUrl?: string;
  manualAccessUntil?: string;
  manualAccessType?: 'NONE' | 'THREE_MONTHS' | 'FOREVER';
}

export interface AccessRequestDto {
  id: string;
  userId: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
  decisionType?: 'APPROVE_3_MONTH' | 'APPROVE_FOREVER' | 'REJECT';
  resolvedAt?: string;
  resolvedBy?: string;
}

export interface ChatDto {
  id: string;
  chatId: number;
  type: 'GROUP' | 'SUPERGROUP';
  title?: string;
  username?: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  joinedAt: string;
  initiatedBy?: string;
  initiatedByRole?: UserRole;
  approvedBy?: string;
  approvedAt?: string;
}

export interface VpnSummaryDto {
  totalUsers: number;
  activeUsers: number;
}
