package com.example.socialapp.service;

import com.example.socialapp.dto.GeocodeDtos.GeocodeResult;
import com.example.socialapp.dto.PostDtos.LocationDto;
import com.example.socialapp.dto.PostDtos.PhotoResponse;
import com.example.socialapp.dto.PostDtos.PostResponse;
import com.example.socialapp.entity.Photo;
import com.example.socialapp.entity.Post;
import com.example.socialapp.entity.User;
import com.example.socialapp.exception.ForbiddenOperationException;
import com.example.socialapp.exception.InvalidFileException;
import com.example.socialapp.exception.ResourceNotFoundException;
import com.example.socialapp.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final String PHOTOS_SUBDIR = "photos";

    private final PostRepository postRepository;
    private final FileValidationService fileValidationService;
    private final StorageService storageService;
    private final GeocodingService geocodingService;

    @Transactional
    public PostResponse createPost(User author, String caption, Double latitude, Double longitude,
                                    String address, List<MultipartFile> photoFiles) {
        if (photoFiles == null || photoFiles.isEmpty()) {
            throw new InvalidFileException("È necessario allegare almeno una fotografia al post.");
        }

        Post post = Post.builder()
                .author(author)
                .caption(caption)
                .latitude(latitude)
                .longitude(longitude)
                .address(address)
                .build();

        // Se manca una delle due informazioni (coordinate o indirizzo), provo a completarla col geocoding.
        if (latitude != null && longitude != null && (address == null || address.isBlank())) {
            GeocodeResult reverse = geocodingService.reverse(latitude, longitude);
            if (reverse != null) {
                post.setAddress(reverse.displayName());
            }
        } else if ((latitude == null || longitude == null) && address != null && !address.isBlank()) {
            GeocodeResult forward = geocodingService.forward(address);
            if (forward != null) {
                post.setLatitude(forward.latitude());
                post.setLongitude(forward.longitude());
            }
        }

        int order = 0;
        for (MultipartFile file : photoFiles) {
            String detectedType = fileValidationService.detectAndValidateImage(file);
            String storedFilename = storageService.store(file, PHOTOS_SUBDIR, detectedType);
            Photo photo = Photo.builder()
                    .storedFilename(storedFilename)
                    .originalFilename(file.getOriginalFilename())
                    .contentType(detectedType)
                    .sizeBytes(file.getSize())
                    .displayOrder(order++)
                    .build();
            post.addPhoto(photo);
        }

        Post saved = postRepository.save(post);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> listPosts(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public void deletePost(Long id, User requester) {
        Post post = findOrThrow(id);
        if (!post.getAuthor().getId().equals(requester.getId())) {
            throw new ForbiddenOperationException("Non puoi eliminare un post di un altro utente.");
        }
        for (Photo photo : post.getPhotos()) {
            storageService.delete(PHOTOS_SUBDIR, photo.getStoredFilename());
        }
        postRepository.delete(post);
    }

    private Post findOrThrow(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post non trovato: " + id));
    }

    private PostResponse toResponse(Post post) {
        List<PhotoResponse> photos = post.getPhotos().stream()
                .map(p -> new PhotoResponse(
                        p.getId(),
                        "/api/files/photos/" + p.getStoredFilename(),
                        p.getOriginalFilename(),
                        p.getDisplayOrder()))
                .toList();

        LocationDto location = post.hasLocation()
                ? new LocationDto(post.getLatitude(), post.getLongitude(), post.getAddress())
                : null;

        return new PostResponse(
                post.getId(),
                post.getAuthor().getId(),
                post.getAuthor().getUsername(),
                post.getCaption(),
                post.getCreatedAt(),
                location,
                photos
        );
    }
}
