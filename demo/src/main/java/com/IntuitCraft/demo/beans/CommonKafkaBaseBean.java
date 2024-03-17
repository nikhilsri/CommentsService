package com.IntuitCraft.demo.beans;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@RequiredArgsConstructor
public class CommonKafkaBaseBean implements Serializable {

    @NonNull
    private String eventName;
    private String eventData;
    private LocalDate eventPublishedTime;
    private String eventType;
}
