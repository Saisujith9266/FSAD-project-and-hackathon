package com.taskmanager.backend.controller;

import com.taskmanager.backend.model.Category;
import com.taskmanager.backend.model.User;
import com.taskmanager.backend.repository.CategoryRepository;
import com.taskmanager.backend.repository.UserRepository;
import com.taskmanager.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    UserRepository userRepository;

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public List<Category> getUserCategories() {
        return categoryRepository.findByUser(getCurrentUser());
    }

    @PostMapping
    public Category createCategory(@RequestBody Category categoryRequest) {
        Category category = new Category(categoryRequest.getName(), getCurrentUser());
        return categoryRepository.save(category);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryRepository.findById(id).ifPresent(category -> {
            if (category.getUser().getId().equals(getCurrentUser().getId())) {
                categoryRepository.delete(category);
            }
        });
        return ResponseEntity.ok().build();
    }
}
