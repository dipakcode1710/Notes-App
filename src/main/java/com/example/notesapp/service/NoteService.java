
package com.example.notesapp.service;

import com.example.notesapp.entity.Note;
import com.example.notesapp.repository.NoteRepository;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
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

    public List<Note> filterNotes(String searchTerm, String tags) {
        String normalizedSearch = searchTerm == null ? "" : searchTerm.trim().toLowerCase();
        Set<String> requestedTags = parseTags(tags).stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return getAllNotes().stream()
                .filter(note -> matchesSearch(note, normalizedSearch))
                .filter(note -> matchesTags(note, requestedTags))
                .sorted(Comparator.comparing(Note::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }
    
    public List<Note> findNotesByTags(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return getAllNotes();
        }
        List<String> tagList = parseTags(tags);
        return noteRepository.findByTagsIn(tagList);
    }

    public List<String> getTopTags(int limit) {
        return getAllNotes().stream()
                .flatMap(note -> note.getTags().stream())
                .filter(tag -> !tag.isBlank())
                .collect(Collectors.groupingBy(String::toLowerCase, Collectors.counting()))
                .entrySet().stream()
                .sorted((left, right) -> Long.compare(right.getValue(), left.getValue()))
                .limit(limit)
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
    }
    
    public String convertMarkdownToHtml(String markdown) {
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
    
    public Note createNote(String title, String content, String tagsString) {
        Note note = new Note(title, content);
        note.setTags(parseTags(tagsString).stream().collect(Collectors.toSet()));
        return saveNote(note);
    }
    
    public Note updateNote(Long id, String title, String content, String tagsString) {
        Optional<Note> optionalNote = getNoteById(id);
        if (optionalNote.isPresent()) {
            Note note = optionalNote.get();
            note.setTitle(title);
            note.setContent(content);
            
            note.setTags(parseTags(tagsString).stream().collect(Collectors.toSet()));
            
            return saveNote(note);
        }
        return null;
    }

    private List<String> parseTags(String tagsString) {
        if (tagsString == null || tagsString.trim().isEmpty()) {
            return List.of();
        }

        return Arrays.stream(tagsString.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean matchesSearch(Note note, String normalizedSearch) {
        if (normalizedSearch.isBlank()) {
            return true;
        }

        String title = note.getTitle() == null ? "" : note.getTitle().toLowerCase();
        String content = note.getContent() == null ? "" : note.getContent().toLowerCase();

        return title.contains(normalizedSearch)
                || content.contains(normalizedSearch)
                || note.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(normalizedSearch));
    }

    private boolean matchesTags(Note note, Set<String> requestedTags) {
        if (requestedTags.isEmpty()) {
            return true;
        }

        Set<String> lowerCaseTags = note.getTags().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        return lowerCaseTags.containsAll(requestedTags);
    }
}
