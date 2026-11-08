import { useState } from 'react'

const MAX_COVER_MB = 5
const COVER_TYPES = 'image/png,image/jpeg,image/webp,image/avif'
import { CONDITION_LABELS, type Condition, type Magazine, type MagazineInput } from './types'

interface Props {
  initial?: Magazine
  onSubmit: (input: MagazineInput, cover: File | null) => Promise<void>
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
  const [cover, setCover] = useState<File | null>(null)
  const [coverError, setCoverError] = useState<string | null>(null)
  const [fileKey, setFileKey] = useState(0) // trocar a key limpa o campo de arquivo
  const [busy, setBusy] = useState(false)

  // Validação rápida no navegador (a de verdade é feita pela API, que olha o conteúdo do arquivo).
  function handleCover(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null
    setCoverError(null)
    if (file && file.size > MAX_COVER_MB * 1024 * 1024) {
      setCoverError(`A imagem passa de ${MAX_COVER_MB} MB.`)
      setCover(null)
      return
    }
    setCover(file)
  }

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    if (title.trim() === '') return
    setBusy(true)
    try {
      await onSubmit(
        {
          title: title.trim(),
          issue: issue.trim() || null,
          year: year === '' ? null : Number(year),
          month: month === '' ? null : Number(month),
          coverModel: coverModel.trim() || null,
          coverImageUrl: coverImageUrl.trim() || null,
          condition: condition || null,
          acquired,
          isPublic,
        },
        cover,
      )
      setCover(null)
      setFileKey((k) => k + 1)
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
        Capa (imagem JPEG, PNG, WebP ou AVIF, até {MAX_COVER_MB} MB)
        <input key={fileKey} type="file" accept={COVER_TYPES} onChange={handleCover} />
      </label>
      {coverError && (
        <p role="alert" className="error">
          {coverError}
        </p>
      )}
      <label>
        ...ou endereço da imagem (https://...)
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
