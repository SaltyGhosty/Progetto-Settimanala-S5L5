const STATUS_LABELS = {
  PENDING: 'In coda',
  PROCESSING: 'Elaborazione OCR in corso...',
  PROCESSED: 'Testo estratto',
  FAILED: 'OCR non riuscito',
}

export default function DocumentCard({ doc, onDelete }) {
  return (
    <article className="document-card">
      <header className="document-card-header">
        <span className="document-name">{doc.originalFilename}</span>
        <span className={`document-status status-${doc.status.toLowerCase()}`}>
          {STATUS_LABELS[doc.status] ?? doc.status}
        </span>
      </header>
      <p className="document-meta">
        {doc.contentType} · {(doc.sizeBytes / 1024).toFixed(0)} KB · caricato il{' '}
        {new Date(doc.uploadedAt).toLocaleString('it-IT')}
      </p>

      {doc.status === 'PROCESSED' && (
        <pre className="document-ocr-text">
          {doc.ocrText?.trim() ? doc.ocrText : '(Nessun testo rilevato nel documento.)'}
        </pre>
      )}
      {doc.status === 'FAILED' && (
        <p className="form-error">{doc.ocrError || "Errore sconosciuto durante l'elaborazione OCR."}</p>
      )}

      {onDelete && (
        <button type="button" className="link-button danger" onClick={() => onDelete(doc.id)}>
          Elimina
        </button>
      )}
    </article>
  )
}
