package towersim.control;

import towersim.aircraft.Aircraft;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a first-in-first-out (FIFO) queue of aircraft waiting to take off.
 *
 * FIFO ensures that the order in which aircraft are allowed to take off is based on long they have
 * been waiting in the queue. An aircraft that has been waiting for longer than another aircraft
 * will always be allowed to take off before the other aircraft.
 */
public class TakeoffQueue extends AircraftQueue {
    /** Queue for aircraft that are going to take off */
    private LinkedList<Aircraft> takeoffQueue;

    /**
     * Constructs a new TakeoffQueue with an initially empty queue of aircraft.
     */
    public TakeoffQueue() {
        takeoffQueue = new LinkedList<>();
    }

    /**
     * Adds the given aircraft to the queue.
     *
     * Specified by:
     *     addAircraft in class AircraftQueue
     *
     * @param aircraft aircraft to add to queue
     */
    @Override
    public void addAircraft(Aircraft aircraft) {
        takeoffQueue.add(aircraft);
    }

    /**
     * Returns the aircraft at the front of the queue without removing it from the queue, or null
     * if the queue is empty.
     *
     * @return aircraft at front of queue
     */
    @Override
    public Aircraft peekAircraft() {
        if (takeoffQueue.size() < 1) {
            return null;
        } else {
            return takeoffQueue.peek();
        }
    }

    /**
     * Removes and returns the aircraft at the front of the queue. Returns null if the queue is
     * empty.
     *
     * Aircraft returned by removeAircraft() should be in the same order that they were added via
     * addAircraft().
     *
     * @return aircraft at front of queue
     */
    @Override
    public Aircraft removeAircraft() {
        if (takeoffQueue.size() < 1) {
            return null;
        } else {
            return takeoffQueue.remove();
        }
    }

    /**
     * Returns a list containing all aircraft in the queue, in order.
     * That is, the first element of the returned list should be the first aircraft that would be
     * returned by calling removeAircraft(), and so on.
     * Adding or removing elements from the returned list should not affect the original queue.
     *
     * @return list of all aircraft in queue, in queue order
     */
    @Override
    public List<Aircraft> getAircraftInOrder() {
        return new LinkedList<>(takeoffQueue);
    }

    /**
     * Returns true if the given aircraft is in the queue.
     *
     * Specified by:
     *     containsAircraft in class AircraftQueue
     *
     * @param aircraft aircraft to find in queue
     * @return true if aircraft is in queue; false otherwise
     */
    @Override
    public boolean containsAircraft(Aircraft aircraft) {
        return takeoffQueue.contains(aircraft);
    }
}
