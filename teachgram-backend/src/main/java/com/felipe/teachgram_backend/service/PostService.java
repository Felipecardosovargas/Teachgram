package com.felipe.teachgram_backend.service;

import com.felipe.teachgram_backend.dto.user.UserResponseDTO;
import com.felipe.teachgram_backend.mapper.PostMapper;
import com.felipe.teachgram_backend.mapper.UserMapper;
import com.felipe.teachgram_backend.dto.post.PostRequestDTO;
import com.felipe.teachgram_backend.dto.post.PostResponseDTO;
import com.felipe.teachgram_backend.entity.Post;
import com.felipe.teachgram_backend.entity.User;
import com.felipe.teachgram_backend.exception.ResourceNotFoundException;
import com.felipe.teachgram_backend.exception.ValidationException;
import com.felipe.teachgram_backend.repository.PostRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for managing Post entities.
 * Handles creation, retrieval, updates, and deletion of posts,
 * along with specific features like privacy toggling and liking.
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    /**
     * Creates a new post for a specific user.
     *
     * @param postRequestDTO DTO containing post data.
     * @param userId ID of the user creating the post.
     * @return {@link PostResponseDTO} of the created post.
     * @throws ResourceNotFoundException if the user is not found.
     * @throws ValidationException if required, media links are missing (e.g., photo or video).
     */

    @Transactional
    public PostResponseDTO createPost(PostRequestDTO postRequestDTO, UUID userId) {
        User user = userService.findUserEntityById(userId);

        if (postRequestDTO.getPhotoLink() == null && postRequestDTO.getVideoLink() == null) {
            throw new ValidationException("Post must contain a photo or a video link.");
        }

        Post post = new Post();
        post.setTitle(postRequestDTO.getTitle());
        post.setDescription(postRequestDTO.getDescription());
        post.setPhotoLink(postRequestDTO.getPhotoLink());
        post.setVideoLink(postRequestDTO.getVideoLink());
        post.setPrivatePost(postRequestDTO.getPrivatePost());
        post.setUser(user);

        Post savedPost = postRepository.save(post);

        return postMapper.toDto(savedPost);
    }

    /**
     * Retrieves a paginated list of all public posts.
     *
     * @param pageable Pagination and sorting information.
     * @return A {@link Page} of public {@link PostResponseDTO}.
     */

    public Page<PostResponseDTO> getAllPosts(Pageable pageable) {
        Page<Post> postsPage = postRepository.findByPrivatePostFalse(pageable);
        List<PostResponseDTO> postResponseDTOs = postsPage.getContent().stream()
                // Antes: .map(this::mapToPostResponseDTO)
                .map(postMapper::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(postResponseDTOs, pageable, postsPage.getTotalElements());
    }

    /**
     * Retrieves a paginated list of all posts (public and private) belonging to a specific user.
     *
     * @param userId ID of the user.
     * @param pageable Pagination and sorting information.
     * @return A {@link Page} of {@link PostResponseDTO} for the user.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public Page<PostResponseDTO> getPostsByUserId(UUID userId, Pageable pageable) {
        User user = userService.findUserEntityById(userId);
        Page<Post> postsPage = postRepository.findByUser(user, pageable);
        List<PostResponseDTO> postResponseDTOs = postsPage.getContent().stream()
                .map(this::mapToPostResponseDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(postResponseDTOs, pageable, postsPage.getTotalElements());
    }

    /**
     * Retrieves a single post by ID, enforcing access control for private posts.
     *
     * @param id The ID of the post to retrieve.
     * @param currentUser The authenticated user.
     * @return {@link PostResponseDTO} of the post.
     * @throws ResourceNotFoundException if the post is not found.
     * @throws ValidationException if the post is private and the current user is not the owner.
     */
    public PostResponseDTO getPostById(Long id, User currentUser) {
        Post post = findPostEntityById(id);

        if (post.getPrivatePost() && !post.getUser().getId().equals(currentUser.getId())) {
            throw new ValidationException("Access denied. This post is private and only visible to the owner.");
        }

        return mapToPostResponseDTO(post);
    }
    /**
     * Updates an existing post.
     *
     * @param id ID of the post to update.
     * @param postRequestDTO DTO containing updated post-data.
     * @param userId ID of the authenticated user attempting the update.
     * @return {@link PostResponseDTO} of the updated post.
     * @throws ResourceNotFoundException if the post is not found.
     * @throws ValidationException if the authenticated user is not the owner of the post.
     */
    @Transactional
    public PostResponseDTO updatePost(Long id, PostRequestDTO postRequestDTO, UUID userId) {
        Post existingPost = validatePostOwnership(id, userId);

        existingPost.setTitle(postRequestDTO.getTitle());
        existingPost.setDescription(postRequestDTO.getDescription());
        existingPost.setPhotoLink(postRequestDTO.getPhotoLink());
        existingPost.setVideoLink(postRequestDTO.getVideoLink());
        existingPost.setPrivatePost(postRequestDTO.getPrivatePost());

        Post updatedPost = postRepository.save(existingPost);
        return mapToPostResponseDTO(updatedPost);
    }

    /**
     * Deletes a post (soft delete).
     *
     * @param id ID of the post to delete.
     * @param userId ID of the authenticated user attempting the deletion.
     * @throws ResourceNotFoundException if the post is not found.
     * @throws ValidationException if the authenticated user is not the owner of the post.
     */
    @Transactional
    public void deletePost(Long id, UUID userId) {
        Post postToDelete = validatePostOwnership(id, userId);

        // The entity's @SQLDelete annotation handles soft deletion here.
        postRepository.delete(postToDelete);
    }

    /**
     * Toggles the privacy status of a post.
     *
     * @param id ID of the post.
     * @param userId ID of the authenticated user attempting the toggle.
     * @return {@link PostResponseDTO} of the updated post.
     * @throws ResourceNotFoundException if the post is not found.
     * @throws ValidationException if the authenticated user is not the owner of the post.
     */
    @Transactional
    public PostResponseDTO togglePostPrivacy(Long id, UUID userId) {
        Post post = validatePostOwnership(id, userId);

        post.setPrivatePost(!post.getPrivatePost()); // Invert privacy state
        Post updatedPost = postRepository.save(post);
        return mapToPostResponseDTO(updatedPost);
    }

    /**
     * Increments the like count of a post.
     *
     * @param postId ID of the post to like.
     * @return {@link PostResponseDTO} of the post with the updated like count.
     * @throws ResourceNotFoundException if the post is not found.
     */
    @Transactional
    public PostResponseDTO likePost(Long postId) {
        Post post = findPostEntityById(postId);
        post.setLikesCount(post.getLikesCount() + 1);
        Post updatedPost = postRepository.save(post);
        return mapToPostResponseDTO(updatedPost);
    }

    /**
     * Finds a Post entity by ID, throwing ResourceNotFoundException if not found.
     * This is a utility method for internal use within the service.
     *
     * @param postId The ID of the post.
     * @return The Post entity.
     * @throws ResourceNotFoundException if the post is not found.
     */
    private Post findPostEntityById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));
    }

    /**
     * Validates that the authenticated user is the owner of the specified post.
     * This centralizes ownership validation logic.
     *
     * @param postId The ID of the post to validate.
     * @param userId The ID of the authenticated user.
     * @return The Post entity if the user is the owner.
     * @throws ResourceNotFoundException if the post is not found.
     * @throws ValidationException if the user is not the owner.
     */
    private Post validatePostOwnership(Long postId, UUID userId) {
        Post post = findPostEntityById(postId);
        if (!post.getUser().getId().equals(userId)) {
            throw new ValidationException("User does not have permission to modify this post.");
        }
        return post;
    }

    /**
     * Retrieves a paginated list of all public posts for a specific user.
     *
     * @param userId The ID of the user.
     * @param pageable Pagination and sorting information.
     * @return A {@link Page} of public {@link PostResponseDTO} for the user.
     */
    public Page<PostResponseDTO> getPublicPostsByUserId(UUID userId, Pageable pageable) {
        User user = userService.findUserEntityById(userId);
        Page<Post> postsPage = postRepository.findByUserAndPrivatePostFalse(user, pageable);

        List<PostResponseDTO> postResponseDTOs = postsPage.getContent().stream()
                .map(this::mapToPostResponseDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(postResponseDTOs, pageable, postsPage.getTotalElements());
    }

    /**
     * Retrieves all posts (public and private) created by a specific user.
     *
     * @param userId The ID of the user.
     * @return A {@link List} of {@link PostResponseDTO} representing the user's posts.
     */
    public List<PostResponseDTO> getAllPostsByUser(UUID userId) {
        User user = userService.findUserEntityById(userId);
        List<Post> posts = postRepository.findByUser(user);

        return posts.stream()
                .map(this::mapToPostResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Utility method to map a {@link Post} entity to a {@link PostResponseDTO}.
     * It uses {@link UserMapper} to map the associated User entity.
     *
     * @param post The Post entity.
     * @return The corresponding PostResponseDTO.
     */
    private PostResponseDTO mapToPostResponseDTO(Post post) {
        UserResponseDTO userResponseDTO = this.userMapper.toDto(post.getUser());

        return PostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .photoLink(post.getPhotoLink())
                .videoLink(post.getVideoLink())
                .privatePost(post.getPrivatePost())
                .likesCount(post.getLikesCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .userResponseDTO(userResponseDTO)
                .build();
    }
}