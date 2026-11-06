package ca.ucalgary.seng300.booking;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ca.ucalgary.seng300.booking.TestFixtures.NOW;
import static ca.ucalgary.seng300.booking.TestFixtures.at;
import static org.junit.jupiter.api.Assertions.*;

class FailureRepositoryTest {
    private FailingRepository repository;
    private TestFixtures.RecordingNotifier notifier;
    private BookingService service;
    private Booking unrelated;

    @BeforeEach
    void setUp() {
        repository = new FailingRepository();
        notifier = new TestFixtures.RecordingNotifier();
        service = new BookingService(repository, notifier,
            Clock.fixed(NOW, ZoneOffset.UTC), () -> "created-1");
        repository.save(new Booking("b1", "R1", "alice", at(120), at(180), BookingState.ACTIVE));
        unrelated = new Booking("b2", "R2", "carol", at(180), at(240), BookingState.ACTIVE);
        repository.save(unrelated);
    }

    @Test
    void failedSaveLeavesCancellationUnchanged() {
        List<Booking> before = repository.all();
        RepositoryException failure = new RepositoryException("Simulated save failure");
        repository.failOnSave(failure);

        RepositoryException thrown = assertThrows(RepositoryException.class,
            () -> service.cancel("b1", "alice"));

        assertSame(failure, thrown);
        List<Booking> after = repository.all();
        assertEquals(before.size(), after.size());
        assertEquals(new HashSet<>(before), new HashSet<>(after));
        assertTrue(notifier.requests().isEmpty());
    }

    @Test
    void successfulSaveCancelsAndNotifies() {
        CancelStatus result = service.cancel("b1", "alice");

        Booking cancelled = new Booking("b1", "R1", "alice",
            at(120), at(180), BookingState.CANCELLED);
        assertEquals(CancelStatus.CANCELLED, result);
        assertEquals(cancelled, repository.find("b1"));
        assertEquals(unrelated, repository.find("b2"));
        assertEquals(2, repository.all().size());
        assertEquals(List.of(cancelled), notifier.requests());
    }

    private static class FailingRepository implements BookingRepository {
        private final InMemoryBookingRepository stored = new InMemoryBookingRepository();
        private RepositoryException saveFailure;

        void failOnSave(RepositoryException failure) {
            saveFailure = failure;
        }

        @Override
        public Booking find(String id) {
            return stored.find(id);
        }

        @Override
        public List<Booking> all() {
            return stored.all();
        }

        @Override
        public void save(Booking booking) {
            if (saveFailure != null) {
                throw saveFailure;
            }
            stored.save(booking);
        }
    }
}
