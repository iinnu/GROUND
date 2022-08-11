import { Grid, IconButton } from "@mui/material";
import ChatBubbleOutlineIcon from "@mui/icons-material/ChatBubbleOutline";
import Comment from "./Comment";
import { useEffect, useState } from "react";
import { getUserState } from "api/user";
import { deleteComment, registerComment } from "api/comment";
import { useParams } from "react-router-dom";
import CommentBox from "./CommentBox";

function ArticleDetailComment({ commentList }) {
  const { boardId } = useParams();
  const [userId, setUserId] = useState(0);
  const [comments, setComments] = useState(commentList);

  // 댓글 등록 핸들러
  const handleCommentRegister = (comment) => {
    registerComment(boardId, comment, (res) => {
      const newComment = res.data;
      setComments([...comments, newComment]);
    });
  };

  // 댓글 삭제 핸들러
  const handleCommentDelete = (commentId) => {
    if (window.confirm("댓글을 삭제하시겠습니까?")) {
      deleteComment(commentId, (res) => {
        const deletedComments = comments.filter((comment) => comment.id !== commentId);
        setComments(deletedComments);
      });
    }
  };

  // 댓글 수정 핸들러
  const handleCommentEdit = (commentId) => {
    console.log("edit");
  };

  useEffect(() => {
    getUserState((res) => {
      setUserId(res.data.id);
    });
  }, []);

  return (
    <Grid className="activity_comment" container>
      <IconButton>
        <ChatBubbleOutlineIcon />
      </IconButton>
      <Grid item>
        댓글 <span className="bold">{comments.length}개</span>
      </Grid>
      <CommentBox handleCommentRegister={handleCommentRegister} />
      {comments.map((comment, index) => (
        <Comment
          key={index}
          comment={comment}
          userId={userId}
          handleCommentEdit={handleCommentEdit}
          handleCommentDelete={handleCommentDelete}
        />
      ))}
    </Grid>
  );
}

export default ArticleDetailComment;