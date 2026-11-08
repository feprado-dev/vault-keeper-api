// O formato que a API Java devolve (MagazineResponse) e aceita (MagazineRequest).
export type Condition = 'EXCELENTE' | 'BOM' | 'REGULAR' | 'RUIM'

export const CONDITION_LABELS: Record<Condition, string> = {
  EXCELENTE: 'Excelente',
  BOM: 'Bom',
  REGULAR: 'Regular',
  RUIM: 'Ruim',
}

export interface Magazine {
  id: string
  title: string
  issue: string | null
  year: number | null
  month: number | null
  coverModel: string | null
  condition: Condition | null
  acquired: boolean
  isPublic: boolean
  coverImageUrl: string | null
}

export type MagazineInput = Omit<Magazine, 'id'>
