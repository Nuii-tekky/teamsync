package com.teamsync.repository;

import com.teamsync.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    Page<Task> findByAssigneeId(UUID assigneeId, Pageable pageable);
    long countByAssigneeId(UUID assigneeId);
}