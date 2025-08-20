
package com.example.notesapp.repository;

import com.example.notesapp.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    
    @Query("SELECT DISTINCT n FROM Note n LEFT JOIN n.tags t WHERE " +
           "LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Note> findBySearchTerm(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT DISTINCT n FROM Note n JOIN n.tags t WHERE t IN :tags")
    List<Note> findByTagsIn(@Param("tags") List<String> tags);
    
    List<Note> findByOrderByUpdatedAtDesc();
}
