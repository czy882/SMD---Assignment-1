import java.util.List;

public class FloorRobot extends Robot {

    private int LEFT = Decision.LEFT.getDecision();
    private int RIGHT = Decision.RIGHT.getDecision();
    protected int ADJACENT = Decision.ADJACENT.getDecision();
    private int recieveLocation = -1;
    private List<Robot> robots;

    FloorRobot(MailRoom mailroom, int numRooms, int floor, int room, List<Robot> robots) {
        super(mailroom);
        this.robots = robots;
        this.setFloor(floor);
        this.setRoom(room);
        this.setNumRoom(numRooms);
    }

    public int getRecieveLocation() {
        return this.recieveLocation;
    }

    public void setRecieveLocation(int recieveLocation) {
        this.recieveLocation = recieveLocation;
    }


    public void floorRobotMovement(Robot robot) {
        System.out.printf("About to tick: " + robot.toString() + "\n");

        // do unloaded action if robots are carrying nothing
        if (robot.getItems().isEmpty()) {
            moveWithoutItems(robot);

        }
        // do loaded action if robots are carrying items
        else {
            moveWithItems (robot);
        }
    }

    public void moveWithItems (Robot robot) {
        // deliver items
        if (robot.getFloor() == robot.getItems().getFirst().myFloor()
                && robot.getRoom() == robot.getItems().getFirst().myRoom()) {
            while (!robot.getItems().isEmpty() && robot.getRoom() == robot.getItems().getFirst().myRoom()) {
                Item firstItem = robot.getItems().get(0);
                robot.setCapacity(robot.getCapacity()+ firstItem.myWeight());
                Simulation.deliver(robot.getItems().removeFirst());
            }
        }
        // keep moving, if robots have not arrived
        else if (((FloorRobot) robot).getRecieveLocation() == 0) {
            robot.move(Building.Direction.RIGHT);
        }

        else if (((FloorRobot) robot).getRecieveLocation() == 1) {
            robot.move(Building.Direction.LEFT);
        }
    }

    public void moveWithoutItems (Robot robot){

        // if the waiting one is on the left and close, transfer
        if (robot.waitingRobot(robot, getMailroom().getActiveColumnRobots()) == LEFT
                && robot.getRoom() == ADJACENT) {
            Robot targetRobot = null;
            for (Robot robot2 : getMailroom().getActiveColumnRobots()) {
                if (robot2.getId().equals("R1")) {
                    targetRobot = robot2;
                    break;
                }
            }
            if (targetRobot != null) {
                robot.transfer(targetRobot);
            }
            ((FloorRobot) robot).setRecieveLocation(0);
        }

        // if the waiting one is on the right and close, transfer
        else if (robot.waitingRobot(robot, getMailroom().getActiveColumnRobots()) == RIGHT
                && robot.getRoom() == getMailroom().getNumRooms()) {
            Robot targetRobot = null;
            for (Robot r : getMailroom().getActiveColumnRobots()) {
                if (r.getId().equals("R2")) {
                    targetRobot = r;
                    break;
                }
            }
            if (targetRobot != null) {
                robot.transfer(targetRobot);
            }
            ((FloorRobot) robot).setRecieveLocation(1);

        }

        // if the waiting one is on the left but not close, moving leftward
        else if (robot.waitingRobot(robot, getMailroom().getActiveColumnRobots())
                == LEFT && robot.getRoom() != ADJACENT) {
            robot.move(Building.Direction.LEFT);
        }

        // if the waiting one is on the right but not close, moving rightward
        else if (robot.waitingRobot(robot, getMailroom().getActiveColumnRobots())
                == RIGHT && robot.getRoom() != getMailroom().getNumRooms()) {

            robot.move(Building.Direction.RIGHT);
        }
    }


}