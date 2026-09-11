import { useEffect, useState } from 'react'
import apiClient from '../api/client'
import PostCard from '../components/PostCard'
import { useAuth } from '../context/AuthContext'

export default function FeedPage() {
  const { user } = useAuth()
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  async function loadPosts() {
    setLoading(true)
    try {
      const res = await apiClient.get('/api/posts')
      setPosts(res.data.content)
      setError('')
    } catch {
      setError('Impossibile caricare i post.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadPosts()
  }, [])

  async function handleDelete(postId) {
    if (!window.confirm('Eliminare questo post?')) return
    await apiClient.delete(`/api/posts/${postId}`)
    setPosts((prev) => prev.filter((p) => p.id !== postId))
  }

  if (loading) return <div className="page-loading">Caricamento...</div>

  return (
    <div className="feed-page">
      {error && <p className="form-error">{error}</p>}
      {posts.length === 0 && !error && <p className="empty-state">Nessun post ancora. Creane uno!</p>}
      {posts.map((post) => (
        <PostCard key={post.id} post={post} onDelete={handleDelete} currentUserId={user?.id} />
      ))}
    </div>
  )
}
