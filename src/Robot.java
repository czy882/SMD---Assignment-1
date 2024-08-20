import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class Robot {
    private static int count = 1;
    final private String id;
    private int floor;
    private int room;
    final private MailRoom mailroom;
    final private List<Letter> letters = new ArrayList<>();

    public String toString() {
        return "Id: " + id + " Floor: " + floor + ", Room: " + room + ", #items: " + numItems() + ", Load: " + 0 ;
    }

    Robot(MailRoom mailroom) {
        this.id = "R" + count++;
        this.mailroom = mailroom;
    }

    int getFloor() { return floor; }
    int getRoom() { return room; }
    boolean isEmpty() { return letters.isEmpty(); }

    public void place(int floor, int room) {
        Building building = Building.getBuilding();
        building.place(floor, room, id);
        this.floor = floor;
        this.room = room;
    }

    private void move(Building.Direction direction) {
        Building building = Building.getBuilding();
        int dfloor, droom;
        switch (direction) {
            case UP    -> {dfloor = floor+1; droom = room;}
            case DOWN  -> {dfloor = floor-1; droom = room;}
            case LEFT  -> {dfloor = floor;   droom = room-1;}
            case RIGHT -> {dfloor = floor;   droom = room+1;}
            default -> throw new IllegalArgumentException("Unexpected value: " + direction);
        }
        if (!building.isOccupied(dfloor, droom)) { // If destination is occupied, do nothing
            building.move(floor, room, direction, id);
            floor = dfloor; room = droom;
            if (floor == 0) {
                System.out.printf("About to return: " + this + "\n");
                mailroom.robotReturn(this);
            }
        }
    }

    void transfer(Robot robot) {  // Transfers every item assuming receiving robot has capacity
        ListIterator<Letter> iter = robot.letters.listIterator();
        while(iter.hasNext()) {
            Letter letter = iter.next();
            this.add(letter); //Hand it over
            iter.remove();
        }
    }

    void tick() {
            Building building = Building.getBuilding();
            if (letters.isEmpty()) {
                // Return to MailRoom
                if (room == building.NUMROOMS + 1) { // in right end column
                    move(Building.Direction.DOWN);  //move towards mailroom
                } else {
                    move(Building.Direction.RIGHT); // move towards right end column
                }
            } else {
                // Items to deliver
                if (floor == letters.getFirst().myFloor()) {
                    // On the right floor
                    if (room == letters.getFirst().myRoom()) { //then deliver all relevant items to that room
                        do {
                            Simulation.deliver(letters.removeFirst());
                        } while (!letters.isEmpty() && room == letters.getFirst().myRoom());
                    } else {
                        move(Building.Direction.RIGHT); // move towards next delivery
                    }
                } else {
                    move(Building.Direction.UP); // move towards floor
                }
            }
    }

    public String getId() {
        return id;
    }

    public int numItems () {
        return letters.size();
    }

    public void add(Letter item) {
        letters.add(item);
    }

    void sort() {
        Collections.sort(letters);
    }

}
