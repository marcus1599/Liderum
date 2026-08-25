export interface GuildRegistrationRequest {
  guildName: string;
  serverName: string;
  username: string;
  email: string;
  password: string;
}

export interface GuildRegistrationResponse {
  id: number;
  username: string;
  email: string;
  guildRole: string;
}
