
package p466.lostandfound.web;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import p466.lostandfound.FoundItem;
import p466.lostandfound.data.FoundItemRepository;
import p466.lostandfound.user.AppUser;
import p466.lostandfound.user.AppUserRepository;

import java.time.LocalDateTime;

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
    public String showItems(Model model) {
        model.addAttribute("items", foundItemRepository.findAll());
        return "foundItems";
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
