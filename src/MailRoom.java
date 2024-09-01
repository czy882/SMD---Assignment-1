import java.util.*;

import static java.lang.String.format;

public class MailRoom {
    protected int TRUE = Decision.TRUE.getDecision();
    protected int FALSE = Decision.FALSE.getDecision();

    public enum Mode {CYCLING, FLOORING}


    List<Item>[] waitingForDelivery;
    Queue<Robot> idleRobots;
    List<Robot> activeFloorRobots; // for floor robots which is moving
    List<Robot> activeColumnRobots; // for column robots which is moving
    List<Robot> deactivatingRobots;
    List<Robot> activeCyclingRobots;// for cycling robots which is moving

    private int capacity;
    private int numRooms;
    private Mode mode;
    private int initial = FALSE;
    private final int numRobots;


    public int getNumRooms () { return  numRooms; }
    public List<Robot> getActiveColumnRobots () { return   activeColumnRobots; }
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

    public int getCapacity() { return capacity;}

    MailRoom(int numFloors, int numRobots, int capacity, int numRooms, Mode mode) {
        this.capacity = capacity;
        this.mode = mode;
        this.numRooms = numRooms;
        this.numRobots = numRobots;

        idleRobots = new LinkedList<>();
        activeFloorRobots = new ArrayList<>();
        activeColumnRobots = new ArrayList<>();
        deactivatingRobots = new ArrayList<>();
        activeCyclingRobots = new ArrayList<>();


        waitingForDelivery = new List[numFloors];
        for (int i = 0; i < numFloors; i++) {
            waitingForDelivery[i] = new LinkedList<>();
        }

        // mode switch
        if (mode == Mode.CYCLING) {
            for (int i = 0; i < numRobots; i++)
                idleRobots.add(new CyclingRobot(MailRoom.this));
        } else if (mode == Mode.FLOORING) {
            idleRobots.add(new ColumnRobot(MailRoom.this, activeFloorRobots,numRooms));
            idleRobots.add(new ColumnRobot(MailRoom.this, activeFloorRobots,numRooms));
            Building building = Building.getBuilding();
            for (int i = 0; i < building.NUMFLOORS; i++)
                activeFloorRobots.add(new FloorRobot(MailRoom.this, numRooms,i+1,1, activeColumnRobots));
        }
    }



    // the first item with need to be delivered
    void arrive(List<Item> items) {

        // add item into waitingForDelivery based on the floor number
        for (Item item : items) {
            waitingForDelivery[item.myFloor()-1].add(item);

            System.out.printf("Item: Time = %d Floor = %d Room = %d Weight = %d\n",
                    item.myArrival(), item.myFloor(), item.myRoom(), item.myWeight());
        }
    }





    // load the items on column robot
    public void robotColumnDispatch(int length) {
        for (; length > 0; length--) {
            System.out.println("Dispatch at time = " + Simulation.now());
            int fwei = floorWithEarliestItem();
            // call robots and load items on them
            if (fwei >= 0) {
                Robot robot = idleRobots.remove();
                loadRobot(fwei, robot);

                // sort for left robots
                if (robot.getId().equals("R1")) {
                    robot.sort();
                }
                // // sort for right robots
                else if (robot.getId().equals("R2")) {
                    robot.reverseSort();
                }

                // robots activated
                activeColumnRobots.add(robot);
                System.out.println("Dispatch @ " + Simulation.now() +
                        " of Robot " + robot.getId() + " with " + robot.numItems() + " item(s)");
                if (robot.getId().equals("R1")) {
                    robot.place(0, 0);
                } else if (robot.getId().equals("R2")) {
                    robot.place(0, numRooms + 1);
                }
            }

        }
    }



    public void tick() {

        if (this.mode == Mode.FLOORING) {
            // the movement logic for floor robots
            for (Robot robot : activeFloorRobots) {
                ((FloorRobot) robot).floorRobotMovement(robot);
            }

            // floor robots dispatch
            robotFloorDispatch();


            for (Robot robot : activeColumnRobots) {
                ((ColumnRobot) robot).columnRobotMovement(robot);
            }

            // column robots dispatch
            robotColumnDispatch(idleRobots.size());

            // inactivated robot if returning
            ListIterator<Robot> iter = deactivatingRobots.listIterator();
            while (iter.hasNext()) {  // In timestamp order
                Robot robot = iter.next();
                iter.remove();
                activeColumnRobots.remove(robot);
                idleRobots.add(robot);
            }
        }


        if (this.mode == Mode.CYCLING ) {
            for (Robot activeRobot : activeCyclingRobots) {
                ((CyclingRobot) activeRobot).cyclingOutput(activeRobot);
            }

            // cycling robots dispatch
            robotCyclingDispatch();

            // inactivated robot if returning
            ListIterator<Robot> iter = deactivatingRobots.listIterator();
            while (iter.hasNext()) {
                Robot robot = iter.next();
                iter.remove();
                activeCyclingRobots.remove(robot);
                idleRobots.add(robot);
            }
        }
    }

    void robotCyclingDispatch() {

        System.out.println("Dispatch at time = " + Simulation.now());
        if (!idleRobots.isEmpty() && !Building.getBuilding().isOccupied(0, 0)) {

            int fwei = floorWithEarliestItem();

            if (fwei >= 0) {

                Robot robot = idleRobots.remove();

                loadRobot(fwei, robot);

                robot.sort();

                activeCyclingRobots.add(robot);
                System.out.println("Dispatch @ " + Simulation.now() +
                        " of Robot " + robot.getId() + " with " + robot.numItems() + " item(s)");
                robot.place(0, 0);
            }
        }
    }

    void robotFloorDispatch () {
        if (initial == FALSE) {
            initial = TRUE;
            // set floor robot to the right place
            int floorNum = 1;
            for (Robot robot : activeFloorRobots) {
                robot.place(floorNum, 1);
                floorNum += 1;
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

        // sort the items that waiting to deliver by time order
        List<Item> itemsOnFloor = waitingForDelivery[floor];

        Collections.sort(itemsOnFloor, (item1, item2) -> {
            int arrivalTime1 = item1.myArrival();
            int arrivalTime2 = item2.myArrival();
            return Integer.compare(arrivalTime1, arrivalTime2);
        });

        ListIterator<Item> iter = waitingForDelivery[floor].listIterator();

        // update robot capacity
        int remainingCapacity = robot.getCapacity();

        while (iter.hasNext()) {
            Item item = iter.next();

            // load the letter
            if (item.myWeight() == 0) {
                robot.add(item); // Hand it over
                iter.remove();
            }
            // load the parcel, update robot capacity
            else if (item.myWeight() > 0 && item.myWeight() < remainingCapacity) {
                robot.add(item); // Hand it over
                remainingCapacity -= item.myWeight(); // Decrease the remaining capacity
                iter.remove();
                robot.setCapacity(remainingCapacity);
            }

        }


    }

}
