export interface AuthUser {
  id: string;
  name: string;
  username?: string;
  email?: string;
  profileLink?: string;
}

export interface AuthSession {
  user: AuthUser;
  accessToken: string;
}
