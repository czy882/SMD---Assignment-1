public class Parcel extends Item {

    Parcel(int floor, int room, int arrival, int weight) {
        super(floor, room, arrival);
        this.setWeight(weight);
    }



}
