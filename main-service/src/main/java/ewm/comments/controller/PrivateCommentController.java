package ewm.comments.controller;

import ewm.comments.dto.CommentDto;
import ewm.comments.dto.NewCommentDto;
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
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(
            @PathVariable @Positive Long userId,
            @RequestParam @Positive Long eventId,
            @Valid @RequestBody NewCommentDto newCommentDto
            ) {
        log.info("POST /users/{}/comments?eventId={} - Добавление комментария", userId, eventId);
        return commentService.createComment(userId, eventId, newCommentDto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long commentId,
            @Valid @RequestBody UpdateCommentDto updateCommentDto
            ) {
        log.info("PATCH /users/{}/comments/{} - Обновление комментария", userId, commentId);
        return commentService.updateComment(userId, commentId, updateCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long commentId
    ) {
        log.info("DELETE /users/{}/comments/{} - Удаление комментария пользователем", userId, commentId);
        commentService.deleteCommentByUser(userId, commentId);
    }

    @GetMapping("/{commentId}")
    public CommentDto getCommentById(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long commentId
    ) {
        log.info("GET /users/{}/comments/{} - Получение комментария пользователем", userId, commentId);
        return commentService.getCommentById(userId, commentId);
    }
}
