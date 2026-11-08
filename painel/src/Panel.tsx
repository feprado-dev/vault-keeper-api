import { useEffect, useState } from 'react'
import { ApiError, createMagazine, deleteMagazine, listMagazines, setPublic, updateMagazine } from './api'
import { MagazineForm } from './MagazineForm'
import { CONDITION_LABELS, type Magazine, type MagazineInput } from './types'

export function Panel({ onLogout }: { onLogout: () => void }) {
  const [magazines, setMagazines] = useState<Magazine[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [query, setQuery] = useState('')

  // Token vencido (401): volta ao login. Qualquer outro erro aparece na tela.
  function fail(e: unknown) {
    if (e instanceof ApiError && e.status === 401) return onLogout()
    setError((e as Error).message)
  }

  useEffect(() => {
    let cancelled = false
    listMagazines()
      .then((data) => !cancelled && setMagazines(data))
      .catch((e) => !cancelled && fail(e))
      .finally(() => !cancelled && setLoading(false))
    return () => {
      cancelled = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  async function handleAdd(input: MagazineInput) {
    try {
      setError(null)
      const created = await createMagazine(input)
      setMagazines((current) => [created, ...current])
    } catch (e) {
      fail(e)
    }
  }

  async function handleUpdate(id: string, input: MagazineInput) {
    try {
      setError(null)
      const updated = await updateMagazine(id, input)
      setMagazines((current) => current.map((m) => (m.id === id ? updated : m)))
      setEditingId(null)
    } catch (e) {
      fail(e)
    }
  }

  async function handleTogglePublic(m: Magazine) {
    try {
      setError(null)
      const updated = await setPublic(m.id, !m.isPublic)
      setMagazines((current) => current.map((x) => (x.id === m.id ? updated : x)))
    } catch (e) {
      fail(e)
    }
  }

  async function handleDelete(m: Magazine) {
    if (!window.confirm(`Apagar "${m.title}"?`)) return
    try {
      setError(null)
      await deleteMagazine(m.id)
      setMagazines((current) => current.filter((x) => x.id !== m.id))
    } catch (e) {
      fail(e)
    }
  }

  const q = query.trim().toLowerCase()
  const visible = q ? magazines.filter((m) => m.title.toLowerCase().includes(q)) : magazines

  return (
    <>
      <header className="bar">
        <strong className="brand">Vault Keeper · Painel</strong>
        <nav>
          <a href="/catalogo">Catálogo público</a>
          <button type="button" onClick={onLogout}>
            Sair
          </button>
        </nav>
      </header>
      <main>
        {error && (
          <p role="alert" className="error">
            Erro: {error}
          </p>
        )}

        <details className="panel">
          <summary>+ Nova revista</summary>
          <MagazineForm onSubmit={handleAdd} />
        </details>

        <label className="search">
          Buscar
          <input type="search" value={query} onChange={(e) => setQuery(e.target.value)} placeholder="título" />
        </label>
        <p className="muted">{loading ? 'Carregando...' : `${visible.length} de ${magazines.length} revistas`}</p>

        <ul className="list">
          {visible.map((m) => (
            <li key={m.id}>
              {editingId === m.id ? (
                <MagazineForm
                  initial={m}
                  onSubmit={(input) => handleUpdate(m.id, input)}
                  onCancel={() => setEditingId(null)}
                />
              ) : (
                <article className="row-item">
                  <div>
                    <h3>{m.title}</h3>
                    <p className="muted">
                      {[
                        m.issue && `Nº ${m.issue}`,
                        m.year && `${m.month ? String(m.month).padStart(2, '0') + '/' : ''}${m.year}`,
                        m.condition && CONDITION_LABELS[m.condition],
                      ]
                        .filter(Boolean)
                        .join(' · ') || 'Sem detalhes'}
                    </p>
                    <p>
                      <span className={m.isPublic ? 'tag tag--on' : 'tag'}>{m.isPublic ? 'Pública' : 'Privada'}</span>
                      {!m.acquired && <span className="tag">Procurando</span>}
                    </p>
                  </div>
                  <div className="actions">
                    <button type="button" onClick={() => handleTogglePublic(m)}>
                      {m.isPublic ? 'Tornar privada' : 'Tornar pública'}
                    </button>
                    <button type="button" onClick={() => setEditingId(m.id)}>
                      Editar
                    </button>
                    <button type="button" className="danger" onClick={() => handleDelete(m)}>
                      Deletar
                    </button>
                  </div>
                </article>
              )}
            </li>
          ))}
        </ul>
      </main>
    </>
  )
}
