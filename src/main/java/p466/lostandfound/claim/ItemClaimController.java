package p466.lostandfound.claim;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import p466.lostandfound.FoundItem;
import p466.lostandfound.data.FoundItemRepository;
import p466.lostandfound.user.AppUser;
import p466.lostandfound.user.AppUserRepository;

@Controller
public class ItemClaimController {

    private final ItemClaimRepository claimRepository;
    private final FoundItemRepository itemRepository;
    private final AppUserRepository userRepository;

    public ItemClaimController(ItemClaimRepository claimRepository,
                               FoundItemRepository itemRepository,
                               AppUserRepository userRepository) {
        this.claimRepository = claimRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/items/{id}/claim")
    public String showClaimForm(@PathVariable Long id, Model model) {
        FoundItem item = itemRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND));

        model.addAttribute("item", item);
        return "claimItem";
    }

    @PostMapping("/items/{id}/claim")
    public String submitClaim(@PathVariable Long id,
                              @RequestParam String explanation,
                              Authentication authentication,
                              Model model) {

        FoundItem item = itemRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND));

        if (explanation == null || explanation.isBlank()
                || explanation.length() > 1000) {
            model.addAttribute("item", item);
            model.addAttribute("error",
                    "Please provide an explanation (up to 1000 characters).");
            return "claimItem";
        }

        AppUser student = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        ItemClaim claim = new ItemClaim(
                item.getId(),
                student.getId(),
                explanation.trim()
        );

        claimRepository.save(claim);

        return "redirect:/items?claimed";
    }
}
