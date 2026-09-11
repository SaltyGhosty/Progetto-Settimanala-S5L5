import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import apiClient from '../api/client'
import PhotoPicker from '../components/PhotoPicker'
import LocationPicker from '../components/LocationPicker'

export default function CreatePostPage() {
  const navigate = useNavigate()
  const [caption, setCaption] = useState('')
  const [photos, setPhotos] = useState([])
  const [location, setLocation] = useState(null)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    if (photos.length === 0) {
      setError('Allega almeno una fotografia.')
      return
    }

    setSubmitting(true)
    setError('')
    try {
      const formData = new FormData()
      if (caption) formData.append('caption', caption)
      if (location?.latitude != null) formData.append('latitude', location.latitude)
      if (location?.longitude != null) formData.append('longitude', location.longitude)
      if (location?.address) formData.append('address', location.address)
      photos.forEach((file) => formData.append('photos', file))

      await apiClient.post('/api/posts', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      navigate('/')
    } catch (err) {
      setError(err.response?.data?.message || 'Creazione del post non riuscita.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="create-post-page">
      <form className="create-post-form" onSubmit={handleSubmit}>
        <h1>Nuovo post</h1>
        {error && <p className="form-error">{error}</p>}

        <label>
          Cosa pensi...
          <textarea value={caption} onChange={(e) => setCaption(e.target.value)} rows={3} maxLength={2000} />
        </label>

        <PhotoPicker photos={photos} onChange={setPhotos} />

        <fieldset>
          <legend>Posizione (opzionale)</legend>
          <LocationPicker location={location} onChange={setLocation} />
        </fieldset>

        <button type="submit" disabled={submitting}>{submitting ? 'Pubblicazione...' : 'Pubblica'}</button>
      </form>
    </div>
  )
}
