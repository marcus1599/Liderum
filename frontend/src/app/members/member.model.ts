export interface Member {
    id: number;
    nickname: string;
    phone: string;
    guildRole: 'MARECHAL' | 'GENERAL' | 'MAJOR' | 'CAPITÃO' | 'SOLDIER';
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
  | 'ARCANO';
    team?: {
      id: number;
      name: string;
    } | null;
  }
  