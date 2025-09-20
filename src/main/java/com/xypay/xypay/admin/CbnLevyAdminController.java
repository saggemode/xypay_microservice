package com.xypay.xypay.admin;

import com.xypay.xypay.domain.CBNLevy;
import com.xypay.xypay.service.CBNLevyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin/cbn-levies")
public class CbnLevyAdminController {

    @Autowired
    private CBNLevyService cbnLevyService;

    @GetMapping
    public String cbnLevies(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(defaultValue = "name") String sortBy,
                           @RequestParam(defaultValue = "asc") String sortDir,
                           @RequestParam(required = false) String search) {
        
        // Create pageable object
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get levies data
        Page<CBNLevy> leviesPage;
        if (search != null && !search.trim().isEmpty()) {
            leviesPage = cbnLevyService.searchLevies(search, pageable);
        } else {
            leviesPage = cbnLevyService.getAllLevies(pageable);
        }
        
        // Get statistics
        CBNLevyService.LevyStatistics stats = cbnLevyService.getLevyStatistics();
        
        // Add attributes to model
        model.addAttribute("levies", leviesPage.getContent());
        model.addAttribute("currentPage", leviesPage.getNumber());
        model.addAttribute("totalPages", leviesPage.getTotalPages());
        model.addAttribute("totalElements", leviesPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        model.addAttribute("stats", stats);
        
        return "admin/cbn-levies";
    }

    @GetMapping("/api/levies")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLeviesApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<CBNLevy> leviesPage;
            if (search != null && !search.trim().isEmpty()) {
                leviesPage = cbnLevyService.searchLevies(search, pageable);
            } else {
                leviesPage = cbnLevyService.getAllLevies(pageable);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", leviesPage.getContent());
            response.put("totalElements", leviesPage.getTotalElements());
            response.put("totalPages", leviesPage.getTotalPages());
            response.put("currentPage", leviesPage.getNumber());
            response.put("size", leviesPage.getSize());
            response.put("first", leviesPage.isFirst());
            response.put("last", leviesPage.isLast());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch levies: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<CBNLevyService.LevyStatistics> getStats() {
        try {
            CBNLevyService.LevyStatistics stats = cbnLevyService.getLevyStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/api/levies")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createLevy(@RequestBody CBNLevy levy) {
        try {
            CBNLevy createdLevy = cbnLevyService.createLevy(levy);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "CBN levy created successfully");
            response.put("levy", createdLevy);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create levy: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/api/levies/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateLevy(@PathVariable Long id, @RequestBody CBNLevy levy) {
        try {
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID levyId = new UUID(0L, id); // Create UUID from Long
            CBNLevy updatedLevy = cbnLevyService.updateLevy(levyId, levy);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "CBN levy updated successfully");
            response.put("levy", updatedLevy);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update levy: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/api/levies/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteLevy(@PathVariable Long id) {
        try {
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID levyId = new UUID(0L, id); // Create UUID from Long
            boolean deleted = cbnLevyService.deleteLevy(levyId);
            Map<String, Object> response = new HashMap<>();
            if (deleted) {
                response.put("success", true);
                response.put("message", "CBN levy deleted successfully");
            } else {
                response.put("success", false);
                response.put("error", "Levy not found");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to delete levy: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/api/levies/{id}/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLevyStatus(@PathVariable Long id) {
        try {
            // Convert Long to UUID - this is a workaround for the ID type mismatch
            UUID levyId = new UUID(0L, id); // Create UUID from Long
            CBNLevy updatedLevy = cbnLevyService.toggleLevyStatus(levyId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Levy status updated successfully");
            response.put("levy", updatedLevy);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to toggle levy status: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/api/levies/initialize")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> initializeDefaultLevies() {
        try {
            cbnLevyService.createDefaultLevies();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Default CBN levies initialized successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to initialize default levies: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Show create form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("levy", new CBNLevy());
        return "admin/cbn-levies-create";
    }

    // Form-based create method (no authentication required for form submission)
    @PostMapping("/create")
    public String createLevyForm(@ModelAttribute CBNLevy levy) {
        try {
            cbnLevyService.createLevy(levy);
            return "redirect:/admin/cbn-levies?success=created";
        } catch (Exception e) {
            return "redirect:/admin/cbn-levies?error=" + e.getMessage();
        }
    }

    // Form-based initialize method
    @PostMapping("/initialize")
    public String initializeDefaultLeviesForm() {
        try {
            cbnLevyService.createDefaultLevies();
            return "redirect:/admin/cbn-levies?success=initialized";
        } catch (Exception e) {
            return "redirect:/admin/cbn-levies?error=" + e.getMessage();
        }
    }

    // Test endpoint to check if controller is working
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "CBN Levies Controller is working!";
    }
}


