export type GuildRole = 'MARECHAL' | 'GENERAL' | 'MAJOR' | 'CAPITÃO' | 'SOLDADO';

export interface ManagedUser {
  id: number;
  username: string;
  email: string;
  guildRole: GuildRole;
  status: 'PENDING' | 'ACTIVE' | 'DISABLED';
}

export interface CreateUserRequest {
  username: string;
  email: string;
  role: GuildRole;
}
export interface UserActivationResponse { user: ManagedUser; activationToken: string; }

export interface UpdateUserRoleRequest {
  role: GuildRole;
}
