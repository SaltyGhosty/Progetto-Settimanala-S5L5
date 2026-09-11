import { useEffect, useRef, useState } from 'react'

/**
 * Apre la webcam con getUserMedia e permette di scattare una foto.
 * Non uso <input capture>: sui browser desktop viene ignorato e apre comunque
 * l'esplora-file, quindi non garantisce che la foto arrivi davvero dalla fotocamera.
 */
export default function CameraCapture({ onCapture, onClose }) {
  const videoRef = useRef(null)
  const streamRef = useRef(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false

    if (!navigator.mediaDevices?.getUserMedia) {
      setError('Il browser non supporta l\'accesso alla fotocamera.')
      return
    }

    navigator.mediaDevices
      .getUserMedia({ video: { facingMode: 'environment' }, audio: false })
      .then((stream) => {
        if (cancelled) {
          stream.getTracks().forEach((track) => track.stop())
          return
        }
        streamRef.current = stream
        if (videoRef.current) videoRef.current.srcObject = stream
      })
      .catch(() => {
        setError('Impossibile accedere alla fotocamera. Controlla i permessi del browser.')
      })

    return () => {
      cancelled = true
      streamRef.current?.getTracks().forEach((track) => track.stop())
    }
  }, [])

  function handleCapture() {
    const video = videoRef.current
    if (!video || !video.videoWidth) return

    const canvas = document.createElement('canvas')
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    canvas.getContext('2d').drawImage(video, 0, 0)

    canvas.toBlob((blob) => {
      if (!blob) return
      onCapture(new File([blob], `foto-${Date.now()}.jpg`, { type: 'image/jpeg' }))
    }, 'image/jpeg', 0.92)
  }

  return (
    <div className="camera-modal-backdrop" onClick={onClose}>
      <div className="camera-modal" onClick={(e) => e.stopPropagation()}>
        {error ? (
          <p className="form-error">{error}</p>
        ) : (
          /* eslint-disable-next-line jsx-a11y/media-has-caption */
          <video ref={videoRef} autoPlay playsInline muted className="camera-preview" />
        )}
        <div className="camera-modal-actions">
          <button type="button" onClick={onClose}>Annulla</button>
          {!error && (
            <button type="button" onClick={handleCapture}>Scatta</button>
          )}
        </div>
      </div>
    </div>
  )
}
