package com.felipe.teachgram_backend.service;

import com.felipe.teachgram_backend.dto.post.PostRequestDTO;
import com.felipe.teachgram_backend.dto.post.PostResponseDTO;
import com.felipe.teachgram_backend.dto.user.UserResponseDTO;
import com.felipe.teachgram_backend.entity.Post;
import com.felipe.teachgram_backend.entity.User;
import com.felipe.teachgram_backend.exception.ResourceNotFoundException;
import com.felipe.teachgram_backend.exception.ValidationException;
import com.felipe.teachgram_backend.mapper.PostMapper;
import com.felipe.teachgram_backend.mapper.UserMapper;
import com.felipe.teachgram_backend.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class PostServiceTest {
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserService userService;
    @Mock
    private PostMapper postMapper;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private PostService postService;

    private User user;
    private Post post;
    private PostRequestDTO postRequestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(UUID.fromString("2a5e88c8-319a-486e-90b0-630627462a77"));
        post = new Post();
        post.setId(1L);
        post.setUser(user);
        post.setPrivatePost(false);
        post.setLikesCount(0);

        postRequestDTO = new PostRequestDTO();
        postRequestDTO.setTitle("Title");
        postRequestDTO.setDescription("Desc");
        postRequestDTO.setPhotoLink("http://photo.com/img.jpg");
        postRequestDTO.setPrivatePost(false);
    }

    @Test
    void createPost_success() {
        // Arrange
        postRequestDTO.setTitle("Título de Teste");
        postRequestDTO.setDescription("Descrição de Teste");
        postRequestDTO.setPhotoLink("http://foto.com/teste.jpg");
        postRequestDTO.setVideoLink(null);
        postRequestDTO.setPrivatePost(true);

        when(userService.findUserEntityById(user.getId())).thenReturn(user);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        PostResponseDTO expectedResponse = PostResponseDTO.builder()
                .id(1L)
                .title(postRequestDTO.getTitle())
                .description(postRequestDTO.getDescription())
                .photoLink(postRequestDTO.getPhotoLink())
                .videoLink(postRequestDTO.getVideoLink())
                .privatePost(postRequestDTO.getPrivatePost())
                .likesCount(0)
                .userResponseDTO(UserResponseDTO.builder().id(user.getId()).build())
                .build();

        when(postMapper.toDto(any(Post.class))).thenReturn(expectedResponse);

        // Act
        PostResponseDTO response = postService.createPost(postRequestDTO, user.getId());

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo(postRequestDTO.getTitle());
        assertThat(response.getDescription()).isEqualTo(postRequestDTO.getDescription());
        assertThat(response.getPhotoLink()).isEqualTo(postRequestDTO.getPhotoLink());
        assertThat(response.getPrivatePost()).isTrue();

        verify(userService).findUserEntityById(user.getId());
        verify(postRepository).save(any(Post.class));
        verify(postMapper).toDto(any(Post.class));
    }

    @Test
    void createPost_withoutMedia_throws() {
        // Arrange
        postRequestDTO.setPhotoLink(null);
        postRequestDTO.setVideoLink(null);
        when(userService.findUserEntityById(user.getId())).thenReturn(user);

        // Act & Assert
        assertThatThrownBy(() -> postService.createPost(postRequestDTO, user.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Post must contain a photo or a video link");

        verify(userService).findUserEntityById(user.getId());
        verifyNoInteractions(postMapper);
        verify(postRepository, never()).save(any());
    }

    @Test
    void getAllPosts_success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        Post post = new Post();
        post.setId(123L);
        post.setTitle("Título público");
        post.setDescription("Descrição do post");
        post.setPhotoLink("http://img.com/foto.jpg");
        post.setVideoLink(null);
        post.setPrivatePost(false);
        post.setLikesCount(10);
        post.setCreatedAt(LocalDate.now());
        post.setUpdatedAt(LocalDate.now());
        post.setUser(user);

        PostResponseDTO postResponseDTO = PostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .photoLink(post.getPhotoLink())
                .videoLink(post.getVideoLink())
                .privatePost(post.getPrivatePost())
                .likesCount(post.getLikesCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .userResponseDTO(UserResponseDTO.builder()
                        .id(user.getId())
                        .userName(user.getUsername())
                        .email(user.getEmail())
                        .build())
                .build();

        List<Post> posts = List.of(post);
        Page<Post> postPage = new PageImpl<>(posts, pageable, posts.size());

        when(postRepository.findByPrivatePostFalse(pageable)).thenReturn(postPage);
        when(postMapper.toDto(post)).thenReturn(postResponseDTO);

        // Act
        Page<PostResponseDTO> responsePage = postService.getAllPosts(pageable);

        // Assert
        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getContent()).hasSize(1);

        PostResponseDTO responsePost = responsePage.getContent().get(0);
        assertThat(responsePost.getId()).isEqualTo(post.getId());
        assertThat(responsePost.getTitle()).isEqualTo(post.getTitle());
        assertThat(responsePost.getDescription()).isEqualTo(post.getDescription());
        assertThat(responsePost.getPhotoLink()).isEqualTo(post.getPhotoLink());
        assertThat(responsePost.getLikesCount()).isEqualTo(post.getLikesCount());
        assertThat(responsePost.getUserResponseDTO().getId()).isEqualTo(user.getId());

        verify(postRepository, times(1)).findByPrivatePostFalse(pageable);
        verify(postMapper, times(1)).toDto(post);
        verifyNoMoreInteractions(postRepository, postMapper);
    }


    @Test
    void getPostsByUserId_success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("john_doe");
        user.setEmail("john@example.com");

        Post post = new Post();
        post.setId(1L);
        post.setTitle("Título");
        post.setDescription("Descrição");
        post.setPhotoLink("http://image.com/foto.jpg");
        post.setVideoLink(null);
        post.setPrivatePost(false);
        post.setLikesCount(5);
        post.setCreatedAt(LocalDate.now());
        post.setUpdatedAt(LocalDate.now());
        post.setUser(user);

        UserResponseDTO userResponseDTO = UserResponseDTO.builder()
                .id(user.getId())
                .userName(user.getUsername())
                .email(user.getEmail())
                .build();

        PostResponseDTO postResponseDTO = PostResponseDTO.builder()
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

        Page<Post> postPage = new PageImpl<>(List.of(post));

        when(userService.findUserEntityById(user.getId())).thenReturn(user);
        when(postRepository.findByUser(user, pageable)).thenReturn(postPage);
        when(userMapper.toDto(user)).thenReturn(userResponseDTO);

        // Act
        Page<PostResponseDTO> responsePage = postService.getPostsByUserId(user.getId(), pageable);

        // Assert
        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getContent()).hasSize(1);

        PostResponseDTO response = responsePage.getContent().get(0);
        assertThat(response.getId()).isEqualTo(post.getId());
        assertThat(response.getTitle()).isEqualTo(post.getTitle());
        assertThat(response.getDescription()).isEqualTo(post.getDescription());
        assertThat(response.getPhotoLink()).isEqualTo(post.getPhotoLink());
        assertThat(response.getLikesCount()).isEqualTo(post.getLikesCount());
        assertThat(response.getUserResponseDTO().getId()).isEqualTo(user.getId());
        assertThat(response.getUserResponseDTO().getEmail()).isEqualTo(user.getEmail());

        verify(userService, times(1)).findUserEntityById(user.getId());
        verify(postRepository, times(1)).findByUser(user, pageable);
        verify(userMapper, times(1)).toDto(user);
        verifyNoMoreInteractions(userService, postRepository, userMapper);
    }

    @Test
    void getPublicPostsByUserId_success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");

        Post post = new Post();
        post.setId(1L);
        post.setTitle("Public Post");
        post.setDescription("Descrição do post público");
        post.setPhotoLink("https://cdn.example.com/image.jpg");
        post.setVideoLink(null);
        post.setPrivatePost(false);
        post.setLikesCount(10);
        post.setCreatedAt(LocalDate.now());
        post.setUpdatedAt(LocalDate.now());
        post.setUser(user);

        UserResponseDTO userDTO = UserResponseDTO.builder()
                .id(userId)
                .userName(user.getUsername())
                .email(user.getEmail())
                .build();

        PostResponseDTO postDTO = PostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .photoLink(post.getPhotoLink())
                .videoLink(post.getVideoLink())
                .privatePost(post.getPrivatePost())
                .likesCount(post.getLikesCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .userResponseDTO(userDTO)
                .build();

        Page<Post> postPage = new PageImpl<>(List.of(post));

        when(userService.findUserEntityById(userId)).thenReturn(user);
        when(postRepository.findByUserAndPrivatePostFalse(user, pageable)).thenReturn(postPage);
        when(userMapper.toDto(user)).thenReturn(userDTO);

        // Act
        Page<PostResponseDTO> responsePage = postService.getPublicPostsByUserId(userId, pageable);

        // Assert
        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getTotalElements()).isEqualTo(1);
        PostResponseDTO response = responsePage.getContent().get(0);

        assertThat(response.getId()).isEqualTo(post.getId());
        assertThat(response.getTitle()).isEqualTo(post.getTitle());
        assertThat(response.getDescription()).isEqualTo(post.getDescription());
        assertThat(response.getPhotoLink()).isEqualTo(post.getPhotoLink());
        assertThat(response.getPrivatePost()).isFalse();
        assertThat(response.getLikesCount()).isEqualTo(post.getLikesCount());

        assertThat(response.getUserResponseDTO()).isNotNull();
        assertThat(response.getUserResponseDTO().getId()).isEqualTo(user.getId());
        assertThat(response.getUserResponseDTO().getUserName()).isEqualTo(user.getUsername());

        // Verify interactions
        verify(userService).findUserEntityById(userId);
        verify(postRepository).findByUserAndPrivatePostFalse(user, pageable);
        verify(userMapper).toDto(user);
        verifyNoMoreInteractions(userService, postRepository, userMapper);
    }

    @Test
    void getAllPostsByUser_success() {
        // Arrange
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");

        Post post = new Post();
        post.setId(1L);
        post.setTitle("Título do Post");
        post.setDescription("Descrição do Post");
        post.setPhotoLink("https://cdn.example.com/image.jpg");
        post.setVideoLink(null);
        post.setPrivatePost(false);
        post.setLikesCount(5);
        post.setCreatedAt(LocalDate.now());
        post.setUpdatedAt(LocalDate.now());
        post.setUser(user);

        UserResponseDTO userDTO = UserResponseDTO.builder()
                .id(userId)
                .userName(user.getUsername())
                .email(user.getEmail())
                .build();

        PostResponseDTO postDTO = PostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .photoLink(post.getPhotoLink())
                .videoLink(post.getVideoLink())
                .privatePost(post.getPrivatePost())
                .likesCount(post.getLikesCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .userResponseDTO(userDTO)
                .build();

        when(userService.findUserEntityById(userId)).thenReturn(user);
        when(postRepository.findByUser(user)).thenReturn(List.of(post));
        when(userMapper.toDto(user)).thenReturn(userDTO);

        // Act
        List<PostResponseDTO> responseList = postService.getAllPostsByUser(userId);

        // Assert
        assertThat(responseList).isNotNull().hasSize(1);

        PostResponseDTO response = responseList.get(0);
        assertThat(response.getId()).isEqualTo(post.getId());
        assertThat(response.getTitle()).isEqualTo(post.getTitle());
        assertThat(response.getDescription()).isEqualTo(post.getDescription());
        assertThat(response.getPhotoLink()).isEqualTo(post.getPhotoLink());
        assertThat(response.getPrivatePost()).isFalse();
        assertThat(response.getLikesCount()).isEqualTo(post.getLikesCount());
        assertThat(response.getUserResponseDTO()).isNotNull();
        assertThat(response.getUserResponseDTO().getId()).isEqualTo(user.getId());

        // Verificações de interação
        verify(userService).findUserEntityById(userId);
        verify(postRepository).findByUser(user);
        verify(userMapper).toDto(user);
        verifyNoMoreInteractions(userService, postRepository, userMapper);
    }

    @Test
    void getPostById_privateByOtherUser_throws() {
        // Arrange
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        User owner = new User();
        owner.setId(ownerId);

        User otherUser = new User();
        otherUser.setId(otherUserId);

        Post privatePost = new Post();
        privatePost.setId(1L);
        privatePost.setPrivatePost(true);
        privatePost.setUser(owner);

        when(postRepository.findById(privatePost.getId())).thenReturn(Optional.of(privatePost));

        // Act & Assert
        assertThatThrownBy(() -> postService.getPostById(privatePost.getId(), otherUser))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Access denied");

        // Verifica que o post foi realmente buscado
        verify(postRepository).findById(privatePost.getId());
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    void getPostById_success() {
        // Arrange
        post.setPrivatePost(false);  // Garantir que é um post público (ou do próprio usuário)
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(userMapper.toDto(any())).thenReturn(new UserResponseDTO());

        // Act
        PostResponseDTO response = postService.getPostById(post.getId(), user);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(post.getId());
        assertThat(response.getTitle()).isEqualTo(post.getTitle());
        assertThat(response.getUserResponseDTO()).isNotNull();

        // Verify interactions
        verify(postRepository).findById(post.getId());
        verify(userMapper).toDto(post.getUser());
        verifyNoMoreInteractions(postRepository, userMapper);
    }

    @Test
    void updatePost_success() {
        // Arrange
        post.setUser(user);  // Garantir que o post pertence ao usuário correto
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Retorna o objeto salvo
        when(userMapper.toDto(any())).thenReturn(new UserResponseDTO());

        // Atualiza o DTO para dados novos e diferentes para garantir atualização
        postRequestDTO.setTitle("Updated Title");
        postRequestDTO.setDescription("Updated Description");
        postRequestDTO.setPhotoLink("updated-photo-link.jpg");
        postRequestDTO.setVideoLink("updated-video-link.mp4");
        postRequestDTO.setPrivatePost(true);

        // Act
        PostResponseDTO response = postService.updatePost(post.getId(), postRequestDTO, user.getId());

        // Assert
        assertThat(response).isNotNull();
        assertThat(post.getTitle()).isEqualTo(postRequestDTO.getTitle());
        assertThat(post.getDescription()).isEqualTo(postRequestDTO.getDescription());
        assertThat(post.getPhotoLink()).isEqualTo(postRequestDTO.getPhotoLink());
        assertThat(post.getVideoLink()).isEqualTo(postRequestDTO.getVideoLink());
        assertThat(post.getPrivatePost()).isEqualTo(postRequestDTO.getPrivatePost());

        // Verify repository interactions
        verify(postRepository).findById(post.getId());
        verify(postRepository).save(post);
        verify(userMapper).toDto(post.getUser());
        verifyNoMoreInteractions(postRepository, userMapper);
    }

    @Test
    void updatePost_byWrongUser_throws() {
        // Arrange
        post.setUser(user);  // Dono original do post (diferente do otherUser)
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        // Act & Assert
        assertThatThrownBy(() -> postService.updatePost(post.getId(), postRequestDTO, otherUser.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("User does not have permission");

        // Verifica que save não foi chamado
        verify(postRepository, never()).save(any());
    }

    @Test
    void deletePost_success() {
        // Arrange
        post.setUser(user); // garante que o post pertence ao user
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        // Act & Assert
        assertDoesNotThrow(() -> postService.deletePost(post.getId(), user.getId()));

        // Verifica se delete foi chamado exatamente 1 vez com o post esperado
        verify(postRepository, times(1)).delete(post);

        // Verifica se findById foi chamado corretamente
        verify(postRepository, times(1)).findById(post.getId());
    }

    @Test
    void togglePostPrivacy_success() {
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(postRepository.save(any())).thenReturn(post);
        when(userMapper.toDto(any())).thenReturn(new UserResponseDTO());

        PostResponseDTO result = postService.togglePostPrivacy(post.getId(), user.getId());

        assertThat(result).isNotNull();
    }

    @Test
    void likePost_success() {
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(postRepository.save(any())).thenReturn(post);
        when(userMapper.toDto(any())).thenReturn(new UserResponseDTO());

        PostResponseDTO response = postService.likePost(post.getId());

        assertThat(response).isNotNull();
    }

    @Test
    void findPostEntityById_notFound_throws() {
        when(postRepository.findById(post.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.likePost(post.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
