import { useRef, useState } from 'react'
import CameraCapture from './CameraCapture'

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']

export default function PhotoPicker({ photos, onChange }) {
  const [error, setError] = useState('')
  const [cameraOpen, setCameraOpen] = useState(false)
  const uploadInputRef = useRef(null)

  function addFiles(fileList) {
    const incoming = Array.from(fileList)
    const accepted = []
    let rejected = false

    for (const file of incoming) {
      if (ALLOWED_TYPES.includes(file.type)) {
        accepted.push(file)
      } else {
        rejected = true
      }
    }

    setError(rejected ? 'Alcuni file sono stati ignorati: formato non supportato (solo JPEG, PNG, WEBP).' : '')
    if (accepted.length > 0) {
      onChange([...photos, ...accepted])
    }
  }

  function removePhoto(index) {
    onChange(photos.filter((_, i) => i !== index))
  }

  return (
    <div className="photo-picker">
      <div className="photo-picker-actions">
        <button type="button" onClick={() => setCameraOpen(true)}>
          Scatta una foto
        </button>
        <button type="button" onClick={() => uploadInputRef.current?.click()}>
          Carica foto
        </button>
      </div>

      {cameraOpen && (
        <CameraCapture
          onCapture={(file) => {
            addFiles([file])
            setCameraOpen(false)
          }}
          onClose={() => setCameraOpen(false)}
        />
      )}

      {/* una o più foto scelte dal dispositivo */}
      <input
        ref={uploadInputRef}
        type="file"
        accept="image/jpeg,image/png,image/webp"
        multiple
        hidden
        onChange={(e) => {
          if (e.target.files?.length) addFiles(e.target.files)
          e.target.value = ''
        }}
      />

      {error && <p className="form-error">{error}</p>}

      {photos.length > 0 && (
        <div className="photo-preview-grid">
          {photos.map((file, index) => (
            <div className="photo-preview" key={`${file.name}-${index}`}>
              <img src={URL.createObjectURL(file)} alt={file.name} />
              <button type="button" className="photo-remove" onClick={() => removePhoto(index)}>
                &times;
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
