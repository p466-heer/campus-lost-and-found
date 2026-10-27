package p466.lostandfound.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import p466.lostandfound.FoundItem;
import p466.lostandfound.data.FoundItemRepository;
import p466.lostandfound.user.AppUser;
import p466.lostandfound.user.AppUserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class FoundItemController {

    private final FoundItemRepository foundItemRepository;
    private final AppUserRepository userRepository;

    public FoundItemController(FoundItemRepository foundItemRepository,
                               AppUserRepository userRepository) {
        this.foundItemRepository = foundItemRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/items";
    }

    @GetMapping("/items")
    public String showItems(@RequestParam(required = false) String search,
                            Model model,
                            Authentication authentication) {

        List<FoundItem> items = new ArrayList<>();
        foundItemRepository.findAll().forEach(items::add);

        if (search != null && !search.isBlank()) {
            String keyword = search.trim().toLowerCase();

            items.removeIf(item ->
                    !item.getName().toLowerCase().contains(keyword)
                            && !item.getDescription().toLowerCase().contains(keyword)
                            && !item.getFoundLocation().toLowerCase().contains(keyword));
        }

        Long currentUserId = null;

        if (authentication != null && authentication.isAuthenticated()) {
            AppUser user = userRepository.findByEmail(authentication.getName())
                    .orElse(null);

            if (user != null) {
                currentUserId = user.getId();
            }
        }

        model.addAttribute("items", items);
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("currentUserId", currentUserId);

        return "foundItems";
    }

    // CLFB-18: Show recently resolved items
    @GetMapping("/items/resolved")
    public String showResolvedItems(Model model) {

        List<FoundItem> items = new ArrayList<>();
        foundItemRepository.findAll().forEach(items::add);

        items.removeIf(item -> !item.isResolved());

        items.sort((a, b) -> {
            if (a.getDateResolved() == null && b.getDateResolved() == null) {
                return 0;
            }
            if (a.getDateResolved() == null) {
                return 1;
            }
            if (b.getDateResolved() == null) {
                return -1;
            }
            return b.getDateResolved().compareTo(a.getDateResolved());
        });

        model.addAttribute("items", items);

        return "resolvedItems";
    }

    @GetMapping("/items/{id}")
    public String showItemDetails(@PathVariable Long id,
                                  Model model,
                                  Authentication authentication) {

        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean canClaim = !item.isResolved();

        if (authentication != null && authentication.isAuthenticated()) {
            AppUser user = userRepository.findByEmail(authentication.getName())
                    .orElse(null);

            if (user != null &&
                    user.getId().equals(item.getReportedByUserId())) {
                canClaim = false;
            }
        }

        model.addAttribute("item", item);
        model.addAttribute("canClaim", canClaim);

        return "itemDetails";
    }

    @GetMapping("/items/report")
    public String showReportForm(Model model) {
        model.addAttribute("foundItem", new FoundItem());
        return "reportItem";
    }

    @PostMapping("/items/report")
    public String reportItem(@Valid @ModelAttribute FoundItem foundItem,
                             Errors errors,
                             Authentication authentication) {

        if (errors.hasErrors()) {
            return "reportItem";
        }

        AppUser user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("User not found"));

        foundItem.setId(null);
        foundItem.setDateReported(LocalDateTime.now());
        foundItem.setReportedByUserId(user.getId());
        foundItem.setResolved(false);
        foundItem.setDateResolved(null);

        foundItemRepository.save(foundItem);

        return "redirect:/items";
    }

    // CLFB-17: Admin marks a returned item as resolved
    @PostMapping("/admin/items/{id}/resolve")
    public String resolveItem(@PathVariable Long id,
                              Authentication authentication) {

        if (authentication == null ||
                authentication.getAuthorities().stream()
                        .noneMatch(authority ->
                                "ROLE_ADMIN".equals(authority.getAuthority()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!item.isResolved()) {
            item.setResolved(true);
            item.setDateResolved(LocalDateTime.now());
            foundItemRepository.save(item);
        }

        return "redirect:/items/" + id;
    }
}
