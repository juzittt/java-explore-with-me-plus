package ewm.comments.controller;

import ewm.comments.dto.CommentDto;
import ewm.comments.dto.UpdateCommentDto;
import ewm.comments.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(@PathVariable @Positive Long commentId) {
        log.info("DELETE /admin/comments/{} - Удаление комментария администратором", commentId);
        commentService.deleteCommentByAdmin(commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateCommentByAdmin(
            @PathVariable @Positive Long commentId,
            @Valid @RequestBody UpdateCommentDto updateCommentDto
    ) {
        log.info("PATCH /admin/comments/{} - Обновление комментария администратором", commentId);
        return commentService.updateCommentByAdmin(commentId, updateCommentDto);
    }
}
