package com.ground.domain.board.service;

import com.ground.domain.board.dto.BoardRequestDto;
import com.ground.domain.board.dto.BoardResponseDto;
import com.ground.domain.board.dto.CommentRequestDto;
import com.ground.domain.board.entity.*;
import com.ground.domain.board.repository.*;
import com.ground.domain.follow.entity.Follow;
import com.ground.domain.global.entity.Category;
import com.ground.domain.global.entity.Location;
import com.ground.domain.global.repository.CategoryRepository;
import com.ground.domain.global.repository.LocationRepository;
import com.ground.domain.notification.entity.NotificationBoard;
import com.ground.domain.notification.repository.NotificationBoardRepository;
import com.ground.domain.search.repository.sUserRepository;
import com.ground.domain.user.entity.User;
import com.ground.domain.user.entity.UserCategory;
import com.ground.domain.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardService {

//    private final UserRepository userRepository;

    private final BoardRepository boardRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final BoardImageRepository boardImageRepository;
    private final sUserRepository userRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final BoardSaveRepository boardSaveRepository;
    private final NotificationBoardRepository notificationBoardRepository;

    private final BoardFollowRepository followRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private final CommentRepository commentReository;


    // 게시글 생성
    @Transactional
    public Board addBoard(BoardRequestDto params, User user) {

        Board board = params.toEntity();

        //카테고리, 지역
        Category category = categoryRepository.findById(params.getCategoryId()).get();
        Location location = locationRepository.findById(params.getLocationId()).get();
        board.setCategory(category);
        board.setLocation(location);

        // 유저, 작성시간, 공개유무
        board.setUser(user);
        board.setPrivateYN(params.isPrivateYN());
        board.setRegDttm(LocalDateTime.now());

        Board entity = boardRepository.save(board);

        // 사진들 엔티티 생성하고 setBoard 해주기
        for (BoardImage image : params.getImages()) {
           BoardImage boardImage = BoardImage.builder()
                   .board(entity)
                   .imageType(image.getImageType())
                   .imageUrl(image.getImageUrl()).build();
           boardImageRepository.save(boardImage);
        }
        return entity;
    }

    // 게시글 조회
    @Transactional
    public Board getBoard(Long boardId) {

        // 게시글 찾기
        Board board = boardRepository.findById(boardId).get();
        return board;
    }


    // 게시글 수정
    @Transactional
    public Board updateBoard(Long boardId, BoardRequestDto params, User user) {
        // 게시글 찾기
        Board board = boardRepository.findById(boardId).get();

        if (user != board.getUser()) {
            throw new RuntimeException("사용자가 해당 글의 작성자가 아닙니다.");
        }


        // 카테고리, 지역
        Category category = categoryRepository.findById(params.getCategoryId()).get();
        Location location = locationRepository.findById(params.getLocationId()).get();
        board.setCategory(category);
        board.setLocation(location);

        //수정한 유저, 시간, 공개유무
        board.setModUser(user);
        board.setModDttm(LocalDateTime.now());
        board.setPrivateYN(params.isPrivateYN());

        // 게시글 내용
        board.setContent(params.getContent());

        // 기존 이미지 삭제
        final List<BoardImage> boardImages = boardImageRepository.findAllByBoard(board);
        if (boardImages != null) {
            board.getImages().clear();
            for (BoardImage boardImage : boardImages) {
                boardImageRepository.delete(boardImage);
            }
        }

        // 사진들 엔티티 생성하고 setBoard 해주기
        for (BoardImage image : params.getImages()) {
            BoardImage boardImage = BoardImage.builder()
                    .board(board)
                    .imageType(image.getImageType())
                    .imageUrl(image.getImageUrl()).build();
            boardImageRepository.save(boardImage);
        }
        Board entity = boardRepository.save(board);
        return entity;

    }


    // 게시글 삭제
    // 로그인 유저 확인 필요함 (수정도)
    @Transactional
    public void deleteBoard(Long boardId, User user) {
        Board board = boardRepository.findById(boardId).get();

        if (user != board.getUser()) {
            throw new RuntimeException("사용자가 해당 글의 작성자가 아닙니다.");
        }

        boardRepository.delete(board);

    }

    // 게시글 좋아요
    // 로그인 유저 확인 필요함 (수정도)
    @Transactional
    public void likeBoard(Long boardId, User user) {
        Board board = boardRepository.findById(boardId).get();
        for (BoardLike boardLike : board.getBoardLikes()) {
            if (boardLike.getUser().equals(user)) {
                throw new RuntimeException("이미 좋아하는 게시글입니다.");
            }
        }
        board.setLikeCnt(board.getLikeCnt()+1);
        boardLikeRepository.save(new BoardLike(user, board));

        User to = board.getUser();
        notificationBoardRepository.save(new NotificationBoard(user, to, boardId, true, LocalDateTime.now()));
    }

    // 게시글 좋아요 취소
    // 로그인 유저 확인 필요함
    @Transactional
    public void unLikeBoard(Long boardId, User user) {

        Board board = boardRepository.findById(boardId).get();
        BoardLike boardLike = boardLikeRepository.findByUserAndBoard(user, board).get();
        boardLikeRepository.delete(boardLike);
        board.setLikeCnt(board.getLikeCnt()-1);

    }


    // 게시글 저장
    @Transactional
    public void saveBoard(Long boardId, User user) {
        Board board = boardRepository.findById(boardId).get();
        for (BoardSave boardSave : board.getBoardSaves()) {
            if (boardSave.getUser().equals(user)) {
                throw new RuntimeException("이미 저장한 게시글입니다.");
            }
        }
        boardSaveRepository.save(new BoardSave(user, board));
        board.setSaveCnt(board.getSaveCnt()+1);
    }

    // 게시글 저장 취소
    // 로그인 유저 확인 필요함
    @Transactional
    public void unSaveBoard(Long boardId, User user) {

        Board board = boardRepository.findById(boardId).get();
        BoardSave boardSave = boardSaveRepository.findByUserAndBoard(user, board).get();
        board.setSaveCnt(board.getSaveCnt()-1);
        boardSaveRepository.delete(boardSave);
    }



    // ================= 관심종목 피드 조회 ========================
    @Transactional
    public List<BoardResponseDto> getInterestBoard(User user, int pageNumber) {

        // 카테고리
        List<Long> categoryIdList = new ArrayList<>();
        List<BoardResponseDto> lst = new ArrayList<>();
        for (UserCategory userCategory : user.getUserCategories()) { categoryIdList.add(userCategory.getCategory().getId()); }

        List<User> userList = new ArrayList<>();
        // 작성자가 공개유저
        List<User> openUserList = userRepository.findAllByPrivateYN(false);
        userList.addAll(openUserList);
        // 작성자가 팔로우 유저
        List<Follow> followList = followRepository.findAllByFromUserIdAndFlag(user, true);
        for (Follow follow : followList) userList.add(follow.getToUserId());
        // 작성자가 나
        userList.add(user);

        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("id").descending());

        // 카테고리 포함 AND 게시글이 공개 글 AND 작성자가 공개유저 OR 작성자가 팔로우 유저 OR 작성자가 나
        Page<Board> boardList = boardRepository.findAllByCategoryIdInAndUserInAndPrivateYN(categoryIdList, userList, false, pageable);
        for (Board board : boardList) { lst.add(new BoardResponseDto(board, user)); }
//        Collections.sort(lst, boardComparator);
        return lst;
    }



    // ================= 팔로우 피드 조회 ====================
    @Transactional
    public List<BoardResponseDto> getFollowBoard(User user, int pageNumber) {

        List<Follow> followList = followRepository.findAllByFromUserIdAndFlag(user, true);
        List<User> userList = new ArrayList<>();
        for (Follow follow : followList) userList.add(follow.getToUserId());

        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("id").descending());
        Page<Board> boardList = boardRepository.findAllByUserInAndPrivateYN(userList, false, pageable);
        List<BoardResponseDto> lst = new ArrayList<>();

        for (Board board : boardList) {
            lst.add(new BoardResponseDto(board, user));
        }

//        Collections.sort(lst, boardComparator);
        return lst;
    }

    // =================== 댓글 생성 =======================
    @Transactional
    public Comment addComment(CommentRequestDto params, Long boardId, User user) {
        Comment comment = params.toEntity();
        comment.setUser(user);
        Board board = boardRepository.findById(boardId).get();
        comment.setBoard(board);
        comment.setRegDttm(LocalDateTime.now());
        Comment entity = commentReository.save(comment);
        board.setCommentCnt(board.getCommentCnt()+1);

        User to = board.getUser();

        notificationBoardRepository.save(new NotificationBoard(user, to, boardId, false, LocalDateTime.now()));

        return entity;
    }

    // ===================== 댓글 수정 ====================
    @Transactional
    public Comment updateComment(CommentRequestDto params, Long commentId, User user) {
        // 유저 == 게시글 작성자 확인 필요
        Comment comment = commentReository.findById(commentId).get();
        if (user != comment.getUser()) {
            throw new RuntimeException("사용자가 해당 댓글의 작성자가 아닙니다.");
        }
        comment.setModDttm(LocalDateTime.now());
        comment.setReply(params.getReply());
        Comment entity = commentReository.save(comment);
        return entity;

    }
    // ===================== 댓글 삭제 ======================
    @Transactional
    public void deleteComment(Long commentId, User user) {
        // 유저 == 게시글 작성자 확인 필요
        Comment comment = commentReository.findById(commentId).get();
        if (user != comment.getUser()) {
            throw new RuntimeException("사용자가 해당 댓의 작성자가 아닙니다.");
        }
        commentReository.deleteById(commentId);
        Board board = boardRepository.findById(comment.getBoard().getId()).get();
        board.setCommentCnt(board.getCommentCnt()-1);
    }

    // -----------------BSH-----------------
    // 유저가 쓴 피드 조회
    @Transactional
    public List<BoardResponseDto> getMyBoard(long userId, int pageNumber, User loginUser) {

        List<BoardResponseDto> result = new ArrayList<>();
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("id").descending());
        List<Board> boardList = boardRepository.findAllByUserIdOrderByRegDttmDesc(userId, pageable);

        for (Board board : boardList) {
            result.add(new BoardResponseDto(board, loginUser));
        }
        return result;
    }

    // 저장한 피드 조회
    @Transactional
    public List<BoardResponseDto> getSaveBoard(long userId, int pageNumber, User loginUser) {
        List<Long> boardIdList = new ArrayList<>();
        List<BoardSave> saveList = boardSaveRepository.findAllByUserId(userId);
        for (BoardSave boardSave : saveList) boardIdList.add(boardSave.getBoard().getId());

        List<User> userList = new ArrayList<>();
        // 작성자가 공개유저
        List<User> openUserList = userRepository.findAllByPrivateYN(false);
        userList.addAll(openUserList);
        // 작성자가 팔로우 유저
        List<Follow> followList = followRepository.findAllByFromUserIdAndFlag(loginUser, true);
        for (Follow follow : followList) userList.add(follow.getToUserId());
        // 작성자가 나
        userList.add(loginUser);

        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("id").descending());
        List<Board> boardList = boardRepository.findAllByIdInAndUserInAndPrivateYNOrderByRegDttmDesc(boardIdList, userList,false, pageable);
        List<BoardResponseDto> result = new ArrayList<>();
        for (Board board : boardList) result.add(new BoardResponseDto(board, loginUser));

        return result;
    }
}