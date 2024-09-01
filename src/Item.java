

public class Item implements Comparable<Item> {
    private final int floor;
    private final int room;
    private final int arrival;
    private int weight = 0;

    @Override public int compareTo(Item i) {
        int floorDiff = this.floor - i.floor;  // Don't really need this as only deliver to one floor at a time
        return (floorDiff == 0) ? this.room - i.room : floorDiff;
    }

    Item(int floor, int room, int arrival) {
        this.floor = floor;
        this.room = room;
        this.arrival = arrival;
    }

    public String toString() {
        return "Floor: " + floor + ", Room: " + room + ", Arrival: " + arrival + ", Weight: " + weight;
    }


    int myFloor() { return floor; }
    int myRoom() { return room; }
    int myArrival() { return arrival; }
    public int myWeight() { return weight; }

    public void setWeight(int weight) {this.weight = weight;}
}
