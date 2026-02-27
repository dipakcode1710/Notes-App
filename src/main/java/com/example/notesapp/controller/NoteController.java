
package com.example.notesapp.controller;

import com.example.notesapp.entity.Note;
import com.example.notesapp.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class NoteController {
    
    @Autowired
    private NoteService noteService;
    
    @GetMapping("/")
    public String index(Model model, 
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) String tags) {
        List<Note> notes = noteService.filterNotes(search, tags);
        
        model.addAttribute("notes", notes);
        model.addAttribute("search", search);
        model.addAttribute("tags", tags);
        model.addAttribute("topTags", noteService.getTopTags(8));
        return "index";
    }
    
    @GetMapping("/new")
    public String newNote(Model model) {
        model.addAttribute("note", new Note());
        return "note-form";
    }
    
    @GetMapping("/edit/{id}")
    public String editNote(@PathVariable Long id, Model model) {
        Optional<Note> note = noteService.getNoteById(id);
        if (note.isPresent()) {
            model.addAttribute("note", note.get());
            model.addAttribute("tagsString", String.join(", ", note.get().getTags()));
            return "note-form";
        }
        return "redirect:/";
    }
    
    @GetMapping("/view/{id}")
    public String viewNote(@PathVariable Long id, Model model) {
        Optional<Note> note = noteService.getNoteById(id);
        if (note.isPresent()) {
            Note n = note.get();
            String htmlContent = noteService.convertMarkdownToHtml(n.getContent());
            model.addAttribute("note", n);
            model.addAttribute("htmlContent", htmlContent);
            return "note-view";
        }
        return "redirect:/";
    }
    
    @PostMapping("/save")
    public String saveNote(@RequestParam String title,
                          @RequestParam String content,
                          @RequestParam(required = false) String tags,
                          @RequestParam(required = false) Long id) {
        if (id != null) {
            noteService.updateNote(id, title, content, tags != null ? tags : "");
        } else {
            noteService.createNote(title, content, tags != null ? tags : "");
        }
        return "redirect:/";
    }
    
    @PostMapping("/delete/{id}")
    public String deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return "redirect:/";
    }
}
