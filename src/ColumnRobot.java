import java.util.Comparator;
import java.util.List;


public class ColumnRobot extends Robot {
    protected int LEFT = Decision.LEFT.getDecision();
    protected int RIGHT = Decision.RIGHT.getDecision();

    private List<Robot> robots;

    ColumnRobot(MailRoom mailroom, List<Robot> robots, int numRoom) {

        super(mailroom);

        this.robots = robots;
        this.setNumRoom(numRoom);
    }

    public int findEarlyRobot(Robot left, Robot right) {
        // get the early arrival time at left
        int leftArrivalTime = left.getItems().stream()
                .min(Comparator.comparing(Item::myArrival)).get().myArrival();

        // get the early arrival time at right
        int rightArrivalTime = right.getItems().stream()
                .min(Comparator.comparing(Item::myArrival)).get().myArrival();

        // find the earliest one
        return (leftArrivalTime <= rightArrivalTime) ? LEFT: RIGHT;
    }


    public void columnRobotMovement (Robot robot) {
        // go up stair before deliver complete
        if (!robot.getItems().isEmpty() && robot.getFloor() != robot.getItems().get(0).myFloor()) {
            robot.move(Building.Direction.UP);
        }
        //go down stair after delivered
        else if (robot.getItems().isEmpty() && robot.getFloor() != 0) {
            robot.move(Building.Direction.DOWN);
        }
        // stay put
        else if (!robot.getItems().isEmpty() && robot.getFloor() == robot.getItems().get(0).myFloor()) { }
    }
}
