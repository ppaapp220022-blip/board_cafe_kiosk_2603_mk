package org.example.board_cafe_kiosk_2603.domain.kiosk;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Menu {
    private int id;
    private int categoryId;
    private String name;
    private int price;
    private String description;
    private String imageUrl;
    private boolean isAvailable;
    private boolean isDeleted;
    private LocalDateTime createdAt;
}
