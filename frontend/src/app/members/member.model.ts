export interface Member {
    id: number;
    nickname: string;
    phone: string;
    guildRole: 'MARECHAL' | 'GENERAL' | 'MAJOR' | 'CAPITÃO' | 'SOLDADO';
    rank: string;
    classe:
  | 'GUERREIRO'
  | 'MAGO'
  | 'ATIRADORA'
  | 'SACERDOTE'
  | 'ARQUEIRO'
  | 'PALADINO'
  | 'BARBARO'
  | 'FEITICEIRA'
  | 'ANDARILHO'
  | 'MERCENARIO'
  | 'ESPIRITUALISTA'
  | 'MISTICO'
  | 'BARDO'
  | 'ARCANO';
    teamName?: string | null;
    teamId?: number | null;
  }
