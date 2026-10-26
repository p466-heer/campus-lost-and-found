package p466.lostandfound.admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        List<ItemClaim> claims = new ArrayList<>();
        claims.addAll(claimRepository.findByStatus("PENDING"));
        claims.addAll(claimRepository.findByStatus("MORE_INFO_REQUESTED"));
        claims.addAll(claimRepository.findByStatus("MORE_INFO_RECEIVED"));

        model.addAttribute("claims", claims);
        return "adminClaims";
    }

    @Transactional
    @PostMapping("/admin/claims/{id}/approve")
    public String approveClaim(@PathVariable Long id) {

        ItemClaim claim = claimRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        foundItemRepository.findByIdForUpdate(claim.getFoundItemId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        claim = claimRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!canReview(claim)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This claim cannot be approved");
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

        if (!canReview(claim)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This claim cannot be declined");
        }

        claim.setStatus("DECLINED");
        claimRepository.save(claim);

        return "redirect:/admin/claims?declined";
    }

    @Transactional
    @PostMapping("/admin/claims/{id}/request-info")
    public String requestMoreInformation(
            @PathVariable Long id,
            @RequestParam String adminRequest) {

        ItemClaim claim = claimRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!canReview(claim)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This claim cannot request more information");
        }

        if (adminRequest.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please enter the information needed");
        }

        claim.setAdminRequest(adminRequest.trim());
        claim.setStudentResponse(null);
        claim.setStatus("MORE_INFO_REQUESTED");
        claimRepository.save(claim);

        return "redirect:/admin/claims?infoRequested";
    }

    private boolean canReview(ItemClaim claim) {
        return "PENDING".equals(claim.getStatus())
                || "MORE_INFO_RECEIVED".equals(claim.getStatus());
    }
}