package com.teamsync.repository;

import com.teamsync.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// JPA repository for Task entity
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Custom query method to find tasks by status
    List<Task> findByStatus(String status);
}