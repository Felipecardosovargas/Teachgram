export interface User {
  id: string; 
  name: string;
  userName: string;
  mail: string;
  phone: string;
  profileLink?: string; 
  deleted: boolean;
  createdAt: string; 
  updatedAt: string; 
  description?: string;
}

export interface SignupRequest {
  name: string;
  userName: string;
  mail: string;
  phone: string;
  password: string;
}

export interface LoginRequest {
  userName: string;
  password: string;
}

export interface LoginResponse {
  jwtToken: string;
  user: User;
}

export interface UserResponse {
  id: string;
  name: string;
  userName: string;
  mail: string;
  phone: string;
  profileLink?: string;
  deleted: boolean;
  createdAt: string;
  updatedAt: string;
  description?: string;
}