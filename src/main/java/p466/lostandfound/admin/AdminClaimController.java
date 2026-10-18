package p466.lostandfound.admin;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import p466.lostandfound.claim.ItemClaim;
import p466.lostandfound.claim.ItemClaimRepository;

@Controller
public class AdminClaimController {

    private final ItemClaimRepository claimRepository;

    public AdminClaimController(ItemClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @GetMapping("/admin/claims")
    public String showPendingClaims(Model model) {
        model.addAttribute("claims", claimRepository.findByStatus("PENDING"));
        return "adminClaims";
    }

    @PostMapping("/admin/claims/{id}/approve")
    public String approveClaim(@PathVariable Long id) {
        ItemClaim claim = claimRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!"PENDING".equals(claim.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "This claim has already been reviewed");
        }

        claim.setStatus("APPROVED");
        claimRepository.save(claim);

        return "redirect:/admin/claims?approved";
    }

    @PostMapping("/admin/claims/{id}/decline")
    public String declineClaim(@PathVariable Long id) {
        ItemClaim claim = claimRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!"PENDING".equals(claim.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "This claim has already been reviewed");
        }

        claim.setStatus("DECLINED");
        claimRepository.save(claim);

        return "redirect:/admin/claims?declined";
    }

    @PostMapping("/admin/claims/{id}/request-info")
    public String requestMoreInformation(@PathVariable Long id) {
        ItemClaim claim = claimRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!"PENDING".equals(claim.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "This claim has already been reviewed");
        }

        claim.setStatus("MORE_INFO_REQUESTED");
        claimRepository.save(claim);

        return "redirect:/admin/claims?infoRequested";
    }
}
