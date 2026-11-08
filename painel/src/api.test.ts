import { describe, expect, it, vi } from 'vitest'
import { ApiError, createMagazine, deleteMagazine, listMagazines, login, session, uploadCover } from './api'

function respostaJson(corpo: unknown, status = 200) {
  return Promise.resolve(new Response(JSON.stringify(corpo), { status }))
}

describe('api', () => {
  it('login guarda o token e as chamadas seguintes o enviam em Authorization', async () => {
    const fetchMock = vi
      .fn()
      .mockReturnValueOnce(respostaJson({ token: 'abc', expiresInSeconds: 60 }))
      .mockReturnValueOnce(respostaJson([]))
    vi.stubGlobal('fetch', fetchMock)

    await login('dono', 'senha')
    expect(session.get()).toBe('abc')

    await listMagazines()
    const [url, init] = fetchMock.mock.calls[1]
    expect(url).toBe('/api/magazines')
    expect(init.headers.Authorization).toBe('Bearer abc')
  })

  it('envia o corpo como JSON ao criar', async () => {
    const fetchMock = vi.fn().mockReturnValue(respostaJson({ id: '1' }, 201))
    vi.stubGlobal('fetch', fetchMock)
    await createMagazine({
      title: 'X',
      issue: null,
      year: null,
      month: null,
      coverModel: null,
      condition: null,
      acquired: true,
      isPublic: false,
      coverImageUrl: null,
    })
    const [, init] = fetchMock.mock.calls[0]
    expect(init.method).toBe('POST')
    expect(init.headers['Content-Type']).toBe('application/json')
    expect(JSON.parse(init.body)).toMatchObject({ title: 'X', condition: null })
  })

  it('401 fora do login derruba a sessão e lança ApiError', async () => {
    session.set('velho')
    vi.stubGlobal('fetch', vi.fn().mockReturnValue(respostaJson({ message: 'expirou' }, 401)))
    await expect(listMagazines()).rejects.toMatchObject({ status: 401 })
    expect(session.get()).toBeNull()
  })

  it('401 no próprio login mostra a mensagem da API sem mexer na sessão', async () => {
    vi.stubGlobal('fetch', vi.fn().mockReturnValue(respostaJson({ error: 'Usuário ou senha inválidos' }, 401)))
    await expect(login('x', 'y')).rejects.toThrow('Usuário ou senha inválidos')
    await expect(login('x', 'y')).rejects.toBeInstanceOf(ApiError)
  })

  it('204 (apagar) não tenta ler JSON', async () => {
    vi.stubGlobal('fetch', vi.fn().mockReturnValue(Promise.resolve(new Response(null, { status: 204 }))))
    await expect(deleteMagazine('1')).resolves.toBeUndefined()
  })
})

describe('uploadCover', () => {
  it('envia o arquivo como FormData, sem forçar Content-Type JSON', async () => {
    session.set('t')
    const fetchMock = vi.fn().mockReturnValue(respostaJson({ id: '1', coverImageUrl: '/capas/x.png' }))
    vi.stubGlobal('fetch', fetchMock)
    const arquivo = new File(['x'], 'capa.png', { type: 'image/png' })

    await uploadCover('1', arquivo)

    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/api/magazines/1/cover')
    expect(init.method).toBe('POST')
    expect(init.body).toBeInstanceOf(FormData)
    expect((init.body as FormData).get('file')).toBeInstanceOf(File)
    expect(init.headers['Content-Type']).toBeUndefined()
    expect(init.headers.Authorization).toBe('Bearer t')
  })
})
