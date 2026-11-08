import { useState } from 'react'
import { CONDITION_LABELS, type Condition, type Magazine, type MagazineInput } from './types'

interface Props {
  initial?: Magazine
  onSubmit: (input: MagazineInput) => Promise<void>
  onCancel?: () => void
}

const CONDITIONS = Object.keys(CONDITION_LABELS) as Condition[]

export function MagazineForm({ initial, onSubmit, onCancel }: Props) {
  const [title, setTitle] = useState(initial?.title ?? '')
  const [issue, setIssue] = useState(initial?.issue ?? '')
  const [year, setYear] = useState(initial?.year?.toString() ?? '')
  const [month, setMonth] = useState(initial?.month?.toString() ?? '')
  const [coverModel, setCoverModel] = useState(initial?.coverModel ?? '')
  const [coverImageUrl, setCoverImageUrl] = useState(initial?.coverImageUrl ?? '')
  // '' = condição não informada (vira null): não inventamos "Bom" ao salvar.
  const [condition, setCondition] = useState<Condition | ''>(initial?.condition ?? '')
  const [acquired, setAcquired] = useState(initial?.acquired ?? true)
  const [isPublic, setIsPublic] = useState(initial?.isPublic ?? false)
  const [busy, setBusy] = useState(false)

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    if (title.trim() === '') return
    setBusy(true)
    try {
      await onSubmit({
        title: title.trim(),
        issue: issue.trim() || null,
        year: year === '' ? null : Number(year),
        month: month === '' ? null : Number(month),
        coverModel: coverModel.trim() || null,
        coverImageUrl: coverImageUrl.trim() || null,
        condition: condition || null,
        acquired,
        isPublic,
      })
      if (!initial) {
        setTitle('')
        setIssue('')
        setCoverModel('')
        setCoverImageUrl('')
      }
    } finally {
      setBusy(false)
    }
  }

  return (
    <form className="card-form" onSubmit={handleSubmit}>
      <h2>{initial ? 'Editar revista' : 'Nova revista'}</h2>
      <label>
        Título
        <input value={title} onChange={(e) => setTitle(e.target.value)} required />
      </label>
      <div className="row">
        <label>
          Edição
          <input value={issue} onChange={(e) => setIssue(e.target.value)} />
        </label>
        <label>
          Mês
          <input type="number" min={1} max={12} value={month} onChange={(e) => setMonth(e.target.value)} />
        </label>
        <label>
          Ano
          <input type="number" min={1953} max={2100} value={year} onChange={(e) => setYear(e.target.value)} />
        </label>
      </div>
      <label>
        Modelo da capa
        <input value={coverModel} onChange={(e) => setCoverModel(e.target.value)} />
      </label>
      <label>
        Endereço da imagem da capa (https://...)
        <input type="url" value={coverImageUrl} onChange={(e) => setCoverImageUrl(e.target.value)} />
      </label>
      <label>
        Condição
        <select value={condition} onChange={(e) => setCondition(e.target.value as Condition | '')}>
          <option value="">Não informada</option>
          {CONDITIONS.map((c) => (
            <option key={c} value={c}>
              {CONDITION_LABELS[c]}
            </option>
          ))}
        </select>
      </label>
      <label className="check">
        <input type="checkbox" checked={acquired} onChange={(e) => setAcquired(e.target.checked)} />
        Já tenho esta revista
      </label>
      <label className="check">
        <input type="checkbox" checked={isPublic} onChange={(e) => setIsPublic(e.target.checked)} />
        Mostrar no catálogo público
      </label>
      <div className="row">
        <button type="submit" disabled={busy}>
          {busy ? 'Salvando...' : initial ? 'Salvar' : 'Adicionar'}
        </button>
        {onCancel && (
          <button type="button" onClick={onCancel}>
            Cancelar
          </button>
        )}
      </div>
    </form>
  )
}
