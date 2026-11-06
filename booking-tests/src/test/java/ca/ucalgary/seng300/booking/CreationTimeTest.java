package ca.ucalgary.seng300.booking;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ca.ucalgary.seng300.booking.TestFixtures.at;
import static org.junit.jupiter.api.Assertions.*;

class CreationTimeTest {
    private TestFixtures fixture;
    private Booking unrelated;

    @BeforeEach
    void setUp() {
        fixture = new TestFixtures();
        unrelated = fixture.seed("b2", "R2", "carol", 180, 240, BookingState.ACTIVE);
    }

    @Test
    void startBeforeNowIsInvalid() {
        assertInvalidTime("alice", -1, 60);
    }

    @Test
    void startAtNowIsInvalid() {
        assertInvalidTime("alice", 0, 60);
    }

    @Test
    void startAfterNowCreatesBooking() {
        assertCreated(1, 60);
    }

    @Test
    void endBeforeStartIsInvalid() {
        assertInvalidTime("alice", 60, 59);
    }

    @Test
    void endAtStartIsInvalid() {
        assertInvalidTime("alice", 60, 60);
    }

    @Test
    void endAfterStartCreatesBooking() {
        assertCreated(60, 61);
    }

    @Test
    void invalidTimeTakesPrecedenceOverConflict() {
        fixture.seed("b1", "R1", "alice", 60, 120, BookingState.ACTIVE);

        assertInvalidTime("bob", 0, 90);
    }

    private void assertInvalidTime(String owner, long start, long end) {
        List<Booking> before = fixture.repository.all();

        CreateResult result = fixture.service.create("R1", owner, at(start), at(end));

        assertEquals(CreateStatus.INVALID_TIME, result.status());
        assertNull(result.bookingId());
        List<Booking> after = fixture.repository.all();
        assertEquals(before.size(), after.size());
        assertEquals(new HashSet<>(before), new HashSet<>(after));
        assertTrue(fixture.notifier.requests().isEmpty());
    }

    private void assertCreated(long start, long end) {
        CreateResult result = fixture.service.create("R1", "alice", at(start), at(end));

        assertEquals(CreateStatus.CREATED, result.status());
        assertNotNull(result.bookingId());
        assertNotEquals(unrelated.id(), result.bookingId());
        Booking expected = new Booking(result.bookingId(), "R1", "alice",
            at(start), at(end), BookingState.ACTIVE);
        assertEquals(expected, fixture.repository.find(result.bookingId()));
        assertEquals(2, fixture.repository.all().size());
        assertEquals(unrelated, fixture.repository.find(unrelated.id()));
        assertTrue(fixture.notifier.requests().isEmpty());
    }
}
