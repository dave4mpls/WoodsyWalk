package com.davewhitesoftware.woodsywalk;

import java.io.Serializable;

//  Simple coordinate class for use by various other modules.

public class Coordinates implements Serializable {
    // Coordinate class for use with some return values.
    static final long serialVersionUID = 1L;
    private int x;
    private int y;
    private boolean notFound;

    Coordinates(int ax, int ay, boolean aNotFound) {
        this.x = ax;
        this.y = ay;
        this.notFound = aNotFound;
    }
    Coordinates(int ax, int ay) {
        this.x = ax;
        this.y = ay;
        this.notFound = false;
    }
    public boolean equals(Coordinates b) {
        return (this.x == b.x && this.y == b.y && this.notFound == b.notFound);
    }
    public int x() { return this.x; }
    public int y() { return this.y; }
    public boolean notFound() { return this.notFound; }
}
