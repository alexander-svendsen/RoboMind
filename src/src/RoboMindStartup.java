package src;

/**
 * The starting point of the program
 */
public class RoboMindStartup {

    public RoboMindStartup(){

    }

    public static void main(String[] args) {
        System.out.println("Started");
        //new TuneThread().start(); //Tune to know that the program has started

//        SampleThread sampleThread = new SampleThread(thread);
//        sampleThread.start();
//
//
//        System.out.println("wooot");
//        Gson gson = new GsonBuilder().create();
//
        Communication communication = new Communication();
        MonitorSensorsThread thread = new MonitorSensorsThread(communication);

//        communication.setUpConnection();

        thread.start();
        while(true){
//            gson.toJson("Hello", System.out);
//            gson.toJson(123, System.out);
        }





    }


}
