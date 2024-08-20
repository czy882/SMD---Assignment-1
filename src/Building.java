import static java.lang.String.format;

public class Building {
    private static boolean initialised = false;
    private static Building singleton = null;
    final public int NUMFLOORS;
    final public int NUMROOMS;
    private boolean[][] occupied;
    public enum Direction {LEFT, RIGHT, UP, DOWN}

    private static BuildingGrid bg;

    private static int NUMF;
    private static int NUMR;

    public static void initialise(int numFloors, int numRooms) {
        assert !initialised : "Attempt to reinitialise Building";
        assert numFloors > 0 : "Non-positive numFloors";
        assert numRooms > 0 : "Non-positive numRooms";
        NUMF = numFloors;
        NUMR = numRooms;
        initialised = true;
    }

    private Building() {
        // System.out.println("Building constructor");
        this.NUMFLOORS = NUMF;
        this.NUMROOMS = NUMR;
        occupied = new boolean[NUMFLOORS+1][NUMROOMS+2]; // robot space in building, initialised to zero (false)
        bg = new BuildingGrid(NUMFLOORS, NUMROOMS);
    }

    public static Building getBuilding() {
        // System.out.print("getBuilding ");
        if (singleton == null) {
            // System.out.println("null");
            assert initialised : "Failure to initialise Building";
            singleton = new Building();
        } else {
            // System.out.println("not null");
        }
        return singleton;
    }

    boolean isOccupied(int floor, int room) {
        return occupied[floor][room];
    }

    void remove(int floor, int room) {
        assert occupied[floor][room] : format("remove from unoccupied position floor=%d; room=%d", floor, room);
        occupied[floor][room] = false;
        bg.update(floor, room, "");  // Display
    }

    void place(int floor, int room, String id) {
        assert !occupied[floor][room] : format("place at occupied position floor=%d; room=%d", floor, room);
        occupied[floor][room] = true;
        bg.update(floor, room, id);  // Display
    }

    void move(int floor, int room, Direction direction, String id) {
        assert occupied[floor][room] : format("move from unoccupied position floor=%d; room=%d", floor, room);
        int dfloor, droom;
        switch (direction) {
            case UP -> {
                assert floor < NUMFLOORS + 1 : format("attempt to move above building floor=%d; room=%d", floor, room);
                assert room == 0 || room == NUMROOMS + 1 : format("attempt to move up through ceiling floor=%d; room=%d", floor, room);
                dfloor = floor + 1;
                droom = room;
            }
            case DOWN -> {
                assert floor > 0 : format("attempt to move below mailroom floor=%d; room=%d", floor, room);
                assert room == 0 || room == NUMROOMS + 1 : format("attempt to move down through floor floor=%d; room=%d", floor, room);
                dfloor = floor - 1;
                droom = room;
            }
            case LEFT -> {
                assert room > 1 : format("attempt to move left outside building floor=%d; room=%d", floor, room);
                dfloor = floor;
                droom = room - 1;
            }
            case RIGHT -> {
                assert room < NUMROOMS + 1 : format("attempt to move right outside building floor=%d; room=%d", floor, room);
                dfloor = floor;
                droom = room + 1;
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + direction);
        }
        assert !occupied[dfloor][droom] : format("attempt move to occupied position floor=%d; room=%d", dfloor, droom);
        occupied[floor][room] = false;
        bg.update(floor, room, ""); // Display
        occupied[dfloor][droom] = true;
        bg.update(dfloor, droom, id); // Display
    }
}
