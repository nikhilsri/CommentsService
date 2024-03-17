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
public class PostRequest implements Serializable {

    @Schema(example = "title of the post",description = "this is the title of the post")
    private String title;
    @Schema(example = "content of the post",description = "this is the content of the post")
    private String content;
    @Schema(example = "userId of who is posting",description = "this is the userId of the owner of post")
    private String userId;
    // Constructors, getters, and setters
}

