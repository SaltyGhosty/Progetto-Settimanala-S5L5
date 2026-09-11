import { useCallback, useState } from 'react'
import { GoogleMap, Marker, useJsApiLoader } from '@react-google-maps/api'
import apiClient from '../api/client'

const GOOGLE_MAPS_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY

const DEFAULT_CENTER = { lat: 45.4642, lng: 9.19 } // Milano, come centro di default

const MAP_CONTAINER_STYLE = { width: '100%', height: '280px' }

export default function LocationPicker({ location, onChange }) {
  const [addressQuery, setAddressQuery] = useState('')
  const [searching, setSearching] = useState(false)
  const [searchError, setSearchError] = useState('')

  const { isLoaded, loadError } = useJsApiLoader({
    id: 'google-map-script',
    googleMapsApiKey: GOOGLE_MAPS_API_KEY,
  })

  const position = location?.latitude != null && location?.longitude != null
    ? { lat: location.latitude, lng: location.longitude }
    : null

  async function handlePickOnMap(lat, lng) {
    onChange({ latitude: lat, longitude: lng, address: location?.address ?? null })
    try {
      const res = await apiClient.get('/api/geocode/reverse', { params: { lat, lon: lng } })
      onChange({ latitude: lat, longitude: lng, address: res.data.displayName })
    } catch {
      // se il reverse geocoding fallisce va bene lo stesso: restano valide solo le coordinate
    }
  }

  const handleMapClick = useCallback((e) => {
    handlePickOnMap(e.latLng.lat(), e.latLng.lng())
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location?.address])

  async function handleAddressSearch() {
    if (!addressQuery.trim()) return
    setSearching(true)
    setSearchError('')
    try {
      const res = await apiClient.get('/api/geocode/search', { params: { address: addressQuery } })
      onChange({ latitude: res.data.latitude, longitude: res.data.longitude, address: res.data.displayName })
    } catch {
      setSearchError('Indirizzo non trovato. Prova a essere più specifico.')
    } finally {
      setSearching(false)
    }
  }

  function clearLocation() {
    onChange(null)
    setAddressQuery('')
  }

  return (
    <div className="location-picker">
      {/* È un div e non un form: questo componente sta già dentro il form di creazione del post,
          e l'HTML non permette form annidati. L'invio con Enter è gestito a mano più sotto. */}
      <div className="address-search">
        <input
          type="text"
          placeholder="Cerca un indirizzo..."
          value={addressQuery}
          onChange={(e) => setAddressQuery(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              e.preventDefault()
              handleAddressSearch()
            }
          }}
        />
        <button type="button" disabled={searching} onClick={handleAddressSearch}>
          {searching ? 'Ricerca...' : 'Cerca'}
        </button>
      </div>
      {searchError && <p className="form-error">{searchError}</p>}

      <div className="map-wrapper">
        {loadError && <p className="form-error">Impossibile caricare Google Maps.</p>}
        {!loadError && !isLoaded && <p className="section-hint">Caricamento mappa...</p>}
        {isLoaded && (
          <GoogleMap
            mapContainerStyle={MAP_CONTAINER_STYLE}
            center={position ?? DEFAULT_CENTER}
            zoom={position ? 15 : 6}
            onClick={handleMapClick}
          >
            {position && <Marker position={position} />}
          </GoogleMap>
        )}
      </div>

      {location?.address && <p className="location-address">📍 {location.address}</p>}
      {position && (
        <button type="button" className="link-button" onClick={clearLocation}>
          Rimuovi posizione
        </button>
      )}
    </div>
  )
}
