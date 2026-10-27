package p466.lostandfound.claim;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

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
    public String showClaimForm(@PathVariable Long id,
                                Authentication authentication,
                                Model model) {

        FoundItem item = itemRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        AppUser student = getStudent(authentication);
        checkClaimAllowed(item, student);

        model.addAttribute("item", item);
        return "claimItem";
    }

    @PostMapping("/items/{id}/claim")
    public String submitClaim(@PathVariable Long id,
                              @RequestParam String explanation,
                              Authentication authentication,
                              Model model) {

        FoundItem item = itemRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        AppUser student = getStudent(authentication);
        checkClaimAllowed(item, student);

        if (explanation == null || explanation.isBlank()
                || explanation.length() > 1000) {
            model.addAttribute("item", item);
            model.addAttribute("error",
                    "Please provide an explanation (up to 1000 characters).");
            return "claimItem";
        }

        ItemClaim claim = new ItemClaim(
                item.getId(),
                student.getId(),
                explanation.trim()
        );

        claimRepository.save(claim);

        return "redirect:/items?claimed";
    }

    @GetMapping("/my-claims")
    public String showMyClaims(Authentication authentication, Model model) {
        AppUser student = getStudent(authentication);

        model.addAttribute("claims",
                claimRepository.findByStudentId(student.getId()));

        return "myClaims";
    }

    @PostMapping("/my-claims/{id}/respond")
    public String respondToRequest(@PathVariable Long id,
                                   @RequestParam String studentResponse,
                                   Authentication authentication) {

        AppUser student = getStudent(authentication);

        ItemClaim claim = claimRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!claim.getStudentId().equals(student.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (!"MORE_INFO_REQUESTED".equals(claim.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This claim is not waiting for more information");
        }

        if (studentResponse == null || studentResponse.isBlank()
                || studentResponse.length() > 1000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please provide a response (up to 1000 characters)");
        }

        claim.setStudentResponse(studentResponse.trim());
        claim.setStatus("MORE_INFO_RECEIVED");
        claimRepository.save(claim);

        return "redirect:/my-claims?responded";
    }

    private void checkClaimAllowed(FoundItem item, AppUser student) {
        if (item.isResolved()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This item has already been resolved");
        }

        if (student.getId().equals(item.getReportedByUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot claim an item you reported");
        }

        boolean alreadyApproved = claimRepository
                .findByFoundItemId(item.getId())
                .stream()
                .anyMatch(claim -> "APPROVED".equals(claim.getStatus()));

        if (alreadyApproved) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This item has already been claimed");
        }
    }

    private AppUser getStudent(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("User not found"));
    }
}
