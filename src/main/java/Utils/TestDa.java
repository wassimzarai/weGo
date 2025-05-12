package Utils;

import java.sql.Connection;

public class TestDa {

    public static void main(String[] args) {
        Datasource data1=Datasource.getInstance();

        Datasource data2=Datasource.getInstance();

        System.out.println(data1);
        System.out.println(data2);

        Connection con1=Datasource.getInstance().getCon();

        Connection con2=Datasource.getInstance().getCon();


    }
}
