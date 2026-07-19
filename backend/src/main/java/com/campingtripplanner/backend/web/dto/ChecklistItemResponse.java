package com.campingtripplanner.backend.web.dto;

import com.campingtripplanner.backend.domain.ChecklistItem;

public record ChecklistItemResponse(Long id, String itemName, String category, boolean isPacked) {

    public static ChecklistItemResponse from(ChecklistItem item) {
        return new ChecklistItemResponse(item.getId(), item.getItemName(), item.getCategory(), item.isPacked());
    }
}
