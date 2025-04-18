package ai;

//for games with moves that have a clearly defined destination coordinate (e.g. not blokus, because moves there cover multiple coordinates)
public interface LocalizedMove {

    int getX();

    int getY();

}
