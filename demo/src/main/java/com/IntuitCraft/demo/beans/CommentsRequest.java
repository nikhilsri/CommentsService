package com.IntuitCraft.demo.beans;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CommentsRequest {

    @Schema(example = "content of the comment", description = "this is the content of the comment")
    private String content;
    @Schema(example = "userId of who is commenting", description = "this is the userId of the owner of comment")
    private String userId;

}
