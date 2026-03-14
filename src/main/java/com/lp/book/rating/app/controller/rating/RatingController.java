package com.lp.book.rating.app.controller.rating;

import com.lp.book.rating.app.controller.rating.dto.PatchRatingRequest;
import com.lp.book.rating.app.controller.rating.dto.RatingRequest;
import com.lp.book.rating.app.controller.rating.dto.RatingResponse;
import com.lp.book.rating.app.service.RatingService;
import com.lp.book.rating.app.util.ETagUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpHeaders.IF_MATCH;

@Validated
@RestController
@RequestMapping("/api/v1/book/{bookId}/rating")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @Valid
    @PostMapping
    public ResponseEntity<RatingResponse> create(@PathVariable @Valid @PositiveOrZero Long bookId,
                                                 @Valid @RequestBody RatingRequest ratingRequest) {
        return ResponseEntity.ok(ratingService.create(bookId, ratingRequest));
    }

    @Valid
    @GetMapping
    public ResponseEntity<RatingResponse> get(@PathVariable @PositiveOrZero Long bookId) {
        return ResponseEntity.ok(ratingService.getByBookId(bookId));
    }

    @Valid
    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable @PositiveOrZero Long bookId,
                                       @RequestHeader(IF_MATCH) String ifMatch) {
        var version = ETagUtils.extractETag(ifMatch);

        ratingService.delete(bookId, version);

        return ResponseEntity.noContent().build();
    }


    @Valid
    @PatchMapping
    public ResponseEntity<RatingResponse> patch(@PathVariable @PositiveOrZero Long bookId,
                                                @RequestHeader(IF_MATCH) String ifMatch,
                                                @Valid @RequestBody PatchRatingRequest patchRatingRequest) {

        var version = ETagUtils.extractETag(ifMatch);

        var rating = ratingService.patch(bookId, patchRatingRequest, version);

        return ResponseEntity.ok()
            .eTag(ETagUtils.buildETag(rating.version()))
            .body(rating);
    }

}
