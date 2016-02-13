package com.main.robotcontroller;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Rafał on 2016-01-31.
 */
public class ComputedInverseCinematics {
    public static double THETA_1=0, THETA_2=0, D_3=0;

    public static void setTheta1(double theta1) {
        THETA_1 = theta1;
    }

    public static boolean ComputeInverseCinematics(double px, double py, double pz, double length_1, double length_2)
    {
        double[] theta_1 =new double[2];
        double[] theta_2=new double[2];
        {
            D_3=Math.sqrt(Math.pow(px,2)+Math.pow(py,2)-Math.pow(length_2,2)+Math.pow(length_1-pz,2));
            D_3=(D_3-100)/0.3;
            if(!ComputedInverseCinematics.inRange(-90,90,D_3)) return false;
            theta_1[0]=Math.toDegrees(-Math.atan2(px,py)-Math.atan2(Math.sqrt(Math.pow(px,2)+Math.pow(py,2)-Math.pow(length_2,2)),length_2)); //-- -+
            theta_2[0]=Math.toDegrees(+Math.atan2(Math.sqrt(Math.pow(px,2)+Math.pow(py,2)-Math.pow(length_2,2)),length_1-pz)); //+-
            theta_1[1]=Math.toDegrees(-Math.atan2(px,py)+Math.atan2(Math.sqrt(Math.pow(px,2)+Math.pow(py,2)-Math.pow(length_2,2)),length_2)); //-- -+
            theta_2[1]=Math.toDegrees(-Math.atan2(Math.sqrt(Math.pow(px,2)+Math.pow(py,2)-Math.pow(length_2,2)),length_1-pz)); //+-


            if(Double.isNaN(theta_1[0]) || Double.isNaN(theta_1[1]) || Double.isNaN(theta_2[0]) || Double.isNaN(theta_2[1])) {
                return false;
            } else {

                double temp_THETA_1=THETA_1;
            if(Math.abs(THETA_1-theta_1[0])<=Math.abs(THETA_1-theta_1[1]))
            {
                THETA_1=theta_1[0];
                THETA_2=theta_2[0];
            }
            else
            {
                THETA_1=theta_1[1];
                THETA_2=theta_2[1];
            }

            if(ComputedInverseCinematics.inRange(-90,90,THETA_1) && ComputedInverseCinematics.inRange(-90,90,THETA_2)) {
                return true;
            } else {
                THETA_1=temp_THETA_1;
                return false;
            }
            }

        }
    }

    public static double getD3() {
        return D_3;
    }

    public static double getTheta1() {
        return THETA_1;
    }

    public static double getTheta2() {
        return THETA_2;
    }

    private static boolean inRange(double min, double max, double val) {
        return (min<=val) && (max>=val);
    }
}
