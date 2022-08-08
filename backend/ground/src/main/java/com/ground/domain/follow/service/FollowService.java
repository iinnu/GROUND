package com.ground.domain.follow.service;

import com.ground.domain.user.dto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.qlrm.mapper.JpaResultMapper;

import com.ground.domain.follow.repository.FollowRepository;
import com.ground.domain.follow.dto.FollowDto;

import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final EntityManager em;

    // 팔로우
    @Transactional
    public void follow(Long fromUserId, Long toUserId) {
//        if(followRepository.findFollowByFromUserIdAndToUserId(fromUserId, toUserId) != null) throw new CustomApiException("이미 팔로우 하였습니다.");
        followRepository.follow(fromUserId, toUserId);
    }

    // 언팔로우
    @Transactional
    public void unFollow(long fromUserId, long toUserId) {

        followRepository.unFollow(fromUserId, toUserId);
    }

    // 팔로워 목록 조회
    @Transactional
    public List<FollowDto> getFollower(long profileId, long userId) {
        StringBuffer sb = new StringBuffer();

        // 3. userId와 userNickname 을 가져옴
        sb.append("SELECT u.id, u.nickname, u.user_image,");
        // 4. 그중 fromUserId(팔로워)가 userId(로그인한 유저) 이면 followState 를 True로 해줌
        sb.append("if ((SELECT 1 FROM t_user_follow WHERE from_user_id = ? AND to_user_id = u.id), TRUE, FALSE) AS followState, ");
        // 5.
        sb.append("if ((?=u.id), TRUE, FALSE) AS loginUser ");
        // 1. follow 테이블에서 (user 테이블 정보도 들고옴)
        sb.append("FROM t_user u, t_user_follow f ");
        // 2. fromUserId 가 userId 인것중 toUserId 가 profileId 인 <-> (profileId 를 팔로우 하고 있는 유저의)
        sb.append("WHERE u.id = f.from_user_id AND f.to_user_id = ?");

        Query query = em.createNativeQuery(sb.toString())
                .setParameter(1, userId)
                .setParameter(2, userId)
                .setParameter(3, profileId);

        JpaResultMapper result = new JpaResultMapper();
        List<FollowDto> followDtoList = result.list(query, FollowDto.class);
        return followDtoList;
    }

    // 팔로잉 목록 조회
    @Transactional
    public List<FollowDto> getFollowing(long profileId, long userId) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT u.id, u.nickname, u.user_image, ");
        sb.append("if ((SELECT 1 FROM t_user_follow WHERE from_user_id = ? AND to_user_id = u.id), TRUE, FALSE) AS followState, ");
        sb.append("if ((?=u.id), TRUE, FALSE) AS loginUser ");
        sb.append("FROM t_user u, t_user_follow f ");
        sb.append("WHERE u.id = f.to_user_id AND f.from_user_id = ?");

        Query query = em.createNativeQuery(sb.toString())
                .setParameter(1, userId)
                .setParameter(2, userId)
                .setParameter(3, profileId);

        JpaResultMapper result = new JpaResultMapper();
        List<FollowDto> followDtoList = result.list(query, FollowDto.class);
        return followDtoList;
    }
}