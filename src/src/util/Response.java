package src.util;

public class Response {
    public String msg = "ok";
    public float data = 0;
    public int seq = 0;

    public float[] sample = null;
    public String sample_string = "";


    public void reset(){
        data = 0;
        sample = null;
        sample_string = "";
    }

}
