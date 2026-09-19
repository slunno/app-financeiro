package com.appfinanceiro.service;

import com.appfinanceiro.domain.Category;
import com.appfinanceiro.domain.User;
import com.appfinanceiro.domain.enums.TransactionType;
import com.appfinanceiro.dto.request.CategoryRequestDTO;
import com.appfinanceiro.dto.response.CategoryResponseDTO;
import com.appfinanceiro.exception.ResourceNotFoundException;
import com.appfinanceiro.repository.CategoryRepository;
import com.appfinanceiro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findAllForUser(UUID userId, TransactionType type) {
        if (type != null) {
            return categoryRepository.findAvailableForUserAndType(userId, type).stream()
                    .map(this::mapToResponse)
                    .toList();
        }
        return categoryRepository.findAllAvailableForUser(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public CategoryResponseDTO createCustomCategory(UUID userId, CategoryRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));

        Category category = Category.builder()
                .user(user)
                .name(request.name())
                .icon(request.icon())
                .color(request.color())
                .type(request.type())
                .isSystemDefault(false)
                .build();

        category = categoryRepository.save(category);
        return mapToResponse(category);
    }

    public CategoryResponseDTO mapToResponse(Category category) {
        return new CategoryResponseDTO(
                category.getId(),
                category.getName(),
                category.getIcon(),
                category.getColor(),
                category.getType(),
                category.getIsSystemDefault()
        );
    }
}
