export enum UserRole {
  SUPERVISOR = 'SUPERVISOR',
  OPERATOR = 'OPERATOR'
}

export interface User {
  id: number;
  email: string;
  name: string;
  role: UserRole;
  createdAt: string | Date;
}

export interface UserForm {
  email: string;
  password: string;
}

export type LoginRequest = UserForm;
export type SignupRequest = UserForm; 