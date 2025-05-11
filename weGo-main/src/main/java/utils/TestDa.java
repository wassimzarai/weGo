package utils;

import java.sql.Connection;

public class TestDa {

    public static void main(String[] args) {
        database data1=database.getInstance();

        database data2=database.getInstance();

        System.out.println(data1);
        System.out.println(data2);

        Connection con1=database.getInstance().connectDb();

        Connection con2=database.getInstance().connectDb();


    }
}
