
package com.example.notesapp.service;

import com.example.notesapp.entity.Note;
import com.example.notesapp.repository.NoteRepository;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NoteService {
    
    @Autowired
    private NoteRepository noteRepository;
    
    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().build();
    
    public List<Note> getAllNotes() {
        return noteRepository.findByOrderByUpdatedAtDesc();
    }
    
    public Optional<Note> getNoteById(Long id) {
        return noteRepository.findById(id);
    }
    
    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }
    
    public void deleteNote(Long id) {
        noteRepository.deleteById(id);
    }
    
    public List<Note> searchNotes(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllNotes();
        }
        return noteRepository.findBySearchTerm(searchTerm.trim());
    }
    
    public List<Note> findNotesByTags(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return getAllNotes();
        }
        List<String> tagList = Arrays.asList(tags.split(","))
                .stream()
                .map(String::trim)
                .collect(Collectors.toList());
        return noteRepository.findByTagsIn(tagList);
    }
    
    public String convertMarkdownToHtml(String markdown) {
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
    
    public Note createNote(String title, String content, String tagsString) {
        Note note = new Note(title, content);
        if (tagsString != null && !tagsString.trim().isEmpty()) {
            Set<String> tags = Arrays.stream(tagsString.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toSet());
            note.setTags(tags);
        }
        return saveNote(note);
    }
    
    public Note updateNote(Long id, String title, String content, String tagsString) {
        Optional<Note> optionalNote = getNoteById(id);
        if (optionalNote.isPresent()) {
            Note note = optionalNote.get();
            note.setTitle(title);
            note.setContent(content);
            
            Set<String> tags = Arrays.stream(tagsString.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toSet());
            note.setTags(tags);
            
            return saveNote(note);
        }
        return null;
    }
}
