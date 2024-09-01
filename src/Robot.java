import java.util.*;

public class Robot {
    private static int count = 1;
    final private String id;
    private int floor;
    private int room;
    private int numRoom;
    private int load;
    final private MailRoom mailroom;
    final private List<Item> items = new ArrayList<>();
    private int robotCapacity;
    public String toString() {
        return "Id: " + id + " Floor: " + floor + ", Room: " + room + ", #items: " + numItems() + ", Load: " + robotLoad();
    }

    Robot(MailRoom mailroom) {
        this.id = "R" + count++;
        this.mailroom = mailroom;
        this.robotCapacity = mailroom.getCapacity();
    }

    int getFloor() { return floor; }
    int getRoom() { return room; }
    public void setFloor (int floor) {this.floor = floor;}
    public void setRoom (int room) {this.room = room;}
    public void setNumRoom (int numRoom) {this.numRoom = numRoom;}
    boolean isEmpty() { return items.isEmpty(); }

    public void place(int floor, int room) {
        Building building = Building.getBuilding();
        building.place(floor, room, id);
        this.floor = floor;
        this.room = room;
    }

    public MailRoom getMailroom () { return mailroom; }

    public void move(Building.Direction direction) {
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
        ListIterator<Item> iter = robot.items.listIterator();
        do {
            Item item = iter.next();
            this.add(item); //Hand it over
            this.robotCapacity -= item.myWeight();
            iter.remove();
            robot.setCapacity(robot.getCapacity() + item.myWeight());
        } while(iter.hasNext());
    }

    void tick() {
            Building building = Building.getBuilding();
            if (items.isEmpty()) {
                // Return to MailRoom
                if (room == building.NUMROOMS + 1) { // in right end column
                    move(Building.Direction.DOWN);  //move towards mailroom
                } else {
                    move(Building.Direction.RIGHT); // move towards right end column
                }
            } else {
                // Items to deliver
                if (floor == items.getFirst().myFloor()) {
                    // On the right floor
                    if (room == items.getFirst().myRoom()) { //then deliver all relevant items to that room
                        do {
                            Item firstItem = items.getFirst();
                            this.robotCapacity += firstItem.myWeight();
                            Simulation.deliver(items.removeFirst());
                        } while (!items.isEmpty() && room == items.getFirst().myRoom());
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
        return items.size();
    }

    public void add(Item item) {
        items.add(item);
    }

    void sort() {
        Collections.sort(items);
    }


    public int getCapacity() {
        return robotCapacity;
    }

    public int robotLoad () {
        load = 0;
        for (Item item : items) {
            load += item.myWeight();
        }
        return load;
    }

    public void setCapacity(int capacity) {
        this.robotCapacity = capacity;
    }

    public List<Item> getItems() { return items; }

    void reverseSort() {Collections.sort(items, Comparator.reverseOrder());}

    public int waitingRobot(Robot waitRoomRobot, List<Robot> activeColumnRobots) {
        Robot leftOne = null;
        Robot rightOne = null;

        for (Robot robot : activeColumnRobots) {

            // if robots are not carrying items or at a incorrect floor, skip cur robot
            if (robot.getItems().isEmpty() || robot.getFloor() != waitRoomRobot.getFloor()) {
                continue;
            }

            // check the robot should be settled on right or left
            if (robot.getRoom() == 0
                    && robot.getItems().getFirst().myFloor() == waitRoomRobot.getFloor()) {
                leftOne = robot;
            } else if (robot.getRoom() == Building.getBuilding().NUMROOMS + 1
                    && robot.getItems().getFirst().myFloor() == waitRoomRobot.getFloor()) {
                rightOne = robot;
            }
        }


        // check if there is a robot on left side but not right side
        if (leftOne != null && rightOne == null) {
            return 0;
        }
        // check if there is a robot on right side but not left side
        else if (leftOne == null && rightOne != null) {
            return 1;
        }
        // compare the arriving time if there is a robot on both side
        else if (leftOne != null && rightOne != null) {
            return ((ColumnRobot)waitRoomRobot).findEarlyRobot(leftOne, rightOne) == 0 ? 0 : 1;
        }

        return -1;
    }
}

