export interface Member {
    id: number;
    nickname: string;
    phone: string;
    guildRole: 'MARECHAL' | 'GENERAL' | 'MAJOR' | 'CAPITÃO' | 'SOLDIER';
    rank: string;
    classe:
      | 'Guerreiro'
      | 'Mago'
      | 'Atiradora'
      | 'Sacerdote'
      | 'Arqueiro'
      | 'Paladino'
      | 'Bárbaro'
      | 'Feiticeira'
      | 'Macaco'
      | 'Mercenário'
      | 'Espiritualista'
      | 'Místico'
      | 'Arcano';
    team?: {
      id: number;
      name: string;
    } | null;
  }
  