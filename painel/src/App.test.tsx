import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import App from './App'
import { session } from './api'
import type { Magazine } from './types'

const revista = (over: Partial<Magazine> = {}): Magazine => ({
  id: '1',
  title: 'Aurora',
  issue: '293',
  year: 1999,
  month: 3,
  coverModel: null,
  condition: null,
  acquired: true,
  isPublic: false,
  coverImageUrl: null,
  ...over,
})

const json = (corpo: unknown, status = 200) => Promise.resolve(new Response(JSON.stringify(corpo), { status }))

describe('App (painel)', () => {
  it('sem token mostra o login; com a senha certa entra e lista as revistas', async () => {
    const fetchMock = vi
      .fn()
      .mockReturnValueOnce(json({ token: 't', expiresInSeconds: 60 }))
      .mockReturnValueOnce(json([revista()]))
    vi.stubGlobal('fetch', fetchMock)

    render(<App />)
    await userEvent.type(screen.getByLabelText('Usuário'), 'dono')
    await userEvent.type(screen.getByLabelText('Senha'), 'senha')
    await userEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    expect(await screen.findByText('Aurora')).toBeInTheDocument()
    expect(screen.getByText('1 de 1 revistas')).toBeInTheDocument()
    expect(screen.getByText('Privada')).toBeInTheDocument()
  })

  it('mostra o erro quando o login é recusado', async () => {
    vi.stubGlobal('fetch', vi.fn().mockReturnValue(json({ error: 'Usuário ou senha inválidos' }, 401)))
    render(<App />)
    await userEvent.type(screen.getByLabelText('Usuário'), 'x')
    await userEvent.type(screen.getByLabelText('Senha'), 'y')
    await userEvent.click(screen.getByRole('button', { name: 'Entrar' }))
    expect(await screen.findByRole('alert')).toHaveTextContent('Usuário ou senha inválidos')
  })

  it('torna pública, e a lista atualiza com a resposta da API', async () => {
    session.set('t')
    const fetchMock = vi
      .fn()
      .mockReturnValueOnce(json([revista()]))
      .mockReturnValueOnce(json(revista({ isPublic: true })))
    vi.stubGlobal('fetch', fetchMock)

    render(<App />)
    await userEvent.click(await screen.findByRole('button', { name: 'Tornar pública' }))
    expect(await screen.findByText('Pública')).toBeInTheDocument()
    const [url, init] = fetchMock.mock.calls[1]
    expect(url).toBe('/api/magazines/1/public')
    expect(JSON.parse(init.body)).toEqual({ isPublic: true })
  })

  it('REGRESSÃO: editar revista sem condição e salvar envia condition null (não "Bom")', async () => {
    session.set('t')
    const fetchMock = vi
      .fn()
      .mockReturnValueOnce(json([revista()]))
      .mockReturnValueOnce(json(revista()))
    vi.stubGlobal('fetch', fetchMock)

    render(<App />)
    await userEvent.click(await screen.findByRole('button', { name: 'Editar' }))
    await userEvent.click(screen.getByRole('button', { name: 'Salvar' }))
    const corpo = JSON.parse(fetchMock.mock.calls[1][1].body)
    expect(corpo.condition).toBeNull()
  })

  it('apaga só depois de confirmar', async () => {
    session.set('t')
    const fetchMock = vi
      .fn()
      .mockReturnValueOnce(json([revista()]))
      .mockReturnValueOnce(Promise.resolve(new Response(null, { status: 204 })))
    vi.stubGlobal('fetch', fetchMock)
    const confirmar = vi.spyOn(window, 'confirm').mockReturnValueOnce(false).mockReturnValueOnce(true)

    render(<App />)
    const botao = await screen.findByRole('button', { name: 'Deletar' })
    await userEvent.click(botao)
    expect(fetchMock).toHaveBeenCalledTimes(1) // recusou: nada foi apagado
    await userEvent.click(botao)
    expect(screen.queryByText('Aurora')).not.toBeInTheDocument()
    expect(confirmar).toHaveBeenCalledTimes(2)
  })

  it('token vencido (401 na lista) volta para o login', async () => {
    session.set('velho')
    vi.stubGlobal('fetch', vi.fn().mockReturnValue(json({ message: 'expirou' }, 401)))
    render(<App />)
    const form = await screen.findByRole('button', { name: 'Entrar' })
    expect(within(form.closest('form')!).getByLabelText('Usuário')).toBeInTheDocument()
  })
})
