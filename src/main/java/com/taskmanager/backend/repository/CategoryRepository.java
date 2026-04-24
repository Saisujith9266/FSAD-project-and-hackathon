package com.taskmanager.backend.repository;

import com.taskmanager.backend.model.Category;
import com.taskmanager.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUser(User user);
}
