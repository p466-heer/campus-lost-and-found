package p466.lostandfound.admin;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import p466.lostandfound.claim.ItemClaim;
import p466.lostandfound.claim.ItemClaimRepository;
import p466.lostandfound.data.FoundItemRepository;

@Controller
public class AdminClaimController {

    private final ItemClaimRepository claimRepository;
    private final FoundItemRepository foundItemRepository;

    public AdminClaimController(
            ItemClaimRepository claimRepository,
            FoundItemRepository foundItemRepository) {
        this.claimRepository = claimRepository;
        this.foundItemRepository = foundItemRepository;
    }

    @GetMapping("/admin/claims")
    public String showPendingClaims(Model model) {
        model.addAttribute("claims", claimRepository.findByStatus("PENDING"));
        return "adminClaims";
    }

    @Transactional
    @PostMapping("/admin/claims/{id}/approve")
    public String approveClaim(@PathVariable Long id) {

        ItemClaim claim = claimRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Lock the found item first so approvals for the same
        // item cannot happen simultaneously.
        foundItemRepository.findByIdForUpdate(claim.getFoundItemId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Lock the individual claim before reviewing its status.
        claim = claimRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!"PENDING".equals(claim.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This claim has already been reviewed");
        }

        boolean alreadyApproved = claimRepository
                .findByFoundItemId(claim.getFoundItemId())
                .stream()
                .anyMatch(otherClaim ->
                        "APPROVED".equals(otherClaim.getStatus()));

        if (alreadyApproved) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another claim for this item has already been approved");
        }

        claim.setStatus("APPROVED");
        claimRepository.save(claim);

        return "redirect:/admin/claims?approved";
    }

    @Transactional
    @PostMapping("/admin/claims/{id}/decline")
    public String declineClaim(@PathVariable Long id) {

        ItemClaim claim = claimRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!"PENDING".equals(claim.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This claim has already been reviewed");
        }

        claim.setStatus("DECLINED");
        claimRepository.save(claim);

        return "redirect:/admin/claims?declined";
    }

    @Transactional
    @PostMapping("/admin/claims/{id}/request-info")
    public String requestMoreInformation(@PathVariable Long id) {

        ItemClaim claim = claimRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!"PENDING".equals(claim.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This claim has already been reviewed");
        }

        claim.setStatus("MORE_INFO_REQUESTED");
        claimRepository.save(claim);

        return "redirect:/admin/claims?infoRequested";
    }
}
