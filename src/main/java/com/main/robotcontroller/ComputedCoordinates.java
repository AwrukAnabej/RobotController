package com.main.robotcontroller;

/**
 * Created by Rafał on 2016-01-30.
 */
public class ComputedCoordinates {
    private static double px, py, pz;

    public static void ComputeCoordinates(double t1, double t2, double d_3,  double l_1, double l_2)
    {

        px=(-l_2*Math.sin(t1*Math.PI/180)-d_3*Math.cos(t1*Math.PI/180)*Math.sin(t2*Math.PI/180));
        py=(l_2*Math.cos(t1*Math.PI/180)-d_3*Math.sin(t1*Math.PI/180)*Math.sin(t2*Math.PI/180));
        pz=(l_1-d_3*Math.cos(t2*Math.PI/180));
    }

    public static double getPx() {
        return px;
    }
    public static double getPy()
    {
        return py;
    }
    public static double getPz() { return pz; }
}
