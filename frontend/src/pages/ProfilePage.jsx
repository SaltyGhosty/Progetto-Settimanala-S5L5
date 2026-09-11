import { useEffect, useRef, useState } from 'react'
import apiClient from '../api/client'
import DocumentCard from '../components/DocumentCard'
import { useAuth } from '../context/AuthContext'

const ALLOWED_TYPES = ['application/pdf', 'image/jpeg', 'image/png', 'image/tiff', 'image/bmp']

export default function ProfilePage() {
  const { user } = useAuth()
  const [profile, setProfile] = useState(null)
  const [documents, setDocuments] = useState([])
  const [error, setError] = useState('')
  const [uploading, setUploading] = useState(false)
  const fileInputRef = useRef(null)

  async function loadProfile() {
    const res = await apiClient.get('/api/users/me')
    setProfile(res.data)
  }

  async function loadDocuments() {
    const res = await apiClient.get('/api/documents')
    setDocuments(res.data)
  }

  useEffect(() => {
    loadProfile()
    loadDocuments()
  }, [])

  // Controllo periodicamente finché l'OCR di qualche documento è ancora in corso.
  useEffect(() => {
    const hasPending = documents.some((d) => d.status === 'PENDING' || d.status === 'PROCESSING')
    if (!hasPending) return
    const interval = setInterval(loadDocuments, 2500)
    return () => clearInterval(interval)
  }, [documents])

  async function handleDelete(id) {
    setError('')
    try {
      await apiClient.delete(`/api/documents/${id}`)
      await Promise.all([loadDocuments(), loadProfile()])
    } catch {
      setError("Eliminazione del documento non riuscita.")
    }
  }

  async function handleFileSelected(e) {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return

    if (!ALLOWED_TYPES.includes(file.type)) {
      setError('Formato non supportato. Formati ammessi: PDF, JPEG, PNG, TIFF, BMP.')
      return
    }

    setUploading(true)
    setError('')
    try {
      const formData = new FormData()
      formData.append('file', file)
      await apiClient.post('/api/documents', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      await Promise.all([loadDocuments(), loadProfile()])
    } catch (err) {
      setError(err.response?.data?.message || 'Caricamento del documento non riuscito.')
    } finally {
      setUploading(false)
    }
  }

  return (
    <div className="profile-page">
      <section className="profile-summary">
        <h1>{user?.username}</h1>
        {profile && (
          <p className="profile-meta">
            {profile.email} · membro dal {new Date(profile.createdAt).toLocaleDateString('it-IT')} ·{' '}
            {profile.postCount} post · {profile.documentCount} documenti
          </p>
        )}
      </section>

      <section className="profile-documents">
        <h2>Documenti</h2>
        <p className="section-hint">
          I documenti caricati vengono elaborati automaticamente tramite OCR per estrarne il testo.
        </p>

        <input
          ref={fileInputRef}
          type="file"
          accept="application/pdf,image/jpeg,image/png,image/tiff,image/bmp"
          hidden
          onChange={handleFileSelected}
        />
        <button type="button" onClick={() => fileInputRef.current?.click()} disabled={uploading}>
          {uploading ? 'Caricamento...' : 'Carica documento'}
        </button>

        {error && <p className="form-error">{error}</p>}

        <div className="document-list">
          {documents.length === 0 && <p className="empty-state">Nessun documento caricato.</p>}
          {documents.map((doc) => (
            <DocumentCard key={doc.id} doc={doc} onDelete={handleDelete} />
          ))}
        </div>
      </section>
    </div>
  )
}
