import { fileUrl } from '../api/client'

export default function PostCard({ post, onDelete, currentUserId }) {
  const isOwner = post.authorId === currentUserId

  return (
    <article className="post-card">
      <header className="post-card-header">
        <span className="post-author">{post.authorUsername}</span>
        <span className="post-date">{new Date(post.createdAt).toLocaleString('it-IT')}</span>
      </header>

      {post.photos.length > 0 && (
        <div className={`post-photos post-photos-${Math.min(post.photos.length, 4)}`}>
          {post.photos.map((photo) => (
            <img key={photo.id} src={fileUrl(photo.url)} alt={photo.originalFilename} />
          ))}
        </div>
      )}

      {post.caption && <p className="post-caption">{post.caption}</p>}

      {post.location && (
        <p className="post-location">
          📍 {post.location.address || `${post.location.latitude?.toFixed(5)}, ${post.location.longitude?.toFixed(5)}`}
        </p>
      )}

      {isOwner && (
        <button type="button" className="link-button danger" onClick={() => onDelete(post.id)}>
          Elimina post
        </button>
      )}
    </article>
  )
}
