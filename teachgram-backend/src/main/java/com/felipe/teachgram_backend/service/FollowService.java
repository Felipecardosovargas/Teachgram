package com.felipe.teachgram_backend.service;

import com.felipe.teachgram_backend.dto.user.UserFollowDTO;
import com.felipe.teachgram_backend.entity.Follow;
import com.felipe.teachgram_backend.entity.User;
import com.felipe.teachgram_backend.repository.FollowRepository;
import com.felipe.teachgram_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Serviço responsável por gerenciar o relacionamento de "seguir" entre usuários,
 * incluindo seguir, deixar de seguir, e listar seguidores ou seguidos.
 *
 * Implementa uma modelagem unidirecional (estilo Instagram), onde um usuário pode seguir outro
 * sem necessidade de reciprocidade.
 */
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    /**
     * Registra que o usuário de ID {@code followerId} está seguindo o usuário {@code followingId}.
     *
     * @param followerId  ID do usuário que está seguindo.
     * @param followingId ID do usuário que será seguido.
     * @throws RuntimeException se os usuários não forem encontrados ou se o relacionamento já existir.
     */
    public void followUser(UUID followerId, UUID followingId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Usuário seguidor não encontrado"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("Usuário a seguir não encontrado"));

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new RuntimeException("Já está seguindo esse usuário");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        follow.setFollowedAt(LocalDateTime.now());

        followRepository.save(follow);
    }

    /**
     * Remove a relação de "seguir" entre o usuário {@code followerId} e {@code followingId}.
     *
     * @param followerId  ID do usuário que está deixando de seguir.
     * @param followingId ID do usuário que será deixado de seguir.
     */
    public void unfollowUser(UUID followerId, UUID followingId) {
        User follower = userRepository.findById(followerId).orElseThrow();
        User following = userRepository.findById(followingId).orElseThrow();
        followRepository.deleteByFollowerAndFollowing(follower, following);
    }

    /**
     * Retorna uma lista de usuários que seguem o usuário especificado.
     *
     * @param userId ID do usuário autenticado.
     * @return Lista de usuários que seguem o usuário, mapeada como DTOs leves.
     */
    public List<UserFollowDTO> getFollowers(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return followRepository.findAllByFollowing(user)
                .stream()
                .map(f -> toDTO(f.getFollower()))
                .toList();
    }

    /**
     * Retorna uma lista de usuários que o usuário especificado está seguindo.
     *
     * @param userId ID do usuário autenticado.
     * @return Lista de usuários seguidos, mapeada como DTOs leves.
     */
    public List<UserFollowDTO> getFollowing(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return followRepository.findAllByFollower(user)
                .stream()
                .map(f -> toDTO(f.getFollowing()))
                .toList();
    }

    /**
     * Converte a entidade {@link User} em {@link UserFollowDTO}, contendo apenas dados necessários para o frontend.
     *
     * @param user Entidade do usuário.
     * @return DTO com dados de identificação e perfil.
     */
    private UserFollowDTO toDTO(User user) {
        return new UserFollowDTO(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getProfileLink()
        );
    }
}
