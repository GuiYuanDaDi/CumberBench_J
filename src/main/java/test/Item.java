package test;

public class Item {
    int id;
    // 0: insert, 1: delete, 4: no-op
    int opt;

    public Item(int id, int opt) {
        this.id = id;
        this.opt = opt;
    }
    
}