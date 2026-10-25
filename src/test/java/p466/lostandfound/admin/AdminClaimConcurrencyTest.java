package p466.lostandfound.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import p466.lostandfound.FoundItem;
import p466.lostandfound.claim.ItemClaim;
import p466.lostandfound.claim.ItemClaimRepository;
import p466.lostandfound.data.FoundItemRepository;
import p466.lostandfound.user.AppUser;
import p466.lostandfound.user.AppUserRepository;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AdminClaimConcurrencyTest {

    @Autowired
    private AdminClaimController controller;

    @Autowired
    private FoundItemRepository itemRepository;

    @Autowired
    private ItemClaimRepository claimRepository;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void secondAdminWaitsForFirstAdminToFinish() throws Exception {

        FoundItem item = itemRepository.save(
                new FoundItem("Wallet", "Black wallet", "Library")
        );

        AppUser student = userRepository.save(
                new AppUser(
                        "Test Student One",
                        "student-one-concurrency@test.com",
                        "testpassword"
                )
        );

        ItemClaim claim = claimRepository.save(
                new ItemClaim(
                        item.getId(),
                        student.getId(),
                        "This wallet belongs to me"
                )
        );

        Long claimId = claim.getId();

        CountDownLatch firstAdminHasLock = new CountDownLatch(1);
        CountDownLatch releaseFirstAdmin = new CountDownLatch(1);
        CountDownLatch secondAdminStarted = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<?> firstAdmin = executor.submit(() -> {
                TransactionTemplate transaction =
                        new TransactionTemplate(transactionManager);

                transaction.executeWithoutResult(status -> {
                    claimRepository.findByIdForUpdate(claimId)
                            .orElseThrow();

                    firstAdminHasLock.countDown();

                    try {
                        if (!releaseFirstAdmin.await(
                                10, TimeUnit.SECONDS)) {
                            throw new IllegalStateException(
                                    "Timed out waiting to release lock"
                            );
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(e);
                    }

                    ItemClaim lockedClaim = claimRepository
                            .findByIdForUpdate(claimId)
                            .orElseThrow();

                    lockedClaim.setStatus("APPROVED");
                    claimRepository.save(lockedClaim);
                });
            });

            assertTrue(
                    firstAdminHasLock.await(5, TimeUnit.SECONDS),
                    "First admin did not acquire the lock"
            );

            Future<ResponseStatusException> secondAdmin =
                    executor.submit(() -> {
                        secondAdminStarted.countDown();

                        return assertThrows(
                                ResponseStatusException.class,
                                () -> controller.declineClaim(claimId)
                        );
                    });

            assertTrue(
                    secondAdminStarted.await(5, TimeUnit.SECONDS)
            );

            assertFalse(
                    secondAdmin.isDone(),
                    "Second admin should not finish while lock is held"
            );

            releaseFirstAdmin.countDown();

            firstAdmin.get(10, TimeUnit.SECONDS);

            ResponseStatusException exception =
                    secondAdmin.get(10, TimeUnit.SECONDS);

            assertEquals(
                    409,
                    exception.getStatusCode().value()
            );

            ItemClaim updatedClaim = claimRepository
                    .findById(claimId)
                    .orElseThrow();

            assertEquals("APPROVED", updatedClaim.getStatus());

        } finally {
            releaseFirstAdmin.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void onlyOneClaimCanBeApprovedForSameItem() throws Exception {

        FoundItem item = itemRepository.save(
                new FoundItem(
                        "Phone",
                        "Black phone",
                        "Student Center"
                )
        );

        AppUser firstStudent = userRepository.save(
                new AppUser(
                        "First Student",
                        "student-two-concurrency@test.com",
                        "testpassword"
                )
        );

        AppUser secondStudent = userRepository.save(
                new AppUser(
                        "Second Student",
                        "student-three-concurrency@test.com",
                        "testpassword"
                )
        );

        ItemClaim firstClaim = claimRepository.save(
                new ItemClaim(
                        item.getId(),
                        firstStudent.getId(),
                        "This is my phone"
                )
        );

        ItemClaim secondClaim = claimRepository.save(
                new ItemClaim(
                        item.getId(),
                        secondStudent.getId(),
                        "I believe this is my phone"
                )
        );

        CountDownLatch startTogether = new CountDownLatch(1);
        CountDownLatch bothReady = new CountDownLatch(2);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> firstAdmin = executor.submit(() -> {
                bothReady.countDown();
                startTogether.await();

                try {
                    controller.approveClaim(firstClaim.getId());
                    return true;
                } catch (ResponseStatusException e) {
                    assertEquals(
                            409,
                            e.getStatusCode().value()
                    );
                    return false;
                }
            });

            Future<Boolean> secondAdmin = executor.submit(() -> {
                bothReady.countDown();
                startTogether.await();

                try {
                    controller.approveClaim(secondClaim.getId());
                    return true;
                } catch (ResponseStatusException e) {
                    assertEquals(
                            409,
                            e.getStatusCode().value()
                    );
                    return false;
                }
            });

            assertTrue(
                    bothReady.await(5, TimeUnit.SECONDS),
                    "Both administrators should be ready"
            );

            startTogether.countDown();

            boolean firstApproved =
                    firstAdmin.get(10, TimeUnit.SECONDS);

            boolean secondApproved =
                    secondAdmin.get(10, TimeUnit.SECONDS);

            assertNotEquals(
                    firstApproved,
                    secondApproved,
                    "Exactly one administrator should approve a claim"
            );

            List<ItemClaim> claims = claimRepository
                    .findByFoundItemId(item.getId());

            long approvedCount = claims.stream()
                    .filter(c -> "APPROVED".equals(c.getStatus()))
                    .count();

            assertEquals(
                    1,
                    approvedCount,
                    "Only one claim should be APPROVED"
            );

        } finally {
            startTogether.countDown();
            executor.shutdownNow();
        }
    }
}
