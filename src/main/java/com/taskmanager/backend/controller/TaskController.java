package com.taskmanager.backend.controller;

import com.taskmanager.backend.model.Task;
import com.taskmanager.backend.model.User;
import com.taskmanager.backend.repository.CategoryRepository;
import com.taskmanager.backend.repository.TaskRepository;
import com.taskmanager.backend.repository.UserRepository;
import com.taskmanager.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    UserRepository userRepository;

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return taskRepository.findByUser(getCurrentUser());
    }

    @PostMapping
    public Task createTask(@RequestBody Task taskRequest) {
        Task task = new Task();
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setPriority(taskRequest.getPriority());
        task.setStatus(taskRequest.getStatus() != null ? taskRequest.getStatus() : "Pending");
        task.setDueDate(taskRequest.getDueDate());
        task.setUser(getCurrentUser());

        if (taskRequest.getCategory() != null && taskRequest.getCategory().getId() != null) {
            categoryRepository.findById(taskRequest.getCategory().getId()).ifPresent(task::setCategory);
        }

        return taskRepository.save(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task taskRequest) {
        return taskRepository.findById(id)
                .filter(task -> task.getUser().getId().equals(getCurrentUser().getId()))
                .map(task -> {
                    task.setTitle(taskRequest.getTitle());
                    task.setDescription(taskRequest.getDescription());
                    task.setPriority(taskRequest.getPriority());
                    task.setStatus(taskRequest.getStatus());
                    task.setDueDate(taskRequest.getDueDate());
                    
                    if (taskRequest.getCategory() != null && taskRequest.getCategory().getId() != null) {
                        categoryRepository.findById(taskRequest.getCategory().getId()).ifPresent(task::setCategory);
                    } else {
                        task.setCategory(null);
                    }
                    
                    return ResponseEntity.ok(taskRepository.save(task));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        taskRepository.findById(id).ifPresent(task -> {
            if (task.getUser().getId().equals(getCurrentUser().getId())) {
                taskRepository.delete(task);
            }
        });
        return ResponseEntity.ok().build();
    }
}
