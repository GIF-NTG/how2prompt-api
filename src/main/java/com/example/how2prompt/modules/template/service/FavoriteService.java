package com.example.how2prompt.modules.template.service;

import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.modules.template.entity.Favorite;
import com.example.how2prompt.modules.template.entity.FavoriteId;
import com.example.how2prompt.modules.template.entity.Template;
import com.example.how2prompt.modules.template.repository.FavoriteRepository;
import com.example.how2prompt.modules.template.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final TemplateRepository templateRepository;

    @Transactional
    public void addFavorite(UUID templateId, UUID userId) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> ResourceNotFoundException.of("Template", templateId));

        FavoriteId favoriteId = new FavoriteId(userId, templateId);
        if (!favoriteRepository.existsById(favoriteId)) {
            Favorite favorite = new Favorite(favoriteId);
            favorite.setTemplate(template);
            favoriteRepository.save(favorite);
            templateRepository.incrementFavoriteCount(templateId);
            log.info("User {} favorited template {}", userId, templateId);
        }
    }

    @Transactional
    public void removeFavorite(UUID templateId, UUID userId) {
        if (!templateRepository.existsById(templateId)) {
            throw ResourceNotFoundException.of("Template", templateId);
        }

        FavoriteId favoriteId = new FavoriteId(userId, templateId);
        if (favoriteRepository.existsById(favoriteId)) {
            favoriteRepository.deleteById(favoriteId);
            templateRepository.decrementFavoriteCount(templateId);
            log.info("User {} unfavorited template {}", userId, templateId);
        }
    }

    @Transactional(readOnly = true)
    public boolean isFavorited(UUID userId, UUID templateId) {
        if (userId == null) {
            return false;
        }
        return favoriteRepository.existsByIdUserIdAndIdTemplateId(userId, templateId);
    }
}
