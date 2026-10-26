package p466.lostandfound.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import p466.lostandfound.FoundItem;
import p466.lostandfound.claim.ItemClaim;
import p466.lostandfound.claim.ItemClaimRepository;
import p466.lostandfound.data.FoundItemRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminClaimControllerTest {

    private ItemClaimRepository claimRepository;
    private FoundItemRepository foundItemRepository;
    private AdminClaimController controller;

    @BeforeEach
    void setUp() {
        claimRepository = mock(ItemClaimRepository.class);
        foundItemRepository = mock(FoundItemRepository.class);

        controller = new AdminClaimController(
                claimRepository,
                foundItemRepository
        );

        when(foundItemRepository.findByIdForUpdate(anyLong()))
                .thenReturn(Optional.of(new FoundItem()));
    }

    @Test
    void approveClaimShouldUpdateStatus() {
        ItemClaim claim = new ItemClaim(1L, 2L, "My wallet");

        when(claimRepository.findById(1L))
                .thenReturn(Optional.of(claim));

        when(claimRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(claim));

        when(claimRepository.findByFoundItemId(1L))
                .thenReturn(List.of(claim));

        String result = controller.approveClaim(1L);

        assertEquals("APPROVED", claim.getStatus());
        assertEquals("redirect:/admin/claims?approved", result);

        verify(foundItemRepository).findByIdForUpdate(1L);
        verify(claimRepository).save(claim);
    }

    @Test
    void declineClaimShouldUpdateStatus() {
        ItemClaim claim = new ItemClaim(1L, 2L, "My wallet");

        when(claimRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(claim));

        String result = controller.declineClaim(1L);

        assertEquals("DECLINED", claim.getStatus());
        assertEquals("redirect:/admin/claims?declined", result);
        verify(claimRepository).save(claim);
    }

    @Test
    void requestMoreInformationShouldUpdateStatus() {
        ItemClaim claim = new ItemClaim(1L, 2L, "My wallet");

        when(claimRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(claim));

        String result = controller.requestMoreInformation(
                1L, "What color is the wallet?");

        assertEquals("MORE_INFO_REQUESTED", claim.getStatus());
        assertEquals("What color is the wallet?", claim.getAdminRequest());
        assertEquals("redirect:/admin/claims?infoRequested", result);
        verify(claimRepository).save(claim);
    }

    @Test
    void adminCanRequestMoreInformationAgain() {
        ItemClaim claim = new ItemClaim(1L, 2L, "My wallet");
        claim.setStatus("MORE_INFO_RECEIVED");
        claim.setStudentResponse("It is black.");

        when(claimRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(claim));

        controller.requestMoreInformation(
                1L, "Does it have any cards inside?");

        assertEquals("MORE_INFO_REQUESTED", claim.getStatus());
        assertEquals("Does it have any cards inside?",
                claim.getAdminRequest());
        assertNull(claim.getStudentResponse());
        verify(claimRepository).save(claim);
    }

    @Test
    void adminCanApproveAfterStudentResponds() {
        ItemClaim claim = new ItemClaim(1L, 2L, "My wallet");
        claim.setStatus("MORE_INFO_RECEIVED");
        claim.setStudentResponse("It is black.");

        when(claimRepository.findById(1L))
                .thenReturn(Optional.of(claim));

        when(claimRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(claim));

        when(claimRepository.findByFoundItemId(1L))
                .thenReturn(List.of(claim));

        controller.approveClaim(1L);

        assertEquals("APPROVED", claim.getStatus());
        verify(claimRepository).save(claim);
    }

    @Test
    void alreadyReviewedClaimCannotBeApprovedAgain() {
        ItemClaim claim = new ItemClaim(1L, 2L, "My wallet");
        claim.setStatus("DECLINED");

        when(claimRepository.findById(1L))
                .thenReturn(Optional.of(claim));

        when(claimRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(claim));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.approveClaim(1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(claimRepository, never()).save(any());
    }

    @Test
    void cannotApproveTwoClaimsForSameItem() {
        ItemClaim firstClaim = new ItemClaim(1L, 2L, "My wallet");
        firstClaim.setStatus("APPROVED");

        ItemClaim secondClaim = new ItemClaim(1L, 3L, "My wallet too");

        when(claimRepository.findById(2L))
                .thenReturn(Optional.of(secondClaim));

        when(claimRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(secondClaim));

        when(claimRepository.findByFoundItemId(1L))
                .thenReturn(List.of(firstClaim, secondClaim));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.approveClaim(2L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("PENDING", secondClaim.getStatus());
        verify(claimRepository, never()).save(any());
    }
}