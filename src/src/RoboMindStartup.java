package src;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class RoboMindStartup {

    public RoboMindStartup(){

    }

    public static void main(String[] args) {
        System.out.println("SASATAN");
        //new TuneThread().start(); //Tune to know that the program has started
        MonitorSensorsThread thread = new MonitorSensorsThread();
        thread.start();

        SampleThread sampleThread = new SampleThread(thread);
        sampleThread.start();


        System.out.println("wooot");
        Gson gson = new GsonBuilder().create();

        while(true){
            gson.toJson("Hello", System.out);
            gson.toJson(123, System.out);
        }




    }


}
