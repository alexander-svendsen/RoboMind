package src;

public interface SensorEventListener {
    void newSensor(String sensorClassName, int portNumber);
    void newInfo(String cmd, int portNumber);
    void newSamples(float[][] sampleArray);
}
