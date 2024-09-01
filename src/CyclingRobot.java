public class CyclingRobot extends Robot{
    CyclingRobot(MailRoom mailroom) {

        super(mailroom);
    }

    public void cyclingOutput(Robot activeRobot) {
        System.out.printf("About to tick: " + activeRobot.toString() + "\n"); activeRobot.tick();
    }
}
