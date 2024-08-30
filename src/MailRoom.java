import java.util.*;

import static java.lang.String.format;

public class MailRoom {
    public enum Mode {CYCLING, FLOORING}
    Mode mode;
    private int robotCapacity;
    List<Item>[] waitingForDelivery;
    private final int numRobots;

    Queue<Robot> idleRobots;
    List<Robot> activeFloorRobots;
    List<Robot> activeRobots;
    List<Robot> deactivatingRobots; // Don't treat a robot as both active and idle by swapping directly

    public boolean someItems() {
        for (int i = 0; i < Building.getBuilding().NUMFLOORS; i++) {
            if (!waitingForDelivery[i].isEmpty()) {
                    return true;
            }
        }
        return false;
    }

    private int floorWithEarliestItem() {
        int floor = -1;
        int earliest = Simulation.now() + 1;
        for (int i = 0; i < Building.getBuilding().NUMFLOORS; i++) {
            if (!waitingForDelivery[i].isEmpty()) {
                int arrival = waitingForDelivery[i].getFirst().myArrival();
                if (earliest > arrival) {
                    floor = i;
                    earliest = arrival;
                }
            }
        }
        return floor;
    }

    MailRoom(int numFloors, int numRobots, Mode mode, int robotCapacity) {
        this.mode = mode;
        this.robotCapacity = robotCapacity;
        waitingForDelivery = new List[numFloors];
        for (int i = 0; i < numFloors; i++) {
            waitingForDelivery[i] = new LinkedList<>();
        }
        this.numRobots = numRobots;
        activeRobots = new ArrayList<>();
        deactivatingRobots = new ArrayList<>();
        activeFloorRobots = new ArrayList<>();
        idleRobots = new LinkedList<>();

        if (this.mode == mode.FLOORING) {

        } else if (this.mode == mode.CYCLING) {
            for (int i = 0; i < numRobots; i++)
                idleRobots.add(new Robot(MailRoom.this, this.robotCapacity));  // In mailroom, floor/room is not significant
        }
    }


    void arrive(List<Item> items) {
        for (Item item : items) {
            waitingForDelivery[item.myFloor()-1].add(item);
            System.out.printf("Item: Time = %d Floor = %d Room = %d Weight = %d\n",
                    item.myArrival(), item.myArrival(),item.myFloor(), item.myRoom(), 0);
        }
    }

    public void tick() { // Simulation time unit
        if (this.mode == Mode.FLOORING) {

        } else if (this.mode == mode.CYCLING) {
            for (Robot activeRobot : activeRobots) {
                System.out.printf("About to tick: " + activeRobot.toString() + "\n");
                activeRobot.tick();
            }

            robotDispatch();  // dispatch a robot if conditions are met

            // These are returning robots who shouldn't be dispatched in the previous step
            ListIterator<Robot> iter = deactivatingRobots.listIterator();
            while (iter.hasNext()) {  // In timestamp order
                Robot robot = iter.next();
                iter.remove();
                activeRobots.remove(robot);
                idleRobots.add(robot);
            }
        }

    }

    void robotDispatch() { // Can dispatch at most one robot; it needs to move out of the way for the next
        System.out.println("Dispatch at time = " + Simulation.now());
        // Need an idle robot and space to dispatch (could be a traffic jam)
        if (!idleRobots.isEmpty() && !Building.getBuilding().isOccupied(0,0)) {
            int fwei = floorWithEarliestItem();
            if (fwei >= 0) {  // Need an item or items to deliver, starting with earliest
                Robot robot = idleRobots.remove();
                loadRobot(fwei, robot);
                // Room order for left to right delivery
                robot.sort();
                activeRobots.add(robot);
                System.out.println("Dispatch @ " + Simulation.now() +
                        " of Robot " + robot.getId() + " with " + robot.numItems() + " item(s)");
                robot.place(0, 0);
            }
        }
    }

    void robotReturn(Robot robot) {
        Building building = Building.getBuilding();
        int floor = robot.getFloor();
        int room = robot.getRoom();
        assert floor == 0 && room == building.NUMROOMS+1: format("robot returning from wrong place - floor=%d, room ==%d", floor, room);
        assert robot.isEmpty() : "robot has returned still carrying at least one item";
        building.remove(floor, room);
        deactivatingRobots.add(robot);
    }

    void loadRobot(int floor, Robot robot) {
        List<Item> itemsOnFloor = waitingForDelivery[floor];
        Collections.sort(itemsOnFloor, (item1, item2) -> {
            int arrivalTime1 = item1.myArrival();
            int arrivalTime2 = item2.myArrival();
            return Integer.compare(arrivalTime1, arrivalTime2);
        });
        ListIterator<Item> iter = itemsOnFloor.listIterator();
        int capacity = robot.getCapacity();

        while (iter.hasNext()) {  // In timestamp order
            Item item = iter.next();
            if (item.myWeight() == 0) {
                robot.add(item); //Hand it over
                iter.remove();
            }
            else {
                if (item.myWeight() > 0 && item.myWeight() < robot.getCapacity()) {
                    robot.add(item);
                    robot.setCapacity(robot.getCapacity() - item.myWeight());
                    iter.remove();
                }
            }
        }
    }

}
