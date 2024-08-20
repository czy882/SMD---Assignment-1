public class Letter implements Comparable<Letter> {
    private final int floor;
    private final int room;
    private final int arrival;

    @Override public int compareTo(Letter i) {
        int floorDiff = this.floor - i.floor;  // Don't really need this as only deliver to one floor at a time
        return (floorDiff == 0) ? this.room - i.room : floorDiff;
    }

    Letter(int floor, int room, int arrival) {
        this.floor = floor;
        this.room = room;
        this.arrival = arrival;
    }

    public String toString() {
        return "Floor: " + floor + ", Room: " + room + ", Arrival: " + arrival + ", Weight: " + 0;
    }

    int myFloor() { return floor; }
    int myRoom() { return room; }
    int myArrival() { return arrival; }
}
