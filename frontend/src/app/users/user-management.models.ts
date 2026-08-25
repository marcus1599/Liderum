export type GuildRole = 'MARECHAL' | 'GENERAL' | 'MAJOR' | 'CAPITÃO' | 'SOLDADO';

export interface ManagedUser {
  id: number;
  username: string;
  email: string;
  guildRole: GuildRole;
}

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  role: GuildRole;
}

export interface UpdateUserRoleRequest {
  role: GuildRole;
}
