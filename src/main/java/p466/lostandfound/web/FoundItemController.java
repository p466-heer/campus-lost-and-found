package p466.lostandfound.web;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String showItems(
            @RequestParam(required = false) String search,
            Model model) {

        List<FoundItem> items = new ArrayList<>();
        foundItemRepository.findAll().forEach(items::add);

        if (search != null && !search.isBlank()) {
            String keyword = search.trim().toLowerCase();

            items.removeIf(item ->
                    !item.getName().toLowerCase().contains(keyword)
                            && !item.getDescription().toLowerCase().contains(keyword)
                            && !item.getFoundLocation().toLowerCase().contains(keyword));
        }

        model.addAttribute("items", items);
        model.addAttribute("search", search == null ? "" : search);

        return "foundItems";
    }

    @GetMapping("/items/{id}")
    public String showItemDetails(@PathVariable Long id, Model model) {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() ->
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND));

        model.addAttribute("item", item);
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
                .orElseThrow(() -> new IllegalStateException("User not found"));

        foundItem.setDateReported(LocalDateTime.now());
        foundItem.setReportedByUserId(user.getId());

        foundItemRepository.save(foundItem);

        return "redirect:/items";
    }
}
