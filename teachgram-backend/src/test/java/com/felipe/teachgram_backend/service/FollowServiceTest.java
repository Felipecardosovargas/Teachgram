package com.felipe.teachgram_backend.service;

import com.felipe.teachgram_backend.dto.user.UserFollowDTO;
import com.felipe.teachgram_backend.entity.Follow;
import com.felipe.teachgram_backend.entity.User;
import com.felipe.teachgram_backend.repository.FollowRepository;
import com.felipe.teachgram_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FollowServiceTest {

    @InjectMocks
    private FollowService followService;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    private UUID followerId;
    private UUID followingId;
    private User follower;
    private User following;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        followerId = UUID.randomUUID();
        followingId = UUID.randomUUID();

        follower = new User();
        follower.setId(followerId);
        follower.setName("Follower User");
        follower.setUsername("follower");
        follower.setProfileLink("profileFollower");

        following = new User();
        following.setId(followingId);
        following.setName("Following User");
        following.setUsername("following");
        following.setProfileLink("profileFollowing");
    }

    @Test
    void followUser_success() {
        // Arrange
        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followingId)).thenReturn(Optional.of(following));
        when(followRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(false);
        when(followRepository.save(any(Follow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        followService.followUser(followerId, followingId);

        // Assert
        verify(userRepository, times(1)).findById(followerId);
        verify(userRepository, times(1)).findById(followingId);
        verify(followRepository, times(1)).existsByFollowerAndFollowing(follower, following);
        ArgumentCaptor<Follow> captor = ArgumentCaptor.forClass(Follow.class);
        verify(followRepository).save(captor.capture());

        Follow savedFollow = captor.getValue();
        assertThat(savedFollow.getFollower()).isEqualTo(follower);
        assertThat(savedFollow.getFollowing()).isEqualTo(following);
        assertThat(savedFollow.getFollowedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void followUser_followerNotFound_throws() {
        // Arrange
        when(userRepository.findById(followerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> followService.followUser(followerId, followingId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário seguidor não encontrado");

        verify(userRepository).findById(followerId);
        verify(userRepository, never()).findById(followingId);
        verify(followRepository, never()).existsByFollowerAndFollowing(any(), any());
        verify(followRepository, never()).save(any());
    }

    @Test
    void followUser_followingNotFound_throws() {
        // Arrange
        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> followService.followUser(followerId, followingId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário a seguir não encontrado");

        verify(userRepository).findById(followerId);
        verify(userRepository).findById(followingId);
        verify(followRepository, never()).existsByFollowerAndFollowing(any(), any());
        verify(followRepository, never()).save(any());
    }

    @Test
    void followUser_alreadyFollowing_throws() {
        // Arrange
        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followingId)).thenReturn(Optional.of(following));
        when(followRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> followService.followUser(followerId, followingId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Já está seguindo esse usuário");

        verify(followRepository, times(1)).existsByFollowerAndFollowing(follower, following);
        verify(followRepository, never()).save(any());
    }

    @Test
    void unfollowUser_success() {
        // Arrange
        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followingId)).thenReturn(Optional.of(following));

        // Act
        followService.unfollowUser(followerId, followingId);

        // Assert
        verify(userRepository).findById(followerId);
        verify(userRepository).findById(followingId);
        verify(followRepository).deleteByFollowerAndFollowing(follower, following);
    }

    @Test
    void unfollowUser_followerNotFound_throws() {
        // Arrange
        when(userRepository.findById(followerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> followService.unfollowUser(followerId, followingId))
                .isInstanceOf(NoSuchElementException.class);

        verify(userRepository).findById(followerId);
        verify(userRepository, never()).findById(followingId);
        verify(followRepository, never()).deleteByFollowerAndFollowing(any(), any());
    }

    @Test
    void unfollowUser_followingNotFound_throws() {
        // Arrange
        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> followService.unfollowUser(followerId, followingId))
                .isInstanceOf(NoSuchElementException.class);

        verify(userRepository).findById(followerId);
        verify(userRepository).findById(followingId);
        verify(followRepository, never()).deleteByFollowerAndFollowing(any(), any());
    }

    @Test
    void getFollowers_success() {
        // Arrange
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        follow.setFollowedAt(LocalDateTime.now());

        when(userRepository.findById(followingId)).thenReturn(Optional.of(following));
        when(followRepository.findAllByFollowing(following)).thenReturn(List.of(follow));

        // Act
        List<UserFollowDTO> followers = followService.getFollowers(followingId);

        // Assert
        assertThat(followers).hasSize(1);
        assertThat(followers.get(0).id()).isEqualTo(followerId);
        assertThat(followers.get(0).name()).isEqualTo(follower.getName());
        assertThat(followers.get(0).username()).isEqualTo(follower.getUsername());
        assertThat(followers.get(0).profileLink()).isEqualTo(follower.getProfileLink());
    }

    @Test
    void getFollowers_userNotFound_throws() {
        // Arrange
        when(userRepository.findById(followingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> followService.getFollowers(followingId))
                .isInstanceOf(NoSuchElementException.class);

        verify(userRepository).findById(followingId);
        verify(followRepository, never()).findAllByFollowing(any());
    }

    @Test
    void getFollowing_success() {
        // Arrange
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        follow.setFollowedAt(LocalDateTime.now());

        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(followRepository.findAllByFollower(follower)).thenReturn(List.of(follow));

        // Act
        List<UserFollowDTO> followingList = followService.getFollowing(followerId);

        // Assert
        assertThat(followingList).hasSize(1);
        assertThat(followingList.get(0).id()).isEqualTo(followingId);
        assertThat(followingList.get(0).name()).isEqualTo(following.getName());
        assertThat(followingList.get(0).username()).isEqualTo(following.getUsername());
        assertThat(followingList.get(0).profileLink()).isEqualTo(following.getProfileLink());
    }

    @Test
    void getFollowing_userNotFound_throws() {
        // Arrange
        when(userRepository.findById(followerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> followService.getFollowing(followerId))
                .isInstanceOf(NoSuchElementException.class);

        verify(userRepository).findById(followerId);
        verify(followRepository, never()).findAllByFollower(any());
    }
}
