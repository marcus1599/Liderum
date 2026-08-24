export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  role: string;
}

export interface UserProfile {
  id: number;
  username: string;
  email: string;
  guildRole: string;
}
