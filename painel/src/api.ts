import type { Magazine, MagazineInput } from './types'

// Único arquivo que conversa com a API Java (o equivalente do magazineService.ts do outro projeto).
// O token fica no sessionStorage: some ao fechar a aba. Um XSS poderia lê-lo, por isso o
// React nunca usa dangerouslySetInnerHTML e o painel é só do dono.
const TOKEN_KEY = 'vk-token'

export const session = {
  get(): string | null {
    try {
      return sessionStorage.getItem(TOKEN_KEY)
    } catch {
      return null
    }
  },
  set(token: string) {
    try {
      sessionStorage.setItem(TOKEN_KEY, token)
    } catch {
      /* sem armazenamento: o login vale só até recarregar */
    }
  },
  clear() {
    try {
      sessionStorage.removeItem(TOKEN_KEY)
    } catch {
      /* nada a fazer */
    }
  },
}

export class ApiError extends Error {
  status: number
  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = session.get()
  const res = await fetch(path, {
    ...init,
    headers: {
      // FormData (upload) define sozinho o Content-Type, com o "boundary": não pode ser sobrescrito.
      ...(init.body && !(init.body instanceof FormData) ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  })

  if (!res.ok) {
    // 401 fora do login = token vencido ou inválido: derruba a sessão para voltar à tela de login.
    if (res.status === 401 && path !== '/api/auth/login') session.clear()
    const body = (await res.json().catch(() => null)) as { error?: string; message?: string } | null
    throw new ApiError(res.status, body?.error ?? body?.message ?? `Erro ${res.status}`)
  }
  return res.status === 204 ? (undefined as T) : ((await res.json()) as T)
}

export async function login(username: string, password: string): Promise<void> {
  const { token } = await request<{ token: string }>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
  session.set(token)
}

export const listMagazines = () => request<Magazine[]>('/api/magazines')

export const createMagazine = (input: MagazineInput) =>
  request<Magazine>('/api/magazines', { method: 'POST', body: JSON.stringify(input) })

export const updateMagazine = (id: string, input: MagazineInput) =>
  request<Magazine>(`/api/magazines/${id}`, { method: 'PUT', body: JSON.stringify(input) })

export const setPublic = (id: string, isPublic: boolean) =>
  request<Magazine>(`/api/magazines/${id}/public`, { method: 'PATCH', body: JSON.stringify({ isPublic }) })

export function uploadCover(id: string, file: File) {
  const form = new FormData()
  form.append('file', file)
  return request<Magazine>(`/api/magazines/${id}/cover`, { method: 'POST', body: form })
}

export const deleteMagazine = (id: string) => request<void>(`/api/magazines/${id}`, { method: 'DELETE' })
