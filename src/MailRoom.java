import java.util.*;

import static java.lang.String.format;

public class MailRoom {
    public enum Mode {CYCLING, FLOORING}

    private Mode mode;
    private int robotCapacity;
    List<Item>[] waitingForDelivery;
    private final int numRobots;

    private final static int LEFT = 0;
    private final static int RIGHT = 1;
    private final static int ADJACENT = 1;
    private int initial = 0;
    private int numRooms;

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

    MailRoom(int numRooms, int numFloors, int numRobots, Mode mode, int robotCapacity) {
        this.mode = mode;
        this.robotCapacity = robotCapacity;
        this.numRooms = numRooms;
        waitingForDelivery = new List[numFloors];

        for (int i = 0; i < numFloors; i++) {
            waitingForDelivery[i] = new LinkedList<>();
        }
        this.numRobots = numRobots;

        deactivatingRobots = new ArrayList<>();
        activeFloorRobots = new ArrayList<>();
        activeRobots = new ArrayList<>();
        idleRobots = new LinkedList<>();

        if (this.mode == mode.FLOORING) {
            idleRobots.add(new FloorRobot(MailRoom.this, this.robotCapacity, activeRobots,numRooms));
            idleRobots.add(new FloorRobot(MailRoom.this, this.robotCapacity, activeRobots,numRooms));
            Building building = Building.getBuilding();
            for (int i = 0; i < building.NUMFLOORS; i++) {
                activeRobots.add(new RoomRobot(MailRoom.this, this.robotCapacity,numRooms,i+1,1, activeFloorRobots));
            }

        } else if (this.mode == mode.CYCLING) {
            for (int i = 0; i < numRobots; i++)
                idleRobots.add(new Robot(MailRoom.this, this.robotCapacity));  // In mailroom, floor/room is not significant
        }
    }


    void arrive(List<Item> items) {
        for (Item item : items) {
            waitingForDelivery[item.myFloor() - 1].add(item);
            System.out.printf("Item: Time = %d Floor = %d Room = %d Weight = %d\n",
                    item.myArrival(), item.myFloor(), item.myRoom(), item.myWeight());
        }
    }

    public int waitingRobot(Robot waitRoomRobot) {
        Robot leftOne = null;
        Robot rightOne = null;
        boolean isRobotLeft = false;
        boolean isRobotRight = false;

        for (Robot robot : activeFloorRobots) {
            if (!robot.getItems().isEmpty() && robot.getFloor() == waitRoomRobot.getFloor()) {
                if (waitRoomRobot.getFloor() == robot.getItems().getFirst().myFloor()) {
                    if (robot.getRoom() == LEFT) {
                        leftOne = robot;
                        isRobotLeft = true;
                    } else if (robot.getRoom() == Building.getBuilding().NUMROOMS + 1) {
                        rightOne = robot;
                        isRobotRight = true;
                    }
                }
            }
        }
        if (isRobotLeft && !isRobotRight) {
            return LEFT;
        } else if (!isRobotLeft && isRobotRight) {
            return RIGHT;
        } else if (isRobotLeft && isRobotRight) {
            return findEarlyRobot(leftOne, rightOne) == LEFT ? LEFT : RIGHT;
        }
        return -1;
    }


    public int findEarlyRobot(Robot left, Robot right) {
        // get the early arrival time at left
        int leftArrivalTime = left.getItems().stream()
                .min(Comparator.comparing(Item::myArrival)).get().myArrival();

        // get the early arrival time at right
        int rightArrivalTime = right.getItems().stream()
                .min(Comparator.comparing(Item::myArrival)).get().myArrival();

        // find the earliest one
        return (leftArrivalTime <= rightArrivalTime) ? LEFT : RIGHT;
    }


    public void tick() { // Simulation time unit
        if (this.mode == Mode.FLOORING) {
            // RoomRobot behaviour
            for (Robot robot : activeRobots) {
                // System.out.println("Robot type: " + robot.getClass().getName());
                //System.out.println (robot.numItems());
                //System.out.println (robot.getFloor() + " " + robot.getRoom());
                System.out.println("About to tick: " + robot.toString());

                // stay or walk to the floor robot if there is no items loaded on robot
                if (robot.getItems().isEmpty()) {
                    // if there is a Robot waiting, transfer if it is close to the room robot
                    if (waitingRobot(robot) == LEFT) {
                        // robot waiting at left side
                        if (robot.getRoom() == ADJACENT) {
                            Robot targetRobot = null;
                            for (Robot r : activeFloorRobots) {
                                // System.out.println("Robot type: " + r.getClass().getName());
                                if (r.getId().equals("R1")) {
                                    targetRobot = r;
                                    break;
                                }
                            }
                            if (targetRobot != null) {
                                robot.transfer(targetRobot);
                            }
                            // System.out.println("Robot type 2222222: " + robot.getClass().getName());
                            ((RoomRobot) robot).setRecieveLocation(0);
                        }
                    } else if (waitingRobot(robot) == RIGHT) {
                        if (robot.getRoom() == Building.getBuilding().NUMROOMS) {
                            Robot targetRobot = null;
                            for (Robot robot2 : activeFloorRobots) {
                                if (robot2.getId().equals("R2")) {
                                    targetRobot = robot2;
                                    break;
                                }
                            }

                            if (targetRobot != null) {
                                robot.transfer(targetRobot);
                            }

                            ((RoomRobot) robot).setRecieveLocation(RIGHT);
                        }
                    } else if (waitingRobot(robot) == LEFT && robot.getRoom() != RIGHT) {
                        robot.move(Building.Direction.LEFT);
                    } else if (this.waitingRobot(robot) == RIGHT && robot.getRoom() != Building.getBuilding().NUMROOMS) {
                        robot.move(Building.Direction.RIGHT);
                    }
                } else {
                    if (robot.getFloor() == robot.getItems().getFirst().myFloor()) {
                        if (robot.getRoom() == robot.getItems().getFirst().myRoom()) {
                            do {
                                Item firstItem = robot.getItems().get(0);
                                robot.setCapacity(robot.getCapacity() + firstItem.myWeight());
                                Simulation.deliver(robot.getItems().removeFirst());
                            } while (!robot.getItems().isEmpty() && robot.getRoom() == robot.getItems().getFirst().myRoom());

                        } else if (((RoomRobot) robot).getRecieveLocation() == LEFT) {
                            robot.move(Building.Direction.RIGHT);
                        } else if (((RoomRobot) robot).getRecieveLocation() == RIGHT) {
                            robot.move(Building.Direction.LEFT);
                        }
                    }
                }
            }

            robotDispatch();

            for (Robot robot : activeFloorRobots) {
                if (!robot.isEmpty() && robot.getFloor() != robot.getItems().get(0).myFloor()) {
                    robot.move(Building.Direction.UP);
                } else if (!robot.getItems().isEmpty() && robot.getFloor() == robot.getItems().get(0).myFloor()) {
                } else if (robot.getItems().isEmpty() && robot.getFloor() != 0) {
                    robot.move(Building.Direction.DOWN);
                }
            }

            int length = idleRobots.size();
            while (length > 0) {
                System.out.println("Dispatch at time = " + Simulation.now());
                int fwei = floorWithEarliestItem();
                if (fwei >= 0) {  // Need an item or items to deliver, starting with earliest
                    Robot robot = idleRobots.remove();
                    length -= 1;
                    loadRobot(fwei, robot);
                    // Room order for left to right delivery
                    if (robot.getId().equals("R1")) {
                        robot.sort();
                    } else if (robot.getId().equals("R2")) {
                        robot.reverseSort();
                    }
                    activeFloorRobots.add(robot);
                    System.out.println("Dispatch @ " + Simulation.now() +
                            " of Robot " + robot.getId() + " with " + robot.numItems() + " item(s)");
                    if (robot.getId().equals("R1")) {
                        robot.place(0, 0);
                    } else if (robot.getId().equals("R2")) {
                        robot.place(0, Building.getBuilding().NUMROOMS + 1);
                    }
                }

            }

            ListIterator<Robot> iter = deactivatingRobots.listIterator();
            while (iter.hasNext()) {  // In timestamp order
                Robot robot = iter.next();
                iter.remove();
                activeFloorRobots.remove(robot);
                idleRobots.add(robot);
            }

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
        if (this.mode == mode.CYCLING) {
            System.out.println("Dispatch at time = " + Simulation.now());
            // Need an idle robot and space to dispatch (could be a traffic jam)
            if (!idleRobots.isEmpty() && !Building.getBuilding().isOccupied(0, 0)) {
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
        if (this.mode == mode.FLOORING) {
            if (initial == 0) {
                initial = 1;
                int floorNum = 1;
                for (Robot robot : activeRobots) {
                    robot.place(floorNum, 1);
                    floorNum += 1;
                }
            }
        }
    }

    void robotReturn(Robot robot) {
        Building building = Building.getBuilding();
        int floor = robot.getFloor();
        int room = robot.getRoom();
        assert floor == 0 && room == building.NUMROOMS + 1 : format("robot returning from wrong place - floor=%d, room ==%d", floor, room);
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

        int robotRemainCapacity = robot.getCapacity();

        while (iter.hasNext()) {  // In timestamp order
            Item item = iter.next();
            if (item.myWeight() == 0) {
                robot.add(item); //Hand it over
                iter.remove();
            } else if (item.myWeight() > 0 && item.myWeight() < robotRemainCapacity) {
                robot.add(item);
                robotRemainCapacity -= item.myWeight();
                iter.remove();
                robot.setCapacity(robotRemainCapacity);
            }
        }
    }
}


