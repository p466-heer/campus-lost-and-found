package p466.lostandfound.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import p466.lostandfound.claim.ItemClaim;
import p466.lostandfound.claim.ItemClaimRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminClaimControllerTest {

    private ItemClaimRepository claimRepository;
    private AdminClaimController controller;

    @BeforeEach
    void setUp() {
        claimRepository = mock(ItemClaimRepository.class);
        controller = new AdminClaimController(claimRepository);
    }

    @Test
    void approveClaimShouldUpdateStatus() {
        ItemClaim claim = new ItemClaim(1L, 2L, "My wallet");

        when(claimRepository.findById(1L))
                .thenReturn(Optional.of(claim));

        String result = controller.approveClaim(1L);

        assertEquals("APPROVED", claim.getStatus());
        assertEquals("redirect:/admin/claims?approved", result);
        verify(claimRepository).save(claim);
    }

    @Test
    void declineClaimShouldUpdateStatus() {
        ItemClaim claim = new ItemClaim(1L, 2L, "My wallet");

        when(claimRepository.findById(1L))
                .thenReturn(Optional.of(claim));

        String result = controller.declineClaim(1L);

        assertEquals("DECLINED", claim.getStatus());
        assertEquals("redirect:/admin/claims?declined", result);
        verify(claimRepository).save(claim);
    }

    @Test
    void requestMoreInformationShouldUpdateStatus() {
        ItemClaim claim = new ItemClaim(1L, 2L, "My wallet");

        when(claimRepository.findById(1L))
                .thenReturn(Optional.of(claim));

        String result = controller.requestMoreInformation(1L);

        assertEquals("MORE_INFO_REQUESTED", claim.getStatus());
        assertEquals("redirect:/admin/claims?infoRequested", result);
        verify(claimRepository).save(claim);
    }

    @Test
    void alreadyReviewedClaimCannotBeApprovedAgain() {
        ItemClaim claim = new ItemClaim(1L, 2L, "My wallet");
        claim.setStatus("DECLINED");

        when(claimRepository.findById(1L))
                .thenReturn(Optional.of(claim));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.approveClaim(1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(claimRepository, never()).save(any());
    }
}
