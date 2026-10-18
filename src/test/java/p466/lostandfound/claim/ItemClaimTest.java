package p466.lostandfound.claim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemClaimTest {

    @Test
    void newClaimShouldHavePendingStatus() {
        ItemClaim claim = new ItemClaim(
                1L,
                2L,
                "This wallet belongs to me."
        );

        assertEquals(1L, claim.getFoundItemId());
        assertEquals(2L, claim.getStudentId());
        assertEquals("This wallet belongs to me.", claim.getExplanation());
        assertEquals("PENDING", claim.getStatus());
        assertNotNull(claim.getDateSubmitted());
    }
}
