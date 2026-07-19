package com.campingtripplanner.backend.web;

import com.campingtripplanner.backend.domain.ChecklistItem;
import com.campingtripplanner.backend.domain.User;
import com.campingtripplanner.backend.security.CurrentUserProvider;
import com.campingtripplanner.backend.service.ChecklistService;
import com.campingtripplanner.backend.web.dto.ChecklistItemResponse;
import com.campingtripplanner.backend.web.dto.ChecklistUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checklist")
public class ChecklistController {

    private final ChecklistService checklistService;
    private final CurrentUserProvider currentUserProvider;

    public ChecklistController(ChecklistService checklistService, CurrentUserProvider currentUserProvider) {
        this.checklistService = checklistService;
        this.currentUserProvider = currentUserProvider;
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ChecklistItemResponse> updateItem(@PathVariable Long id,
                                                              @RequestBody ChecklistUpdateRequest request,
                                                              Authentication authentication) {
        User user = currentUserProvider.require(authentication);
        ChecklistItem item = checklistService.updatePacked(user, id, request.isPacked());
        return ResponseEntity.ok(ChecklistItemResponse.from(item));
    }
}
