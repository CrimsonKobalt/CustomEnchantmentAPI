package db.chris.customenchantment.mergers;

public interface Merger<T> {

    int getCost();

    T getResult();
}
